package org.larder.mealplanning.adapter.out.recipecatalog;

import java.util.Optional;

import org.larder.mealplanning.adapter.out.recipecatalog.client.api.RecipesApi;
import org.larder.mealplanning.adapter.out.recipecatalog.client.model.Recipe;
import org.larder.mealplanning.application.NotPermittedException;
import org.larder.mealplanning.application.RecipeCatalog;
import org.larder.mealplanning.application.RecipeCatalogUnavailableException;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.RecipeFacts;
import org.larder.mealplanning.domain.RecipeId;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Anticorruption layer to the Recipe Catalog: translates the catalog's {@code Recipe} into Meal
 * Planning's {@link RecipeFacts}. Nothing of the generated client leaves this package.
 *
 * <ul>
 *   <li>404 → the recipe is unknown ({@link Optional#empty()})</li>
 *   <li>403 → the caller may not read the recipe ({@link NotPermittedException})</li>
 *   <li>anything else that is not a recipe → {@link RecipeCatalogUnavailableException}</li>
 * </ul>
 */
class HttpRecipeCatalog implements RecipeCatalog {

    /** The version of the Recipe Catalog contract the client was generated from. */
    static final String CONTRACT_VERSION = "1.0.0";

    private final RecipesApi recipes;

    HttpRecipeCatalog(RecipesApi recipes) {
        this.recipes = recipes;
    }

    @Override
    public Optional<RecipeFacts> recipe(RecipeId id) {
        Recipe recipe;
        try {
            recipe = recipes.getRecipeById(id.value(), CONTRACT_VERSION);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return Optional.empty();
            }
            if (e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
                throw new NotPermittedException("The Recipe Catalog does not let the caller read recipe " + id.value());
            }
            throw unavailable(id, e);
        } catch (RestClientException e) {
            throw unavailable(id, e);
        }
        if (recipe == null || recipe.getDiet() == null) {
            throw unavailable(id, null);
        }
        return Optional.of(new RecipeFacts(id, toDomain(recipe.getDiet())));
    }

    private static Diet toDomain(org.larder.mealplanning.adapter.out.recipecatalog.client.model.Diet diet) {
        return switch (diet) {
            case VEGAN -> Diet.VEGAN;
            case VEGETARIAN -> Diet.VEGETARIAN;
            case NORMAL -> Diet.NORMAL;
        };
    }

    private static RecipeCatalogUnavailableException unavailable(RecipeId id, Exception cause) {
        return new RecipeCatalogUnavailableException(
                "The Recipe Catalog cannot answer for recipe " + id.value(), cause);
    }
}
