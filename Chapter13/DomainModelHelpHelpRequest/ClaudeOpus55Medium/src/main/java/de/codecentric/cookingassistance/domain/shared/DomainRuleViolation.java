package de.codecentric.cookingassistance.domain.shared;

/**
 * Base of every rule the Cooking Assistance glossary asserts. Each concrete
 * violation is named after the rule it protects, never "invalid argument".
 */
public abstract class DomainRuleViolation extends RuntimeException {

    protected DomainRuleViolation(String message) {
        super(message);
    }

    /** A term drawn with cardinality 1 (or 1..*) is missing or blank. */
    public static final class RequiredTermMissing extends DomainRuleViolation {
        public RequiredTermMissing(String term) {
            super("'" + term + "' is required");
        }
    }
}
