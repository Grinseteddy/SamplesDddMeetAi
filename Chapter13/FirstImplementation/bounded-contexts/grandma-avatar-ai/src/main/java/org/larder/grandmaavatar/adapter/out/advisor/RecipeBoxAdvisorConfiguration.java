package org.larder.grandmaavatar.adapter.out.advisor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Selects the advisor behind Grandma: {@code larder.grandma-avatar-ai.advisor=recipe-box} (default).
 * Another advisor (e.g. an external AI) comes with its own configuration and property value; if the
 * property names an advisor that does not exist, the application does not start.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "larder.grandma-avatar-ai", name = "advisor", havingValue = "recipe-box", matchIfMissing = true)
class RecipeBoxAdvisorConfiguration {

    @Bean
    RecipeBoxAdvisor grandmaavatarRecipeBoxAdvisor() {
        return new RecipeBoxAdvisor();
    }
}
