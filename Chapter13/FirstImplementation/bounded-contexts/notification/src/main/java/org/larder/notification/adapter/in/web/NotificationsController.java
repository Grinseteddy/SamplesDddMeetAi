package org.larder.notification.adapter.in.web;

import static org.larder.notification.adapter.in.web.NotificationMapper.caller;

import java.util.Optional;
import java.util.UUID;

import org.larder.notification.adapter.in.web.api.NotificationsApi;
import org.larder.notification.adapter.in.web.model.Notification;
import org.larder.notification.adapter.in.web.model.NotificationLink;
import org.larder.notification.adapter.in.web.model.Notifications;
import org.larder.notification.adapter.in.web.model.Status;
import org.larder.notification.adapter.in.web.model.StatusUpdate;
import org.larder.notification.application.NotificationService;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.ReceiverId;
import org.larder.platform.web.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for {@code contracts/openapi/notifications.openapi.yaml}, tag Notifications.
 * Notifications are created only from events - the contract has no operation to create one.
 */
@RestController("notificationNotificationsController")
@RequestMapping("/notifications")
class NotificationsController implements NotificationsApi {

    private final NotificationService service;

    NotificationsController(NotificationService service) {
        this.service = service;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_notifications:read', 'SCOPE_notifications:write')")
    public ResponseEntity<Notifications> listNotifications(String version, UUID receiver, Status status) {
        ReceiverId caller = caller();
        var found = service.notificationsOf(caller, Optional.ofNullable(receiver).map(ReceiverId::new),
                Optional.ofNullable(status).map(NotificationMapper::toDomain));
        return ResponseEntity.ok(new Notifications(found.stream().map(n -> NotificationMapper.toApi(n, caller)).toList()));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('SCOPE_notifications:read', 'SCOPE_notifications:write')")
    public ResponseEntity<Notification> getNotificationById(UUID notificationId, String version) {
        ReceiverId caller = caller();
        return ResponseEntity.ok(NotificationMapper.toApi(service.notification(caller, new NotificationId(notificationId)), caller));
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_notifications:write')")
    public ResponseEntity<Void> deleteNotification(UUID notificationId, String version) {
        service.delete(caller(), new NotificationId(notificationId));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasAuthority('SCOPE_notifications:write')")
    public ResponseEntity<NotificationLink> updateNotificationStatus(UUID notificationId, String version,
                                                                     StatusUpdate statusUpdate) {
        service.changeStatus(caller(), new NotificationId(notificationId), NotificationMapper.toDomain(statusUpdate.getStatus()));
        return ResponseEntity.ok(new NotificationLink(NotificationMapper.notificationOf(Links.current())));
    }
}
