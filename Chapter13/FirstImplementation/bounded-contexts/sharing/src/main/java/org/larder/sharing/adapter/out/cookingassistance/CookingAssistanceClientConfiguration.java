package org.larder.sharing.adapter.out.cookingassistance;

import org.larder.platform.web.UpstreamClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

/**
 * Cooking Assistance as seen from Sharing; base URL from
 * {@code larder.sharing.upstream.cooking-assistance.base-url}.
 */
@Configuration(proxyBeanMethods = false)
class CookingAssistanceClientConfiguration {

    @Bean
    CookingAssistanceHelps sharingHelps(RestClient.Builder restClientBuilder, Environment environment) {
        return CookingAssistanceHelps.create(restClientBuilder,
                UpstreamClients.baseUrl(environment, "sharing", "cooking-assistance"));
    }
}
