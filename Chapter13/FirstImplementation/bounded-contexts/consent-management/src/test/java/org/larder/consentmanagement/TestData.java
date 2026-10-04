package org.larder.consentmanagement;

import java.time.Instant;
import java.util.UUID;

import org.larder.consentmanagement.domain.ConsentText;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.larder.consentmanagement.domain.SubjectId;

/** The examples of the contract and the visual glossary. */
public final class TestData {

    public static final SubjectId COOK = new SubjectId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final SubjectId OTHER_COOK = new SubjectId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final ConsentText PHOTOS = new ConsentText(
            new ConsentTextId(UUID.fromString("5f8d8a1c-515d-4eae-a6b1-0a0313edfc31")),
            "I allow to use my photos in public thanks and recipes.");
    public static final ConsentText MENTION_AS_HELPER = new ConsentText(
            new ConsentTextId(UUID.fromString("3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60")),
            "I allow other cooks to mention me as helper in their thanks.");
    public static final Instant NOW = Instant.parse("2026-09-21T10:34:00Z");

    private TestData() {
    }
}
