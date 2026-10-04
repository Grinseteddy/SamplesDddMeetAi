package org.larder.grandmaavatar.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.ContractExamples;
import org.larder.grandmaavatar.adapter.out.advisor.RecipeBoxAdvisor;
import org.larder.grandmaavatar.application.HandledRequestRecorder;
import org.larder.grandmaavatar.application.HelpRequestHandler;
import org.larder.grandmaavatar.application.InMemoryHandledHelpRequests;
import org.larder.grandmaavatar.application.RecordingHelpPublisher;
import org.larder.grandmaavatar.domain.HelpRequestId;
import org.larder.grandmaavatar.domain.HelpType;
import org.larder.grandmaavatar.domain.Outcome;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.larder.platform.test.AsyncApiContract;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The contracts' own HelpRequested examples are valid for Grandma's contract and are consumed as
 * intended; messages breaking the contract are acknowledged (no exception) and change nothing.
 */
class HelpRequestedContractTest {

    private final AsyncApiContract contract = AsyncApiContract.of("grandma-avatar.asyncapi.yaml");
    private final ObjectMapper json = new ObjectMapper();
    private final InMemoryHandledHelpRequests notebook = new InMemoryHandledHelpRequests();
    private final RecordingHelpPublisher publisher = new RecordingHelpPublisher();
    private final HelpRequestedListener listener = new HelpRequestedListener(new HelpRequestHandler(notebook,
            new RecipeBoxAdvisor(), new HandledRequestRecorder(notebook, publisher), Clock.systemUTC()), json);

    @Test
    void theBurningCatastropheOfNotificationsIsAnsweredWithItsCorrelation() throws Exception {
        JsonNode example = ContractExamples.notificationsHelpRequested().get(0);
        assertThat(example.get("name").asText()).isEqualTo("burningCatastrophe");
        Map<String, Object> headers = json.convertValue(example.get("headers"), Map.class);
        contract.assertPayload("helpRequested", example.get("payload"));
        contract.assertHeaders("helpRequested", headers);

        listener.onHelpRequested(message(example.get("headers"), example.get("payload")));

        assertThat(publisher.published).hasSize(1);
        var published = publisher.published.get(0);
        assertThat(published.correlationId()).isEqualTo(UUID.fromString(example.at("/headers/correlationId").asText()));
        assertThat(published.help().type()).isEqualTo(HelpType.STEPS_TO_MITIGATE_CATASTROPHE);
        assertThat(published.help().answerTitle()).isEqualTo("Stay calm");
    }

    @Test
    void oneRequestPerHelpTypeFromCookingAssistanceIsConsumedAsTheContractSays() throws Exception {
        var requests = ContractExamples.cookingAssistanceHelpRequests();
        assertThat(requests).extracting(it -> it.get("type").asText()).containsExactlyInAnyOrder(
                "STEPS_TO_MITIGATE_CATASTROPHE", "INGREDIENT_SUBSTITUTE", "PREPARATION_STEP_EXPLANATION", "MENU_PROPOSAL");

        for (ObjectNode request : requests) {
            contract.assertPayload("helpRequested", request);
            listener.onHelpRequested(message(headers(), request));
        }

        // Burning catastrophe (Grandma + community): answered. Folding the dough (Grandma): answered.
        // No buttermilk (community only) and the chef-only menu: none of Grandma's business.
        assertThat(outcomeOf(requests.get(0))).isEqualTo(Outcome.ANSWERED);
        assertThat(outcomeOf(requests.get(1))).isEqualTo(Outcome.NOT_FOR_GRANDMA);
        assertThat(outcomeOf(requests.get(2))).isEqualTo(Outcome.ANSWERED);
        assertThat(outcomeOf(requests.get(3))).isEqualTo(Outcome.NOT_FOR_GRANDMA);
        assertThat(publisher.published).extracting(it -> it.help().type())
                .containsExactly(HelpType.STEPS_TO_MITIGATE_CATASTROPHE, HelpType.PREPARATION_STEP_EXPLANATION);
    }

    @Test
    void requestsGrandmaCannotAnswerAreNotedButNotAnswered() throws Exception {
        ObjectNode buttermilk = ContractExamples.cookingAssistanceHelpRequests().get(1);
        buttermilk.putArray("preferredProvider").add("GRANDMA_AVATAR");
        ObjectNode menu = ContractExamples.cookingAssistanceHelpRequests().get(3);
        menu.putArray("preferredProvider").add("GRANDMA_AVATAR").add("COMMUNITY");
        for (ObjectNode request : java.util.List.of(buttermilk, menu)) {
            contract.assertPayload("helpRequested", request);
            listener.onHelpRequested(message(headers(), request));
            assertThat(outcomeOf(request)).isEqualTo(Outcome.NO_ADVICE);
        }
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void messagesBreakingTheContractAreAcknowledgedAndChangeNothing() throws Exception {
        ObjectNode valid = ContractExamples.cookingAssistanceHelpRequests().get(0);
        ObjectNode chefForACatastrophe = valid.deepCopy();
        chefForACatastrophe.putArray("preferredProvider").add("CHEF");
        ObjectNode unknownType = valid.deepCopy();
        unknownType.put("type", "WINE_PAIRING");
        ObjectNode withoutRequester = valid.deepCopy();
        withoutRequester.remove("requester");
        ObjectNode repeatedProvider = valid.deepCopy();
        repeatedProvider.putArray("preferredProvider").add("GRANDMA_AVATAR").add("GRANDMA_AVATAR");

        for (ObjectNode broken : java.util.List.of(chefForACatastrophe, unknownType, withoutRequester, repeatedProvider)) {
            assertThatThrownBy(() -> contract.assertPayload("helpRequested", broken)).isInstanceOf(AssertionError.class);
            assertThatCode(() -> listener.onHelpRequested(message(headers(), broken))).doesNotThrowAnyException();
        }
        assertThatCode(() -> listener.onHelpRequested(raw(headers(), "not json"))).doesNotThrowAnyException();
        assertThatCode(() -> listener.onHelpRequested(raw(json.createObjectNode(), json.writeValueAsString(valid))))
                .doesNotThrowAnyException();

        assertThat(notebook.size()).isZero();
        assertThat(publisher.published).isEmpty();
    }

    private Outcome outcomeOf(JsonNode request) {
        return notebook.find(new HelpRequestId(UUID.fromString(request.get("helpRequestId").asText())))
                .orElseThrow().outcome();
    }

    private ObjectNode headers() {
        ObjectNode headers = json.createObjectNode();
        headers.put("correlationId", UUID.randomUUID().toString());
        headers.put("messageId", UUID.randomUUID().toString());
        headers.put("source", "cooking-assistance");
        return headers;
    }

    private Message message(JsonNode headers, JsonNode payload) throws Exception {
        return ContractMessage.of("HelpRequested", MessageHeader.fromAmqpHeaders(json.convertValue(headers, Map.class)),
                json.writeValueAsString(payload));
    }

    @SuppressWarnings("unchecked")
    private Message raw(JsonNode headers, String body) {
        MessageProperties properties = new MessageProperties();
        json.convertValue(headers, Map.class).forEach((key, value) -> properties.setHeader((String) key, value));
        return MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8)).andProperties(properties).build();
    }
}
