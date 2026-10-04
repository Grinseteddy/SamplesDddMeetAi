package org.larder.cookingassistance.application;

import java.time.Clock;
import java.util.List;

import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestDraft;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestRevision;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of help requests. A cook raises, changes and withdraws only their own requests; every
 * authenticated caller reads them (help providers look for open requests). Raising a request
 * publishes HelpRequested in the same transaction (outbox).
 */
@Service
public class HelpRequestService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "cookingassistanceTransactionManager";

    private final HelpRequestRepository requests;
    private final HelpRepository helps;
    private final HelpEvents events;
    private final Clock clock;

    public HelpRequestService(HelpRequestRepository requests, HelpRepository helps, HelpEvents events, Clock clock) {
        this.requests = requests;
        this.helps = helps;
        this.events = events;
        this.clock = clock;
    }

    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public HelpRequest raise(CookId caller, HelpRequestDraft draft) {
        HelpRequest request = HelpRequest.raise(caller, draft, clock.instant());
        requests.save(request);
        events.helpRequested(request);
        return request;
    }

    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public HelpRequest revise(CookId caller, HelpRequestId id, HelpRequestRevision revision) {
        HelpRequest request = ownRequest(caller, id);
        request.revise(revision, helps.existsFor(id), clock.instant());
        requests.save(request);
        return request;
    }

    @Transactional(transactionManager = HelpRequestService.TRANSACTIONS)
    public void withdraw(CookId caller, HelpRequestId id) {
        HelpRequest request = ownRequest(caller, id);
        request.checkCanBeWithdrawn();
        requests.delete(id);
    }

    public HelpRequest helpRequest(HelpRequestId id) {
        return requests.findById(id).orElseThrow(() -> notFound(id));
    }

    /** {@code null} filters are ignored. */
    public List<HelpRequest> helpRequests(CookId requester, HelpRequestStatus status) {
        return requests.search(requester, status);
    }

    /** Unknown requests are 404 before ownership is checked (403); locked against concurrent helps. */
    private HelpRequest ownRequest(CookId caller, HelpRequestId id) {
        HelpRequest request = requests.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        if (!request.isRaisedBy(caller)) {
            throw new NotPermittedException("Only the requester of help request " + id.value() + " can change it");
        }
        return request;
    }

    private static NotFoundException notFound(HelpRequestId id) {
        return new NotFoundException("Help request " + id.value() + " not found");
    }
}
