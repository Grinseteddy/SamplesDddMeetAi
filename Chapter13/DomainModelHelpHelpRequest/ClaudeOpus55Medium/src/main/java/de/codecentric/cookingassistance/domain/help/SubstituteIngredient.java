package de.codecentric.cookingassistance.domain.help;

import de.codecentric.cookingassistance.domain.shared.DomainRuleViolation;
import de.codecentric.cookingassistance.domain.shared.Require;

import java.math.BigDecimal;

/**
 * Glossary term "substitute ingredient" — Name, Value and Unit, always together.
 *
 * <p>Value &gt; 0 is not drawn; it is assumed (a quantity of zero or less is
 * not an amount to use) and listed as an open question.
 */
public record SubstituteIngredient(String name, BigDecimal value, Unit unit) {

    public SubstituteIngredient {
        Require.text(name, "Name");
        Require.present(value, "Value");
        Require.present(unit, "Unit");
        if (value.signum() <= 0) {
            throw new QuantityNotPositive(value);
        }
    }

    public static final class QuantityNotPositive extends DomainRuleViolation {
        public QuantityNotPositive(BigDecimal value) {
            super("a substitute ingredient's Value must be greater than zero, not " + value);
        }
    }
}
