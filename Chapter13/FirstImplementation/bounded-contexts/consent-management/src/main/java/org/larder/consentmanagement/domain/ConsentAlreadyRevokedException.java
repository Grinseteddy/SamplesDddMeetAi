package org.larder.consentmanagement.domain;

public class ConsentAlreadyRevokedException extends RuntimeException {

    public ConsentAlreadyRevokedException(ConsentId id) {
        super("Consent " + id.value() + " is already revoked; give a new consent instead");
    }
}
