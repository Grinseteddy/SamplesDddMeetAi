package org.larder.mealplanning.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.mealplanning.TestData.COOK;
import static org.larder.mealplanning.TestData.HOW_TO_SERVE;
import static org.larder.mealplanning.TestData.OCCASION;
import static org.larder.mealplanning.TestData.OTHER_COOK;
import static org.larder.mealplanning.TestData.ROAST;
import static org.larder.mealplanning.TestData.ROAST_FACTS;
import static org.larder.mealplanning.TestData.SCONES;
import static org.larder.mealplanning.TestData.SCONES_FACTS;
import static org.larder.mealplanning.TestData.SOUP;
import static org.larder.mealplanning.TestData.SOUP_FACTS;
import static org.larder.mealplanning.TestData.scones;
import static org.larder.mealplanning.TestData.soup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.mealplanning.domain.CookId;
import org.larder.mealplanning.domain.CourseDraft;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.Meal;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanId;
import org.larder.mealplanning.domain.MealPlanSearch;
import org.larder.mealplanning.domain.RecipeFacts;
import org.larder.mealplanning.domain.RecipeId;

class MealPlanServiceTest {

    private final InMemoryMealPlans mealPlans = new InMemoryMealPlans();
    private final FakeRecipeCatalog catalog = new FakeRecipeCatalog(SCONES_FACTS, SOUP_FACTS, ROAST_FACTS);
    private final MealPlanService service = new MealPlanService(mealPlans, catalog);

    private static SetUpMealPlan dinnerWith(CourseDraft... courses) {
        return new SetUpMealPlan(OCCASION, 6, Meal.DINNER, HOW_TO_SERVE, List.of(courses));
    }

    @Test
    void aCookSetsUpAnEmptyMealPlanOfTheirOwn() {
        MealPlan plan = service.setUp(COOK, SetUpMealPlan.empty());

        assertThat(mealPlans.findById(plan.id())).isPresent();
        assertThat(plan.isOwnedBy(COOK)).isTrue();
        assertThat(catalog.lookups).isEmpty();
    }

    @Test
    void coursesKeepTheDietOfTheirRecipe() {
        MealPlan plan = service.setUp(COOK, dinnerWith(new CourseDraft(1, SOUP), new CourseDraft(2, SCONES)));

        assertThat(plan.courses()).containsExactly(soup(1), scones(2));
        assertThat(plan.diet()).contains(Diet.VEGETARIAN);
    }

    @Test
    void everyReferencedRecipeIsLookedUpOnce() {
        service.setUp(COOK, dinnerWith(new CourseDraft(1, SOUP), new CourseDraft(2, SOUP), new CourseDraft(3, ROAST)));

        assertThat(catalog.lookups).containsExactly(SOUP, ROAST);
    }

    @Test
    void aCourseMustReferToAnExistingRecipe() {
        RecipeId unknown = new RecipeId(UUID.randomUUID());

        assertThatThrownBy(() -> service.setUp(COOK, dinnerWith(new CourseDraft(1, unknown))))
                .isInstanceOf(UnknownRecipeException.class);
        assertThat(mealPlans.store).isEmpty();
    }

    @Test
    void theOwnerFillsThePlanStepByStep() {
        MealPlan plan = service.setUp(COOK, SetUpMealPlan.empty());

        service.change(COOK, plan.id(), new ChangeMealPlan(OCCASION, null, null, null, false, null));
        service.change(COOK, plan.id(), new ChangeMealPlan(null, 6, Meal.DINNER, HOW_TO_SERVE, false,
                List.of(new CourseDraft(1, SOUP))));
        service.change(COOK, plan.id(), new ChangeMealPlan(null, null, null, null, true, null));

        MealPlan stored = service.mealPlan(COOK, plan.id());
        assertThat(stored.occasion()).contains(OCCASION);
        assertThat(stored.servings()).contains(6);
        assertThat(stored.courses()).containsExactly(soup(1));
        assertThat(stored.howToServe()).isEmpty();
    }

    @Test
    void onlyTheOwnerReadsChangesOrDeletesAPlan() {
        MealPlan plan = service.setUp(COOK, SetUpMealPlan.empty());

        assertThatThrownBy(() -> service.mealPlan(OTHER_COOK, plan.id())).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.change(OTHER_COOK, plan.id(), new ChangeMealPlan(OCCASION, null, null, null, false, null)))
                .isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.delete(OTHER_COOK, plan.id())).isInstanceOf(NotPermittedException.class);

        service.delete(COOK, plan.id());
        assertThat(mealPlans.findById(plan.id())).isEmpty();
    }

    @Test
    void ownershipIsCheckedBeforeTheRecipesAreLookedUp() {
        MealPlan plan = service.setUp(COOK, SetUpMealPlan.empty());

        assertThatThrownBy(() -> service.change(OTHER_COOK, plan.id(),
                new ChangeMealPlan(null, null, null, null, false, List.of(new CourseDraft(1, SOUP)))))
                .isInstanceOf(NotPermittedException.class);
        assertThat(catalog.lookups).isEmpty();
    }

    @Test
    void unknownPlansAreNotFound() {
        MealPlanId unknown = MealPlanId.newId();

        assertThatThrownBy(() -> service.mealPlan(COOK, unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.change(COOK, unknown, ChangeMealPlan.none())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(COOK, unknown)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void aSearchFindsOnlyTheCallersOwnPlans() {
        MealPlan mine = service.setUp(COOK, dinnerWith(new CourseDraft(1, SOUP)));
        service.setUp(OTHER_COOK, dinnerWith(new CourseDraft(1, SOUP)));

        assertThat(service.search(COOK, MealPlanSearch.of(List.of(Diet.VEGETARIAN), OCCASION)))
                .extracting(MealPlan::id).containsExactly(mine.id());
    }

    @Test
    void anUnavailableCatalogStopsTheChange() {
        catalog.failing = true;

        assertThatThrownBy(() -> service.setUp(COOK, dinnerWith(new CourseDraft(1, SOUP))))
                .isInstanceOf(RecipeCatalogUnavailableException.class);
    }

    static class InMemoryMealPlans implements MealPlanRepository {
        final Map<MealPlanId, MealPlan> store = new HashMap<>();

        @Override
        public void save(MealPlan mealPlan) {
            store.put(mealPlan.id(), mealPlan);
        }

        @Override
        public Optional<MealPlan> findById(MealPlanId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<MealPlan> search(CookId owner, MealPlanSearch search) {
            return store.values().stream().filter(plan -> plan.isOwnedBy(owner)).filter(search::isSatisfiedBy).toList();
        }

        @Override
        public void delete(MealPlanId id) {
            store.remove(id);
        }
    }

    static class FakeRecipeCatalog implements RecipeCatalog {
        final Map<RecipeId, RecipeFacts> recipes = new HashMap<>();
        final List<RecipeId> lookups = new ArrayList<>();
        boolean failing;

        FakeRecipeCatalog(RecipeFacts... known) {
            for (RecipeFacts recipe : known) {
                recipes.put(recipe.id(), recipe);
            }
        }

        @Override
        public Optional<RecipeFacts> recipe(RecipeId id) {
            if (failing) {
                throw new RecipeCatalogUnavailableException("down", null);
            }
            lookups.add(id);
            return Optional.ofNullable(recipes.get(id));
        }
    }
}
