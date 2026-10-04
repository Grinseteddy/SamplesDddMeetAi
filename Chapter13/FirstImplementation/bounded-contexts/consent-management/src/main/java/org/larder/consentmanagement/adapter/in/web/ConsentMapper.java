package org.larder.consentmanagement.adapter.in.web;

import java.time.ZoneOffset;

import org.larder.consentmanagement.adapter.in.web.model.Consent;
import org.larder.consentmanagement.adapter.in.web.model.ConsentText;

/** Translates the domain model into the contract's model - and only in this direction. */
final class ConsentMapper {

    private ConsentMapper() {
    }

    static Consent toApi(org.larder.consentmanagement.domain.Consent consent) {
        return new Consent(
                consent.id().value(),
                consent.subject().value(),
                toApi(consent.consentText()),
                consent.givenAt().atOffset(ZoneOffset.UTC))
                .revokedAt(consent.revokedAt().map(at -> at.atOffset(ZoneOffset.UTC)).orElse(null));
    }

    static ConsentText toApi(org.larder.consentmanagement.domain.ConsentText text) {
        return new ConsentText(text.id().value(), text.text());
    }
}
