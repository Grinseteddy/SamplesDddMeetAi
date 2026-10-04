package org.larder.cookprofile.adapter.out.persistence;

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
import static org.larder.cookprofile.TestData.jane;
import static org.larder.cookprofile.TestData.joe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.cookprofile.domain.Cook;
import org.larder.cookprofile.domain.CookChange;
import org.larder.cookprofile.domain.CookStatus;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.platform.test.TestDatabase;
import org.springframework.dao.DuplicateKeyException;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcCookRepositoryTest {

    private JdbcCookRepository cooks;

    @BeforeEach
    void freshSchema() {
        cooks = new JdbcCookRepository(TestDatabase.forSchema("cookprofile").jdbcClient());
    }

    @Test
    void storesAndChangesACook() {
        Cook cook = joe();
        cooks.save(cook);

        cook.change(new CookChange(null, JANE, null, CookStatus.PREMIUM));
        cooks.save(cook);

        Cook stored = cooks.findById(COOK).orElseThrow();
        assertThat(stored.email()).isEqualTo(JOE_EMAIL);
        assertThat(stored.name()).isEqualTo(JANE);
        assertThat(stored.givenName()).isEqualTo(DOE);
        assertThat(stored.memberSince()).isEqualTo(TODAY);
        assertThat(stored.status()).isEqualTo(CookStatus.PREMIUM);
    }

    @Test
    void findsACookByEmailIgnoringCase() {
        cooks.save(joe());

        assertThat(cooks.findByEmail(new EmailAddress("JOE.DOE@larder.org"))).map(Cook::id).contains(COOK);
        assertThat(cooks.findByEmail(OTHER_EMAIL)).isEmpty();
    }

    @Test
    void filtersByNameAndEmail() {
        cooks.save(joe());
        cooks.save(jane());

        assertThat(cooks.findAll(null, null)).extracting(Cook::id).containsExactly(OTHER_COOK, COOK);
        assertThat(cooks.findAll(JOE, null)).extracting(Cook::id).containsExactly(COOK);
        assertThat(cooks.findAll(null, OTHER_EMAIL)).extracting(Cook::id).containsExactly(OTHER_COOK);
        assertThat(cooks.findAll(JOE, JOE_EMAIL)).extracting(Cook::id).containsExactly(COOK);
        assertThat(cooks.findAll(JOE, OTHER_EMAIL)).isEmpty();
    }

    @Test
    void theSchemaKeepsEmailAddressesUnique() {
        cooks.save(joe());

        assertThatThrownBy(() -> cooks.save(Cook.register(OTHER_COOK, new EmailAddress("Joe.Doe@larder.org"), JANE, DOE, TODAY)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void deregisteringDeletesTheCook() {
        cooks.save(joe());

        cooks.delete(COOK);

        assertThat(cooks.findById(COOK)).isEmpty();
    }
}
