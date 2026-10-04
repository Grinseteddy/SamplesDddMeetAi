package org.larder.recipecatalog.domain;

import java.net.URI;
import java.util.List;

/**
 * A partial change of a recipe's own details. {@code null} means "leave as it is";
 * ingredients, how-to steps and the meal are changed through their own operations.
 */
public record RecipeRevision(
        String name,
        String subtitle,
        URI mainImage,
        List<URI> furtherImages,
        PreparationTime preparationTime,
        Integer servings,
        Diet diet) {

    public static RecipeRevision none() {
        return new RecipeRevision(null, null, null, null, null, null, null);
    }

    public RecipeRevision withName(String newName) {
        return new RecipeRevision(newName, subtitle, mainImage, furtherImages, preparationTime, servings, diet);
    }

    public RecipeRevision withServings(Integer newServings) {
        return new RecipeRevision(name, subtitle, mainImage, furtherImages, preparationTime, newServings, diet);
    }

    public RecipeRevision withDiet(Diet newDiet) {
        return new RecipeRevision(name, subtitle, mainImage, furtherImages, preparationTime, servings, newDiet);
    }
}
