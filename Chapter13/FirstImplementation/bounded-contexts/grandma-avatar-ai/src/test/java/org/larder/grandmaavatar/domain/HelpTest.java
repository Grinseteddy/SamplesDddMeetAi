package org.larder.grandmaavatar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.grandmaavatar.TestData.BAKE;
import static org.larder.grandmaavatar.TestData.BUTTER;
import static org.larder.grandmaavatar.TestData.BUTTERMILK;
import static org.larder.grandmaavatar.TestData.COOK;
import static org.larder.grandmaavatar.TestData.FOLD_IN;
import static org.larder.grandmaavatar.TestData.SCONES;
import static org.larder.grandmaavatar.TestData.SOUP;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.TestData;
import org.larder.grandmaavatar.domain.MenuProposal.Course;
import org.larder.grandmaavatar.domain.Substitutes.Substitute;

class HelpTest {

    private static final SubstituteIngredient YOGHURT = new SubstituteIngredient("Yoghurt", new BigDecimal("250"), Unit.MILLILITER);
    private static final SubstituteIngredient MARGARINE = new SubstituteIngredient("Margarine", new BigDecimal("100"), Unit.GRAM);

    @Test
    void answersACatastropheWithTheRequestsRecipe() {
        HelpRequest request = TestData.burningScones();
        Help help = Help.answer(request, new Advice("Stay calm", new CatastropheMitigation(Optional.of(SCONES), "use a new, cold pan")));

        assertThat(help.id()).isEqualTo(HelpId.forRequest(request.id()));
        assertThat(help.helpRequest()).isEqualTo(request.id());
        assertThat(help.helpRequester()).isEqualTo(COOK);
        assertThat(help.answerTitle()).isEqualTo("Stay calm");
        assertThat(help.type()).isEqualTo(HelpType.STEPS_TO_MITIGATE_CATASTROPHE);
        assertThat(help.providerType()).isEqualTo(HelpProviderType.GRANDMA_AVATAR);
    }

    @Test
    void aCatastropheMitigationMayLeaveTheRecipeOutButNotNameAnother() {
        HelpRequest request = TestData.burningScones();
        assertThat(Help.answer(request, new Advice("Stay calm", new CatastropheMitigation(Optional.empty(), "breathe")))
                .type()).isEqualTo(HelpType.STEPS_TO_MITIGATE_CATASTROPHE);
        assertRejected(request, new CatastropheMitigation(Optional.of(SOUP), "breathe"));
        assertRejected(TestData.catastropheWithoutRecipe(), new CatastropheMitigation(Optional.of(SCONES), "breathe"));
    }

    @Test
    void explainsTheRequestedStepOfTheRequestedRecipe() {
        HelpRequest request = TestData.foldingTheDough();
        Help help = Help.answer(request, new Advice("Fold gently", new PreparationStepExplanation(SCONES, FOLD_IN,
                "Lift and turn with a spatula", List.of(URI.create("https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a")))));
        assertThat(help.type()).isEqualTo(HelpType.PREPARATION_STEP_EXPLANATION);

        assertRejected(request, new PreparationStepExplanation(SOUP, FOLD_IN, "x", List.of()));
        assertRejected(request, new PreparationStepExplanation(SCONES, BAKE, "x", List.of()));
    }

    @Test
    void substitutesExactlyTheRequestedIngredientsOfTheRequestedRecipe() {
        HelpRequest request = TestData.noButtermilk();
        Help help = Help.answer(request, new Advice("Use yoghurt", new Substitutes(SCONES,
                List.of(new Substitute(BUTTERMILK, YOGHURT), new Substitute(BUTTER, MARGARINE)))));
        assertThat(help.type()).isEqualTo(HelpType.INGREDIENT_SUBSTITUTE);

        assertRejected(request, new Substitutes(SOUP, List.of(new Substitute(BUTTERMILK, YOGHURT), new Substitute(BUTTER, MARGARINE))));
        assertRejected(request, new Substitutes(SCONES, List.of(new Substitute(BUTTERMILK, YOGHURT))));
        assertRejected(request, new Substitutes(SCONES, List.of(new Substitute(BUTTERMILK, YOGHURT),
                new Substitute(BUTTER, MARGARINE), new Substitute(BUTTER, YOGHURT))));
        assertRejected(request, new Substitutes(SCONES, List.of(new Substitute(BUTTERMILK, YOGHURT),
                new Substitute(new IngredientId(java.util.UUID.randomUUID()), MARGARINE))));
    }

    @Test
    void proposesAMenu() {
        Help help = Help.answer(TestData.dinnerForTheInLaws(), new Advice("A cosy dinner", new MenuProposal(
                "Be careful, prepare everything", 6, MealType.DINNER, Optional.of("hold course 2 warm while serving soup"),
                List.of(new Course(1, SOUP), new Course(2, SCONES)))));
        assertThat(help.type()).isEqualTo(HelpType.MENU_PROPOSAL);
    }

    @Test
    void theAnswerTypeEqualsTheRequestType() {
        assertRejected(TestData.burningScones(), new PreparationStepExplanation(SCONES, FOLD_IN, "x", List.of()));
        assertRejected(TestData.foldingTheDough(), new CatastropheMitigation(Optional.of(SCONES), "x"));
    }

    @Test
    void grandmaDoesNotAnswerRequestsThatAreNotForHer() {
        assertRejected(TestData.chefOnlyMenu(), new MenuProposal("note", 6, MealType.DINNER, Optional.empty(),
                List.of(new Course(1, SOUP))));
    }

    @Test
    void theHelpIdIsDerivedFromTheRequest() {
        HelpRequestId request = TestData.newId();
        assertThat(HelpId.forRequest(request)).isEqualTo(HelpId.forRequest(request));
        assertThat(HelpId.forRequest(request)).isNotEqualTo(HelpId.forRequest(TestData.newId()));
        assertThat(HelpId.forRequest(request).value()).isNotEqualTo(request.value());
    }

    @Test
    void answersRespectTheLimitsOfTheContract() {
        assertThatThrownBy(() -> new Advice("", new CatastropheMitigation(Optional.empty(), "x")))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new Advice("x".repeat(201), new CatastropheMitigation(Optional.empty(), "x")))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new CatastropheMitigation(Optional.empty(), "x".repeat(2001)))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new CatastropheMitigation(Optional.empty(), " "))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new SubstituteIngredient("Yoghurt", BigDecimal.ZERO, Unit.GRAM))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new SubstituteIngredient("x".repeat(101), BigDecimal.ONE, Unit.GRAM))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new Substitutes(SCONES, List.of())).isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new PreparationStepExplanation(SCONES, FOLD_IN, "x", List.of(URI.create("images/1"))))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new MenuProposal("note", 0, MealType.DINNER, Optional.empty(), List.of(new Course(1, SOUP))))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new MenuProposal("note", 2, MealType.DINNER, Optional.empty(), List.of()))
                .isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new MenuProposal("note", 2, MealType.DINNER, Optional.empty(),
                java.util.Collections.nCopies(11, new Course(1, SOUP)))).isInstanceOf(InvalidAnswerException.class);
        assertThatThrownBy(() -> new Course(0, SOUP)).isInstanceOf(InvalidAnswerException.class);
    }

    @Test
    void onlyAnsweredRequestsCarryAHelp() {
        HelpRequest request = TestData.burningScones();
        Help help = Help.answer(request, new Advice("Stay calm", new CatastropheMitigation(Optional.empty(), "breathe")));
        HandledHelpRequest answered = HandledHelpRequest.answered(help, java.time.Instant.EPOCH);
        assertThat(answered.outcome()).isEqualTo(Outcome.ANSWERED);
        assertThat(answered.help()).contains(help.id());
        assertThat(answered.answerTitle()).contains("Stay calm");

        HandledHelpRequest ignored = HandledHelpRequest.notAnswered(request, Outcome.NO_ADVICE, java.time.Instant.EPOCH);
        assertThat(ignored.help()).isEmpty();
        assertThatThrownBy(() -> HandledHelpRequest.notAnswered(request, Outcome.ANSWERED, java.time.Instant.EPOCH))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static void assertRejected(HelpRequest request, Answer answer) {
        assertThatThrownBy(() -> Help.answer(request, new Advice("Title", answer))).isInstanceOf(InvalidAnswerException.class);
    }
}
