package org.larder.mealpreparation.application;

import java.util.Optional;

import org.larder.mealpreparation.domain.CookId;
import org.larder.mealpreparation.domain.HowToStep;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.MealPreparation;
import org.larder.mealpreparation.domain.PreparationId;
import org.larder.mealpreparation.domain.RecipeId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Meal Preparation. A meal preparation belongs to the cook who started it: only that
 * cook reads it, reads its steps and moves its current step; any other cook gets
 * {@link NotPermittedException}. The contract offers no way to share a preparation.
 */
@Service
public class MealPreparationService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "mealpreparationTransactionManager";

    private final MealPreparationRepository preparations;
    private final RecipeCatalog recipeCatalog;

    public MealPreparationService(MealPreparationRepository preparations, RecipeCatalog recipeCatalog) {
        this.preparations = preparations;
        this.recipeCatalog = recipeCatalog;
    }

    /**
     * Starts preparing {@code recipe} with a snapshot of its steps. Not one transaction on purpose:
     * the call to Recipe Catalog must not hold a database connection; storing is atomic on its own.
     */
    public MealPreparation start(CookId caller, RecipeId recipe) {
        var steps = recipeCatalog.stepsOf(recipe).orElseThrow(() -> new UnknownRecipeException(recipe));
        MealPreparation preparation = MealPreparation.start(caller, recipe, steps);
        preparations.save(preparation);
        return preparation;
    }

    public MealPreparation preparation(CookId caller, PreparationId id) {
        return requireStartedBy(caller, preparations.findById(id), id);
    }

    public HowToStep howToStep(CookId caller, PreparationId id, HowToStepId stepId) {
        return preparation(caller, id).step(stepId).orElseThrow(() -> new NotFoundException(
                "How-to step " + stepId.value() + " is not a step of meal preparation " + id.value()));
    }

    @Transactional(transactionManager = MealPreparationService.TRANSACTIONS)
    public HowToStep moveToNext(CookId caller, PreparationId id, HowToStepId from) {
        MealPreparation preparation = requireStartedBy(caller, preparations.findByIdForMove(id), id);
        HowToStep now = preparation.moveToNext(from);
        preparations.save(preparation);
        return now;
    }

    @Transactional(transactionManager = MealPreparationService.TRANSACTIONS)
    public HowToStep moveToPrevious(CookId caller, PreparationId id, HowToStepId from) {
        MealPreparation preparation = requireStartedBy(caller, preparations.findByIdForMove(id), id);
        HowToStep now = preparation.moveToPrevious(from);
        preparations.save(preparation);
        return now;
    }

    private static MealPreparation requireStartedBy(CookId caller, Optional<MealPreparation> found,
                                                    PreparationId id) {
        MealPreparation preparation = found.orElseThrow(
                () -> new NotFoundException("Meal preparation " + id.value() + " not found"));
        if (!preparation.isStartedBy(caller)) {
            throw new NotPermittedException("Only the cook who started meal preparation " + id.value() + " may use it");
        }
        return preparation;
    }
}
