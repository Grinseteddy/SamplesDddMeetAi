package org.larder.cookingassistance.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.cookingassistance.TestData.BUTTERMILK;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.HELP_REQUEST_ID;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.SCONES;
import static org.larder.cookingassistance.TestData.burningCatastrophe;
import static org.larder.cookingassistance.TestData.noButtermilk;
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
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.HelpRequestService;
import org.larder.cookingassistance.application.NotFoundException;
import org.larder.cookingassistance.application.NotPermittedException;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequest;
import org.larder.cookingassistance.domain.HelpRequestDraft;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRequestRevision;
import org.larder.cookingassistance.domain.HelpRequestStatus;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
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

@WebMvcTest({HelpRequestsController.class, CookingAssistanceErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class HelpRequestsControllerTest {

    private static final String HELP_REQUESTS = "/cooking-assistance/help-requests";
    private static final HelpRequestId ID = new HelpRequestId(HELP_REQUEST_ID);
    private static final String BURNING_CATASTROPHE = """
            {"title":"Burning Catastrophe","type":"STEPS_TO_MITIGATE_CATASTROPHE",
             "description":"Scones are burned and mother in law is coming in 30 minutes",
             "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913","preferredProvider":["GRANDMA_AVATAR","COMMUNITY"]}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HelpRequestService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static MockHttpServletRequestBuilder createBurningCatastrophe() {
        return post(HELP_REQUESTS).header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON)
                .content(BURNING_CATASTROPHE);
    }

    private static HelpRequest stored(HelpRequestDraft draft) {
        return HelpRequest.restore(ID, COOK, draft.title(), draft.type(), draft.description(), draft.recipe(),
                draft.howToStep(), draft.ingredients(), draft.preferredProviders(), HelpRequestStatus.OPEN, NOW, NOW);
    }

    @Test
    void raisesAHelpRequestForTheCallingCookAndLinksToIt() throws Exception {
        given(service.raise(eq(COOK), any())).willReturn(stored(burningCatastrophe()));

        mvc.perform(createBurningCatastrophe().with(cook("help:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(HELP_REQUESTS + "/" + HELP_REQUEST_ID)))
                .andExpect(jsonPath("$.helpRequestLink").value(endsWith(HELP_REQUEST_ID.toString())));

        ArgumentCaptor<HelpRequestDraft> draft = ArgumentCaptor.forClass(HelpRequestDraft.class);
        then(service).should().raise(eq(COOK), draft.capture());
        org.assertj.core.api.Assertions.assertThat(draft.getValue()).isEqualTo(burningCatastrophe());
    }

    @Test
    void raisingNeedsTheWriteScopeAndAToken() throws Exception {
        mvc.perform(createBurningCatastrophe().with(cook("help:read"))).andExpect(status().isForbidden());
        mvc.perform(createBurningCatastrophe()).andExpect(status().isUnauthorized());
        then(service).should(never()).raise(any(), any());
    }

    @Test
    void aBrokenInvariantIsABadRequestWithItsCode() throws Exception {
        given(service.raise(eq(COOK), any())).willThrow(new HelpRuleViolationException(
                HelpRuleViolationException.CHEF_ONLY_FOR_MENU_PROPOSAL, "chef only for menus"));

        mvc.perform(createBurningCatastrophe().with(cook("help:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHEF_ONLY_FOR_MENU_PROPOSAL"));
    }

    @Test
    void malformedRequestsAreBadRequests() throws Exception {
        mvc.perform(post(HELP_REQUESTS).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Only a title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(post(HELP_REQUESTS).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BURNING_CATASTROPHE.replace("[\"GRANDMA_AVATAR\",\"COMMUNITY\"]",
                                "[\"GRANDMA_AVATAR\",\"COMMUNITY\",\"CHEF\"]")))
                .andExpect(status().isBadRequest());
        mvc.perform(post(HELP_REQUESTS).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mvc.perform(get(HELP_REQUESTS).param("status", "CLOSED").header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isBadRequest());
        then(service).should(never()).raise(any(), any());
    }

    @Test
    void showsAHelpRequestInTheContractShape() throws Exception {
        given(service.helpRequest(ID)).willReturn(stored(noButtermilk()));

        mvc.perform(get(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpRequestId").value(HELP_REQUEST_ID.toString()))
                .andExpect(jsonPath("$.requester").value(COOK.value().toString()))
                .andExpect(jsonPath("$.type").value("INGREDIENT_SUBSTITUTE"))
                .andExpect(jsonPath("$.recipe").value(SCONES.value().toString()))
                .andExpect(jsonPath("$.ingredients[0]").value(BUTTERMILK.value().toString()))
                .andExpect(jsonPath("$.preferredProvider[0]").value("COMMUNITY"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").value("2026-10-03T16:30:00Z"));
    }

    @Test
    void anUnknownHelpRequestIsNotFound() throws Exception {
        given(service.helpRequest(ID)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:write")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void listsHelpRequestsFilteredByRequesterAndStatus() throws Exception {
        given(service.helpRequests(COOK, HelpRequestStatus.OPEN)).willReturn(List.of(stored(burningCatastrophe())));

        mvc.perform(get(HELP_REQUESTS).param("requester", COOK.value().toString()).param("status", "OPEN")
                        .header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].helpRequestId").value(HELP_REQUEST_ID.toString()))
                .andExpect(jsonPath("$[0].type").value("STEPS_TO_MITIGATE_CATASTROPHE"));
    }

    @Test
    void changesAHelpRequestPartially() throws Exception {
        given(service.revise(eq(COOK), eq(ID), any())).willReturn(stored(noButtermilk()));

        mvc.perform(patch(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"No buttermilk at all\",\"status\":\"ANSWERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpRequestLink").value(endsWith(HELP_REQUESTS + "/" + HELP_REQUEST_ID)));

        then(service).should().revise(COOK, ID, HelpRequestRevision.none().withTitle("No buttermilk at all")
                .withStatus(HelpRequestStatus.ANSWERED));
    }

    @Test
    void changingSomebodyElsesRequestIsForbidden() throws Exception {
        given(service.revise(eq(COOK), eq(ID), any())).willThrow(new NotPermittedException("not yours"));

        mvc.perform(patch(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Mine now\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void withdrawsAnOpenRequestButNotAnAnsweredOne() throws Exception {
        mvc.perform(delete(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:write")))
                .andExpect(status().isNoContent());
        then(service).should().withdraw(COOK, ID);

        willThrow(new HelpRuleViolationException(HelpRuleViolationException.HELP_REQUEST_NOT_OPEN, "answered"))
                .given(service).withdraw(COOK, ID);
        mvc.perform(delete(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("HELP_REQUEST_NOT_OPEN"));
        mvc.perform(delete(HELP_REQUESTS + "/" + HELP_REQUEST_ID).header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theDraftCarriesTypeSpecificReferences() throws Exception {
        given(service.raise(eq(COOK), any())).willReturn(stored(noButtermilk()));

        mvc.perform(post(HELP_REQUESTS).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"No buttermilk","type":"INGREDIENT_SUBSTITUTE","description":"Instead?",
                                 "recipe":"%s","ingredients":["%s"],"preferredProvider":["COMMUNITY"]}
                                """.formatted(SCONES.value(), BUTTERMILK.value())))
                .andExpect(status().isCreated());

        then(service).should().raise(COOK, new HelpRequestDraft("No buttermilk", HelpType.INGREDIENT_SUBSTITUTE,
                "Instead?", SCONES, null, Set.of(BUTTERMILK), Set.of(HelpProviderType.COMMUNITY)));
    }
}
