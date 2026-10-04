package org.larder.cookingassistance.domain;

import java.util.Objects;
import java.util.UUID;

public record HelpId(UUID value) {

    public HelpId {
        Objects.requireNonNull(value, "helpId must not be null");
    }

    public static HelpId newId() {
        return new HelpId(UUID.randomUUID());
    }
}
