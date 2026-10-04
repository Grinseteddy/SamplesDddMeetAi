package org.larder.platform.messaging;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;

/**
 * An AMQP message as the AsyncAPI contracts define it: JSON payload, the {@link MessageHeader}
 * as headers, the message name (e.g. {@code HelpRequested}) as AMQP type, persistent delivery
 * ({@code deliveryMode: 2}). No Java class names travel with the message.
 */
public final class ContractMessage {

    private ContractMessage() {
    }

    public static Message of(String messageType, MessageHeader header, String jsonPayload) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setType(messageType);
        properties.setMessageId(header.messageId().toString());
        properties.setCorrelationId(header.correlationId().toString());
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        header.asAmqpHeaders().forEach(properties::setHeader);
        return MessageBuilder.withBody(jsonPayload.getBytes(StandardCharsets.UTF_8)).andProperties(properties).build();
    }

    public static MessageHeader header(Message message) {
        return MessageHeader.fromAmqpHeaders(message.getMessageProperties().getHeaders());
    }

    public static String messageType(Message message) {
        return message.getMessageProperties().getType();
    }

    public static UUID messageId(Message message) {
        return header(message).messageId();
    }
}
