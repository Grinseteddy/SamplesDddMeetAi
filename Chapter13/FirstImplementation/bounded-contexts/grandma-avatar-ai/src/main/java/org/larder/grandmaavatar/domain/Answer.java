package org.larder.grandmaavatar.domain;

/**
 * The body of a Help: exactly one of four kinds, one per {@link HelpType}. Each kind checks its own
 * values on construction and, with {@link #mustFit}, whether it answers the given request - the
 * recipe, step and ingredients it refers to must be those of the request.
 */
public sealed interface Answer permits Substitutes, PreparationStepExplanation, CatastropheMitigation, MenuProposal {

    HelpType type();

    /** @throws InvalidAnswerException if this answer refers to anything the request did not ask about */
    void mustFit(HelpRequest request);

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new InvalidAnswerException(message);
        }
    }
}
