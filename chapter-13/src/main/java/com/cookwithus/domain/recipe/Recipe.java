package com.cookwithus.domain.recipe;

import com.cookwithus.domain.cook.CookId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Recipe — Aggregate Root of the Sharing bounded context.
 *
 * <p>A Recipe is authored by a Cook, published to the community feed, and may be
 * revised by its author. The Recipe owns its Ingredients and enforces all
 * recipe-level invariants.
 *
 * <p>Domain events raised by this aggregate (to be published via the event store):
 * <ul>
 *   <li>{@code RecipePublished} — when a Recipe is first published</li>
 *   <li>{@code RecipeRevised} — when an author revises their Recipe</li>
 * </ul>
 *
 * <p>Invariants confirmed by the domain team (Chapter 13):
 * <ol>
 *   <li>A Recipe must have at least one Ingredient.</li>
 *   <li>A Recipe's title must be non-empty and at most 120 characters.</li>
 *   <li>An Ingredient's quantity must be positive (enforced in {@link Ingredient}).</li>
 * </ol>
 */
public class Recipe {

    private static final int TITLE_MAX_LENGTH = 120;

    private final RecipeId recipeId;
    private final CookId authorId;
    private String title;
    private String description;
    private String category;
    private final List<Ingredient> ingredients;
    private final List<String> steps;
    private final Instant publishedAt;
    private Instant revisedAt;

    /**
     * Publish a new Recipe.
     *
     * @param recipeId    unique identity of this Recipe
     * @param authorId    CookId of the authoring Cook
     * @param title       recipe title
     * @param description optional description
     * @param category    optional category
     * @param ingredients list of Ingredients (must contain at least one)
     * @param steps       list of preparation steps (must contain at least one)
     * @param publishedAt the instant of publication
     */
    public Recipe(
            RecipeId recipeId,
            CookId authorId,
            String title,
            String description,
            String category,
            List<Ingredient> ingredients,
            List<String> steps,
            Instant publishedAt) {
        this.recipeId = Objects.requireNonNull(recipeId, "recipeId must not be null");
        this.authorId = Objects.requireNonNull(authorId, "authorId must not be null");
        this.title = validateTitle(title);
        this.description = description;
        this.category = category;
        this.ingredients = new ArrayList<>(validateIngredients(ingredients));
        this.steps = new ArrayList<>(validateSteps(steps));
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt must not be null");
    }

    // --- Identity -----------------------------------------------------------

    public RecipeId getRecipeId() {
        return recipeId;
    }

    // --- Accessors ----------------------------------------------------------

    public CookId getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public List<String> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getRevisedAt() {
        return revisedAt;
    }

    // --- Behaviour ----------------------------------------------------------

    /**
     * Revise this Recipe.
     * Only the authoring Cook may revise their Recipe.
     *
     * @param requestingCookId the Cook requesting the revision
     * @param newTitle         updated title
     * @param newDescription   updated description (may be null)
     * @param newIngredients   updated ingredient list (must contain at least one)
     * @param newSteps         updated steps (must contain at least one)
     * @param revisedAt        the instant of revision
     * @throws IllegalArgumentException if the requesting Cook is not the author
     */
    public void revise(
            CookId requestingCookId,
            String newTitle,
            String newDescription,
            List<Ingredient> newIngredients,
            List<String> newSteps,
            Instant revisedAt) {
        if (!authorId.equals(requestingCookId)) {
            throw new IllegalArgumentException(
                    "Only the authoring Cook may revise this Recipe. Author: "
                    + authorId + ", requester: " + requestingCookId);
        }
        this.title = validateTitle(newTitle);
        this.description = newDescription;
        this.ingredients.clear();
        this.ingredients.addAll(validateIngredients(newIngredients));
        this.steps.clear();
        this.steps.addAll(validateSteps(newSteps));
        this.revisedAt = Objects.requireNonNull(revisedAt, "revisedAt must not be null");
    }

    // --- Equality (identity-based) ------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe recipe)) return false;
        return recipeId.equals(recipe.recipeId);
    }

    @Override
    public int hashCode() {
        return recipeId.hashCode();
    }

    @Override
    public String toString() {
        return "Recipe{recipeId=" + recipeId + ", title='" + title + "'}";
    }

    // --- Private validation -------------------------------------------------

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Recipe title must not be blank");
        }
        String trimmed = title.trim();
        if (trimmed.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Recipe title must not exceed " + TITLE_MAX_LENGTH + " characters");
        }
        return trimmed;
    }

    private static List<Ingredient> validateIngredients(List<Ingredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("A Recipe must have at least one Ingredient");
        }
        return ingredients;
    }

    private static List<String> validateSteps(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("A Recipe must have at least one preparation step");
        }
        return steps;
    }
}
