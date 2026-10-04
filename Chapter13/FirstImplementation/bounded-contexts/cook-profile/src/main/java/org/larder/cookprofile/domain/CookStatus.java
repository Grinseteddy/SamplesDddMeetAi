package org.larder.cookprofile.domain;

/** Standing of a cook in the community. A newly registered cook is {@link #ACTIVE}. */
public enum CookStatus {
    ACTIVE,
    INACTIVE,
    PREMIUM
}
