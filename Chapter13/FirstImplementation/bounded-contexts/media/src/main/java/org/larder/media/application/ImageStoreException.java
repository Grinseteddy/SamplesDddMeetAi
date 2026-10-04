package org.larder.media.application;

/** The bucket is not reachable or does not hold what the metadata promise. */
public class ImageStoreException extends RuntimeException {

    public ImageStoreException(String message) {
        super(message);
    }

    public ImageStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
