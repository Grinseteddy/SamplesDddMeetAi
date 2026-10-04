package org.larder.consentmanagement.application;

/** The caller acts on a consent that is not their own. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
