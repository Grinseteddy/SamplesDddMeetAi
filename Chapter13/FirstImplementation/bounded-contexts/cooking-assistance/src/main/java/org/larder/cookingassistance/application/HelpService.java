package org.larder.cookingassistance.application;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.cookingassistance.domain.HelpType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of helps. Chefs and the community answer over REST, the Grandma Avatar by message - both
 * through {@link #answer}: the request checks the help, becomes Answered, and HelpProvided is
 * queued in the same transaction (outbox). Only the provider changes or withdraws a help; a chef's
 * help is seen only by the cook who raised the request.
 */
@Service
public class HelpService {

    private final HelpRequestRepository requests;
    private final HelpRepository helps;
    private final HelpEvents events;
    private final Clock clock;

    public HelpService(HelpRequestRepository requests, HelpRepository helps, HelpEvents events, Clock clock) {
        this.requests = requests;
        this.helps = helps;
        this.events = events;
        this.clock = clock;
    }

    /** {@code caller} answers a request as a chef or as the community; the journey is traced by the request id. */
    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public Help give(CookId caller, HelpRequestId helpRequest, HelpProviderType providerType, String answerTitle,
                     Answer answer) {
        HelpRequest request = requests.findByIdForUpdate(helpRequest)
                .orElseThrow(() -> new UnknownHelpRequestException(helpRequest));
        return answer(request, HelpId.newId(), providerType, caller, answerTitle, answer, helpRequest.value());
    }

    /**
     * Records the Grandma Avatar's help. Idempotent: a help received before is recognised by its id and
     * changes nothing. A help for an unknown request raises {@link NotFoundException}, one that does not
     * fit the request {@link HelpRuleViolationException}.
     *
     * @param correlationId of the incoming message - the journey goes on under the same id
     */
    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public GrandmaHelpOutcome receiveGrandmaHelp(GrandmaHelp grandmaHelp, UUID correlationId) {
        HelpRequest request = requests.findByIdForUpdate(grandmaHelp.helpRequest()).orElseThrow(
                () -> new NotFoundException("Help request " + grandmaHelp.helpRequest().value() + " not found"));
        // checked under the request's lock, so a concurrent redelivery sees the committed first one
        if (helps.exists(grandmaHelp.helpId())) {
            return GrandmaHelpOutcome.DUPLICATE;
        }
        if (!request.isRaisedBy(grandmaHelp.helpRequester())) {
            throw new HelpRuleViolationException(HelpRuleViolationException.ANSWER_REFERENCE_MISMATCH,
                    "Help request " + request.id().value() + " was not raised by cook "
                            + grandmaHelp.helpRequester().value());
        }
        answer(request, grandmaHelp.helpId(), HelpProviderType.GRANDMA_AVATAR, null, grandmaHelp.answerTitle(),
                grandmaHelp.answer(), correlationId);
        return GrandmaHelpOutcome.RECORDED;
    }

    private Help answer(HelpRequest request, HelpId helpId, HelpProviderType providerType, CookId helpProvider,
                        String answerTitle, Answer answer, UUID correlationId) {
        Help help = request.answer(helpId, providerType, helpProvider, answerTitle, answer, clock.instant());
        requests.save(request);
        helps.save(help);
        events.helpProvided(help, correlationId);
        return help;
    }

    /** {@code null} leaves title or answer as they are. */
    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public Help revise(CookId caller, HelpId id, String answerTitle, Answer answer) {
        Help help = ownHelp(caller, id);
        HelpRequest request = requests.findById(help.helpRequest())
                .orElseThrow(() -> new IllegalStateException("Help " + id.value() + " answers no stored request"));
        help.revise(answerTitle, answer, request, clock.instant());
        helps.save(help);
        return help;
    }

    /** Withdrawing the last help of a request opens the request again. */
    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public void withdraw(CookId caller, HelpId id) {
        Help help = ownHelp(caller, id);
        HelpRequest request = requests.findByIdForUpdate(help.helpRequest())
                .orElseThrow(() -> new IllegalStateException("Help " + id.value() + " answers no stored request"));
        helps.delete(id);
        request.helpWithdrawn(helps.existsFor(request.id()), clock.instant());
        requests.save(request);
    }

    public Help help(CookId caller, HelpId id) {
        Help help = existingHelp(id);
        if (!help.isVisibleTo(caller)) {
            throw new NotPermittedException("A chef's help is visible only to the cook who asked for it");
        }
        return help;
    }

    /** {@code null} filters are ignored; chefs' helps for other cooks' requests are left out. */
    public List<Help> helps(CookId caller, HelpRequestId helpRequest, HelpType type, CookId helpProvider) {
        return helps.search(helpRequest, type, helpProvider).stream().filter(help -> help.isVisibleTo(caller)).toList();
    }

    /** Unknown helps are 404 before ownership is checked (403); the avatar's helps belong to nobody. */
    private Help ownHelp(CookId caller, HelpId id) {
        Help help = existingHelp(id);
        if (!help.isProvidedBy(caller)) {
            throw new NotPermittedException("Only the provider of help " + id.value() + " can change it");
        }
        return help;
    }

    private Help existingHelp(HelpId id) {
        return helps.findById(id).orElseThrow(() -> new NotFoundException("Help " + id.value() + " not found"));
    }
}
