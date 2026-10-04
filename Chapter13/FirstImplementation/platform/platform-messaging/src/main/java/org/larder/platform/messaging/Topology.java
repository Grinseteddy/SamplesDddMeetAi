package org.larder.platform.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

/**
 * Builds the RabbitMQ topology exactly as the AsyncAPI contracts describe it:
 * a publisher owns its topic exchange, a consumer owns its queue and binding.
 * <p>
 * Operational addition, not part of the contracts: every consumer queue dead-letters into
 * {@code <queue>.dlq} via the exchange {@value #DEAD_LETTER_EXCHANGE}, so a message that still
 * fails after the listener retries is parked instead of lost.
 */
public final class Topology {

    public static final String DEAD_LETTER_EXCHANGE = "larder.dead-letter";

    private Topology() {
    }

    public static TopicExchange exchange(String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    /** Durable queue owned by the consumer, bound to the publisher's exchange, with its dead-letter queue. */
    public static Declarables consumerQueue(String queue, String exchange, String routingKey) {
        TopicExchange topicExchange = exchange(exchange);
        Queue durableQueue = QueueBuilder.durable(queue)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(queue)
                .build();
        Binding binding = BindingBuilder.bind(durableQueue).to(topicExchange).with(routingKey);
        DirectExchange deadLetterExchange = ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE).durable(true).build();
        Queue deadLetterQueue = QueueBuilder.durable(queue + ".dlq").build();
        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(queue);
        return new Declarables(topicExchange, durableQueue, binding, deadLetterExchange, deadLetterQueue, deadLetterBinding);
    }
}
