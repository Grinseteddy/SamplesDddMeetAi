package com.cookwithus.infrastructure;

import com.cookwithus.domain.cook.CookId;
import com.cookwithus.domain.recipe.Recipe;
import com.cookwithus.domain.recipe.RecipeId;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Recipe persistence in the Sharing bounded context.
 *
 * <p>This interface is part of the domain layer. Implementations live in the
 * infrastructure layer and must not leak persistence concerns into the domain.
 *
 * <p>Because the Sharing context uses Event Sourcing (ADR 002), implementations
 * are expected to persist and replay domain events rather than storing current
 * state directly. This interface deliberately hides that detail: callers work
 * with fully reconstituted Recipe aggregates.
 *
 * <p>Invariant enforcement at the persistence boundary:
 * the {@link #save(Recipe)} method must reject a Recipe that violates any
 * confirmed domain invariant, even if the domain model failed to enforce it.
 * This is a defence-in-depth measure, not a substitute for domain validation.
 */
public interface RecipeRepository {

    /**
     * Persist a Recipe (new or revised).
     *
     * <p>For a new Recipe this publishes a {@code RecipePublished} event to the event store.
     * For a revised Recipe this appends a {@code RecipeRevised} event.
     *
     * @param recipe the Recipe to persist; must not be null
     * @throws IllegalArgumentException if the Recipe violates a domain invariant
     */
    void save(Recipe recipe);

    /**
     * Find a Recipe by its identity.
     *
     * @param recipeId the identity of the Recipe to find
     * @return the Recipe, or {@link Optional#empty()} if no Recipe with the given id exists
     */
    Optional<Recipe> findById(RecipeId recipeId);

    /**
     * Find all Recipes published by a given Cook.
     *
     * @param cookId the identity of the authoring Cook
     * @return the list of Recipes authored by the Cook; never null, may be empty
     */
    List<Recipe> findByAuthor(CookId cookId);

    /**
     * Find all published Recipes, optionally filtered by category.
     *
     * @param category the category to filter by, or {@code null} for all categories
     * @param page     zero-based page number
     * @param pageSize maximum number of results per page
     * @return the list of matching Recipes; never null, may be empty
     */
    List<Recipe> findAll(String category, int page, int pageSize);
}
