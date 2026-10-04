package org.larder.notification.domain;

/** Somebody who is not a Receiver of a notification tries to read, mark or delete it. */
public class NotAReceiverException extends RuntimeException {

    public NotAReceiverException(NotificationId id, ReceiverId receiver) {
        super("Cook " + receiver.value() + " is not a Receiver of notification " + id.value());
    }
}
