package org.larder.notification.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.larder.notification.domain.EventId;
import org.larder.notification.domain.Notification;
import org.larder.notification.domain.NotificationId;
import org.larder.notification.domain.ReceiverId;
import org.larder.notification.domain.Status;

/** Keeps copies, so a change counts only once it is saved - as with the database. */
public class InMemoryNotifications implements NotificationRepository {

    private final Map<NotificationId, Notification> store = new LinkedHashMap<>();
    private final Map<EventId, NotificationId> byOrigin = new LinkedHashMap<>();

    @Override
    public boolean addIfNew(Notification notification) {
        if (byOrigin.containsKey(notification.origin())) {
            return false;
        }
        byOrigin.put(notification.origin(), notification.id());
        store.put(notification.id(), copy(notification));
        return true;
    }

    @Override
    public void save(Notification notification) {
        store.put(notification.id(), copy(notification));
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return Optional.ofNullable(store.get(id)).map(InMemoryNotifications::copy);
    }

    @Override
    public Optional<Notification> findByIdForUpdate(NotificationId id) {
        return findById(id);
    }

    @Override
    public List<Notification> findVisibleTo(ReceiverId receiver, Optional<Status> status) {
        return new ArrayList<>(store.values().stream()
                .filter(n -> n.isVisibleTo(receiver))
                .filter(n -> status.map(s -> n.statusFor(receiver) == s).orElse(true))
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .map(InMemoryNotifications::copy)
                .toList());
    }

    public List<Notification> all() {
        return List.copyOf(store.values());
    }

    private static Notification copy(Notification n) {
        return Notification.restore(n.id(), n.origin(), n.title(), n.text(), n.link(), n.createdAt(), n.deliveries());
    }
}
