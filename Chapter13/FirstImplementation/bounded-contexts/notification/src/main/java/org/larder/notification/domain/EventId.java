package org.larder.notification.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity of the event a notification was created from (the {@code messageId} of the
 * upstream message). Notifications are created exclusively on events, and at most once per event.
 */
public record EventId(UUID value) {

    public EventId {
        Objects.requireNonNull(value, "eventId must not be null");
    }
}
