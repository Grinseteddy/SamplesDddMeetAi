package org.larder.grandmaavatar.application;

import java.util.UUID;

import org.larder.grandmaavatar.domain.Help;

/**
 * Port announcing Grandma's Help ({@code HelpProvided}). Must take part in the caller's transaction
 * (transactional outbox): the Help is published if and only if its handling was committed.
 */
public interface HelpPublisher {

    /** @param correlationId the correlation id of the help request, so the journey can be traced */
    void helpProvided(Help help, UUID correlationId);
}
