package org.larder.mealplanning.adapter.in.web;

import static org.larder.mealplanning.adapter.in.web.MealPlanRequests.caller;

import java.util.List;
import java.util.UUID;

import org.larder.mealplanning.adapter.in.web.api.MealPlansApi;
import org.larder.mealplanning.adapter.in.web.model.MealPlan;
import org.larder.mealplanning.adapter.in.web.model.MealPlanCreate;
import org.larder.mealplanning.adapter.in.web.model.MealPlanLink;
import org.larder.mealplanning.adapter.in.web.model.MealPlanUpdate;
import org.larder.mealplanning.application.MealPlanService;
import org.larder.mealplanning.domain.MealPlanId;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/meal-planning.openapi.yaml}, tag Meal Plans. */
@RestController("mealplanningMealPlansController")
@RequestMapping("/meal-planning")
class MealPlansController implements MealPlansApi {

    private final MealPlanService service;

    MealPlansController(MealPlanService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_meal-plan:read', 'SCOPE_meal-plan:write')")
    public ResponseEntity<List<MealPlan>> searchMealPlans(String version, List<String> diet, String occasion) {
        var search = MealPlanRequests.toSearch(diet, occasion);
        return ResponseEntity.ok(service.search(caller(), search).stream().map(MealPlanMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_meal-plan:write')")
    public ResponseEntity<MealPlanLink> createMealPlan(String version, MealPlanCreate request) {
        var mealPlan = service.setUp(caller(), MealPlanRequests.toCommand(request));
        var link = Links.below(mealPlan.id().value());
        return ResponseEntity.created(link).body(new MealPlanLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_meal-plan:read', 'SCOPE_meal-plan:write')")
    public ResponseEntity<MealPlan> getMealPlanById(UUID mealPlanId, String version) {
        return ResponseEntity.ok(MealPlanMapper.toApi(service.mealPlan(caller(), new MealPlanId(mealPlanId))));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_meal-plan:write')")
    public ResponseEntity<MealPlanLink> updateMealPlan(UUID mealPlanId, String version, MealPlanUpdate request) {
        service.change(caller(), new MealPlanId(mealPlanId),
                MealPlanRequests.toCommand(request, MealPlanRequestBodyAdvice.givenProperties()));
        return ResponseEntity.ok(new MealPlanLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_meal-plan:write')")
    public ResponseEntity<Void> deleteMealPlan(UUID mealPlanId, String version) {
        service.delete(caller(), new MealPlanId(mealPlanId));
        return ResponseEntity.noContent().build();
    }
}
