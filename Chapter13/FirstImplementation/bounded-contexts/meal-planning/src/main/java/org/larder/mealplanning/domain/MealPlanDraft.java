package org.larder.mealplanning.domain;

import java.util.List;

/**
 * What a cook supplies when setting up a meal plan; every part may be missing ({@code null}).
 * {@code courses} are already resolved against the Recipe Catalog.
 */
public record MealPlanDraft(String occasion, Integer servings, Meal meal, String howToServe, List<Course> courses) {

    public static MealPlanDraft empty() {
        return new MealPlanDraft(null, null, null, null, null);
    }
}
