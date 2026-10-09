package org.larder.platform.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class UpstreamClientsTest {

    @Test
    void theBaseUrlIsConfiguredPerContextAndUpstreamOrDefaultsToThisApplication() {
        var environment = new MockEnvironment()
                .withProperty("larder.sharing.upstream.media.base-url", "http://media.example/media")
                .withProperty("server.port", "9999");

        assertThat(UpstreamClients.baseUrl(environment, "sharing", "media")).isEqualTo("http://media.example/media");
        assertThat(UpstreamClients.baseUrl(environment, "sharing", "consent-management"))
                .isEqualTo("http://localhost:9999/consent-management");
        assertThat(UpstreamClients.baseUrl(new MockEnvironment(), "sharing", "media")).isEqualTo("http://localhost:8080/media");
    }

    @Test
    void theClientUsesTheBaseUrlAndTheGivenInterceptors() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://localhost:8080/media/images"))
                .andExpect(header("X-Probe", "relayed"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        RestClient client = UpstreamClients.restClient(builder, "http://localhost:8080/media", (request, body, execution) -> {
            request.getHeaders().add("X-Probe", "relayed");
            return execution.execute(request, body);
        });

        assertThat(client.get().uri("/images").retrieve().body(String.class)).isEqualTo("[]");
        server.verify();
    }
}
