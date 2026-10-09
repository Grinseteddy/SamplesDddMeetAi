package org.larder.platform.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 401 and 403 decided by Spring Security (no or invalid token, missing scope) carry the contracts'
 * {@code Error} body like every other error - Spring's own handlers answer with an empty body. The
 * standard bearer-token behaviour, including the {@code WWW-Authenticate} header, is kept.
 */
final class ContractErrorResponses implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final BearerTokenAuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final BearerTokenAccessDeniedHandler bearerAccessDenied = new BearerTokenAccessDeniedHandler();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        bearerEntryPoint.commence(request, response, exception);
        writeError(response, "UNAUTHORIZED", "A valid access token is required.");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        bearerAccessDenied.handle(request, response, exception);
        writeError(response, "NOT_PERMITTED", "The access token does not grant this operation.");
    }

    private static void writeError(HttpServletResponse response, String code, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
