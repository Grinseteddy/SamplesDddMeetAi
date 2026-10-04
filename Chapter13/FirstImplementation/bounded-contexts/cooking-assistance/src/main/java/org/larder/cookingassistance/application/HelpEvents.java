package org.larder.cookingassistance.application;

import java.util.UUID;

import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpRequest;

/**
 * Port: announces what happened to help requests and helps (published language HelpRequested and
 * HelpProvided). Must be called inside the transaction of the change - the adapter writes to the
 * transactional outbox, so a message is published if and only if the change is committed.
 */
public interface HelpEvents {

    /** A cook raised {@code request}; it starts a help journey traced by the request's id. */
    void helpRequested(HelpRequest request);

    /** {@code help} answers its request, which is now Answered. */
    void helpProvided(Help help, UUID correlationId);
}
