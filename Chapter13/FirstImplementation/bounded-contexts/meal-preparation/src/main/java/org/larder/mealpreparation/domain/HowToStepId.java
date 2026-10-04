package org.larder.mealpreparation.domain;

import java.util.Objects;
import java.util.UUID;

/** Identifier of a how-to step, given by Recipe Catalog (the glossary's howToStepId). */
public record HowToStepId(UUID value) {

    public HowToStepId {
        Objects.requireNonNull(value, "howToStepId must not be null");
    }
}
