package org.larder.sharing.application;

import org.larder.sharing.domain.HelpId;

/** Thanks refer to a help Cooking Assistance does not know (a broken request, not a missing resource). */
public class UnknownHelpException extends RuntimeException {

    public UnknownHelpException(HelpId id) {
        super("Help " + id.value() + " does not exist");
    }
}
