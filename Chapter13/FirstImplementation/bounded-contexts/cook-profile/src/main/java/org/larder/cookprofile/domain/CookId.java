package org.larder.cookprofile.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of a cook. On Larder it is the {@code cookId} the IAM puts into the access token. */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }
}
