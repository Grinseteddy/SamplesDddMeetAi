package org.larder.mealplanning.application;

import java.util.List;

import org.larder.mealplanning.domain.CourseDraft;
import org.larder.mealplanning.domain.Meal;

/** Command: set up a meal plan; every part may be missing ({@code null}). */
public record SetUpMealPlan(String occasion, Integer servings, Meal meal, String howToServe, List<CourseDraft> courses) {

    public static SetUpMealPlan empty() {
        return new SetUpMealPlan(null, null, null, null, null);
    }
}
