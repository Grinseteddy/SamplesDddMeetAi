package org.larder.cookingassistance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.GRANDMA_HELP_ID;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.OTHER_COOK;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.dinnerForTheInLaws;
import static org.larder.cookingassistance.TestData.foldingTheDough;
import static org.larder.cookingassistance.TestData.stayCalm;
import static org.larder.cookingassistance.TestData.threeCourses;
import static org.larder.cookingassistance.TestData.useAColdPan;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.InMemoryAdapters.InMemoryHelpRequests;
import org.larder.cookingassistance.application.InMemoryAdapters.InMemoryHelps;
import org.larder.cookingassistance.application.InMemoryAdapters.RecordedEvents;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.CookId;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.cookingassistance.domain.HelpType;

class HelpServiceTest {

    private static final UUID INCOMING_CORRELATION = UUID.fromString("5c2f0e9a-1d6b-4f3c-9a0e-2b7d1e4c8f10");

    private final InMemoryHelpRequests requests = new InMemoryHelpRequests();
    private final InMemoryHelps helps = new InMemoryHelps();
    private final RecordedEvents events = new RecordedEvents();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final HelpRequestService requestService = new HelpRequestService(requests, helps, events, clock);
    private final HelpService service = new HelpService(requests, helps, events, clock);

    private HelpRequest burning() {
        return requestService.raise(COOK, burningCatastrophe());
    }

    private GrandmaHelp grandmaStayCalm(HelpRequest request) {
        return new GrandmaHelp(new HelpId(GRANDMA_HELP_ID), request.id(), COOK, "Stay calm", stayCalm());
    }

    @Test
    void aCommunityHelpAnswersTheRequestAndIsAnnouncedUnderTheRequestsCorrelationId() {
        HelpRequest request = burning();

        Help help = service.give(OTHER_COOK, request.id(), HelpProviderType.COMMUNITY, "Stay calm", stayCalm());

        assertThat(helps.findById(help.id())).isPresent();
        assertThat(help.helpProvider()).contains(OTHER_COOK);
        assertThat(requests.findById(request.id()).orElseThrow().status()).isEqualTo(HelpRequestStatus.ANSWERED);
        assertThat(events.provided).containsExactly(help);
        assertThat(events.providedCorrelationIds).containsExactly(request.id().value());
    }

    @Test
    void aHelpForAnUnknownRequestIsABrokenRequest() {
        assertThatThrownBy(() -> service.give(OTHER_COOK, HelpRequestId.newId(), HelpProviderType.COMMUNITY, "Calm", stayCalm()))
                .isInstanceOf(UnknownHelpRequestException.class);
    }

    @Test
    void aHelpThatDoesNotFitIsNeitherStoredNorAnnounced() {
        HelpRequest request = burning();

        assertThatThrownBy(() -> service.give(OTHER_COOK, request.id(), HelpProviderType.COMMUNITY, "Cold pan", useAColdPan()))
                .isInstanceOf(HelpRuleViolationException.class);
        assertThat(helps.store).isEmpty();
        assertThat(events.provided).isEmpty();
        assertThat(request.status()).isEqualTo(HelpRequestStatus.OPEN);
    }

    @Test
    void overRestNobodyAnswersAsTheGrandmaAvatar() {
        HelpRequest request = burning();

        assertThatThrownBy(() -> service.give(OTHER_COOK, request.id(), HelpProviderType.GRANDMA_AVATAR, "Calm", stayCalm()))
                .isInstanceOf(HelpRuleViolationException.class)
                .extracting(e -> ((HelpRuleViolationException) e).code())
                .isEqualTo(HelpRuleViolationException.INVALID_HELP_PROVIDER);
    }

    @Test
    void theGrandmaAvatarsHelpIsRecordedLikeAnyHelpAndContinuesTheJourney() {
        HelpRequest request = burning();

        GrandmaHelpOutcome outcome = service.receiveGrandmaHelp(grandmaStayCalm(request), INCOMING_CORRELATION);

        assertThat(outcome).isEqualTo(GrandmaHelpOutcome.RECORDED);
        Help help = helps.findById(new HelpId(GRANDMA_HELP_ID)).orElseThrow();
        assertThat(help.providerType()).isEqualTo(HelpProviderType.GRANDMA_AVATAR);
        assertThat(help.helpProvider()).isEmpty();
        assertThat(request.status()).isEqualTo(HelpRequestStatus.ANSWERED);
        assertThat(events.providedCorrelationIds).containsExactly(INCOMING_CORRELATION);
    }

    @Test
    void aRedeliveredGrandmaHelpChangesNothing() {
        HelpRequest request = burning();
        service.receiveGrandmaHelp(grandmaStayCalm(request), INCOMING_CORRELATION);

        GrandmaHelpOutcome outcome = service.receiveGrandmaHelp(grandmaStayCalm(request), INCOMING_CORRELATION);

        assertThat(outcome).isEqualTo(GrandmaHelpOutcome.DUPLICATE);
        assertThat(helps.store).hasSize(1);
        assertThat(events.provided).hasSize(1);
    }

    @Test
    void aGrandmaHelpForAWithdrawnRequestIsNotFound() {
        HelpRequest request = burning();
        requestService.withdraw(COOK, request.id());

        assertThatThrownBy(() -> service.receiveGrandmaHelp(grandmaStayCalm(request), INCOMING_CORRELATION))
                .isInstanceOf(NotFoundException.class);
        assertThat(events.provided).isEmpty();
    }

    @Test
    void aGrandmaHelpMustFitTheRequest() {
        HelpRequest chefRequest = requestService.raise(COOK, dinnerForTheInLaws());
        HelpRequest burning = burning();

        assertThatThrownBy(() -> service.receiveGrandmaHelp(new GrandmaHelp(HelpId.newId(), chefRequest.id(), COOK,
                "Menu", threeCourses()), INCOMING_CORRELATION))
                .isInstanceOf(HelpRuleViolationException.class)
                .extracting(e -> ((HelpRuleViolationException) e).code())
                .isEqualTo(HelpRuleViolationException.PROVIDER_NOT_PREFERRED);
        assertThatThrownBy(() -> service.receiveGrandmaHelp(new GrandmaHelp(HelpId.newId(), burning.id(), OTHER_COOK,
                "Stay calm", stayCalm()), INCOMING_CORRELATION))
                .isInstanceOf(HelpRuleViolationException.class);
        assertThatThrownBy(() -> service.receiveGrandmaHelp(new GrandmaHelp(HelpId.newId(), burning.id(), COOK,
                "Cold pan", useAColdPan()), INCOMING_CORRELATION))
                .isInstanceOf(HelpRuleViolationException.class);
        assertThat(helps.store).isEmpty();
        assertThat(events.provided).isEmpty();
    }

    @Test
    void onlyTheProviderRevisesAHelp() {
        HelpRequest request = burning();
        Help help = service.give(OTHER_COOK, request.id(), HelpProviderType.COMMUNITY, "Stay calm", stayCalm());
        var breathe = new CatastropheMitigation(Optional.empty(), "Breathe");

        assertThatThrownBy(() -> service.revise(COOK, help.id(), "Mine now", null)).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.revise(OTHER_COOK, help.id(), null, useAColdPan()))
                .isInstanceOf(HelpRuleViolationException.class);

        assertThat(service.revise(OTHER_COOK, help.id(), null, breathe).answer()).isEqualTo(breathe);
    }

    @Test
    void nobodyChangesTheGrandmaAvatarsHelpOverRest() {
        HelpRequest request = burning();
        service.receiveGrandmaHelp(grandmaStayCalm(request), INCOMING_CORRELATION);

        assertThatThrownBy(() -> service.withdraw(COOK, new HelpId(GRANDMA_HELP_ID))).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.revise(COOK, new HelpId(GRANDMA_HELP_ID), "x", null))
                .isInstanceOf(NotPermittedException.class);
    }

    @Test
    void withdrawingTheLastHelpOpensTheRequestAgain() {
        HelpRequest request = burning();
        Help first = service.give(OTHER_COOK, request.id(), HelpProviderType.COMMUNITY, "Stay calm", stayCalm());
        Help second = service.give(COOK, request.id(), HelpProviderType.COMMUNITY, "Found it", stayCalm());

        assertThatThrownBy(() -> service.withdraw(COOK, first.id())).isInstanceOf(NotPermittedException.class);
        service.withdraw(OTHER_COOK, first.id());
        assertThat(request.status()).isEqualTo(HelpRequestStatus.ANSWERED);

        service.withdraw(COOK, second.id());
        assertThat(request.status()).isEqualTo(HelpRequestStatus.OPEN);
        assertThat(helps.store).isEmpty();
    }

    @Test
    void aChefsHelpIsSeenOnlyByTheRequester() {
        HelpRequest request = requestService.raise(COOK, dinnerForTheInLaws());
        Help chef = service.give(OTHER_COOK, request.id(), HelpProviderType.CHEF, "Chef's menu", threeCourses());
        CookId stranger = new CookId(UUID.randomUUID());

        assertThat(service.help(COOK, chef.id())).isEqualTo(chef);
        assertThatThrownBy(() -> service.help(stranger, chef.id())).isInstanceOf(NotPermittedException.class);
        assertThat(service.helps(COOK, null, null, null)).containsExactly(chef);
        assertThat(service.helps(stranger, null, null, null)).isEmpty();
    }

    @Test
    void helpsAreFilteredByRequestTypeAndProvider() {
        HelpRequest burning = burning();
        HelpRequest folding = requestService.raise(COOK, foldingTheDough());
        Help calm = service.give(OTHER_COOK, burning.id(), HelpProviderType.COMMUNITY, "Stay calm", stayCalm());
        service.receiveGrandmaHelp(new GrandmaHelp(HelpId.newId(), folding.id(), COOK, "Cold pan", useAColdPan()),
                INCOMING_CORRELATION);

        assertThat(service.helps(COOK, burning.id(), null, null)).containsExactly(calm);
        assertThat(service.helps(COOK, null, HelpType.STEPS_TO_MITIGATE_CATASTROPHE, null)).containsExactly(calm);
        assertThat(service.helps(COOK, null, null, OTHER_COOK)).containsExactly(calm);
        assertThat(service.helps(COOK, null, null, null)).hasSize(2);
    }

    @Test
    void anUnknownHelpIsNotFound() {
        assertThatThrownBy(() -> service.help(COOK, HelpId.newId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.withdraw(COOK, HelpId.newId())).isInstanceOf(NotFoundException.class);
    }
}
