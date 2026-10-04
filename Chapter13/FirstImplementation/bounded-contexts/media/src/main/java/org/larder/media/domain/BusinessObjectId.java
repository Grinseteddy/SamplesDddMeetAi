package org.larder.media.domain;

import java.util.Objects;
import java.util.UUID;

/** Identifier of a recipe, thanks, help request or help in its owning context. */
public record BusinessObjectId(UUID value) {

    public BusinessObjectId {
        Objects.requireNonNull(value, "businessObjectId must not be null");
    }
}
