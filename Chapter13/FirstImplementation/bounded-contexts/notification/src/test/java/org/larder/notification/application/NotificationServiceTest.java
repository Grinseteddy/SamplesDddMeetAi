package org.larder.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP;
import static org.larder.notification.TestData.HELP_LINK;
import static org.larder.notification.TestData.HELP_REQUEST;
import static org.larder.notification.TestData.NOW;
import static org.larder.notification.TestData.OTHER_COOK;
import static org.larder.notification.TestData.STAY_CALM_MESSAGE;
import static org.larder.notification.TestData.newEvent;

import java.net.URI;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.notification.domain.HelpProvider;
import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.Status;

class NotificationServiceTest {

    private final InMemoryNotifications notifications = new InMemoryNotifications();
    private final NotificationService service = new NotificationService(notifications,
            helpId -> URI.create("https://larder.org/cooking-assistance/helps/" + helpId), Clock.fixed(NOW, ZoneOffset.UTC));

    private static HelpProvided stayCalm() {
        return new HelpProvided(STAY_CALM_MESSAGE, HELP, COOK, "Stay calm", HelpProvider.GRANDMA_AVATAR);
    }

    private Notification notifiedCook() {
        service.helpProvided(stayCalm());
        return notifications.all().getFirst();
    }

    @Test
    void helpProvidedCreatesOneNewNotificationForTheRequesterLinkingToTheHelp() {
        assertThat(service.helpProvided(stayCalm())).isTrue();

        assertThat(notifications.all()).singleElement().satisfies(notification -> {
            assertThat(notification.receivers()).containsExactly(COOK);
            assertThat(notification.link()).isEqualTo(HELP_LINK);
            assertThat(notification.statusFor(COOK)).isEqualTo(Status.NEW);
            assertThat(notification.createdAt()).isEqualTo(NOW);
        });
    }

    @Test
    void aRedeliveredHelpProvidedCreatesNoSecondNotification() {
        service.helpProvided(stayCalm());

        assertThat(service.helpProvided(stayCalm())).isFalse();
        assertThat(notifications.all()).hasSize(1);
    }

    @Test
    void helpRequestedCreatesNoNotification() {
        service.helpRequested(new HelpRequested(newEvent(), HELP_REQUEST, COOK,
                List.of(HelpProvider.GRANDMA_AVATAR, HelpProvider.COMMUNITY)));

        assertThat(notifications.all()).isEmpty();
    }

    @Test
    void incompleteEventsAreInvalid() {
        assertThatThrownBy(() -> new HelpProvided(newEvent(), HELP, null, "Stay calm", HelpProvider.CHEF))
                .isInstanceOf(InvalidEventException.class).hasMessageContaining("helpRequester");
        assertThatThrownBy(() -> new HelpProvided(newEvent(), HELP, COOK, " ", HelpProvider.CHEF))
                .isInstanceOf(InvalidEventException.class).hasMessageContaining("answerTitle");
        assertThatThrownBy(() -> new HelpRequested(newEvent(), HELP_REQUEST, COOK, List.of()))
                .isInstanceOf(InvalidEventException.class);
    }

    @Test
    void aCookListsTheirOwnNotificationsOptionallyByStatus() {
        Notification notification = notifiedCook();
        service.helpProvided(new HelpProvided(newEvent(), UUID.randomUUID(), OTHER_COOK, "Use a cold pan", HelpProvider.COMMUNITY));

        assertThat(service.notificationsOf(COOK, Optional.empty(), Optional.empty()))
                .extracting(Notification::id).containsExactly(notification.id());
        assertThat(service.notificationsOf(COOK, Optional.of(COOK), Optional.of(Status.NEW))).hasSize(1);
        assertThat(service.notificationsOf(COOK, Optional.empty(), Optional.of(Status.READ))).isEmpty();
    }

    @Test
    void aCookCannotListSomebodyElsesNotifications() {
        assertThatThrownBy(() -> service.notificationsOf(COOK, Optional.of(OTHER_COOK), Optional.empty()))
                .isInstanceOf(NotPermittedException.class);
    }

    @Test
    void onlyAReceiverReadsMarksAndDeletes() {
        NotificationId id = notifiedCook().id();

        assertThatThrownBy(() -> service.notification(OTHER_COOK, id)).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.changeStatus(OTHER_COOK, id, Status.READ)).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.delete(OTHER_COOK, id)).isInstanceOf(NotPermittedException.class);
        assertThat(service.notification(COOK, id).statusFor(COOK)).isEqualTo(Status.NEW);
    }

    @Test
    void markingAsReadIsStored() {
        NotificationId id = notifiedCook().id();

        service.changeStatus(COOK, id, Status.READ);

        assertThat(service.notification(COOK, id).statusFor(COOK)).isEqualTo(Status.READ);
        assertThat(service.notificationsOf(COOK, Optional.empty(), Optional.of(Status.READ))).hasSize(1);
        assertThatCode(() -> service.changeStatus(COOK, id, Status.READ)).doesNotThrowAnyException();
    }

    @Test
    void aDeletedNotificationIsGoneForTheReceiver() {
        NotificationId id = notifiedCook().id();

        service.delete(COOK, id);

        assertThat(service.notificationsOf(COOK, Optional.empty(), Optional.empty())).isEmpty();
        assertThatThrownBy(() -> service.notification(COOK, id)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.changeStatus(COOK, id, Status.READ)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(COOK, id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void aDeletedNotificationDoesNotComeBackWithARedelivery() {
        NotificationId id = notifiedCook().id();
        service.delete(COOK, id);

        assertThat(service.helpProvided(stayCalm())).isFalse();
        assertThat(service.notificationsOf(COOK, Optional.empty(), Optional.empty())).isEmpty();
    }

    @Test
    void unknownNotificationsAreNotFound() {
        NotificationId unknown = NotificationId.newId();

        assertThatThrownBy(() -> service.notification(COOK, unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.changeStatus(COOK, unknown, Status.READ)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(COOK, unknown)).isInstanceOf(NotFoundException.class);
    }
}
