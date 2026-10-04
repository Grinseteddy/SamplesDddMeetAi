package org.larder.notification.adapter.in.messaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.springframework.amqp.core.Message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** The message examples of {@code contracts/asyncapi/notifications.asyncapi.yaml}, read from the contract itself. */
final class ContractExamples {

    static final String CONTRACT = "notifications.asyncapi.yaml";

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON = new ObjectMapper();

    private ContractExamples() {
    }

    /** The example {@code name} of message {@code messageKey}, e.g. {@code helpProvided}/{@code stayCalm}. */
    static JsonNode example(String messageKey, String name) {
        for (JsonNode example : contract().at("/components/messages/" + messageKey + "/examples")) {
            if (example.path("name").asText().equals(name)) {
                return example;
            }
        }
        throw new IllegalArgumentException("No example " + name + " for " + messageKey);
    }

    static String payloadJson(JsonNode example) {
        try {
            return JSON.writeValueAsString(example.get("payload"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static MessageHeader header(JsonNode example) {
        JsonNode headers = example.get("headers");
        return new MessageHeader(UUID.fromString(headers.get("correlationId").asText()),
                UUID.fromString(headers.get("messageId").asText()), headers.get("source").asText());
    }

    /** The example as it arrives over AMQP. */
    static Message message(String messageName, JsonNode example) {
        return ContractMessage.of(messageName, header(example), payloadJson(example));
    }

    private static JsonNode contract() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("contracts/asyncapi").resolve(CONTRACT);
            if (Files.isRegularFile(candidate)) {
                try {
                    return YAML.readTree(candidate.toFile());
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read " + candidate, e);
                }
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException(CONTRACT + " not found above " + Path.of("").toAbsolutePath());
    }
}
