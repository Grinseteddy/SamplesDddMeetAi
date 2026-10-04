package org.larder.consentmanagement.domain;

import java.util.Objects;

/** A wording a subject can consent to. Maintained only via the backend (database migrations). */
public record ConsentText(ConsentTextId id, String text) {

    public ConsentText {
        Objects.requireNonNull(id, "id must not be null");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("A consent text needs a wording");
        }
    }
}
