package org.larder.platform.persistence;

import java.time.Clock;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Shared beans for all Bounded Contexts' persistence. */
@AutoConfiguration
public class LarderPersistenceConfiguration {

    /** One clock for all contexts; tests replace it with a fixed one. */
    @Bean
    @ConditionalOnMissingBean
    Clock clock() {
        return Clock.systemUTC();
    }
}
