package de.codecentric.cookingassistance.domain.external;

import de.codecentric.cookingassistance.domain.shared.Require;

import java.util.UUID;

/** Identity of a recipe step, owned outside Cooking Assistance (glossary term "howTo Step"). */
public record HowToStepId(UUID value) {

    public HowToStepId {
        Require.present(value, "howTo Step");
    }

    public static HowToStepId of(String value) {
        return new HowToStepId(UUID.fromString(value));
    }
}
