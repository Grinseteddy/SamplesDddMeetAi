package org.larder.mealplanning.application;

import java.util.Optional;

import org.larder.mealplanning.domain.RecipeFacts;
import org.larder.mealplanning.domain.RecipeId;

/**
 * Port to the upstream Recipe Catalog, in Meal Planning's own language.
 *
 * @throws NotPermittedException when the catalog refuses the caller (e.g. the token lacks a recipe scope)
 * @throws RecipeCatalogUnavailableException when the catalog cannot answer
 */
public interface RecipeCatalog {

    /** The facts of the recipe, empty when the catalog does not know it. */
    Optional<RecipeFacts> recipe(RecipeId id);
}
