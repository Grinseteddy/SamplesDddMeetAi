package org.larder.notification.application;

/** An incoming event lacks what Notification needs; it can never be processed, retrying is useless. */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}
