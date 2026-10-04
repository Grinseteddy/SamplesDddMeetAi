package org.larder.mealplanning.domain;

import java.util.Objects;

/**
 * What Meal Planning needs to know about a recipe of the Recipe Catalog: that it exists and which
 * diet it is suitable for. Meal Planning's own view of a recipe, not the catalog's model.
 */
public record RecipeFacts(RecipeId id, Diet diet) {

    public RecipeFacts {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(diet, "diet must not be null");
    }
}
