package org.larder.platform.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * Checks messages against the AsyncAPI contracts in {@code contracts/asyncapi}: payload and
 * headers of a message are validated against the schemas the contract declares, with
 * {@code $ref}s resolved within and across contract files (e.g. Notifications referencing the
 * published language of Cooking Assistance).
 */
public final class AsyncApiContract {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<Path, JsonNode> DOCUMENTS = new HashMap<>();

    private final Path file;

    private AsyncApiContract(Path file) {
        this.file = file;
    }

    /** @param fileName e.g. {@code cooking-assistance.asyncapi.yaml} */
    public static AsyncApiContract of(String fileName) {
        return new AsyncApiContract(contractsDir().resolve(fileName).normalize());
    }

    /** Fails with all violations if {@code payload} (object or JSON string) breaks the payload schema of the message. */
    public void assertPayload(String messageKey, Object payload) {
        assertValid(messageKey + " payload", resolved(file, "/components/messages/" + messageKey + "/payload"), payload);
    }

    /** Fails if the AMQP headers break the header schema of the message. */
    public void assertHeaders(String messageKey, Map<String, Object> headers) {
        assertValid(messageKey + " headers", resolved(file, "/components/messages/" + messageKey + "/headers"), headers);
    }

    /** The message name, e.g. {@code HelpRequested} for key {@code helpRequested}. */
    public String messageName(String messageKey) {
        return resolved(file, "/components/messages/" + messageKey).path("name").asText();
    }

    private static void assertValid(String what, JsonNode schemaNode, Object value) {
        JsonNode instance = value instanceof String json ? read(json) : JSON.valueToTree(value);
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaNode);
        Set<ValidationMessage> errors = schema.validate(instance);
        if (!errors.isEmpty()) {
            throw new AssertionError(what + " violates the contract:\n  "
                    + errors.stream().map(ValidationMessage::getMessage).sorted().collect(Collectors.joining("\n  "))
                    + "\n  message: " + instance);
        }
    }

    /** The node at {@code pointer} with every $ref replaced by its target, recursively. */
    private static JsonNode resolved(Path file, String pointer) {
        JsonNode node = document(file).at(pointer);
        if (node.isMissingNode()) {
            throw new IllegalArgumentException(pointer + " not found in " + file.getFileName());
        }
        return resolve(file, node, 0);
    }

    private static JsonNode resolve(Path file, JsonNode node, int depth) {
        if (depth > 50) {
            throw new IllegalStateException("$ref nesting too deep in " + file.getFileName());
        }
        if (node.isObject() && node.has("$ref")) {
            String ref = node.get("$ref").asText();
            int hash = ref.indexOf('#');
            Path target = hash == 0 ? file : file.resolveSibling(ref.substring(0, hash)).normalize();
            JsonNode referenced = resolve(target, document(target).at(ref.substring(hash + 1)), depth + 1);
            if (node.size() == 1) {
                return referenced;
            }
            ObjectNode merged = ((ObjectNode) referenced).deepCopy();
            node.fields().forEachRemaining(e -> {
                if (!e.getKey().equals("$ref")) {
                    merged.set(e.getKey(), resolve(file, e.getValue(), depth + 1));
                }
            });
            return merged;
        }
        if (node.isObject()) {
            ObjectNode copy = JSON.createObjectNode();
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                var e = it.next();
                copy.set(e.getKey(), e.getKey().equals("examples") ? e.getValue() : resolve(file, e.getValue(), depth + 1));
            }
            return copy;
        }
        if (node.isArray()) {
            var copy = JSON.createArrayNode();
            node.forEach(item -> copy.add(resolve(file, item, depth + 1)));
            return copy;
        }
        return node;
    }

    private static synchronized JsonNode document(Path file) {
        return DOCUMENTS.computeIfAbsent(file, f -> {
            try {
                return YAML.readTree(f.toFile());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read contract " + f, e);
            }
        });
    }

    private static JsonNode read(String json) {
        try {
            return JSON.readTree(json);
        } catch (IOException e) {
            throw new AssertionError("Not JSON: " + json, e);
        }
    }

    private static Path contractsDir() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("contracts/asyncapi");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("contracts/asyncapi not found above " + Path.of("").toAbsolutePath());
    }
}
