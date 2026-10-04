package org.larder.media.domain;

/** A link does not identify a business object, or the links of a media break a rule. */
public class InvalidLinkException extends RuntimeException {

    public InvalidLinkException(String message) {
        super(message);
    }
}
