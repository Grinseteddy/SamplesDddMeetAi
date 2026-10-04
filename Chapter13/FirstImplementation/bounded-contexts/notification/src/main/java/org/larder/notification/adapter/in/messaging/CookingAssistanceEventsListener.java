package org.larder.notification.adapter.in.messaging;

import static org.larder.notification.adapter.in.messaging.CookingAssistanceQueuesConfiguration.HELP_PROVIDED_QUEUE;
import static org.larder.notification.adapter.in.messaging.CookingAssistanceQueuesConfiguration.HELP_REQUESTED_QUEUE;

import java.util.Optional;

import org.larder.notification.application.InvalidEventException;
import org.larder.notification.application.NotificationService;
import org.larder.notification.domain.NotificationRuleViolationException;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes Cooking Assistance's events ({@code contracts/asyncapi/notifications.asyncapi.yaml}),
 * conformist to its published language.
 * <p>
 * Delivery is at least once, so handling is idempotent: the {@code messageId} of the
 * {@link MessageHeader} identifies the event, and one event creates at most one notification.
 * A message that can never be processed - no readable header, a missing field, a value breaking a
 * notification rule - is logged and acknowledged ({@link InvalidMessageHandler} does the same for
 * unreadable bodies). Any other failure, e.g. the database being unavailable, is thrown: the
 * listener retries (3 attempts, configured centrally) and then dead-letters into {@code <queue>.dlq}.
 * The message is acknowledged only after the notification is committed.
 */
@Component("notificationCookingAssistanceEventsListener")
class CookingAssistanceEventsListener {

    private static final Logger LOG = LoggerFactory.getLogger(CookingAssistanceEventsListener.class);

    private final NotificationService service;

    CookingAssistanceEventsListener(NotificationService service) {
        this.service = service;
    }

    @RabbitListener(id = "notificationHelpProvided", queues = HELP_PROVIDED_QUEUE, errorHandler = InvalidMessageHandler.BEAN)
    void onHelpProvided(HelpProvidedPayload payload, Message message) {
        header(message).ifPresent(header -> {
            try {
                service.helpProvided(payload.toEvent(header));
            } catch (InvalidEventException | NotificationRuleViolationException e) {
                drop(message, header, e);
            }
        });
    }

    @RabbitListener(id = "notificationHelpRequested", queues = HELP_REQUESTED_QUEUE, errorHandler = InvalidMessageHandler.BEAN)
    void onHelpRequested(HelpRequestedPayload payload, Message message) {
        header(message).ifPresent(header -> {
            try {
                service.helpRequested(payload.toEvent(header));
            } catch (InvalidEventException e) {
                drop(message, header, e);
            }
        });
    }

    private static Optional<MessageHeader> header(Message message) {
        try {
            return Optional.of(ContractMessage.header(message));
        } catch (RuntimeException e) {
            LOG.warn("Dropping message of type {} from queue {} without a valid MessageHeader: {}",
                    ContractMessage.messageType(message), message.getMessageProperties().getConsumerQueue(), e.getMessage());
            return Optional.empty();
        }
    }

    private static void drop(Message message, MessageHeader header, RuntimeException reason) {
        LOG.warn("Dropping invalid {} {} (correlationId {}): {}", ContractMessage.messageType(message),
                header.messageId(), header.correlationId(), reason.getMessage());
    }
}
