package org.larder.grandmaavatar.domain;

import java.util.Objects;
import java.util.UUID;

/** A preparation step of a recipe, known to Grandma only by its id. */
public record HowToStepId(UUID value) {

    public HowToStepId {
        Objects.requireNonNull(value, "howToStepId");
    }
}
