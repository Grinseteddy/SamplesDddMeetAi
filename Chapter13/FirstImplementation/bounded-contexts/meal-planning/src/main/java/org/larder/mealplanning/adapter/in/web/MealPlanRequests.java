package org.larder.mealplanning.adapter.in.web;

import java.util.List;
import java.util.Set;

import org.larder.mealplanning.adapter.in.web.model.Course;
import org.larder.mealplanning.adapter.in.web.model.MealPlanCreate;
import org.larder.mealplanning.adapter.in.web.model.MealPlanUpdate;
import org.larder.mealplanning.application.ChangeMealPlan;
import org.larder.mealplanning.application.SetUpMealPlan;
import org.larder.mealplanning.domain.CookId;
import org.larder.mealplanning.domain.CourseDraft;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.Meal;
import org.larder.mealplanning.domain.MealPlanSearch;
import org.larder.mealplanning.domain.RecipeId;
import org.larder.platform.security.CurrentCook;

/**
 * Translates the contract's requests into the application's commands and the domain's search. Bean
 * validation of the generated model has already checked the contract's constraints; the domain checks
 * the rest (e.g. no blank occasion).
 */
final class MealPlanRequests {

    private MealPlanRequests() {
    }

    static CookId caller() {
        return new CookId(CurrentCook.require().value());
    }

    /** Diet names ignore case (the contract's example is {@code vegetarian}); an unknown diet is a bad request. */
    static MealPlanSearch toSearch(List<String> diets, String occasion) {
        List<Diet> requested = diets == null ? List.of() : diets.stream().map(Diet::named).toList();
        return MealPlanSearch.of(requested, occasion);
    }

    static SetUpMealPlan toCommand(MealPlanCreate request) {
        return new SetUpMealPlan(
                request.getOccasion(),
                request.getServings(),
                toDomain(request.getMeal()),
                request.getHowToServe(),
                toDrafts(request.getCourses()));
    }

    /**
     * Only properties given in the body change the plan. {@code howToServe} given as {@code null}
     * removes the serving instructions - the generated model cannot tell that from an absent
     * property, hence the {@code given} property names read from the body.
     */
    static ChangeMealPlan toCommand(MealPlanUpdate request, Set<String> given) {
        boolean removeHowToServe = given.contains("howToServe") && request.getHowToServe() == null;
        return new ChangeMealPlan(
                request.getOccasion(),
                request.getServings(),
                toDomain(request.getMeal()),
                request.getHowToServe(),
                removeHowToServe,
                toDrafts(request.getCourses()));
    }

    private static List<CourseDraft> toDrafts(List<Course> courses) {
        return courses == null ? null : courses.stream()
                .map(course -> new CourseDraft(course.getStep(), new RecipeId(course.getMeal().getRecipe())))
                .toList();
    }

    static Meal toDomain(org.larder.mealplanning.adapter.in.web.model.Meal meal) {
        return meal == null ? null : Meal.valueOf(meal.name());
    }
}
