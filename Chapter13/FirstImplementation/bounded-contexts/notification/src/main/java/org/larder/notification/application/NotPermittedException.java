package org.larder.notification.application;

/** The caller is not a Receiver of the notification, or asks for somebody else's notifications. */
public class NotPermittedException extends RuntimeException {

    public NotPermittedException(String message) {
        super(message);
    }
}
