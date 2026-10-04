package org.larder.sharing.domain;

import java.util.Objects;
import java.util.UUID;

/** Identifier of an image kept by Media. */
public record MediaId(UUID value) {

    public MediaId {
        Objects.requireNonNull(value, "mediaId must not be null");
    }
}
