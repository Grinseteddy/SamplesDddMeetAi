package org.larder.recipecatalog.domain;

import java.util.Objects;
import java.util.UUID;

/** A cook on Larder; in this context the owner of a recipe. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }
}
