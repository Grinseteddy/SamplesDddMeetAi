package org.larder.grandmaavatar.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.larder.grandmaavatar.domain.HandledHelpRequest;
import org.larder.grandmaavatar.domain.HelpRequestId;

/** Grandma's notebook in memory. */
public class InMemoryHandledHelpRequests implements HandledHelpRequests {

    private final Map<HelpRequestId, HandledHelpRequest> handled = new HashMap<>();
    /** Simulates a concurrent duplicate: the check finds nothing, the insert loses. */
    boolean hideOnFind;

    @Override
    public Optional<HandledHelpRequest> find(HelpRequestId helpRequest) {
        return hideOnFind ? Optional.empty() : Optional.ofNullable(handled.get(helpRequest));
    }

    @Override
    public boolean add(HandledHelpRequest request) {
        return handled.putIfAbsent(request.helpRequest(), request) == null;
    }

    public int size() {
        return handled.size();
    }
}
