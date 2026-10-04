package org.larder.mealplanning.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class DietTest {

    @Test
    void veganIsMoreRestrictiveThanVegetarianWhichIsMoreRestrictiveThanNormal() {
        assertThat(Diet.VEGAN.isAtLeastAsRestrictiveAs(Diet.VEGETARIAN)).isTrue();
        assertThat(Diet.VEGETARIAN.isAtLeastAsRestrictiveAs(Diet.NORMAL)).isTrue();
        assertThat(Diet.VEGETARIAN.isAtLeastAsRestrictiveAs(Diet.VEGETARIAN)).isTrue();
        assertThat(Diet.VEGETARIAN.isAtLeastAsRestrictiveAs(Diet.VEGAN)).isFalse();
        assertThat(Diet.NORMAL.isAtLeastAsRestrictiveAs(Diet.VEGETARIAN)).isFalse();
    }

    @Test
    void theSuitableDietsOfADietAreItselfAndTheMoreRestrictiveOnes() {
        assertThat(Diet.NORMAL.suitableDiets()).containsExactlyInAnyOrder(Diet.NORMAL, Diet.VEGETARIAN, Diet.VEGAN);
        assertThat(Diet.VEGETARIAN.suitableDiets()).containsExactlyInAnyOrder(Diet.VEGETARIAN, Diet.VEGAN);
        assertThat(Diet.VEGAN.suitableDiets()).containsExactly(Diet.VEGAN);
    }

    @Test
    void picksTheMostAndTheLeastRestrictiveDiet() {
        assertThat(Diet.mostRestrictive(List.of(Diet.VEGETARIAN, Diet.VEGAN, Diet.NORMAL))).isEqualTo(Diet.VEGAN);
        assertThat(Diet.leastRestrictive(List.of(Diet.VEGETARIAN, Diet.VEGAN))).isEqualTo(Diet.VEGETARIAN);
    }

    @Test
    void dietNamesIgnoreCase() {
        assertThat(Diet.named("vegetarian")).isEqualTo(Diet.VEGETARIAN);
        assertThat(Diet.named(" Vegan ")).isEqualTo(Diet.VEGAN);
        assertThatThrownBy(() -> Diet.named("keto"))
                .isInstanceOf(MealPlanRuleViolationException.class)
                .extracting("code").isEqualTo(MealPlanRuleViolationException.UNKNOWN_DIET);
    }
}
