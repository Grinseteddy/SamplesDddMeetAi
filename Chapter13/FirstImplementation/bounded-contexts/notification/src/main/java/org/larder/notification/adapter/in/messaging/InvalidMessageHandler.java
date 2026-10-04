package org.larder.notification.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;

/**
 * A message whose body cannot be read as the contract's payload (no JSON, wrong types, unknown enum
 * value) will never be processable: it is logged and acknowledged instead of retried and dead-lettered.
 * Every other failure (e.g. the database is down) is rethrown, so the listener retries it and finally
 * parks it in {@code <queue>.dlq}.
 */
@Component(InvalidMessageHandler.BEAN)
class InvalidMessageHandler implements RabbitListenerErrorHandler {

    static final String BEAN = "notificationInvalidMessageHandler";

    private static final Logger LOG = LoggerFactory.getLogger(InvalidMessageHandler.class);

    @Override
    public Object handleError(Message amqpMessage, Channel channel, org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) throws Exception {
        if (isUnreadable(exception)) {
            LOG.warn("Dropping unreadable message {} (type {}) from queue {}: {}",
                    amqpMessage.getMessageProperties().getMessageId(),
                    amqpMessage.getMessageProperties().getType(),
                    amqpMessage.getMessageProperties().getConsumerQueue(),
                    rootMessage(exception));
            return null;
        }
        throw exception;
    }

    private static boolean isUnreadable(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            if (cause instanceof MessageConversionException
                    || cause instanceof org.springframework.messaging.converter.MessageConversionException
                    || cause instanceof JsonProcessingException) {
                return true;
            }
        }
        return false;
    }

    private static String rootMessage(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
