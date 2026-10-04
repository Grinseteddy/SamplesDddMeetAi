package org.larder.media;

import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.larder.media.domain.BusinessObjectId;
import org.larder.media.domain.ImageContent;
import org.larder.media.domain.Link;
import org.larder.media.domain.LinkType;
import org.larder.media.domain.UploaderId;

/** The examples of the contract and the visual glossary. */
public final class TestData {

    public static final UploaderId COOK = new UploaderId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final UploaderId OTHER_COOK = new UploaderId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final BusinessObjectId RECIPE_ID = new BusinessObjectId(UUID.fromString("7cf09822-77a1-46bb-812f-b7852bca0913"));
    public static final BusinessObjectId HELP_REQUEST_ID = new BusinessObjectId(UUID.fromString("0b6f3c1e-8a3d-4a59-9a8e-5c7d2f1e9b40"));
    public static final Link RECIPE = new Link(LinkType.RECIPE,
            URI.create("https://larder.org/recipe-catalog/recipes/" + RECIPE_ID.value()));
    public static final Link HELP_REQUEST = new Link(LinkType.HELP_REQUEST,
            URI.create("https://larder.org/cooking-assistance/help-requests/" + HELP_REQUEST_ID.value()));
    /** A 1x1 pixel PNG. */
    public static final String PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    public static final ImageContent PNG = ImageContent.of(Base64.getDecoder().decode(PNG_BASE64));
    public static final Instant NOW = Instant.parse("2026-09-21T10:34:00Z");

    private TestData() {
    }
}
