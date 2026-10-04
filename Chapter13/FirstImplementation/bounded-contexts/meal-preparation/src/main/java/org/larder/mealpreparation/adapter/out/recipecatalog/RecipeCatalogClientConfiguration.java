package org.larder.mealpreparation.adapter.out.recipecatalog;

import org.larder.platform.web.UpstreamClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/**
 * Recipe Catalog as seen from Meal Preparation; base URL from
 * {@code larder.meal-preparation.upstream.recipe-catalog.base-url}.
 */
@Configuration(proxyBeanMethods = false)
class RecipeCatalogClientConfiguration {

    @Bean
    RecipeCatalogClient mealpreparationRecipeCatalog(RestClient.Builder restClientBuilder, Environment environment) {
        return RecipeCatalogClient.create(restClientBuilder,
                UpstreamClients.baseUrl(environment, "meal-preparation", "recipe-catalog"));
    }
}
