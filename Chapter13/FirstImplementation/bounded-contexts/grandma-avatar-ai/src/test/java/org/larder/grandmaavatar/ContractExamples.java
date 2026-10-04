package org.larder.grandmaavatar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Examples taken verbatim from the contracts in {@code contracts/}. */
public final class ContractExamples {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private ContractExamples() {
    }

    /** The {@code HelpRequested} examples of {@code notifications.asyncapi.yaml} (headers + payload). */
    public static List<JsonNode> notificationsHelpRequested() {
        List<JsonNode> examples = new ArrayList<>();
        read("asyncapi/notifications.asyncapi.yaml").at("/components/messages/helpRequested/examples").forEach(examples::add);
        return examples;
    }

    /** The {@code HelpRequestStayCalm}-like answer example of Notifications ({@code stayCalm}). */
    public static JsonNode notificationsStayCalm() {
        return read("asyncapi/notifications.asyncapi.yaml").at("/components/messages/helpProvided/examples/0/payload");
    }

    /**
     * The OpenAPI example {@code HelpRequestList} of Cooking Assistance (one help request per type) as
     * {@code HelpRequested} payloads: without the resource's timestamps, and {@code OPEN} - a
     * {@code HelpRequested} always announces a new, open request.
     */
    public static List<ObjectNode> cookingAssistanceHelpRequests() {
        List<ObjectNode> requests = new ArrayList<>();
        JsonNode openApi = read("openapi/cooking-assistance.openapi.yaml");
        openApi.at("/components/examples/HelpRequestList/value").forEach(node -> {
            // The first entry is the YAML alias *burningCatastrophe, which Jackson does not resolve.
            JsonNode resolved = node.isObject() ? node : openApi.at("/components/examples/BurningCatastropheHelpRequest/value");
            ObjectNode request = ((ObjectNode) resolved).deepCopy();
            request.remove(List.of("createdAt", "updatedAt"));
            request.put("status", "OPEN");
            requests.add(request);
        });
        return requests;
    }

    private static JsonNode read(String file) {
        try {
            return YAML.readTree(contractsDir().resolve(file).toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read contract " + file, e);
        }
    }

    private static Path contractsDir() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            if (Files.isDirectory(dir.resolve("contracts/asyncapi"))) {
                return dir.resolve("contracts");
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("contracts not found");
    }
}
