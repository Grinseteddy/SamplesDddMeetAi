package org.larder.mealpreparation.domain;

import java.util.Objects;
import java.util.UUID;

/** A cook on Larder; in this context the cook who started a meal preparation and stands at the stove. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }
}
