package org.larder.media.adapter.out.persistence;

import org.larder.platform.persistence.BoundedContextDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/** Own schema {@code media} with its own database user (ADR0002). */
@Configuration(proxyBeanMethods = false)
class MediaPersistenceConfiguration {

    @Bean
    BoundedContextDatabase mediaDatabase(Environment environment) {
        return BoundedContextDatabase.fromEnvironment(environment, "media");
    }
}
