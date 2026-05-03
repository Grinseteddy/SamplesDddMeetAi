package com.cookwithus.domain.recipe;

import java.util.UUID;

/**
 * Value Object representing the identity of a Recipe.
 */
public record RecipeId(UUID value) {

    public RecipeId {
        if (value == null) {
            throw new IllegalArgumentException("RecipeId value must not be null");
        }
    }

    public static RecipeId newRecipeId() {
        return new RecipeId(UUID.randomUUID());
    }

    public static RecipeId of(String value) {
        return new RecipeId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
