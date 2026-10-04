package org.larder.mealpreparation.adapter.in.web;

import static org.larder.mealpreparation.adapter.in.web.MealPreparationMapper.caller;

import java.util.UUID;

import org.larder.mealpreparation.adapter.in.web.api.MealPreparationsApi;
import org.larder.mealpreparation.adapter.in.web.model.MealPreparation;
import org.larder.mealpreparation.adapter.in.web.model.MealPreparationCreate;
import org.larder.mealpreparation.adapter.in.web.model.MealPreparationLink;
import org.larder.mealpreparation.application.MealPreparationService;
import org.larder.mealpreparation.domain.PreparationId;
import org.larder.mealpreparation.domain.RecipeId;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/meal-preparation.openapi.yaml}, tag Meal Preparations. */
@RestController("mealpreparationMealPreparationsController")
@RequestMapping("/meal-preparation")
class MealPreparationsController implements MealPreparationsApi {

    private final MealPreparationService service;

    MealPreparationsController(MealPreparationService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_meal-preparation:write')")
    public ResponseEntity<MealPreparationLink> createMealPreparation(String version, MealPreparationCreate request) {
        var preparation = service.start(caller(), new RecipeId(request.getRecipe()));
        var link = Links.below(preparation.id().value());
        return ResponseEntity.created(link).body(new MealPreparationLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_meal-preparation:read', 'SCOPE_meal-preparation:write')")
    public ResponseEntity<MealPreparation> getMealPreparationById(UUID preparationId, String version) {
        return ResponseEntity.ok(MealPreparationMapper.toApi(service.preparation(caller(), new PreparationId(preparationId))));
    }
}
