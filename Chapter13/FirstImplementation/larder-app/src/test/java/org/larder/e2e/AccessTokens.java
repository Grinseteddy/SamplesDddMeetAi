package org.larder.e2e;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Access tokens as the dev Keycloak realm ({@code infra/keycloak/larder-realm.json}) issues them -
 * a {@code cookId} claim and all API scopes - but signed by a key pair of the test, so no Keycloak
 * is needed. {@link Security} replaces Spring Boot's issuer-based decoder by one for this key.
 */
final class AccessTokens {

    /** All scopes of the realm's client {@code larder-dev}. */
    static final List<String> ALL_SCOPES = List.of("recipe:read", "recipe:write", "recipe:admin", "meal-plan:read",
            "meal-plan:write", "meal-preparation:read", "meal-preparation:write", "help:read", "help:write",
            "cook:read", "cook:write", "consent:read", "consent:write", "media:read", "media:write",
            "notifications:read", "notifications:write", "sharing:read", "sharing:write", "sharing:admin");

    private static final KeyPair KEYS = rsaKeyPair();

    private AccessTokens() {
    }

    /** A token of {@code cookId} with all scopes, valid for an hour. */
    static String forCook(UUID cookId) {
        return forCook(cookId, ALL_SCOPES);
    }

    /** A token of {@code cookId} with exactly {@code scopes}. */
    static String forCook(UUID cookId, List<String> scopes) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("http://localhost:8180/realms/larder")
                .subject(cookId.toString())
                .audience("account")
                .claim("azp", "larder-dev")
                .claim("cookId", cookId.toString())
                .claim("scope", String.join(" ", scopes))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)))
                .jwtID(UUID.randomUUID().toString())
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("e2e").build(), claims);
            jwt.sign(new RSASSASigner((RSAPrivateKey) KEYS.getPrivate()));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign the access token", e);
        }
    }

    /** The application trusts the test's key instead of Keycloak; Spring Boot's own decoder backs off. */
    @TestConfiguration(proxyBeanMethods = false)
    static class Security {

        @Bean
        JwtDecoder jwtDecoder() {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) KEYS.getPublic()).build();
        }
    }

    private static KeyPair rsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
