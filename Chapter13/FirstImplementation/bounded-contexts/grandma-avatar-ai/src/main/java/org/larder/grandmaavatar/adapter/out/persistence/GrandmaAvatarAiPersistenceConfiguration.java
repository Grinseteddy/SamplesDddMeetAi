package org.larder.grandmaavatar.adapter.out.persistence;

import org.larder.grandmaavatar.application.HelpRequestHandler;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code grandmaavatar} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class GrandmaAvatarAiPersistenceConfiguration {

    @Bean
    BoundedContextDatabase grandmaavatarDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "grandma-avatar-ai");
    }

    @Bean(HelpRequestHandler.TRANSACTIONS)
    PlatformTransactionManager grandmaavatarTransactionManager(BoundedContextDatabase grandmaavatarDatabase) {
        return new DataSourceTransactionManager(grandmaavatarDatabase.dataSource());
    }

    @Bean
    JdbcHandledHelpRequests grandmaavatarHandledHelpRequests(BoundedContextDatabase grandmaavatarDatabase) {
        return new JdbcHandledHelpRequests(grandmaavatarDatabase.jdbcClient());
    }
}
