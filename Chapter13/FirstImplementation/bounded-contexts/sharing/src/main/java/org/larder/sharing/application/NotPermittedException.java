package org.larder.sharing.application;

/** The caller acts on thanks or a help that are not their own, or an upstream refuses the caller. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
