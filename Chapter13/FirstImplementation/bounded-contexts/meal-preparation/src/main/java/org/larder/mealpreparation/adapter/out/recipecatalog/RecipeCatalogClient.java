package org.larder.mealpreparation.adapter.out.recipecatalog;

import java.util.List;
import java.util.Optional;

import org.larder.mealpreparation.adapter.out.recipecatalog.client.ApiClient;
import org.larder.mealpreparation.adapter.out.recipecatalog.client.api.HowToStepsApi;
import org.larder.mealpreparation.application.NotPermittedException;
import org.larder.mealpreparation.application.RecipeCatalog;
import org.larder.mealpreparation.application.RecipeCatalogUnavailableException;
import org.larder.mealpreparation.domain.HowToStep;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.RecipeId;
import org.larder.mealpreparation.domain.RecipeSteps;
import org.larder.platform.security.BearerTokenRelay;
import org.larder.platform.web.UpstreamClients;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Anticorruption layer to Recipe Catalog: calls {@code GET /recipes/{recipeId}/how-to-steps} through
 * the client generated from {@code contracts/openapi/recipe-catalog.openapi.yaml} with the calling
 * cook's own token, and keeps only what a meal preparation needs - identifier and sequence number of
 * each step. Nothing of the generated model leaves this package.
 */
class RecipeCatalogClient implements RecipeCatalog {

    /** Version of the Recipe Catalog contract this client is generated from. */
    static final String VERSION = "1.0.0";

    private final HowToStepsApi howToSteps;

    RecipeCatalogClient(HowToStepsApi howToSteps) {
        this.howToSteps = howToSteps;
    }

    /** A client for Recipe Catalog at {@code baseUrl} that relays the caller's bearer token. */
    static RecipeCatalogClient create(RestClient.Builder restClientBuilder, String baseUrl) {
        var apiClient = new ApiClient(UpstreamClients.restClient(restClientBuilder, baseUrl, new BearerTokenRelay()));
        // The generated client builds absolute URLs from its own base path (the contract's server URL).
        apiClient.setBasePath(baseUrl);
        return new RecipeCatalogClient(new HowToStepsApi(apiClient));
    }

    @Override
    public Optional<RecipeSteps> stepsOf(RecipeId recipe) {
        List<org.larder.mealpreparation.adapter.out.recipecatalog.client.model.HowToStep> steps;
        try {
            steps = howToSteps.getRecipeHowToSteps(recipe.value(), VERSION);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException.Forbidden e) {
            throw new NotPermittedException("Recipe Catalog does not let the cook read recipe " + recipe.value());
        } catch (RestClientException e) {
            throw new RecipeCatalogUnavailableException("Recipe Catalog cannot deliver the steps of recipe "
                    + recipe.value() + ": " + e.getMessage(), e);
        }
        return Optional.of(translate(recipe, steps));
    }

    private static RecipeSteps translate(RecipeId recipe,
                                         List<org.larder.mealpreparation.adapter.out.recipecatalog.client.model.HowToStep> steps) {
        try {
            return new RecipeSteps(steps == null ? List.of() : steps.stream()
                    .map(step -> new HowToStep(new HowToStepId(step.getHowToStepId()), step.getSequenceNumber()))
                    .toList());
        } catch (RuntimeException e) {
            throw new RecipeCatalogUnavailableException("Recipe Catalog delivered unusable steps for recipe "
                    + recipe.value() + ": " + e.getMessage(), e);
        }
    }
}
