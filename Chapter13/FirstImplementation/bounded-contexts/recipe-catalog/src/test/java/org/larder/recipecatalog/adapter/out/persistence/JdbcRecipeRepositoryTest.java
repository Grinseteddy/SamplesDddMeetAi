package org.larder.recipecatalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.recipecatalog.TestData.BAKE_ID;
import static org.larder.recipecatalog.TestData.BUTTER_ID;
import static org.larder.recipecatalog.TestData.COOK;
import static org.larder.recipecatalog.TestData.FLOUR_ID;
import static org.larder.recipecatalog.TestData.MAIN_IMAGE;
import static org.larder.recipecatalog.TestData.MIX;
import static org.larder.recipecatalog.TestData.OTHER_COOK;
import static org.larder.recipecatalog.TestData.SCONES;
import static org.larder.recipecatalog.TestData.SCONES_ID;
import static org.larder.recipecatalog.TestData.SUBTITLE;
import static org.larder.recipecatalog.TestData.draft;
import static org.larder.recipecatalog.TestData.scones;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.TestDatabase;
import org.larder.recipecatalog.domain.Diet;
import org.larder.recipecatalog.domain.HowToStep;
import org.larder.recipecatalog.domain.HowToStepDraft;
import org.larder.recipecatalog.domain.Ingredient;
import org.larder.recipecatalog.domain.IngredientDraft;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.Quantity;
import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeRevision;
import org.larder.recipecatalog.domain.RecipeSearch;
import org.larder.recipecatalog.domain.Unit;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcRecipeRepositoryTest {

    private BoundedContextDatabase database;
    private JdbcRecipeRepository recipes;

    @BeforeEach
    void freshSchema() {
        database = TestDatabase.forSchema("recipecatalog");
        recipes = new JdbcRecipeRepository(database.jdbcClient());
    }

    @Test
    void storesARecipeAsAWhole() {
        Recipe recipe = scones();
        recipe.revise(new RecipeRevision(null, null, null,
                List.of(URI.create("https://larder.org/media/images/2.jpg"), URI.create("https://larder.org/media/images/1.jpg")),
                null, null, null));
        recipes.save(recipe);

        Recipe stored = recipes.findById(SCONES_ID).orElseThrow();
        assertThat(stored.owner()).isEqualTo(COOK);
        assertThat(stored.name()).isEqualTo(SCONES);
        assertThat(stored.subtitle()).contains(SUBTITLE);
        assertThat(stored.mainImage()).contains(MAIN_IMAGE);
        assertThat(stored.furtherImages()).extracting(URI::toString)
                .containsExactly("https://larder.org/media/images/2.jpg", "https://larder.org/media/images/1.jpg");
        assertThat(stored.preparationTime()).hasToString("02:00");
        assertThat(stored.servings()).isEqualTo(4);
        assertThat(stored.meal()).isEqualTo(Meal.BREAKFAST);
        assertThat(stored.diet()).isEqualTo(Diet.VEGETARIAN);
        assertThat(stored.ingredients()).extracting(Ingredient::id).containsExactly(FLOUR_ID, BUTTER_ID);
        assertThat(stored.ingredient(FLOUR_ID).orElseThrow().quantity()).isEqualTo(Quantity.of("0.5", Unit.KILOGRAM));
        assertThat(stored.howToSteps()).extracting(HowToStep::description).containsExactly(MIX, "Bake for 15 minutes");
    }

    @Test
    void savingAgainReplacesTheChangedParts() {
        Recipe recipe = scones();
        recipes.save(recipe);

        recipe.removeIngredient(BUTTER_ID);
        recipe.addIngredient(new IngredientDraft("Eggs", Quantity.of("2", Unit.PIECE)));
        // swap the order of the two steps - the unique sequence numbers must not get in the way
        recipe.changeHowToStep(BAKE_ID, 3, null, URI.create("https://larder.org/media/images/oven.jpg"));
        recipe.addHowToStep(new HowToStepDraft(2, "Rest the dough", null));
        recipe.assignTo(Meal.SUPPER);
        recipe.revise(RecipeRevision.none().withName("Scones for Saturday"));
        recipes.save(recipe);

        Recipe stored = recipes.findById(SCONES_ID).orElseThrow();
        assertThat(stored.name()).isEqualTo("Scones for Saturday");
        assertThat(stored.meal()).isEqualTo(Meal.SUPPER);
        assertThat(stored.ingredients()).extracting(Ingredient::name).containsExactly("Flour", "Eggs");
        assertThat(stored.howToSteps()).extracting(HowToStep::sequenceNumber).containsExactly(1, 2, 3);
        assertThat(stored.howToStep(BAKE_ID).orElseThrow().illustration()).isPresent();
    }

    @Test
    void deletingARecipeDeletesItsParts() {
        recipes.save(scones());

        recipes.delete(SCONES_ID);

        assertThat(recipes.findById(SCONES_ID)).isEmpty();
        assertThat(database.jdbcClient().sql("select count(*) from ingredient").query(Integer.class).single()).isZero();
        assertThat(database.jdbcClient().sql("select count(*) from how_to_step").query(Integer.class).single()).isZero();
    }

    @Test
    void aFailingSaveInsideATransactionLeavesTheStoredRecipeUntouched() {
        recipes.save(scones());
        var transactions = new TransactionTemplate(new DataSourceTransactionManager(database.dataSource()));

        assertThatThrownBy(() -> transactions.executeWithoutResult(status -> {
            Recipe recipe = recipes.findById(SCONES_ID).orElseThrow();
            recipe.removeIngredient(BUTTER_ID);
            recipes.save(recipe);
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(recipes.findById(SCONES_ID).orElseThrow().ingredients()).hasSize(2);
    }

    @Test
    void searchesByMealDietAndEveryIngredientName() {
        recipes.save(scones()); // BREAKFAST, VEGETARIAN, Flour + Butter
        Recipe soup = Recipe.create(OTHER_COOK, draft("Pumpkin soup", Meal.DINNER, Diet.VEGAN, "Pumpkin", "Flour"));
        Recipe pancakes = Recipe.create(OTHER_COOK, draft("Pancakes", Meal.BREAKFAST, Diet.NORMAL, "Flour", "Eggs", "Milk"));
        recipes.save(soup);
        recipes.save(pancakes);

        assertThat(recipes.search(RecipeSearch.all())).extracting(Recipe::name)
                .containsExactly("Pancakes", "Pumpkin soup", SCONES);
        assertThat(recipes.search(new RecipeSearch(Meal.BREAKFAST, null, List.of()))).extracting(Recipe::name)
                .containsExactly("Pancakes", SCONES);
        assertThat(recipes.search(new RecipeSearch(null, Diet.VEGAN, List.of()))).extracting(Recipe::id)
                .containsExactly(soup.id());
        assertThat(recipes.search(new RecipeSearch(null, null, List.of("flour", "EGGS")))).extracting(Recipe::id)
                .containsExactly(pancakes.id());
        assertThat(recipes.search(new RecipeSearch(Meal.BREAKFAST, Diet.VEGETARIAN, List.of("Flour", "Butter"))))
                .extracting(Recipe::id).containsExactly(SCONES_ID);
        assertThat(recipes.search(new RecipeSearch(Meal.LUNCH, null, List.of()))).isEmpty();
        assertThat(recipes.search(new RecipeSearch(null, null, List.of("Flou")))).isEmpty();
    }

    @Test
    void foundRecipesComeWithAllTheirParts() {
        recipes.save(scones());

        Recipe found = recipes.search(new RecipeSearch(null, null, List.of("Butter"))).getFirst();

        assertThat(found.ingredients()).hasSize(2);
        assertThat(found.howToSteps()).hasSize(2);
    }
}
