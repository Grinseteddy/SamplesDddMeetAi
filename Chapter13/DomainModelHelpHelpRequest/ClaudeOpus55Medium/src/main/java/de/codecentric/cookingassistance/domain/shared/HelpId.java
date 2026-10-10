package de.codecentric.cookingassistance.domain.shared;

import java.util.UUID;

/** Identity of a Help (glossary: "help Id"). Shared because Help Request learns of accepted Help by this id. */
public record HelpId(UUID value) {

    public HelpId {
        Require.present(value, "help Id");
    }

    public static HelpId of(String value) {
        return new HelpId(UUID.fromString(value));
    }

    public static HelpId generate() {
        return new HelpId(UUID.randomUUID());
    }
}
