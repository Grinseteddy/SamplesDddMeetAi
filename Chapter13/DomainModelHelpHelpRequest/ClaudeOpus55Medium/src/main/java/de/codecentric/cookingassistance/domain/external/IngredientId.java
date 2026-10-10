package de.codecentric.cookingassistance.domain.external;

import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.UUID;

/** Identity of an ingredient, owned outside Cooking Assistance (glossary terms "ingredients" / "ingredient"). */
public record IngredientId(UUID value) {

    public IngredientId {
        Require.present(value, "ingredient");
    }

    public static IngredientId of(String value) {
        return new IngredientId(UUID.fromString(value));
    }
}
