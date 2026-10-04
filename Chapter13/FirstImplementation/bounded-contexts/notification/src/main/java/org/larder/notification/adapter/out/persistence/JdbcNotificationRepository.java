package org.larder.notification.adapter.out.persistence;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.larder.notification.application.NotificationRepository;
import org.larder.notification.domain.Delivery;
import org.larder.notification.domain.EventId;
import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.ReceiverId;
import org.larder.notification.domain.Status;
import org.springframework.jdbc.core.simple.JdbcClient;

class JdbcNotificationRepository implements NotificationRepository {

    private static final String SELECT = """
            select n.notification_id, n.source_message_id, n.title, n.text, n.link, n.created_at,
                   r.receiver, r.status, r.deleted_at
              from notification n
              join notification_receiver r on r.notification_id = n.notification_id
            """;

    private final JdbcClient jdbc;

    JdbcNotificationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean addIfNew(Notification notification) {
        int inserted = jdbc.sql("""
                insert into notification (notification_id, source_message_id, title, text, link, created_at)
                values (:id, :origin, :title, :text, :link, :createdAt)
                on conflict (source_message_id) do nothing
                """)
                .param("id", notification.id().value())
                .param("origin", notification.origin().value())
                .param("title", notification.title())
                .param("text", notification.text())
                .param("link", notification.link().toString())
                .param("createdAt", Timestamp.from(notification.createdAt()))
                .update();
        if (inserted == 0) {
            return false;
        }
        for (Delivery delivery : notification.deliveries()) {
            jdbc.sql("""
                    insert into notification_receiver (notification_id, receiver, status, deleted_at)
                    values (:id, :receiver, :status, :deletedAt)
                    """)
                    .param("id", notification.id().value())
                    .param("receiver", delivery.receiver().value())
                    .param("status", delivery.status().name())
                    .param("deletedAt", delivery.deletedAtIfAny().map(Timestamp::from).orElse(null))
                    .update();
        }
        return true;
    }

    @Override
    public void save(Notification notification) {
        for (Delivery delivery : notification.deliveries()) {
            jdbc.sql("""
                    update notification_receiver set status = :status, deleted_at = :deletedAt
                     where notification_id = :id and receiver = :receiver
                    """)
                    .param("id", notification.id().value())
                    .param("receiver", delivery.receiver().value())
                    .param("status", delivery.status().name())
                    .param("deletedAt", delivery.deletedAtIfAny().map(Timestamp::from).orElse(null))
                    .update();
        }
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return single(jdbc.sql(SELECT + " where n.notification_id = :id order by r.receiver")
                .param("id", id.value()).query(this::row).list());
    }

    @Override
    public Optional<Notification> findByIdForUpdate(NotificationId id) {
        jdbc.sql("select notification_id from notification where notification_id = :id for update")
                .param("id", id.value()).query(UUID.class).optional();
        return findById(id);
    }

    @Override
    public List<Notification> findVisibleTo(ReceiverId receiver, Optional<Status> status) {
        return assemble(jdbc.sql(SELECT + """
                 where n.notification_id in (
                       select notification_id from notification_receiver
                        where receiver = :receiver and deleted_at is null
                          and (cast(:status as text) is null or status = cast(:status as text)))
                 order by n.created_at desc, n.notification_id, r.receiver
                """)
                .param("receiver", receiver.value())
                .param("status", status.map(Status::name).orElse(null))
                .query(this::row).list());
    }

    private static Optional<Notification> single(List<Row> rows) {
        List<Notification> found = assemble(rows);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.getFirst());
    }

    /** Groups the joined rows (one per Receiver) into notifications, keeping their order. */
    private static List<Notification> assemble(List<Row> rows) {
        Map<UUID, List<Row>> byNotification = new LinkedHashMap<>();
        rows.forEach(row -> byNotification.computeIfAbsent(row.id(), id -> new ArrayList<>()).add(row));
        return byNotification.values().stream().map(JdbcNotificationRepository::toNotification).toList();
    }

    private static Notification toNotification(List<Row> rows) {
        Row first = rows.getFirst();
        return Notification.restore(new NotificationId(first.id()), new EventId(first.origin()), first.title(),
                first.text(), URI.create(first.link()), first.createdAt(), rows.stream().map(Row::delivery).toList());
    }

    private Row row(ResultSet rs, int row) throws SQLException {
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        return new Row(
                rs.getObject("notification_id", UUID.class),
                rs.getObject("source_message_id", UUID.class),
                rs.getString("title"),
                rs.getString("text"),
                rs.getString("link"),
                rs.getTimestamp("created_at").toInstant(),
                new Delivery(new ReceiverId(rs.getObject("receiver", UUID.class)), Status.valueOf(rs.getString("status")),
                        deletedAt == null ? null : deletedAt.toInstant()));
    }

    private record Row(UUID id, UUID origin, String title, String text, String link, Instant createdAt,
                       Delivery delivery) {
    }
}
