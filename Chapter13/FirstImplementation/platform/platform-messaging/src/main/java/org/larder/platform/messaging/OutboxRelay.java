package org.larder.platform.messaging;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.SmartLifecycle;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Publishes the {@link Outbox} of one Bounded Context: polls the table, sends each message and
 * waits for the broker's confirm, then deletes it - all in one transaction with
 * {@code for update skip locked}, so several instances can relay side by side. A message is only
 * removed after the broker confirmed it; a crash in between leads to a duplicate, never to a loss,
 * which is why consumers are idempotent.
 * <p>
 * <p>
 * Messages are sent {@code mandatory} ({@code mandatory: true} in the AsyncAPI bindings): a message
 * no queue is bound for comes back from the broker and is logged as unroutable. It is still removed
 * from the outbox - publish/subscribe means nobody subscribed, and keeping it would block the
 * messages behind it. {@link #unroutable()} counts them for monitoring (AP0007).
 * <p>
 * Needs {@code spring.rabbitmq.publisher-confirm-type: simple} and {@code spring.rabbitmq.publisher-returns: true}.
 */
public class OutboxRelay implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxRelay.class);
    private static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(5);

    private final JdbcClient jdbc;
    private final RabbitTemplate rabbit;
    private final TransactionTemplate transactions;
    private final Duration interval;
    private final String name;
    private final AtomicLong unroutable = new AtomicLong();
    private ScheduledExecutorService scheduler;

    public OutboxRelay(String name, JdbcClient jdbc, RabbitTemplate rabbit, PlatformTransactionManager transactionManager,
            Duration interval) {
        this.name = name;
        this.jdbc = jdbc;
        this.rabbit = mandatory(rabbit);
        this.transactions = new TransactionTemplate(transactionManager);
        this.interval = interval;
    }

    /** Publishes up to 50 pending messages; returns how many were published. */
    public int relay() {
        Integer published = transactions.execute(status -> {
            List<Pending> pending = jdbc.sql("""
                    select message_id, exchange, routing_key, message_type, correlation_id, source, payload
                      from outbox order by created_at limit 50 for update skip locked
                    """)
                    .query((rs, row) -> new Pending(
                            rs.getObject("message_id", UUID.class), rs.getString("exchange"), rs.getString("routing_key"),
                            rs.getString("message_type"), rs.getObject("correlation_id", UUID.class),
                            rs.getString("source"), rs.getString("payload")))
                    .list();
            for (Pending message : pending) {
                rabbit.invoke(operations -> {
                    operations.send(message.exchange(), message.routingKey(), ContractMessage.of(message.messageType(),
                            new MessageHeader(message.correlationId(), message.messageId(), message.source()), message.payload()));
                    operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT.toMillis());
                    return null;
                });
                jdbc.sql("delete from outbox where message_id = :id").param("id", message.messageId()).update();
            }
            return pending.size();
        });
        return published == null ? 0 : published;
    }

    /** Number of messages the broker returned as unroutable since start. */
    public long unroutable() {
        return unroutable.get();
    }

    /** An own template per relay: a RabbitTemplate supports only one returns callback. */
    private RabbitTemplate mandatory(RabbitTemplate shared) {
        RabbitTemplate template = new RabbitTemplate(shared.getConnectionFactory());
        template.setMandatory(true);
        template.setReturnsCallback(returned -> {
            unroutable.incrementAndGet();
            LOG.warn("Outbox relay {}: message {} of type {} to {}/{} is unroutable ({}) - no queue is bound",
                    name, returned.getMessage().getMessageProperties().getMessageId(),
                    returned.getMessage().getMessageProperties().getType(),
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText());
        });
        return template;
    }

    @Override
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "outbox-relay-" + name);
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::relaySafely, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void relaySafely() {
        try {
            relay();
        } catch (RuntimeException e) {
            LOG.warn("Outbox relay {} failed, retrying in {}: {}", name, interval, e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @Override
    public boolean isRunning() {
        return scheduler != null;
    }

    private record Pending(UUID messageId, String exchange, String routingKey, String messageType, UUID correlationId,
            String source, String payload) {
    }
}
