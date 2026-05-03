package com.cookwithus.domain.cook;

import java.time.Instant;
import java.util.Objects;

/**
 * Cook — Entity and Aggregate Root of the Registration bounded context.
 *
 * <p>A Cook has a stable identity (CookId) that survives changes to the Cook's
 * display name or bio. Two Cook instances with the same CookId are the same Cook,
 * regardless of their other attributes.
 *
 * <p>Invariants enforced by this class:
 * <ol>
 *   <li>A Cook's email address must be non-null and non-empty.</li>
 *   <li>A Cook's display name must be between 2 and 60 characters.</li>
 * </ol>
 */
public class Cook {

    private static final int DISPLAY_NAME_MAX_LENGTH = 60;
    private static final int DISPLAY_NAME_MIN_LENGTH = 2;

    private final CookId cookId;
    private final String emailAddress;
    private String displayName;
    private String bio;
    private final Instant registeredAt;

    /**
     * Register a new Cook.
     *
     * @param cookId       unique identity of this Cook
     * @param emailAddress the Cook's e-mail address (login credential)
     * @param displayName  the name shown on the Cook's public profile
     * @param registeredAt the instant at which the Cook registered
     */
    public Cook(CookId cookId, String emailAddress, String displayName, Instant registeredAt) {
        this.cookId = Objects.requireNonNull(cookId, "cookId must not be null");
        this.emailAddress = validateEmailAddress(emailAddress);
        this.displayName = validateDisplayName(displayName);
        this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt must not be null");
    }

    // --- Identity -----------------------------------------------------------

    public CookId getCookId() {
        return cookId;
    }

    // --- Accessors ----------------------------------------------------------

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    // --- Behaviour ----------------------------------------------------------

    /**
     * Update the Cook's public profile.
     *
     * @param newDisplayName the updated display name
     * @param newBio         the updated bio (may be null)
     */
    public void updateProfile(String newDisplayName, String newBio) {
        this.displayName = validateDisplayName(newDisplayName);
        this.bio = newBio;
    }

    // --- Equality (identity-based) ------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cook cook)) return false;
        return cookId.equals(cook.cookId);
    }

    @Override
    public int hashCode() {
        return cookId.hashCode();
    }

    @Override
    public String toString() {
        return "Cook{cookId=" + cookId + ", displayName='" + displayName + "'}";
    }

    // --- Private validation -------------------------------------------------

    private static String validateEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("Cook email address must not be blank");
        }
        return emailAddress.trim();
    }

    private static String validateDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Cook display name must not be blank");
        }
        String trimmed = displayName.trim();
        if (trimmed.length() < DISPLAY_NAME_MIN_LENGTH || trimmed.length() > DISPLAY_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Cook display name must be between " + DISPLAY_NAME_MIN_LENGTH
                    + " and " + DISPLAY_NAME_MAX_LENGTH + " characters");
        }
        return trimmed;
    }
}
