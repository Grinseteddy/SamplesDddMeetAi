package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

/** One course of a proposed menu; courses served in parallel share a step. */
public record Course(int step, RecipeId recipe) {

    public Course {
        if (step < 1) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "The serving step of a course starts at 1");
        }
        if (recipe == null) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "A course names the recipe of its dish");
        }
    }
}
