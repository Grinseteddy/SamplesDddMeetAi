package org.larder.cookingassistance.adapter.in.messaging;

import org.larder.platform.messaging.Topology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Queue owned by Cooking Assistance for the answers of the Grandma Avatar
 * (channel {@code grandmaHelpProvidedQueue}).
 */
@Configuration(proxyBeanMethods = false)
class GrandmaHelpProvidedQueueConfiguration {

    static final String QUEUE = "cooking-assistance.grandma-avatar-help-provided";

    @Bean
    Declarables grandmaHelpProvidedQueue() {
        return Topology.consumerQueue(QUEUE, "grandma-avatar", "grandma-avatar.help.provided");
    }
}
