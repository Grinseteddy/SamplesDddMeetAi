package org.larder.mealplanning.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.larder.mealplanning.domain.CookId;
import org.larder.mealplanning.domain.Course;
import org.larder.mealplanning.domain.CourseDraft;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanDraft;
import org.larder.mealplanning.domain.MealPlanId;
import org.larder.mealplanning.domain.MealPlanRevision;
import org.larder.mealplanning.domain.MealPlanSearch;
import org.larder.mealplanning.domain.RecipeFacts;
import org.larder.mealplanning.domain.RecipeId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Meal Planning. A cook sets up meal plans of their own and fills them step by step.
 *
 * <ul>
 *   <li>Only the owner reads, changes or deletes a meal plan, and a search finds only the caller's own
 *       plans - the contract speaks of cooks searching "their" meal plans and lists 403 for every
 *       single-plan operation; nothing grants reading somebody else's plans.</li>
 *   <li>Every recipe a course refers to must exist in the Recipe Catalog; its diet is kept on the
 *       course as a snapshot (see {@link Course}).</li>
 * </ul>
 */
@Service
public class MealPlanService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "mealplanningTransactionManager";

    private final MealPlanRepository mealPlans;
    private final RecipeCatalog recipeCatalog;

    public MealPlanService(MealPlanRepository mealPlans, RecipeCatalog recipeCatalog) {
        this.mealPlans = mealPlans;
        this.recipeCatalog = recipeCatalog;
    }

    @Transactional(transactionManager = MealPlanService.TRANSACTIONS)
    public MealPlan setUp(CookId caller, SetUpMealPlan command) {
        MealPlan mealPlan = MealPlan.setUp(caller, new MealPlanDraft(command.occasion(), command.servings(),
                command.meal(), command.howToServe(), resolve(command.courses())));
        mealPlans.save(mealPlan);
        return mealPlan;
    }

    /** Unknown plans are 404 before ownership (403) and before the courses' recipes are checked (400). */
    @Transactional(transactionManager = MealPlanService.TRANSACTIONS)
    public MealPlan change(CookId caller, MealPlanId id, ChangeMealPlan command) {
        MealPlan mealPlan = ownMealPlan(caller, id);
        mealPlan.revise(new MealPlanRevision(command.occasion(), command.servings(), command.meal(),
                command.howToServe(), command.removeHowToServe(), resolve(command.courses())));
        mealPlans.save(mealPlan);
        return mealPlan;
    }

    @Transactional(transactionManager = MealPlanService.TRANSACTIONS)
    public void delete(CookId caller, MealPlanId id) {
        ownMealPlan(caller, id);
        mealPlans.delete(id);
    }

    public MealPlan mealPlan(CookId caller, MealPlanId id) {
        return ownMealPlan(caller, id);
    }

    public List<MealPlan> search(CookId caller, MealPlanSearch search) {
        return mealPlans.search(caller, search);
    }

    // --- helpers -------------------------------------------------------------------------------

    private MealPlan ownMealPlan(CookId caller, MealPlanId id) {
        MealPlan mealPlan = mealPlans.findById(id)
                .orElseThrow(() -> new NotFoundException("Meal plan " + id.value() + " not found"));
        if (!mealPlan.isOwnedBy(caller)) {
            throw new NotPermittedException("Only the owner of meal plan " + id.value() + " can access it");
        }
        return mealPlan;
    }

    /** Looks every referenced recipe up once; {@code null} stays {@code null} (courses not given). */
    private List<Course> resolve(List<CourseDraft> drafts) {
        if (drafts == null) {
            return null;
        }
        Map<RecipeId, RecipeFacts> recipes = new LinkedHashMap<>();
        for (CourseDraft draft : drafts) {
            if (!recipes.containsKey(draft.recipe())) {
                recipes.put(draft.recipe(), recipeCatalog.recipe(draft.recipe())
                        .orElseThrow(() -> new UnknownRecipeException(draft.recipe())));
            }
        }
        return drafts.stream().map(draft -> Course.of(draft, recipes.get(draft.recipe()))).toList();
    }
}
