package org.larder.platform.security;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Resolves the {@link CookId} of the authenticated caller. */
public final class CurrentCook {

    public static final String COOK_ID_CLAIM = "cookId";

    private CurrentCook() {
    }

    public static Optional<CookId> id() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaimAsString(COOK_ID_CLAIM)).map(CookId::of);
        }
        return Optional.empty();
    }

    public static CookId require() {
        return id().orElseThrow(() -> new IllegalStateException("No cookId claim in the access token"));
    }
}
