package org.larder.consentmanagement.domain;

import java.util.Objects;
import java.util.UUID;

/** The one who gives a consent; on Larder always a cook. */
public record SubjectId(UUID value) {

    public SubjectId {
        Objects.requireNonNull(value, "subject must not be null");
    }
}
