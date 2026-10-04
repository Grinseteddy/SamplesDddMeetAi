package org.larder.cookingassistance.application;

import java.util.List;
import java.util.Optional;

import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestStatus;

/** Port: the help requests raised on Larder. */
public interface HelpRequestRepository {

    void save(HelpRequest request);

    Optional<HelpRequest> findById(HelpRequestId id);

    /** Like {@link #findById}, but locks the request until the end of the transaction (status changes). */
    Optional<HelpRequest> findByIdForUpdate(HelpRequestId id);

    /** {@code null} filters are ignored; oldest first. */
    List<HelpRequest> search(CookId requester, HelpRequestStatus status);

    void delete(HelpRequestId id);
}
