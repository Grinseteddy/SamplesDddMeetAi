package org.larder.recipecatalog.adapter.in.web;

import static org.larder.recipecatalog.adapter.in.web.RecipeRequests.caller;

import java.util.UUID;

import org.larder.platform.web.Links;
import org.larder.recipecatalog.adapter.in.web.api.MealsApi;
import org.larder.recipecatalog.adapter.in.web.model.MealAssignment;
import org.larder.recipecatalog.adapter.in.web.model.MealLink;
import org.larder.recipecatalog.application.RecipeService;
import org.larder.recipecatalog.domain.RecipeId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/recipe-catalog.openapi.yaml}, tag Meals. */
@RestController("recipecatalogMealsController")
@RequestMapping("/recipe-catalog")
class MealsController implements MealsApi {

    private final RecipeService service;

    MealsController(RecipeService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<MealAssignment> getRecipeMeal(UUID recipeId, String version) {
        return ResponseEntity.ok(RecipeMapper.toMealAssignment(service.recipe(new RecipeId(recipeId))));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<MealLink> updateRecipeMeal(UUID recipeId, String version, MealAssignment request) {
        service.assignMeal(caller(), new RecipeId(recipeId), RecipeRequests.toDomain(request.getMeal()));
        return ResponseEntity.ok(new MealLink(Links.current()));
    }
}
