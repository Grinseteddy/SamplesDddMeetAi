package org.larder.mealplanning.domain;

import java.util.List;
import java.util.Optional;

/**
 * Search criteria for meal plans; both are optional and combined with AND.
 *
 * <ul>
 *   <li>{@code diet}: the plan is suitable for this diet (see {@link MealPlan#isSuitableFor(Diet)}).
 *       When several diets are requested only the most restrictive one is used.</li>
 *   <li>{@code occasion}: the plan's occasion equals this text, ignoring case. No substring or
 *       fuzzy matching - the contract does not say more than "occasion the meal plans were set up for".</li>
 * </ul>
 */
public record MealPlanSearch(Diet diet, String occasion) {

    public static MealPlanSearch all() {
        return new MealPlanSearch(null, null);
    }

    /** The search for the most restrictive of {@code diets} (none: any diet) and {@code occasion}. */
    public static MealPlanSearch of(List<Diet> diets, String occasion) {
        Diet diet = diets == null || diets.isEmpty() ? null : Diet.mostRestrictive(diets);
        return new MealPlanSearch(diet, occasion);
    }

    public Optional<Diet> dietFilter() {
        return Optional.ofNullable(diet);
    }

    public Optional<String> occasionFilter() {
        return Optional.ofNullable(occasion);
    }

    public boolean isSatisfiedBy(MealPlan plan) {
        return (diet == null || plan.isSuitableFor(diet))
                && (occasion == null || plan.isFor(occasion));
    }
}
