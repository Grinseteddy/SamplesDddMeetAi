package org.larder.mealplanning.adapter.out.recipecatalog;

import org.larder.mealplanning.adapter.out.recipecatalog.client.ApiClient;
import org.larder.mealplanning.adapter.out.recipecatalog.client.api.RecipesApi;
import org.larder.platform.security.BearerTokenRelay;
import org.larder.platform.web.UpstreamClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/**
 * Client of the upstream Recipe Catalog, generated from its contract. The call relays the caller's
 * own access token (AP0008), so the catalog checks the caller's recipe scopes.
 */
@Configuration(proxyBeanMethods = false)
class RecipeCatalogClientConfiguration {

    @Bean
    HttpRecipeCatalog mealplanningRecipeCatalog(RestClient.Builder restClientBuilder, Environment environment) {
        return new HttpRecipeCatalog(recipesApi(restClientBuilder,
                UpstreamClients.baseUrl(environment, "meal-planning", "recipe-catalog")));
    }

    /** The generated client resolves its paths against its own base path, so it is set as well. */
    static RecipesApi recipesApi(RestClient.Builder restClientBuilder, String baseUrl) {
        ApiClient apiClient = new ApiClient(UpstreamClients.restClient(restClientBuilder, baseUrl, new BearerTokenRelay()));
        apiClient.setBasePath(baseUrl);
        return new RecipesApi(apiClient);
    }
}
