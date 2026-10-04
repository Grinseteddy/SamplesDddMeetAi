package org.larder.notification.application;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.larder.notification.domain.EventId;
import org.larder.notification.domain.HelpProvider;
import org.larder.notification.domain.ReceiverId;

/** Cooking Assistance's event "help requested", as far as Notification reads it. */
public record HelpRequested(EventId eventId, UUID helpRequestId, ReceiverId requester,
                            List<HelpProvider> preferredProviders) {

    public HelpRequested {
        require(eventId, "messageId");
        require(helpRequestId, "helpRequestId");
        require(requester, "requester");
        if (preferredProviders == null || preferredProviders.isEmpty() || preferredProviders.stream().anyMatch(Objects::isNull)) {
            throw new InvalidEventException("HelpRequested without preferredProvider");
        }
        preferredProviders = List.copyOf(preferredProviders);
    }

    private static void require(Object value, String name) {
        if (Objects.isNull(value)) {
            throw new InvalidEventException("HelpRequested without " + name);
        }
    }
}
