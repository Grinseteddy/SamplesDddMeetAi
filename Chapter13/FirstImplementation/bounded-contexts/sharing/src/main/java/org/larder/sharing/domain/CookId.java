package org.larder.sharing.domain;

import java.util.Objects;
import java.util.UUID;

/** A cook of Larder - as giver of thanks, as receiver of a help or as mentioned helper. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }
}
