package org.larder.notification.domain;

/** Read state of a notification for one Receiver: created as NEW, READ once the Receiver marked it. */
public enum Status {
    NEW,
    READ
}
