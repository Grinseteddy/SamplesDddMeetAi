package org.larder.grandmaavatar.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code grandmaavatar} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class GrandmaAvatarAiPersistenceConfiguration {

    @Bean
    BoundedContextDatabase grandmaavatarDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "grandma-avatar-ai");
    }
}
