package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

import java.util.Optional;

/** Answer of type Steps to Mitigate Catastrophe: what to do to rescue the meal. */
public record CatastropheMitigation(Optional<RecipeId> recipe, String explanation) implements Answer {

    public CatastropheMitigation {
        recipe = recipe == null ? Optional.empty() : recipe;
        explanation = Texts.required(explanation, Texts.LONG_TEXT, INVALID_ANSWER, "The mitigation of the catastrophe");
    }

    @Override
    public HelpType type() {
        return HelpType.STEPS_TO_MITIGATE_CATASTROPHE;
    }
}
