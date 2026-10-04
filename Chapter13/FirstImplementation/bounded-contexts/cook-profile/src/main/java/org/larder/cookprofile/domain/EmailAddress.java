package org.larder.cookprofile.domain;

import java.util.Locale;

/** The address a cook registered and logs in with. Two addresses are the same regardless of case. */
public record EmailAddress(String value) {

    public static final int MAX_LENGTH = 254;

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new InvalidCookException("A cook needs an email address");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidCookException("An email address has at most " + MAX_LENGTH + " characters");
        }
        int at = value.indexOf('@');
        if (at <= 0 || at != value.lastIndexOf('@') || at == value.length() - 1 || value.chars().anyMatch(Character::isWhitespace)) {
            throw new InvalidCookException("'" + value + "' is not an email address");
        }
    }

    /** The form used to compare addresses. */
    public String normalized() {
        return value.toLowerCase(Locale.ROOT);
    }

    public boolean sameAs(EmailAddress other) {
        return normalized().equals(other.normalized());
    }
}
