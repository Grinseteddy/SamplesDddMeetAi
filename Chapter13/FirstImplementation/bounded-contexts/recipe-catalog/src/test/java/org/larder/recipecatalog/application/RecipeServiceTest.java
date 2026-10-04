package org.larder.recipecatalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.recipecatalog.TestData.BUTTER_ID;
import static org.larder.recipecatalog.TestData.COOK;
import static org.larder.recipecatalog.TestData.FLOUR_ID;
import static org.larder.recipecatalog.TestData.MIX_ID;
import static org.larder.recipecatalog.TestData.OTHER_COOK;
import static org.larder.recipecatalog.TestData.SCONES_ID;
import static org.larder.recipecatalog.TestData.draft;
import static org.larder.recipecatalog.TestData.scones;
import static org.larder.recipecatalog.TestData.sconesDraft;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.recipecatalog.domain.Diet;
import org.larder.recipecatalog.domain.HowToStepDraft;
import org.larder.recipecatalog.domain.HowToStepId;
import org.larder.recipecatalog.domain.IngredientDraft;
import org.larder.recipecatalog.domain.IngredientId;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.Quantity;
import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.RecipeRevision;
import org.larder.recipecatalog.domain.RecipeRuleViolationException;
import org.larder.recipecatalog.domain.RecipeSearch;
import org.larder.recipecatalog.domain.Unit;

class RecipeServiceTest {

    private final InMemoryRecipes recipes = new InMemoryRecipes();
    private final RecipeService service = new RecipeService(recipes);

    @BeforeEach
    void theGlossaryRecipeExists() {
        recipes.save(scones());
    }

    @Test
    void theCallerBecomesTheOwnerOfANewRecipe() {
        Recipe recipe = service.create(OTHER_COOK, sconesDraft());

        assertThat(recipes.findById(recipe.id())).isPresent();
        assertThat(service.recipe(recipe.id()).owner()).isEqualTo(OTHER_COOK);
    }

    @Test
    void onlyTheOwnerChangesARecipe() {
        assertNotPermitted(() -> service.revise(OTHER_COOK, SCONES_ID, RecipeRevision.none().withServings(8)));
        assertNotPermitted(() -> service.assignMeal(OTHER_COOK, SCONES_ID, Meal.LUNCH));
        assertNotPermitted(() -> service.addIngredient(OTHER_COOK, SCONES_ID, new IngredientDraft("Eggs", Quantity.of("2", Unit.PIECE))));
        assertNotPermitted(() -> service.changeIngredient(OTHER_COOK, SCONES_ID, FLOUR_ID, "Rye", null, null));
        assertNotPermitted(() -> service.removeIngredient(OTHER_COOK, SCONES_ID, BUTTER_ID));
        assertNotPermitted(() -> service.addHowToStep(OTHER_COOK, SCONES_ID, new HowToStepDraft(3, "Serve", null)));
        assertNotPermitted(() -> service.changeHowToStep(OTHER_COOK, SCONES_ID, MIX_ID, null, "Stir", null));
        assertNotPermitted(() -> service.removeHowToStep(OTHER_COOK, SCONES_ID, MIX_ID));
        assertNotPermitted(() -> service.delete(OTHER_COOK, SCONES_ID));

        assertThat(service.recipe(SCONES_ID).servings()).isEqualTo(4);
        assertThat(service.recipe(SCONES_ID).ingredients()).hasSize(2);
    }

    @Test
    void theOwnerMaintainsTheRecipe() {
        service.revise(COOK, SCONES_ID, RecipeRevision.none().withServings(8));
        service.assignMeal(COOK, SCONES_ID, Meal.SUPPER);
        var eggs = service.addIngredient(COOK, SCONES_ID, new IngredientDraft("Eggs", Quantity.of("2", Unit.PIECE)));
        service.changeIngredient(COOK, SCONES_ID, FLOUR_ID, null, new BigDecimal("0.75"), null);
        var serve = service.addHowToStep(COOK, SCONES_ID, new HowToStepDraft(3, "Serve warm", null));

        Recipe recipe = service.recipe(SCONES_ID);
        assertThat(recipe.servings()).isEqualTo(8);
        assertThat(recipe.meal()).isEqualTo(Meal.SUPPER);
        assertThat(service.ingredient(SCONES_ID, eggs.id()).name()).isEqualTo("Eggs");
        assertThat(service.ingredient(SCONES_ID, FLOUR_ID).quantity().isSameAs(Quantity.of("0.75", Unit.KILOGRAM))).isTrue();
        assertThat(service.howToStep(SCONES_ID, serve.id()).sequenceNumber()).isEqualTo(3);

        service.delete(COOK, SCONES_ID);
        assertThat(recipes.findById(SCONES_ID)).isEmpty();
    }

    @Test
    void changingOnlyTheUnitKeepsTheValue() {
        service.changeIngredient(COOK, SCONES_ID, BUTTER_ID, null, null, Unit.TABLE_SPOON);

        assertThat(service.ingredient(SCONES_ID, BUTTER_ID).quantity().isSameAs(Quantity.of("125", Unit.TABLE_SPOON))).isTrue();
    }

    @Test
    void unknownRecipesIngredientsAndStepsAreNotFound() {
        RecipeId unknown = RecipeId.newId();
        assertThatThrownBy(() -> service.recipe(unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.revise(COOK, unknown, RecipeRevision.none())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.ingredient(SCONES_ID, IngredientId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.removeIngredient(COOK, SCONES_ID, IngredientId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.howToStep(SCONES_ID, HowToStepId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.changeHowToStep(COOK, SCONES_ID, HowToStepId.newId(), 5, null, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void anUnknownRecipeIsNotFoundEvenForSomebodyElse() {
        assertThatThrownBy(() -> service.delete(OTHER_COOK, RecipeId.newId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void brokenRulesAreNotStored() {
        service.removeIngredient(COOK, SCONES_ID, BUTTER_ID);

        assertThatThrownBy(() -> service.removeIngredient(COOK, SCONES_ID, FLOUR_ID))
                .isInstanceOf(RecipeRuleViolationException.class);
        assertThatThrownBy(() -> service.changeIngredient(COOK, SCONES_ID, FLOUR_ID, null, BigDecimal.ZERO, null))
                .isInstanceOf(RecipeRuleViolationException.class);
        assertThat(service.recipe(SCONES_ID).ingredients()).extracting(i -> i.id()).containsExactly(FLOUR_ID);
    }

    @Test
    void searchesTheCatalog() {
        Recipe soup = service.create(OTHER_COOK, draft("Pumpkin soup", Meal.DINNER, Diet.VEGAN, "Pumpkin", "Flour"));

        assertThat(service.search(RecipeSearch.all())).hasSize(2);
        assertThat(service.search(new RecipeSearch(null, null, List.of("Flour")))).hasSize(2);
        assertThat(service.search(new RecipeSearch(Meal.DINNER, null, List.of("Flour"))))
                .extracting(Recipe::id).containsExactly(soup.id());
    }

    private static void assertNotPermitted(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOf(NotPermittedException.class);
    }

    /** Keeps what was stored - a copy - so unsaved changes of a loaded recipe do not leak into the store. */
    static class InMemoryRecipes implements RecipeRepository {
        private final Map<RecipeId, Recipe> store = new HashMap<>();

        @Override
        public void save(Recipe recipe) {
            store.put(recipe.id(), copy(recipe));
        }

        @Override
        public Optional<Recipe> findById(RecipeId id) {
            return Optional.ofNullable(store.get(id)).map(InMemoryRecipes::copy);
        }

        @Override
        public List<Recipe> search(RecipeSearch search) {
            return store.values().stream().filter(search::isSatisfiedBy).map(InMemoryRecipes::copy).toList();
        }

        @Override
        public void delete(RecipeId id) {
            store.remove(id);
        }

        private static Recipe copy(Recipe recipe) {
            return Recipe.restore(recipe.id(), recipe.owner(), recipe.name(), recipe.subtitle().orElse(null),
                    recipe.mainImage().orElse(null), recipe.furtherImages(), recipe.preparationTime(), recipe.servings(),
                    recipe.meal(), recipe.diet(),
                    recipe.ingredients().stream().map(i -> org.larder.recipecatalog.domain.Ingredient.restore(
                            i.id(), i.name(), i.quantity())).toList(),
                    recipe.howToSteps().stream().map(s -> org.larder.recipecatalog.domain.HowToStep.restore(
                            s.id(), s.sequenceNumber(), s.description(), s.illustration().orElse(null))).toList());
        }
    }
}
