package org.larder.media.application;

/** The caller acts on a media that is not their own. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
