package org.larder.cookingassistance.adapter.out.persistence;

import org.larder.cookingassistance.application.HelpRequestService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code cookingassistance} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class CookingAssistancePersistenceConfiguration {

    @Bean
    BoundedContextDatabase cookingassistanceDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "cooking-assistance");
    }

    @Bean(HelpRequestService.TRANSACTIONS)
    PlatformTransactionManager cookingassistanceTransactionManager(BoundedContextDatabase cookingassistanceDatabase) {
        return new DataSourceTransactionManager(cookingassistanceDatabase.dataSource());
    }

    @Bean
    JdbcHelpRequestRepository cookingassistanceHelpRequestRepository(BoundedContextDatabase cookingassistanceDatabase) {
        return new JdbcHelpRequestRepository(cookingassistanceDatabase.jdbcClient());
    }

    @Bean
    JdbcHelpRepository cookingassistanceHelpRepository(BoundedContextDatabase cookingassistanceDatabase) {
        return new JdbcHelpRepository(cookingassistanceDatabase.jdbcClient());
    }
}
