package org.larder.sharing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.larder.sharing.TestData.THIRD_COOK;
import static org.larder.sharing.TestData.chefHelp;
import static org.larder.sharing.TestData.communityHelp;
import static org.larder.sharing.TestData.grandmaHelp;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelpTest {

    @Test
    void onlyTheRequesterReceivedTheHelp() {
        assertThat(communityHelp().isReceivedBy(COOK)).isTrue();
        assertThat(communityHelp().isReceivedBy(OTHER_COOK)).isFalse();
    }

    @Test
    void thanksGoToTheHelperOfTheHelp() {
        assertThatCode(() -> communityHelp().verifyAddressedToHelper(List.of(Recipient.cooks(OTHER_COOK))))
                .doesNotThrowAnyException();
        assertThatCode(() -> grandmaHelp().verifyAddressedToHelper(List.of(Recipient.grandmaAvatar())))
                .doesNotThrowAnyException();
        assertThatCode(() -> chefHelp().verifyAddressedToHelper(List.of(Recipient.chef("Chef Jamie"))))
                .doesNotThrowAnyException();
        assertThatCode(() -> grandmaHelp().verifyAddressedToHelper(List.of())).doesNotThrowAnyException();
    }

    @Test
    void aRecipientOfAnotherKindThanTheHelperIsRejected() {
        assertNotHelper(() -> grandmaHelp().verifyAddressedToHelper(List.of(Recipient.cooks(OTHER_COOK))));
        assertNotHelper(() -> communityHelp().verifyAddressedToHelper(List.of(Recipient.grandmaAvatar())));
        assertNotHelper(() -> communityHelp().verifyAddressedToHelper(List.of(Recipient.chef("Chef Jamie"))));
        assertNotHelper(() -> chefHelp().verifyAddressedToHelper(List.of(Recipient.cooks(OTHER_COOK))));
    }

    @Test
    void aMentionedCookMustHaveGivenTheHelp() {
        assertNotHelper(() -> communityHelp().verifyAddressedToHelper(List.of(Recipient.cooks(THIRD_COOK))));
        assertNotHelper(() -> communityHelp().verifyAddressedToHelper(List.of(Recipient.cooks(OTHER_COOK, THIRD_COOK))));
    }

    private static void assertNotHelper(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable).isInstanceOf(ThanksRuleViolationException.class)
                .extracting(e -> ((ThanksRuleViolationException) e).code())
                .isEqualTo(ThanksRuleViolationException.RECIPIENT_NOT_HELPER);
    }
}
