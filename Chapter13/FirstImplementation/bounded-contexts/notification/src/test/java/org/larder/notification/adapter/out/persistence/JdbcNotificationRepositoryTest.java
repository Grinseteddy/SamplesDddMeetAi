package org.larder.notification.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.notification.TestData.COOK;
import static org.larder.notification.TestData.HELP_LINK;
import static org.larder.notification.TestData.NOW;
import static org.larder.notification.TestData.OTHER_COOK;
import static org.larder.notification.TestData.newEvent;
import static org.larder.notification.TestData.stayCalm;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.Status;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.TestDatabase;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcNotificationRepositoryTest {

    private BoundedContextDatabase database;
    private JdbcNotificationRepository notifications;

    @BeforeEach
    void freshSchema() {
        database = TestDatabase.forSchema("notification");
        notifications = new JdbcNotificationRepository(database.jdbcClient());
    }

    @Test
    void storesAndRestoresANotification() {
        Notification notification = stayCalm();

        assertThat(notifications.addIfNew(notification)).isTrue();

        Notification stored = notifications.findById(notification.id()).orElseThrow();
        assertThat(stored.origin()).isEqualTo(notification.origin());
        assertThat(stored.receivers()).containsExactly(COOK);
        assertThat(stored.title()).isEqualTo(notification.title());
        assertThat(stored.text()).isEqualTo(notification.text());
        assertThat(stored.link()).isEqualTo(HELP_LINK);
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(stored.statusFor(COOK)).isEqualTo(Status.NEW);
    }

    @Test
    void theSameEventIsStoredOnlyOnce() {
        assertThat(notifications.addIfNew(stayCalm())).isTrue();

        assertThat(notifications.addIfNew(stayCalm())).isFalse();
        assertThat(database.jdbcClient().sql("select count(*) from notification").query(Long.class).single()).isEqualTo(1);
        assertThat(database.jdbcClient().sql("select count(*) from notification_receiver").query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void savesStatusAndDeletionPerReceiver() {
        Notification notification = Notification.create(newEvent(), List.of(COOK, OTHER_COOK), "Title", "Text", HELP_LINK, NOW);
        notifications.addIfNew(notification);

        Notification changed = notifications.findByIdForUpdate(notification.id()).orElseThrow();
        changed.changeStatus(COOK, Status.READ);
        changed.deleteFor(OTHER_COOK, NOW.plusSeconds(60));
        notifications.save(changed);

        Notification stored = notifications.findById(notification.id()).orElseThrow();
        assertThat(stored.receivers()).containsExactlyInAnyOrder(COOK, OTHER_COOK);
        assertThat(stored.statusFor(COOK)).isEqualTo(Status.READ);
        assertThat(stored.isVisibleTo(OTHER_COOK)).isFalse();
        assertThat(stored.isReceiver(OTHER_COOK)).isTrue();
    }

    @Test
    void listsWhatAReceiverHasNotDeletedNewestFirstAndByTheirStatus() {
        Notification older = Notification.create(newEvent(), List.of(COOK), "Older", "Text", HELP_LINK, NOW);
        Notification newer = Notification.create(newEvent(), List.of(COOK, OTHER_COOK), "Newer", "Text", HELP_LINK, NOW.plusSeconds(1));
        Notification deleted = Notification.create(newEvent(), List.of(COOK), "Deleted", "Text", HELP_LINK, NOW.plusSeconds(2));
        Notification othersOnly = Notification.create(newEvent(), List.of(OTHER_COOK), "Other", "Text", HELP_LINK, NOW);
        List.of(older, newer, deleted, othersOnly).forEach(notifications::addIfNew);
        newer.changeStatus(COOK, Status.READ);
        notifications.save(newer);
        deleted.deleteFor(COOK, NOW);
        notifications.save(deleted);

        assertThat(notifications.findVisibleTo(COOK, Optional.empty())).extracting(Notification::id)
                .containsExactly(newer.id(), older.id());
        assertThat(notifications.findVisibleTo(COOK, Optional.of(Status.READ))).extracting(Notification::id)
                .containsExactly(newer.id());
        assertThat(notifications.findVisibleTo(COOK, Optional.of(Status.NEW))).extracting(Notification::id)
                .containsExactly(older.id());
        assertThat(notifications.findVisibleTo(OTHER_COOK, Optional.of(Status.NEW))).extracting(Notification::id)
                .containsExactlyInAnyOrder(newer.id(), othersOnly.id());
        assertThat(notifications.findVisibleTo(COOK, Optional.empty()).getFirst().receivers())
                .containsExactlyInAnyOrder(COOK, OTHER_COOK);
    }

    @Test
    void anUnknownNotificationIsEmpty() {
        assertThat(notifications.findById(NotificationId.newId())).isEmpty();
        assertThat(notifications.findByIdForUpdate(NotificationId.newId())).isEmpty();
    }

    @Test
    void theSchemaGuardsTheContractLimits() {
        assertThatThrownBy(() -> database.jdbcClient().sql("""
                insert into notification (notification_id, source_message_id, title, text, link, created_at)
                values (gen_random_uuid(), gen_random_uuid(), '', 'Text', 'https://larder.org', now())
                """).update()).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
