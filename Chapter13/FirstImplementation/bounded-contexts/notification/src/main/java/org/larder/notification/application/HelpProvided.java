package org.larder.notification.application;

import java.util.Objects;
import java.util.UUID;

import org.larder.notification.domain.EventId;
import org.larder.notification.domain.HelpProvider;
import org.larder.notification.domain.ReceiverId;

/**
 * Cooking Assistance's event "help provided", as far as Notification reads it.
 *
 * @param eventId       the {@code messageId} of the message; one notification per event at most
 * @param helpId        the Help the notification links to
 * @param helpRequester the Cook who asked for help - the Receiver
 */
public record HelpProvided(EventId eventId, UUID helpId, ReceiverId helpRequester, String answerTitle,
                           HelpProvider helpProviderType) {

    public HelpProvided {
        require(eventId, "messageId");
        require(helpId, "helpId");
        require(helpRequester, "helpRequester");
        require(helpProviderType, "helpProviderType");
        if (answerTitle == null || answerTitle.isBlank()) {
            throw new InvalidEventException("HelpProvided without answerTitle");
        }
    }

    private static void require(Object value, String name) {
        if (Objects.isNull(value)) {
            throw new InvalidEventException("HelpProvided without " + name);
        }
    }
}
