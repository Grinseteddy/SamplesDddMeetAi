package org.larder.cookingassistance.adapter.out.messaging;

import org.larder.platform.messaging.Topology;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@code cooking-assistance} topic exchange owned by this context
 * ({@code contracts/asyncapi/cooking-assistance.asyncapi.yaml}).
 */
@Configuration(proxyBeanMethods = false)
class CookingAssistanceExchangeConfiguration {

    static final String EXCHANGE = "cooking-assistance";
    static final String HELP_REQUESTED = "cooking-assistance.help.requested";
    static final String HELP_PROVIDED = "cooking-assistance.help.provided";

    @Bean
    TopicExchange cookingAssistanceExchange() {
        return Topology.exchange(EXCHANGE);
    }
}
