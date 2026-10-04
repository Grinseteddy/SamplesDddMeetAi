package org.larder.recipecatalog.domain;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * One step of a recipe's preparation instructions (entity inside the {@link Recipe} aggregate).
 * Its sequence number gives its position, starting at 1; uniqueness within the recipe is the
 * recipe's rule. Changed only through its recipe.
 */
public final class HowToStep {

    static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final HowToStepId id;
    private int sequenceNumber;
    private String description;
    private URI illustration;

    private HowToStep(HowToStepId id, int sequenceNumber, String description, URI illustration) {
        this.id = Objects.requireNonNull(id);
        this.sequenceNumber = requireSequenceNumber(sequenceNumber);
        this.description = requireDescription(description);
        this.illustration = illustration;
    }

    static HowToStep create(HowToStepDraft draft) {
        Objects.requireNonNull(draft, "A how-to step needs its data");
        return new HowToStep(HowToStepId.newId(), draft.sequenceNumber(), draft.description(), draft.illustration());
    }

    /** Recreates a stored how-to step. */
    public static HowToStep restore(HowToStepId id, int sequenceNumber, String description, URI illustration) {
        return new HowToStep(id, sequenceNumber, description, illustration);
    }

    /** {@code null} leaves the part as it is. The recipe has checked the sequence number already. */
    void change(Integer newSequenceNumber, String newDescription, URI newIllustration) {
        int validSequenceNumber = newSequenceNumber == null ? sequenceNumber : requireSequenceNumber(newSequenceNumber);
        String validDescription = newDescription == null ? description : requireDescription(newDescription);
        sequenceNumber = validSequenceNumber;
        description = validDescription;
        if (newIllustration != null) {
            illustration = newIllustration;
        }
    }

    static int requireSequenceNumber(int sequenceNumber) {
        if (sequenceNumber < 1) {
            throw RecipeRuleViolationException.invalid("Sequence numbers of how-to steps start at 1, not " + sequenceNumber);
        }
        return sequenceNumber;
    }

    static String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            throw RecipeRuleViolationException.invalid("A how-to step needs a description");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw RecipeRuleViolationException.invalid(
                    "The description of a how-to step has at most " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return description;
    }

    public HowToStepId id() {
        return id;
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }

    public String description() {
        return description;
    }

    public Optional<URI> illustration() {
        return Optional.ofNullable(illustration);
    }
}
