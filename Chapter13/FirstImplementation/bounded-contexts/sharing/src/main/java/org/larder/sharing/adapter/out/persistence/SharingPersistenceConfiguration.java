package org.larder.sharing.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.sharing.application.ThanksService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code sharing} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class SharingPersistenceConfiguration {

    @Bean
    BoundedContextDatabase sharingDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "sharing");
    }

    @Bean(ThanksService.TRANSACTIONS)
    PlatformTransactionManager sharingTransactionManager(BoundedContextDatabase sharingDatabase) {
        return new DataSourceTransactionManager(sharingDatabase.dataSource());
    }

    @Bean
    JdbcThanksRepository sharingThanksRepository(BoundedContextDatabase sharingDatabase) {
        return new JdbcThanksRepository(sharingDatabase.jdbcClient());
    }
}
