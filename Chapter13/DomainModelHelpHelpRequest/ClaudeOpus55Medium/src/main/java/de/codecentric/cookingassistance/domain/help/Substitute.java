package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.IngredientId;
import de.codecentric.cookingassistance.domain.shared.Require;

/**
 * Glossary term "substitute" — what replaces one missing ingredient.
 *
 * @param ingredient           "contains 1 ingredient" — "same ingredients as in request" (checked by {@link Help})
 * @param substituteIngredient "contains 1 substitute ingredient"
 */
public record Substitute(IngredientId ingredient, SubstituteIngredient substituteIngredient) {

    public Substitute {
        Require.present(ingredient, "ingredient");
        Require.present(substituteIngredient, "substitute ingredient");
    }
}
