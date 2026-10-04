package org.larder.mealplanning.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.larder.mealplanning.TestData.COOK;
import static org.larder.mealplanning.TestData.HOW_TO_SERVE;
import static org.larder.mealplanning.TestData.OCCASION;
import static org.larder.mealplanning.TestData.PLAN_ID;
import static org.larder.mealplanning.TestData.SCONES;
import static org.larder.mealplanning.TestData.dinner;
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

import org.junit.jupiter.api.Test;
import org.larder.mealplanning.application.ChangeMealPlan;
import org.larder.mealplanning.application.MealPlanService;
import org.larder.mealplanning.application.NotFoundException;
import org.larder.mealplanning.application.NotPermittedException;
import org.larder.mealplanning.application.RecipeCatalogUnavailableException;
import org.larder.mealplanning.application.SetUpMealPlan;
import org.larder.mealplanning.application.UnknownRecipeException;
import org.larder.mealplanning.domain.CourseDraft;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.Meal;
import org.larder.mealplanning.domain.MealPlan;
import org.larder.mealplanning.domain.MealPlanDraft;
import org.larder.mealplanning.domain.MealPlanSearch;
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

@WebMvcTest({MealPlansController.class, MealPlanningErrorAdvice.class, MealPlanRequestBodyAdvice.class,
        MealPlanningJacksonModule.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class MealPlansControllerTest {

    private static final String MEAL_PLANS = "/meal-planning/meal-plans";
    private static final String DINNER_URL = MEAL_PLANS + "/" + PLAN_ID.value();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealPlanService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void searchesTheCallersPlansByTheMostRestrictiveDiet() throws Exception {
        given(service.search(COOK, new MealPlanSearch(Diet.VEGAN, OCCASION))).willReturn(List.of(dinner()));

        mvc.perform(get(MEAL_PLANS).param("diet", "vegetarian", "vegan").param("occasion", OCCASION)
                        .header("version", "1.0.0").with(cook("meal-plan:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mealPlanId").value(PLAN_ID.value().toString()))
                .andExpect(jsonPath("$[0].owner").value(COOK.value().toString()))
                .andExpect(jsonPath("$[0].meal").value("dinner"))
                .andExpect(jsonPath("$[0].servings").value(6))
                .andExpect(jsonPath("$[0].howToServe").value(HOW_TO_SERVE))
                .andExpect(jsonPath("$[0].courses[1].step").value(2))
                .andExpect(jsonPath("$[0].courses[1].meal.recipe").value(SCONES.value().toString()));
    }

    @Test
    void anUnknownDietIsABadRequest() throws Exception {
        mvc.perform(get(MEAL_PLANS).param("diet", "keto").header("version", "1.0.0").with(cook("meal-plan:read")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_DIET"));
    }

    @Test
    void anEmptyPlanLeavesOutItsMissingParts() throws Exception {
        MealPlan empty = MealPlan.setUp(COOK, MealPlanDraft.empty());
        given(service.mealPlan(COOK, empty.id())).willReturn(empty);

        mvc.perform(get(MEAL_PLANS + "/" + empty.id().value()).header("version", "1.0.0").with(cook("meal-plan:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealPlanId").value(empty.id().value().toString()))
                .andExpect(jsonPath("$.occasion").doesNotExist())
                .andExpect(jsonPath("$.servings").doesNotExist())
                .andExpect(jsonPath("$.howToServe").doesNotExist())
                .andExpect(jsonPath("$.courses").doesNotExist());
    }

    @Test
    void setsUpAnEmptyPlanAndLinksToIt() throws Exception {
        MealPlan empty = MealPlan.setUp(COOK, MealPlanDraft.empty());
        given(service.setUp(COOK, SetUpMealPlan.empty())).willReturn(empty);

        mvc.perform(json(post(MEAL_PLANS), "{}").with(cook("meal-plan:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(MEAL_PLANS + "/" + empty.id().value())))
                .andExpect(jsonPath("$.mealPlanLink").value(endsWith(empty.id().value().toString())));
    }

    @Test
    void setsUpAPlanWithCourses() throws Exception {
        given(service.setUp(any(), any())).willReturn(dinner());

        mvc.perform(json(post(MEAL_PLANS), """
                        {"occasion":"%s","servings":6,"meal":"dinner",
                         "courses":[{"step":1,"meal":{"recipe":"%s"}},{"step":1,"meal":{"recipe":"%s"}}]}"""
                        .formatted(OCCASION, SCONES.value(), SCONES.value())).with(cook("meal-plan:write")))
                .andExpect(status().isCreated());

        then(service).should().setUp(COOK, new SetUpMealPlan(OCCASION, 6, Meal.DINNER, null,
                List.of(new CourseDraft(1, SCONES), new CourseDraft(1, SCONES))));
    }

    @Test
    void contractViolationsAreBadRequests() throws Exception {
        String course = "{\"step\":1,\"meal\":{\"recipe\":\"%s\"}}".formatted(SCONES.value());
        for (String body : List.of(
                "{\"courses\":[]}",
                "{\"courses\":[" + String.join(",", java.util.Collections.nCopies(11, course)) + "]}",
                "{\"courses\":[{\"step\":0,\"meal\":{\"recipe\":\"%s\"}}]}".formatted(SCONES.value()),
                "{\"courses\":[{\"step\":1}]}",
                "{\"servings\":0}",
                "{\"meal\":\"brunch\"}",
                "{\"occasion\":null}")) {
            mvc.perform(json(post(MEAL_PLANS), body).with(cook("meal-plan:write")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        }
        then(service).should(never()).setUp(any(), any());
    }

    @Test
    void aCourseWithAnUnknownRecipeIsABadRequest() throws Exception {
        given(service.setUp(any(), any())).willThrow(new UnknownRecipeException(SCONES));

        mvc.perform(json(post(MEAL_PLANS), "{\"courses\":[{\"step\":1,\"meal\":{\"recipe\":\"%s\"}}]}"
                        .formatted(SCONES.value())).with(cook("meal-plan:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_RECIPE"));
    }

    @Test
    void anUnavailableRecipeCatalogIsServiceNotAvailable() throws Exception {
        given(service.setUp(any(), any())).willThrow(new RecipeCatalogUnavailableException("down", null));

        mvc.perform(json(post(MEAL_PLANS), "{\"courses\":[{\"step\":1,\"meal\":{\"recipe\":\"%s\"}}]}"
                        .formatted(SCONES.value())).with(cook("meal-plan:write")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("UPSTREAM_UNAVAILABLE"));
    }

    @Test
    void changesOnlyTheGivenPartsAndLinksToThePlan() throws Exception {
        given(service.change(any(), any(), any())).willReturn(dinner());

        mvc.perform(json(patch(DINNER_URL), "{\"servings\":8}").with(cook("meal-plan:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mealPlanLink").value(endsWith(DINNER_URL)));

        then(service).should().change(COOK, PLAN_ID, new ChangeMealPlan(null, 8, null, null, false, null));
    }

    @Test
    void howToServeGivenAsNullRemovesIt() throws Exception {
        given(service.change(any(), any(), any())).willReturn(dinner());

        mvc.perform(json(patch(DINNER_URL), "{\"howToServe\":null}").with(cook("meal-plan:write")))
                .andExpect(status().isOk());

        ArgumentCaptor<ChangeMealPlan> change = ArgumentCaptor.forClass(ChangeMealPlan.class);
        then(service).should().change(eq(COOK), eq(PLAN_ID), change.capture());
        assertThat(change.getValue()).isEqualTo(new ChangeMealPlan(null, null, null, null, true, null));
    }

    @Test
    void givenCoursesReplaceTheExistingOnes() throws Exception {
        given(service.change(any(), any(), any())).willReturn(dinner());

        mvc.perform(json(patch(DINNER_URL), "{\"courses\":[{\"step\":2,\"meal\":{\"recipe\":\"%s\"}}]}"
                        .formatted(SCONES.value())).with(cook("meal-plan:write")))
                .andExpect(status().isOk());

        then(service).should().change(COOK, PLAN_ID,
                new ChangeMealPlan(null, null, null, null, false, List.of(new CourseDraft(2, SCONES))));
    }

    @Test
    void aChangeNeedsAtLeastOnePropertyAndNoNullsButHowToServe() throws Exception {
        for (String body : List.of("{}", "{\"unknown\":1}", "{\"servings\":null}", "{\"courses\":null}", "{\"courses\":[]}")) {
            mvc.perform(json(patch(DINNER_URL), body).with(cook("meal-plan:write")))
                    .andExpect(status().isBadRequest());
        }
        then(service).should(never()).change(any(), any(), any());
    }

    @Test
    void changingSomebodyElsesPlanIsForbidden() throws Exception {
        given(service.change(any(), any(), any())).willThrow(new NotPermittedException("not yours"));

        mvc.perform(json(patch(DINNER_URL), "{\"servings\":8}").with(cook("meal-plan:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void anUnknownPlanIsNotFound() throws Exception {
        given(service.mealPlan(COOK, PLAN_ID)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(DINNER_URL).header("version", "1.0.0").with(cook("meal-plan:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void deletingReturnsNoContentAndOnlyTheOwnerMayDelete() throws Exception {
        mvc.perform(delete(DINNER_URL).header("version", "1.0.0").with(cook("meal-plan:write")))
                .andExpect(status().isNoContent());
        then(service).should().delete(COOK, PLAN_ID);

        willThrow(new NotPermittedException("not yours")).given(service).delete(COOK, PLAN_ID);
        mvc.perform(delete(DINNER_URL).header("version", "1.0.0").with(cook("meal-plan:write")))
                .andExpect(status().isForbidden());
    }

    @Test
    void writingNeedsTheWriteScopeAndReadingAToken() throws Exception {
        mvc.perform(json(post(MEAL_PLANS), "{}").with(cook("meal-plan:read")))
                .andExpect(status().isForbidden());
        mvc.perform(json(patch(DINNER_URL), "{\"servings\":8}").with(cook("meal-plan:read")))
                .andExpect(status().isForbidden());
        mvc.perform(delete(DINNER_URL).header("version", "1.0.0").with(cook("meal-plan:read")))
                .andExpect(status().isForbidden());
        mvc.perform(get(MEAL_PLANS).header("version", "1.0.0"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(MEAL_PLANS).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theWriteScopeAlsoReads() throws Exception {
        given(service.search(COOK, MealPlanSearch.all())).willReturn(List.of());

        mvc.perform(get(MEAL_PLANS).header("version", "1.0.0").with(cook("meal-plan:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
