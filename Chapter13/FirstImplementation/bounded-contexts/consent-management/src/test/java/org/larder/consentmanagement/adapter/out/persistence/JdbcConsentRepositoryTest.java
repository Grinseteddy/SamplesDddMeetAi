package org.larder.consentmanagement.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.consentmanagement.TestData.COOK;
import static org.larder.consentmanagement.TestData.MENTION_AS_HELPER;
import static org.larder.consentmanagement.TestData.NOW;
import static org.larder.consentmanagement.TestData.OTHER_COOK;
import static org.larder.consentmanagement.TestData.PHOTOS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.consentmanagement.domain.Consent;
import org.larder.platform.test.TestDatabase;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcConsentRepositoryTest {

    private JdbcConsentRepository consents;
    private JdbcConsentTexts texts;

    @BeforeEach
    void freshSchema() {
        var database = TestDatabase.forSchema("consentmanagement");
        consents = new JdbcConsentRepository(database.jdbcClient());
        texts = new JdbcConsentTexts(database.jdbcClient());
    }

    @Test
    void theConsentTextsAreSeeded() {
        assertThat(texts.findAll()).containsExactlyInAnyOrder(PHOTOS, MENTION_AS_HELPER);
        assertThat(texts.findById(PHOTOS.id())).contains(PHOTOS);
    }

    @Test
    void storesAndRevokesAConsent() {
        Consent consent = Consent.give(COOK, PHOTOS, NOW);
        consents.save(consent);

        consent.revoke(NOW.plusSeconds(60));
        consents.save(consent);

        Consent stored = consents.findById(consent.id()).orElseThrow();
        assertThat(stored.subject()).isEqualTo(COOK);
        assertThat(stored.consentText()).isEqualTo(PHOTOS);
        assertThat(stored.givenAt()).isEqualTo(NOW);
        assertThat(stored.revokedAt()).contains(NOW.plusSeconds(60));
    }

    @Test
    void findsTheConsentsOfOneSubject() {
        Consent mine = Consent.give(COOK, PHOTOS, NOW);
        consents.save(mine);
        consents.save(Consent.give(OTHER_COOK, PHOTOS, NOW));

        assertThat(consents.findBySubject(COOK)).extracting(Consent::id).containsExactly(mine.id());
    }
}
