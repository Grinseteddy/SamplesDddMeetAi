package org.larder.recipecatalog.adapter.in.web;

import org.larder.recipecatalog.adapter.in.web.model.Diet;
import org.larder.recipecatalog.adapter.in.web.model.HowToStep;
import org.larder.recipecatalog.adapter.in.web.model.Ingredient;
import org.larder.recipecatalog.adapter.in.web.model.Meal;
import org.larder.recipecatalog.adapter.in.web.model.MealAssignment;
import org.larder.recipecatalog.adapter.in.web.model.Recipe;
import org.larder.recipecatalog.adapter.in.web.model.Unit;

/** Translates the domain model into the contract's model - and only in this direction. */
final class RecipeMapper {

    private RecipeMapper() {
    }

    static Recipe toApi(org.larder.recipecatalog.domain.Recipe recipe) {
        return new Recipe(
                recipe.id().value(),
                recipe.owner().value(),
                recipe.name(),
                recipe.preparationTime().toString(),
                recipe.servings(),
                toApi(recipe.meal()),
                Diet.valueOf(recipe.diet().name()),
                recipe.ingredients().stream().map(RecipeMapper::toApi).toList(),
                recipe.howToSteps().stream().map(RecipeMapper::toApi).toList())
                .subtitle(recipe.subtitle().orElse(null))
                .mainImage(recipe.mainImage().orElse(null))
                .furtherImages(recipe.furtherImages());
    }

    static Ingredient toApi(org.larder.recipecatalog.domain.Ingredient ingredient) {
        return new Ingredient(
                ingredient.id().value(),
                ingredient.name(),
                ingredient.quantity().value(),
                Unit.valueOf(ingredient.quantity().unit().name()));
    }

    static HowToStep toApi(org.larder.recipecatalog.domain.HowToStep step) {
        return new HowToStep(step.id().value(), step.sequenceNumber(), step.description())
                .illustration(step.illustration().orElse(null));
    }

    static MealAssignment toMealAssignment(org.larder.recipecatalog.domain.Recipe recipe) {
        return new MealAssignment(toApi(recipe.meal()));
    }

    private static Meal toApi(org.larder.recipecatalog.domain.Meal meal) {
        return Meal.valueOf(meal.name());
    }
}
