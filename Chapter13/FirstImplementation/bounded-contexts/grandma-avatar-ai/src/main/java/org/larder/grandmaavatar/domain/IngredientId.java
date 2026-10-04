package org.larder.grandmaavatar.domain;

import java.util.Objects;
import java.util.UUID;

/** An ingredient of a recipe, known to Grandma only by its id. */
public record IngredientId(UUID value) {

    public IngredientId {
        Objects.requireNonNull(value, "ingredientId");
    }
}
