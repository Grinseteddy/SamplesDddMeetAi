package org.larder.grandmaavatar.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Grandma's note that she has dealt with a help request - answered or not. There is at most one per
 * request: it is what makes her answer every request at most once although requests may arrive twice.
 */
public record HandledHelpRequest(
        HelpRequestId helpRequest,
        CookId requester,
        HelpType type,
        Outcome outcome,
        Optional<HelpId> help,
        Optional<String> answerTitle,
        Instant handledAt) {

    public HandledHelpRequest {
        Objects.requireNonNull(helpRequest, "helpRequest");
        Objects.requireNonNull(requester, "requester");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(handledAt, "handledAt");
        help = Objects.requireNonNullElse(help, Optional.empty());
        answerTitle = Objects.requireNonNullElse(answerTitle, Optional.empty());
        if ((outcome == Outcome.ANSWERED) != help.isPresent() || help.isPresent() != answerTitle.isPresent()) {
            throw new IllegalArgumentException("exactly the answered requests carry their help");
        }
    }

    public static HandledHelpRequest answered(Help help, Instant handledAt) {
        return new HandledHelpRequest(help.helpRequest(), help.helpRequester(), help.type(), Outcome.ANSWERED,
                Optional.of(help.id()), Optional.of(help.answerTitle()), handledAt);
    }

    public static HandledHelpRequest notAnswered(HelpRequest request, Outcome outcome, Instant handledAt) {
        return new HandledHelpRequest(request.id(), request.requester(), request.type(), outcome,
                Optional.empty(), Optional.empty(), handledAt);
    }
}
