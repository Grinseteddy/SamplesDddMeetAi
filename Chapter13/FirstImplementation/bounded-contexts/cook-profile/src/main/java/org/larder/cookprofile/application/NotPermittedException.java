package org.larder.cookprofile.application;

/** The caller acts on a cook profile that is not their own. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
