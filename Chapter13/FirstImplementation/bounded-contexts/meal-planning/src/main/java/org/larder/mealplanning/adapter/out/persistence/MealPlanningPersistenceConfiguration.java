package org.larder.mealplanning.adapter.out.persistence;

import org.larder.mealplanning.application.MealPlanService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code mealplanning} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class MealPlanningPersistenceConfiguration {

    @Bean
    BoundedContextDatabase mealplanningDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "meal-planning");
    }

    @Bean(MealPlanService.TRANSACTIONS)
    PlatformTransactionManager mealplanningTransactionManager(BoundedContextDatabase mealplanningDatabase) {
        return new DataSourceTransactionManager(mealplanningDatabase.dataSource());
    }

    @Bean
    JdbcMealPlanRepository mealplanningMealPlanRepository(BoundedContextDatabase mealplanningDatabase) {
        return new JdbcMealPlanRepository(mealplanningDatabase.jdbcClient());
    }
}
