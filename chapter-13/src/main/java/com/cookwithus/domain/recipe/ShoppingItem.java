package com.cookwithus.domain.recipe;

/**
 * ShoppingItem — Value Object used in the Ordering bounded context.
 *
 * <p>A ShoppingItem is a snapshot of an Ingredient at the moment a Cook adds it
 * to their shopping list. It preserves the ingredient name, quantity, and unit
 * from the Recipe as published, even if the Recipe is later revised.
 *
 * <p>ShoppingItem is structurally identical to Ingredient but lives in the Ordering
 * context and therefore carries separate domain semantics: it belongs to an order,
 * not to a recipe.
 */
public record ShoppingItem(String ingredientName, double quantity, String unit) {

    public ShoppingItem {
        if (ingredientName == null || ingredientName.isBlank()) {
            throw new IllegalArgumentException("ShoppingItem ingredientName must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("ShoppingItem quantity must be positive, got: " + quantity);
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("ShoppingItem unit must not be blank");
        }
        ingredientName = ingredientName.trim();
        unit = unit.trim();
    }

    /**
     * Factory method: create a ShoppingItem from an Ingredient.
     */
    public static ShoppingItem from(Ingredient ingredient) {
        return new ShoppingItem(ingredient.name(), ingredient.quantity(), ingredient.unit());
    }
}
