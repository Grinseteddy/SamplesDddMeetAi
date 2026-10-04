package org.larder.cookingassistance.application;

import org.larder.cookingassistance.domain.HelpRequestId;

/** A help refers to a help request that does not exist (a broken request body, not a missing resource). */
public class UnknownHelpRequestException extends RuntimeException {

    public UnknownHelpRequestException(HelpRequestId id) {
        super("Help request " + id.value() + " does not exist");
    }
}
