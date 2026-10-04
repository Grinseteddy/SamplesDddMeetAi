package org.larder.media.domain;

import java.util.Objects;
import java.util.UUID;

public record MediaId(UUID value) {

    public MediaId {
        Objects.requireNonNull(value, "mediaId must not be null");
    }

    public static MediaId newId() {
        return new MediaId(UUID.randomUUID());
    }
}
