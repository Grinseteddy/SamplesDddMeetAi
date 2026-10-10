package de.codecentric.cookingassistance.domain.shared;

import java.util.List;

/** Guards for the glossary cardinalities 1 and 0..* — used by every type in this context. */
public final class Require {

    private Require() {
    }

    /** Cardinality 1. */
    public static <T> T present(T value, String term) {
        if (value == null) {
            throw new DomainRuleViolation.RequiredTermMissing(term);
        }
        return value;
    }

    /** Cardinality 1 on a free-text term: the text must say something. */
    public static String text(String value, String term) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleViolation.RequiredTermMissing(term);
        }
        return value;
    }

    /** Optional free text (0..1): absent is fine, blank is treated as absent. */
    public static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /** Cardinality 0..*: may be empty, never null, no null elements, returned unmodifiable. */
    public static <T> List<T> list(List<T> values, String term) {
        if (values == null) {
            return List.of();
        }
        for (T value : values) {
            present(value, term);
        }
        return List.copyOf(values);
    }

    /** Cardinality 1..*: as {@link #list} but must not be empty. */
    public static <T> List<T> nonEmptyList(List<T> values, String term) {
        List<T> copy = list(values, term);
        if (copy.isEmpty()) {
            throw new DomainRuleViolation.RequiredTermMissing(term);
        }
        return copy;
    }
}
