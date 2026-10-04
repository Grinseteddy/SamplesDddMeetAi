package org.larder.mealpreparation.adapter.in.web;

import static org.larder.mealpreparation.adapter.in.web.MealPreparationMapper.caller;

import java.util.UUID;

import org.larder.mealpreparation.adapter.in.web.api.HowToStepsApi;
import org.larder.mealpreparation.adapter.in.web.model.HowToStep;
import org.larder.mealpreparation.application.MealPreparationService;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.PreparationId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for {@code contracts/openapi/meal-preparation.openapi.yaml}, tag How-To Steps.
 * The steps come from the preparation's snapshot of the recipe, not from Recipe Catalog.
 */
@RestController("mealpreparationHowToStepsController")
@RequestMapping("/meal-preparation")
class HowToStepsController implements HowToStepsApi {

    private final MealPreparationService service;

    HowToStepsController(MealPreparationService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_meal-preparation:read', 'SCOPE_meal-preparation:write')")
    public ResponseEntity<HowToStep> getHowToStepById(UUID preparationId, UUID stepId, String version) {
        return ResponseEntity.ok(MealPreparationMapper.toApi(
                service.howToStep(caller(), new PreparationId(preparationId), new HowToStepId(stepId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_meal-preparation:write')")
    public ResponseEntity<HowToStep> moveToNextHowToStep(UUID preparationId, UUID stepId, String version) {
        return ResponseEntity.ok(MealPreparationMapper.toApi(
                service.moveToNext(caller(), new PreparationId(preparationId), new HowToStepId(stepId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_meal-preparation:write')")
    public ResponseEntity<HowToStep> moveToPreviousHowToStep(UUID preparationId, UUID stepId, String version) {
        return ResponseEntity.ok(MealPreparationMapper.toApi(
                service.moveToPrevious(caller(), new PreparationId(preparationId), new HowToStepId(stepId))));
    }
}
