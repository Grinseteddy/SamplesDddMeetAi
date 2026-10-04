package org.larder.recipecatalog.adapter.in.web;

import static org.larder.recipecatalog.adapter.in.web.RecipeRequests.caller;

import java.util.List;
import java.util.UUID;

import org.larder.platform.web.Links;
import org.larder.recipecatalog.adapter.in.web.api.RecipesApi;
import org.larder.recipecatalog.adapter.in.web.model.Diet;
import org.larder.recipecatalog.adapter.in.web.model.Meal;
import org.larder.recipecatalog.adapter.in.web.model.Recipe;
import org.larder.recipecatalog.adapter.in.web.model.RecipeCreate;
import org.larder.recipecatalog.adapter.in.web.model.RecipeLink;
import org.larder.recipecatalog.adapter.in.web.model.RecipeUpdate;
import org.larder.recipecatalog.application.RecipeService;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.RecipeSearch;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/recipe-catalog.openapi.yaml}, tag Recipes. */
@RestController("recipecatalogRecipesController")
@RequestMapping("/recipe-catalog")
class RecipesController implements RecipesApi {

    private final RecipeService service;

    RecipesController(RecipeService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<List<Recipe>> searchRecipes(String version, Diet diet, Meal meal, List<String> ingredients) {
        var search = new RecipeSearch(RecipeRequests.toDomain(meal), RecipeRequests.toDomain(diet), ingredients);
        return ResponseEntity.ok(service.search(search).stream().map(RecipeMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<RecipeLink> createRecipe(String version, RecipeCreate request) {
        var recipe = service.create(caller(), RecipeRequests.toDraft(request));
        var link = Links.below(recipe.id().value());
        return ResponseEntity.created(link).body(new RecipeLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<Recipe> getRecipeById(UUID recipeId, String version) {
        return ResponseEntity.ok(RecipeMapper.toApi(service.recipe(new RecipeId(recipeId))));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<RecipeLink> updateRecipe(UUID recipeId, String version, RecipeUpdate request) {
        service.revise(caller(), new RecipeId(recipeId), RecipeRequests.toRevision(request));
        return ResponseEntity.ok(new RecipeLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<Void> deleteRecipe(UUID recipeId, String version) {
        service.delete(caller(), new RecipeId(recipeId));
        return ResponseEntity.noContent().build();
    }
}
