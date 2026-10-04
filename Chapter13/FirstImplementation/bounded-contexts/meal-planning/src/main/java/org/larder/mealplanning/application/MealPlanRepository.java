package org.larder.mealplanning.application;

import java.util.List;
import java.util.Optional;

import org.larder.mealplanning.domain.CookId;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanId;
import org.larder.mealplanning.domain.MealPlanSearch;

/** Port: the meal plans, each stored as a whole with its courses. */
public interface MealPlanRepository {

    void save(MealPlan mealPlan);

    Optional<MealPlan> findById(MealPlanId id);

    /** The meal plans of {@code owner} satisfying {@code search}. */
    List<MealPlan> search(CookId owner, MealPlanSearch search);

    void delete(MealPlanId id);
}
