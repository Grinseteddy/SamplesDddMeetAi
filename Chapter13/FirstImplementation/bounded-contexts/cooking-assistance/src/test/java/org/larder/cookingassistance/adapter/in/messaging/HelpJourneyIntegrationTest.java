package org.larder.cookingassistance.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.HELP_REQUEST_ID;
import static org.larder.cookingassistance.TestData.burningCatastrophe;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.HelpRequestService;
import org.larder.cookingassistance.application.HelpService;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.larder.platform.messaging.OutboxRelay;
import org.larder.platform.messaging.Topology;
import org.larder.platform.test.AsyncApiContract;
import org.larder.platform.test.TestBroker;
import org.larder.platform.test.TestDatabase;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

/**
 * The help journey across the broker: this context's real beans (use cases with their transactions,
 * JDBC repositories, outbox, relay, listener) against PostgreSQL and RabbitMQ in containers. The REST
 * adapters are left out; the use cases are called directly and the listener method is invoked with a
 * message as the broker delivers it.
 */
@SpringJUnitConfig(HelpJourneyIntegrationTest.Wiring.class)
class HelpJourneyIntegrationTest {

    private static final AsyncApiContract CONTRACT = AsyncApiContract.of("cooking-assistance.asyncapi.yaml");

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @ComponentScan(basePackages = "org.larder.cookingassistance", excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {RestController.class, RestControllerAdvice.class}),
            @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Test.*")})
    static class Wiring {

        @Bean
        ConnectionFactory connectionFactory() {
            return TestBroker.connectionFactory();
        }

        @Bean
        RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
            return new RabbitTemplate(connectionFactory);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        var database = TestDatabase.forSchema("cookingassistance");
        String url = ((HikariDataSource) database.dataSource()).getJdbcUrl();
        registry.add("larder.cooking-assistance.database.url", () -> url);
        registry.add("larder.cooking-assistance.database.schema", () -> "cookingassistance");
        registry.add("larder.cooking-assistance.database.username", () -> "cookingassistance");
        registry.add("larder.cooking-assistance.database.password", () -> "cookingassistance");
    }

    @Autowired
    private HelpRequestService requests;
    @Autowired
    private HelpService helps;
    @Autowired
    private OutboxRelay relay;
    @Autowired
    private GrandmaHelpProvidedListener listener;
    @Autowired
    private ObjectMapper json;

    private final RabbitTemplate rabbit = TestBroker.rabbitTemplate();
    private String queue;

    /** A test queue on the cooking-assistance exchange, as Notification would bind one. */
    @BeforeEach
    void bindTestQueue() {
        RabbitAdmin admin = TestBroker.admin();
        admin.declareExchange(Topology.exchange("cooking-assistance"));
        queue = "cooking-assistance.journey-test." + UUID.randomUUID();
        admin.declareQueue(new Queue(queue, true, false, false));
        admin.declareBinding(BindingBuilder.bind(new Queue(queue)).to(Topology.exchange("cooking-assistance"))
                .with("cooking-assistance.help.*"));
    }

    @AfterEach
    void removeTestQueue() {
        TestBroker.admin().deleteQueue(queue);
    }

    /** Relays the outbox explicitly (the relay also polls on its own) and collects what arrived. */
    private List<Message> published() {
        relay.relay();
        List<Message> messages = new ArrayList<>();
        Message message = rabbit.receive(queue, 3000);
        while (message != null) {
            messages.add(message);
            message = rabbit.receive(queue, 500);
        }
        return messages;
    }

    private static String body(Message message) {
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }

    private static void assertContractHeaders(String messageKey, Message message) {
        assertThatCode(() -> CONTRACT.assertHeaders(messageKey, new HashMap<>(message.getMessageProperties().getHeaders())))
                .doesNotThrowAnyException();
    }

    private Message grandmaMessage(String payload, UUID correlationId) {
        return ContractMessage.of("HelpProvided", MessageHeader.newMessage(correlationId, "grandma-avatar"), payload);
    }

    private void deliver(Message message) throws Exception {
        listener.onHelpProvided(json.readValue(message.getBody(), GrandmaHelpProvidedPayload.class), message);
    }

    @Test
    void raisingAHelpRequestPublishesHelpRequested() {
        HelpRequest request = requests.raise(COOK, burningCatastrophe());

        List<Message> messages = published();

        assertThat(messages).hasSize(1);
        Message message = messages.getFirst();
        assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo("cooking-assistance.help.requested");
        assertThat(ContractMessage.messageType(message)).isEqualTo("HelpRequested");
        assertThat(ContractMessage.header(message).correlationId()).isEqualTo(request.id().value());
        assertThat(ContractMessage.header(message).source()).isEqualTo("cooking-assistance");
        assertContractHeaders("helpRequested", message);
        assertThatCode(() -> CONTRACT.assertPayload("helpRequested", body(message))).doesNotThrowAnyException();
        assertThat(body(message)).contains(request.id().value().toString());
    }

    @Test
    void theGrandmaAvatarsHelpAnswersTheRequestOnceAndIsPublishedOnce() throws Exception {
        HelpRequest request = requests.raise(COOK, burningCatastrophe());
        published();
        UUID helpId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        String payload = GrandmaMessages.STAY_CALM
                .replace("a9caf90d-00b4-4a66-8184-7d02152e8d6a", helpId.toString())
                .replace(HELP_REQUEST_ID.toString(), request.id().value().toString());

        deliver(grandmaMessage(payload, correlationId));
        deliver(grandmaMessage(payload, correlationId));   // at-least-once: the broker delivers it again

        assertThat(requests.helpRequest(request.id()).status()).isEqualTo(HelpRequestStatus.ANSWERED);
        assertThat(helps.helps(COOK, request.id(), null, null)).hasSize(1)
                .allSatisfy(help -> assertThat(help.id().value()).isEqualTo(helpId));
        List<Message> messages = published();
        assertThat(messages).hasSize(1);
        Message message = messages.getFirst();
        assertThat(message.getMessageProperties().getReceivedRoutingKey()).isEqualTo("cooking-assistance.help.provided");
        assertThat(ContractMessage.messageType(message)).isEqualTo("HelpProvided");
        assertThat(ContractMessage.header(message).correlationId()).isEqualTo(correlationId);
        assertContractHeaders("helpProvided", message);
        assertThatCode(() -> CONTRACT.assertPayload("helpProvided", body(message))).doesNotThrowAnyException();
        assertThat(body(message)).contains("\"helpProviderType\":\"GRANDMA_AVATAR\"").contains(helpId.toString());
    }

    @Test
    void aGrandmaHelpThatBreaksARuleIsAcknowledgedWithoutAnyChange() throws Exception {
        HelpRequest request = requests.raise(COOK, burningCatastrophe());
        published();
        String otherRecipe = GrandmaMessages.STAY_CALM
                .replace("a9caf90d-00b4-4a66-8184-7d02152e8d6a", UUID.randomUUID().toString())
                .replace(HELP_REQUEST_ID.toString(), request.id().value().toString())
                .replace("7cf09822-77a1-46bb-812f-b7852bca0913", UUID.randomUUID().toString());
        String unknownRequest = GrandmaMessages.STAY_CALM
                .replace("a9caf90d-00b4-4a66-8184-7d02152e8d6a", UUID.randomUUID().toString())
                .replace(HELP_REQUEST_ID.toString(), UUID.randomUUID().toString());

        deliver(grandmaMessage(otherRecipe, request.id().value()));
        deliver(grandmaMessage(unknownRequest, UUID.randomUUID()));

        assertThat(requests.helpRequest(request.id()).status()).isEqualTo(HelpRequestStatus.OPEN);
        assertThat(helps.helps(COOK, request.id(), null, null)).isEmpty();
        assertThat(published()).isEmpty();
    }
}
