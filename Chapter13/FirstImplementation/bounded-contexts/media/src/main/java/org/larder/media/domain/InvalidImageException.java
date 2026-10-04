package org.larder.media.domain;

/** The uploaded content is not an acceptable image. */
public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String message) {
        super(message);
    }
}
