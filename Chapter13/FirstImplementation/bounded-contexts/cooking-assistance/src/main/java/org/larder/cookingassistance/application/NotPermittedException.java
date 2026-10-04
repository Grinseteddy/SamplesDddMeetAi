package org.larder.cookingassistance.application;

/** The caller acts on a help request or help that is not theirs, or reads a chef's help meant for somebody else. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
