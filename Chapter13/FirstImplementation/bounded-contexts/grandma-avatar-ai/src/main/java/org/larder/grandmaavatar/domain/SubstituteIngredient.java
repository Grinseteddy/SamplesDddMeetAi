package org.larder.grandmaavatar.domain;

import java.math.BigDecimal;

/** What to use instead, and how much of it. */
public record SubstituteIngredient(String name, BigDecimal value, Unit unit) {

    public SubstituteIngredient {
        Texts.answerText(name, "substitute name", 100);
        Answer.require(value != null && value.signum() > 0, "a substitute's amount must be greater than 0");
        Answer.require(unit != null, "a substitute needs a unit");
    }
}
