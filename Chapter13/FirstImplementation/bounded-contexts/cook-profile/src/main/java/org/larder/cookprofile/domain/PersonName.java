package org.larder.cookprofile.domain;

/** Name or given name of a cook: 1 to 100 characters, not only blanks. */
public record PersonName(String value) {

    public static final int MAX_LENGTH = 100;

    public PersonName {
        if (value == null || value.isBlank()) {
            throw new InvalidCookException("A name must not be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidCookException("A name has at most " + MAX_LENGTH + " characters");
        }
    }
}
