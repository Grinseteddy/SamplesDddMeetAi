package org.larder.cookingassistance.domain;

import java.util.Objects;
import java.util.UUID;

/** An ingredient of a recipe in Recipe Catalog, referenced by its id only. */
public record IngredientId(UUID value) {

    public IngredientId {
        Objects.requireNonNull(value, "ingredient must not be null");
    }
}
