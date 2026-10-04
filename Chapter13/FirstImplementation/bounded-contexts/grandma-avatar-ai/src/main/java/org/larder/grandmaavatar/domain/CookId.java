package org.larder.grandmaavatar.domain;

import java.util.Objects;
import java.util.UUID;

/** A cook, here always the one who asked for help. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId");
    }
}
