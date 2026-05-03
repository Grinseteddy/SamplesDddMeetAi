package com.cookwithus.domain.recipe;

/**
 * Ingredient — Value Object in the Sharing bounded context.
 *
 * <p>An Ingredient is defined entirely by its name, quantity, and unit.
 * Two Ingredient instances with the same name, quantity, and unit are equal;
 * they carry no independent identity.
 *
 * <p>Invariants:
 * <ol>
 *   <li>name must be non-null and non-empty.</li>
 *   <li>quantity must be positive (greater than zero).</li>
 *   <li>unit must be non-null and non-empty.</li>
 * </ol>
 */
public record Ingredient(String name, double quantity, String unit) {

    public Ingredient {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ingredient name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Ingredient quantity must be positive, got: " + quantity);
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Ingredient unit must not be blank");
        }
        name = name.trim();
        unit = unit.trim();
    }
}
