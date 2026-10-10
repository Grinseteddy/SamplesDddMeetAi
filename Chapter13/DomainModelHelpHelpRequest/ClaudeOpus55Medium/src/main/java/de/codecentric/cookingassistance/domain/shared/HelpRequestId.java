package de.codecentric.cookingassistance.domain.shared;

import java.util.UUID;

/** Identity of a Help Request (glossary: "help request Id"). Help "refers to" it, so it is shared. */
public record HelpRequestId(UUID value) {

    public HelpRequestId {
        Require.present(value, "help request Id");
    }

    public static HelpRequestId of(String value) {
        return new HelpRequestId(UUID.fromString(value));
    }

    public static HelpRequestId generate() {
        return new HelpRequestId(UUID.randomUUID());
    }
}
