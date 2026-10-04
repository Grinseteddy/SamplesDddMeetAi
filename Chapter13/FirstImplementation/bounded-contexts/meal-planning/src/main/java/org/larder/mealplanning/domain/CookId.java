package org.larder.mealplanning.domain;

import java.util.Objects;
import java.util.UUID;

/** A cook of Larder; here the owner of meal plans. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }
}
