package org.larder.recipecatalog.adapter.in.web;

import static org.larder.recipecatalog.adapter.in.web.RecipeRequests.caller;

import java.util.List;
import java.util.UUID;

import org.larder.platform.web.Links;
import org.larder.recipecatalog.adapter.in.web.api.HowToStepsApi;
import org.larder.recipecatalog.adapter.in.web.model.HowToStep;
import org.larder.recipecatalog.adapter.in.web.model.HowToStepCreate;
import org.larder.recipecatalog.adapter.in.web.model.HowToStepLink;
import org.larder.recipecatalog.adapter.in.web.model.HowToStepUpdate;
import org.larder.recipecatalog.application.RecipeService;
import org.larder.recipecatalog.domain.HowToStepId;
import org.larder.recipecatalog.domain.RecipeId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST adapter for {@code contracts/openapi/recipe-catalog.openapi.yaml}, tag How-To Steps. */
@RestController("recipecatalogHowToStepsController")
@RequestMapping("/recipe-catalog")
class HowToStepsController implements HowToStepsApi {

    private final RecipeService service;

    HowToStepsController(RecipeService service) {
        this.service = service;
    }

    /** Ordered by sequence number. */
    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<List<HowToStep>> getRecipeHowToSteps(UUID recipeId, String version) {
        return ResponseEntity.ok(service.recipe(new RecipeId(recipeId)).howToSteps().stream()
                .map(RecipeMapper::toApi).toList());
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<HowToStepLink> addRecipeHowToStep(UUID recipeId, String version, HowToStepCreate request) {
        var step = service.addHowToStep(caller(), new RecipeId(recipeId), RecipeRequests.toDraft(request));
        var link = Links.below(step.id().value());
        return ResponseEntity.created(link).body(new HowToStepLink(link));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:read', 'SCOPE_recipe:write', 'SCOPE_recipe:admin')")
    public ResponseEntity<HowToStep> getRecipeHowToStepById(UUID recipeId, UUID howToStepId, String version) {
        return ResponseEntity.ok(RecipeMapper.toApi(service.howToStep(new RecipeId(recipeId), new HowToStepId(howToStepId))));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<HowToStepLink> updateRecipeHowToStep(UUID recipeId, UUID howToStepId, String version,
                                                               HowToStepUpdate request) {
        service.changeHowToStep(caller(), new RecipeId(recipeId), new HowToStepId(howToStepId),
                request.getSequenceNumber(), request.getDescription(), request.getIllustration());
        return ResponseEntity.ok(new HowToStepLink(Links.current()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_recipe:write')")
    public ResponseEntity<Void> deleteRecipeHowToStep(UUID recipeId, UUID howToStepId, String version) {
        service.removeHowToStep(caller(), new RecipeId(recipeId), new HowToStepId(howToStepId));
        return ResponseEntity.noContent().build();
    }
}
