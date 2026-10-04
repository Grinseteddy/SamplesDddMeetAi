package org.larder.mealpreparation.domain;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The how-to steps of one recipe in the recipe's order (value object), as Recipe Catalog delivers
 * them. Ordered by sequence number whatever order they arrive in; identifiers and sequence numbers
 * are unique. May be empty - whether a meal preparation can start from it is the preparation's rule.
 */
public record RecipeSteps(List<HowToStep> steps) {

    public RecipeSteps {
        Objects.requireNonNull(steps, "steps must not be null");
        steps = steps.stream().sorted(Comparator.comparingInt(HowToStep::sequenceNumber)).toList();
        var ids = new HashSet<HowToStepId>();
        var sequenceNumbers = new HashSet<Integer>();
        for (HowToStep step : steps) {
            if (!ids.add(step.id())) {
                throw new IllegalArgumentException("How-to step " + step.id().value() + " appears twice");
            }
            if (!sequenceNumbers.add(step.sequenceNumber())) {
                throw new IllegalArgumentException("Sequence number " + step.sequenceNumber() + " appears twice");
            }
        }
    }

    public static RecipeSteps of(HowToStep... steps) {
        return new RecipeSteps(List.of(steps));
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public int size() {
        return steps.size();
    }

    /** The step at {@code position} in the recipe's order, 0 = first. */
    HowToStep at(int position) {
        return steps.get(position);
    }

    /** Position of the step in the recipe's order, or -1 if it is not one of these steps. */
    int positionOf(HowToStepId id) {
        for (int position = 0; position < steps.size(); position++) {
            if (steps.get(position).id().equals(id)) {
                return position;
            }
        }
        return -1;
    }

    public Optional<HowToStep> find(HowToStepId id) {
        int position = positionOf(id);
        return position < 0 ? Optional.empty() : Optional.of(steps.get(position));
    }
}
