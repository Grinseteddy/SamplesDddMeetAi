package org.larder.recipecatalog.domain;

import java.util.Objects;
import java.util.UUID;

public record IngredientId(UUID value) {

    public IngredientId {
        Objects.requireNonNull(value, "ingredientId must not be null");
    }

    public static IngredientId newId() {
        return new IngredientId(UUID.randomUUID());
    }
}
