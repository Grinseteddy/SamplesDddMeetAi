package org.larder.sharing;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.Help;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.HelperKind;
import org.larder.sharing.domain.MediaId;
import org.larder.sharing.domain.Picture;

/** The examples of the contracts and the visual glossary. */
public final class TestData {

    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final CookId OTHER_COOK = new CookId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final CookId THIRD_COOK = new CookId(UUID.fromString("7b1f9a64-2c3d-4e5f-8a9b-0c1d2e3f4a5b"));
    public static final HelpId HELP = new HelpId(UUID.fromString("a9caf90d-00b4-4a66-8184-7d02152e8d6a"));
    public static final HelpId OTHER_HELP = new HelpId(UUID.fromString("c3d2e1f0-a9b8-4c7d-8e6f-5a4b3c2d1e0f"));
    public static final MediaId IMAGE = new MediaId(UUID.fromString("b009a5d1-0205-4b0c-af82-822229cf243a"));
    public static final URI IMAGE_LINK = URI.create("https://larder.org/media/images/" + IMAGE.value());
    public static final Picture PICTURE = new Picture(IMAGE_LINK, IMAGE);
    public static final MediaId OTHER_IMAGE = new MediaId(UUID.fromString("d4e5f6a7-b8c9-4d0e-9f1a-2b3c4d5e6f70"));
    public static final Picture OTHER_PICTURE = new Picture(
            URI.create("https://larder.org/media/images/" + OTHER_IMAGE.value()), OTHER_IMAGE);
    public static final String TEXT = "Thank you so much - the scones were saved!";
    public static final Instant NOW = Instant.parse("2026-10-03T17:00:00Z");

    private TestData() {
    }

    /** OTHER_COOK helped COOK as a community cook. */
    public static Help communityHelp() {
        return new Help(HELP, COOK, HelperKind.COMMUNITY_COOK, Optional.of(OTHER_COOK));
    }

    /** The Grandma Avatar helped COOK (the contract's StayCalmHelp). */
    public static Help grandmaHelp() {
        return new Help(HELP, COOK, HelperKind.GRANDMA_AVATAR, Optional.empty());
    }

    /** OTHER_COOK helped COOK as a chef. */
    public static Help chefHelp() {
        return new Help(HELP, COOK, HelperKind.CHEF, Optional.of(OTHER_COOK));
    }
}
