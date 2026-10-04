package org.larder.sharing.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.HELP;
import static org.larder.sharing.TestData.NOW;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.larder.sharing.TestData.OTHER_HELP;
import static org.larder.sharing.TestData.OTHER_PICTURE;
import static org.larder.sharing.TestData.PICTURE;
import static org.larder.sharing.TestData.TEXT;
import static org.larder.sharing.TestData.THIRD_COOK;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.platform.test.TestDatabase;
import org.larder.sharing.application.ConcurrentChangeException;
import org.larder.sharing.application.ThanksFilter;
import org.larder.sharing.domain.HelpId;
import org.larder.sharing.domain.Recipient;
import org.larder.sharing.domain.Thanks;
import org.larder.sharing.domain.ThanksId;
import org.larder.sharing.domain.ThanksRevision;
import org.larder.sharing.domain.ThanksRuleViolationException;

/** Runs the real migrations against PostgreSQL in its own schema with its own user. */
class JdbcThanksRepositoryTest {

    private JdbcThanksRepository thanks;

    @BeforeEach
    void freshSchema() {
        thanks = new JdbcThanksRepository(TestDatabase.forSchema("sharing").jdbcClient());
    }

    private static HelpId anotherHelp() {
        return new HelpId(UUID.randomUUID());
    }

    @Test
    void storesThanksWithTheirRecipientsInOrder() {
        Thanks given = Thanks.give(COOK, HELP,
                List.of(Recipient.cooks(OTHER_COOK, THIRD_COOK), Recipient.chef("Chef Jamie"), Recipient.grandmaAvatar()),
                TEXT, PICTURE, NOW);
        thanks.add(given);

        Thanks stored = thanks.findById(given.id()).orElseThrow();

        assertThat(stored.giver()).isEqualTo(COOK);
        assertThat(stored.help()).isEqualTo(HELP);
        assertThat(stored.text()).isEqualTo(TEXT);
        assertThat(stored.picture()).isEqualTo(PICTURE);
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(stored.updatedAt()).isEqualTo(NOW);
        assertThat(stored.recipients()).containsExactlyElementsOf(given.recipients());
        assertThat(stored.recipients().get(0).cooks()).containsExactly(OTHER_COOK, THIRD_COOK);
        assertThat(stored.version()).isZero();
    }

    @Test
    void thanksAreGivenOncePerHelp() {
        thanks.add(Thanks.give(COOK, HELP, List.of(), TEXT, PICTURE, NOW));

        assertThat(thanks.existsForHelp(HELP)).isTrue();
        assertThat(thanks.existsForHelp(OTHER_HELP)).isFalse();
        assertThatThrownBy(() -> thanks.add(Thanks.give(COOK, HELP, List.of(), TEXT, PICTURE, NOW)))
                .isInstanceOf(ThanksRuleViolationException.class)
                .extracting(e -> ((ThanksRuleViolationException) e).code())
                .isEqualTo(ThanksRuleViolationException.THANKS_ALREADY_GIVEN);
    }

    @Test
    void storesAChangeAndReplacesTheRecipients() {
        Thanks given = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);
        thanks.add(given);

        Thanks loaded = thanks.findById(given.id()).orElseThrow();
        loaded.revise(new ThanksRevision(List.of(Recipient.grandmaAvatar()), "Thanks again!", OTHER_PICTURE),
                NOW.plusSeconds(60));
        thanks.update(loaded);

        Thanks stored = thanks.findById(given.id()).orElseThrow();
        assertThat(stored.text()).isEqualTo("Thanks again!");
        assertThat(stored.picture()).isEqualTo(OTHER_PICTURE);
        assertThat(stored.recipients()).containsExactlyElementsOf(loaded.recipients());
        assertThat(stored.updatedAt()).isEqualTo(NOW.plusSeconds(60));
        assertThat(stored.createdAt()).isEqualTo(NOW);
        assertThat(stored.version()).isEqualTo(1);
        assertThat(thanks.find(new ThanksFilter(null, OTHER_COOK))).isEmpty();
    }

    @Test
    void aChangeOnAStaleVersionIsRejected() {
        Thanks given = Thanks.give(COOK, HELP, List.of(), TEXT, PICTURE, NOW);
        thanks.add(given);
        Thanks first = thanks.findById(given.id()).orElseThrow();
        Thanks second = thanks.findById(given.id()).orElseThrow();
        first.revise(new ThanksRevision(null, "First", null), NOW);
        thanks.update(first);

        second.revise(new ThanksRevision(List.of(Recipient.grandmaAvatar()), "Second", null), NOW);
        assertThatThrownBy(() -> thanks.update(second)).isInstanceOf(ConcurrentChangeException.class);

        Thanks stored = thanks.findById(given.id()).orElseThrow();
        assertThat(stored.text()).isEqualTo("First");
        assertThat(stored.recipients()).isEmpty();
    }

    @Test
    void filtersByGiverAndByMentionedCookNewestFirst() {
        Thanks byCookForOther = Thanks.give(COOK, anotherHelp(), List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);
        Thanks byCookForThird = Thanks.give(COOK, anotherHelp(), List.of(Recipient.cooks(THIRD_COOK)), TEXT, PICTURE,
                NOW.plusSeconds(10));
        Thanks byOtherForThird = Thanks.give(OTHER_COOK, anotherHelp(), List.of(Recipient.cooks(THIRD_COOK)), TEXT,
                PICTURE, NOW.plusSeconds(20));
        Thanks byOtherToGrandma = Thanks.give(OTHER_COOK, anotherHelp(), List.of(Recipient.grandmaAvatar()), TEXT,
                PICTURE, NOW.plusSeconds(30));
        List.of(byCookForOther, byCookForThird, byOtherForThird, byOtherToGrandma).forEach(thanks::add);

        assertThat(thanks.find(ThanksFilter.all())).extracting(Thanks::id)
                .containsExactly(byOtherToGrandma.id(), byOtherForThird.id(), byCookForThird.id(), byCookForOther.id());
        assertThat(thanks.find(new ThanksFilter(COOK, null))).extracting(Thanks::id)
                .containsExactly(byCookForThird.id(), byCookForOther.id());
        assertThat(thanks.find(new ThanksFilter(null, THIRD_COOK))).extracting(Thanks::id)
                .containsExactly(byOtherForThird.id(), byCookForThird.id());
        assertThat(thanks.find(new ThanksFilter(COOK, THIRD_COOK))).extracting(Thanks::id)
                .containsExactly(byCookForThird.id());
        assertThat(thanks.find(new ThanksFilter(THIRD_COOK, null))).isEmpty();
        assertThat(thanks.find(new ThanksFilter(OTHER_COOK, null)).get(0).recipients())
                .containsExactlyElementsOf(byOtherToGrandma.recipients());
    }

    @Test
    void removesThanksWithTheirRecipients() {
        Thanks given = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);
        thanks.add(given);

        assertThat(thanks.remove(given.id())).isTrue();

        assertThat(thanks.findById(given.id())).isEmpty();
        assertThat(thanks.find(new ThanksFilter(null, OTHER_COOK))).isEmpty();
        assertThat(thanks.remove(given.id())).isFalse();
        assertThat(thanks.existsForHelp(HELP)).isFalse();
    }

    @Test
    void unknownThanksAreNotThere() {
        assertThat(thanks.findById(ThanksId.newId())).isEmpty();
        assertThat(thanks.find(ThanksFilter.all())).isEmpty();
    }
}
