package org.larder.cookprofile.domain;

/** The changeable part of a cook; {@code null} means "leave as it is". At least one part must be given. */
public record CookChange(EmailAddress email, PersonName name, PersonName givenName, CookStatus status) {

    public CookChange {
        if (email == null && name == null && givenName == null && status == null) {
            throw new InvalidCookException("A change needs at least one of email, name, givenName or status");
        }
    }
}
