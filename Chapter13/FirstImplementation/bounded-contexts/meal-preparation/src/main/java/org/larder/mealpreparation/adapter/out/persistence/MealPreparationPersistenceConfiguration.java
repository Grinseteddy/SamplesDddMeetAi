package org.larder.mealpreparation.adapter.out.persistence;

import org.larder.mealpreparation.application.MealPreparationService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code mealpreparation} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class MealPreparationPersistenceConfiguration {

    @Bean
    BoundedContextDatabase mealpreparationDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "meal-preparation");
    }

    @Bean(MealPreparationService.TRANSACTIONS)
    PlatformTransactionManager mealpreparationTransactionManager(BoundedContextDatabase mealpreparationDatabase) {
        return new DataSourceTransactionManager(mealpreparationDatabase.dataSource());
    }

    @Bean
    JdbcMealPreparationRepository mealpreparationMealPreparationRepository(BoundedContextDatabase mealpreparationDatabase) {
        return new JdbcMealPreparationRepository(mealpreparationDatabase.jdbcClient());
    }
}
