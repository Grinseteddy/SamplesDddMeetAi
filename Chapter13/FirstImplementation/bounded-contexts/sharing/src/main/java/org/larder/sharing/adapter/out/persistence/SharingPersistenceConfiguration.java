package org.larder.sharing.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code sharing} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class SharingPersistenceConfiguration {

    @Bean
    BoundedContextDatabase sharingDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "sharing");
    }
}
