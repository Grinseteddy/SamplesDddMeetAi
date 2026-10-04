package org.larder.cookingassistance.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.GRANDMA_HELP_ID;
import static org.larder.cookingassistance.TestData.HELP_REQUEST_ID;
import static org.larder.cookingassistance.TestData.stayCalm;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.GrandmaHelp;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.cookingassistance.domain.IngredientSubstitutes;
import org.larder.cookingassistance.domain.MenuProposal;
import org.larder.cookingassistance.domain.PreparationStepExplanation;
import org.larder.platform.test.AsyncApiContract;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumer side of the Grandma Avatar's HelpProvided (grandma-avatar.asyncapi.yaml): every message the
 * contract allows is read into this context's own payload and translated into the domain.
 */
class GrandmaHelpProvidedContractTest {

    private static final AsyncApiContract GRANDMA = AsyncApiContract.of("grandma-avatar.asyncapi.yaml");
    /** Like Spring Boot's ObjectMapper, which the platform's message converter uses. */
    private final ObjectMapper json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private GrandmaHelpProvidedPayload read(String message) throws Exception {
        return json.readValue(message, GrandmaHelpProvidedPayload.class);
    }

    static List<String> grandmaMessages() {
        return List.of(GrandmaMessages.STAY_CALM, GrandmaMessages.YOGHURT, GrandmaMessages.COLD_PAN, GrandmaMessages.MENU);
    }

    @ParameterizedTest
    @MethodSource("grandmaMessages")
    void theTestMessagesAreValidGrandmaMessages(String message) {
        assertThatCode(() -> GRANDMA.assertPayload("helpProvided", message)).doesNotThrowAnyException();
        assertThat(GRANDMA.messageName("helpProvided")).isEqualTo("HelpProvided");
    }

    @Test
    void theContractsHeadersAreRead() {
        Map<String, Object> headers = Map.of("correlationId", HELP_REQUEST_ID.toString(),
                "messageId", "5c2f0e9a-1d6b-4f3c-9a0e-2b7d1e4c8f10", "source", "grandma-avatar");

        assertThatCode(() -> GRANDMA.assertHeaders("helpProvided", headers)).doesNotThrowAnyException();
    }

    @Test
    void stayCalmIsTranslatedIntoTheDomain() throws Exception {
        GrandmaHelp help = read(GrandmaMessages.STAY_CALM).toGrandmaHelp();

        assertThat(help.helpId().value()).isEqualTo(GRANDMA_HELP_ID);
        assertThat(help.helpRequest().value()).isEqualTo(HELP_REQUEST_ID);
        assertThat(help.helpRequester()).isEqualTo(COOK);
        assertThat(help.answerTitle()).isEqualTo("Stay calm");
        assertThat(help.answer()).isEqualTo(stayCalm());
    }

    @Test
    void everyAnswerKindIsTranslated() throws Exception {
        assertThat(read(GrandmaMessages.YOGHURT).toGrandmaHelp().answer()).isInstanceOf(IngredientSubstitutes.class);
        assertThat(read(GrandmaMessages.COLD_PAN).toGrandmaHelp().answer()).isInstanceOfSatisfying(
                PreparationStepExplanation.class, p -> assertThat(p.images()).hasSize(1));
        assertThat(read(GrandmaMessages.MENU).toGrandmaHelp().answer()).isInstanceOfSatisfying(
                MenuProposal.class, m -> assertThat(m.type()).isEqualTo(HelpType.MENU_PROPOSAL));
    }

    @Test
    void aMessageThatIsNotTheAvatarsOrHasTheWrongBodyIsMalformed() throws Exception {
        String community = GrandmaMessages.STAY_CALM.replace("\"GRANDMA_AVATAR\"", "\"COMMUNITY\"");
        String wrongBody = GrandmaMessages.STAY_CALM.replace("\"answerType\":\"STEPS_TO_MITIGATE_CATASTROPHE\",\n",
                "\"answerType\":\"MENU_PROPOSAL\",\n");

        assertThatThrownBy(() -> read(community).toGrandmaHelp()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> read(wrongBody).toGrandmaHelp()).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> read("{\"helpId\":\"" + GRANDMA_HELP_ID + "\"}").toGrandmaHelp())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
