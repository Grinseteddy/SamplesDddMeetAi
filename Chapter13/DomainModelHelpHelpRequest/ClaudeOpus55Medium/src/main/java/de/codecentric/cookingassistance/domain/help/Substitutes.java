package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.external.RecipeId;
import de.codecentric.cookingassistance.domain.shared.AnswerType;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.List;

/**
 * Glossary term "substitutes" — the answer to an Ingredient Substitute request.
 * Kept plural because the glossary uses "substitute" for its parts.
 *
 * @param recipe     "contains 1 recipe" — "Same recipe as in request" (checked by {@link Help})
 * @param substitute "contains 1..* substitute"
 */
public record Substitutes(RecipeId recipe, List<Substitute> substitute) implements Answer {

    public Substitutes {
        Require.present(recipe, "recipe");
        substitute = Require.nonEmptyList(substitute, "substitute");
    }

    @Override
    public AnswerType answerType() {
        return AnswerType.INGREDIENT_SUBSTITUTE;
    }
}
