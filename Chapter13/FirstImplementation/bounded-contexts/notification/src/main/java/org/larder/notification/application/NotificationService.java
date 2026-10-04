package org.larder.notification.application;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.ReceiverId;
import org.larder.notification.domain.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases of Notification. Notifications are created only from Cooking Assistance's events;
 * Cooks list, read, mark and delete the notifications of which they are a Receiver - each Receiver
 * for themselves. A Cook who is not a Receiver gets {@link NotPermittedException}; an unknown
 * notification, or one the caller deleted, is {@link NotFoundException}.
 */
@Service
public class NotificationService {

    /** Transaction manager of this context's own schema. */
    public static final String TRANSACTIONS = "notificationTransactionManager";

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final HelpLinks helpLinks;
    private final Clock clock;

    public NotificationService(NotificationRepository notifications, HelpLinks helpLinks, Clock clock) {
        this.notifications = notifications;
        this.helpLinks = helpLinks;
        this.clock = clock;
    }

    /**
     * Notifies the help requester that help was provided. Idempotent: a redelivered event creates no
     * second notification. Returns whether a notification was created.
     */
    @Transactional(transactionManager = NotificationService.TRANSACTIONS)
    public boolean helpProvided(HelpProvided event) {
        Notification notification = Notification.helpProvided(event.eventId(), event.helpRequester(),
                event.answerTitle(), event.helpProviderType(), helpLinks.helpLink(event.helpId()), clock.instant());
        boolean created = notifications.addIfNew(notification);
        if (!created) {
            LOG.info("HelpProvided {} was already turned into a notification, ignoring the redelivery",
                    event.eventId().value());
        }
        return created;
    }

    /**
     * Creates no notification - on purpose. The AsyncAPI asks for "a notification for each preferred
     * provider group (Chef, Community, Grandma Avatar)", but a Receiver is the id of a Cook, and
     * Notification knows no members of these groups (the Grandma Avatar is no Cook at all, and it
     * consumes HelpRequested itself). Until the contracts say who the Receivers are, the event is
     * accepted and acknowledged without a notification.
     */
    public void helpRequested(HelpRequested event) {
        LOG.info("HelpRequested {} for help request {} (preferred providers {}): no notification, "
                        + "provider groups are no Receivers", event.eventId().value(), event.helpRequestId(),
                event.preferredProviders());
    }

    /**
     * The notifications of {@code receiver} the caller may see. {@code receiver} defaults to the caller;
     * a Cook may only list their own notifications.
     */
    public List<Notification> notificationsOf(ReceiverId caller, Optional<ReceiverId> receiver,
                                              Optional<Status> status) {
        ReceiverId addressee = receiver.orElse(caller);
        if (!addressee.equals(caller)) {
            throw new NotPermittedException("A cook may only list notifications of which they are a Receiver");
        }
        return notifications.findVisibleTo(caller, status);
    }

    public Notification notification(ReceiverId caller, NotificationId id) {
        return visibleTo(caller, notifications.findById(id), id);
    }

    @Transactional(transactionManager = NotificationService.TRANSACTIONS)
    public Notification changeStatus(ReceiverId caller, NotificationId id, Status status) {
        Notification notification = visibleTo(caller, notifications.findByIdForUpdate(id), id);
        notification.changeStatus(caller, status);
        notifications.save(notification);
        return notification;
    }

    @Transactional(transactionManager = NotificationService.TRANSACTIONS)
    public void delete(ReceiverId caller, NotificationId id) {
        Notification notification = visibleTo(caller, notifications.findByIdForUpdate(id), id);
        notification.deleteFor(caller, clock.instant());
        notifications.save(notification);
    }

    private static Notification visibleTo(ReceiverId caller, Optional<Notification> found, NotificationId id) {
        Notification notification = found.orElseThrow(
                () -> new NotFoundException("Notification " + id.value() + " not found"));
        if (!notification.isReceiver(caller)) {
            throw new NotPermittedException("Only a Receiver of notification " + id.value() + " may use it");
        }
        if (!notification.isVisibleTo(caller)) {
            throw new NotFoundException("Notification " + id.value() + " not found");
        }
        return notification;
    }
}
