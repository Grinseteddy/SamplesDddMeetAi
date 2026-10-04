package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

import java.util.List;
import java.util.Objects;

/** Answer of type Ingredient Substitute: one substitute per requested ingredient of the recipe. */
public record IngredientSubstitutes(RecipeId recipe, List<Substitute> substitutes) implements Answer {

    public IngredientSubstitutes {
        if (recipe == null) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "Ingredient substitutes name the recipe");
        }
        if (substitutes == null || substitutes.isEmpty()) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "Ingredient substitutes propose at least one substitute");
        }
        substitutes = List.copyOf(substitutes);
        substitutes.forEach(Objects::requireNonNull);
    }

    @Override
    public HelpType type() {
        return HelpType.INGREDIENT_SUBSTITUTE;
    }
}
