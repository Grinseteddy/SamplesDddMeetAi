package org.larder.sharing.adapter.in.web;

import java.time.ZoneOffset;

import org.larder.sharing.adapter.in.web.model.Recipient;
import org.larder.sharing.adapter.in.web.model.RecipientType;
import org.larder.sharing.adapter.in.web.model.Thanks;
import org.larder.sharing.domain.CookId;

/** Translates the domain model into the contract's model - and only in this direction. */
final class ThanksMapper {

    private ThanksMapper() {
    }

    static Thanks toApi(org.larder.sharing.domain.Thanks thanks) {
        return new Thanks(
                thanks.id().value(),
                thanks.giver().value(),
                thanks.help().value(),
                thanks.recipients().stream().map(ThanksMapper::toApi).toList(),
                thanks.text(),
                thanks.picture().link())
                .createdAt(thanks.createdAt().atOffset(ZoneOffset.UTC))
                .updatedAt(thanks.updatedAt().atOffset(ZoneOffset.UTC));
    }

    /** {@code chefName} and {@code cooks} appear only where the recipient's type allows them. */
    static Recipient toApi(org.larder.sharing.domain.Recipient recipient) {
        return new Recipient(recipient.id().value(), toApi(recipient.type()))
                .chefName(recipient.chefName())
                .cooks(recipient.cooks().isEmpty() ? null : recipient.cooks().stream().map(CookId::value).toList());
    }

    static RecipientType toApi(org.larder.sharing.domain.RecipientType type) {
        return switch (type) {
            case COOK -> RecipientType.COOK;
            case CHEF -> RecipientType.CHEF;
            case GRANDMA_AVATAR -> RecipientType.GRANDMA_AVATAR;
        };
    }
}
