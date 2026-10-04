package org.larder.mealplanning.application;

import java.util.List;

import org.larder.mealplanning.domain.CourseDraft;
import org.larder.mealplanning.domain.Meal;

/**
 * Command: change parts of a meal plan. {@code null} leaves a part as it is; {@code removeHowToServe}
 * removes the serving instructions; given {@code courses} replace the existing ones as a whole.
 */
public record ChangeMealPlan(String occasion, Integer servings, Meal meal, String howToServe,
                             boolean removeHowToServe, List<CourseDraft> courses) {

    public static ChangeMealPlan none() {
        return new ChangeMealPlan(null, null, null, null, false, null);
    }
}
