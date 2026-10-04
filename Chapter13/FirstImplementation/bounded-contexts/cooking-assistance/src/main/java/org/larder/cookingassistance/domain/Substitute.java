package org.larder.cookingassistance.domain;

import static org.larder.cookingassistance.domain.HelpRuleViolationException.INVALID_ANSWER;

import java.math.BigDecimal;
import java.util.Objects;

/** Replacement of one requested ingredient by {@code value} {@code unit} of {@code name}. */
public record Substitute(IngredientId ingredient, String name, BigDecimal value, Unit unit) {

    public Substitute {
        Objects.requireNonNull(ingredient, "ingredient must not be null");
        name = Texts.required(name, Texts.INGREDIENT_NAME, INVALID_ANSWER, "The name of a substitute ingredient");
        if (value == null || value.signum() <= 0) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "The amount of a substitute ingredient must be greater than 0");
        }
        if (unit == null) {
            throw new HelpRuleViolationException(INVALID_ANSWER, "A substitute ingredient needs a unit");
        }
    }
}
