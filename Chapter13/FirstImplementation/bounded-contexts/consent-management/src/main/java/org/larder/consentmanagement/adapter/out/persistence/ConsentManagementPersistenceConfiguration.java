package org.larder.consentmanagement.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code consentmanagement} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class ConsentManagementPersistenceConfiguration {

    @Bean
    BoundedContextDatabase consentmanagementDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "consent-management");
    }
}
