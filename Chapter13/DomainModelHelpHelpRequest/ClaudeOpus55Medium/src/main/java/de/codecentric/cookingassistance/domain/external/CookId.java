package de.codecentric.cookingassistance.domain.external;

import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.UUID;

/**
 * Identity of a Cook, owned by the Cook Profile context. Used here for the
 * glossary terms "requester", "help requester" and "help provider".
 */
public record CookId(UUID value) {

    public CookId {
        Require.present(value, "cook id");
    }

    public static CookId of(String value) {
        return new CookId(UUID.fromString(value));
    }
}
