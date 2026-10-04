package org.larder.sharing.adapter.out.cookingassistance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.HELP;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.larder.sharing.domain.Help;
import org.larder.sharing.domain.HelperKind;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Talks to a stand-in for Cooking Assistance that answers as its contract says. */
class CookingAssistanceHelpsTest {

    private static final String BASE_URL = "http://larder.test/cooking-assistance";
    private static final String THE_HELP = BASE_URL + "/helps/" + HELP.value();

    /** The contract's example StayCalmHelp. */
    private static final String STAY_CALM = """
            {"helpId":"%s","helpRequest":"23a8eeed-35f6-460b-892e-7bb458a8fded","helpRequester":"%s",
             "answerTitle":"Stay calm","helpProviderType":"GRANDMA_AVATAR",
             "answer":{"answerType":"STEPS_TO_MITIGATE_CATASTROPHE","recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
                       "explanation":"use a new, cold pan"},
             "createdAt":"2026-10-03T16:31:00Z","updatedAt":"2026-10-03T16:31:00Z"}
            """.formatted(HELP.value(), COOK.value());

    private MockRestServiceServer cookingAssistance;
    private CookingAssistanceHelps helps;

    @BeforeEach
    void stubCookingAssistance() {
        RestClient.Builder builder = RestClient.builder();
        cookingAssistance = MockRestServiceServer.bindTo(builder).build();
        helps = CookingAssistanceHelps.create(builder, BASE_URL);
    }

    @AfterEach
    void forgetCaller() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsWhoReceivedAndWhoGaveTheHelpWithTheCallersToken() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("cooks-token")
                .header("alg", "none").claim("cookId", COOK.value().toString()).build()));
        cookingAssistance.expect(requestTo(THE_HELP))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("version", "1.0.0"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer cooks-token"))
                .andRespond(withSuccess(STAY_CALM, MediaType.APPLICATION_JSON));

        Help help = helps.find(HELP).orElseThrow();

        assertThat(help).isEqualTo(new Help(HELP, COOK, HelperKind.GRANDMA_AVATAR, Optional.empty()));
        cookingAssistance.verify();
    }

    @Test
    void aCommunityHelpNamesTheHelpingCook() {
        cookingAssistance.expect(requestTo(THE_HELP))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("""
                        {"helpId":"%s","helpRequest":"23a8eeed-35f6-460b-892e-7bb458a8fded","helpRequester":"%s",
                         "answerTitle":"Cold pan","helpProviderType":"COMMUNITY","helpProvider":"%s",
                         "answer":{"answerType":"STEPS_TO_MITIGATE_CATASTROPHE",
                                   "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913","explanation":"cold pan"}}
                        """.formatted(HELP.value(), COOK.value(), OTHER_COOK.value()), MediaType.APPLICATION_JSON));

        assertThat(helps.find(HELP)).contains(new Help(HELP, COOK, HelperKind.COMMUNITY_COOK, Optional.of(OTHER_COOK)));
    }

    @Test
    void anUnknownHelpIsEmpty() {
        cookingAssistance.expect(requestTo(THE_HELP)).andRespond(withResourceNotFound());

        assertThat(helps.find(HELP)).isEmpty();
    }

    @Test
    void aHelpCookingAssistanceDoesNotShowIsNotPermitted() {
        cookingAssistance.expect(requestTo(THE_HELP)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> helps.find(HELP)).isInstanceOf(NotPermittedException.class);
    }

    @Test
    void aFailingCookingAssistanceIsUnavailable() {
        cookingAssistance.expect(requestTo(THE_HELP)).andRespond(withServerError());

        assertThatThrownBy(() -> helps.find(HELP)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void anUnreachableCookingAssistanceIsUnavailable() {
        cookingAssistance.expect(requestTo(THE_HELP)).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertThatThrownBy(() -> helps.find(HELP)).isInstanceOf(UpstreamUnavailableException.class);
    }

    @Test
    void anUnusableHelpIsUnavailable() {
        cookingAssistance.expect(requestTo(THE_HELP)).andRespond(withSuccess("""
                {"helpId":"%s","answerTitle":"Who asked?","helpProviderType":"GRANDMA_AVATAR"}
                """.formatted(HELP.value()), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> helps.find(HELP)).isInstanceOf(UpstreamUnavailableException.class);
    }
}
