package org.larder.notification;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import org.larder.notification.domain.EventId;
import org.larder.notification.domain.HelpProvider;
import org.larder.notification.domain.Notification;
import org.larder.notification.domain.ReceiverId;

/** The examples of the contracts and the visual glossary. */
public final class TestData {

    public static final ReceiverId COOK = new ReceiverId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final ReceiverId OTHER_COOK = new ReceiverId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final UUID HELP = UUID.fromString("a9caf90d-00b4-4a66-8184-7d02152e8d6a");
    public static final UUID HELP_REQUEST = UUID.fromString("23a8eeed-35f6-460b-892e-7bb458a8fded");
    /** messageId of the contract example stayCalm. */
    public static final EventId STAY_CALM_MESSAGE = new EventId(UUID.fromString("9a7d3c21-6e40-4b58-b1f2-0c3e5d7a9b64"));
    public static final URI HELP_LINK = URI.create("https://larder.org/cooking-assistance/helps/" + HELP);
    public static final Instant NOW = Instant.parse("2026-09-21T10:34:00Z");

    /** The contract's example stayCalm as notification: the Grandma Avatar helped COOK. */
    public static Notification stayCalm() {
        return Notification.helpProvided(STAY_CALM_MESSAGE, COOK, "Stay calm", HelpProvider.GRANDMA_AVATAR, HELP_LINK, NOW);
    }

    public static EventId newEvent() {
        return new EventId(UUID.randomUUID());
    }

    private TestData() {
    }
}
