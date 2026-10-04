package org.larder.media.adapter.out.persistence;

import org.larder.media.application.MediaService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code media} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class MediaPersistenceConfiguration {

    @Bean
    BoundedContextDatabase mediaDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "media");
    }

    @Bean(MediaService.TRANSACTIONS)
    PlatformTransactionManager mediaTransactionManager(BoundedContextDatabase mediaDatabase) {
        return new DataSourceTransactionManager(mediaDatabase.dataSource());
    }

    @Bean
    JdbcMediaRepository mediaMediaRepository(BoundedContextDatabase mediaDatabase) {
        return new JdbcMediaRepository(mediaDatabase.jdbcClient());
    }
}
