package org.larder.notification.adapter.in.web;

import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP_LINK;
import static org.larder.notification.TestData.OTHER_COOK;
import static org.larder.notification.TestData.stayCalm;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.notification.application.NotFoundException;
import org.larder.notification.application.NotPermittedException;
import org.larder.notification.application.NotificationService;
import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.Status;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({NotificationsController.class, NotificationErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class NotificationsControllerTest {

    private static final String NOTIFICATIONS = "/notifications/notifications";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NotificationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    @Test
    void listsTheCallersNotificationsInTheContractShape() throws Exception {
        Notification notification = stayCalm();
        given(service.notificationsOf(COOK, Optional.empty(), Optional.empty())).willReturn(List.of(notification));

        mvc.perform(get(NOTIFICATIONS).header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications[0].notificationId").value(notification.id().value().toString()))
                .andExpect(jsonPath("$.notifications[0].receivers[0]").value(COOK.value().toString()))
                .andExpect(jsonPath("$.notifications[0].title").value("Help provided by the Grandma Avatar"))
                .andExpect(jsonPath("$.notifications[0].text").value(notification.text()))
                .andExpect(jsonPath("$.notifications[0].link").value(HELP_LINK.toString()))
                .andExpect(jsonPath("$.notifications[0].status").value("NEW"));
    }

    @Test
    void passesReceiverAndStatusFilter() throws Exception {
        given(service.notificationsOf(COOK, Optional.of(COOK), Optional.of(Status.READ))).willReturn(List.of());

        mvc.perform(get(NOTIFICATIONS).param("receiver", COOK.value().toString()).param("status", "READ")
                        .header("version", "1.0.0").with(cook("notifications:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isEmpty());
    }

    @Test
    void listingSomebodyElsesNotificationsIsForbidden() throws Exception {
        given(service.notificationsOf(COOK, Optional.of(OTHER_COOK), Optional.empty()))
                .willThrow(new NotPermittedException("not yours"));

        mvc.perform(get(NOTIFICATIONS).param("receiver", OTHER_COOK.value().toString())
                        .header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void getsOneNotificationWithTheCallersStatus() throws Exception {
        Notification notification = stayCalm();
        notification.changeStatus(COOK, Status.READ);
        given(service.notification(COOK, notification.id())).willReturn(notification);

        mvc.perform(get(NOTIFICATIONS + "/" + notification.id().value()).header("version", "1.0.0")
                        .with(cook("notifications:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"));
    }

    @Test
    void notAReceiverIsForbiddenAndUnknownIsNotFound() throws Exception {
        NotificationId foreign = NotificationId.newId();
        NotificationId unknown = NotificationId.newId();
        given(service.notification(COOK, foreign)).willThrow(new NotPermittedException("not a Receiver"));
        given(service.notification(COOK, unknown)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(NOTIFICATIONS + "/" + foreign.value()).header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isForbidden());
        mvc.perform(get(NOTIFICATIONS + "/" + unknown.value()).header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void marksAsReadAndLinksToTheNotification() throws Exception {
        Notification notification = stayCalm();
        given(service.changeStatus(COOK, notification.id(), Status.READ)).willReturn(notification);

        mvc.perform(put(NOTIFICATIONS + "/" + notification.id().value() + "/status").header("version", "1.0.0")
                        .with(cook("notifications:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"READ\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationLink")
                        .value("http://localhost" + NOTIFICATIONS + "/" + notification.id().value()));
    }

    @Test
    void anInvalidStatusIsABadRequest() throws Exception {
        mvc.perform(put(NOTIFICATIONS + "/" + NotificationId.newId().value() + "/status").header("version", "1.0.0")
                        .with(cook("notifications:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ARCHIVED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(put(NOTIFICATIONS + "/" + NotificationId.newId().value() + "/status").header("version", "1.0.0")
                        .with(cook("notifications:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        then(service).should(never()).changeStatus(any(), any(), any());
    }

    @Test
    void deletesForTheCaller() throws Exception {
        NotificationId id = NotificationId.newId();

        mvc.perform(delete(NOTIFICATIONS + "/" + id.value()).header("version", "1.0.0").with(cook("notifications:write")))
                .andExpect(status().isNoContent());

        then(service).should().delete(COOK, id);
    }

    @Test
    void deletingAnAlreadyDeletedNotificationIsNotFound() throws Exception {
        NotificationId id = NotificationId.newId();
        willThrow(new NotFoundException("deleted")).given(service).delete(COOK, id);

        mvc.perform(delete(NOTIFICATIONS + "/" + id.value()).header("version", "1.0.0").with(cook("notifications:write")))
                .andExpect(status().isNotFound());
    }

    @Test
    void writingNeedsTheWriteScope() throws Exception {
        NotificationId id = NotificationId.newId();

        mvc.perform(delete(NOTIFICATIONS + "/" + id.value()).header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isForbidden());
        mvc.perform(put(NOTIFICATIONS + "/" + id.value() + "/status").header("version", "1.0.0")
                        .with(cook("notifications:read"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"READ\"}"))
                .andExpect(status().isForbidden());
        then(service).shouldHaveNoInteractions();
    }

    @Test
    void readingNeedsATokenAndAScope() throws Exception {
        mvc.perform(get(NOTIFICATIONS).header("version", "1.0.0"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(NOTIFICATIONS).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void notificationsCannotBeCreatedThroughTheApi() throws Exception {
        mvc.perform(post(NOTIFICATIONS).header("version", "1.0.0").with(cook("notifications:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is4xxClientError());
        then(service).shouldHaveNoInteractions();
    }

    @Test
    void malformedRequestsAreBadRequests() throws Exception {
        mvc.perform(get(NOTIFICATIONS + "/not-a-uuid").header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isBadRequest());
        mvc.perform(get(NOTIFICATIONS).param("status", "UNREAD").header("version", "1.0.0").with(cook("notifications:read")))
                .andExpect(status().isBadRequest());
    }
}
