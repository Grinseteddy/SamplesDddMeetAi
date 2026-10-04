package org.larder.mealplanning.domain;

import java.util.List;

/**
 * A partial change of a meal plan. {@code null} means "leave as it is"; {@code removeHowToServe}
 * removes the serving instructions. Given {@code courses} replace the existing ones as a whole.
 */
public record MealPlanRevision(String occasion, Integer servings, Meal meal, String howToServe,
                               boolean removeHowToServe, List<Course> courses) {

    public MealPlanRevision {
        if (removeHowToServe && howToServe != null) {
            throw new IllegalArgumentException("howToServe cannot be set and removed at once");
        }
    }

    public static MealPlanRevision none() {
        return new MealPlanRevision(null, null, null, null, false, null);
    }

    public MealPlanRevision withOccasion(String newOccasion) {
        return new MealPlanRevision(newOccasion, servings, meal, howToServe, removeHowToServe, courses);
    }

    public MealPlanRevision withServings(Integer newServings) {
        return new MealPlanRevision(occasion, newServings, meal, howToServe, removeHowToServe, courses);
    }

    public MealPlanRevision withCourses(List<Course> newCourses) {
        return new MealPlanRevision(occasion, servings, meal, howToServe, removeHowToServe, newCourses);
    }

    public MealPlanRevision withHowToServe(String newHowToServe) {
        return new MealPlanRevision(occasion, servings, meal, newHowToServe, false, courses);
    }

    public MealPlanRevision withoutHowToServe() {
        return new MealPlanRevision(occasion, servings, meal, null, true, courses);
    }
}
