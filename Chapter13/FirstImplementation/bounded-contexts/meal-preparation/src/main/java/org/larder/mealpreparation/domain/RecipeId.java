package org.larder.mealpreparation.domain;

import java.util.Objects;
import java.util.UUID;

/** A recipe of Recipe Catalog; this context only refers to it by its identifier. */
public record RecipeId(UUID value) {

    public RecipeId {
        Objects.requireNonNull(value, "recipe must not be null");
    }
}
