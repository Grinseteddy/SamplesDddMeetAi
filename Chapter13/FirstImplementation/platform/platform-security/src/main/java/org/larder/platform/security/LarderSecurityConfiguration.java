package org.larder.platform.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Every API of every Bounded Context is an OAuth2 resource server. Scopes from the
 * OpenAPI contracts (e.g. {@code recipe:read}) arrive as {@code SCOPE_recipe:read}
 * authorities and are checked per operation with {@code @PreAuthorize}.
 */
@AutoConfiguration
@EnableMethodSecurity(proxyTargetClass = true)
public class LarderSecurityConfiguration {

    @Bean
    SecurityFilterChain larderSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(server -> server.jwt(Customizer.withDefaults()))
                .build();
    }
}
