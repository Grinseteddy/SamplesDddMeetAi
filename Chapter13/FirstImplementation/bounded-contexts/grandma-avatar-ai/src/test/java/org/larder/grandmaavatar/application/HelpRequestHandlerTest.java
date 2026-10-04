package org.larder.grandmaavatar.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.grandmaavatar.TestData.CORRELATION;
import static org.larder.grandmaavatar.TestData.FOLD_IN;
import static org.larder.grandmaavatar.TestData.SCONES;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.TestData;
import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.CatastropheMitigation;
import org.larder.grandmaavatar.domain.HelpId;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.InvalidAnswerException;
import org.larder.grandmaavatar.domain.Outcome;
import org.larder.grandmaavatar.domain.PreparationStepExplanation;

class HelpRequestHandlerTest {

    private static final Instant NOW = Instant.parse("2026-10-03T16:30:00Z");

    private final InMemoryHandledHelpRequests notebook = new InMemoryHandledHelpRequests();
    private final RecordingHelpPublisher publisher = new RecordingHelpPublisher();
    private final FakeAdvisor advisor = new FakeAdvisor();
    private final HelpRequestHandler handler = new HelpRequestHandler(notebook, advisor,
            new HandledRequestRecorder(notebook, publisher), Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void answersAnOpenRequestThatNamesGrandmaAndKeepsTheCorrelation() {
        advisor.answer(request -> Optional.of(new Advice("Stay calm",
                new CatastropheMitigation(request.recipe(), "use a new, cold pan"))));
        HelpRequest request = TestData.burningScones();

        var handling = handler.handle(request, CORRELATION);

        assertThat(handling.firstTime()).isTrue();
        assertThat(handling.handled().outcome()).isEqualTo(Outcome.ANSWERED);
        assertThat(handling.handled().help()).contains(HelpId.forRequest(request.id()));
        assertThat(handling.handled().handledAt()).isEqualTo(NOW);
        assertThat(publisher.published).hasSize(1);
        assertThat(publisher.published.get(0).help().helpRequest()).isEqualTo(request.id());
        assertThat(publisher.published.get(0).correlationId()).isEqualTo(CORRELATION);
        assertThat(notebook.find(request.id())).contains(handling.handled());
    }

    @Test
    void aChefOnlyRequestIsNotedButNotAnswered() {
        HelpRequest request = TestData.chefOnlyMenu();

        var handling = handler.handle(request, CORRELATION);

        assertThat(handling.handled().outcome()).isEqualTo(Outcome.NOT_FOR_GRANDMA);
        assertThat(advisor.asked).isZero();
        assertThat(publisher.published).isEmpty();
        assertThat(notebook.find(request.id())).isPresent();
    }

    @Test
    void withoutAdviceNothingIsPublished() {
        advisor.answer(request -> Optional.empty());

        var handling = handler.handle(TestData.noButtermilk(), CORRELATION);

        assertThat(handling.handled().outcome()).isEqualTo(Outcome.NO_ADVICE);
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void adviceBreakingTheInvariantsOfAHelpNeverLeavesGrandma() {
        advisor.answer(request -> Optional.of(new Advice("Wrong type",
                new PreparationStepExplanation(SCONES, FOLD_IN, "fold", List.of()))));

        var handling = handler.handle(TestData.burningScones(), CORRELATION);

        assertThat(handling.handled().outcome()).isEqualTo(Outcome.ADVICE_REJECTED);
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void anAdvisorUnableToBuildAValidAnswerIsRejectedToo() {
        advisor.answer(request -> {
            throw new InvalidAnswerException("explanation must not be blank");
        });

        assertThat(handler.handle(TestData.burningScones(), CORRELATION).handled().outcome())
                .isEqualTo(Outcome.ADVICE_REJECTED);
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void anUnavailableAdvisorFailsTheHandlingSoItIsRetried() {
        advisor.answer(request -> {
            throw new AdvisorUnavailableException("timeout", null);
        });
        HelpRequest request = TestData.burningScones();

        assertThatThrownBy(() -> handler.handle(request, CORRELATION)).isInstanceOf(AdvisorUnavailableException.class);
        assertThat(notebook.find(request.id())).isEmpty();
        assertThat(publisher.published).isEmpty();
    }

    @Test
    void aRequestDeliveredTwiceIsAnsweredOnce() {
        advisor.answer(request -> Optional.of(new Advice("Stay calm", new CatastropheMitigation(request.recipe(), "breathe"))));
        HelpRequest request = TestData.burningScones();

        var first = handler.handle(request, CORRELATION);
        var second = handler.handle(request, UUID.randomUUID());

        assertThat(first.firstTime()).isTrue();
        assertThat(second.firstTime()).isFalse();
        assertThat(second.handled()).isEqualTo(first.handled());
        assertThat(advisor.asked).isEqualTo(1);
        assertThat(publisher.published).hasSize(1);
    }

    @Test
    void aDuplicateThatWinsTheRaceLeavesTheOtherWithoutAnswer() {
        advisor.answer(request -> Optional.of(new Advice("Stay calm", new CatastropheMitigation(request.recipe(), "breathe"))));
        notebook.hideOnFind = true;
        HelpRequest request = TestData.burningScones();
        handler.handle(request, CORRELATION);

        var second = handler.handle(request, CORRELATION);

        assertThat(second.firstTime()).isFalse();
        assertThat(publisher.published).hasSize(1);
    }

    private static final class FakeAdvisor implements HelpAdvisor {
        private Function<HelpRequest, Optional<Advice>> answers = request -> Optional.empty();
        private int asked;

        void answer(Function<HelpRequest, Optional<Advice>> answers) {
            this.answers = answers;
        }

        @Override
        public Optional<Advice> advise(HelpRequest request) {
            asked++;
            return answers.apply(request);
        }
    }
}
