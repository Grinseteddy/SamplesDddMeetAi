package org.larder.sharing.domain;

import static org.larder.sharing.domain.ThanksRuleViolationException.RECIPIENT_NOT_HELPER;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A help of Cooking Assistance as Sharing needs it: who received it and who gave it.
 *
 * @param requester the cook who asked for the help - the only one who may thank for it
 * @param helperKind who gave the help
 * @param helper the cook who gave the help; absent for the Grandma Avatar
 */
public record Help(HelpId id, CookId requester, HelperKind helperKind, Optional<CookId> helper) {

    public Help {
        Objects.requireNonNull(id, "helpId must not be null");
        Objects.requireNonNull(requester, "requester must not be null");
        Objects.requireNonNull(helperKind, "helperKind must not be null");
        helper = helper == null ? Optional.empty() : helper;
    }

    public boolean isReceivedBy(CookId cook) {
        return requester.equals(cook);
    }

    /**
     * Thanks for this help are addressed only to who gave it: every recipient has the type of the
     * helper, and a recipient of type Cook mentions no cook but the helping one. A chef name cannot be
     * checked - the help names the chef's cook account, not a chef name.
     */
    public void verifyAddressedToHelper(List<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            if (recipient.type() != helperKind.thankedAs()) {
                throw new ThanksRuleViolationException(RECIPIENT_NOT_HELPER, "Help " + id.value() + " was given by "
                        + helperKind + "; it cannot be thanked to a recipient of type " + recipient.type());
            }
            for (CookId cook : recipient.cooks()) {
                if (helper.filter(cook::equals).isEmpty()) {
                    throw new ThanksRuleViolationException(RECIPIENT_NOT_HELPER,
                            "Cook " + cook.value() + " did not give help " + id.value());
                }
            }
        }
    }
}
