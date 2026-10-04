package org.larder.platform.messaging;

import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Transactional outbox of one Bounded Context. {@link #add} writes the message into the table
 * {@code outbox} of the context's own schema - in the same transaction as the state change it
 * announces. {@link OutboxRelay} publishes it afterwards. So an event is published if and only if
 * the change was committed (at least once).
 * <p>
 * Each publishing context creates the table with this migration:
 * <pre>
 * create table outbox (
 *     message_id     uuid primary key,
 *     exchange       text not null,
 *     routing_key    text not null,
 *     message_type   text not null,
 *     correlation_id uuid not null,
 *     source         text not null,
 *     payload        text not null,
 *     created_at     timestamptz not null default now()
 * );
 * </pre>
 */
public class Outbox {

    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;
    private final String source;

    /** @param source the publishing Bounded Context, carried as {@code MessageHeader.source} */
    public Outbox(JdbcClient jdbc, ObjectMapper objectMapper, String source) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.source = source;
    }

    /** Must run inside the transaction of the change; returns the header of the queued message. */
    public MessageHeader add(String exchange, String routingKey, String messageType, UUID correlationId, Object payload) {
        MessageHeader header = MessageHeader.newMessage(correlationId, source);
        jdbc.sql("""
                insert into outbox (message_id, exchange, routing_key, message_type, correlation_id, source, payload)
                values (:messageId, :exchange, :routingKey, :messageType, :correlationId, :source, :payload)
                """)
                .param("messageId", header.messageId())
                .param("exchange", exchange)
                .param("routingKey", routingKey)
                .param("messageType", messageType)
                .param("correlationId", correlationId)
                .param("source", source)
                .param("payload", json(payload))
                .update();
        return header;
    }

    private String json(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Payload cannot be written as JSON", e);
        }
    }
}
