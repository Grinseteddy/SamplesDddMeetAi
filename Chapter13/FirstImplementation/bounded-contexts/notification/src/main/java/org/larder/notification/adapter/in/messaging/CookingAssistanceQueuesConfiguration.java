package org.larder.notification.adapter.in.messaging;

import org.larder.platform.messaging.Topology;
import org.springframework.amqp.core.Declarables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Queues owned by Notification, bound to the {@code cooking-assistance} exchange
 * ({@code contracts/asyncapi/notifications.asyncapi.yaml}).
 */
@Configuration(proxyBeanMethods = false)
class CookingAssistanceQueuesConfiguration {

    static final String HELP_REQUESTED_QUEUE = "notifications.help-requested";
    static final String HELP_PROVIDED_QUEUE = "notifications.help-provided";

    @Bean
    Declarables notificationHelpRequestedQueue() {
        return Topology.consumerQueue(HELP_REQUESTED_QUEUE, "cooking-assistance", "cooking-assistance.help.requested");
    }

    @Bean
    Declarables notificationHelpProvidedQueue() {
        return Topology.consumerQueue(HELP_PROVIDED_QUEUE, "cooking-assistance", "cooking-assistance.help.provided");
    }
}
