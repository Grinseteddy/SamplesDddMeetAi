package org.larder.mealplanning.domain;

import java.util.Objects;
import java.util.UUID;

public record MealPlanId(UUID value) {

    public MealPlanId {
        Objects.requireNonNull(value, "mealPlanId must not be null");
    }

    public static MealPlanId newId() {
        return new MealPlanId(UUID.randomUUID());
    }
}
