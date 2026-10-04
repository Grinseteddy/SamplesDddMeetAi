package org.larder.cookingassistance.application;

import java.util.List;
import java.util.Optional;

import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpType;

/** Port: the helps given to help requests. */
public interface HelpRepository {

    void save(Help help);

    Optional<Help> findById(HelpId id);

    boolean exists(HelpId id);

    boolean existsFor(HelpRequestId helpRequest);

    /** {@code null} filters are ignored; oldest first. {@code type} is the type of the answer. */
    List<Help> search(HelpRequestId helpRequest, HelpType type, CookId helpProvider);

    void delete(HelpId id);
}
