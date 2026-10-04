package org.larder.mealplanning.application;

import org.larder.mealplanning.domain.RecipeId;

/** A course refers to a recipe the Recipe Catalog does not know (a broken request, not a missing resource). */
public class UnknownRecipeException extends RuntimeException {

    public UnknownRecipeException(RecipeId id) {
        super("Recipe " + id.value() + " does not exist in the Recipe Catalog");
    }
}
