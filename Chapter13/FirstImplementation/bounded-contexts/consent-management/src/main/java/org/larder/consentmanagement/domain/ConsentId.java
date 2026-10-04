package org.larder.consentmanagement.domain;

import java.util.Objects;
import java.util.UUID;

public record ConsentId(UUID value) {

    public ConsentId {
        Objects.requireNonNull(value, "consentId must not be null");
    }

    public static ConsentId newId() {
        return new ConsentId(UUID.randomUUID());
    }
}
