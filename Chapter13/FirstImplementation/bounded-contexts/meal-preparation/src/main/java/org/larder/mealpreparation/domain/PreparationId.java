package org.larder.mealpreparation.domain;

import java.util.Objects;
import java.util.UUID;

public record PreparationId(UUID value) {

    public PreparationId {
        Objects.requireNonNull(value, "preparationId must not be null");
    }

    public static PreparationId newId() {
        return new PreparationId(UUID.randomUUID());
    }
}
