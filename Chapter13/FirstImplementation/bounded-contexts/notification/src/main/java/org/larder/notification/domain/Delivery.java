package org.larder.notification.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * The notification as one Receiver has it: their own read status, and whether they deleted it.
 * Receivers of the same notification read and delete independently of each other.
 */
public record Delivery(ReceiverId receiver, Status status, Instant deletedAt) {

    public Delivery {
        Objects.requireNonNull(receiver, "receiver must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }

    static Delivery newFor(ReceiverId receiver) {
        return new Delivery(receiver, Status.NEW, null);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Optional<Instant> deletedAtIfAny() {
        return Optional.ofNullable(deletedAt);
    }

    Delivery withStatus(Status newStatus) {
        return new Delivery(receiver, newStatus, deletedAt);
    }

    Delivery deleted(Instant now) {
        return new Delivery(receiver, status, now);
    }
}
