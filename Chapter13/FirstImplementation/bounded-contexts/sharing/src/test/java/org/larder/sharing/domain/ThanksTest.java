package org.larder.sharing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.HELP;
import static org.larder.sharing.TestData.NOW;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.larder.sharing.TestData.OTHER_PICTURE;
import static org.larder.sharing.TestData.PICTURE;
import static org.larder.sharing.TestData.TEXT;
import static org.larder.sharing.TestData.THIRD_COOK;

import java.util.List;

import org.junit.jupiter.api.Test;

class ThanksTest {

    @Test
    void givenThanksKeepGiverHelpAndTime() {
        Thanks thanks = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);

        assertThat(thanks.isGivenBy(COOK)).isTrue();
        assertThat(thanks.isGivenBy(OTHER_COOK)).isFalse();
        assertThat(thanks.help()).isEqualTo(HELP);
        assertThat(thanks.createdAt()).isEqualTo(NOW);
        assertThat(thanks.updatedAt()).isEqualTo(NOW);
        assertThat(thanks.mentionedCooks()).containsExactly(OTHER_COOK);
        assertThat(thanks.version()).isZero();
    }

    @Test
    void thanksMayNameNobody() {
        assertThat(Thanks.give(COOK, HELP, List.of(), TEXT, PICTURE, NOW).recipients()).isEmpty();
        assertThat(Thanks.give(COOK, HELP, null, TEXT, PICTURE, NOW).recipients()).isEmpty();
    }

    @Test
    void eachTypeOfRecipientAppearsOnce() {
        assertThatThrownBy(() -> Thanks.give(COOK, HELP,
                List.of(Recipient.cooks(OTHER_COOK), Recipient.cooks(THIRD_COOK)), TEXT, PICTURE, NOW))
                .isInstanceOf(ThanksRuleViolationException.class)
                .extracting(e -> ((ThanksRuleViolationException) e).code())
                .isEqualTo(ThanksRuleViolationException.DUPLICATE_RECIPIENT);
    }

    @Test
    void theTextIsNotBlankAndAtMost2000Characters() {
        assertThatThrownBy(() -> Thanks.give(COOK, HELP, List.of(), " ", PICTURE, NOW))
                .isInstanceOf(ThanksRuleViolationException.class);
        assertThatThrownBy(() -> Thanks.give(COOK, HELP, List.of(), "x".repeat(2001), PICTURE, NOW))
                .isInstanceOf(ThanksRuleViolationException.class);
        assertThat(Thanks.give(COOK, HELP, List.of(), "x".repeat(2000), PICTURE, NOW).text()).hasSize(2000);
    }

    @Test
    void aRevisionChangesOnlyWhatItNames() {
        Thanks thanks = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);

        thanks.revise(new ThanksRevision(null, "Thanks again!", null), NOW.plusSeconds(60));

        assertThat(thanks.text()).isEqualTo("Thanks again!");
        assertThat(thanks.picture()).isEqualTo(PICTURE);
        assertThat(thanks.mentionedCooks()).containsExactly(OTHER_COOK);
        assertThat(thanks.updatedAt()).isEqualTo(NOW.plusSeconds(60));
        assertThat(thanks.createdAt()).isEqualTo(NOW);
    }

    @Test
    void givenRecipientsReplaceAllAndAnEmptyListNamesNobody() {
        Thanks thanks = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);

        thanks.revise(new ThanksRevision(List.of(), null, OTHER_PICTURE), NOW.plusSeconds(60));

        assertThat(thanks.recipients()).isEmpty();
        assertThat(thanks.picture()).isEqualTo(OTHER_PICTURE);
        assertThat(thanks.text()).isEqualTo(TEXT);
    }

    @Test
    void aRevisionBreakingARuleChangesNothing() {
        Thanks thanks = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);

        assertThatThrownBy(() -> thanks.revise(new ThanksRevision(
                List.of(Recipient.grandmaAvatar(), Recipient.grandmaAvatar()), "new text", OTHER_PICTURE), NOW.plusSeconds(60)))
                .isInstanceOf(ThanksRuleViolationException.class);

        assertThat(thanks.text()).isEqualTo(TEXT);
        assertThat(thanks.picture()).isEqualTo(PICTURE);
        assertThat(thanks.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void aRevisionOfNothingLeavesTheThanksUntouched() {
        Thanks thanks = Thanks.give(COOK, HELP, List.of(), TEXT, PICTURE, NOW);

        thanks.revise(ThanksRevision.none(), NOW.plusSeconds(60));

        assertThat(thanks.updatedAt()).isEqualTo(NOW);
    }
}
