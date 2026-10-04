package org.larder.mealplanning.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.mealplanning.TestData.COOK;
import static org.larder.mealplanning.TestData.HOW_TO_SERVE;
import static org.larder.mealplanning.TestData.OCCASION;
import static org.larder.mealplanning.TestData.OTHER_COOK;
import static org.larder.mealplanning.TestData.SCONES;
import static org.larder.mealplanning.TestData.SCONES_FACTS;
import static org.larder.mealplanning.TestData.SOUP;
import static org.larder.mealplanning.TestData.dinner;
import static org.larder.mealplanning.TestData.planOf;
import static org.larder.mealplanning.TestData.roast;
import static org.larder.mealplanning.TestData.scones;
import static org.larder.mealplanning.TestData.soup;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class MealPlanTest {

    @Test
    void aMealPlanCanBeSetUpEmptyAndBelongsToItsCook() {
        MealPlan plan = MealPlan.setUp(COOK, MealPlanDraft.empty());

        assertThat(plan.isOwnedBy(COOK)).isTrue();
        assertThat(plan.isOwnedBy(OTHER_COOK)).isFalse();
        assertThat(plan.occasion()).isEmpty();
        assertThat(plan.servings()).isEmpty();
        assertThat(plan.meal()).isEmpty();
        assertThat(plan.howToServe()).isEmpty();
        assertThat(plan.courses()).isEmpty();
        assertThat(plan.diet()).isEmpty();
    }

    @Test
    void anEmptyPlanIsFilledStepByStep() {
        MealPlan plan = MealPlan.setUp(COOK, MealPlanDraft.empty());

        plan.revise(MealPlanRevision.none().withOccasion(OCCASION));
        plan.revise(MealPlanRevision.none().withServings(6));
        plan.revise(MealPlanRevision.none().withCourses(List.of(soup(1), scones(2))));

        assertThat(plan.occasion()).contains(OCCASION);
        assertThat(plan.servings()).contains(6);
        assertThat(plan.courses()).containsExactly(soup(1), scones(2));
    }

    @Test
    void givenCoursesReplaceTheExistingOnesAsAWhole() {
        MealPlan plan = dinner();

        plan.revise(MealPlanRevision.none().withCourses(List.of(roast(1))));

        assertThat(plan.courses()).containsExactly(roast(1));
        assertThat(plan.occasion()).contains(OCCASION);
    }

    @Test
    void howToServeCanBeRemoved() {
        MealPlan plan = dinner();
        assertThat(plan.howToServe()).contains(HOW_TO_SERVE);

        plan.revise(MealPlanRevision.none().withoutHowToServe());

        assertThat(plan.howToServe()).isEmpty();
    }

    @Test
    void coursesWithTheSameStepAreServedInParallel() {
        MealPlan plan = MealPlan.setUp(COOK, new MealPlanDraft(null, null, null, null, List.of(soup(1), scones(1))));

        assertThat(plan.courses()).extracting(Course::step).containsExactly(1, 1);
    }

    @Test
    void aPlanHasOneToTenCourses() {
        assertThatThrownBy(() -> MealPlan.setUp(COOK, new MealPlanDraft(null, null, null, null, List.of())))
                .isInstanceOf(MealPlanRuleViolationException.class);
        assertThatThrownBy(() -> dinner().revise(MealPlanRevision.none().withCourses(Collections.nCopies(11, soup(1)))))
                .isInstanceOf(MealPlanRuleViolationException.class);
        MealPlan ten = MealPlan.setUp(COOK, new MealPlanDraft(null, null, null, null, Collections.nCopies(10, soup(1))));
        assertThat(ten.courses()).hasSize(10);
    }

    @Test
    void stepsStartAtOne() {
        assertThatThrownBy(() -> new CourseDraft(0, SOUP)).isInstanceOf(MealPlanRuleViolationException.class);
        assertThatThrownBy(() -> new Course(0, SOUP, Diet.VEGAN)).isInstanceOf(MealPlanRuleViolationException.class);
    }

    @Test
    void aCourseTakesTheDietOfItsRecipe() {
        assertThat(Course.of(new CourseDraft(2, SCONES), SCONES_FACTS)).isEqualTo(scones(2));
        assertThatThrownBy(() -> Course.of(new CourseDraft(2, SOUP), SCONES_FACTS)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRejectedChangeLeavesThePlanAsItWas() {
        MealPlan plan = dinner();

        assertThatThrownBy(() -> plan.revise(MealPlanRevision.none().withOccasion("new occasion").withServings(0)))
                .isInstanceOf(MealPlanRuleViolationException.class);

        assertThat(plan.occasion()).contains(OCCASION);
        assertThat(plan.servings()).contains(6);
    }

    @Test
    void occasionAndServingInstructionsMustNotBeBlankOrTooLong() {
        assertThatThrownBy(() -> dinner().revise(MealPlanRevision.none().withOccasion(" ")))
                .isInstanceOf(MealPlanRuleViolationException.class);
        assertThatThrownBy(() -> dinner().revise(MealPlanRevision.none().withOccasion("x".repeat(201))))
                .isInstanceOf(MealPlanRuleViolationException.class);
        assertThatThrownBy(() -> dinner().revise(MealPlanRevision.none().withHowToServe("x".repeat(2001))))
                .isInstanceOf(MealPlanRuleViolationException.class);
    }

    @Test
    void theDietOfAPlanIsTheLeastRestrictiveDietOfItsCourses() {
        assertThat(dinner().diet()).contains(Diet.VEGETARIAN);
        assertThat(planOf(COOK, OCCASION, soup(1)).diet()).contains(Diet.VEGAN);
        assertThat(planOf(COOK, OCCASION, soup(1), roast(2)).diet()).contains(Diet.NORMAL);
    }

    @Test
    void aPlanSuitsADietWhenEveryCourseIsAtLeastAsRestrictive() {
        MealPlan vegan = planOf(COOK, OCCASION, soup(1));
        MealPlan vegetarian = dinner();
        MealPlan normal = planOf(COOK, OCCASION, soup(1), roast(2));

        assertThat(vegan.isSuitableFor(Diet.VEGETARIAN)).isTrue();
        assertThat(vegan.isSuitableFor(Diet.VEGAN)).isTrue();
        assertThat(vegetarian.isSuitableFor(Diet.VEGETARIAN)).isTrue();
        assertThat(vegetarian.isSuitableFor(Diet.VEGAN)).isFalse();
        assertThat(normal.isSuitableFor(Diet.NORMAL)).isTrue();
        assertThat(normal.isSuitableFor(Diet.VEGETARIAN)).isFalse();
        assertThat(MealPlan.setUp(COOK, MealPlanDraft.empty()).isSuitableFor(Diet.NORMAL)).isFalse();
    }

    @Test
    void theOccasionMatchesIgnoringCase() {
        assertThat(dinner().isFor("Parents In Law Visiting")).isTrue();
        assertThat(dinner().isFor("parents in law")).isFalse();
        assertThat(MealPlan.setUp(COOK, MealPlanDraft.empty()).isFor(OCCASION)).isFalse();
    }

    @Test
    void aSearchUsesTheMostRestrictiveDiet() {
        MealPlanSearch search = MealPlanSearch.of(List.of(Diet.VEGETARIAN, Diet.VEGAN), null);

        assertThat(search.diet()).isEqualTo(Diet.VEGAN);
        assertThat(search.isSatisfiedBy(planOf(COOK, OCCASION, soup(1)))).isTrue();
        assertThat(search.isSatisfiedBy(dinner())).isFalse();
    }

    @Test
    void aSearchWithoutCriteriaFindsEveryPlanEvenEmptyOnes() {
        assertThat(MealPlanSearch.of(List.of(), null).isSatisfiedBy(MealPlan.setUp(COOK, MealPlanDraft.empty()))).isTrue();
        assertThat(MealPlanSearch.all().isSatisfiedBy(dinner())).isTrue();
        assertThat(new MealPlanSearch(null, "PARENTS IN LAW VISITING").isSatisfiedBy(dinner())).isTrue();
    }
}
