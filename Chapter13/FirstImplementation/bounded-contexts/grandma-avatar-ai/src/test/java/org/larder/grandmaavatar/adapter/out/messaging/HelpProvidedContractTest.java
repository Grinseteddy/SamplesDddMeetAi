package org.larder.grandmaavatar.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.grandmaavatar.TestData.BUTTER;
import static org.larder.grandmaavatar.TestData.BUTTERMILK;
import static org.larder.grandmaavatar.TestData.CORRELATION;
import static org.larder.grandmaavatar.TestData.FOLD_IN;
import static org.larder.grandmaavatar.TestData.SCONES;
import static org.larder.grandmaavatar.TestData.SOUP;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.ContractExamples;
import org.larder.grandmaavatar.TestData;
import org.larder.grandmaavatar.adapter.out.advisor.RecipeBoxAdvisor;
import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.CatastropheMitigation;
import org.larder.grandmaavatar.domain.Help;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.MealType;
import org.larder.grandmaavatar.domain.MenuProposal;
import org.larder.grandmaavatar.domain.MenuProposal.Course;
import org.larder.grandmaavatar.domain.PreparationStepExplanation;
import org.larder.grandmaavatar.domain.SubstituteIngredient;
import org.larder.grandmaavatar.domain.Substitutes;
import org.larder.grandmaavatar.domain.Substitutes.Substitute;
import org.larder.grandmaavatar.domain.Unit;
import org.larder.platform.messaging.MessageHeader;
import org.larder.platform.test.AsyncApiContract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Every kind of Help Grandma can publish satisfies {@code grandma-avatar.asyncapi.yaml}. */
class HelpProvidedContractTest {

    private final AsyncApiContract contract = AsyncApiContract.of("grandma-avatar.asyncapi.yaml");
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void aCatastropheMitigationWithRecipe() throws Exception {
        assertPublishable(help(TestData.burningScones(), new CatastropheMitigation(Optional.of(SCONES), "use a new, cold pan")));
    }

    @Test
    void aCatastropheMitigationWithoutRecipe() throws Exception {
        JsonNode payload = assertPublishable(help(TestData.catastropheWithoutRecipe(),
                new CatastropheMitigation(Optional.empty(), "add a peeled potato")));
        assertThat(payload.at("/answer/catastropheMitigation").has("recipe")).isFalse();
    }

    @Test
    void aPreparationStepExplanation() throws Exception {
        assertPublishable(help(TestData.foldingTheDough(), new PreparationStepExplanation(SCONES, FOLD_IN,
                "Lift and turn with a spatula", List.of(URI.create("https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a")))));
        JsonNode withoutImages = assertPublishable(help(TestData.foldingTheDough(),
                new PreparationStepExplanation(SCONES, FOLD_IN, "Lift and turn", List.of())));
        assertThat(withoutImages.at("/answer/preparationStepExplanation").has("images")).isFalse();
    }

    @Test
    void ingredientSubstitutes() throws Exception {
        JsonNode payload = assertPublishable(help(TestData.noButtermilk(), new Substitutes(SCONES, List.of(
                new Substitute(BUTTERMILK, new SubstituteIngredient("Yoghurt", new BigDecimal("250"), Unit.MILLILITER)),
                new Substitute(BUTTER, new SubstituteIngredient("Margarine", new BigDecimal("0.5"), Unit.CUP))))));
        assertThat(payload.at("/answer/substitutes/substitute/1/substituteIngredient/value").decimalValue())
                .isEqualByComparingTo("0.5");
    }

    @Test
    void aMenuProposal() throws Exception {
        assertPublishable(help(TestData.dinnerForTheInLaws(), new MenuProposal("Be careful, prepare everything", 6,
                MealType.DINNER, Optional.of("hold course 2 warm while serving soup"),
                List.of(new Course(1, SOUP), new Course(2, SCONES), new Course(2, SOUP)))));
        assertPublishable(help(TestData.dinnerForTheInLaws(), new MenuProposal("Keep it simple", 1,
                MealType.SUPPER, Optional.empty(), List.of(new Course(1, SOUP)))));
    }

    @Test
    void theRecipeBoxAnswerToTheBurningSconesIsTheGlossarysStayCalmExample() throws Exception {
        HelpRequest request = TestData.burningScones();
        Help help = Help.answer(request, new RecipeBoxAdvisor().advise(request).orElseThrow());
        JsonNode payload = assertPublishable(help);

        JsonNode example = ContractExamples.notificationsStayCalm();
        for (String field : List.of("helpRequest", "helpRequester", "answerTitle", "helpProviderType")) {
            assertThat(payload.get(field)).as(field).isEqualTo(example.get(field));
        }
        assertThat(payload.at("/answer/answerType")).isEqualTo(example.at("/answer/answerType"));
        assertThat(payload.at("/answer/catastropheMitigation/recipe")).isEqualTo(example.at("/answer/catastropheMitigation/recipe"));
    }

    @Test
    void theContractRejectsWhatGrandmaMustNeverSend() throws Exception {
        String valid = json.writeValueAsString(HelpProvidedMapper.toPayload(
                help(TestData.burningScones(), new CatastropheMitigation(Optional.of(SCONES), "breathe"))));
        assertThatThrownBy(() -> contract.assertPayload("helpProvided", valid.replace("\"GRANDMA_AVATAR\"", "\"COMMUNITY\"")))
                .isInstanceOf(AssertionError.class);
        assertThatThrownBy(() -> contract.assertPayload("helpProvided",
                valid.replace("\"answerType\":\"STEPS_TO_MITIGATE_CATASTROPHE\",\"catastrophe", "\"answerType\":\"MENU_PROPOSAL\",\"catastrophe")))
                .isInstanceOf(AssertionError.class);
    }

    private JsonNode assertPublishable(Help help) throws Exception {
        String payload = json.writeValueAsString(HelpProvidedMapper.toPayload(help));
        contract.assertPayload("helpProvided", payload);
        contract.assertHeaders("helpProvided", MessageHeader.newMessage(CORRELATION, "grandma-avatar").asAmqpHeaders());
        assertThat(contract.messageName("helpProvided")).isEqualTo(OutboxHelpPublisher.MESSAGE_TYPE);

        JsonNode node = json.readTree(payload);
        assertThat(node.get("helpId").asText()).isEqualTo(help.id().value().toString());
        assertThat(node.get("helpProviderType").asText()).isEqualTo("GRANDMA_AVATAR");
        assertThat(node.has("helpProvider")).isFalse();
        assertThat(node.has("helpRequestStatus")).isFalse();
        assertThat(node.at("/answer/answerType").asText()).isEqualTo(help.type().name());
        return node;
    }

    private static Help help(HelpRequest request, org.larder.grandmaavatar.domain.Answer answer) {
        return Help.answer(request, new Advice("Grandma's advice", answer));
    }
}
