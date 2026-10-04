package org.larder.grandmaavatar.application;

import java.util.Optional;
import java.util.UUID;

import org.larder.grandmaavatar.domain.HandledHelpRequest;
import org.larder.grandmaavatar.domain.Help;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Notes a handled request and posts its Help in ONE transaction: either both happen or neither. A
 * request noted before (a concurrent or redelivered duplicate) posts nothing.
 */
@Service
public class HandledRequestRecorder {

    private final HandledHelpRequests handledRequests;
    private final HelpPublisher publisher;

    public HandledRequestRecorder(HandledHelpRequests handledRequests, HelpPublisher publisher) {
        this.handledRequests = handledRequests;
        this.publisher = publisher;
    }

    /** @return {@code false} if the request had already been handled */
    @Transactional(transactionManager = HelpRequestHandler.TRANSACTIONS)
    public boolean record(HandledHelpRequest handled, Optional<Help> help, UUID correlationId) {
        if (!handledRequests.add(handled)) {
            return false;
        }
        help.ifPresent(answer -> publisher.helpProvided(answer, correlationId));
        return true;
    }
}
