package de.codecentric.cookingassistance.domain.shared;

/**
 * The fact that a Help was accepted for a Help Request.
 *
 * <p>Not a sticky on either glossary. It exists because Help Request's rule
 * "At least one Help must be accepted" (for status Answered) depends on Help's
 * "isAccepted", which lives in another aggregate. Help produces this fact
 * from {@code Help.accept(...)}; Help Request consumes it in
 * {@code HelpRequest.acceptHelp(...)}. Neither aggregate holds the other.
 */
public record HelpAccepted(HelpId helpId, HelpRequestId helpRequestId) {

    public HelpAccepted {
        Require.present(helpId, "help Id");
        Require.present(helpRequestId, "help request Id");
    }
}
