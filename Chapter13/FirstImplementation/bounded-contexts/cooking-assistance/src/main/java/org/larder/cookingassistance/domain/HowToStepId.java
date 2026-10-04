package org.larder.cookingassistance.domain;

import java.util.Objects;
import java.util.UUID;

/** A how-to step of a recipe in Recipe Catalog, referenced by its id only. */
public record HowToStepId(UUID value) {

    public HowToStepId {
        Objects.requireNonNull(value, "howToStep must not be null");
    }
}
