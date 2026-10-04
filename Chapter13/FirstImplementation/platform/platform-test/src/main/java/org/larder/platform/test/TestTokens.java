package org.larder.platform.test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** Access tokens as Keycloak issues them for Larder: a {@code cookId} claim plus OAuth2 scopes. */
public final class TestTokens {

    private TestTokens() {
    }

    /** A token of {@code cookId} carrying the given scopes, e.g. {@code cook(id, "recipe:read")}. */
    public static RequestPostProcessor cook(UUID cookId, String... scopes) {
        return jwt()
                .jwt(token -> token.claim("cookId", cookId.toString()))
                .authorities(Arrays.stream(scopes)
                        .<GrantedAuthority>map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                        .toList());
    }
}
