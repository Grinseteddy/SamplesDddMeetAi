package org.larder.sharing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.HELP;
import static org.larder.sharing.TestData.IMAGE;
import static org.larder.sharing.TestData.NOW;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.larder.sharing.TestData.OTHER_HELP;
import static org.larder.sharing.TestData.OTHER_IMAGE;
import static org.larder.sharing.TestData.OTHER_PICTURE;
import static org.larder.sharing.TestData.PICTURE;
import static org.larder.sharing.TestData.TEXT;
import static org.larder.sharing.TestData.THIRD_COOK;
import static org.larder.sharing.TestData.communityHelp;
import static org.larder.sharing.TestData.grandmaHelp;
import static org.larder.sharing.application.ConsentPurpose.MENTION_AS_HELPER;
import static org.larder.sharing.application.ConsentPurpose.PHOTOS_IN_PUBLIC_THANKS;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.Help;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.HelperKind;
import org.larder.sharing.domain.MediaId;
import org.larder.sharing.domain.Recipient;
import org.larder.sharing.domain.Thanks;
import org.larder.sharing.domain.ThanksId;
import org.larder.sharing.domain.ThanksRevision;
import org.larder.sharing.domain.ThanksRuleViolationException;

class ThanksServiceTest {

    private final InMemoryThanks thanks = new InMemoryThanks();
    private final FakeHelps helps = new FakeHelps();
    private final FakePictures pictures = new FakePictures();
    private final FakeConsents consents = new FakeConsents();
    private final ThanksService service = new ThanksService(thanks, helps, pictures, consents,
            Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void theRescueFlowHappened() {
        helps.add(communityHelp());
        pictures.add(IMAGE);
        consents.give(COOK, PHOTOS_IN_PUBLIC_THANKS);
        consents.give(OTHER_COOK, MENTION_AS_HELPER);
    }

    private Thanks giveToOtherCook() {
        return service.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE);
    }

    private static String codeOf(Throwable e) {
        return e instanceof ThanksRuleViolationException rule ? rule.code() : ((ConsentMissingException) e).code();
    }

    @Test
    void theCookWhoReceivedTheHelpThanksTheHelper() {
        Thanks given = giveToOtherCook();

        assertThat(thanks.findById(given.id())).isPresent();
        assertThat(given.giver()).isEqualTo(COOK);
        assertThat(given.createdAt()).isEqualTo(NOW);
        assertThat(service.thanks(new ThanksFilter(null, OTHER_COOK))).extracting(Thanks::id).containsExactly(given.id());
    }

    @Test
    void thanksForAnUnknownHelpAreRejected() {
        assertThatThrownBy(() -> service.give(COOK, OTHER_HELP, List.of(), TEXT, PICTURE))
                .isInstanceOf(UnknownHelpException.class);
        assertThat(thanks.store).isEmpty();
    }

    @Test
    void onlyTheCookWhoReceivedTheHelpThanksForIt() {
        consents.give(OTHER_COOK, PHOTOS_IN_PUBLIC_THANKS);

        assertThatThrownBy(() -> service.give(OTHER_COOK, HELP, List.of(), TEXT, PICTURE))
                .isInstanceOf(NotPermittedException.class);
        assertThat(thanks.store).isEmpty();
    }

    @Test
    void aHelpCooksAssistanceDoesNotShowIsNotPermitted() {
        helps.refuse(OTHER_HELP);

        assertThatThrownBy(() -> service.give(COOK, OTHER_HELP, List.of(), TEXT, PICTURE))
                .isInstanceOf(NotPermittedException.class);
    }

    @Test
    void thanksAreGivenOncePerHelp() {
        giveToOtherCook();

        assertThatThrownBy(this::giveToOtherCook)
                .isInstanceOf(ThanksRuleViolationException.class)
                .extracting(ThanksServiceTest::codeOf).isEqualTo(ThanksRuleViolationException.THANKS_ALREADY_GIVEN);
    }

    @Test
    void thanksGoOnlyToWhoHelped() {
        assertThatThrownBy(() -> service.give(COOK, HELP, List.of(Recipient.grandmaAvatar()), TEXT, PICTURE))
                .extracting(ThanksServiceTest::codeOf).isEqualTo(ThanksRuleViolationException.RECIPIENT_NOT_HELPER);
        consents.give(THIRD_COOK, MENTION_AS_HELPER);
        assertThatThrownBy(() -> service.give(COOK, HELP, List.of(Recipient.cooks(THIRD_COOK)), TEXT, PICTURE))
                .extracting(ThanksServiceTest::codeOf).isEqualTo(ThanksRuleViolationException.RECIPIENT_NOT_HELPER);
    }

    @Test
    void theGrandmaAvatarIsThankedWithoutConsent() {
        helps.add(grandmaHelp());

        Thanks given = service.give(COOK, HELP, List.of(Recipient.grandmaAvatar()), TEXT, PICTURE);

        assertThat(given.mentionedCooks()).isEmpty();
    }

    @Test
    void theHelperMustHaveConsentedToBeingMentioned() {
        consents.revoke(OTHER_COOK, MENTION_AS_HELPER);

        assertThatThrownBy(this::giveToOtherCook)
                .isInstanceOf(ConsentMissingException.class)
                .extracting(ThanksServiceTest::codeOf).isEqualTo(ConsentMissingException.MENTION_WITHOUT_CONSENT);
        assertThat(thanks.store).isEmpty();
    }

    @Test
    void aConsentToAnotherPurposeDoesNotAllowMentioning() {
        consents.revoke(OTHER_COOK, MENTION_AS_HELPER);
        consents.give(OTHER_COOK, PHOTOS_IN_PUBLIC_THANKS);

        assertThatThrownBy(this::giveToOtherCook).isInstanceOf(ConsentMissingException.class);
    }

    @Test
    void thanksWithoutMentionsNeedNoMentionConsent() {
        consents.revoke(OTHER_COOK, MENTION_AS_HELPER);

        assertThat(service.give(COOK, HELP, List.of(), TEXT, PICTURE).recipients()).isEmpty();
    }

    @Test
    void aPictureMustBeAnImageOfMedia() {
        assertThatThrownBy(() -> service.give(COOK, HELP, List.of(), TEXT, OTHER_PICTURE))
                .isInstanceOf(UnknownPictureException.class);
    }

    @Test
    void theGiverMustHaveConsentedToSharingTheirPhotos() {
        consents.revoke(COOK, PHOTOS_IN_PUBLIC_THANKS);

        assertThatThrownBy(() -> service.give(COOK, HELP, List.of(), TEXT, PICTURE))
                .isInstanceOf(ConsentMissingException.class)
                .extracting(ThanksServiceTest::codeOf).isEqualTo(ConsentMissingException.PICTURE_WITHOUT_CONSENT);
    }

    @Test
    void theGiverChangesTextWithoutAskingTheUpstreams() {
        Thanks given = giveToOtherCook();
        helps.clear();
        consents.revoke(OTHER_COOK, MENTION_AS_HELPER);

        Thanks changed = service.revise(COOK, given.id(), new ThanksRevision(null, "Thanks again!", null));

        assertThat(changed.text()).isEqualTo("Thanks again!");
        assertThat(thanks.findById(given.id()).orElseThrow().text()).isEqualTo("Thanks again!");
    }

    @Test
    void onlyTheGiverChangesTheThanks() {
        Thanks given = giveToOtherCook();

        assertThatThrownBy(() -> service.revise(OTHER_COOK, given.id(), new ThanksRevision(null, "Mine now", null)))
                .isInstanceOf(NotPermittedException.class);
        assertThat(thanks.findById(given.id()).orElseThrow().text()).isEqualTo(TEXT);
    }

    @Test
    void changedRecipientsFollowTheSameRules() {
        helps.add(new Help(HELP, COOK, HelperKind.COMMUNITY_COOK, Optional.of(OTHER_COOK)));
        Thanks given = service.give(COOK, HELP, List.of(), TEXT, PICTURE);

        consents.revoke(OTHER_COOK, MENTION_AS_HELPER);
        assertThatThrownBy(() -> service.revise(COOK, given.id(),
                new ThanksRevision(List.of(Recipient.cooks(OTHER_COOK)), null, null)))
                .isInstanceOf(ConsentMissingException.class);
        assertThatThrownBy(() -> service.revise(COOK, given.id(),
                new ThanksRevision(List.of(Recipient.grandmaAvatar()), null, null)))
                .extracting(ThanksServiceTest::codeOf).isEqualTo(ThanksRuleViolationException.RECIPIENT_NOT_HELPER);
        assertThat(thanks.findById(given.id()).orElseThrow().recipients()).isEmpty();

        consents.give(OTHER_COOK, MENTION_AS_HELPER);
        service.revise(COOK, given.id(), new ThanksRevision(List.of(Recipient.cooks(OTHER_COOK)), null, null));
        assertThat(thanks.findById(given.id()).orElseThrow().mentionedCooks()).containsExactly(OTHER_COOK);
    }

    @Test
    void changedRecipientsNeedTheHelpStill() {
        Thanks given = giveToOtherCook();
        helps.clear();

        assertThatThrownBy(() -> service.revise(COOK, given.id(), new ThanksRevision(List.of(), null, null)))
                .isInstanceOf(UnknownHelpException.class);
    }

    @Test
    void aChangedPictureMustExistAndBeConsented() {
        Thanks given = giveToOtherCook();

        assertThatThrownBy(() -> service.revise(COOK, given.id(), new ThanksRevision(null, null, OTHER_PICTURE)))
                .isInstanceOf(UnknownPictureException.class);

        pictures.add(OTHER_IMAGE);
        consents.revoke(COOK, PHOTOS_IN_PUBLIC_THANKS);
        assertThatThrownBy(() -> service.revise(COOK, given.id(), new ThanksRevision(null, null, OTHER_PICTURE)))
                .isInstanceOf(ConsentMissingException.class);

        consents.give(COOK, PHOTOS_IN_PUBLIC_THANKS);
        service.revise(COOK, given.id(), new ThanksRevision(null, null, OTHER_PICTURE));
        assertThat(thanks.findById(given.id()).orElseThrow().picture()).isEqualTo(OTHER_PICTURE);
    }

    @Test
    void aConcurrentChangeIsNotOverwritten() {
        Thanks given = giveToOtherCook();
        Thanks stale = service.thanks(given.id());
        service.revise(COOK, given.id(), new ThanksRevision(null, "First change", null));

        stale.revise(new ThanksRevision(null, "Second change", null), NOW);
        assertThatThrownBy(() -> thanks.update(stale)).isInstanceOf(ConcurrentChangeException.class);
        assertThat(thanks.findById(given.id()).orElseThrow().text()).isEqualTo("First change");
    }

    @Test
    void onlyTheGiverWithdrawsTheThanks() {
        Thanks given = giveToOtherCook();

        assertThatThrownBy(() -> service.withdraw(OTHER_COOK, given.id())).isInstanceOf(NotPermittedException.class);
        assertThat(thanks.findById(given.id())).isPresent();

        service.withdraw(COOK, given.id());
        assertThat(thanks.findById(given.id())).isEmpty();
    }

    @Test
    void unknownThanksAreNotFound() {
        ThanksId unknown = ThanksId.newId();

        assertThatThrownBy(() -> service.thanks(unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.withdraw(COOK, unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.revise(COOK, unknown, ThanksRevision.none())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void anUnavailableUpstreamStopsTheThanks() {
        consents.failing = true;

        assertThatThrownBy(this::giveToOtherCook).isInstanceOf(UpstreamUnavailableException.class);
        assertThat(thanks.store).isEmpty();
    }

    /** Keeps copies, as a database would, and checks versions like the JDBC repository. */
    static class InMemoryThanks implements ThanksRepository {
        final Map<ThanksId, Thanks> store = new HashMap<>();

        @Override
        public void add(Thanks thanks) {
            if (existsForHelp(thanks.help())) {
                throw new ThanksRuleViolationException(ThanksRuleViolationException.THANKS_ALREADY_GIVEN, "again");
            }
            store.put(thanks.id(), copy(thanks, thanks.version()));
        }

        @Override
        public void update(Thanks thanks) {
            Thanks stored = store.get(thanks.id());
            if (stored == null || stored.version() != thanks.version()) {
                throw new ConcurrentChangeException(thanks.id());
            }
            store.put(thanks.id(), copy(thanks, thanks.version() + 1));
        }

        @Override
        public boolean remove(ThanksId id) {
            return store.remove(id) != null;
        }

        @Override
        public Optional<Thanks> findById(ThanksId id) {
            return Optional.ofNullable(store.get(id)).map(thanks -> copy(thanks, thanks.version()));
        }

        @Override
        public boolean existsForHelp(HelpId help) {
            return store.values().stream().anyMatch(thanks -> thanks.help().equals(help));
        }

        @Override
        public List<Thanks> find(ThanksFilter filter) {
            return store.values().stream()
                    .filter(thanks -> filter.giver() == null || thanks.isGivenBy(filter.giver()))
                    .filter(thanks -> filter.mentionedCook() == null || thanks.mentionedCooks().contains(filter.mentionedCook()))
                    .sorted(Comparator.comparing(Thanks::createdAt).reversed())
                    .toList();
        }

        private static Thanks copy(Thanks thanks, long version) {
            return Thanks.restore(thanks.id(), thanks.giver(), thanks.help(), thanks.recipients(), thanks.text(),
                    thanks.picture(), thanks.createdAt(), thanks.updatedAt(), version);
        }
    }

    static class FakeHelps implements Helps {
        private final Map<HelpId, Help> helps = new HashMap<>();
        private final Set<HelpId> refused = new HashSet<>();

        void add(Help help) {
            helps.put(help.id(), help);
        }

        void refuse(HelpId id) {
            refused.add(id);
        }

        void clear() {
            helps.clear();
        }

        @Override
        public Optional<Help> find(HelpId id) {
            if (refused.contains(id)) {
                throw new NotPermittedException("refused");
            }
            return Optional.ofNullable(helps.get(id));
        }
    }

    static class FakePictures implements Pictures {
        private final Set<MediaId> images = new HashSet<>();

        void add(MediaId image) {
            images.add(image);
        }

        @Override
        public boolean exists(MediaId image) {
            return images.contains(image);
        }
    }

    static class FakeConsents implements Consents {
        private final List<Map.Entry<CookId, ConsentPurpose>> inForce = new ArrayList<>();
        boolean failing;

        void give(CookId cook, ConsentPurpose purpose) {
            inForce.add(Map.entry(cook, purpose));
        }

        void revoke(CookId cook, ConsentPurpose purpose) {
            inForce.remove(Map.entry(cook, purpose));
        }

        @Override
        public boolean isInForce(CookId cook, ConsentPurpose purpose) {
            if (failing) {
                throw new UpstreamUnavailableException("down", null);
            }
            return inForce.contains(Map.entry(cook, purpose));
        }
    }
}
