package org.larder.platform.security;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity of the calling Cook, taken from the {@code cookId} claim of the JWT.
 * Bounded Contexts never call Cook Profile to find out who is calling.
 */
public record CookId(UUID value) {

    public CookId {
        Objects.requireNonNull(value, "cookId must not be null");
    }

    public static CookId of(String value) {
        return new CookId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
