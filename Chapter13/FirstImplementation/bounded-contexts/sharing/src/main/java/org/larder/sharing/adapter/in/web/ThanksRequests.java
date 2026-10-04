package org.larder.sharing.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.larder.platform.security.CurrentCook;
import org.larder.sharing.adapter.in.web.model.RecipientCreate;
import org.larder.sharing.adapter.in.web.model.ThanksUpdate;
import org.larder.sharing.domain.CookId;
import org.larder.sharing.domain.Picture;
import org.larder.sharing.domain.Recipient;
import org.larder.sharing.domain.RecipientType;
import org.larder.sharing.domain.ThanksRevision;

/**
 * Translates the contract's request bodies into the domain. Bean validation of the generated model has
 * checked the contract's constraints; the domain checks the recipient rules. Thanks to
 * {@code containerDefaultToNull}, an absent list is {@code null} and can be told from an empty one.
 */
final class ThanksRequests {

    private ThanksRequests() {
    }

    static CookId caller() {
        return new CookId(CurrentCook.require().value());
    }

    /** Absent recipients address the thanks to nobody. */
    static List<Recipient> toRecipients(List<RecipientCreate> recipients) {
        return recipients == null ? List.of() : recipients.stream().map(ThanksRequests::toRecipient).toList();
    }

    /** Absent (or {@code null}) properties leave the thanks as they are; given recipients replace all. */
    static ThanksRevision toRevision(ThanksUpdate request) {
        if (request == null) {
            return ThanksRevision.none();
        }
        return new ThanksRevision(
                request.getRecipients() == null ? null : toRecipients(request.getRecipients()),
                request.getThanksText(),
                request.getPictures() == null ? null : Picture.of(request.getPictures()));
    }

    private static Recipient toRecipient(RecipientCreate request) {
        List<UUID> cooks = request.getCooks() == null ? List.of() : request.getCooks();
        return Recipient.name(toDomain(request.getType()), request.getChefName(),
                cooks.stream().map(CookId::new).toList());
    }

    static RecipientType toDomain(org.larder.sharing.adapter.in.web.model.RecipientType type) {
        return type == null ? null : switch (type) {
            case COOK -> RecipientType.COOK;
            case CHEF -> RecipientType.CHEF;
            case GRANDMA_AVATAR -> RecipientType.GRANDMA_AVATAR;
        };
    }
}
