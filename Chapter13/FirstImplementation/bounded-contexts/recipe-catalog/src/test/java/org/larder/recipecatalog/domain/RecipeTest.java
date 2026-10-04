package org.larder.recipecatalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.recipecatalog.TestData.BAKE_ID;
import static org.larder.recipecatalog.TestData.BUTTER_ID;
import static org.larder.recipecatalog.TestData.COOK;
import static org.larder.recipecatalog.TestData.FLOUR_ID;
import static org.larder.recipecatalog.TestData.MIX_ID;
import static org.larder.recipecatalog.TestData.OTHER_COOK;
import static org.larder.recipecatalog.TestData.SCONES;
import static org.larder.recipecatalog.TestData.scones;
import static org.larder.recipecatalog.TestData.sconesDraft;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class RecipeTest {

    @Test
    void aCookCreatesARecipeOfTheirOwnWithIngredientsAndSteps() {
        Recipe recipe = Recipe.create(COOK, sconesDraft());

        assertThat(recipe.isOwnedBy(COOK)).isTrue();
        assertThat(recipe.isOwnedBy(OTHER_COOK)).isFalse();
        assertThat(recipe.name()).isEqualTo(SCONES);
        assertThat(recipe.preparationTime()).hasToString("02:00");
        assertThat(recipe.meal()).isEqualTo(Meal.BREAKFAST);
        assertThat(recipe.ingredients()).extracting(Ingredient::name).containsExactly("Flour", "Butter");
        assertThat(recipe.howToSteps()).extracting(HowToStep::sequenceNumber).containsExactly(1, 2);
        assertThat(recipe.ingredients()).extracting(Ingredient::id).doesNotHaveDuplicates();
    }

    @Test
    void aRecipeNeedsAtLeastOneIngredientAndOneStep() {
        RecipeDraft draft = sconesDraft();
        RecipeDraft noIngredients = new RecipeDraft(draft.name(), null, null, null, draft.preparationTime(), 4,
                draft.meal(), draft.diet(), List.of(), draft.howToSteps());
        RecipeDraft noSteps = new RecipeDraft(draft.name(), null, null, null, draft.preparationTime(), 4,
                draft.meal(), draft.diet(), draft.ingredients(), List.of());

        assertRuleViolated(() -> Recipe.create(COOK, noIngredients), RecipeRuleViolationException.RECIPE_NEEDS_INGREDIENT);
        assertRuleViolated(() -> Recipe.create(COOK, noSteps), RecipeRuleViolationException.RECIPE_NEEDS_HOW_TO_STEP);
    }

    @Test
    void sequenceNumbersAreUniqueWithinARecipe() {
        RecipeDraft draft = sconesDraft();
        RecipeDraft twiceOne = new RecipeDraft(draft.name(), null, null, null, draft.preparationTime(), 4,
                draft.meal(), draft.diet(), draft.ingredients(),
                List.of(new HowToStepDraft(1, "Mix", null), new HowToStepDraft(1, "Bake", null)));
        assertRuleViolated(() -> Recipe.create(COOK, twiceOne), RecipeRuleViolationException.SEQUENCE_NUMBER_TAKEN);

        Recipe recipe = scones();
        assertRuleViolated(() -> recipe.addHowToStep(new HowToStepDraft(2, "Serve", null)),
                RecipeRuleViolationException.SEQUENCE_NUMBER_TAKEN);
        assertRuleViolated(() -> recipe.changeHowToStep(BAKE_ID, 1, "Bake longer", null),
                RecipeRuleViolationException.SEQUENCE_NUMBER_TAKEN);
        assertThat(recipe.howToStep(BAKE_ID).orElseThrow().description()).isEqualTo("Bake for 15 minutes");
    }

    @Test
    void howToStepsComeInTheOrderOfTheirSequenceNumbers() {
        Recipe recipe = scones();
        recipe.changeHowToStep(MIX_ID, 3, null, null);
        recipe.addHowToStep(new HowToStepDraft(1, "Preheat the oven", URI.create("https://larder.org/media/images/oven.jpg")));

        assertThat(recipe.howToSteps()).extracting(HowToStep::description)
                .containsExactly("Preheat the oven", "Bake for 15 minutes", "Carefully mix the water with the flour");
        assertThat(recipe.changeHowToStep(BAKE_ID, 2, null, null).sequenceNumber()).isEqualTo(2);
    }

    @Test
    void theLastIngredientAndTheLastStepCannotBeRemoved() {
        Recipe recipe = scones();
        recipe.removeIngredient(BUTTER_ID);
        recipe.removeHowToStep(BAKE_ID);

        assertRuleViolated(() -> recipe.removeIngredient(FLOUR_ID), RecipeRuleViolationException.RECIPE_NEEDS_INGREDIENT);
        assertRuleViolated(() -> recipe.removeHowToStep(MIX_ID), RecipeRuleViolationException.RECIPE_NEEDS_HOW_TO_STEP);
        assertThat(recipe.ingredients()).hasSize(1);
        assertThat(recipe.howToSteps()).hasSize(1);
    }

    @Test
    void ingredientsHaveANameAndAPositiveValue() {
        assertRuleViolated(() -> Quantity.of("0", Unit.GRAM), RecipeRuleViolationException.INVALID_RECIPE);
        assertRuleViolated(() -> Quantity.of("-1", Unit.GRAM), RecipeRuleViolationException.INVALID_RECIPE);
        assertRuleViolated(() -> new Quantity(java.math.BigDecimal.ONE, null), RecipeRuleViolationException.INVALID_RECIPE);

        Recipe recipe = scones();
        assertRuleViolated(() -> recipe.addIngredient(new IngredientDraft(" ", Quantity.of("1", Unit.PINCH))),
                RecipeRuleViolationException.INVALID_RECIPE);
        assertThat(recipe.ingredients()).hasSize(2);
    }

    @Test
    void anIngredientChangesNameAndQuantityThroughItsRecipe() {
        Recipe recipe = scones();

        recipe.changeIngredient(FLOUR_ID, "Spelt flour", null);
        recipe.changeIngredient(BUTTER_ID, null, Quantity.of("0.125", Unit.KILOGRAM));

        assertThat(recipe.ingredient(FLOUR_ID).orElseThrow().name()).isEqualTo("Spelt flour");
        assertThat(recipe.ingredient(FLOUR_ID).orElseThrow().quantity().isSameAs(Quantity.of("0.50", Unit.KILOGRAM))).isTrue();
        assertThat(recipe.ingredient(BUTTER_ID).orElseThrow().quantity().unit()).isEqualTo(Unit.KILOGRAM);
        assertThat(recipe.contains("spelt FLOUR ")).isTrue();
        assertThat(recipe.contains("Flour")).isFalse();
    }

    @Test
    void aRevisionChangesOnlyTheGivenDetailsAndIsAllOrNothing() {
        Recipe recipe = scones();

        recipe.revise(RecipeRevision.none().withServings(6).withDiet(Diet.VEGAN));
        assertThat(recipe.servings()).isEqualTo(6);
        assertThat(recipe.diet()).isEqualTo(Diet.VEGAN);
        assertThat(recipe.name()).isEqualTo(SCONES);
        assertThat(recipe.subtitle()).isPresent();

        assertRuleViolated(() -> recipe.revise(RecipeRevision.none().withName("Scones").withServings(0)),
                RecipeRuleViolationException.INVALID_RECIPE);
        assertThat(recipe.name()).isEqualTo(SCONES);
        assertThat(recipe.servings()).isEqualTo(6);
    }

    @Test
    void aRecipeCarriesAtMostTenFurtherImages() {
        Recipe recipe = scones();
        List<URI> eleven = Collections.nCopies(11, URI.create("https://larder.org/media/images/1.jpg"));

        assertRuleViolated(() -> recipe.revise(new RecipeRevision(null, null, null, eleven, null, null, null)),
                RecipeRuleViolationException.INVALID_RECIPE);
        recipe.revise(new RecipeRevision(null, null, null, eleven.subList(0, 10), null, null, null));
        assertThat(recipe.furtherImages()).hasSize(10);
    }

    @Test
    void nameAndSubtitleHaveLimits() {
        assertRuleViolated(() -> scones().revise(RecipeRevision.none().withName(" ")), RecipeRuleViolationException.INVALID_RECIPE);
        assertRuleViolated(() -> scones().revise(RecipeRevision.none().withName("x".repeat(201))),
                RecipeRuleViolationException.INVALID_RECIPE);
        assertRuleViolated(() -> scones().revise(new RecipeRevision(null, "x".repeat(301), null, null, null, null, null)),
                RecipeRuleViolationException.INVALID_RECIPE);
    }

    @Test
    void aRecipeIsAssignedToExactlyOneMeal() {
        Recipe recipe = scones();
        recipe.assignTo(Meal.SUPPER);

        assertThat(recipe.meal()).isEqualTo(Meal.SUPPER);
        assertRuleViolated(() -> recipe.assignTo(null), RecipeRuleViolationException.INVALID_RECIPE);
    }

    @Test
    void preparationTimeIsWrittenAsHoursAndMinutes() {
        assertThat(PreparationTime.parse("02:00").totalMinutes()).isEqualTo(120);
        assertThat(PreparationTime.ofMinutes(95)).hasToString("01:35");
        assertThat(PreparationTime.parse("99:59")).isEqualTo(new PreparationTime(99, 59));
        assertRuleViolated(() -> PreparationTime.parse("2:00"), RecipeRuleViolationException.INVALID_RECIPE);
        assertRuleViolated(() -> PreparationTime.parse("02:60"), RecipeRuleViolationException.INVALID_RECIPE);
        assertRuleViolated(() -> PreparationTime.parse(null), RecipeRuleViolationException.INVALID_RECIPE);
    }

    @Test
    void searchCriteriaAreCombinedWithAnd() {
        Recipe recipe = scones();

        assertThat(RecipeSearch.all().isSatisfiedBy(recipe)).isTrue();
        assertThat(new RecipeSearch(Meal.BREAKFAST, Diet.VEGETARIAN, List.of("flour", "Butter")).isSatisfiedBy(recipe)).isTrue();
        assertThat(new RecipeSearch(Meal.BREAKFAST, Diet.VEGAN, List.of()).isSatisfiedBy(recipe)).isFalse();
        assertThat(new RecipeSearch(Meal.LUNCH, null, List.of()).isSatisfiedBy(recipe)).isFalse();
        assertThat(new RecipeSearch(null, null, List.of("Flour", "Eggs")).isSatisfiedBy(recipe)).isFalse();
    }

    private static void assertRuleViolated(org.assertj.core.api.ThrowableAssert.ThrowingCallable change, String code) {
        assertThatThrownBy(change).isInstanceOfSatisfying(RecipeRuleViolationException.class,
                e -> assertThat(e.code()).isEqualTo(code));
    }
}
