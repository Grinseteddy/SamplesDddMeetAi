package org.larder.cookingassistance.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.cookingassistance.TestData.COOK;
import static org.larder.cookingassistance.TestData.GRANDMA_HELP_ID;
import static org.larder.cookingassistance.TestData.HELP_REQUEST_ID;
import static org.larder.cookingassistance.TestData.NOW;
import static org.larder.cookingassistance.TestData.OTHER_COOK;
import static org.larder.cookingassistance.TestData.SCONES;
import static org.larder.cookingassistance.TestData.stayCalm;
import static org.larder.cookingassistance.TestData.threeCourses;
import static org.larder.cookingassistance.TestData.yoghurtForButtermilk;
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
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.larder.cookingassistance.application.HelpService;
import org.larder.cookingassistance.application.NotFoundException;
import org.larder.cookingassistance.application.NotPermittedException;
import org.larder.cookingassistance.application.UnknownHelpRequestException;
import org.larder.cookingassistance.domain.Answer;
import org.larder.cookingassistance.domain.CatastropheMitigation;
import org.larder.cookingassistance.domain.Help;
import org.larder.cookingassistance.domain.HelpId;
import org.larder.cookingassistance.domain.HelpProviderType;
import org.larder.cookingassistance.domain.HelpRequestId;
import org.larder.cookingassistance.domain.HelpRuleViolationException;
import org.larder.cookingassistance.domain.HelpType;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest({HelpsController.class, CookingAssistanceErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class HelpsControllerTest {

    private static final String HELPS = "/cooking-assistance/helps";
    private static final HelpId ID = new HelpId(GRANDMA_HELP_ID);
    private static final HelpRequestId REQUEST = new HelpRequestId(HELP_REQUEST_ID);
    private static final String STAY_CALM = """
            {"helpRequest":"%s","answerTitle":"Stay calm","helpProviderType":"COMMUNITY",
             "answer":{"answerType":"STEPS_TO_MITIGATE_CATASTROPHE","recipe":"%s","explanation":"use a new, cold pan"}}
            """.formatted(HELP_REQUEST_ID, SCONES.value());

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private HelpService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static MockHttpServletRequestBuilder giveStayCalm() {
        return post(HELPS).header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content(STAY_CALM);
    }

    private static Help help(HelpProviderType providerType, Answer answer) {
        return Help.restore(ID, REQUEST, COOK, providerType, providerType == HelpProviderType.GRANDMA_AVATAR ? null : OTHER_COOK,
                "Stay calm", answer, NOW, NOW);
    }

    @Test
    void givesAHelpAsTheCallingCookAndLinksToIt() throws Exception {
        given(service.give(eq(COOK), eq(REQUEST), eq(HelpProviderType.COMMUNITY), eq("Stay calm"), eq(stayCalm())))
                .willReturn(help(HelpProviderType.COMMUNITY, stayCalm()));

        mvc.perform(giveStayCalm().with(cook("help:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(HELPS + "/" + GRANDMA_HELP_ID)))
                .andExpect(jsonPath("$.helpLink").value(endsWith(GRANDMA_HELP_ID.toString())));
    }

    @Test
    void givingNeedsTheWriteScope() throws Exception {
        mvc.perform(giveStayCalm().with(cook("help:read"))).andExpect(status().isForbidden());
        mvc.perform(giveStayCalm()).andExpect(status().isUnauthorized());
        then(service).should(never()).give(any(), any(), any(), any(), any());
    }

    @Test
    void anUnknownRequestOrABrokenInvariantIsABadRequest() throws Exception {
        given(service.give(any(), any(), any(), any(), any())).willThrow(new UnknownHelpRequestException(REQUEST));
        mvc.perform(giveStayCalm().with(cook("help:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_HELP_REQUEST"));

        willThrow(new HelpRuleViolationException(HelpRuleViolationException.ANSWER_TYPE_MISMATCH, "wrong type"))
                .given(service).give(any(), any(), any(), any(), any());
        mvc.perform(giveStayCalm().with(cook("help:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ANSWER_TYPE_MISMATCH"));
    }

    @Test
    void anAnswerWithoutItsBodyIsMalformed() throws Exception {
        mvc.perform(post(HELPS).header("version", "1.0.0").with(cook("help:write")).contentType(MediaType.APPLICATION_JSON)
                        .content(STAY_CALM.replace(",\"explanation\":\"use a new, cold pan\"", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        then(service).should(never()).give(any(), any(), any(), any(), any());
    }

    @Test
    void showsAHelpWithItsAnswerInTheContractShape() throws Exception {
        given(service.help(COOK, ID)).willReturn(help(HelpProviderType.GRANDMA_AVATAR, stayCalm()));

        mvc.perform(get(HELPS + "/" + GRANDMA_HELP_ID).header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpId").value(GRANDMA_HELP_ID.toString()))
                .andExpect(jsonPath("$.helpRequest").value(HELP_REQUEST_ID.toString()))
                .andExpect(jsonPath("$.helpRequester").value(COOK.value().toString()))
                .andExpect(jsonPath("$.helpProviderType").value("GRANDMA_AVATAR"))
                .andExpect(jsonPath("$.helpProvider").doesNotExist())
                .andExpect(jsonPath("$.answer.answerType").value("STEPS_TO_MITIGATE_CATASTROPHE"))
                .andExpect(jsonPath("$.answer.recipe").value(SCONES.value().toString()))
                .andExpect(jsonPath("$.answer.explanation").value("use a new, cold pan"));
    }

    @Test
    void showsSubstitutesAndMenusInTheContractShape() throws Exception {
        given(service.helps(COOK, REQUEST, HelpType.INGREDIENT_SUBSTITUTE, OTHER_COOK)).willReturn(List.of(
                help(HelpProviderType.COMMUNITY, yoghurtForButtermilk()), help(HelpProviderType.CHEF, threeCourses())));

        mvc.perform(get(HELPS).param("helpRequestId", HELP_REQUEST_ID.toString()).param("type", "INGREDIENT_SUBSTITUTE")
                        .param("helpProvider", OTHER_COOK.value().toString()).header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].answer.substitute[0].substituteIngredient.name").value("Yoghurt"))
                .andExpect(jsonPath("$[0].answer.substitute[0].substituteIngredient.unit").value("MILLILITER"))
                .andExpect(jsonPath("$[0].helpProvider").value(OTHER_COOK.value().toString()))
                .andExpect(jsonPath("$[1].answer.answerType").value("MENU_PROPOSAL"))
                .andExpect(jsonPath("$[1].answer.course[1].meal.recipe").value(SCONES.value().toString()))
                .andExpect(jsonPath("$[1].answer.meal").value("DINNER"));
    }

    @Test
    void aChefsHelpForSomebodyElseIsForbiddenAndAnUnknownHelpNotFound() throws Exception {
        given(service.help(COOK, ID)).willThrow(new NotPermittedException("chef help"));
        mvc.perform(get(HELPS + "/" + GRANDMA_HELP_ID).header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));

        willThrow(new NotFoundException("missing")).given(service).help(COOK, ID);
        mvc.perform(get(HELPS + "/" + GRANDMA_HELP_ID).header("version", "1.0.0").with(cook("help:read")))
                .andExpect(status().isNotFound());
    }

    @Test
    void theProviderChangesAHelp() throws Exception {
        var breathe = new CatastropheMitigation(Optional.empty(), "Breathe");
        given(service.revise(COOK, ID, null, breathe)).willReturn(help(HelpProviderType.COMMUNITY, breathe));

        mvc.perform(patch(HELPS + "/" + GRANDMA_HELP_ID).header("version", "1.0.0").with(cook("help:write"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":{\"answerType\":\"STEPS_TO_MITIGATE_CATASTROPHE\",\"explanation\":\"Breathe\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.helpLink").value(endsWith(HELPS + "/" + GRANDMA_HELP_ID)));
        then(service).should().revise(COOK, ID, null, breathe);
    }

    @Test
    void onlyTheProviderWithdrawsAHelp() throws Exception {
        mvc.perform(delete(HELPS + "/" + GRANDMA_HELP_ID).header("version", "1.0.0").with(cook("help:write")))
                .andExpect(status().isNoContent());
        then(service).should().withdraw(COOK, ID);

        willThrow(new NotPermittedException("not yours")).given(service).withdraw(COOK, ID);
        mvc.perform(delete(HELPS + "/" + GRANDMA_HELP_ID).header("version", "1.0.0").with(cook("help:write")))
                .andExpect(status().isForbidden());
    }

    @Test
    void readingNeedsAToken() throws Exception {
        mvc.perform(get(HELPS).header("version", "1.0.0")).andExpect(status().isUnauthorized());
        given(service.helps(COOK, null, null, null)).willReturn(List.of(help(HelpProviderType.COMMUNITY, stayCalm())));
        mvc.perform(get(HELPS).header("version", "1.0.0").with(cook("help:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].answerTitle").value("Stay calm"));
    }
}
