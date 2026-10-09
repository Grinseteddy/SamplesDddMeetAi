package org.larder.platform.test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * Checks HTTP responses against the OpenAPI contracts in {@code contracts/openapi} - the provider
 * side of a contract test. For a response to {@code (method, path, status)} it
 * <ol>
 *   <li>finds the operation: the path template matching the concrete path (literal segments win over
 *       parameters, so {@code /consents/consent-texts} is not taken for {@code /consents/{consentId}});</li>
 *   <li>requires the status to be declared for that operation - {@code default} does not count, the
 *       contracts list every status they mean;</li>
 *   <li>validates the body against the schema of the declared media type ({@code application/json}),
 *       or requires an empty body when the contract declares none (e.g. 204).</li>
 * </ol>
 * OpenAPI 3.1 schemas are JSON Schema 2020-12; {@code $ref}s (also via {@code components/responses})
 * are resolved within the contract file, {@code examples} are ignored, formats ({@code uuid},
 * {@code date-time}, {@code uri}) are asserted.
 */
public final class OpenApiContract {

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<Path, JsonNode> DOCUMENTS = new HashMap<>();
    private static final Set<String> METHODS = Set.of("get", "put", "post", "delete", "patch", "head", "options");

    /** JSON Schema 2020-12 plus the OpenAPI annotations that do not validate anything. */
    private static final JsonSchemaFactory SCHEMAS = JsonSchemaFactory
            .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012))
            .metaSchema(JsonMetaSchema.builder(JsonMetaSchema.getV202012())
                    .addKeywords(List.of(new NonValidationKeyword("example"), new NonValidationKeyword("xml"),
                            new NonValidationKeyword("externalDocs")))
                    .build())
            .build();
    private static final SchemaValidatorsConfig CONFIG = SchemaValidatorsConfig.builder()
            .formatAssertionsEnabled(true)
            .build();

    private final Path file;

    private OpenApiContract(Path file) {
        this.file = file;
    }

    /** @param fileName e.g. {@code recipe-catalog.openapi.yaml} */
    public static OpenApiContract of(String fileName) {
        return new OpenApiContract(contractsDir().resolve(fileName).normalize());
    }

    public String fileName() {
        return file.getFileName().toString();
    }

    /** The path of the contract's first server URL, e.g. {@code /recipe-catalog}. */
    public String basePath() {
        String url = document(file).path("servers").path(0).path("url").asText("");
        return URI.create(url).getPath().replaceAll("/$", "");
    }

    /** The path template of the contract matching {@code path} (relative to {@link #basePath()}, query ignored). */
    public Optional<String> pathTemplate(String path) {
        String[] actual = segments(stripQuery(path));
        String best = null;
        int bestLiterals = -1;
        for (Iterator<String> it = document(file).path("paths").fieldNames(); it.hasNext(); ) {
            String template = it.next();
            String[] expected = segments(template);
            if (expected.length != actual.length) {
                continue;
            }
            int literals = 0;
            boolean matches = true;
            for (int i = 0; i < expected.length && matches; i++) {
                if (expected[i].startsWith("{") && expected[i].endsWith("}")) {
                    matches = !actual[i].isEmpty();
                } else if (expected[i].equals(actual[i])) {
                    literals++;
                } else {
                    matches = false;
                }
            }
            if (matches && literals > bestLiterals) {
                best = template;
                bestLiterals = literals;
            }
        }
        return Optional.ofNullable(best);
    }

    /** The statuses the contract declares for {@code method} on {@code pathTemplate} ({@code default} excluded). */
    public Set<String> declaredStatuses(String method, String pathTemplate) {
        Set<String> statuses = new TreeSet<>();
        operation(method, pathTemplate).path("responses").fieldNames().forEachRemaining(statuses::add);
        statuses.remove("default");
        return statuses;
    }

    /**
     * Fails with all violations if the response breaks the contract.
     *
     * @param method      HTTP method, e.g. {@code GET}
     * @param path        the requested path relative to {@link #basePath()}, e.g. {@code /recipes/42?diet=VEGAN}
     * @param status      the response status
     * @param contentType the response's {@code Content-Type}, may be {@code null}
     * @param body        the response body, may be empty
     * @return the path template of the operation, e.g. {@code /recipes/{recipeId}}
     */
    public String assertResponse(String method, String path, int status, String contentType, String body) {
        String template = pathTemplate(path).orElseThrow(() -> new AssertionError(
                fileName() + " has no path matching " + path));
        String operation = method.toUpperCase(Locale.ROOT) + " " + basePath() + template;
        JsonNode responses = operation(method, template).path("responses");
        JsonNode declared = responses.get(String.valueOf(status));
        if (declared == null) {
            throw new AssertionError(operation + " answered " + status + ", but " + fileName()
                    + " declares only " + declaredStatuses(method, template) + "\n  body: " + abbreviate(body));
        }
        JsonNode response = resolve(file, declared, 0);
        JsonNode content = response.path("content");
        boolean emptyBody = body == null || body.isBlank();
        if (content.isMissingNode() || content.isEmpty()) {
            if (!emptyBody) {
                throw new AssertionError(operation + " " + status + " must have no body (" + fileName()
                        + "), got: " + abbreviate(body));
            }
            return template;
        }
        String mediaType = mediaType(contentType);
        JsonNode schemaNode = mediaType == null ? null : content.path(mediaType).get("schema");
        if (emptyBody || schemaNode == null) {
            throw new AssertionError(operation + " " + status + " must have a body of type " + names(content)
                    + " (" + fileName() + "), got Content-Type " + contentType + " and body: " + abbreviate(body));
        }
        JsonNode instance = read(body, operation);
        JsonSchema schema = SCHEMAS.getSchema(schemaNode, CONFIG);
        Set<ValidationMessage> errors = schema.validate(instance);
        if (!errors.isEmpty()) {
            throw new AssertionError(operation + " " + status + " violates " + fileName() + ":\n  "
                    + errors.stream().map(ValidationMessage::getMessage).sorted().collect(Collectors.joining("\n  "))
                    + "\n  body: " + abbreviate(body));
        }
        return template;
    }

    private JsonNode operation(String method, String pathTemplate) {
        String key = method.toLowerCase(Locale.ROOT);
        JsonNode operation = document(file).path("paths").path(pathTemplate).get(key);
        if (operation == null || !METHODS.contains(key)) {
            throw new AssertionError(fileName() + " has no operation " + method + " " + pathTemplate);
        }
        return operation;
    }

    private static String mediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private static String names(JsonNode content) {
        List<String> names = new ArrayList<>();
        content.fieldNames().forEachRemaining(names::add);
        return String.join(", ", names);
    }

    /** The node with every $ref replaced by its target, recursively; {@code examples} stay untouched. */
    private static JsonNode resolve(Path file, JsonNode node, int depth) {
        if (depth > 50) {
            throw new IllegalStateException("$ref nesting too deep in " + file.getFileName());
        }
        if (node.isObject() && node.has("$ref")) {
            String ref = node.get("$ref").asText();
            int hash = ref.indexOf('#');
            Path target = hash == 0 ? file : file.resolveSibling(ref.substring(0, hash)).normalize();
            JsonNode referenced = document(target).at(ref.substring(hash + 1));
            if (referenced.isMissingNode()) {
                throw new IllegalStateException(ref + " not found in " + target.getFileName());
            }
            referenced = resolve(target, referenced, depth + 1);
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
            node.fields().forEachRemaining(e -> {
                if (isDiscriminator(e.getKey(), e.getValue())) {
                    return; // OpenAPI's discriminator only names the property the oneOf already decides on
                }
                // examples are annotations: neither resolved nor validated
                copy.set(e.getKey(), e.getKey().equals("examples") ? e.getValue() : resolve(file, e.getValue(), depth + 1));
            });
            return copy;
        }
        if (node.isArray()) {
            var copy = JSON.createArrayNode();
            node.forEach(item -> copy.add(resolve(file, item, depth + 1)));
            return copy;
        }
        return node;
    }

    private static boolean isDiscriminator(String key, JsonNode value) {
        return key.equals("discriminator") && value.isObject() && value.has("propertyName");
    }

    private static String[] segments(String path) {
        String trimmed = path.replaceAll("^/+", "").replaceAll("/+$", "");
        return trimmed.isEmpty() ? new String[0] : trimmed.split("/", -1);
    }

    private static String stripQuery(String path) {
        int query = path.indexOf('?');
        return query < 0 ? path : path.substring(0, query);
    }

    private static String abbreviate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
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

    private static JsonNode read(String json, String operation) {
        try {
            return JSON.readTree(json);
        } catch (IOException e) {
            throw new AssertionError(operation + " answered no JSON: " + abbreviate(json), e);
        }
    }

    private static Path contractsDir() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("contracts/openapi");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("contracts/openapi not found above " + Path.of("").toAbsolutePath());
    }
}
