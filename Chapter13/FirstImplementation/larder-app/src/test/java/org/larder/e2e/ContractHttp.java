package org.larder.e2e;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import org.larder.platform.test.OpenApiContract;

/**
 * HTTP against the running application where every response is checked against the OpenAPI
 * contract of the Bounded Context that served it - call and provider contract test in one.
 * The context is the first path segment ({@code /recipe-catalog/recipes} → {@code recipe-catalog.openapi.yaml},
 * whose server URL must end in the same segment). A response with an undeclared status fails, too.
 */
final class ContractHttp {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String baseUrl;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Map<String, OpenApiContract> contracts = new ConcurrentHashMap<>();
    private final AtomicInteger validated = new AtomicInteger();
    private final Map<String, AtomicInteger> validatedPerContract = new ConcurrentHashMap<>();

    ContractHttp(int port) {
        this.baseUrl = "http://localhost:" + port;
    }

    /** Calls with the cook's bearer token. */
    Caller as(String token) {
        return new Caller(token);
    }

    /** Calls without any token. */
    Caller anonymous() {
        return new Caller(null);
    }

    /** Number of responses validated against a contract. */
    int validatedResponses() {
        return validated.get();
    }

    Map<String, AtomicInteger> validatedPerContract() {
        return validatedPerContract;
    }

    /** One caller (token). */
    final class Caller {

        private final String token;

        private Caller(String token) {
            this.token = token;
        }

        Response get(String path) {
            return call("GET", path, null);
        }

        Response post(String path, String json) {
            return call("POST", path, json);
        }

        Response patch(String path, String json) {
            return call("PATCH", path, json);
        }

        Response put(String path, String json) {
            return call("PUT", path, json);
        }

        Response delete(String path) {
            return call("DELETE", path, null);
        }

        private Response call(String method, String path, String json) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("version", "1.0.0")
                    .header("Accept", "application/json")
                    .method(method, json == null ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(json));
            if (json != null) {
                request.header("Content-Type", "application/json");
            }
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response;
            try {
                response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                throw new IllegalStateException(method + " " + path + " failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            String contentType = response.headers().firstValue("Content-Type").orElse(null);
            String operation = validate(method, path, response.statusCode(), contentType, response.body());
            return new Response(method + " " + path, operation, response.statusCode(), parse(response.body()),
                    response.body(), response.headers().firstValue("Location").orElse(null));
        }

        private String validate(String method, String path, int status, String contentType, String body) {
            String context = path.split("[/?]")[1];
            OpenApiContract contract = contracts.computeIfAbsent(context,
                    c -> OpenApiContract.of(c + ".openapi.yaml"));
            if (!contract.basePath().equals("/" + context)) {
                throw new IllegalStateException(contract.fileName() + " is served at " + contract.basePath());
            }
            String relative = path.substring(context.length() + 1);
            validated.incrementAndGet();
            validatedPerContract.computeIfAbsent(contract.fileName(), f -> new AtomicInteger()).incrementAndGet();
            return method + " /" + context + contract.assertResponse(method, relative, status, contentType, body);
        }
    }

    /** A validated response. */
    record Response(String request, String operation, int status, JsonNode body, String rawBody, String location) {

        /** Asserts the status, showing the body when it differs. */
        Response expect(int expected) {
            if (status != expected) {
                throw new AssertionError(request + " answered " + status + ", expected " + expected + ": " + rawBody);
            }
            return this;
        }

        /** Asserts a 400 with the given {@code code} of the contracts' {@code Error}. */
        Response expectError(int expectedStatus, String expectedCode) {
            expect(expectedStatus);
            if (!expectedCode.equals(code())) {
                throw new AssertionError(request + " answered code " + code() + ", expected " + expectedCode + ": " + rawBody);
            }
            return this;
        }

        String code() {
            return body.path("code").asText(null);
        }

        /** The id in the last segment of the {@code Location} header, e.g. of a created resource. */
        String createdId() {
            return Optional.ofNullable(location)
                    .map(link -> link.substring(link.lastIndexOf('/') + 1))
                    .orElseThrow(() -> new AssertionError(request + " answered no Location: " + rawBody));
        }

        JsonNode path(String field) {
            return body.path(field);
        }
    }

    private static JsonNode parse(String body) {
        if (body == null || body.isBlank()) {
            return MissingNode.getInstance();
        }
        try {
            return JSON.readTree(body);
        } catch (IOException e) {
            return MissingNode.getInstance();
        }
    }
}
