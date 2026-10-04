package org.larder.consentmanagement.application;

import org.larder.consentmanagement.domain.ConsentTextId;

/** A consent refers to a consent text that does not exist (a broken request, not a missing resource). */
public class UnknownConsentTextException extends RuntimeException {

    public UnknownConsentTextException(ConsentTextId id) {
        super("Consent text " + id.value() + " does not exist");
    }
}
