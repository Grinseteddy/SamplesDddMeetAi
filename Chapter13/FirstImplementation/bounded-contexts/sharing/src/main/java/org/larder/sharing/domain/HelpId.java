package org.larder.sharing.domain;

import java.util.Objects;
import java.util.UUID;

/** Identifier of a Help of Cooking Assistance; the thanks are given for exactly one help. */
public record HelpId(UUID value) {

    public HelpId {
        Objects.requireNonNull(value, "helpId must not be null");
    }
}
