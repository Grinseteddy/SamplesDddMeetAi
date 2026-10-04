package org.larder.cookprofile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookprofile.TestData.COOK;
import static org.larder.cookprofile.TestData.DOE;
import static org.larder.cookprofile.TestData.JANE;
import static org.larder.cookprofile.TestData.JOE;
import static org.larder.cookprofile.TestData.JOE_EMAIL;
import static org.larder.cookprofile.TestData.NOW;
import static org.larder.cookprofile.TestData.OTHER_COOK;
import static org.larder.cookprofile.TestData.OTHER_EMAIL;
import static org.larder.cookprofile.TestData.ROE;
import static org.larder.cookprofile.TestData.TODAY;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.cookprofile.domain.Cook;
import org.larder.cookprofile.domain.CookChange;
import org.larder.cookprofile.domain.CookId;
import org.larder.cookprofile.domain.CookStatus;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.cookprofile.domain.PersonName;

class CookServiceTest {

    private final InMemoryCooks cooks = new InMemoryCooks();
    private final CookService service = new CookService(cooks, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aUserRegistersThemselvesAsAnActiveCook() {
        Cook cook = service.register(COOK, JOE_EMAIL, JOE, DOE);

        assertThat(cook.id()).isEqualTo(COOK);
        assertThat(cook.memberSince()).isEqualTo(TODAY);
        assertThat(cook.status()).isEqualTo(CookStatus.ACTIVE);
        assertThat(service.cook(COOK).email()).isEqualTo(JOE_EMAIL);
    }

    @Test
    void aUserRegistersOnlyOnce() {
        service.register(COOK, JOE_EMAIL, JOE, DOE);

        assertThatThrownBy(() -> service.register(COOK, OTHER_EMAIL, JOE, DOE)).isInstanceOf(AlreadyRegisteredException.class);
    }

    @Test
    void anEmailAddressBelongsToOneCookOnly() {
        service.register(COOK, JOE_EMAIL, JOE, DOE);
        service.register(OTHER_COOK, OTHER_EMAIL, JANE, ROE);

        assertThatThrownBy(() -> service.register(new CookId(java.util.UUID.randomUUID()), new EmailAddress("JOE.DOE@larder.org"), JOE, DOE))
                .isInstanceOf(AlreadyRegisteredException.class);
        assertThatThrownBy(() -> service.change(OTHER_COOK, OTHER_COOK, new CookChange(JOE_EMAIL, null, null, null)))
                .isInstanceOf(AlreadyRegisteredException.class);
    }

    @Test
    void aCookChangesTheirOwnProfile() {
        service.register(COOK, JOE_EMAIL, JOE, DOE);

        service.change(COOK, COOK, new CookChange(new EmailAddress("Joe.Doe@larder.org"), null, null, CookStatus.PREMIUM));

        Cook cook = service.cook(COOK);
        assertThat(cook.email().value()).isEqualTo("Joe.Doe@larder.org");
        assertThat(cook.status()).isEqualTo(CookStatus.PREMIUM);
        assertThat(cook.name()).isEqualTo(JOE);
    }

    @Test
    void aCookCannotChangeSomebodyElsesProfile() {
        service.register(OTHER_COOK, OTHER_EMAIL, JANE, ROE);

        assertThatThrownBy(() -> service.change(COOK, OTHER_COOK, new CookChange(null, JOE, null, null)))
                .isInstanceOf(NotPermittedException.class);
        assertThat(service.cook(OTHER_COOK).name()).isEqualTo(JANE);
    }

    @Test
    void onlyTheCookDeregistersThemselves() {
        service.register(COOK, JOE_EMAIL, JOE, DOE);

        assertThatThrownBy(() -> service.deregister(OTHER_COOK, COOK)).isInstanceOf(NotPermittedException.class);

        service.deregister(COOK, COOK);
        assertThatThrownBy(() -> service.cook(COOK)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void unknownCooksAreNotFound() {
        assertThatThrownBy(() -> service.cook(COOK)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.change(COOK, COOK, new CookChange(null, JOE, null, null)))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.deregister(COOK, COOK)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsCooksMatchingAllGivenFilters() {
        service.register(COOK, JOE_EMAIL, JOE, DOE);
        service.register(OTHER_COOK, OTHER_EMAIL, JANE, ROE);

        assertThat(service.cooks(null, null)).hasSize(2);
        assertThat(service.cooks(JOE, null)).extracting(Cook::id).containsExactly(COOK);
        assertThat(service.cooks(null, OTHER_EMAIL)).extracting(Cook::id).containsExactly(OTHER_COOK);
        assertThat(service.cooks(JOE, OTHER_EMAIL)).isEmpty();
    }

    static class InMemoryCooks implements CookRepository {
        private final Map<CookId, Cook> store = new HashMap<>();

        @Override
        public void save(Cook cook) {
            store.put(cook.id(), cook);
        }

        @Override
        public Optional<Cook> findById(CookId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Cook> findByEmail(EmailAddress email) {
            return store.values().stream().filter(c -> c.email().sameAs(email)).findFirst();
        }

        @Override
        public List<Cook> findAll(PersonName name, EmailAddress email) {
            return new ArrayList<>(store.values().stream()
                    .filter(c -> name == null || c.name().equals(name))
                    .filter(c -> email == null || c.email().sameAs(email))
                    .toList());
        }

        @Override
        public void delete(CookId id) {
            store.remove(id);
        }
    }
}
