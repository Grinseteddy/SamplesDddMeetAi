package org.larder.cookprofile.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookprofile.TestData.COOK;
import static org.larder.cookprofile.TestData.DOE;
import static org.larder.cookprofile.TestData.JANE;
import static org.larder.cookprofile.TestData.JOE;
import static org.larder.cookprofile.TestData.JOE_EMAIL;
import static org.larder.cookprofile.TestData.OTHER_COOK;
import static org.larder.cookprofile.TestData.OTHER_EMAIL;
import static org.larder.cookprofile.TestData.TODAY;
import static org.larder.cookprofile.TestData.joe;

import org.junit.jupiter.api.Test;

class CookTest {

    @Test
    void aRegisteredCookIsActiveSinceTheRegistrationDate() {
        Cook cook = Cook.register(COOK, JOE_EMAIL, JOE, DOE, TODAY);

        assertThat(cook.id()).isEqualTo(COOK);
        assertThat(cook.status()).isEqualTo(CookStatus.ACTIVE);
        assertThat(cook.memberSince()).isEqualTo(TODAY);
        assertThat(cook.isProfileOf(COOK)).isTrue();
        assertThat(cook.isProfileOf(OTHER_COOK)).isFalse();
    }

    @Test
    void aChangeChangesOnlyTheGivenParts() {
        Cook cook = joe();

        cook.change(new CookChange(null, JANE, null, CookStatus.PREMIUM));

        assertThat(cook.name()).isEqualTo(JANE);
        assertThat(cook.status()).isEqualTo(CookStatus.PREMIUM);
        assertThat(cook.givenName()).isEqualTo(DOE);
        assertThat(cook.email()).isEqualTo(JOE_EMAIL);
        assertThat(cook.id()).isEqualTo(COOK);
        assertThat(cook.memberSince()).isEqualTo(TODAY);

        cook.change(new CookChange(OTHER_EMAIL, null, null, null));
        assertThat(cook.email()).isEqualTo(OTHER_EMAIL);
    }

    @Test
    void aChangeNeedsAtLeastOnePart() {
        assertThatThrownBy(() -> new CookChange(null, null, null, null)).isInstanceOf(InvalidCookException.class);
    }

    @Test
    void namesHaveOneToHundredCharacters() {
        assertThatThrownBy(() -> new PersonName(" ")).isInstanceOf(InvalidCookException.class);
        assertThatThrownBy(() -> new PersonName(null)).isInstanceOf(InvalidCookException.class);
        assertThatThrownBy(() -> new PersonName("x".repeat(101))).isInstanceOf(InvalidCookException.class);
        assertThat(new PersonName("x".repeat(100)).value()).hasSize(100);
    }

    @Test
    void anEmailAddressIsWellFormedAndComparedIgnoringCase() {
        assertThatThrownBy(() -> new EmailAddress("joe")).isInstanceOf(InvalidCookException.class);
        assertThatThrownBy(() -> new EmailAddress("@larder.org")).isInstanceOf(InvalidCookException.class);
        assertThatThrownBy(() -> new EmailAddress("joe@")).isInstanceOf(InvalidCookException.class);
        assertThatThrownBy(() -> new EmailAddress("jo e@larder.org")).isInstanceOf(InvalidCookException.class);
        assertThatThrownBy(() -> new EmailAddress("x".repeat(250) + "@a.de")).isInstanceOf(InvalidCookException.class);
        assertThat(new EmailAddress("Joe.Doe@Larder.org").sameAs(JOE_EMAIL)).isTrue();
    }
}
