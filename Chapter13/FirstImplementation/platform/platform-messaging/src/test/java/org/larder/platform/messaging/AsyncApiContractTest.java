package org.larder.platform.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.larder.platform.test.AsyncApiContract;

/** The contract checker resolves $refs across contract files (Notifications → Cooking Assistance). */
class AsyncApiContractTest {

    private static final String BURNING_CATASTROPHE = """
            {"helpRequestId":"23a8eeed-35f6-460b-892e-7bb458a8fded","requester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074",
             "title":"Burning Catastrophe","type":"STEPS_TO_MITIGATE_CATASTROPHE",
             "description":"Scones are burned and mother in law is coming in 30 minutes",
             "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913","preferredProvider":["GRANDMA_AVATAR","COMMUNITY"],"status":"OPEN"}
            """;

    private final AsyncApiContract notifications = AsyncApiContract.of("notifications.asyncapi.yaml");

    @Test
    void acceptsTheContractsOwnExample() {
        assertThatCode(() -> notifications.assertPayload("helpRequested", BURNING_CATASTROPHE)).doesNotThrowAnyException();
        assertThatCode(() -> notifications.assertHeaders("helpRequested", Map.of(
                "correlationId", "23a8eeed-35f6-460b-892e-7bb458a8fded",
                "messageId", "5c2f0e9a-1d6b-4f3c-9a0e-2b7d1e4c8f10",
                "source", "cooking-assistance"))).doesNotThrowAnyException();
    }

    @Test
    void rejectsAPayloadThatBreaksAnInvariantOfThePublishedLanguage() {
        String chefForACatastrophe = BURNING_CATASTROPHE.replace("[\"GRANDMA_AVATAR\",\"COMMUNITY\"]", "[\"CHEF\"]");
        assertThatThrownBy(() -> notifications.assertPayload("helpRequested", chefForACatastrophe))
                .isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> notifications.assertPayload("helpRequested", "{\"title\":\"incomplete\"}"))
                .isInstanceOf(AssertionError.class).hasMessageContaining("helpRequestId");
    }
}
