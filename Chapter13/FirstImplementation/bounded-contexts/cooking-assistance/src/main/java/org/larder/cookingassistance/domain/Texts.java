package org.larder.cookingassistance.domain;

/**
 * Length rules for texts. The limits are those of the published language (AsyncAPI), which are
 * stricter than the REST contract (e.g. description 2000 vs. 5000): every help request and help must
 * fit into the HelpRequested / HelpProvided messages this context publishes about it.
 */
final class Texts {

    static final int TITLE = 200;
    static final int LONG_TEXT = 2000;
    static final int INGREDIENT_NAME = 100;

    private Texts() {
    }

    /** A non-blank text of at most {@code max} characters. */
    static String required(String value, int max, String code, String what) {
        if (value == null || value.isBlank()) {
            throw new HelpRuleViolationException(code, what + " must not be empty");
        }
        return limited(value, max, code, what);
    }

    /** An optional text ({@code null} allowed) of at most {@code max} characters. */
    static String limited(String value, int max, String code, String what) {
        if (value != null && value.length() > max) {
            throw new HelpRuleViolationException(code, what + " must not be longer than " + max + " characters");
        }
        return value;
    }
}
