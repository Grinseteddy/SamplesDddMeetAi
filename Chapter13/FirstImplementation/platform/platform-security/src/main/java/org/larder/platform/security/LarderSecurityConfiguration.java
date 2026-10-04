package org.larder.platform.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
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
        ContractErrorResponses errors = new ContractErrorResponses();
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health/**").permitAll()
                        // The AppShell and the micro-UIs are static files; they call the APIs with the cook's token
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/app-shell/**", "/ui/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .oauth2ResourceServer(server -> server
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .build();
    }
}
