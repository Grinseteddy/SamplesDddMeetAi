package org.larder.e2e;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.larder.platform.messaging.Topology;
import org.larder.platform.test.AsyncApiContract;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * Listens in on the published language: a private queue bound with {@code #} to the exchanges
 * {@code cooking-assistance} and {@code grandma-avatar} receives a copy of every message, which is
 * validated against the AsyncAPI message of its exchange and AMQP type.
 */
final class MessageTap implements AutoCloseable {

    /** Exchange → contract file of its publisher. */
    private static final Map<String, String> CONTRACTS = Map.of(
            "cooking-assistance", "cooking-assistance.asyncapi.yaml",
            "grandma-avatar", "grandma-avatar.asyncapi.yaml");

    /** Exchange → the message keys published there (grandma-avatar.asyncapi.yaml also describes the consumed HelpRequested). */
    private static final Map<String, List<String>> PUBLISHED = Map.of(
            "cooking-assistance", List.of("helpRequested", "helpProvided"),
            "grandma-avatar", List.of("helpProvided"));

    /** A message as it went over the wire. */
    record TappedMessage(String exchange, String routingKey, String type, Map<String, Object> headers, String payload) {

        String correlationId() {
            return String.valueOf(headers.get("correlationId"));
        }

        String messageId() {
            return String.valueOf(headers.get("messageId"));
        }
    }

    private final CachingConnectionFactory connectionFactory;
    private final SimpleMessageListenerContainer listener;
    private final List<TappedMessage> messages = new CopyOnWriteArrayList<>();

    MessageTap(String host, int port, String username, String password) {
        connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        // exclusive to the tap and removed with its connection (RabbitMQ 4 allows no transient shared queues)
        Queue tap = new Queue("e2e.message-tap", false, true, true);
        admin.declareQueue(tap);
        CONTRACTS.keySet().forEach(exchange -> {
            var topic = Topology.exchange(exchange);
            admin.declareExchange(topic);
            admin.declareBinding(BindingBuilder.bind(tap).to(topic).with("#"));
        });
        listener = new SimpleMessageListenerContainer(connectionFactory);
        listener.setQueues(tap);
        listener.setMessageListener(this::record);
        listener.start();
    }

    private void record(Message message) {
        var properties = message.getMessageProperties();
        messages.add(new TappedMessage(properties.getReceivedExchange(), properties.getReceivedRoutingKey(),
                properties.getType(), new HashMap<>(properties.getHeaders()),
                new String(message.getBody(), StandardCharsets.UTF_8)));
    }

    List<TappedMessage> messages() {
        return List.copyOf(messages);
    }

    /**
     * Validates every tapped message against its AsyncAPI message (payload and headers) and returns how
     * many were validated. A message of an unknown type on a tapped exchange fails.
     */
    int assertAllMessagesMatchTheirContracts() {
        List<String> violations = new ArrayList<>();
        for (TappedMessage message : messages) {
            AsyncApiContract contract = AsyncApiContract.of(CONTRACTS.get(message.exchange()));
            String key = PUBLISHED.get(message.exchange()).stream()
                    .filter(candidate -> contract.messageName(candidate).equals(message.type()))
                    .findFirst()
                    .orElse(null);
            if (key == null) {
                violations.add(message.exchange() + " carries type " + message.type() + ", which "
                        + CONTRACTS.get(message.exchange()) + " does not publish there");
                continue;
            }
            try {
                contract.assertPayload(key, message.payload());
                contract.assertHeaders(key, message.headers());
            } catch (AssertionError violation) {
                violations.add(message.exchange() + "/" + message.type() + ": " + violation.getMessage());
            }
        }
        if (!violations.isEmpty()) {
            throw new AssertionError("Messages break their AsyncAPI contracts:\n" + String.join("\n", violations));
        }
        return messages.size();
    }

    @Override
    public void close() {
        listener.stop();
        connectionFactory.destroy();
    }
}
