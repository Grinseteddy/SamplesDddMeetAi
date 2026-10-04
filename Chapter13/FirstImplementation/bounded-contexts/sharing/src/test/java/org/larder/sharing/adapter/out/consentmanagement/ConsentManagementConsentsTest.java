package org.larder.sharing.adapter.out.consentmanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.larder.sharing.application.ConsentPurpose.MENTION_AS_HELPER;
import static org.larder.sharing.application.ConsentPurpose.PHOTOS_IN_PUBLIC_THANKS;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.sharing.application.ConsentPurpose;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Talks to a stand-in for Consent Management that answers as its contract says. */
class ConsentManagementConsentsTest {

    private static final String BASE_URL = "http://larder.test/consent-management";
    private static final String CONSENTS_OF_OTHER_COOK = BASE_URL + "/consents?subject=" + OTHER_COOK.value();
    private static final UUID MENTION = UUID.fromString(ConsentManagementClientConfiguration.MENTION_AS_HELPER);
    private static final UUID PHOTOS = UUID.fromString(ConsentManagementClientConfiguration.PHOTOS_IN_PUBLIC_THANKS);

    private MockRestServiceServer consentManagement;
    private ConsentManagementConsents consents;

    @BeforeEach
    void stubConsentManagement() {
        RestClient.Builder builder = RestClient.builder();
        consentManagement = MockRestServiceServer.bindTo(builder).build();
        consents = ConsentManagementConsents.create(builder, BASE_URL,
                Map.of(MENTION_AS_HELPER, MENTION, PHOTOS_IN_PUBLIC_THANKS, PHOTOS));
    }

    @AfterEach
    void forgetCaller() {
        SecurityContextHolder.clearContext();
    }

    private static String consent(UUID text, String wording, String revokedAt) {
        return """
                {"consentId":"%s","subject":"%s","consentText":{"consentTextId":"%s","text":"%s"},
                 "givenAt":"2026-09-21T10:34:00Z"%s}
                """.formatted(UUID.randomUUID(), OTHER_COOK.value(), text, wording,
                revokedAt == null ? "" : ",\"revokedAt\":\"" + revokedAt + "\"");
    }

    private void answer(String... consentsJson) {
        consentManagement.expect(requestTo(CONSENTS_OF_OTHER_COOK))
                .andRespond(withSuccess("[" + String.join(",", consentsJson) + "]", MediaType.APPLICATION_JSON));
    }

    @Test
    void aConsentInForceToTheMentionTextAllowsMentioningAskedWithTheCallersToken() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("cooks-token")
                .header("alg", "none").claim("cookId", COOK.value().toString()).build()));
        consentManagement.expect(requestTo(CONSENTS_OF_OTHER_COOK))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("version", "1.0.0"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer cooks-token"))
                .andRespond(withSuccess("[" + consent(MENTION, "I allow other cooks to mention me as helper in their thanks.", null)
                        + "]", MediaType.APPLICATION_JSON));

        assertThat(consents.isInForce(OTHER_COOK, MENTION_AS_HELPER)).isTrue();
        consentManagement.verify();
    }

    @Test
    void aRevokedConsentIsNotInForce() {
        answer(consent(MENTION, "mention", "2026-09-22T08:00:00Z"));

        assertThat(consents.isInForce(OTHER_COOK, MENTION_AS_HELPER)).isFalse();
    }

    @Test
    void aConsentGivenAgainAfterRevokingIsInForce() {
        answer(consent(MENTION, "mention", "2026-09-22T08:00:00Z"), consent(MENTION, "mention", null));

        assertThat(consents.isInForce(OTHER_COOK, MENTION_AS_HELPER)).isTrue();
    }

    @Test
    void aConsentToAnotherTextDoesNotCount() {
        answer(consent(PHOTOS, "photos", null));

        assertThat(consents.isInForce(OTHER_COOK, MENTION_AS_HELPER)).isFalse();
    }

    @Test
    void thePhotoConsentIsItsOwnText() {
        answer(consent(PHOTOS, "photos", null));

        assertThat(consents.isInForce(OTHER_COOK, PHOTOS_IN_PUBLIC_THANKS)).isTrue();
    }

    @Test
    void noConsentsAreNoConsent() {
        answer();

        assertThat(consents.isInForce(OTHER_COOK, MENTION_AS_HELPER)).isFalse();
    }

    @Test
    void anUnknownSubjectHasNoConsent() {
        consentManagement.expect(requestTo(CONSENTS_OF_OTHER_COOK)).andRespond(withResourceNotFound());

        assertThat(consents.isInForce(OTHER_COOK, MENTION_AS_HELPER)).isFalse();
    }

    @Test
    void aRefusalOfConsentManagementIsNotPermitted() {
        consentManagement.expect(requestTo(CONSENTS_OF_OTHER_COOK)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> consents.isInForce(OTHER_COOK, MENTION_AS_HELPER))
                .isInstanceOf(NotPermittedException.class);
    }

    @Test
    void aFailingConsentManagementIsUnavailable() {
        consentManagement.expect(requestTo(CONSENTS_OF_OTHER_COOK)).andRespond(withServerError());

        assertThatThrownBy(() -> consents.isInForce(OTHER_COOK, MENTION_AS_HELPER))
                .isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void anUnreachableConsentManagementIsUnavailable() {
        consentManagement.expect(requestTo(CONSENTS_OF_OTHER_COOK)).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertThatThrownBy(() -> consents.isInForce(OTHER_COOK, MENTION_AS_HELPER))
                .isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void everyPurposeNeedsAConsentText() {
        assertThatThrownBy(() -> ConsentManagementConsents.create(RestClient.builder(), BASE_URL,
                Map.<ConsentPurpose, UUID>of(MENTION_AS_HELPER, MENTION)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
