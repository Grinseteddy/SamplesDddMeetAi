package org.larder.mealplanning.domain;

import java.util.Objects;

/**
 * One course of a meal plan (value object): served at {@code step} and referring to a recipe of the
 * Recipe Catalog. Courses with the same step are served in parallel.
 *
 * <p>{@code diet} is a snapshot of the recipe's diet taken when the courses were set. Later changes of
 * the recipe's diet in the Recipe Catalog are not reflected until the cook sets the courses again.
 */
public record Course(int step, RecipeId recipe, Diet diet) {

    public Course {
        requireStep(step);
        Objects.requireNonNull(recipe, "A course refers to a recipe");
        Objects.requireNonNull(diet, "A course knows the diet of its recipe");
    }

    /** The course of a draft, with the diet of the recipe it refers to. */
    public static Course of(CourseDraft draft, RecipeFacts recipe) {
        if (!draft.recipe().equals(recipe.id())) {
            throw new IllegalArgumentException("Recipe " + recipe.id().value() + " is not the recipe of the course");
        }
        return new Course(draft.step(), draft.recipe(), recipe.diet());
    }

    static void requireStep(int step) {
        if (step < 1) {
            throw MealPlanRuleViolationException.invalid("The step of a course starts at 1, not " + step);
        }
    }
}
