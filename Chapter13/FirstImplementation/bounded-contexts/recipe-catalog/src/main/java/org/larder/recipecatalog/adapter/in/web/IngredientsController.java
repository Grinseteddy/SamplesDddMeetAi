package org.larder.recipecatalog.adapter.in.web;

import static org.larder.recipecatalog.adapter.in.web.RecipeRequests.caller;

import java.util.List;
import java.util.UUID;

import org.larder.platform.web.Links;
import org.larder.recipecatalog.adapter.in.web.api.IngredientsApi;
import org.larder.recipecatalog.adapter.in.web.model.Ingredient;
import org.larder.recipecatalog.adapter.in.web.model.IngredientCreate;
import org.larder.recipecatalog.adapter.in.web.model.IngredientLink;
import org.larder.recipecatalog.adapter.in.web.model.IngredientUpdate;
import org.larder.recipecatalog.application.RecipeService;
import org.larder.recipecatalog.domain.IngredientId;
import org.larder.recipecatalog.domain.RecipeId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/recipe-catalog.openapi.yaml}, tag Ingredients. */
@RestController("recipecatalogIngredientsController")
@RequestMapping("/recipe-catalog")
class IngredientsController implements IngredientsApi {

    private final RecipeService service;

    IngredientsController(RecipeService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<List<Ingredient>> getRecipeIngredients(UUID recipeId, String version) {
        return ResponseEntity.ok(service.recipe(new RecipeId(recipeId)).ingredients().stream()
                .map(RecipeMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<IngredientLink> addRecipeIngredient(UUID recipeId, String version, IngredientCreate request) {
        var ingredient = service.addIngredient(caller(), new RecipeId(recipeId), RecipeRequests.toDraft(request));
        var link = Links.below(ingredient.id().value());
        return ResponseEntity.created(link).body(new IngredientLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<Ingredient> getRecipeIngredientById(UUID recipeId, UUID ingredientId, String version) {
        return ResponseEntity.ok(RecipeMapper.toApi(service.ingredient(new RecipeId(recipeId), new IngredientId(ingredientId))));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<IngredientLink> updateRecipeIngredient(UUID recipeId, UUID ingredientId, String version,
                                                                 IngredientUpdate request) {
        service.changeIngredient(caller(), new RecipeId(recipeId), new IngredientId(ingredientId),
                request.getName(), request.getValue(), RecipeRequests.toDomain(request.getUnit()));
        return ResponseEntity.ok(new IngredientLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<Void> deleteRecipeIngredient(UUID recipeId, UUID ingredientId, String version) {
        service.removeIngredient(caller(), new RecipeId(recipeId), new IngredientId(ingredientId));
        return ResponseEntity.noContent().build();
    }
}
