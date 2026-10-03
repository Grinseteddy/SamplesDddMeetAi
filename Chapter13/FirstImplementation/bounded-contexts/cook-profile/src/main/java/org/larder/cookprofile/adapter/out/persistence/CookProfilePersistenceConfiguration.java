package org.larder.cookprofile.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code cookprofile} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class CookProfilePersistenceConfiguration {

    @Bean
    BoundedContextDatabase cookprofileDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "cook-profile");
    }
}
