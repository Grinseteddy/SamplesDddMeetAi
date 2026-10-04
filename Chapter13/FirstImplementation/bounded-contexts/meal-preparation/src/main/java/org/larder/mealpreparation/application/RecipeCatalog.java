package org.larder.mealpreparation.application;

import java.util.Optional;

import org.larder.mealpreparation.domain.RecipeId;
import org.larder.mealpreparation.domain.RecipeSteps;

/**
 * Port: Recipe Catalog, the upstream that owns recipes. Answers in this context's language;
 * nothing of Recipe Catalog's own model passes this port.
 */
public interface RecipeCatalog {

    /**
     * The how-to steps of {@code recipe} as they are now, or empty if Recipe Catalog does not know the recipe.
     *
     * @throws NotPermittedException if Recipe Catalog does not let the calling cook read the recipe
     * @throws RecipeCatalogUnavailableException if Recipe Catalog cannot answer
     */
    Optional<RecipeSteps> stepsOf(RecipeId recipe);
}
