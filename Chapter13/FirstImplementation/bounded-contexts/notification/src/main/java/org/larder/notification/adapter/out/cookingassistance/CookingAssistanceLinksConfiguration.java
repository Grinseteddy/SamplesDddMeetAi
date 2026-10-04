package org.larder.notification.adapter.out.cookingassistance;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Where notifications point to: {@code larder.notification.links.base-url}, the public URL of
 * Larder (default {@value #DEFAULT_BASE_URL}).
 */
@Configuration(proxyBeanMethods = false)
class CookingAssistanceLinksConfiguration {

    static final String BASE_URL_PROPERTY = "larder.notification.links.base-url";
    static final String DEFAULT_BASE_URL = "https://larder.org";

    @Bean
    CookingAssistanceHelpLinks notificationHelpLinks(Environment environment) {
        return new CookingAssistanceHelpLinks(environment.getProperty(BASE_URL_PROPERTY, DEFAULT_BASE_URL));
    }
}
