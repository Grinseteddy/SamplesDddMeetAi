package org.larder.mealplanning.domain;

import java.util.Objects;

/** A course as a cook enters it: the step it is served at and the recipe it refers to. */
public record CourseDraft(int step, RecipeId recipe) {

    public CourseDraft {
        Course.requireStep(step);
        Objects.requireNonNull(recipe, "A course refers to a recipe");
    }
}
