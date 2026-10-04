package org.larder.notification.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP;
import static org.larder.notification.TestData.HELP_REQUEST;
import static org.larder.notification.TestData.NOW;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.notification.application.InMemoryNotifications;
import org.larder.notification.application.NotificationService;
import org.larder.notification.domain.Notification;
import org.larder.platform.messaging.ContractMessage;
import org.larder.platform.messaging.MessageHeader;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.dao.DataAccessResourceFailureException;

class CookingAssistanceEventsListenerTest {

    private final InMemoryNotifications notifications = new InMemoryNotifications();
    private final CookingAssistanceEventsListener listener = listenerOn(notifications);

    private static CookingAssistanceEventsListener listenerOn(InMemoryNotifications repository) {
        return new CookingAssistanceEventsListener(new NotificationService(repository,
                helpId -> URI.create("https://larder.org/cooking-assistance/helps/" + helpId), Clock.fixed(NOW, ZoneOffset.UTC)));
    }

    private static HelpProvidedPayload helpProvided(UUID requester, String answerTitle) {
        return new HelpProvidedPayload(HELP, HELP_REQUEST, requester, answerTitle, HelpProviderType.COMMUNITY,
                UUID.randomUUID(), null, "ANSWERED");
    }

    private static Message message(String type, UUID messageId) {
        return ContractMessage.of(type, new MessageHeader(HELP_REQUEST, messageId, "cooking-assistance"), "{}");
    }

    @Test
    void theSameMessageTwiceCreatesOneNotification() {
        UUID messageId = UUID.randomUUID();

        listener.onHelpProvided(helpProvided(COOK.value(), "Use a cold pan"), message("HelpProvided", messageId));
        listener.onHelpProvided(helpProvided(COOK.value(), "Use a cold pan"), message("HelpProvided", messageId));

        assertThat(notifications.all()).singleElement()
                .satisfies(n -> assertThat(n.origin().value()).isEqualTo(messageId));
    }

    @Test
    void anInvalidHelpProvidedIsAcknowledgedWithoutANotification() {
        assertThatCode(() -> {
            listener.onHelpProvided(helpProvided(null, "Use a cold pan"), message("HelpProvided", UUID.randomUUID()));
            listener.onHelpProvided(helpProvided(COOK.value(), ""), message("HelpProvided", UUID.randomUUID()));
            listener.onHelpProvided(helpProvided(COOK.value(), "x".repeat(201)), message("HelpProvided", UUID.randomUUID()));
        }).doesNotThrowAnyException();

        assertThat(notifications.all()).isEmpty();
    }

    @Test
    void aMessageWithoutHeaderIsAcknowledgedWithoutANotification() {
        MessageProperties properties = new MessageProperties();
        properties.setType("HelpProvided");
        Message withoutHeader = new Message("{}".getBytes(), properties);

        assertThatCode(() -> listener.onHelpProvided(helpProvided(COOK.value(), "Use a cold pan"), withoutHeader))
                .doesNotThrowAnyException();
        assertThat(notifications.all()).isEmpty();
    }

    @Test
    void anInvalidHelpRequestedIsAcknowledged() {
        var payload = new HelpRequestedPayload(HELP_REQUEST, COOK.value(), "Burning Catastrophe",
                HelpType.STEPS_TO_MITIGATE_CATASTROPHE, "Scones are burned", null, null, null, List.of(), "OPEN");

        assertThatCode(() -> listener.onHelpRequested(payload, message("HelpRequested", UUID.randomUUID())))
                .doesNotThrowAnyException();
    }

    @Test
    void anUnavailableDatabaseIsThrownSoTheMessageIsRetriedAndThenDeadLettered() {
        var unavailable = new InMemoryNotifications() {
            @Override
            public boolean addIfNew(Notification notification) {
                throw new DataAccessResourceFailureException("database down");
            }

            @Override
            public Optional<Notification> findById(org.larder.notification.domain.NotificationId id) {
                throw new DataAccessResourceFailureException("database down");
            }
        };

        assertThatThrownBy(() -> listenerOn(unavailable)
                .onHelpProvided(helpProvided(COOK.value(), "Use a cold pan"), message("HelpProvided", UUID.randomUUID())))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }
}
