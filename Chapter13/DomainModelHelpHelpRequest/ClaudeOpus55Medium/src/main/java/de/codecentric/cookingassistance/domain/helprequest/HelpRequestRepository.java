package de.codecentric.cookingassistance.domain.helprequest;

import de.codecentric.cookingassistance.domain.shared.HelpRequestId;

import java.util.Optional;

/** Repository for the Help Request aggregate. Implemented outside the domain layer. */
public interface HelpRequestRepository {

    Optional<HelpRequest> findById(HelpRequestId helpRequestId);

    void save(HelpRequest helpRequest);
}
