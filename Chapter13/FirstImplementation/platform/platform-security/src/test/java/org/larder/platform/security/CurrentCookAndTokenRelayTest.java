package org.larder.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CurrentCookAndTokenRelayTest {

    private static final UUID COOK = UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074");

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void signIn(String tokenValue, String cookId) {
        Jwt.Builder jwt = Jwt.withTokenValue(tokenValue).header("alg", "RS256")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).subject("cook");
        if (cookId != null) {
            jwt.claim(CurrentCook.COOK_ID_CLAIM, cookId);
        }
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt.build()));
    }

    @Test
    void theCookIdComesFromTheTokensClaim() {
        signIn("token", COOK.toString());

        assertThat(CurrentCook.id()).contains(new CookId(COOK));
        assertThat(CurrentCook.require().toString()).isEqualTo(COOK.toString());
    }

    @Test
    void withoutClaimOrAuthenticationThereIsNoCook() {
        assertThat(CurrentCook.id()).isEmpty();
        signIn("token", null);
        assertThat(CurrentCook.id()).isEmpty();
        assertThatThrownBy(CurrentCook::require).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new CookId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void upstreamCallsCarryTheCallersToken() {
        RestClient.Builder builder = RestClient.builder().requestInterceptor(new BearerTokenRelay());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/recipes")).andExpect(header("Authorization", "Bearer the-cooks-token"))
                .andRespond(withSuccess());
        server.expect(requestTo("/recipes")).andExpect(headerDoesNotExist("Authorization")).andRespond(withSuccess());
        RestClient client = builder.build();

        signIn("the-cooks-token", COOK.toString());
        client.get().uri("/recipes").retrieve().toBodilessEntity();
        SecurityContextHolder.clearContext();
        client.get().uri("/recipes").retrieve().toBodilessEntity();

        server.verify();
    }
}
