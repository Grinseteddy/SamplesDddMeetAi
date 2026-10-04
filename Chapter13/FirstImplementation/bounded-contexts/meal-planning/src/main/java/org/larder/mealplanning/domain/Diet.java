package org.larder.mealplanning.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The diet a meal plan is suitable for. Meal Planning keeps no diet of its own: the diet of a plan is
 * derived from the diets of the recipes its courses refer to (see {@link MealPlan#diet()}).
 *
 * <p>Diets are ordered by restrictiveness: {@code VEGAN} &gt; {@code VEGETARIAN} &gt; {@code NORMAL}.
 * Food suitable for a more restrictive diet is also suitable for every less restrictive one -
 * a vegan dish is fine for vegetarians.
 */
public enum Diet {
    NORMAL(0), VEGETARIAN(1), VEGAN(2);

    private final int restrictiveness;

    Diet(int restrictiveness) {
        this.restrictiveness = restrictiveness;
    }

    /** Whether food of this diet may be served to someone following {@code other}. */
    public boolean isAtLeastAsRestrictiveAs(Diet other) {
        return restrictiveness >= other.restrictiveness;
    }

    /** All diets whose food is suitable for this diet, i.e. this one and the more restrictive ones. */
    public Set<Diet> suitableDiets() {
        return Arrays.stream(values()).filter(diet -> diet.isAtLeastAsRestrictiveAs(this))
                .collect(Collectors.toUnmodifiableSet());
    }

    /** The most restrictive of the given diets. */
    public static Diet mostRestrictive(Collection<Diet> diets) {
        return diets.stream().max(Comparator.comparingInt(diet -> diet.restrictiveness))
                .orElseThrow(() -> new IllegalArgumentException("No diet given"));
    }

    /** The least restrictive of the given diets. */
    public static Diet leastRestrictive(Collection<Diet> diets) {
        return diets.stream().min(Comparator.comparingInt(diet -> diet.restrictiveness))
                .orElseThrow(() -> new IllegalArgumentException("No diet given"));
    }

    /** A diet by its name, ignoring case and surrounding blanks, e.g. {@code vegetarian}. */
    public static Diet named(String name) {
        String normalized = name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(diet -> diet.name().equals(normalized)).findFirst()
                .orElseThrow(() -> new MealPlanRuleViolationException(MealPlanRuleViolationException.UNKNOWN_DIET,
                        "Unknown diet '" + name + "'; known diets are " + Arrays.toString(values())));
    }
}
