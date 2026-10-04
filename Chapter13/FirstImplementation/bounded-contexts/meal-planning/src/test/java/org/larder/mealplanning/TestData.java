package org.larder.mealplanning;

import java.util.List;
import java.util.UUID;

import org.larder.mealplanning.domain.CookId;
import org.larder.mealplanning.domain.Course;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.Meal;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanDraft;
import org.larder.mealplanning.domain.MealPlanId;
import org.larder.mealplanning.domain.RecipeFacts;
import org.larder.mealplanning.domain.RecipeId;

/** The examples of the contract and the visual glossary. */
public final class TestData {

    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final CookId OTHER_COOK = new CookId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final MealPlanId PLAN_ID = new MealPlanId(UUID.fromString("0d3c5a1e-6b7f-4c2d-9e8a-1f2b3c4d5e6f"));

    /** The glossary's scones, a vegetarian recipe. */
    public static final RecipeId SCONES = new RecipeId(UUID.fromString("7cf09822-77a1-46bb-812f-b7852bca0913"));
    public static final RecipeId SOUP = new RecipeId(UUID.fromString("a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"));
    public static final RecipeId ROAST = new RecipeId(UUID.fromString("b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e"));

    public static final RecipeFacts SCONES_FACTS = new RecipeFacts(SCONES, Diet.VEGETARIAN);
    public static final RecipeFacts SOUP_FACTS = new RecipeFacts(SOUP, Diet.VEGAN);
    public static final RecipeFacts ROAST_FACTS = new RecipeFacts(ROAST, Diet.NORMAL);

    public static final String OCCASION = "parents in law visiting";
    public static final String HOW_TO_SERVE = "hold course 2 warm while serving soup";

    private TestData() {
    }

    public static Course soup(int step) {
        return new Course(step, SOUP, Diet.VEGAN);
    }

    public static Course scones(int step) {
        return new Course(step, SCONES, Diet.VEGETARIAN);
    }

    public static Course roast(int step) {
        return new Course(step, ROAST, Diet.NORMAL);
    }

    /** The contract's example plan: soup first, scones second, owned by {@link #COOK}. */
    public static MealPlan dinner() {
        return MealPlan.restore(PLAN_ID, COOK, OCCASION, 6, Meal.DINNER, HOW_TO_SERVE, List.of(soup(1), scones(2)));
    }

    public static MealPlan planOf(CookId owner, String occasion, Course... courses) {
        return MealPlan.setUp(owner, new MealPlanDraft(occasion, 4, Meal.DINNER, null,
                courses.length == 0 ? null : List.of(courses)));
    }
}
