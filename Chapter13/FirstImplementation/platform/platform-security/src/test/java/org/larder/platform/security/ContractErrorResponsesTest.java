package org.larder.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

/** 401 and 403 of Spring Security carry the contracts' Error body and keep the bearer-token header. */
class ContractErrorResponsesTest {

    private final ContractErrorResponses errors = new ContractErrorResponses();

    @Test
    void aMissingTokenIsAnUnauthorizedErrorBody() throws Exception {
        var response = new MockHttpServletResponse();

        errors.commence(new MockHttpServletRequest(), response, new InsufficientAuthenticationException("no token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("WWW-Authenticate")).startsWith("Bearer");
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"code\":\"UNAUTHORIZED\"").contains("\"message\":");
    }

    @Test
    void aMissingScopeIsANotPermittedErrorBody() throws Exception {
        var response = new MockHttpServletResponse();

        errors.handle(new MockHttpServletRequest(), response, new AccessDeniedException("scope missing"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"code\":\"NOT_PERMITTED\"");
    }
}
