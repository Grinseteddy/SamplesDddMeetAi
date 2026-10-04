package org.larder.cookingassistance.domain;

import java.util.Objects;
import java.util.UUID;

/** A cook - the requester of a help request or the person giving a help. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }
}
