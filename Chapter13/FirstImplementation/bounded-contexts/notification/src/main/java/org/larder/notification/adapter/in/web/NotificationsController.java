package org.larder.notification.adapter.in.web;

import org.larder.notification.adapter.in.web.api.NotificationsApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter for the Notifications operations of the contract
 * {@code contracts/openapi/notifications.openapi.yaml}. Operations not yet
 * overridden answer 501 Not Implemented.
 */
@RestController("notificationNotificationsController")
@RequestMapping("/notifications")
class NotificationsController implements NotificationsApi {
}
