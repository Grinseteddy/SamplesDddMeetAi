package org.larder.sharing.application;

import java.util.List;
import java.util.Optional;

import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.Thanks;
import org.larder.sharing.domain.ThanksId;

/** Port: the thanks given on Larder. Each write is atomic on its own. */
public interface ThanksRepository {

    /**
     * Stores new thanks; {@link org.larder.sharing.domain.ThanksRuleViolationException} with
     * {@code THANKS_ALREADY_GIVEN} when thanks for the same help exist.
     */
    void add(Thanks thanks);

    /** Stores changed thanks onto the version they were read in; {@link ConcurrentChangeException} otherwise. */
    void update(Thanks thanks);

    /** {@code false} when there were no such thanks. */
    boolean remove(ThanksId id);

    Optional<Thanks> findById(ThanksId id);

    boolean existsForHelp(HelpId help);

    /** Newest first. */
    List<Thanks> find(ThanksFilter filter);
}
