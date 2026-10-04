package org.larder.notification.adapter.in.web;

import java.net.URI;

import org.larder.notification.adapter.in.web.model.Notification;
import org.larder.notification.adapter.in.web.model.Status;
import org.larder.notification.domain.ReceiverId;
import org.larder.platform.security.CurrentCook;

/** Translates between the contract's model and the domain model. */
final class NotificationMapper {

    private static final String STATUS_SEGMENT = "/status";

    private NotificationMapper() {
    }

    /** The notification as {@code viewer} sees it: their own read status. */
    static Notification toApi(org.larder.notification.domain.Notification notification, ReceiverId viewer) {
        return new Notification(
                notification.id().value(),
                notification.receivers().stream().map(ReceiverId::value).toList(),
                notification.title(),
                notification.text(),
                notification.link(),
                toApi(notification.statusFor(viewer)));
    }

    static Status toApi(org.larder.notification.domain.Status status) {
        return Status.valueOf(status.name());
    }

    static org.larder.notification.domain.Status toDomain(Status status) {
        return org.larder.notification.domain.Status.valueOf(status.name());
    }

    static ReceiverId caller() {
        return new ReceiverId(CurrentCook.require().value());
    }

    /** {@code .../notifications/{id}/status} → {@code .../notifications/{id}}: the changed notification. */
    static URI notificationOf(URI statusLink) {
        String link = statusLink.toString();
        return link.endsWith(STATUS_SEGMENT)
                ? URI.create(link.substring(0, link.length() - STATUS_SEGMENT.length()))
                : statusLink;
    }
}
