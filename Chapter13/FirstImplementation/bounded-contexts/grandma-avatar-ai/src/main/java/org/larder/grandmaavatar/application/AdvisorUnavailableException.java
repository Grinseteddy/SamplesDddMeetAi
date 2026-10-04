package org.larder.grandmaavatar.application;

/** The advisor behind Grandma cannot be reached right now - a transient failure, the request is retried. */
public class AdvisorUnavailableException extends RuntimeException {

    public AdvisorUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
