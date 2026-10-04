package org.larder.grandmaavatar.application;

import java.util.Optional;

import org.larder.grandmaavatar.domain.HandledHelpRequest;
import org.larder.grandmaavatar.domain.HelpRequestId;

/** Grandma's notebook of handled help requests (own schema). */
public interface HandledHelpRequests {

    Optional<HandledHelpRequest> find(HelpRequestId helpRequest);

    /**
     * Notes a handled request unless one for the same request is already there.
     *
     * @return {@code false} if the request had already been handled - nothing was written
     */
    boolean add(HandledHelpRequest handled);
}
