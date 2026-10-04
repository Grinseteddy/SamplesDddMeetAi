package org.larder.recipecatalog.domain;

import java.util.Objects;
import java.util.UUID;

public record RecipeId(UUID value) {

    public RecipeId {
        Objects.requireNonNull(value, "recipeId must not be null");
    }

    public static RecipeId newId() {
        return new RecipeId(UUID.randomUUID());
    }
}
