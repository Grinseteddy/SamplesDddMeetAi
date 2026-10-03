package org.larder.platform.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

/**
 * Builds the RabbitMQ topology exactly as the AsyncAPI contracts describe it:
 * a publisher owns its topic exchange, a consumer owns its queue and binding.
 */
public final class Topology {

    private Topology() {
    }

    public static TopicExchange exchange(String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    /** Durable queue owned by the consumer, bound to the publisher's exchange. */
    public static Declarables consumerQueue(String queue, String exchange, String routingKey) {
        TopicExchange topicExchange = exchange(exchange);
        Queue durableQueue = QueueBuilder.durable(queue).build();
        Binding binding = BindingBuilder.bind(durableQueue).to(topicExchange).with(routingKey);
        return new Declarables(topicExchange, durableQueue, binding);
    }
}
