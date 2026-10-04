package org.larder.notification.domain;

import java.util.Objects;
import java.util.UUID;

/** A Cook a notification is addressed to, identified by the Cook's id from Cook Profile. */
public record ReceiverId(UUID value) {

    public ReceiverId {
        Objects.requireNonNull(value, "receiver must not be null");
    }
}
