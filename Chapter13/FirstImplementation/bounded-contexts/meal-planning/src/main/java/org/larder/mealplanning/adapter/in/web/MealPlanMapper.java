package org.larder.mealplanning.adapter.in.web;

import org.larder.mealplanning.adapter.in.web.model.Course;
import org.larder.mealplanning.adapter.in.web.model.CourseMeal;
import org.larder.mealplanning.adapter.in.web.model.Meal;
import org.larder.mealplanning.adapter.in.web.model.MealPlan;

/**
 * Translates the domain model into the contract's model - and only in this direction. The courses'
 * diet snapshots are internal to Meal Planning and not part of the contract.
 */
final class MealPlanMapper {

    private MealPlanMapper() {
    }

    static MealPlan toApi(org.larder.mealplanning.domain.MealPlan mealPlan) {
        return new MealPlan(mealPlan.id().value(), mealPlan.owner().value())
                .occasion(mealPlan.occasion().orElse(null))
                .servings(mealPlan.servings().orElse(null))
                .meal(mealPlan.meal().map(meal -> Meal.valueOf(meal.name())).orElse(null))
                .howToServe(mealPlan.howToServe().orElse(null))
                .courses(mealPlan.courses().isEmpty() ? null
                        : mealPlan.courses().stream().map(MealPlanMapper::toApi).toList());
    }

    static Course toApi(org.larder.mealplanning.domain.Course course) {
        return new Course(course.step(), new CourseMeal(course.recipe().value()));
    }
}
