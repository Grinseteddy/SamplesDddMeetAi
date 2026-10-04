package org.larder.mealpreparation.adapter.in.web;

import org.larder.mealpreparation.adapter.in.web.model.HowToStep;
import org.larder.mealpreparation.adapter.in.web.model.MealPreparation;
import org.larder.mealpreparation.domain.CookId;
import org.larder.platform.security.CurrentCook;

/** Translates the domain model into the contract's model - and only in this direction. */
final class MealPreparationMapper {

    private MealPreparationMapper() {
    }

    static MealPreparation toApi(org.larder.mealpreparation.domain.MealPreparation preparation) {
        return new MealPreparation(preparation.id().value(), preparation.recipe().value(), toApi(preparation.currentStep()));
    }

    static HowToStep toApi(org.larder.mealpreparation.domain.HowToStep step) {
        return new HowToStep(step.id().value(), step.sequenceNumber());
    }

    /** The calling cook, from the {@code cookId} claim of the access token. */
    static CookId caller() {
        return new CookId(CurrentCook.require().value());
    }
}
