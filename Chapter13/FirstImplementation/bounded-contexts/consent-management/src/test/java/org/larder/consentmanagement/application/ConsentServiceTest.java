package org.larder.consentmanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.consentmanagement.TestData.COOK;
import static org.larder.consentmanagement.TestData.NOW;
import static org.larder.consentmanagement.TestData.OTHER_COOK;
import static org.larder.consentmanagement.TestData.PHOTOS;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.consentmanagement.domain.Consent;
import org.larder.consentmanagement.domain.ConsentAlreadyRevokedException;
import org.larder.consentmanagement.domain.ConsentId;
import org.larder.consentmanagement.domain.ConsentText;
import org.larder.consentmanagement.domain.ConsentTextId;
import org.larder.consentmanagement.domain.SubjectId;

class ConsentServiceTest {

    private final InMemoryConsents consents = new InMemoryConsents();
    private final ConsentService service = new ConsentService(consents, new FixedTexts(PHOTOS), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void aCookGivesAConsentOfTheirOwn() {
        Consent consent = service.give(COOK, COOK, PHOTOS.id());

        assertThat(consents.findById(consent.id())).isPresent();
        assertThat(service.consentsOf(COOK)).extracting(Consent::id).containsExactly(consent.id());
    }

    @Test
    void aCookCannotGiveAConsentForSomebodyElse() {
        assertThatThrownBy(() -> service.give(COOK, OTHER_COOK, PHOTOS.id())).isInstanceOf(NotPermittedException.class);
    }

    @Test
    void aConsentNeedsAnExistingConsentText() {
        assertThatThrownBy(() -> service.give(COOK, COOK, new ConsentTextId(UUID.randomUUID())))
                .isInstanceOf(UnknownConsentTextException.class);
    }

    @Test
    void onlyTheSubjectRevokesTheirConsent() {
        Consent consent = service.give(COOK, COOK, PHOTOS.id());

        assertThatThrownBy(() -> service.revoke(OTHER_COOK, consent.id())).isInstanceOf(NotPermittedException.class);

        service.revoke(COOK, consent.id());
        assertThat(service.consent(consent.id()).isInForce()).isFalse();
        assertThatThrownBy(() -> service.revoke(COOK, consent.id())).isInstanceOf(ConsentAlreadyRevokedException.class);
    }

    @Test
    void unknownConsentsAndTextsAreNotFound() {
        assertThatThrownBy(() -> service.consent(ConsentId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.consentText(new ConsentTextId(UUID.randomUUID()))).isInstanceOf(NotFoundException.class);
    }

    static class InMemoryConsents implements ConsentRepository {
        private final Map<ConsentId, Consent> store = new HashMap<>();

        @Override
        public void save(Consent consent) {
            store.put(consent.id(), consent);
        }

        @Override
        public Optional<Consent> findById(ConsentId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Consent> findBySubject(SubjectId subject) {
            return new ArrayList<>(store.values().stream().filter(c -> c.isGivenBy(subject)).toList());
        }
    }

    record FixedTexts(ConsentText text) implements ConsentTexts {
        @Override
        public List<ConsentText> findAll() {
            return List.of(text);
        }

        @Override
        public Optional<ConsentText> findById(ConsentTextId id) {
            return text.id().equals(id) ? Optional.of(text) : Optional.empty();
        }
    }
}
