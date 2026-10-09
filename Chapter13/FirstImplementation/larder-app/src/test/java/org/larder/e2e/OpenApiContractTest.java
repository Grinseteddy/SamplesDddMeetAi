package org.larder.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.larder.platform.test.OpenApiContract;

/** The provider contract check itself: it must reject what the contracts do not allow, or the story proves nothing. */
class OpenApiContractTest {

    static final OpenApiContract SHARING = OpenApiContract.of("sharing.openapi.yaml");
    static final OpenApiContract CONSENTS = OpenApiContract.of("consent-management.openapi.yaml");
    static final String JSON = "application/json";

    @Test
    void findsTheOperationLiteralSegmentsFirst() {
        assertThat(SHARING.basePath()).isEqualTo("/sharing");
        assertThat(SHARING.pathTemplate("/thanks/42?x=1")).contains("/thanks/{thanksId}");
        assertThat(CONSENTS.pathTemplate("/consents/consent-texts")).contains("/consents/consent-texts");
        assertThat(CONSENTS.pathTemplate("/consents/7")).contains("/consents/{consentId}");
        assertThat(SHARING.pathTemplate("/unknown")).isEmpty();
    }

    @Test
    void acceptsAResponseAsDeclared() {
        assertThatCode(() -> SHARING.assertResponse("POST", "/thanks", 400, JSON,
                "{\"code\": \"MENTION_WITHOUT_CONSENT\", \"message\": \"Ben has not consented\"}"))
                .doesNotThrowAnyException();
        assertThatCode(() -> SHARING.assertResponse("DELETE", "/thanks/1", 204, null, "")).doesNotThrowAnyException();
    }

    @Test
    void rejectsABodyBreakingTheSchema() {
        assertThatThrownBy(() -> SHARING.assertResponse("POST", "/thanks", 400, JSON, "{\"message\": \"no code\"}"))
                .hasMessageContaining("code");
        assertThatThrownBy(() -> SHARING.assertResponse("POST", "/thanks", 201, JSON, "{\"thanksLink\": 42}"))
                .hasMessageContaining("thanksLink");
    }

    @Test
    void rejectsAnUndeclaredStatus() {
        assertThatThrownBy(() -> SHARING.assertResponse("POST", "/thanks", 409, JSON, "{\"code\": \"x\", \"message\": \"y\"}"))
                .hasMessageContaining("declares only");
    }

    @Test
    void rejectsAMissingOrUnexpectedBody() {
        assertThatThrownBy(() -> SHARING.assertResponse("GET", "/thanks", 401, null, ""))
                .hasMessageContaining("must have a body");
        assertThatThrownBy(() -> SHARING.assertResponse("DELETE", "/thanks/1", 204, JSON, "{}"))
                .hasMessageContaining("must have no body");
    }
}
