package org.larder.sharing.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.sharing.TestData.COOK;
import static org.larder.sharing.TestData.HELP;
import static org.larder.sharing.TestData.IMAGE_LINK;
import static org.larder.sharing.TestData.NOW;
import static org.larder.sharing.TestData.OTHER_COOK;
import static org.larder.sharing.TestData.PICTURE;
import static org.larder.sharing.TestData.TEXT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
import org.larder.sharing.application.ConcurrentChangeException;
import org.larder.sharing.application.ConsentMissingException;
import org.larder.sharing.application.NotFoundException;
import org.larder.sharing.application.NotPermittedException;
import org.larder.sharing.application.ThanksFilter;
import org.larder.sharing.application.ThanksService;
import org.larder.sharing.application.UnknownHelpException;
import org.larder.sharing.application.UnknownPictureException;
import org.larder.sharing.application.UpstreamUnavailableException;
import org.larder.sharing.domain.Recipient;
import org.larder.sharing.domain.RecipientType;
import org.larder.sharing.domain.Thanks;
import org.larder.sharing.domain.ThanksId;
import org.larder.sharing.domain.ThanksRevision;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({ThanksController.class, SharingErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class ThanksControllerTest {

    private static final String THANKS = "/sharing/thanks";
    private static final ThanksId ID = new ThanksId(UUID.fromString("e1e82a2e-9555-49fc-9df0-b85edb5b5997"));
    private static final String THANKS_URL = THANKS + "/" + ID.value();
    private static final String GIVE = """
            {"helpId":"%s","thanksText":"%s","pictures":"%s",
             "recipients":[{"type":"Cook","cooks":["%s"]}]}
            """.formatted(HELP.value(), TEXT, IMAGE_LINK, OTHER_COOK.value());

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ThanksService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static MockHttpServletRequestBuilder give(String body) {
        return post(THANKS).header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static MockHttpServletRequestBuilder change(String body) {
        return patch(THANKS_URL).header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static Thanks thanksToOtherCook() {
        return Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK)), TEXT, PICTURE, NOW);
    }

    @Test
    void givesThanksForTheCallingCookAndLinksToThem() throws Exception {
        Thanks thanks = thanksToOtherCook();
        given(service.give(eq(COOK), eq(HELP), any(), eq(TEXT), eq(PICTURE))).willReturn(thanks);

        mvc.perform(give(GIVE).with(cook("sharing:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(THANKS + "/" + thanks.id().value())))
                .andExpect(jsonPath("$.thanksLink").value(endsWith(thanks.id().value().toString())));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Recipient>> recipients = ArgumentCaptor.forClass(List.class);
        then(service).should().give(eq(COOK), eq(HELP), recipients.capture(), eq(TEXT), eq(PICTURE));
        org.assertj.core.api.Assertions.assertThat(recipients.getValue()).singleElement()
                .satisfies(recipient -> {
                    org.assertj.core.api.Assertions.assertThat(recipient.type()).isEqualTo(RecipientType.COOK);
                    org.assertj.core.api.Assertions.assertThat(recipient.cooks()).containsExactly(OTHER_COOK);
                });
    }

    @Test
    void givingNeedsTheWriteScopeAndAToken() throws Exception {
        mvc.perform(give(GIVE).with(cook("sharing:read"))).andExpect(status().isForbidden());
        mvc.perform(give(GIVE).with(cook("sharing:admin"))).andExpect(status().isForbidden());
        mvc.perform(give(GIVE)).andExpect(status().isUnauthorized());
        then(service).should(never()).give(any(), any(), any(), any(), any());
    }

    @Test
    void recipientRulesAreCheckedBeforeTheService() throws Exception {
        mvc.perform(give("""
                        {"helpId":"%s","thanksText":"Thanks","pictures":"%s",
                         "recipients":[{"type":"GrandmaAvatar","chefName":"Grandma"}]}
                        """.formatted(HELP.value(), IMAGE_LINK)).with(cook("sharing:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RECIPIENT"));
        mvc.perform(give("""
                        {"helpId":"%s","thanksText":"Thanks","pictures":"https://larder.org/media/videos/%s"}
                        """.formatted(HELP.value(), UUID.randomUUID())).with(cook("sharing:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PICTURE"));
        then(service).should(never()).give(any(), any(), any(), any(), any());
    }

    @Test
    void malformedRequestsAreBadRequests() throws Exception {
        mvc.perform(give("{\"helpId\":\"%s\",\"thanksText\":\"Thanks\"}".formatted(HELP.value()))
                        .with(cook("sharing:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(give("""
                        {"helpId":"%s","thanksText":"","pictures":"%s"}""".formatted(HELP.value(), IMAGE_LINK))
                        .with(cook("sharing:write")))
                .andExpect(status().isBadRequest());
        mvc.perform(give("""
                        {"helpId":"%s","thanksText":"Thanks","pictures":"%s","recipients":[{"type":"Neighbour"}]}
                        """.formatted(HELP.value(), IMAGE_LINK)).with(cook("sharing:write")))
                .andExpect(status().isBadRequest());
        mvc.perform(get(THANKS).param("giver", "not-a-cook").header("version", "1.0.0").with(cook("sharing:read")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upstreamAndRuleFailuresKeepTheContractsCodes() throws Exception {
        given(service.give(any(), any(), any(), any(), any()))
                .willThrow(new UnknownHelpException(HELP))
                .willThrow(new NotPermittedException("not your help"))
                .willThrow(new UnknownPictureException(PICTURE))
                .willThrow(new UpstreamUnavailableException("down", null));

        mvc.perform(give(GIVE).with(cook("sharing:write")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_HELP"));
        mvc.perform(give(GIVE).with(cook("sharing:write")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
        mvc.perform(give(GIVE).with(cook("sharing:write")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_PICTURE"));
        mvc.perform(give(GIVE).with(cook("sharing:write")))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("UPSTREAM_UNAVAILABLE"));
    }

    @Test
    void mentioningACookWithoutConsentIsABadRequest() throws Exception {
        var noConsent = org.mockito.Mockito.mock(ConsentMissingException.class);
        given(noConsent.code()).willReturn(ConsentMissingException.MENTION_WITHOUT_CONSENT);
        given(noConsent.getMessage()).willReturn("no consent");
        given(service.give(any(), any(), any(), any(), any())).willThrow(noConsent);

        mvc.perform(give(GIVE).with(cook("sharing:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MENTION_WITHOUT_CONSENT"));
    }

    @Test
    void readsThanksInTheContractShape() throws Exception {
        Thanks thanks = Thanks.give(COOK, HELP, List.of(Recipient.cooks(OTHER_COOK), Recipient.chef("Chef Jamie")),
                TEXT, PICTURE, NOW);
        given(service.thanks(ID)).willReturn(thanks);

        mvc.perform(get(THANKS_URL).header("version", "1.0.0").with(cook("sharing:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanksId").value(thanks.id().value().toString()))
                .andExpect(jsonPath("$.giver").value(COOK.value().toString()))
                .andExpect(jsonPath("$.helpId").value(HELP.value().toString()))
                .andExpect(jsonPath("$.thanksText").value(TEXT))
                .andExpect(jsonPath("$.pictures").value(IMAGE_LINK.toString()))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T17:00:00Z"))
                .andExpect(jsonPath("$.recipients[0].type").value("Cook"))
                .andExpect(jsonPath("$.recipients[0].cooks[0]").value(OTHER_COOK.value().toString()))
                .andExpect(jsonPath("$.recipients[0].chefName").doesNotExist())
                .andExpect(jsonPath("$.recipients[1].type").value("Chef"))
                .andExpect(jsonPath("$.recipients[1].chefName").value("Chef Jamie"))
                .andExpect(jsonPath("$.recipients[1].cooks").doesNotExist());
    }

    @Test
    void anAdminReadsButUnknownThanksAreNotFound() throws Exception {
        given(service.thanks(ID)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(THANKS_URL).header("version", "1.0.0").with(cook("sharing:admin")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void listsThanksFilteredByGiverAndMentionedCook() throws Exception {
        Thanks thanks = thanksToOtherCook();
        given(service.thanks(new ThanksFilter(COOK, OTHER_COOK))).willReturn(List.of(thanks));
        given(service.thanks(ThanksFilter.all())).willReturn(List.of());

        mvc.perform(get(THANKS).param("giver", COOK.value().toString()).param("recipient", OTHER_COOK.value().toString())
                        .header("version", "1.0.0").with(cook("sharing:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanks[0].thanksId").value(thanks.id().value().toString()));
        mvc.perform(get(THANKS).header("version", "1.0.0").with(cook("sharing:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanks").isEmpty());
        mvc.perform(get(THANKS).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anAbsentPartOfAChangeLeavesItAsItIs() throws Exception {
        given(service.revise(any(), any(), any())).willReturn(thanksToOtherCook());

        mvc.perform(change("{\"thanksText\":\"Thanks again!\"}").with(cook("sharing:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thanksLink").value(endsWith(THANKS_URL)));

        then(service).should().revise(COOK, ID, new ThanksRevision(null, "Thanks again!", null));
    }

    @Test
    void anEmptyListOfRecipientsAddressesTheThanksToNobody() throws Exception {
        given(service.revise(any(), any(), any())).willReturn(thanksToOtherCook());

        mvc.perform(change("{\"recipients\":[]}").with(cook("sharing:write"))).andExpect(status().isOk());

        then(service).should().revise(COOK, ID, new ThanksRevision(List.of(), null, null));
    }

    @Test
    void changingSomebodyElsesThanksIsForbidden() throws Exception {
        given(service.revise(any(), any(), any())).willThrow(new NotPermittedException("not yours"));

        mvc.perform(change("{\"thanksText\":\"Mine now\"}").with(cook("sharing:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
        mvc.perform(change("{\"thanksText\":\"Mine now\"}").with(cook("sharing:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void aLostRaceIsABadRequestToRepeat() throws Exception {
        given(service.revise(any(), any(), any())).willThrow(new ConcurrentChangeException(ID));

        mvc.perform(change("{\"thanksText\":\"Again\"}").with(cook("sharing:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("THANKS_CHANGED_CONCURRENTLY"));
    }

    @Test
    void theGiverWithdrawsThanks() throws Exception {
        mvc.perform(delete(THANKS_URL).header("version", "1.0.0").with(cook("sharing:write")))
                .andExpect(status().isNoContent());
        then(service).should().withdraw(COOK, ID);
    }

    @Test
    void withdrawingSomebodyElsesThanksIsForbiddenEvenForAnAdmin() throws Exception {
        willThrow(new NotPermittedException("not yours")).given(service).withdraw(COOK, ID);

        mvc.perform(delete(THANKS_URL).header("version", "1.0.0").with(cook("sharing:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
        mvc.perform(delete(THANKS_URL).header("version", "1.0.0").with(cook("sharing:admin")))
                .andExpect(status().isForbidden());
    }
}
