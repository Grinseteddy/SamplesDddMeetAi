package org.larder.platform.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.TestBroker;
import org.larder.platform.test.TestDatabase;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

class OutboxRelayTest {

    private BoundedContextDatabase database;
    private Outbox outbox;
    private OutboxRelay relay;
    private RabbitTemplate rabbit;
    private String queue;

    @BeforeEach
    void setUp() {
        database = TestDatabase.forSchema("outboxtest");
        database.jdbcClient().sql("""
                create table outbox (
                    message_id uuid primary key, exchange text not null, routing_key text not null,
                    message_type text not null, correlation_id uuid not null, source text not null,
                    payload text not null, created_at timestamptz not null default now())
                """).update();
        var transactionManager = new DataSourceTransactionManager(database.dataSource());
        outbox = new Outbox(database.jdbcClient(), new ObjectMapper(), "outbox-test");
        rabbit = TestBroker.rabbitTemplate();
        relay = new OutboxRelay("test", database.jdbcClient(), rabbit, transactionManager, Duration.ofSeconds(1));

        RabbitAdmin admin = TestBroker.admin();
        admin.declareExchange(Topology.exchange("outbox-test"));
        queue = "outbox-test." + UUID.randomUUID();
        admin.declareQueue(new Queue(queue, true, false, true));
        admin.declareBinding(BindingBuilder.bind(new Queue(queue)).to(Topology.exchange("outbox-test")).with("thing.happened"));
    }

    @Test
    void publishesCommittedMessagesWithTheContractHeadersAndRemovesThem() {
        UUID correlationId = UUID.randomUUID();
        var transactions = new TransactionTemplate(new DataSourceTransactionManager(database.dataSource()));
        MessageHeader header = transactions.execute(status ->
                outbox.add("outbox-test", "thing.happened", "ThingHappened", correlationId, Map.of("answer", 42)));

        assertThat(relay.relay()).isEqualTo(1);

        Message message = rabbit.receive(queue, 5000);
        assertThat(message).isNotNull();
        assertThat(new String(message.getBody(), StandardCharsets.UTF_8)).isEqualTo("{\"answer\":42}");
        assertThat(ContractMessage.messageType(message)).isEqualTo("ThingHappened");
        assertThat(ContractMessage.header(message)).isEqualTo(header);
        assertThat(message.getMessageProperties().getHeaders()).doesNotContainKey("__TypeId__");
        assertThat(relay.relay()).isZero();
    }

    @Test
    void anUnroutableMessageIsReportedAndDoesNotBlockTheOutbox() throws InterruptedException {
        var transactions = new TransactionTemplate(new DataSourceTransactionManager(database.dataSource()));
        transactions.executeWithoutResult(status -> {
            outbox.add("outbox-test", "nobody.listens", "Unheard", UUID.randomUUID(), Map.of("answer", 0));
            outbox.add("outbox-test", "thing.happened", "ThingHappened", UUID.randomUUID(), Map.of("answer", 2));
        });

        assertThat(relay.relay()).isEqualTo(2);

        assertThat(rabbit.receive(queue, 5000)).isNotNull();
        for (int i = 0; i < 50 && relay.unroutable() == 0; i++) {
            Thread.sleep(50);
        }
        assertThat(relay.unroutable()).isEqualTo(1);
        assertThat(relay.relay()).isZero();
    }

    @Test
    void theRelayRunsInTheBackgroundUntilStopped() throws InterruptedException {
        var transactions = new TransactionTemplate(new DataSourceTransactionManager(database.dataSource()));
        transactions.executeWithoutResult(status ->
                outbox.add("outbox-test", "thing.happened", "ThingHappened", UUID.randomUUID(), Map.of("answer", 3)));
        var background = new OutboxRelay("background", database.jdbcClient(), rabbit,
                new DataSourceTransactionManager(database.dataSource()), Duration.ofMillis(50));

        background.start();
        try {
            assertThat(background.isRunning()).isTrue();
            assertThat(rabbit.receive(queue, 5000)).isNotNull();
        } finally {
            background.stop();
        }
        assertThat(background.isRunning()).isFalse();
    }

    @Test
    void aRolledBackChangePublishesNothing() {
        var transactions = new TransactionTemplate(new DataSourceTransactionManager(database.dataSource()));
        transactions.executeWithoutResult(status -> {
            outbox.add("outbox-test", "thing.happened", "ThingHappened", UUID.randomUUID(), Map.of("answer", 1));
            status.setRollbackOnly();
        });

        assertThat(relay.relay()).isZero();
        assertThat(rabbit.receive(queue, 500)).isNull();
    }
}
