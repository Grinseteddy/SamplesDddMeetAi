package org.larder.mealpreparation.domain;

import java.util.Objects;

/**
 * One step of the recipe as a meal preparation sees it (value object): which step and where it stands
 * in the recipe's order. Recipe Catalog owns the step; this is the copy taken when the preparation
 * started. Sequence numbers start at 1.
 */
public record HowToStep(HowToStepId id, int sequenceNumber) {

    public HowToStep {
        Objects.requireNonNull(id, "A how-to step needs its identifier");
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("Sequence numbers of how-to steps start at 1, not " + sequenceNumber);
        }
    }
}
