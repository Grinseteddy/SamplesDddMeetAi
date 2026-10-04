package org.larder.mealplanning.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.larder.mealplanning.adapter.in.web.model.MealPlan;
import org.springframework.stereotype.Component;

/**
 * A meal plan that is still being filled lacks occasion, servings, meal, serving instructions or
 * courses. The contract declares none of them nullable and requires 1..10 courses when present, so
 * missing parts are left out of the response instead of being written as {@code null} or {@code []}.
 * Applies only to this context's generated {@link MealPlan}.
 */
@Component("mealplanningJacksonModule")
class MealPlanningJacksonModule extends SimpleModule {

    MealPlanningJacksonModule() {
        super("mealplanning");
        setMixInAnnotation(MealPlan.class, OmitMissingParts.class);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private abstract static class OmitMissingParts {
    }
}
