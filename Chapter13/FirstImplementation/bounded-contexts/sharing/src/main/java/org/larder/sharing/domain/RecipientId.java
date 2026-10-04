package org.larder.sharing.domain;

import java.util.Objects;
import java.util.UUID;

public record RecipientId(UUID value) {

    public RecipientId {
        Objects.requireNonNull(value, "recipientId must not be null");
    }

    public static RecipientId newId() {
        return new RecipientId(UUID.randomUUID());
    }
}
