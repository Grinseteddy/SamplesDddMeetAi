package org.larder.mealplanning.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.mealplanning.TestData.COOK;
import static org.larder.mealplanning.TestData.HOW_TO_SERVE;
import static org.larder.mealplanning.TestData.OCCASION;
import static org.larder.mealplanning.TestData.OTHER_COOK;
import static org.larder.mealplanning.TestData.dinner;
import static org.larder.mealplanning.TestData.planOf;
import static org.larder.mealplanning.TestData.roast;
import static org.larder.mealplanning.TestData.scones;
import static org.larder.mealplanning.TestData.soup;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.Meal;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanDraft;
import org.larder.mealplanning.domain.MealPlanRevision;
import org.larder.mealplanning.domain.MealPlanSearch;
import org.larder.platform.test.TestDatabase;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcMealPlanRepositoryTest {

    private JdbcMealPlanRepository mealPlans;

    @BeforeEach
    void freshSchema() {
        mealPlans = new JdbcMealPlanRepository(TestDatabase.forSchema("mealplanning").jdbcClient());
    }

    @Test
    void storesAMealPlanWithItsCourses() {
        mealPlans.save(dinner());

        MealPlan stored = mealPlans.findById(dinner().id()).orElseThrow();
        assertThat(stored.owner()).isEqualTo(COOK);
        assertThat(stored.occasion()).contains(OCCASION);
        assertThat(stored.servings()).contains(6);
        assertThat(stored.meal()).contains(Meal.DINNER);
        assertThat(stored.howToServe()).contains(HOW_TO_SERVE);
        assertThat(stored.courses()).containsExactly(soup(1), scones(2));
    }

    @Test
    void storesAnEmptyMealPlan() {
        MealPlan empty = MealPlan.setUp(COOK, MealPlanDraft.empty());
        mealPlans.save(empty);

        MealPlan stored = mealPlans.findById(empty.id()).orElseThrow();
        assertThat(stored.occasion()).isEmpty();
        assertThat(stored.servings()).isEmpty();
        assertThat(stored.meal()).isEmpty();
        assertThat(stored.courses()).isEmpty();
    }

    @Test
    void savingReplacesTheCoursesAndRemovesServingInstructions() {
        MealPlan plan = dinner();
        mealPlans.save(plan);

        plan.revise(MealPlanRevision.none().withCourses(List.of(roast(1), roast(1))).withoutHowToServe());
        mealPlans.save(plan);

        MealPlan stored = mealPlans.findById(plan.id()).orElseThrow();
        assertThat(stored.courses()).containsExactly(roast(1), roast(1));
        assertThat(stored.howToServe()).isEmpty();
    }

    @Test
    void deletesAMealPlanWithItsCourses() {
        mealPlans.save(dinner());

        mealPlans.delete(dinner().id());

        assertThat(mealPlans.findById(dinner().id())).isEmpty();
    }

    @Test
    void searchesTheOwnersPlansByTheMostRestrictiveDietAndOccasion() {
        MealPlan vegan = planOf(COOK, OCCASION, soup(1));
        MealPlan vegetarian = planOf(COOK, OCCASION, soup(1), scones(2));
        MealPlan normal = planOf(COOK, "birthday", soup(1), roast(2));
        MealPlan empty = MealPlan.setUp(COOK, MealPlanDraft.empty());
        MealPlan somebodyElses = planOf(OTHER_COOK, OCCASION, soup(1));
        List.of(vegan, vegetarian, normal, empty, somebodyElses).forEach(mealPlans::save);

        assertThat(mealPlans.search(COOK, MealPlanSearch.all())).extracting(MealPlan::id)
                .containsExactlyInAnyOrder(vegan.id(), vegetarian.id(), normal.id(), empty.id());
        assertThat(mealPlans.search(COOK, MealPlanSearch.of(List.of(Diet.NORMAL), null))).extracting(MealPlan::id)
                .containsExactlyInAnyOrder(vegan.id(), vegetarian.id(), normal.id());
        assertThat(mealPlans.search(COOK, MealPlanSearch.of(List.of(Diet.VEGETARIAN), null))).extracting(MealPlan::id)
                .containsExactlyInAnyOrder(vegan.id(), vegetarian.id());
        assertThat(mealPlans.search(COOK, MealPlanSearch.of(List.of(Diet.VEGETARIAN, Diet.VEGAN), null)))
                .extracting(MealPlan::id).containsExactly(vegan.id());
        assertThat(mealPlans.search(COOK, new MealPlanSearch(null, "Parents In Law Visiting"))).extracting(MealPlan::id)
                .containsExactlyInAnyOrder(vegan.id(), vegetarian.id());
        assertThat(mealPlans.search(COOK, new MealPlanSearch(Diet.NORMAL, "BIRTHDAY"))).extracting(MealPlan::id)
                .containsExactly(normal.id());
        assertThat(mealPlans.search(COOK, new MealPlanSearch(null, "parents"))).isEmpty();
    }
}
