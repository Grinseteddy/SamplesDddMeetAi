package org.larder.sharing.adapter.out.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.IMAGE;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Talks to a stand-in for Media that answers as its contract says. */
class MediaPicturesTest {

    private static final String BASE_URL = "http://larder.test/media";
    private static final String THE_IMAGE = BASE_URL + "/images/" + IMAGE.value();

    private MockRestServiceServer media;
    private MediaPictures pictures;

    @BeforeEach
    void stubMedia() {
        RestClient.Builder builder = RestClient.builder();
        media = MockRestServiceServer.bindTo(builder).build();
        pictures = MediaPictures.create(builder, BASE_URL);
    }

    @AfterEach
    void forgetCaller() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void anImageOfMediaExistsAskedWithTheCallersToken() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("cooks-token")
                .header("alg", "none").claim("cookId", COOK.value().toString()).build()));
        media.expect(requestTo(THE_IMAGE))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("version", "1.0.0"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer cooks-token"))
                .andRespond(withSuccess("""
                        {"mediaId":"%s","media":"iVBORw0KGgo=",
                         "links":[{"type":"recipe","url":"https://larder.org/recipe-catalog/recipes/7cf09822-77a1-46bb-812f-b7852bca0913"}]}
                        """.formatted(IMAGE.value()), MediaType.APPLICATION_JSON));

        assertThat(pictures.exists(IMAGE)).isTrue();
        media.verify();
    }

    @Test
    void anUnknownImageDoesNotExist() {
        media.expect(requestTo(THE_IMAGE)).andRespond(withResourceNotFound());

        assertThat(pictures.exists(IMAGE)).isFalse();
    }

    @Test
    void aRefusalOfMediaIsNotPermitted() {
        media.expect(requestTo(THE_IMAGE)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> pictures.exists(IMAGE)).isInstanceOf(NotPermittedException.class);
    }

    @Test
    void aFailingMediaIsUnavailable() {
        media.expect(requestTo(THE_IMAGE)).andRespond(withServerError());

        assertThatThrownBy(() -> pictures.exists(IMAGE)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void anUnreachableMediaIsUnavailable() {
        media.expect(requestTo(THE_IMAGE)).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertThatThrownBy(() -> pictures.exists(IMAGE)).isInstanceOf(UpstreamUnavailableException.class);
    }
}
