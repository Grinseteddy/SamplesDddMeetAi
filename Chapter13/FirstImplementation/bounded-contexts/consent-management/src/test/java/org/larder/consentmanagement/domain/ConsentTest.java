package org.larder.consentmanagement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.consentmanagement.TestData.COOK;
import static org.larder.consentmanagement.TestData.NOW;
import static org.larder.consentmanagement.TestData.PHOTOS;

import org.junit.jupiter.api.Test;

class ConsentTest {

    @Test
    void aGivenConsentIsInForceAndKeepsTheWording() {
        Consent consent = Consent.give(COOK, PHOTOS, NOW);

        assertThat(consent.isInForce()).isTrue();
        assertThat(consent.consentText()).isEqualTo(PHOTOS);
        assertThat(consent.givenAt()).isEqualTo(NOW);
        assertThat(consent.revokedAt()).isEmpty();
    }

    @Test
    void revokingEndsTheConsentButKeepsIt() {
        Consent consent = Consent.give(COOK, PHOTOS, NOW);

        consent.revoke(NOW.plusSeconds(60));

        assertThat(consent.isInForce()).isFalse();
        assertThat(consent.revokedAt()).contains(NOW.plusSeconds(60));
    }

    @Test
    void aRevokedConsentCannotBeRevokedAgain() {
        Consent consent = Consent.give(COOK, PHOTOS, NOW);
        consent.revoke(NOW);

        assertThatThrownBy(() -> consent.revoke(NOW)).isInstanceOf(ConsentAlreadyRevokedException.class);
    }

    @Test
    void aConsentTextNeedsAWording() {
        assertThatThrownBy(() -> new ConsentText(PHOTOS.id(), " ")).isInstanceOf(IllegalArgumentException.class);
    }
}
