package org.larder.recipecatalog.domain;

import java.util.Objects;
import java.util.UUID;

public record HowToStepId(UUID value) {

    public HowToStepId {
        Objects.requireNonNull(value, "howToStepId must not be null");
    }

    public static HowToStepId newId() {
        return new HowToStepId(UUID.randomUUID());
    }
}
