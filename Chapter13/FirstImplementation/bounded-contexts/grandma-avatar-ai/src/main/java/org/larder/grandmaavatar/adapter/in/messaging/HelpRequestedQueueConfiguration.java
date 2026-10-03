package org.larder.grandmaavatar.adapter.in.messaging;

import org.larder.platform.messaging.Topology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Queue owned by the Grandma Avatar for the help requests of Cooking Assistance
 * (channel {@code helpRequestedQueue}).
 */
@Configuration(proxyBeanMethods = false)
class HelpRequestedQueueConfiguration {

    static final String QUEUE = "grandma-avatar.help-requested";

    @Bean
    Declarables grandmaAvatarHelpRequestedQueue() {
        return Topology.consumerQueue(QUEUE, "cooking-assistance", "cooking-assistance.help.requested");
    }
}
