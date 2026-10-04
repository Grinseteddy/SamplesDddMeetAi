package org.larder.cookprofile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.larder.cookprofile.domain.Cook;
import org.larder.cookprofile.domain.CookId;
import org.larder.cookprofile.domain.EmailAddress;
import org.larder.cookprofile.domain.PersonName;

/** The examples of the contract and the visual glossary. */
public final class TestData {

    public static final CookId COOK = new CookId(UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074"));
    public static final CookId OTHER_COOK = new CookId(UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74"));
    public static final EmailAddress JOE_EMAIL = new EmailAddress("joe.doe@larder.org");
    public static final EmailAddress OTHER_EMAIL = new EmailAddress("jane.roe@larder.org");
    public static final PersonName JOE = new PersonName("Joe");
    public static final PersonName DOE = new PersonName("Doe");
    public static final PersonName JANE = new PersonName("Jane");
    public static final PersonName ROE = new PersonName("Roe");
    public static final Instant NOW = Instant.parse("2026-09-21T10:34:00Z");
    public static final LocalDate TODAY = LocalDate.parse("2026-09-21");

    private TestData() {
    }

    public static Cook joe() {
        return Cook.register(COOK, JOE_EMAIL, JOE, DOE, TODAY);
    }

    public static Cook jane() {
        return Cook.register(OTHER_COOK, OTHER_EMAIL, JANE, ROE, TODAY);
    }
}
