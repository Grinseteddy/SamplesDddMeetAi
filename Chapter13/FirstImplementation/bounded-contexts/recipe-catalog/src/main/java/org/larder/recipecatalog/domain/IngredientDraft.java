package org.larder.recipecatalog.domain;

/** What a cook supplies for a new ingredient; the recipe gives it its identity. */
public record IngredientDraft(String name, Quantity quantity) {
}
