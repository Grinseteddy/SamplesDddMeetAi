package org.larder.platform.messaging;

import java.util.Map;
import java.util.UUID;

/**
 * The {@code MessageHeader} of the AsyncAPI contracts, carried as AMQP headers.
 *
 * @param correlationId traces one help journey end to end (request, help, thanks)
 * @param messageId     unique per message
 * @param source        the Bounded Context that produced the message
 */
public record MessageHeader(UUID correlationId, UUID messageId, String source) {

    public static final String CORRELATION_ID = "correlationId";
    public static final String MESSAGE_ID = "messageId";
    public static final String SOURCE = "source";

    public static MessageHeader newMessage(UUID correlationId, String source) {
        return new MessageHeader(correlationId, UUID.randomUUID(), source);
    }

    public Map<String, Object> asAmqpHeaders() {
        return Map.of(CORRELATION_ID, correlationId.toString(), MESSAGE_ID, messageId.toString(), SOURCE, source);
    }

    public static MessageHeader fromAmqpHeaders(Map<String, Object> headers) {
        return new MessageHeader(
                UUID.fromString(String.valueOf(headers.get(CORRELATION_ID))),
                UUID.fromString(String.valueOf(headers.get(MESSAGE_ID))),
                String.valueOf(headers.get(SOURCE)));
    }
}
