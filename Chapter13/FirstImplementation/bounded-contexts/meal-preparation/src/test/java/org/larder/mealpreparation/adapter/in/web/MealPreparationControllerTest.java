package org.larder.mealpreparation.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.mealpreparation.TestData.BAKE;
import static org.larder.mealpreparation.TestData.COOK;
import static org.larder.mealpreparation.TestData.KNEAD;
import static org.larder.mealpreparation.TestData.MIX;
import static org.larder.mealpreparation.TestData.SCONES;
import static org.larder.mealpreparation.TestData.sconeSteps;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.mealpreparation.application.MealPreparationService;
import org.larder.mealpreparation.application.NotFoundException;
import org.larder.mealpreparation.application.NotPermittedException;
import org.larder.mealpreparation.application.RecipeCatalogUnavailableException;
import org.larder.mealpreparation.application.UnknownRecipeException;
import org.larder.mealpreparation.domain.HowToStepId;
import org.larder.mealpreparation.domain.MealPreparation;
import org.larder.mealpreparation.domain.MealPreparationRuleViolationException;
import org.larder.mealpreparation.domain.PreparationId;
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

@WebMvcTest({MealPreparationsController.class, HowToStepsController.class, MealPreparationErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class MealPreparationControllerTest {

    private static final String PREPARATIONS = "/meal-preparation/preparations";
    private static final PreparationId ID = new PreparationId(UUID.fromString("0e1d2c3b-4a59-4687-b7a6-958473625140"));
    private static final String PREPARATION_URL = PREPARATIONS + "/" + ID.value();
    private static final String START_SCONES = "{\"recipe\":\"%s\"}".formatted(SCONES.value());

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealPreparationService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static MockHttpServletRequestBuilder startScones() {
        return post(PREPARATIONS).header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content(START_SCONES);
    }

    private static MockHttpServletRequestBuilder move(String direction, HowToStepId from) {
        return patch(PREPARATION_URL + "/how-to-steps/" + direction).param("stepId", from.value().toString())
                .header("version", "1.0.0");
    }

    @Test
    void startsAPreparationForTheCallingCookAndLinksToIt() throws Exception {
        MealPreparation preparation = MealPreparation.start(COOK, SCONES, sconeSteps());
        given(service.start(COOK, SCONES)).willReturn(preparation);

        mvc.perform(startScones().with(cook("meal-preparation:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(PREPARATIONS + "/" + preparation.id().value())))
                .andExpect(jsonPath("$.preparationLink").value(endsWith(preparation.id().value().toString())));
    }

    @Test
    void startingNeedsTheWriteScopeAndAToken() throws Exception {
        mvc.perform(startScones().with(cook("meal-preparation:read"))).andExpect(status().isForbidden());
        mvc.perform(startScones()).andExpect(status().isUnauthorized());
        then(service).should(never()).start(any(), any());
    }

    @Test
    void anUnknownRecipeIsABadRequest() throws Exception {
        given(service.start(COOK, SCONES)).willThrow(new UnknownRecipeException(SCONES));

        mvc.perform(startScones().with(cook("meal-preparation:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_RECIPE"));
    }

    @Test
    void aRecipeWithoutStepsIsABadRequest() throws Exception {
        given(service.start(COOK, SCONES)).willThrow(new MealPreparationRuleViolationException(
                MealPreparationRuleViolationException.RECIPE_WITHOUT_STEPS, "no steps"));

        mvc.perform(startScones().with(cook("meal-preparation:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RECIPE_WITHOUT_STEPS"));
    }

    @Test
    void anUnavailableRecipeCatalogIsAServerError() throws Exception {
        given(service.start(COOK, SCONES)).willThrow(new RecipeCatalogUnavailableException("down", null));

        mvc.perform(startScones().with(cook("meal-preparation:write")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UPSTREAM_UNAVAILABLE"));
    }

    @Test
    void malformedRequestsAreBadRequests() throws Exception {
        mvc.perform(post(PREPARATIONS).header("version", "1.0.0").with(cook("meal-preparation:write"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(patch(PREPARATION_URL + "/how-to-steps/next").header("version", "1.0.0")
                        .with(cook("meal-preparation:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void showsThePreparationWithItsCurrentStep() throws Exception {
        MealPreparation preparation = MealPreparation.restore(ID, COOK, SCONES, sconeSteps(), KNEAD.id());
        given(service.preparation(COOK, ID)).willReturn(preparation);

        mvc.perform(get(PREPARATION_URL).header("version", "1.0.0").with(cook("meal-preparation:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preparationId").value(ID.value().toString()))
                .andExpect(jsonPath("$.recipe").value(SCONES.value().toString()))
                .andExpect(jsonPath("$.currentStep.howToStepId").value(KNEAD.id().value().toString()))
                .andExpect(jsonPath("$.currentStep.sequenceNumber").value(2));
    }

    @Test
    void somebodyElsesPreparationIsForbidden() throws Exception {
        given(service.preparation(COOK, ID)).willThrow(new NotPermittedException("not yours"));

        mvc.perform(get(PREPARATION_URL).header("version", "1.0.0").with(cook("meal-preparation:read")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void anUnknownPreparationIsNotFound() throws Exception {
        given(service.preparation(COOK, ID)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(PREPARATION_URL).header("version", "1.0.0").with(cook("meal-preparation:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void showsAStepOfThePreparation() throws Exception {
        given(service.howToStep(COOK, ID, BAKE.id())).willReturn(BAKE);

        mvc.perform(get(PREPARATION_URL + "/how-to-steps/" + BAKE.id().value()).header("version", "1.0.0")
                        .with(cook("meal-preparation:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.howToStepId").value(BAKE.id().value().toString()))
                .andExpect(jsonPath("$.sequenceNumber").value(3));
    }

    @Test
    void anUnknownStepIsNotFound() throws Exception {
        HowToStepId unknown = new HowToStepId(UUID.randomUUID());
        given(service.howToStep(COOK, ID, unknown)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(PREPARATION_URL + "/how-to-steps/" + unknown.value()).header("version", "1.0.0")
                        .with(cook("meal-preparation:read")))
                .andExpect(status().isNotFound());
    }

    @Test
    void movesToTheNextAndPreviousStep() throws Exception {
        given(service.moveToNext(COOK, ID, MIX.id())).willReturn(KNEAD);
        given(service.moveToPrevious(COOK, ID, KNEAD.id())).willReturn(MIX);

        mvc.perform(move("next", MIX.id()).with(cook("meal-preparation:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.howToStepId").value(KNEAD.id().value().toString()))
                .andExpect(jsonPath("$.sequenceNumber").value(2));
        mvc.perform(move("previous", KNEAD.id()).with(cook("meal-preparation:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sequenceNumber").value(1));
    }

    @Test
    void movingNeedsTheWriteScope() throws Exception {
        mvc.perform(move("next", MIX.id()).with(cook("meal-preparation:read"))).andExpect(status().isForbidden());
        then(service).should(never()).moveToNext(any(), any(), any());
    }

    @Test
    void refusedMovesAreBadRequests() throws Exception {
        given(service.moveToNext(COOK, ID, MIX.id())).willThrow(new MealPreparationRuleViolationException(
                MealPreparationRuleViolationException.STEP_NOT_CURRENT, "stale"));
        given(service.moveToNext(COOK, ID, BAKE.id())).willThrow(new MealPreparationRuleViolationException(
                MealPreparationRuleViolationException.LAST_STEP_REACHED, "last"));
        given(service.moveToPrevious(COOK, ID, MIX.id())).willThrow(new MealPreparationRuleViolationException(
                MealPreparationRuleViolationException.FIRST_STEP_REACHED, "first"));

        mvc.perform(move("next", MIX.id()).with(cook("meal-preparation:write")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("STEP_NOT_CURRENT"));
        mvc.perform(move("next", BAKE.id()).with(cook("meal-preparation:write")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("LAST_STEP_REACHED"));
        mvc.perform(move("previous", MIX.id()).with(cook("meal-preparation:write")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FIRST_STEP_REACHED"));
    }
}
