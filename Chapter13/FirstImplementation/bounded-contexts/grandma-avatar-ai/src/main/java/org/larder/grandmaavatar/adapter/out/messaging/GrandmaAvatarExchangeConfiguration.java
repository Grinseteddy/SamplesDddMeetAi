package org.larder.grandmaavatar.adapter.out.messaging;

import org.larder.platform.messaging.Topology;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@code grandma-avatar} topic exchange owned by this context
 * ({@code contracts/asyncapi/grandma-avatar.asyncapi.yaml}).
 */
@Configuration(proxyBeanMethods = false)
class GrandmaAvatarExchangeConfiguration {

    static final String EXCHANGE = "grandma-avatar";
    static final String HELP_PROVIDED = "grandma-avatar.help.provided";

    @Bean
    TopicExchange grandmaAvatarExchange() {
        return Topology.exchange(EXCHANGE);
    }
}
