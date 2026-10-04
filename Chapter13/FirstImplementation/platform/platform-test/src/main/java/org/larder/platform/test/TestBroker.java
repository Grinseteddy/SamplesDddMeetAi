package org.larder.platform.test;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.RabbitMQContainer;

/** One RabbitMQ container per test JVM, same major version as {@code infra/docker-compose.yml}. */
public final class TestBroker {

    private static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:4-management");
    private static CachingConnectionFactory connectionFactory;

    private TestBroker() {
    }

    /** Connection factory with publisher confirms and returns, as the application uses it. */
    public static synchronized CachingConnectionFactory connectionFactory() {
        if (!RABBIT.isRunning()) {
            RABBIT.start();
        }
        if (connectionFactory == null) {
            connectionFactory = new CachingConnectionFactory(RABBIT.getHost(), RABBIT.getAmqpPort());
            connectionFactory.setUsername(RABBIT.getAdminUsername());
            connectionFactory.setPassword(RABBIT.getAdminPassword());
            connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.SIMPLE);
            connectionFactory.setPublisherReturns(true);
        }
        return connectionFactory;
    }

    public static RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }

    public static RabbitAdmin admin() {
        return new RabbitAdmin(connectionFactory());
    }

    public static String host() {
        connectionFactory();
        return RABBIT.getHost();
    }

    public static int port() {
        connectionFactory();
        return RABBIT.getAmqpPort();
    }
}
