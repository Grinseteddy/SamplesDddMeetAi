package org.larder.mealpreparation.application;

import org.larder.mealpreparation.domain.RecipeId;

/** A meal preparation refers to a recipe Recipe Catalog does not know (a broken request, not a missing resource). */
public class UnknownRecipeException extends RuntimeException {

    public UnknownRecipeException(RecipeId recipe) {
        super("Recipe " + recipe.value() + " does not exist in Recipe Catalog");
    }
}
