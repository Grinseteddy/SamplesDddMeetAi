package org.larder.grandmaavatar.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.grandmaavatar.TestData;
import org.larder.grandmaavatar.Transactions;
import org.larder.grandmaavatar.application.HandledRequestRecorder;
import org.larder.grandmaavatar.domain.Advice;
import org.larder.grandmaavatar.domain.CatastropheMitigation;
import org.larder.grandmaavatar.domain.HandledHelpRequest;
import org.larder.grandmaavatar.domain.Help;
import org.larder.grandmaavatar.domain.HelpRequest;
import org.larder.grandmaavatar.domain.Outcome;
import org.larder.platform.persistence.BoundedContextDatabase;
import org.larder.platform.test.TestDatabase;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcHandledHelpRequestsTest {

    private static final Instant HANDLED_AT = Instant.parse("2026-10-03T16:31:00Z");

    private BoundedContextDatabase database;
    private JdbcHandledHelpRequests notebook;

    @BeforeEach
    void freshSchema() {
        database = TestDatabase.forSchema("grandmaavatar");
        notebook = new JdbcHandledHelpRequests(database.jdbcClient());
    }

    @Test
    void notesAnAnsweredRequestWithItsHelp() {
        HandledHelpRequest answered = HandledHelpRequest.answered(stayCalm(TestData.burningScones()), HANDLED_AT);

        assertThat(notebook.add(answered)).isTrue();

        assertThat(notebook.find(answered.helpRequest())).contains(answered);
    }

    @Test
    void notesARequestGrandmaDidNotAnswer() {
        HelpRequest request = TestData.chefOnlyMenu();
        HandledHelpRequest ignored = HandledHelpRequest.notAnswered(request, Outcome.NOT_FOR_GRANDMA, HANDLED_AT);

        assertThat(notebook.add(ignored)).isTrue();

        assertThat(notebook.find(request.id())).contains(ignored);
    }

    @Test
    void notesEveryRequestOnlyOnce() {
        HelpRequest request = TestData.burningScones();
        HandledHelpRequest first = HandledHelpRequest.answered(stayCalm(request), HANDLED_AT);

        assertThat(notebook.add(first)).isTrue();
        assertThat(notebook.add(HandledHelpRequest.notAnswered(request, Outcome.NO_ADVICE, HANDLED_AT.plusSeconds(1))))
                .isFalse();

        assertThat(notebook.find(request.id())).contains(first);
    }

    @Test
    void anUnknownRequestIsNotNoted() {
        assertThat(notebook.find(TestData.newId())).isEmpty();
    }

    @Test
    void theNoteIsRolledBackWhenTheHelpCannotBePosted() {
        var transactionManager = new DataSourceTransactionManager(database.dataSource());
        HandledRequestRecorder recorder = Transactions.transactional(new HandledRequestRecorder(notebook, (help, correlation) -> {
            throw new IllegalStateException("outbox unavailable");
        }), transactionManager);
        HelpRequest request = TestData.burningScones();
        Help help = stayCalm(request);

        assertThatThrownBy(() -> recorder.record(HandledHelpRequest.answered(help, HANDLED_AT), Optional.of(help),
                UUID.randomUUID())).isInstanceOf(IllegalStateException.class);

        assertThat(notebook.find(request.id())).isEmpty();
    }

    private static Help stayCalm(HelpRequest request) {
        return Help.answer(request, new Advice("Stay calm", new CatastropheMitigation(request.recipe(), "use a new, cold pan")));
    }
}
