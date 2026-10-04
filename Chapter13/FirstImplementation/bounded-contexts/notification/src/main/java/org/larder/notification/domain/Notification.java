package org.larder.notification.domain;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A message addressed to one or more Cooks (the Receivers), pointing them to something that
 * happened elsewhere on the platform (aggregate root).
 *
 * <ul>
 *   <li>Notifications are created exclusively on events, never by a Cook; {@link #origin()} is the
 *       event it was created from, and one event creates at most one notification.</li>
 *   <li>A notification has at least one Receiver; title 1..200, text 1..2000 characters, and a link.</li>
 *   <li>Title, text, link and Receivers never change after creation.</li>
 *   <li>Each Receiver has their own {@link Delivery}: a notification starts NEW for every Receiver,
 *       and each Receiver marks it READ (or NEW again) and deletes it for themselves only.</li>
 *   <li>Only a Receiver may read, mark or delete it; once a Receiver deleted it, it no longer exists
 *       for them - deleting is irreversible.</li>
 * </ul>
 */
public final class Notification {

    public static final int TITLE_MAX_LENGTH = 200;
    public static final int TEXT_MAX_LENGTH = 2000;

    private final NotificationId id;
    private final EventId origin;
    private final String title;
    private final String text;
    private final URI link;
    private final Instant createdAt;
    private final Map<ReceiverId, Delivery> deliveries;

    private Notification(NotificationId id, EventId origin, String title, String text, URI link, Instant createdAt,
                         Collection<Delivery> deliveries) {
        this.id = Objects.requireNonNull(id);
        this.origin = Objects.requireNonNull(origin);
        this.title = checkLength("title", title, TITLE_MAX_LENGTH);
        this.text = checkLength("text", text, TEXT_MAX_LENGTH);
        this.link = Objects.requireNonNull(link, "link must not be null");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.deliveries = new LinkedHashMap<>();
        for (Delivery delivery : deliveries) {
            if (this.deliveries.putIfAbsent(delivery.receiver(), delivery) != null) {
                throw new NotificationRuleViolationException("Receiver " + delivery.receiver().value() + " is named twice");
            }
        }
        if (this.deliveries.isEmpty()) {
            throw new NotificationRuleViolationException("A notification has at least one Receiver");
        }
    }

    /** A new notification, NEW for each of its Receivers. */
    public static Notification create(EventId origin, List<ReceiverId> receivers, String title, String text, URI link,
                                      Instant now) {
        Objects.requireNonNull(receivers, "receivers must not be null");
        return new Notification(NotificationId.newId(), origin, title, text, link, now,
                receivers.stream().map(Delivery::newFor).toList());
    }

    /**
     * Tells the Cook who asked for help that help was provided: who answered and the answer's title,
     * linking to the Help in Cooking Assistance.
     */
    public static Notification helpProvided(EventId origin, ReceiverId helpRequester, String answerTitle,
                                            HelpProvider provider, URI helpLink, Instant now) {
        Objects.requireNonNull(provider, "provider must not be null");
        checkLength("answer title", answerTitle, TITLE_MAX_LENGTH);
        String title = "Help provided by " + provider.phrase();
        String text = capitalized(provider.phrase()) + " answered your help request: \"" + answerTitle
                + "\". Open the help to see the answer.";
        return create(origin, List.of(helpRequester), title, text, helpLink, now);
    }

    /** Recreates a stored notification. */
    public static Notification restore(NotificationId id, EventId origin, String title, String text, URI link,
                                       Instant createdAt, Collection<Delivery> deliveries) {
        return new Notification(id, origin, title, text, link, createdAt, deliveries);
    }

    public boolean isReceiver(ReceiverId candidate) {
        return deliveries.containsKey(candidate);
    }

    /** Whether {@code receiver} is a Receiver and has not deleted it. */
    public boolean isVisibleTo(ReceiverId receiver) {
        Delivery delivery = deliveries.get(receiver);
        return delivery != null && !delivery.isDeleted();
    }

    /** The read status of {@code receiver}. */
    public Status statusFor(ReceiverId receiver) {
        return visibleDeliveryOf(receiver).status();
    }

    /** Sets the read status for {@code receiver} only; setting the current status again changes nothing. */
    public void changeStatus(ReceiverId receiver, Status status) {
        Objects.requireNonNull(status, "status must not be null");
        deliveries.put(receiver, visibleDeliveryOf(receiver).withStatus(status));
    }

    /** Deletes the notification for {@code receiver} only; the other Receivers keep it. */
    public void deleteFor(ReceiverId receiver, Instant now) {
        deliveries.put(receiver, visibleDeliveryOf(receiver).deleted(now));
    }

    private Delivery visibleDeliveryOf(ReceiverId receiver) {
        Delivery delivery = deliveries.get(receiver);
        if (delivery == null) {
            throw new NotAReceiverException(id, receiver);
        }
        if (delivery.isDeleted()) {
            throw new NotificationDeletedException(id);
        }
        return delivery;
    }

    public NotificationId id() {
        return id;
    }

    public EventId origin() {
        return origin;
    }

    public String title() {
        return title;
    }

    public String text() {
        return text;
    }

    public URI link() {
        return link;
    }

    public Instant createdAt() {
        return createdAt;
    }

    /** Everybody the notification is addressed to, including Receivers who deleted it for themselves. */
    public List<ReceiverId> receivers() {
        return List.copyOf(deliveries.keySet());
    }

    public List<Delivery> deliveries() {
        return List.copyOf(deliveries.values());
    }

    private static String checkLength(String what, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new NotificationRuleViolationException("A notification's " + what + " must not be empty");
        }
        if (value.length() > maxLength) {
            throw new NotificationRuleViolationException(
                    "A notification's " + what + " has at most " + maxLength + " characters");
        }
        return value;
    }

    private static String capitalized(String phrase) {
        return Character.toUpperCase(phrase.charAt(0)) + phrase.substring(1);
    }
}
