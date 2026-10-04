package org.larder.grandmaavatar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.grandmaavatar.TestData.BUTTERMILK;
import static org.larder.grandmaavatar.TestData.COOK;
import static org.larder.grandmaavatar.TestData.FOLD_IN;
import static org.larder.grandmaavatar.TestData.SCONES;
import static org.larder.grandmaavatar.TestData.newId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.TestData;

class HelpRequestTest {

    private static final Set<HelpProviderType> GRANDMA = Set.of(HelpProviderType.GRANDMA_AVATAR);

    @Test
    void grandmaAnswersOpenRequestsThatNameHer() {
        assertThat(TestData.burningScones().isForGrandma()).isTrue();
        assertThat(TestData.foldingTheDough().isForGrandma()).isTrue();
    }

    @Test
    void aChefOnlyRequestIsNotForGrandma() {
        assertThat(TestData.chefOnlyMenu().isForGrandma()).isFalse();
    }

    @Test
    void aCommunityOnlyRequestIsNotForGrandma() {
        assertThat(request(HelpType.STEPS_TO_MITIGATE_CATASTROPHE, Optional.empty(), Optional.empty(), List.of(),
                Set.of(HelpProviderType.COMMUNITY), true).isForGrandma()).isFalse();
    }

    @Test
    void aRequestNoLongerOpenIsNotForGrandma() {
        assertThat(request(HelpType.STEPS_TO_MITIGATE_CATASTROPHE, Optional.empty(), Optional.empty(), List.of(),
                GRANDMA, false).isForGrandma()).isFalse();
    }

    @Test
    void aStepExplanationNeedsRecipeAndStep() {
        assertInvalid(HelpType.PREPARATION_STEP_EXPLANATION, Optional.empty(), Optional.of(FOLD_IN), List.of(), GRANDMA);
        assertInvalid(HelpType.PREPARATION_STEP_EXPLANATION, Optional.of(SCONES), Optional.empty(), List.of(), GRANDMA);
        assertInvalid(HelpType.PREPARATION_STEP_EXPLANATION, Optional.of(SCONES), Optional.of(FOLD_IN), List.of(BUTTERMILK), GRANDMA);
    }

    @Test
    void aSubstituteRequestNeedsTheRecipeAndNoStep() {
        assertInvalid(HelpType.INGREDIENT_SUBSTITUTE, Optional.empty(), Optional.empty(), List.of(BUTTERMILK), GRANDMA);
        assertInvalid(HelpType.INGREDIENT_SUBSTITUTE, Optional.of(SCONES), Optional.of(FOLD_IN), List.of(BUTTERMILK), GRANDMA);
    }

    @Test
    void aCatastropheOrAMenuNamesNeitherStepNorIngredients() {
        for (HelpType type : List.of(HelpType.STEPS_TO_MITIGATE_CATASTROPHE, HelpType.MENU_PROPOSAL)) {
            assertInvalid(type, Optional.of(SCONES), Optional.of(FOLD_IN), List.of(), GRANDMA);
            assertInvalid(type, Optional.of(SCONES), Optional.empty(), List.of(BUTTERMILK), GRANDMA);
        }
    }

    @Test
    void aChefOnlyAnswersMenusAndThenExclusively() {
        assertInvalid(HelpType.STEPS_TO_MITIGATE_CATASTROPHE, Optional.empty(), Optional.empty(), List.of(),
                Set.of(HelpProviderType.CHEF));
        assertInvalid(HelpType.MENU_PROPOSAL, Optional.empty(), Optional.empty(), List.of(),
                Set.of(HelpProviderType.CHEF, HelpProviderType.GRANDMA_AVATAR));
    }

    @Test
    void oneOrTwoPreferredProviders() {
        assertInvalid(HelpType.STEPS_TO_MITIGATE_CATASTROPHE, Optional.empty(), Optional.empty(), List.of(), Set.of());
        assertInvalid(HelpType.MENU_PROPOSAL, Optional.empty(), Optional.empty(), List.of(), Set.of(HelpProviderType.values()));
    }

    @Test
    void titleAndDescriptionFollowTheContract() {
        assertThatThrownBy(() -> new HelpRequest(newId(), COOK, "", HelpType.MENU_PROPOSAL, "x", Optional.empty(),
                Optional.empty(), List.of(), GRANDMA, true)).isInstanceOf(InvalidHelpRequestException.class);
        assertThatThrownBy(() -> new HelpRequest(newId(), COOK, "x".repeat(201), HelpType.MENU_PROPOSAL, "x",
                Optional.empty(), Optional.empty(), List.of(), GRANDMA, true)).isInstanceOf(InvalidHelpRequestException.class);
        assertThatThrownBy(() -> new HelpRequest(newId(), COOK, "x", HelpType.MENU_PROPOSAL, "x".repeat(2001),
                Optional.empty(), Optional.empty(), List.of(), GRANDMA, true)).isInstanceOf(InvalidHelpRequestException.class);
        assertThat(new HelpRequest(newId(), COOK, "x".repeat(200), HelpType.MENU_PROPOSAL, "x".repeat(2000),
                Optional.empty(), Optional.empty(), List.of(), GRANDMA, true).isForGrandma()).isTrue();
    }

    @Test
    void anEmptyIngredientListIsAllowedBecauseTheContractAllowsIt() {
        assertThat(request(HelpType.INGREDIENT_SUBSTITUTE, Optional.of(SCONES), Optional.empty(), List.of(), GRANDMA, true)
                .ingredients()).isEmpty();
    }

    private static void assertInvalid(HelpType type, Optional<RecipeId> recipe, Optional<HowToStepId> step,
                                      List<IngredientId> ingredients, Set<HelpProviderType> providers) {
        assertThatThrownBy(() -> request(type, recipe, step, ingredients, providers, true))
                .isInstanceOf(InvalidHelpRequestException.class);
    }

    private static HelpRequest request(HelpType type, Optional<RecipeId> recipe, Optional<HowToStepId> step,
                                       List<IngredientId> ingredients, Set<HelpProviderType> providers, boolean open) {
        return new HelpRequest(newId(), COOK, "Help", type, "Something went wrong", recipe, step, ingredients, providers, open);
    }
}
