package org.larder.sharing.domain;

import java.util.Objects;
import java.util.UUID;

public record ThanksId(UUID value) {

    public ThanksId {
        Objects.requireNonNull(value, "thanksId must not be null");
    }

    public static ThanksId newId() {
        return new ThanksId(UUID.randomUUID());
    }
}
