package org.larder.recipecatalog.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code recipecatalog} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class RecipeCatalogPersistenceConfiguration {

    @Bean
    BoundedContextDatabase recipecatalogDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "recipe-catalog");
    }
}
