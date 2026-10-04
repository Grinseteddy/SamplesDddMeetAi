package org.larder.platform.web;

import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

/**
 * HTTP clients from a Bounded Context to an upstream one. Even inside the monolith the call goes
 * over HTTP through the upstream's published contract, so a context can be cut out later
 * (AP0001) without touching its callers.
 */
public final class UpstreamClients {

    private UpstreamClients() {
    }

    /**
     * Base URL of {@code upstream} as seen from {@code boundedContext}, configured as
     * {@code larder.<boundedContext>.upstream.<upstream>.base-url}; defaults to this application.
     */
    public static String baseUrl(Environment environment, String boundedContext, String upstream) {
        return environment.getProperty("larder." + boundedContext + ".upstream." + upstream + ".base-url",
                "http://localhost:" + environment.getProperty("server.port", "8080") + "/" + upstream);
    }

    /** A RestClient for one upstream, built from Spring Boot's builder (JSON settings) plus the given interceptors. */
    public static RestClient restClient(RestClient.Builder builder, String baseUrl, ClientHttpRequestInterceptor... interceptors) {
        RestClient.Builder upstream = builder.clone().baseUrl(baseUrl);
        for (ClientHttpRequestInterceptor interceptor : interceptors) {
            upstream.requestInterceptor(interceptor);
        }
        return upstream.build();
    }
}
