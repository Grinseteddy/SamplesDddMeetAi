package org.larder.recipecatalog.domain;

import java.net.URI;
import java.util.List;

/** What a cook supplies to create a recipe; identifiers and the owner are set by the catalog. */
public record RecipeDraft(
        String name,
        String subtitle,
        URI mainImage,
        List<URI> furtherImages,
        PreparationTime preparationTime,
        int servings,
        Meal meal,
        Diet diet,
        List<IngredientDraft> ingredients,
        List<HowToStepDraft> howToSteps) {

    public RecipeDraft {
        furtherImages = furtherImages == null ? List.of() : List.copyOf(furtherImages);
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
        howToSteps = howToSteps == null ? List.of() : List.copyOf(howToSteps);
    }
}
