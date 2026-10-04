package org.larder.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP_LINK;
import static org.larder.notification.TestData.NOW;
import static org.larder.notification.TestData.OTHER_COOK;
import static org.larder.notification.TestData.STAY_CALM_MESSAGE;
import static org.larder.notification.TestData.newEvent;
import static org.larder.notification.TestData.stayCalm;

import java.util.List;

import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void helpProvidedNotifiesTheRequesterWithProviderAndAnswerTitle() {
        Notification notification = stayCalm();

        assertThat(notification.receivers()).containsExactly(COOK);
        assertThat(notification.origin()).isEqualTo(STAY_CALM_MESSAGE);
        assertThat(notification.title()).isEqualTo("Help provided by the Grandma Avatar");
        assertThat(notification.text()).isEqualTo(
                "The Grandma Avatar answered your help request: \"Stay calm\". Open the help to see the answer.");
        assertThat(notification.link()).isEqualTo(HELP_LINK);
        assertThat(notification.statusFor(COOK)).isEqualTo(Status.NEW);
    }

    @Test
    void namesEveryKindOfProvider() {
        assertThat(Notification.helpProvided(newEvent(), COOK, "Use yoghurt", HelpProvider.CHEF, HELP_LINK, NOW).title())
                .isEqualTo("Help provided by a Chef");
        assertThat(Notification.helpProvided(newEvent(), COOK, "Use yoghurt", HelpProvider.COMMUNITY, HELP_LINK, NOW).text())
                .startsWith("The Community answered your help request");
    }

    @Test
    void aReceiverMarksOnlyTheirOwnCopyAsRead() {
        Notification notification = Notification.create(newEvent(), List.of(COOK, OTHER_COOK), "Title", "Text", HELP_LINK, NOW);

        notification.changeStatus(COOK, Status.READ);

        assertThat(notification.statusFor(COOK)).isEqualTo(Status.READ);
        assertThat(notification.statusFor(OTHER_COOK)).isEqualTo(Status.NEW);

        notification.changeStatus(COOK, Status.NEW);
        assertThat(notification.statusFor(COOK)).isEqualTo(Status.NEW);
    }

    @Test
    void deletingIsPerReceiverAndIrreversible() {
        Notification notification = Notification.create(newEvent(), List.of(COOK, OTHER_COOK), "Title", "Text", HELP_LINK, NOW);

        notification.deleteFor(COOK, NOW);

        assertThat(notification.isVisibleTo(COOK)).isFalse();
        assertThat(notification.isReceiver(COOK)).isTrue();
        assertThat(notification.isVisibleTo(OTHER_COOK)).isTrue();
        assertThatThrownBy(() -> notification.deleteFor(COOK, NOW)).isInstanceOf(NotificationDeletedException.class);
        assertThatThrownBy(() -> notification.changeStatus(COOK, Status.READ)).isInstanceOf(NotificationDeletedException.class);
        assertThatThrownBy(() -> notification.statusFor(COOK)).isInstanceOf(NotificationDeletedException.class);
    }

    @Test
    void onlyReceiversMayTouchIt() {
        Notification notification = stayCalm();

        assertThat(notification.isReceiver(OTHER_COOK)).isFalse();
        assertThatThrownBy(() -> notification.changeStatus(OTHER_COOK, Status.READ)).isInstanceOf(NotAReceiverException.class);
        assertThatThrownBy(() -> notification.deleteFor(OTHER_COOK, NOW)).isInstanceOf(NotAReceiverException.class);
    }

    @Test
    void needsAReceiverAndNamesEachOnlyOnce() {
        assertThatThrownBy(() -> Notification.create(newEvent(), List.of(), "Title", "Text", HELP_LINK, NOW))
                .isInstanceOf(NotificationRuleViolationException.class);
        assertThatThrownBy(() -> Notification.create(newEvent(), List.of(COOK, COOK), "Title", "Text", HELP_LINK, NOW))
                .isInstanceOf(NotificationRuleViolationException.class);
    }

    @Test
    void titleAndTextFollowTheContractLimits() {
        assertThatThrownBy(() -> Notification.create(newEvent(), List.of(COOK), " ", "Text", HELP_LINK, NOW))
                .isInstanceOf(NotificationRuleViolationException.class);
        assertThatThrownBy(() -> Notification.create(newEvent(), List.of(COOK), "x".repeat(201), "Text", HELP_LINK, NOW))
                .isInstanceOf(NotificationRuleViolationException.class);
        assertThatThrownBy(() -> Notification.create(newEvent(), List.of(COOK), "Title", "x".repeat(2001), HELP_LINK, NOW))
                .isInstanceOf(NotificationRuleViolationException.class);
        assertThat(Notification.helpProvided(newEvent(), COOK, "x".repeat(200), HelpProvider.CHEF, HELP_LINK, NOW).text())
                .hasSizeLessThanOrEqualTo(Notification.TEXT_MAX_LENGTH);
        assertThatThrownBy(() -> Notification.helpProvided(newEvent(), COOK, "x".repeat(201), HelpProvider.CHEF, HELP_LINK, NOW))
                .isInstanceOf(NotificationRuleViolationException.class);
    }
}
