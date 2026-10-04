package org.larder.cookingassistance.domain;

import java.util.Objects;
import java.util.UUID;

public record HelpRequestId(UUID value) {

    public HelpRequestId {
        Objects.requireNonNull(value, "helpRequestId must not be null");
    }

    public static HelpRequestId newId() {
        return new HelpRequestId(UUID.randomUUID());
    }
}
