package org.larder.grandmaavatar;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.adapter.in.messaging.HelpRequestedListener;
import org.larder.grandmaavatar.adapter.out.advisor.RecipeBoxAdvisor;
import org.larder.grandmaavatar.adapter.out.messaging.OutboxHelpPublisher;
import org.larder.grandmaavatar.adapter.out.persistence.JdbcHandledHelpRequests;
import org.larder.grandmaavatar.application.HandledRequestRecorder;
import org.larder.grandmaavatar.application.HelpRequestHandler;
import org.larder.grandmaavatar.domain.HelpRequestId;
import org.larder.grandmaavatar.domain.Outcome;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.larder.platform.messaging.Outbox;
import org.larder.platform.messaging.OutboxRelay;
import org.larder.platform.messaging.Topology;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.AsyncApiContract;
import org.larder.platform.test.TestBroker;
import org.larder.platform.test.TestDatabase;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Grandma end to end on a real broker and database: HelpRequested on the {@code cooking-assistance}
 * exchange -> listener -> note + outbox in one transaction -> relay -> HelpProvided on the
 * {@code grandma-avatar} exchange, with the request's correlationId.
 */
class GrandmaAvatarMessagingIntegrationTest {

    private static final String REQUESTS_QUEUE = "grandma-avatar.help-requested";

    private final ObjectMapper json = new ObjectMapper();
    private final AsyncApiContract contract = AsyncApiContract.of("grandma-avatar.asyncapi.yaml");
    private BoundedContextDatabase database;
    private JdbcHandledHelpRequests notebook;
    private HelpRequestedListener listener;
    private OutboxRelay relay;
    private RabbitTemplate rabbit;
    private String answers;
    private SimpleMessageListenerContainer container;

    @BeforeEach
    void setUp() {
        database = TestDatabase.forSchema("grandmaavatar");
        var transactionManager = new DataSourceTransactionManager(database.dataSource());
        notebook = new JdbcHandledHelpRequests(database.jdbcClient());
        var publisher = new OutboxHelpPublisher(new Outbox(database.jdbcClient(), json, "grandma-avatar"));
        var recorder = Transactions.transactional(new HandledRequestRecorder(notebook, publisher), transactionManager);
        listener = new HelpRequestedListener(
                new HelpRequestHandler(notebook, new RecipeBoxAdvisor(), recorder, Clock.systemUTC()), json);
        rabbit = TestBroker.rabbitTemplate();
        relay = new OutboxRelay("grandma-avatar", database.jdbcClient(), rabbit, transactionManager, Duration.ofMillis(200));

        RabbitAdmin admin = TestBroker.admin();
        for (Declarable declarable : Topology.consumerQueue(REQUESTS_QUEUE, "cooking-assistance",
                "cooking-assistance.help.requested").getDeclarables()) {
            switch (declarable) {
                case Exchange exchange -> admin.declareExchange(exchange);
                case Queue queue -> admin.declareQueue(queue);
                case org.springframework.amqp.core.Binding binding -> admin.declareBinding(binding);
                default -> throw new IllegalStateException("unexpected " + declarable);
            }
        }
        admin.purgeQueue(REQUESTS_QUEUE);
        admin.declareExchange(Topology.exchange(OutboxHelpPublisher.EXCHANGE));
        answers = "test.grandma-avatar.help-provided." + UUID.randomUUID();
        admin.declareQueue(new Queue(answers, true, false, false));
        admin.declareBinding(BindingBuilder.bind(new Queue(answers)).to(Topology.exchange(OutboxHelpPublisher.EXCHANGE))
                .with(OutboxHelpPublisher.ROUTING_KEY));
    }

    @AfterEach
    void cleanUp() {
        if (container != null) {
            container.stop();
        }
        // Not auto-delete: receive() with a timeout consumes and cancels, which would delete such a queue.
        TestBroker.admin().deleteQueue(answers);
    }

    @Test
    void aHelpRequestOnTheBrokerIsAnsweredOnTheGrandmaAvatarExchange() throws Exception {
        container = new SimpleMessageListenerContainer(TestBroker.connectionFactory());
        container.setQueueNames(REQUESTS_QUEUE);
        container.setMessageListener(listener::onHelpRequested);
        container.start();
        UUID correlationId = UUID.randomUUID();
        HelpRequestId requestId = new HelpRequestId(UUID.randomUUID());

        rabbit.send("cooking-assistance", "cooking-assistance.help.requested", helpRequested(requestId, correlationId));
        awaitHandled(requestId);
        assertThat(relay.relay()).isEqualTo(1);

        Message answer = rabbit.receive(answers, 5000);
        assertThat(answer).isNotNull();
        assertThat(ContractMessage.messageType(answer)).isEqualTo("HelpProvided");
        MessageHeader header = ContractMessage.header(answer);
        assertThat(header.correlationId()).isEqualTo(correlationId);
        assertThat(header.source()).isEqualTo("grandma-avatar");
        contract.assertHeaders("helpProvided", answer.getMessageProperties().getHeaders());
        String payload = new String(answer.getBody(), StandardCharsets.UTF_8);
        contract.assertPayload("helpProvided", payload);
        JsonNode help = json.readTree(payload);
        assertThat(help.get("helpRequest").asText()).isEqualTo(requestId.value().toString());
        assertThat(help.at("/answer/answerType").asText()).isEqualTo("STEPS_TO_MITIGATE_CATASTROPHE");
        assertThat(notebook.find(requestId).orElseThrow().outcome()).isEqualTo(Outcome.ANSWERED);
    }

    @Test
    void aHelpRequestDeliveredTwiceIsAnsweredOnce() throws Exception {
        UUID correlationId = UUID.randomUUID();
        HelpRequestId requestId = new HelpRequestId(UUID.randomUUID());

        listener.onHelpRequested(helpRequested(requestId, correlationId));
        assertThat(relay.relay()).isEqualTo(1);
        listener.onHelpRequested(helpRequested(requestId, correlationId));
        assertThat(relay.relay()).isZero();

        assertThat(rabbit.receive(answers, 5000)).isNotNull();
        assertThat(rabbit.receive(answers, 500)).isNull();
    }

    @Test
    void aChefOnlyRequestIsAcknowledgedWithoutAnswer() throws Exception {
        HelpRequestId requestId = new HelpRequestId(UUID.randomUUID());
        String chefOnly = """
                {"helpRequestId":"%s","requester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074","title":"Dinner for my in-laws",
                 "type":"MENU_PROPOSAL","description":"Six guests on Saturday","preferredProvider":["CHEF"],"status":"OPEN"}
                """.formatted(requestId.value());
        contract.assertPayload("helpRequested", chefOnly);

        listener.onHelpRequested(ContractMessage.of("HelpRequested",
                MessageHeader.newMessage(UUID.randomUUID(), "cooking-assistance"), chefOnly));

        assertThat(relay.relay()).isZero();
        assertThat(notebook.find(requestId).orElseThrow().outcome()).isEqualTo(Outcome.NOT_FOR_GRANDMA);
        assertThat(rabbit.receive(answers, 500)).isNull();
    }

    private Message helpRequested(HelpRequestId requestId, UUID correlationId) {
        String payload = """
                {"helpRequestId":"%s","requester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074","title":"Burning Catastrophe",
                 "type":"STEPS_TO_MITIGATE_CATASTROPHE","description":"Scones are burned and mother in law is coming in 30 minutes",
                 "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913","preferredProvider":["GRANDMA_AVATAR","COMMUNITY"],"status":"OPEN"}
                """.formatted(requestId.value());
        contract.assertPayload("helpRequested", payload);
        return ContractMessage.of("HelpRequested", MessageHeader.newMessage(correlationId, "cooking-assistance"), payload);
    }

    private void awaitHandled(HelpRequestId requestId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (notebook.find(requestId).isEmpty()) {
            assertThat(System.currentTimeMillis()).as("help request handled in time").isLessThan(deadline);
            Thread.sleep(50);
        }
    }
}
