package org.larder.mealpreparation.application;

import java.util.Optional;

import org.larder.mealpreparation.domain.MealPreparation;
import org.larder.mealpreparation.domain.PreparationId;

/** Port: the meal preparations on Larder. */
public interface MealPreparationRepository {

    /** Stores a new preparation with its snapshot of steps, or the new current step of a stored one. */
    void save(MealPreparation preparation);

    Optional<MealPreparation> findById(PreparationId id);

    /**
     * Like {@link #findById}, but holds the preparation until the surrounding transaction ends, so
     * two requests moving from the same step cannot both succeed.
     */
    Optional<MealPreparation> findByIdForMove(PreparationId id);
}
