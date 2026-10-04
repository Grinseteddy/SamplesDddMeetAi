package org.larder.grandmaavatar.domain;

import java.util.Objects;
import java.util.UUID;

/** A recipe of Recipe Catalog, known to Grandma only by its id. */
public record RecipeId(UUID value) {

    public RecipeId {
        Objects.requireNonNull(value, "recipeId");
    }
}
