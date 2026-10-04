package org.larder.recipecatalog.application;

import java.util.List;
import java.util.Optional;

import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.RecipeSearch;

/** Port: the recipes of the catalog, each stored as a whole with its ingredients and how-to steps. */
public interface RecipeRepository {

    void save(Recipe recipe);

    Optional<Recipe> findById(RecipeId id);

    List<Recipe> search(RecipeSearch search);

    void delete(RecipeId id);
}
