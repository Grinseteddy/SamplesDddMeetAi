package org.larder.cookprofile.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A registered member of the cooking community (aggregate root).
 *
 * <ul>
 *   <li>A user registers themselves: the cook's id is the id of the registering user.</li>
 *   <li>A newly registered cook is {@link CookStatus#ACTIVE}; {@code memberSince} is the registration date.</li>
 *   <li>{@code cookId} and {@code memberSince} never change.</li>
 *   <li>Only the cook changes or deregisters their own profile.</li>
 * </ul>
 */
public final class Cook {

    private final CookId id;
    private final LocalDate memberSince;
    private EmailAddress email;
    private PersonName name;
    private PersonName givenName;
    private CookStatus status;

    private Cook(CookId id, EmailAddress email, PersonName name, PersonName givenName, LocalDate memberSince, CookStatus status) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.name = Objects.requireNonNull(name);
        this.givenName = Objects.requireNonNull(givenName);
        this.memberSince = Objects.requireNonNull(memberSince);
        this.status = Objects.requireNonNull(status);
    }

    public static Cook register(CookId user, EmailAddress email, PersonName name, PersonName givenName, LocalDate today) {
        return new Cook(user, email, name, givenName, today, CookStatus.ACTIVE);
    }

    /** Recreates a stored cook. */
    public static Cook restore(CookId id, EmailAddress email, PersonName name, PersonName givenName, LocalDate memberSince, CookStatus status) {
        return new Cook(id, email, name, givenName, memberSince, status);
    }

    /** Applies the given parts of {@code change}; everything else stays as it is. */
    public void change(CookChange change) {
        if (change.email() != null) {
            email = change.email();
        }
        if (change.name() != null) {
            name = change.name();
        }
        if (change.givenName() != null) {
            givenName = change.givenName();
        }
        if (change.status() != null) {
            status = change.status();
        }
    }

    public boolean isProfileOf(CookId candidate) {
        return id.equals(candidate);
    }

    public CookId id() {
        return id;
    }

    public EmailAddress email() {
        return email;
    }

    public PersonName name() {
        return name;
    }

    public PersonName givenName() {
        return givenName;
    }

    public LocalDate memberSince() {
        return memberSince;
    }

    public CookStatus status() {
        return status;
    }
}
