package org.larder.mealplanning.adapter.out.recipecatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.mealplanning.TestData.SCONES;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.mealplanning.application.NotPermittedException;
import org.larder.mealplanning.application.RecipeCatalogUnavailableException;
import org.larder.mealplanning.domain.Diet;
import org.larder.mealplanning.domain.RecipeFacts;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpRecipeCatalogTest {

    private static final String BASE_URL = "http://localhost:8080/recipe-catalog";
    private static final String SCONES_URL = BASE_URL + "/recipes/" + SCONES.value();

    private static final String SCONES_JSON = """
            {"recipeId":"%s","owner":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074","name":"Scones for Sunday",
             "preparationTime":"02:00","servings":4,"meal":"BREAKFAST","diet":"%s",
             "ingredients":[{"ingredientId":"2b1c4d6e-8f0a-4b2c-9d4e-6f8a0b2c4d6e","name":"Flour","value":0.5,"unit":"KILOGRAM"}],
             "howToSteps":[{"howToStepId":"4d3e6f8a-0b2c-4d4e-9f6a-8b0c2d4e6f8a","sequenceNumber":1,"description":"Mix"}]}""";

    private MockRestServiceServer server;
    private HttpRecipeCatalog catalog;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        catalog = new HttpRecipeCatalog(RecipeCatalogClientConfiguration.recipesApi(builder, BASE_URL));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsTheDietOfARecipeAtTheConfiguredBaseUrlRelayingTheCallersToken() {
        Jwt jwt = Jwt.withTokenValue("cooks-token").header("alg", "none").claim("cookId", "f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        server.expect(requestTo(SCONES_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer cooks-token"))
                .andExpect(header("version", "1.0.0"))
                .andRespond(withSuccess(SCONES_JSON.formatted(SCONES.value(), "VEGETARIAN"), MediaType.APPLICATION_JSON));

        assertThat(catalog.recipe(SCONES)).contains(new RecipeFacts(SCONES, Diet.VEGETARIAN));
        server.verify();
    }

    @Test
    void translatesEveryDietOfTheCatalog() {
        for (var diet : Map.of("VEGAN", Diet.VEGAN, "VEGETARIAN", Diet.VEGETARIAN, "NORMAL", Diet.NORMAL).entrySet()) {
            server.reset();
            server.expect(requestTo(SCONES_URL))
                    .andRespond(withSuccess(SCONES_JSON.formatted(SCONES.value(), diet.getKey()), MediaType.APPLICATION_JSON));
            assertThat(catalog.recipe(SCONES)).contains(new RecipeFacts(SCONES, diet.getValue()));
        }
    }

    @Test
    void withoutAnAuthenticatedCallerNoTokenIsSent() {
        server.expect(requestTo(SCONES_URL))
                .andExpect(headerDoesNotExist("Authorization"))
                .andRespond(withSuccess(SCONES_JSON.formatted(SCONES.value(), "VEGAN"), MediaType.APPLICATION_JSON));

        assertThat(catalog.recipe(SCONES)).isPresent();
        server.verify();
    }

    @Test
    void anUnknownRecipeIsEmpty() {
        server.expect(requestTo(SCONES_URL)).andRespond(withResourceNotFound());

        assertThat(catalog.recipe(SCONES)).isEmpty();
    }

    @Test
    void aRefusedCallerIsNotPermitted() {
        server.expect(requestTo(SCONES_URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> catalog.recipe(SCONES)).isInstanceOf(NotPermittedException.class);
    }

    @Test
    void aFailingCatalogIsUnavailable() {
        server.expect(requestTo(SCONES_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> catalog.recipe(SCONES)).isInstanceOf(RecipeCatalogUnavailableException.class);
    }

    @Test
    void anAnswerOutsideTheContractIsUnavailable() {
        server.expect(requestTo(SCONES_URL))
                .andRespond(withSuccess(SCONES_JSON.formatted(SCONES.value(), "KETO"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> catalog.recipe(SCONES)).isInstanceOf(RecipeCatalogUnavailableException.class);
    }
}
