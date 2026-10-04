package org.larder.cookingassistance.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.OTHER_COOK;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.noButtermilk;
import static org.larder.cookingassistance.TestData.stayCalm;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.InMemoryAdapters.InMemoryHelpRequests;
import org.larder.cookingassistance.application.InMemoryAdapters.InMemoryHelps;
import org.larder.cookingassistance.application.InMemoryAdapters.RecordedEvents;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestRevision;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.HelpRuleViolationException;

class HelpRequestServiceTest {

    private final InMemoryHelpRequests requests = new InMemoryHelpRequests();
    private final InMemoryHelps helps = new InMemoryHelps();
    private final RecordedEvents events = new RecordedEvents();
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final HelpRequestService service = new HelpRequestService(requests, helps, events, clock);
    private final HelpService helpService = new HelpService(requests, helps, events, clock);

    @Test
    void raisingARequestStoresItAndAnnouncesHelpRequested() {
        HelpRequest request = service.raise(COOK, burningCatastrophe());

        assertThat(requests.findById(request.id())).isPresent();
        assertThat(request.isRaisedBy(COOK)).isTrue();
        assertThat(events.requested).containsExactly(request);
    }

    @Test
    void anInvalidRequestIsNeitherStoredNorAnnounced() {
        var invalid = new org.larder.cookingassistance.domain.HelpRequestDraft("No recipe", noButtermilk().type(),
                "?", null, null, noButtermilk().ingredients(), noButtermilk().preferredProviders());

        assertThatThrownBy(() -> service.raise(COOK, invalid)).isInstanceOf(HelpRuleViolationException.class);
        assertThat(requests.store).isEmpty();
        assertThat(events.requested).isEmpty();
    }

    @Test
    void onlyTheRequesterRevisesOrWithdrawsARequest() {
        HelpRequest request = service.raise(COOK, burningCatastrophe());
        HelpRequestRevision newTitle = HelpRequestRevision.none().withTitle("Burnt");

        assertThatThrownBy(() -> service.revise(OTHER_COOK, request.id(), newTitle)).isInstanceOf(NotPermittedException.class);
        assertThatThrownBy(() -> service.withdraw(OTHER_COOK, request.id())).isInstanceOf(NotPermittedException.class);

        assertThat(service.revise(COOK, request.id(), newTitle).title()).isEqualTo("Burnt");
        service.withdraw(COOK, request.id());
        assertThat(requests.store).isEmpty();
    }

    @Test
    void unknownRequestsAreNotFound() {
        HelpRequestId unknown = HelpRequestId.newId();
        assertThatThrownBy(() -> service.helpRequest(unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.revise(COOK, unknown, HelpRequestRevision.none())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.withdraw(COOK, unknown)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void statusAnsweredDependsOnStoredHelps() {
        HelpRequest request = service.raise(COOK, burningCatastrophe());
        HelpRequestRevision answered = HelpRequestRevision.none().withStatus(HelpRequestStatus.ANSWERED);

        assertThatThrownBy(() -> service.revise(COOK, request.id(), answered))
                .isInstanceOf(HelpRuleViolationException.class)
                .extracting(e -> ((HelpRuleViolationException) e).code())
                .isEqualTo(HelpRuleViolationException.ANSWERED_WITHOUT_HELP);
    }

    @Test
    void anAnsweredRequestCannotBeWithdrawn() {
        HelpRequest request = service.raise(COOK, burningCatastrophe());
        helpService.give(OTHER_COOK, request.id(), HelpProviderType.COMMUNITY, "Stay calm", stayCalm());

        assertThatThrownBy(() -> service.withdraw(COOK, request.id()))
                .isInstanceOf(HelpRuleViolationException.class)
                .extracting(e -> ((HelpRuleViolationException) e).code())
                .isEqualTo(HelpRuleViolationException.HELP_REQUEST_NOT_OPEN);
        assertThat(requests.store).containsKey(request.id());
    }

    @Test
    void listsFilterByRequesterAndStatus() {
        HelpRequest mine = service.raise(COOK, burningCatastrophe());
        HelpRequest theirs = service.raise(OTHER_COOK, noButtermilk());
        helpService.give(COOK, theirs.id(), HelpProviderType.COMMUNITY, "Yoghurt", org.larder.cookingassistance.TestData.yoghurtForButtermilk());

        assertThat(service.helpRequests(null, null)).containsExactly(mine, theirs);
        assertThat(service.helpRequests(COOK, null)).containsExactly(mine);
        assertThat(service.helpRequests(null, HelpRequestStatus.OPEN)).containsExactly(mine);
        assertThat(service.helpRequests(OTHER_COOK, HelpRequestStatus.ANSWERED)).containsExactly(theirs);
    }
}
