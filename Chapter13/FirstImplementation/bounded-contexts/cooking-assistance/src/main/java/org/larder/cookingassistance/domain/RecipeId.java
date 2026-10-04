package org.larder.cookingassistance.domain;

import java.util.Objects;
import java.util.UUID;

/** A recipe of Recipe Catalog, referenced by its id only. */
public record RecipeId(UUID value) {

    public RecipeId {
        Objects.requireNonNull(value, "recipe must not be null");
    }
}
