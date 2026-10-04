package org.larder.notification.domain;

/** A notification would break one of its rules, e.g. a title longer than 200 characters. */
public class NotificationRuleViolationException extends RuntimeException {

    public NotificationRuleViolationException(String message) {
        super(message);
    }
}
