package org.larder.consentmanagement.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A subject's agreement to exactly one consent text (aggregate root).
 *
 * <ul>
 *   <li>The text is kept as it read when the consent was given - later changes of the
 *       consent text do not change what was agreed to.</li>
 *   <li>A consent is never deleted. Revoking sets {@code revokedAt}; the consent stays as evidence.</li>
 *   <li>A revoked consent cannot be given again; the subject gives a new consent instead.</li>
 * </ul>
 */
public final class Consent {

    private final ConsentId id;
    private final SubjectId subject;
    private final ConsentText consentText;
    private final Instant givenAt;
    private Instant revokedAt;

    private Consent(ConsentId id, SubjectId subject, ConsentText consentText, Instant givenAt, Instant revokedAt) {
        this.id = Objects.requireNonNull(id);
        this.subject = Objects.requireNonNull(subject);
        this.consentText = Objects.requireNonNull(consentText);
        this.givenAt = Objects.requireNonNull(givenAt);
        this.revokedAt = revokedAt;
    }

    public static Consent give(SubjectId subject, ConsentText consentText, Instant now) {
        return new Consent(ConsentId.newId(), subject, consentText, now, null);
    }

    /** Recreates a stored consent. */
    public static Consent restore(ConsentId id, SubjectId subject, ConsentText consentText, Instant givenAt, Instant revokedAt) {
        return new Consent(id, subject, consentText, givenAt, revokedAt);
    }

    public void revoke(Instant now) {
        if (revokedAt != null) {
            throw new ConsentAlreadyRevokedException(id);
        }
        revokedAt = now;
    }

    public boolean isInForce() {
        return revokedAt == null;
    }

    public boolean isGivenBy(SubjectId candidate) {
        return subject.equals(candidate);
    }

    public ConsentId id() {
        return id;
    }

    public SubjectId subject() {
        return subject;
    }

    public ConsentText consentText() {
        return consentText;
    }

    public Instant givenAt() {
        return givenAt;
    }

    public Optional<Instant> revokedAt() {
        return Optional.ofNullable(revokedAt);
    }
}
