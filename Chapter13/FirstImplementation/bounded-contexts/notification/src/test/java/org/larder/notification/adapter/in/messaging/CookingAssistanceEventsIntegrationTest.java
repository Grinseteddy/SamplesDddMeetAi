package org.larder.notification.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP;
import static org.larder.notification.TestData.HELP_REQUEST;
import static org.larder.notification.TestData.OTHER_COOK;
import static org.larder.notification.adapter.in.messaging.CookingAssistanceQueuesConfiguration.HELP_PROVIDED_QUEUE;
import static org.larder.notification.adapter.in.messaging.CookingAssistanceQueuesConfiguration.HELP_REQUESTED_QUEUE;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.larder.platform.messaging.Topology;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.AsyncApiContract;
import org.larder.platform.test.TestBroker;
import org.larder.platform.test.TestDatabase;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.zaxxer.hikari.HikariDataSource;

/**
 * The listeners of this module against a real RabbitMQ and PostgreSQL: messages are published to the
 * {@code cooking-assistance} exchange as Cooking Assistance publishes them and arrive through the
 * queues declared with {@link Topology#consumerQueue}, read by the application's message converter.
 * Failing messages are not requeued, so anything that is not acknowledged lands in {@code <queue>.dlq}.
 */
@SpringJUnitConfig(CookingAssistanceEventsIntegrationTest.Context.class)
class CookingAssistanceEventsIntegrationTest {

    private static final String EXCHANGE = "cooking-assistance";
    private static final String HELP_PROVIDED_KEY = "cooking-assistance.help.provided";
    private static final String HELP_REQUESTED_KEY = "cooking-assistance.help.requested";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final AsyncApiContract contract = AsyncApiContract.of(ContractExamples.CONTRACT);
    private final RabbitTemplate rabbit = TestBroker.rabbitTemplate();
    private final RabbitAdmin admin = TestBroker.admin();

    @Autowired
    private BoundedContextDatabase database;

    @BeforeAll
    static void declareTopology() {
        RabbitAdmin admin = TestBroker.admin();
        declare(admin, Topology.consumerQueue(HELP_PROVIDED_QUEUE, EXCHANGE, HELP_PROVIDED_KEY));
        declare(admin, Topology.consumerQueue(HELP_REQUESTED_QUEUE, EXCHANGE, HELP_REQUESTED_KEY));
    }

    private static void declare(RabbitAdmin admin, Declarables declarables) {
        declarables.getDeclarablesByType(Exchange.class).forEach(admin::declareExchange);
        declarables.getDeclarablesByType(Queue.class).forEach(admin::declareQueue);
        declarables.getDeclarablesByType(Binding.class).forEach(admin::declareBinding);
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        var migrated = TestDatabase.forSchema("notification");
        String url = ((HikariDataSource) migrated.dataSource()).getJdbcUrl();
        registry.add("larder.notification.database.url", () -> url);
        registry.add("larder.notification.database.schema", () -> "notification");
        registry.add("larder.notification.database.username", () -> "notification");
        registry.add("larder.notification.database.password", () -> "notification");
        registry.add("larder.notification.database.pool-size", () -> "2");
    }

    @BeforeEach
    void emptyQueuesAndTables() {
        for (String queue : new String[] {HELP_PROVIDED_QUEUE, HELP_REQUESTED_QUEUE}) {
            admin.purgeQueue(queue, false);
            admin.purgeQueue(queue + ".dlq", false);
        }
        database.jdbcClient().sql("delete from notification_receiver").update();
        database.jdbcClient().sql("delete from notification").update();
    }

    @Test
    void aHelpProvidedDeliveredTwiceNotifiesTheRequesterExactlyOnce() {
        UUID messageId = UUID.randomUUID();
        String stayCalm = helpProvided(HELP, COOK.value(), "Stay calm");

        publishHelpProvided(messageId, stayCalm);
        publishHelpProvided(messageId, stayCalm);
        UUID marker = publishMarker();

        assertThat(notificationsFrom(messageId)).isEqualTo(1);
        assertThat(database.jdbcClient().sql("""
                select count(*) from notification n join notification_receiver r using (notification_id)
                 where n.source_message_id = :id and r.receiver = :cook and r.status = 'NEW'
                   and n.link = 'https://larder.org/cooking-assistance/helps/' || :help
                """).param("id", messageId).param("cook", COOK.value()).param("help", HELP.toString())
                .query(Long.class).single()).isEqualTo(1);
        assertThat(notificationsFrom(marker)).isEqualTo(1);
        assertThat(deadLettered(HELP_PROVIDED_QUEUE)).isZero();
    }

    @Test
    void theContractsHelpRequestedExampleIsConsumedWithoutANotification() throws InterruptedException {
        var example = ContractExamples.example("helpRequested", "burningCatastrophe");
        String payload = ContractExamples.payloadJson(example);
        contract.assertPayload("helpRequested", payload);

        rabbit.send(EXCHANGE, HELP_REQUESTED_KEY, ContractExamples.message("HelpRequested", example));

        awaitTrue(() -> admin.getQueueInfo(HELP_REQUESTED_QUEUE).getMessageCount() == 0);
        Thread.sleep(500);
        assertThat(deadLettered(HELP_REQUESTED_QUEUE)).isZero();
        assertThat(database.jdbcClient().sql("select count(*) from notification").query(Long.class).single()).isZero();
    }

    @Test
    void unreadableAndIncompleteMessagesAreAcknowledgedNotDeadLettered() {
        UUID unreadable = UUID.randomUUID();
        rabbit.send(EXCHANGE, HELP_PROVIDED_KEY, MessageBuilder.withBody("not json".getBytes(StandardCharsets.UTF_8))
                .andProperties(ContractMessage.of("HelpProvided", header(unreadable), "{}").getMessageProperties()).build());
        rabbit.send(EXCHANGE, HELP_PROVIDED_KEY, ContractMessage.of("HelpProvided", header(UUID.randomUUID()),
                helpProvided(HELP, COOK.value(), "Stay calm").replace("\"GRANDMA_AVATAR\"", "\"ROBOT\"")));
        rabbit.send(EXCHANGE, HELP_PROVIDED_KEY, ContractMessage.of("HelpProvided", header(UUID.randomUUID()),
                "{\"helpId\":\"" + HELP + "\",\"answerTitle\":\"Stay calm\"}"));

        UUID marker = publishMarker();

        assertThat(notificationsFrom(marker)).isEqualTo(1);
        assertThat(database.jdbcClient().sql("select count(*) from notification").query(Long.class).single()).isEqualTo(1);
        assertThat(deadLettered(HELP_PROVIDED_QUEUE)).isZero();
    }

    /** A second, different HelpProvided; once it is stored, everything published before it was handled. */
    private UUID publishMarker() {
        UUID marker = UUID.randomUUID();
        publishHelpProvided(marker, helpProvided(UUID.randomUUID(), OTHER_COOK.value(), "Use a cold pan"));
        awaitTrue(() -> notificationsFrom(marker) == 1);
        return marker;
    }

    private void publishHelpProvided(UUID messageId, String payload) {
        contract.assertPayload("helpProvided", payload);
        contract.assertHeaders("helpProvided", header(messageId).asAmqpHeaders());
        Message message = ContractMessage.of("HelpProvided", header(messageId), payload);
        rabbit.send(EXCHANGE, HELP_PROVIDED_KEY, message);
    }

    private static MessageHeader header(UUID messageId) {
        return new MessageHeader(HELP_REQUEST, messageId, "cooking-assistance");
    }

    private static String helpProvided(UUID helpId, UUID requester, String answerTitle) {
        return """
                {"helpId":"%s","helpRequest":"%s","helpRequester":"%s","answerTitle":"%s",
                 "helpProviderType":"GRANDMA_AVATAR","helpRequestStatus":"ANSWERED",
                 "answer":{"answerType":"STEPS_TO_MITIGATE_CATASTROPHE",
                           "catastropheMitigation":{"explanation":"use a new, cold pan","answerType":"STEPS_TO_MITIGATE_CATASTROPHE"}}}
                """.formatted(helpId, HELP_REQUEST, requester, answerTitle);
    }

    private long notificationsFrom(UUID messageId) {
        return database.jdbcClient().sql("select count(*) from notification where source_message_id = :id")
                .param("id", messageId).query(Long.class).single();
    }

    private long deadLettered(String queue) {
        return admin.getQueueInfo(queue + ".dlq").getMessageCount();
    }

    private static void awaitTrue(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Condition not met within " + TIMEOUT);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableRabbit
    @EnableTransactionManagement
    @ComponentScan(basePackages = {
            "org.larder.notification.application",
            "org.larder.notification.adapter.in.messaging",
            "org.larder.notification.adapter.out"})
    static class Context {

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }

        /** As the application configures listeners, but without retries and without requeueing. */
        @Bean
        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
            Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
            converter.setAlwaysConvertToInferredType(true);
            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(TestBroker.connectionFactory());
            factory.setMessageConverter(converter);
            factory.setDefaultRequeueRejected(false);
            factory.setConcurrentConsumers(1);
            factory.setPrefetchCount(1);
            return factory;
        }
    }
}
