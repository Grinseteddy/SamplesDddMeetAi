package org.larder.recipecatalog;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.larder.recipecatalog.domain.CookId;
import org.larder.recipecatalog.domain.Diet;
import org.larder.recipecatalog.domain.HowToStep;
import org.larder.recipecatalog.domain.HowToStepDraft;
import org.larder.recipecatalog.domain.HowToStepId;
import org.larder.recipecatalog.domain.Ingredient;
import org.larder.recipecatalog.domain.IngredientDraft;
import org.larder.recipecatalog.domain.IngredientId;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.PreparationTime;
import org.larder.recipecatalog.domain.Quantity;
import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeDraft;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.Unit;

/** The examples of the contract and the visual glossary. */
public final class TestData {

    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final CookId OTHER_COOK = new CookId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final RecipeId SCONES_ID = new RecipeId(UUID.fromString("7cf09822-77a1-46bb-812f-b7852bca0913"));
    public static final IngredientId FLOUR_ID = new IngredientId(UUID.fromString("2b1c4d6e-8f0a-4b2c-9d4e-6f8a0b2c4d6e"));
    public static final IngredientId BUTTER_ID = new IngredientId(UUID.fromString("3c2d5e7f-9a1b-4c3d-8e5f-7a9b1c3d5e7f"));
    public static final HowToStepId MIX_ID = new HowToStepId(UUID.fromString("4d3e6f8a-0b2c-4d4e-9f6a-8b0c2d4e6f8a"));
    public static final HowToStepId BAKE_ID = new HowToStepId(UUID.fromString("5e4f7a9b-1c3d-4e5f-8a7b-9c1d3e5f7a9b"));
    public static final URI MAIN_IMAGE = URI.create("https://larder.org/media/images/scones.jpg");

    public static final String SCONES = "Scones for Sunday";
    public static final String SUBTITLE = "Easy to prepare on Saturday";
    public static final String MIX = "Carefully mix the water with the flour";

    private TestData() {
    }

    /** What a cook enters for the glossary's recipe. */
    public static RecipeDraft sconesDraft() {
        return new RecipeDraft(SCONES, SUBTITLE, MAIN_IMAGE, List.of(), PreparationTime.parse("02:00"), 4,
                Meal.BREAKFAST, Diet.VEGETARIAN,
                List.of(new IngredientDraft("Flour", Quantity.of("0.5", Unit.KILOGRAM)),
                        new IngredientDraft("Butter", Quantity.of("125", Unit.GRAM))),
                List.of(new HowToStepDraft(1, MIX, null),
                        new HowToStepDraft(2, "Bake for 15 minutes", null)));
    }

    /** The glossary's recipe as stored, owned by {@link #COOK}. */
    public static Recipe scones() {
        return Recipe.restore(SCONES_ID, COOK, SCONES, SUBTITLE, MAIN_IMAGE, List.of(), PreparationTime.parse("02:00"), 4,
                Meal.BREAKFAST, Diet.VEGETARIAN,
                List.of(Ingredient.restore(FLOUR_ID, "Flour", Quantity.of("0.5", Unit.KILOGRAM)),
                        Ingredient.restore(BUTTER_ID, "Butter", Quantity.of("125", Unit.GRAM))),
                List.of(HowToStep.restore(MIX_ID, 1, MIX, null),
                        HowToStep.restore(BAKE_ID, 2, "Bake for 15 minutes", null)));
    }

    /** A draft with one ingredient and one step only. */
    public static RecipeDraft draft(String name, Meal meal, Diet diet, String... ingredientNames) {
        return new RecipeDraft(name, null, null, null, PreparationTime.parse("00:30"), 2, meal, diet,
                java.util.Arrays.stream(ingredientNames)
                        .map(ingredient -> new IngredientDraft(ingredient, Quantity.of("1", Unit.PIECE))).toList(),
                List.of(new HowToStepDraft(1, "Prepare " + name, null)));
    }
}
