package org.larder.notification.domain;

/** The Receiver already deleted the notification; for them it no longer exists. */
public class NotificationDeletedException extends RuntimeException {

    public NotificationDeletedException(NotificationId id) {
        super("Notification " + id.value() + " not found");
    }
}
