package com.cookwithus.domain.cook;

import java.util.UUID;

/**
 * Value Object representing the identity of a Cook.
 * Using a typed identifier prevents accidental mixing of IDs across aggregates.
 */
public record CookId(UUID value) {

    public CookId {
        if (value == null) {
            throw new IllegalArgumentException("CookId value must not be null");
        }
    }

    public static CookId newCookId() {
        return new CookId(UUID.randomUUID());
    }

    public static CookId of(String value) {
        return new CookId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
