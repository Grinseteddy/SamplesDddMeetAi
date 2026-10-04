package org.larder.consentmanagement.domain;

import java.util.Objects;
import java.util.UUID;

public record ConsentTextId(UUID value) {

    public ConsentTextId {
        Objects.requireNonNull(value, "consentTextId must not be null");
    }
}
