package org.larder.mealplanning.domain;

import java.util.Objects;
import java.util.UUID;

/** Reference to a recipe of the Recipe Catalog; the recipe itself is owned there. */
public record RecipeId(UUID value) {

    public RecipeId {
        Objects.requireNonNull(value, "recipeId must not be null");
    }
}
