package org.larder.recipecatalog.adapter.in.web;

import static org.hamcrest.Matchers.endsWith;
import static org.larder.recipecatalog.TestData.BUTTER_ID;
import static org.larder.recipecatalog.TestData.COOK;
import static org.larder.recipecatalog.TestData.FLOUR_ID;
import static org.larder.recipecatalog.TestData.MIX;
import static org.larder.recipecatalog.TestData.MIX_ID;
import static org.larder.recipecatalog.TestData.SCONES;
import static org.larder.recipecatalog.TestData.SCONES_ID;
import static org.larder.recipecatalog.TestData.scones;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.larder.platform.security.LarderSecurityConfiguration;
import org.larder.platform.test.TestTokens;
import org.larder.platform.web.LarderWebConfiguration;
import org.larder.recipecatalog.application.NotFoundException;
import org.larder.recipecatalog.application.NotPermittedException;
import org.larder.recipecatalog.application.RecipeService;
import org.larder.recipecatalog.domain.CookId;
import org.larder.recipecatalog.domain.Diet;
import org.larder.recipecatalog.domain.HowToStepDraft;
import org.larder.recipecatalog.domain.HowToStepId;
import org.larder.recipecatalog.domain.IngredientDraft;
import org.larder.recipecatalog.domain.Meal;
import org.larder.recipecatalog.domain.Recipe;
import org.larder.recipecatalog.domain.RecipeDraft;
import org.larder.recipecatalog.domain.RecipeId;
import org.larder.recipecatalog.domain.RecipeRevision;
import org.larder.recipecatalog.domain.RecipeRuleViolationException;
import org.larder.recipecatalog.domain.RecipeSearch;
import org.larder.recipecatalog.domain.Unit;
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

@WebMvcTest({RecipesController.class, IngredientsController.class, HowToStepsController.class, MealsController.class,
        RecipeCatalogErrorAdvice.class})
@Import({LarderSecurityConfiguration.class, LarderWebConfiguration.class})
class RecipeCatalogControllerTest {

    private static final String RECIPES = "/recipe-catalog/recipes";
    private static final String SCONES_URL = RECIPES + "/" + SCONES_ID.value();

    private static final String SCONES_REQUEST = """
            {"name":"Scones for Sunday","subtitle":"Easy to prepare on Saturday","preparationTime":"02:00",
             "servings":4,"meal":"BREAKFAST","diet":"VEGETARIAN",
             "ingredients":[{"name":"Flour","value":0.5,"unit":"KILOGRAM"}],
             "howToSteps":[{"sequenceNumber":1,"description":"Carefully mix the water with the flour"}]}""";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RecipeService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor cook(String... scopes) {
        return TestTokens.cook(COOK.value(), scopes);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.header("version", "1.0.0").contentType(MediaType.APPLICATION_JSON).content(body);
    }

    // --- Recipes -------------------------------------------------------------------------------

    @Test
    void returnsARecipeInTheContractShape() throws Exception {
        given(service.recipe(SCONES_ID)).willReturn(scones());

        mvc.perform(get(SCONES_URL).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeId").value(SCONES_ID.value().toString()))
                .andExpect(jsonPath("$.owner").value(COOK.value().toString()))
                .andExpect(jsonPath("$.name").value(SCONES))
                .andExpect(jsonPath("$.preparationTime").value("02:00"))
                .andExpect(jsonPath("$.meal").value("BREAKFAST"))
                .andExpect(jsonPath("$.diet").value("VEGETARIAN"))
                .andExpect(jsonPath("$.ingredients[0].ingredientId").value(FLOUR_ID.value().toString()))
                .andExpect(jsonPath("$.ingredients[0].value").value(0.5))
                .andExpect(jsonPath("$.ingredients[0].unit").value("KILOGRAM"))
                .andExpect(jsonPath("$.howToSteps[0].sequenceNumber").value(1))
                .andExpect(jsonPath("$.howToSteps[0].description").value(MIX));
    }

    @Test
    void searchesWithTheContractsQueryParameters() throws Exception {
        given(service.search(any())).willReturn(List.of(scones()));

        mvc.perform(get(RECIPES).param("meal", "BREAKFAST").param("diet", "VEGETARIAN")
                        .param("ingredients", "Flour").param("ingredients", "Butter")
                        .header("version", "1.0.0").with(cook("recipe:admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipeId").value(SCONES_ID.value().toString()));

        var search = ArgumentCaptor.forClass(RecipeSearch.class);
        then(service).should().search(search.capture());
        org.assertj.core.api.Assertions.assertThat(search.getValue())
                .isEqualTo(new RecipeSearch(Meal.BREAKFAST, Diet.VEGETARIAN, List.of("Flour", "Butter")));
    }

    @Test
    void searchingWithoutFiltersReturnsTheWholeCatalog() throws Exception {
        given(service.search(RecipeSearch.all())).willReturn(List.of());

        mvc.perform(get(RECIPES).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void anUnknownMealIsABadRequest() throws Exception {
        mvc.perform(get(RECIPES).param("meal", "BRUNCH").header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsARecipeForTheCallingCookAndLinksToIt() throws Exception {
        Recipe created = scones();
        given(service.create(eq(COOK), any())).willReturn(created);

        mvc.perform(json(post(RECIPES), SCONES_REQUEST).with(cook("recipe:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(SCONES_URL)))
                .andExpect(jsonPath("$.recipeLink").value(endsWith(SCONES_URL)));

        var draft = ArgumentCaptor.forClass(RecipeDraft.class);
        then(service).should().create(eq(COOK), draft.capture());
        org.assertj.core.api.Assertions.assertThat(draft.getValue().preparationTime()).hasToString("02:00");
        org.assertj.core.api.Assertions.assertThat(draft.getValue().ingredients().getFirst().quantity().unit())
                .isEqualTo(Unit.KILOGRAM);
    }

    @Test
    void writingNeedsTheWriteScope() throws Exception {
        mvc.perform(json(post(RECIPES), SCONES_REQUEST).with(cook("recipe:read", "recipe:admin")))
                .andExpect(status().isForbidden());
        mvc.perform(delete(SCONES_URL).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isForbidden());
        then(service).shouldHaveNoInteractions();
    }

    @Test
    void readingNeedsAToken() throws Exception {
        mvc.perform(get(RECIPES).header("version", "1.0.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contractViolationsAreBadRequests() throws Exception {
        mvc.perform(json(post(RECIPES), SCONES_REQUEST.replace("\"02:00\"", "\"2h\"")).with(cook("recipe:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mvc.perform(json(post(RECIPES), SCONES_REQUEST.replace("\"ingredients\":[{\"name\":\"Flour\",\"value\":0.5,\"unit\":\"KILOGRAM\"}]",
                        "\"ingredients\":[]")).with(cook("recipe:write")))
                .andExpect(status().isBadRequest());
        mvc.perform(json(post(RECIPES), SCONES_REQUEST.replace("\"servings\":4", "\"servings\":0")).with(cook("recipe:write")))
                .andExpect(status().isBadRequest());
        then(service).shouldHaveNoInteractions();
    }

    @Test
    void anIngredientValueOfZeroBreaksARecipeRule() throws Exception {
        mvc.perform(json(post(RECIPES), SCONES_REQUEST.replace("\"value\":0.5", "\"value\":0")).with(cook("recipe:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RECIPE"));
    }

    @Test
    void updatingARecipeAppliesOnlyTheGivenDetails() throws Exception {
        mvc.perform(json(patch(SCONES_URL), "{\"servings\":6,\"preparationTime\":\"01:30\"}").with(cook("recipe:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeLink").value(endsWith(SCONES_URL)));

        var revision = ArgumentCaptor.forClass(RecipeRevision.class);
        then(service).should().revise(eq(COOK), eq(SCONES_ID), revision.capture());
        org.assertj.core.api.Assertions.assertThat(revision.getValue().servings()).isEqualTo(6);
        org.assertj.core.api.Assertions.assertThat(revision.getValue().preparationTime()).hasToString("01:30");
        org.assertj.core.api.Assertions.assertThat(revision.getValue().name()).isNull();
        org.assertj.core.api.Assertions.assertThat(revision.getValue().furtherImages()).isNull();
    }

    @Test
    void changingSomebodyElsesRecipeIsForbidden() throws Exception {
        willThrow(new NotPermittedException("not yours")).given(service).delete(COOK, SCONES_ID);

        mvc.perform(delete(SCONES_URL).header("version", "1.0.0").with(cook("recipe:write")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_PERMITTED"));
    }

    @Test
    void anUnknownRecipeIsNotFound() throws Exception {
        RecipeId unknown = RecipeId.newId();
        given(service.recipe(unknown)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(RECIPES + "/" + unknown.value()).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void deletingReturnsNoContent() throws Exception {
        mvc.perform(delete(SCONES_URL).header("version", "1.0.0").with(cook("recipe:write")))
                .andExpect(status().isNoContent());
        then(service).should().delete(COOK, SCONES_ID);
    }

    // --- Ingredients ---------------------------------------------------------------------------

    @Test
    void listsAndReturnsIngredients() throws Exception {
        Recipe recipe = scones();
        given(service.recipe(SCONES_ID)).willReturn(recipe);
        given(service.ingredient(SCONES_ID, BUTTER_ID)).willReturn(recipe.ingredient(BUTTER_ID).orElseThrow());

        mvc.perform(get(SCONES_URL + "/ingredients").header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].name").value("Butter"));
        mvc.perform(get(SCONES_URL + "/ingredients/" + BUTTER_ID.value()).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unit").value("GRAM"))
                .andExpect(jsonPath("$.value").value(125));
    }

    @Test
    void addsAnIngredientAndLinksToIt() throws Exception {
        Recipe recipe = scones();
        var eggs = recipe.addIngredient(new IngredientDraft("Eggs", org.larder.recipecatalog.domain.Quantity.of("2", Unit.PIECE)));
        given(service.addIngredient(eq(COOK), eq(SCONES_ID), any())).willReturn(eggs);

        mvc.perform(json(post(SCONES_URL + "/ingredients"), "{\"name\":\"Eggs\",\"value\":2,\"unit\":\"PIECE\"}")
                        .with(cook("recipe:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(SCONES_URL + "/ingredients/" + eggs.id().value())))
                .andExpect(jsonPath("$.ingredientLink").value(endsWith(eggs.id().value().toString())));
    }

    @Test
    void changesPartsOfAnIngredient() throws Exception {
        String url = SCONES_URL + "/ingredients/" + FLOUR_ID.value();

        mvc.perform(json(patch(url), "{\"value\":0.75}").with(cook("recipe:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredientLink").value(endsWith(url)));

        then(service).should().changeIngredient(eq(COOK), eq(SCONES_ID), eq(FLOUR_ID), isNull(),
                eq(new BigDecimal("0.75")), isNull());
    }

    @Test
    void removingTheLastIngredientIsABadRequest() throws Exception {
        willThrow(new RecipeRuleViolationException(RecipeRuleViolationException.RECIPE_NEEDS_INGREDIENT, "last one"))
                .given(service).removeIngredient(COOK, SCONES_ID, FLOUR_ID);

        mvc.perform(delete(SCONES_URL + "/ingredients/" + FLOUR_ID.value()).header("version", "1.0.0").with(cook("recipe:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RECIPE_NEEDS_INGREDIENT"));
    }

    // --- How-To Steps --------------------------------------------------------------------------

    @Test
    void listsHowToStepsInSequenceOrder() throws Exception {
        Recipe recipe = scones();
        recipe.changeHowToStep(MIX_ID, 3, null, null);
        given(service.recipe(SCONES_ID)).willReturn(recipe);

        mvc.perform(get(SCONES_URL + "/how-to-steps").header("version", "1.0.0").with(cook("recipe:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sequenceNumber").value(2))
                .andExpect(jsonPath("$[1].sequenceNumber").value(3));
    }

    @Test
    void addsAHowToStepAndLinksToIt() throws Exception {
        Recipe recipe = scones();
        var serve = recipe.addHowToStep(new HowToStepDraft(3, "Serve warm", null));
        given(service.addHowToStep(eq(COOK), eq(SCONES_ID), any())).willReturn(serve);

        mvc.perform(json(post(SCONES_URL + "/how-to-steps"), "{\"sequenceNumber\":3,\"description\":\"Serve warm\"}")
                        .with(cook("recipe:write")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/how-to-steps/" + serve.id().value())))
                .andExpect(jsonPath("$.howToStepLink").value(endsWith(serve.id().value().toString())));
    }

    @Test
    void aTakenSequenceNumberIsABadRequest() throws Exception {
        given(service.changeHowToStep(COOK, SCONES_ID, MIX_ID, 2, null, null))
                .willThrow(new RecipeRuleViolationException(RecipeRuleViolationException.SEQUENCE_NUMBER_TAKEN, "taken"));

        mvc.perform(json(patch(SCONES_URL + "/how-to-steps/" + MIX_ID.value()), "{\"sequenceNumber\":2}")
                        .with(cook("recipe:write")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SEQUENCE_NUMBER_TAKEN"));
    }

    @Test
    void anUnknownHowToStepIsNotFound() throws Exception {
        HowToStepId unknown = HowToStepId.newId();
        given(service.howToStep(SCONES_ID, unknown)).willThrow(new NotFoundException("missing"));

        mvc.perform(get(SCONES_URL + "/how-to-steps/" + unknown.value()).header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingAHowToStepReturnsNoContent() throws Exception {
        mvc.perform(delete(SCONES_URL + "/how-to-steps/" + MIX_ID.value()).header("version", "1.0.0").with(cook("recipe:write")))
                .andExpect(status().isNoContent());
        then(service).should().removeHowToStep(COOK, SCONES_ID, MIX_ID);
    }

    // --- Meals ---------------------------------------------------------------------------------

    @Test
    void returnsAndSetsTheMealOfARecipe() throws Exception {
        given(service.recipe(SCONES_ID)).willReturn(scones());

        mvc.perform(get(SCONES_URL + "/meals").header("version", "1.0.0").with(cook("recipe:read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meal").value("BREAKFAST"));
        mvc.perform(json(put(SCONES_URL + "/meals"), "{\"meal\":\"SUPPER\"}").with(cook("recipe:write")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipeMealLink").value(endsWith(SCONES_URL + "/meals")));

        then(service).should().assignMeal(COOK, SCONES_ID, Meal.SUPPER);
    }

    @Test
    void settingTheMealNeedsAMeal() throws Exception {
        mvc.perform(json(put(SCONES_URL + "/meals"), "{}").with(cook("recipe:write")))
                .andExpect(status().isBadRequest());
        then(service).should(never()).assignMeal(any(CookId.class), any(), any());
    }
}
