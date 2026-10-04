package org.larder.grandmaavatar.application;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.HandledHelpRequest;
import org.larder.grandmaavatar.domain.Help;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.InvalidAnswerException;
import org.larder.grandmaavatar.domain.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case: Grandma considers a help request of Cooking Assistance.
 * <ol>
 *   <li>Already handled? Then nothing happens - requests arrive at least once, Grandma answers at most once.</li>
 *   <li>Not for her (not open, or she is not a preferred provider)? Noted, not answered.</li>
 *   <li>Otherwise her {@link HelpAdvisor} is asked - outside any transaction, an AI may take its time.
 *       No advice, or advice breaking the invariants of a Help: noted, not answered.</li>
 *   <li>The note and the Help go out together ({@link HandledRequestRecorder}).</li>
 * </ol>
 */
@Service
public class HelpRequestHandler {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "grandmaavatarTransactionManager";

    private static final Logger LOG = LoggerFactory.getLogger(HelpRequestHandler.class);

    private final HandledHelpRequests handledRequests;
    private final HelpAdvisor advisor;
    private final HandledRequestRecorder recorder;
    private final Clock clock;

    public HelpRequestHandler(HandledHelpRequests handledRequests, HelpAdvisor advisor, HandledRequestRecorder recorder,
                              Clock clock) {
        this.handledRequests = handledRequests;
        this.advisor = advisor;
        this.recorder = recorder;
        this.clock = clock;
    }

    /**
     * @param correlationId correlation id of the incoming request, carried on to the Help
     * @throws AdvisorUnavailableException if the advisor cannot be reached (transient, worth a retry)
     */
    public Handling handle(HelpRequest request, UUID correlationId) {
        Objects.requireNonNull(correlationId, "correlationId");
        Optional<HandledHelpRequest> before = handledRequests.find(request.id());
        if (before.isPresent()) {
            return new Handling(before.get(), false);
        }
        Decision decision = consider(request);
        boolean firstTime = recorder.record(decision.handled(), decision.help(), correlationId);
        return new Handling(decision.handled(), firstTime);
    }

    private Decision consider(HelpRequest request) {
        if (!request.isForGrandma()) {
            return notAnswered(request, Outcome.NOT_FOR_GRANDMA);
        }
        Optional<Advice> advice;
        Help help;
        try {
            advice = advisor.advise(request);
            if (advice.isEmpty()) {
                return notAnswered(request, Outcome.NO_ADVICE);
            }
            help = Help.answer(request, advice.get());
        } catch (InvalidAnswerException e) {
            LOG.warn("Advice for help request {} discarded: {}", request.id().value(), e.getMessage());
            return notAnswered(request, Outcome.ADVICE_REJECTED);
        }
        return new Decision(HandledHelpRequest.answered(help, clock.instant()), Optional.of(help));
    }

    private Decision notAnswered(HelpRequest request, Outcome outcome) {
        return new Decision(HandledHelpRequest.notAnswered(request, outcome, clock.instant()), Optional.empty());
    }

    private record Decision(HandledHelpRequest handled, Optional<Help> help) {
    }

    /**
     * @param handled   what Grandma decided
     * @param firstTime {@code false} for a duplicate - then nothing was written or published, and
     *                  {@code handled} is the earlier decision (or, after a lost race, the one discarded)
     */
    public record Handling(HandledHelpRequest handled, boolean firstTime) {
    }
}
