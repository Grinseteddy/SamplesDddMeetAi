package org.larder.cookprofile.adapter.out.persistence;

import org.larder.cookprofile.application.CookService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code cookprofile} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class CookProfilePersistenceConfiguration {

    @Bean
    BoundedContextDatabase cookprofileDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "cook-profile");
    }

    @Bean(CookService.TRANSACTIONS)
    PlatformTransactionManager cookprofileTransactionManager(BoundedContextDatabase cookprofileDatabase) {
        return new DataSourceTransactionManager(cookprofileDatabase.dataSource());
    }

    @Bean
    JdbcCookRepository cookprofileCookRepository(BoundedContextDatabase cookprofileDatabase) {
        return new JdbcCookRepository(cookprofileDatabase.jdbcClient());
    }
}
