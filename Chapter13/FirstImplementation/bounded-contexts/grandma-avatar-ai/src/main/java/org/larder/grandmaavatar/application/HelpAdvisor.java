package org.larder.grandmaavatar.application;

import java.util.Optional;

import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.HelpRequest;

/**
 * Port to whatever gives Grandma her ideas - today her recipe box, tomorrow an external AI. It speaks
 * Grandma's language only: a {@link HelpRequest} in, an {@link Advice} out. Adapters translate to and
 * from the AI's model; nothing of it leaks past this port (anticorruption layer).
 * <p>
 * Whatever an adapter returns is checked by the domain ({@code Help.answer}) before it is published;
 * an adapter cannot break the invariants of a Help.
 */
public interface HelpAdvisor {

    /**
     * @return advice for the request, or empty if the advisor has nothing sensible to say
     * @throws AdvisorUnavailableException if the advisor cannot be asked right now (worth retrying)
     * @throws org.larder.grandmaavatar.domain.InvalidAnswerException if it can only produce an invalid answer
     */
    Optional<Advice> advise(HelpRequest request);
}
