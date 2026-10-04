package org.larder.notification.application;

import java.util.List;
import java.util.Optional;

import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.ReceiverId;
import org.larder.notification.domain.Status;

/** Port: the notifications of Larder. */
public interface NotificationRepository {

    /**
     * Stores a new notification unless one was already created from the same event
     * ({@link Notification#origin()}); returns whether it was stored. Atomic, also under concurrent
     * redeliveries of the same event.
     */
    boolean addIfNew(Notification notification);

    /** Stores the changed deliveries (status, deletion) of an existing notification. */
    void save(Notification notification);

    Optional<Notification> findById(NotificationId id);

    /** Like {@link #findById}, locking the notification until the end of the transaction. */
    Optional<Notification> findByIdForUpdate(NotificationId id);

    /** The notifications {@code receiver} has not deleted, newest first; optionally only in their {@code status}. */
    List<Notification> findVisibleTo(ReceiverId receiver, Optional<Status> status);
}
