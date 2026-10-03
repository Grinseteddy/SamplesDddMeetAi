package org.larder.notification.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code notification} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class NotificationPersistenceConfiguration {

    @Bean
    BoundedContextDatabase notificationDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "notification");
    }
}
