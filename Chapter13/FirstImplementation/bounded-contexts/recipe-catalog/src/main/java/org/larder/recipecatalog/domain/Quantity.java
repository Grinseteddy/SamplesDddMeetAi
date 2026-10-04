package org.larder.recipecatalog.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** How much of an ingredient the recipe's servings need, e.g. 0.5 KILOGRAM. The value is always positive. */
public record Quantity(BigDecimal value, Unit unit) {

    public Quantity {
        if (value == null || value.signum() <= 0) {
            throw RecipeRuleViolationException.invalid("The value of an ingredient must be greater than 0, not " + value);
        }
        if (unit == null) {
            throw RecipeRuleViolationException.invalid("The value of an ingredient needs a unit");
        }
    }

    public static Quantity of(String value, Unit unit) {
        return new Quantity(new BigDecimal(value), unit);
    }

    /** Equal values regardless of their scale, e.g. 0.5 and 0.50. */
    public boolean isSameAs(Quantity other) {
        return Objects.equals(unit, other.unit) && value.compareTo(other.value) == 0;
    }
}
