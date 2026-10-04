package org.larder.recipecatalog.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.recipecatalog.application.RecipeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code recipecatalog} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class RecipeCatalogPersistenceConfiguration {

    @Bean
    BoundedContextDatabase recipecatalogDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "recipe-catalog");
    }

    @Bean(RecipeService.TRANSACTIONS)
    PlatformTransactionManager recipecatalogTransactionManager(BoundedContextDatabase recipecatalogDatabase) {
        return new DataSourceTransactionManager(recipecatalogDatabase.dataSource());
    }

    @Bean
    JdbcRecipeRepository recipecatalogRecipeRepository(BoundedContextDatabase recipecatalogDatabase) {
        return new JdbcRecipeRepository(recipecatalogDatabase.jdbcClient());
    }
}
