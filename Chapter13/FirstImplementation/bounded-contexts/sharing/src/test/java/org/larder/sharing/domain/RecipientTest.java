package org.larder.sharing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.OTHER_COOK;

import java.util.List;

import org.junit.jupiter.api.Test;

class RecipientTest {

    @Test
    void aCookRecipientMentionsCooksOnly() {
        Recipient recipient = Recipient.cooks(OTHER_COOK, COOK);

        assertThat(recipient.type()).isEqualTo(RecipientType.COOK);
        assertThat(recipient.cooks()).containsExactly(OTHER_COOK, COOK);
        assertThat(recipient.chef()).isEmpty();
    }

    @Test
    void aCookRecipientMentionsAtLeastOneCookEachOnce() {
        assertViolation(() -> Recipient.name(RecipientType.COOK, null, List.of()));
        assertViolation(() -> Recipient.name(RecipientType.COOK, null, null));
        assertViolation(() -> Recipient.cooks(OTHER_COOK, OTHER_COOK));
    }

    @Test
    void onlyAChefCarriesAChefName() {
        assertThat(Recipient.chef("Chef Jamie").chef()).contains("Chef Jamie");
        assertThat(Recipient.name(RecipientType.CHEF, null, List.of()).chef()).isEmpty();

        assertViolation(() -> Recipient.name(RecipientType.COOK, "Chef Jamie", List.of(OTHER_COOK)));
        assertViolation(() -> Recipient.name(RecipientType.GRANDMA_AVATAR, "Grandma", List.of()));
    }

    @Test
    void aChefNameIsNotBlankAndAtMost200Characters() {
        assertViolation(() -> Recipient.chef(" "));
        assertViolation(() -> Recipient.chef("x".repeat(201)));
        assertThat(Recipient.chef("x".repeat(200)).chefName()).hasSize(200);
    }

    @Test
    void onlyACookRecipientMentionsCooks() {
        assertViolation(() -> Recipient.name(RecipientType.CHEF, "Chef Jamie", List.of(OTHER_COOK)));
        assertViolation(() -> Recipient.name(RecipientType.GRANDMA_AVATAR, null, List.of(OTHER_COOK)));
        assertThat(Recipient.grandmaAvatar().cooks()).isEmpty();
    }

    @Test
    void aRecipientNeedsAType() {
        assertViolation(() -> Recipient.name(null, null, List.of()));
    }

    private static void assertViolation(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(ThanksRuleViolationException.class)
                .extracting(e -> ((ThanksRuleViolationException) e).code())
                .isEqualTo(ThanksRuleViolationException.INVALID_RECIPIENT);
    }
}
