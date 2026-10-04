package org.larder.sharing.application;

/** An upstream context (Cooking Assistance, Media, Consent Management) cannot answer; the contract's 500. */
public class UpstreamUnavailableException extends RuntimeException {

    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
