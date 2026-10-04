package org.larder.mealplanning.domain;

/** A change would break one of the meal plan's rules; the change is not applied. */
public class MealPlanRuleViolationException extends RuntimeException {

    public static final String INVALID_MEAL_PLAN = "INVALID_MEAL_PLAN";
    public static final String UNKNOWN_DIET = "UNKNOWN_DIET";

    private final String code;

    public MealPlanRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    static MealPlanRuleViolationException invalid(String message) {
        return new MealPlanRuleViolationException(INVALID_MEAL_PLAN, message);
    }

    public String code() {
        return code;
    }
}
