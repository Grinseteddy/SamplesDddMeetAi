package org.larder.recipecatalog.domain;

import java.util.Objects;

/**
 * One ingredient of a recipe (entity inside the {@link Recipe} aggregate) with the quantity
 * needed for the recipe's servings. Changed only through its recipe.
 */
public final class Ingredient {

    static final int MAX_NAME_LENGTH = 100;

    private final IngredientId id;
    private String name;
    private Quantity quantity;

    private Ingredient(IngredientId id, String name, Quantity quantity) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.quantity = requireQuantity(quantity);
    }

    static Ingredient create(IngredientDraft draft) {
        Objects.requireNonNull(draft, "An ingredient needs its data");
        return new Ingredient(IngredientId.newId(), draft.name(), draft.quantity());
    }

    /** Recreates a stored ingredient. */
    public static Ingredient restore(IngredientId id, String name, Quantity quantity) {
        return new Ingredient(id, name, quantity);
    }

    /** Changes name and/or quantity; {@code null} leaves the part as it is. Validates before changing anything. */
    void change(String newName, Quantity newQuantity) {
        String validName = newName == null ? name : requireName(newName);
        Quantity validQuantity = newQuantity == null ? quantity : newQuantity;
        name = validName;
        quantity = validQuantity;
    }

    static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw RecipeRuleViolationException.invalid("An ingredient needs a name");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw RecipeRuleViolationException.invalid("The name of an ingredient has at most " + MAX_NAME_LENGTH + " characters");
        }
        return name;
    }

    private static Quantity requireQuantity(Quantity quantity) {
        if (quantity == null) {
            throw RecipeRuleViolationException.invalid("An ingredient needs a value and a unit");
        }
        return quantity;
    }

    public boolean isCalled(String candidate) {
        return candidate != null && name.equalsIgnoreCase(candidate.trim());
    }

    public IngredientId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Quantity quantity() {
        return quantity;
    }
}
