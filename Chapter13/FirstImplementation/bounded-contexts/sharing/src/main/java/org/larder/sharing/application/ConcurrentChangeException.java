package org.larder.sharing.application;

import org.larder.sharing.domain.ThanksId;

/** The thanks were changed by another request between reading and storing them; nothing is stored. */
public class ConcurrentChangeException extends RuntimeException {

    public ConcurrentChangeException(ThanksId id) {
        super("Thanks " + id.value() + " were changed meanwhile; read them again and repeat the change");
    }
}
