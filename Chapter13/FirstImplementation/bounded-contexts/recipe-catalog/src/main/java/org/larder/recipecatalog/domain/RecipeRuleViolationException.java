package org.larder.recipecatalog.domain;

/** A change would break one of the recipe's rules; the change is not applied. */
public class RecipeRuleViolationException extends RuntimeException {

    public static final String INVALID_RECIPE = "INVALID_RECIPE";
    public static final String RECIPE_NEEDS_INGREDIENT = "RECIPE_NEEDS_INGREDIENT";
    public static final String RECIPE_NEEDS_HOW_TO_STEP = "RECIPE_NEEDS_HOW_TO_STEP";
    public static final String SEQUENCE_NUMBER_TAKEN = "SEQUENCE_NUMBER_TAKEN";

    private final String code;

    public RecipeRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    static RecipeRuleViolationException invalid(String message) {
        return new RecipeRuleViolationException(INVALID_RECIPE, message);
    }

    public String code() {
        return code;
    }
}
