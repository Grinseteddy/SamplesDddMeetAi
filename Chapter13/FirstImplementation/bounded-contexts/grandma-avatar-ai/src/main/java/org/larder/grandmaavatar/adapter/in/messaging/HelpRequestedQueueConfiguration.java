package org.larder.grandmaavatar.adapter.in.messaging;

import org.larder.grandmaavatar.application.HelpRequestHandler;
import org.larder.platform.messaging.Topology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Queue owned by the Grandma Avatar for the help requests of Cooking Assistance
 * (channel {@code helpRequestedQueue}), and its listener.
 */
@Configuration(proxyBeanMethods = false)
class HelpRequestedQueueConfiguration {

    static final String QUEUE = "grandma-avatar.help-requested";
    static final String EXCHANGE = "cooking-assistance";
    static final String ROUTING_KEY = "cooking-assistance.help.requested";

    @Bean
    Declarables grandmaAvatarHelpRequestedQueue() {
        return Topology.consumerQueue(QUEUE, EXCHANGE, ROUTING_KEY);
    }

    @Bean
    HelpRequestedListener grandmaavatarHelpRequestedListener(HelpRequestHandler helpRequestHandler, ObjectMapper objectMapper) {
        return new HelpRequestedListener(helpRequestHandler, objectMapper);
    }
}
