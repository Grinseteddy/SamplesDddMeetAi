package org.larder.notification.adapter.out.persistence;

import org.larder.notification.application.NotificationService;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Own schema {@code notification} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class NotificationPersistenceConfiguration {

    @Bean
    BoundedContextDatabase notificationDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "notification");
    }

    @Bean(NotificationService.TRANSACTIONS)
    PlatformTransactionManager notificationTransactionManager(BoundedContextDatabase notificationDatabase) {
        return new DataSourceTransactionManager(notificationDatabase.dataSource());
    }

    @Bean
    JdbcNotificationRepository notificationNotificationRepository(BoundedContextDatabase notificationDatabase) {
        return new JdbcNotificationRepository(notificationDatabase.jdbcClient());
    }
}
