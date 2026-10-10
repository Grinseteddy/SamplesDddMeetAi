package de.codecentric.cookingassistance.domain.external;

import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.UUID;

/** Identity of a Recipe, owned outside Cooking Assistance (glossary term "recipe"). */
public record RecipeId(UUID value) {

    public RecipeId {
        Require.present(value, "recipe");
    }

    public static RecipeId of(String value) {
        return new RecipeId(UUID.fromString(value));
    }
}
