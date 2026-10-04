package org.larder.mealpreparation.adapter.out.recipecatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.larder.mealpreparation.TestData.BAKE;
import static org.larder.mealpreparation.TestData.COOK;
import static org.larder.mealpreparation.TestData.KNEAD;
import static org.larder.mealpreparation.TestData.MIX;
import static org.larder.mealpreparation.TestData.SCONES;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.larder.mealpreparation.application.NotPermittedException;
import org.larder.mealpreparation.application.RecipeCatalogUnavailableException;
import org.larder.mealpreparation.domain.HowToStep;
import org.larder.mealpreparation.domain.RecipeSteps;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Talks to a stand-in for Recipe Catalog that answers as its contract says. */
class RecipeCatalogClientTest {

    private static final String BASE_URL = "http://larder.test/recipe-catalog";
    private static final String SCONE_STEPS = BASE_URL + "/recipes/" + SCONES.value() + "/how-to-steps";

    private MockRestServiceServer recipeCatalog;
    private RecipeCatalogClient client;

    @BeforeEach
    void stubRecipeCatalog() {
        RestClient.Builder builder = RestClient.builder();
        recipeCatalog = MockRestServiceServer.bindTo(builder).build();
        client = RecipeCatalogClient.create(builder, BASE_URL);
    }

    @AfterEach
    void forgetCaller() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsTheStepsInTheRecipesOrderWithTheCallersToken() {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("cooks-token")
                .header("alg", "none").claim("cookId", COOK.value().toString()).build()));
        recipeCatalog.expect(requestTo(SCONE_STEPS))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("version", "1.0.0"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer cooks-token"))
                .andRespond(withSuccess("""
                        [{"howToStepId":"%s","sequenceNumber":3,"description":"Bake for 15 minutes"},
                         {"howToStepId":"%s","sequenceNumber":1,"description":"Carefully mix the water with the flour",
                          "illustration":null},
                         {"howToStepId":"%s","sequenceNumber":2,"description":"Knead the dough"}]
                        """.formatted(BAKE.id().value(), MIX.id().value(), KNEAD.id().value()), MediaType.APPLICATION_JSON));

        RecipeSteps steps = client.stepsOf(SCONES).orElseThrow();

        assertThat(steps.steps()).containsExactly(MIX, KNEAD, BAKE);
        recipeCatalog.verify();
    }

    @Test
    void sendsNoTokenWhenThereIsNoCaller() {
        recipeCatalog.expect(requestTo(SCONE_STEPS))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.stepsOf(SCONES)).contains(new RecipeSteps(List.<HowToStep>of()));
        recipeCatalog.verify();
    }

    @Test
    void anUnknownRecipeIsEmpty() {
        recipeCatalog.expect(requestTo(SCONE_STEPS)).andRespond(withResourceNotFound());

        assertThat(client.stepsOf(SCONES)).isEmpty();
    }

    @Test
    void aRefusalOfRecipeCatalogIsNotPermitted() {
        recipeCatalog.expect(requestTo(SCONE_STEPS)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.stepsOf(SCONES)).isInstanceOf(NotPermittedException.class);
    }

    @Test
    void aFailingRecipeCatalogIsUnavailable() {
        recipeCatalog.expect(requestTo(SCONE_STEPS)).andRespond(withServerError());

        assertThatThrownBy(() -> client.stepsOf(SCONES)).isInstanceOf(RecipeCatalogUnavailableException.class);
    }

    @Test
    void anUnreachableRecipeCatalogIsUnavailable() {
        recipeCatalog.expect(requestTo(SCONE_STEPS)).andRespond(request -> {
            throw new IOException("connection refused");
        });

        assertThatThrownBy(() -> client.stepsOf(SCONES)).isInstanceOf(RecipeCatalogUnavailableException.class);
    }

    @Test
    void unusableStepsAreUnavailable() {
        recipeCatalog.expect(requestTo(SCONE_STEPS)).andRespond(withSuccess("""
                [{"howToStepId":"%1$s","sequenceNumber":1,"description":"Mix"},
                 {"howToStepId":"%1$s","sequenceNumber":2,"description":"Mix again"}]
                """.formatted(MIX.id().value()), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.stepsOf(SCONES)).isInstanceOf(RecipeCatalogUnavailableException.class);
    }
}
