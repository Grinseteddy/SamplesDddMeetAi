package org.larder.cookingassistance.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code cookingassistance} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class CookingAssistancePersistenceConfiguration {

    @Bean
    BoundedContextDatabase cookingassistanceDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "cooking-assistance");
    }
}
