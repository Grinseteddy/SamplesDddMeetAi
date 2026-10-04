package org.larder.mealpreparation.domain;

/** A start or move would break one of the meal preparation's rules; nothing is changed. */
public class MealPreparationRuleViolationException extends RuntimeException {

    public static final String RECIPE_WITHOUT_STEPS = "RECIPE_WITHOUT_STEPS";
    public static final String STEP_NOT_CURRENT = "STEP_NOT_CURRENT";
    public static final String LAST_STEP_REACHED = "LAST_STEP_REACHED";
    public static final String FIRST_STEP_REACHED = "FIRST_STEP_REACHED";

    private final String code;

    public MealPreparationRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
