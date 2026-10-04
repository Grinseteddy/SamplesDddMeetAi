package org.larder.grandmaavatar.domain;

/** A help request breaks an invariant of the published language; Grandma cannot consider it. */
public class InvalidHelpRequestException extends RuntimeException {

    public InvalidHelpRequestException(String message) {
        super(message);
    }
}
