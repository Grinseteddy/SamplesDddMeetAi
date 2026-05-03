package com.cookwithus.sharing;

import com.cookwithus.domain.cook.CookId;
import com.cookwithus.domain.recipe.Ingredient;
import com.cookwithus.domain.recipe.Recipe;
import com.cookwithus.domain.recipe.RecipeId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * EXACT test suite for Recipe publication and revision scenarios.
 *
 * <p>Tests are derived directly from the Example Mapping session documented in Chapter 15.
 * Each test corresponds to a concrete example agreed upon by the domain team.
 *
 * <p>EXACT workflow: failing test → AI prompt → implementation → human decision → refactor.
 */
class RecipeSharingTest {

    private static final CookId AUTHOR = CookId.newCookId();
    private static final CookId OTHER_COOK = CookId.newCookId();
    private static final Instant NOW = Instant.now();

    private static final List<Ingredient> VALID_INGREDIENTS = List.of(
            new Ingredient("Pasta", 200, "g"),
            new Ingredient("Olive oil", 2, "tbsp")
    );
    private static final List<String> VALID_STEPS = List.of(
            "Boil salted water.",
            "Cook pasta al dente.",
            "Drain and toss with olive oil."
    );

    // -------------------------------------------------------------------------
    // Publication scenarios
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Publishing a Recipe")
    class PublishingARecipe {

        @Test
        @DisplayName("a Cook can publish a Recipe with a title, at least one Ingredient, and at least one step")
        void happyPath() {
            Recipe recipe = new Recipe(
                    RecipeId.newRecipeId(), AUTHOR,
                    "Quick Pasta", "Simple weeknight pasta", "Quick",
                    VALID_INGREDIENTS, VALID_STEPS, NOW);

            assertThat(recipe.getTitle()).isEqualTo("Quick Pasta");
            assertThat(recipe.getIngredients()).hasSize(2);
            assertThat(recipe.getAuthorId()).isEqualTo(AUTHOR);
        }

        @Test
        @DisplayName("publishing without Ingredients is rejected (invariant: at least one Ingredient)")
        void rejectsEmptyIngredients() {
            assertThatThrownBy(() ->
                    new Recipe(RecipeId.newRecipeId(), AUTHOR, "Title", null, null,
                            List.of(), VALID_STEPS, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one Ingredient");
        }

        @Test
        @DisplayName("publishing with a blank title is rejected (invariant: non-empty title)")
        void rejectsBlankTitle() {
            assertThatThrownBy(() ->
                    new Recipe(RecipeId.newRecipeId(), AUTHOR, "  ", null, null,
                            VALID_INGREDIENTS, VALID_STEPS, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("title");
        }

        @Test
        @DisplayName("publishing with a title exceeding 120 characters is rejected")
        void rejectsTitleTooLong() {
            String longTitle = "A".repeat(121);
            assertThatThrownBy(() ->
                    new Recipe(RecipeId.newRecipeId(), AUTHOR, longTitle, null, null,
                            VALID_INGREDIENTS, VALID_STEPS, NOW))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("120");
        }

        @Test
        @DisplayName("an Ingredient with zero quantity is rejected (invariant: positive quantity)")
        void rejectsZeroQuantity() {
            assertThatThrownBy(() -> new Ingredient("Sugar", 0, "g"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("an Ingredient with negative quantity is rejected")
        void rejectsNegativeQuantity() {
            assertThatThrownBy(() -> new Ingredient("Sugar", -1, "g"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    // -------------------------------------------------------------------------
    // Revision scenarios
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Revising a Recipe")
    class RevisingARecipe {

        @Test
        @DisplayName("the authoring Cook can revise their own Recipe")
        void authorCanRevise() {
            Recipe recipe = publishedRecipe();
            List<Ingredient> updatedIngredients = List.of(
                    new Ingredient("Pasta", 250, "g"),
                    new Ingredient("Olive oil", 3, "tbsp"),
                    new Ingredient("Parmesan", 30, "g")
            );
            Instant revisedAt = NOW.plusSeconds(3600);

            recipe.revise(AUTHOR, "Quick Pasta (updated)", "Even simpler.",
                    updatedIngredients, VALID_STEPS, revisedAt);

            assertThat(recipe.getTitle()).isEqualTo("Quick Pasta (updated)");
            assertThat(recipe.getIngredients()).hasSize(3);
            assertThat(recipe.getRevisedAt()).isEqualTo(revisedAt);
        }

        @Test
        @DisplayName("a Cook who is not the author cannot revise the Recipe — edge case found during Example Mapping")
        void nonAuthorCannotRevise() {
            Recipe recipe = publishedRecipe();

            // This edge case was surfaced during Example Mapping in Chapter 15.
            // The domain team decided: only the authoring Cook may revise their Recipe.
            assertThatThrownBy(() ->
                    recipe.revise(OTHER_COOK, "Hacked Title", null,
                            VALID_INGREDIENTS, VALID_STEPS, NOW.plusSeconds(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("author");
        }

        @Test
        @DisplayName("revising to have no Ingredients is rejected (invariant preserved after revision)")
        void revisionMustKeepAtLeastOneIngredient() {
            Recipe recipe = publishedRecipe();

            assertThatThrownBy(() ->
                    recipe.revise(AUTHOR, "New Title", null,
                            List.of(), VALID_STEPS, NOW.plusSeconds(1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least one Ingredient");
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Recipe publishedRecipe() {
        return new Recipe(
                RecipeId.newRecipeId(), AUTHOR,
                "Quick Pasta", "Simple weeknight pasta", "Quick",
                VALID_INGREDIENTS, VALID_STEPS, NOW);
    }
}
