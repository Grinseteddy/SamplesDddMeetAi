package org.larder.notification.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP;
import static org.larder.notification.TestData.HELP_LINK;
import static org.larder.notification.TestData.HELP_REQUEST;
import static org.larder.notification.TestData.NOW;
import static org.larder.notification.TestData.STAY_CALM_MESSAGE;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.larder.notification.application.InMemoryNotifications;
import org.larder.notification.application.NotificationService;
import org.larder.notification.domain.Status;
import org.larder.platform.test.AsyncApiContract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Notification conforms to Cooking Assistance's published language: the contract's own examples
 * satisfy the contract, are read into the payload records without losing a field, and are handled.
 */
class CookingAssistanceContractTest {

    /** Fails on properties the records do not know - unlike the application's tolerant mapper. */
    private static final ObjectMapper STRICT = new ObjectMapper();

    private final AsyncApiContract contract = AsyncApiContract.of(ContractExamples.CONTRACT);
    private final InMemoryNotifications notifications = new InMemoryNotifications();
    private final CookingAssistanceEventsListener listener = new CookingAssistanceEventsListener(new NotificationService(
            notifications, helpId -> URI.create("https://larder.org/cooking-assistance/helps/" + helpId),
            Clock.fixed(NOW, ZoneOffset.UTC)));

    private final JsonNode stayCalm = ContractExamples.example("helpProvided", "stayCalm");
    private final JsonNode burningCatastrophe = ContractExamples.example("helpRequested", "burningCatastrophe");

    @Test
    void theExamplesSatisfyTheContract() {
        contract.assertPayload("helpProvided", ContractExamples.payloadJson(stayCalm));
        contract.assertHeaders("helpProvided", ContractExamples.header(stayCalm).asAmqpHeaders());
        contract.assertPayload("helpRequested", ContractExamples.payloadJson(burningCatastrophe));
        contract.assertHeaders("helpRequested", ContractExamples.header(burningCatastrophe).asAmqpHeaders());
        assertThat(contract.messageName("helpProvided")).isEqualTo("HelpProvided");
        assertThat(contract.messageName("helpRequested")).isEqualTo("HelpRequested");
    }

    @Test
    void stayCalmIsReadIntoThePayloadRecord() throws Exception {
        HelpProvidedPayload payload = STRICT.readValue(ContractExamples.payloadJson(stayCalm), HelpProvidedPayload.class);

        assertThat(payload.helpId()).isEqualTo(HELP);
        assertThat(payload.helpRequest()).isEqualTo(HELP_REQUEST);
        assertThat(payload.helpRequester()).isEqualTo(COOK.value());
        assertThat(payload.answerTitle()).isEqualTo("Stay calm");
        assertThat(payload.helpProviderType()).isEqualTo(HelpProviderType.GRANDMA_AVATAR);
        assertThat(payload.helpRequestStatus()).isEqualTo("ANSWERED");
        assertThat(payload.answer().path("catastropheMitigation").path("explanation").asText()).isEqualTo("use a new, cold pan");
        ObjectMapper withoutNulls = STRICT.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        assertThat(withoutNulls.readTree(withoutNulls.writeValueAsString(payload))).isEqualTo(stayCalm.get("payload"));
    }

    @Test
    void burningCatastropheIsReadIntoThePayloadRecord() throws Exception {
        HelpRequestedPayload payload = STRICT.readValue(ContractExamples.payloadJson(burningCatastrophe), HelpRequestedPayload.class);

        assertThat(payload.helpRequestId()).isEqualTo(HELP_REQUEST);
        assertThat(payload.requester()).isEqualTo(COOK.value());
        assertThat(payload.type()).isEqualTo(HelpType.STEPS_TO_MITIGATE_CATASTROPHE);
        assertThat(payload.preferredProvider()).containsExactly(HelpProviderType.GRANDMA_AVATAR, HelpProviderType.COMMUNITY);
        assertThat(payload.status()).isEqualTo("OPEN");
    }

    @Test
    void stayCalmNotifiesTheRequester() throws Exception {
        HelpProvidedPayload payload = STRICT.readValue(ContractExamples.payloadJson(stayCalm), HelpProvidedPayload.class);

        listener.onHelpProvided(payload, ContractExamples.message("HelpProvided", stayCalm));

        assertThat(notifications.all()).singleElement().satisfies(notification -> {
            assertThat(notification.origin()).isEqualTo(STAY_CALM_MESSAGE);
            assertThat(notification.receivers()).containsExactly(COOK);
            assertThat(notification.title()).isEqualTo("Help provided by the Grandma Avatar");
            assertThat(notification.text()).contains("\"Stay calm\"");
            assertThat(notification.link()).isEqualTo(HELP_LINK);
            assertThat(notification.statusFor(COOK)).isEqualTo(Status.NEW);
        });
    }

    @Test
    void burningCatastropheIsAcceptedWithoutANotification() throws Exception {
        HelpRequestedPayload payload = STRICT.readValue(ContractExamples.payloadJson(burningCatastrophe), HelpRequestedPayload.class);

        listener.onHelpRequested(payload, ContractExamples.message("HelpRequested", burningCatastrophe));

        assertThat(notifications.all()).isEmpty();
    }
}
