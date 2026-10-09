package org.larder.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.larder.LarderApplication;
import org.larder.e2e.ContractHttp.Response;
import org.larder.e2e.MessageTap.TappedMessage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The rescue story of Larder as an executable specification, end to end through the whole modular
 * monolith: real PostgreSQL (one schema per Bounded Context), real RabbitMQ, an S3 bucket, all ten
 * Bounded Contexts calling each other over HTTP - only Keycloak is replaced by tokens the test signs.
 * <p>
 * Every HTTP response is checked against the OpenAPI contract of the context that served it
 * ({@link ContractHttp}), every message on the exchanges {@code cooking-assistance} and
 * {@code grandma-avatar} against its AsyncAPI contract ({@link MessageTap}).
 * <p>
 * Two cooks: Anna (the cook of the glossary examples) burns her scones; Ben from the community
 * explains a step to her.
 */
@SpringBootTest(classes = LarderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Import(AccessTokens.Security.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("The rescue story")
class RescueStoryEndToEndTest {

    static final UUID ANNA = UUID.fromString("f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074");
    static final UUID BEN = UUID.fromString("39a7aed5-2e50-48c8-8aa6-f3afa9f03f74");

    /** Consent texts seeded by Consent Management (CHANGES.md, phase 2). */
    static final String PHOTOS_IN_PUBLIC_THANKS = "5f8d8a1c-515d-4eae-a6b1-0a0313edfc31";
    static final String MENTION_AS_HELPER = "3c6e2b7a-9d41-4f0b-8e5a-2b7c9d1e4f60";

    /** A 1x1 pixel PNG - the picture of the rescued scones. */
    static final String PNG = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";

    static final Duration GRANDMA_PATIENCE = Duration.ofSeconds(10);

    @DynamicPropertySource
    static void infrastructure(DynamicPropertyRegistry registry) {
        LarderInfrastructure.register(registry);
    }

    final ContractHttp api = new ContractHttp(LarderInfrastructure.APP_PORT);
    final ContractHttp.Caller anna = api.as(AccessTokens.forCook(ANNA));
    final ContractHttp.Caller ben = api.as(AccessTokens.forCook(BEN));
    MessageTap tap;
    long startedAt;

    // what the story builds up, step by step
    String recipe;
    String firstStep;
    String secondStep;
    String mealPlan;
    String preparation;
    String helpRequest;
    String grandmasHelp;
    String picture;
    String communityRequest;
    String bensHelp;
    String thanksToBen;

    @BeforeAll
    void listenInOnThePublishedLanguage() {
        startedAt = System.nanoTime();
        tap = new MessageTap(LarderInfrastructure.RABBITMQ.getHost(), LarderInfrastructure.RABBITMQ.getAmqpPort(),
                LarderInfrastructure.RABBITMQ.getAdminUsername(), LarderInfrastructure.RABBITMQ.getAdminPassword());
    }

    // ---------------------------------------------------------------------------------------------
    // Anna cooks

    @Test
    @Order(1)
    @DisplayName("1. Anna registers as a cook (Cook Profile)")
    void annaRegisters() {
        Response registered = anna.post("/cook-profile/cooks", """
                {"email": "anna@larder.org", "name": "Baker", "givenName": "Anna"}""").expect(201);

        assertThat(registered.createdId()).isEqualTo(ANNA.toString());
        assertThat(anna.get("/cook-profile/cooks/" + ANNA).expect(200).path("givenName").asText()).isEqualTo("Anna");
    }

    @Test
    @Order(2)
    @DisplayName("2. Anna writes down her scones recipe (Recipe Catalog)")
    void annaCreatesARecipe() {
        recipe = anna.post("/recipe-catalog/recipes", """
                {"name": "Scones for Sunday", "preparationTime": "00:45", "servings": 6,
                 "meal": "BREAKFAST", "diet": "VEGETARIAN",
                 "ingredients": [{"name": "Flour", "value": 500, "unit": "GRAM"},
                                 {"name": "Buttermilk", "value": 250, "unit": "MILLILITER"}],
                 "howToSteps": [{"sequenceNumber": 1, "description": "Fold the buttermilk into the flour"},
                                {"sequenceNumber": 2, "description": "Bake at 220 degrees for 12 minutes"}]}
                """).expect(201).createdId();

        JsonNode steps = anna.get("/recipe-catalog/recipes/" + recipe).expect(200).path("howToSteps");
        firstStep = steps.get(0).path("howToStepId").asText();
        secondStep = steps.get(1).path("howToStepId").asText();
        assertThat(anna.get("/recipe-catalog/recipes?ingredients=flour&diet=VEGETARIAN").expect(200).body())
                .anyMatch(found -> found.path("recipeId").asText().equals(recipe));
    }

    @Test
    @Order(3)
    @DisplayName("3. Anna plans Sunday breakfast with it (Meal Planning reads Recipe Catalog)")
    void annaPlansAMeal() {
        mealPlan = anna.post("/meal-planning/meal-plans", "{}").expect(201).createdId();
        anna.patch("/meal-planning/meal-plans/" + mealPlan, """
                {"occasion": "Sunday breakfast with my mother-in-law", "servings": 6, "meal": "breakfast",
                 "courses": [{"step": 1, "meal": {"recipe": "%s"}}]}""".formatted(recipe)).expect(200);

        assertThat(anna.get("/meal-planning/meal-plans/" + mealPlan).expect(200).path("courses")).hasSize(1);
    }

    @Test
    @Order(4)
    @DisplayName("4. Anna starts cooking and moves on to step 2 (Meal Preparation)")
    void annaStartsThePreparation() {
        preparation = anna.post("/meal-preparation/preparations", """
                {"recipe": "%s"}""".formatted(recipe)).expect(201).createdId();
        assertThat(anna.get("/meal-preparation/preparations/" + preparation).expect(200)
                .path("currentStep").path("sequenceNumber").asInt()).isEqualTo(1);

        Response next = anna.patch("/meal-preparation/preparations/" + preparation
                + "/how-to-steps/next?stepId=" + firstStep, null).expect(200);

        assertThat(next.path("howToStepId").asText()).isEqualTo(secondStep);
    }

    // ---------------------------------------------------------------------------------------------
    // The catastrophe

    @Test
    @Order(5)
    @DisplayName("5. The scones burn - Anna asks Grandma and the community for help (Cooking Assistance)")
    void annaAsksForHelp() {
        helpRequest = anna.post("/cooking-assistance/help-requests", """
                {"title": "Burning Catastrophe", "type": "STEPS_TO_MITIGATE_CATASTROPHE",
                 "description": "Scones are burned and my mother-in-law is coming in 30 minutes",
                 "recipe": "%s", "preferredProvider": ["GRANDMA_AVATAR", "COMMUNITY"]}
                """.formatted(recipe)).expect(201).createdId();
    }

    @Test
    @Order(6)
    @DisplayName("6. Grandma answers asynchronously: the request becomes ANSWERED")
    void grandmaAnswers() {
        await().atMost(GRANDMA_PATIENCE).pollInterval(Duration.ofMillis(200)).untilAsserted(() ->
                assertThat(anna.get("/cooking-assistance/help-requests/" + helpRequest).expect(200)
                        .path("status").asText()).isEqualTo("ANSWERED"));

        JsonNode helps = anna.get("/cooking-assistance/helps?helpRequestId=" + helpRequest).expect(200).body();
        assertThat(helps).hasSize(1);
        assertThat(helps.get(0).path("helpProviderType").asText()).isEqualTo("GRANDMA_AVATAR");
        grandmasHelp = helps.get(0).path("helpId").asText();
        anna.get("/cooking-assistance/helps/" + grandmasHelp).expect(200);
    }

    @Test
    @Order(7)
    @DisplayName("7. The journey is one correlation: HelpRequested -> Grandma's HelpProvided -> HelpProvided")
    void theHelpJourneyIsTracedByTheHelpRequestId() {
        await().atMost(GRANDMA_PATIENCE).until(() -> journeyOf(helpRequest).size() >= 3);

        List<TappedMessage> journey = journeyOf(helpRequest);
        assertThat(journey).extracting(message -> message.exchange() + "/" + message.type()).containsExactly(
                "cooking-assistance/HelpRequested", "grandma-avatar/HelpProvided", "cooking-assistance/HelpProvided");
        assertThat(journey).extracting(TappedMessage::messageId).doesNotHaveDuplicates();
        assertThat(journey.get(0).payload()).contains(helpRequest);
        assertThat(journey.get(1).payload()).contains(helpRequest, grandmasHelp);
        assertThat(journey.get(2).payload()).contains(helpRequest, grandmasHelp);
    }

    @Test
    @Order(8)
    @DisplayName("8. Anna is notified about Grandma's help (Notification) - Ben is not")
    void annaIsNotified() {
        await().atMost(GRANDMA_PATIENCE).pollInterval(Duration.ofMillis(200)).until(() ->
                notificationAbout(anna, grandmasHelp) != null);
        JsonNode notification = notificationAbout(anna, grandmasHelp);
        String id = notification.path("notificationId").asText();
        assertThat(notification.path("status").asText()).isEqualTo("NEW");

        anna.put("/notifications/notifications/" + id + "/status", """
                {"status": "READ"}""").expect(200);

        assertThat(anna.get("/notifications/notifications/" + id).expect(200).path("status").asText()).isEqualTo("READ");
        assertThat(notificationAbout(ben, grandmasHelp)).isNull();
    }

    // ---------------------------------------------------------------------------------------------
    // Thanking Grandma

    @Test
    @Order(9)
    @DisplayName("9. Anna takes a picture of the rescued scones (Media)")
    void annaUploadsAPicture() {
        Response uploaded = anna.post("/media/images", """
                {"media": "%s", "links": [{"type": "helpRequest",
                  "url": "https://larder.org/cooking-assistance/help-requests/%s"}]}""".formatted(PNG, helpRequest))
                .expect(201);
        picture = uploaded.location();

        assertThat(anna.get("/media/images/" + uploaded.createdId()).expect(200).path("media").asText()).isEqualTo(PNG);
    }

    @Test
    @Order(10)
    @DisplayName("10. Without her photo consent Anna cannot thank publicly - with it she thanks Grandma (Consent Management, Sharing)")
    void annaThanksGrandma() {
        String thanksToGrandma = """
                {"helpId": "%s", "recipients": [{"type": "GrandmaAvatar"}],
                 "thanksText": "Grandma saved my scones!", "pictures": "%s"}""".formatted(grandmasHelp, picture);
        anna.post("/sharing/thanks", thanksToGrandma).expectError(400, "PICTURE_WITHOUT_CONSENT");

        anna.post("/consent-management/consents", """
                {"subject": "%s", "consentTextId": "%s"}""".formatted(ANNA, PHOTOS_IN_PUBLIC_THANKS)).expect(201);
        String thanks = anna.post("/sharing/thanks", thanksToGrandma).expect(201).createdId();

        assertThat(anna.get("/sharing/thanks/" + thanks).expect(200).path("thanksText").asText())
                .isEqualTo("Grandma saved my scones!");
        anna.post("/sharing/thanks", thanksToGrandma).expectError(400, "THANKS_ALREADY_GIVEN");
    }

    // ---------------------------------------------------------------------------------------------
    // The community: Ben

    @Test
    @Order(11)
    @DisplayName("11. Ben registers and answers Anna's question about folding over REST (community help)")
    void benExplainsAStep() {
        ben.post("/cook-profile/cooks", """
                {"email": "ben@larder.org", "name": "Cook", "givenName": "Ben"}""").expect(201);
        communityRequest = anna.post("/cooking-assistance/help-requests", """
                {"title": "Fold in?", "type": "PREPARATION_STEP_EXPLANATION",
                 "description": "What does fold in mean?", "recipe": "%s", "howToStep": "%s",
                 "preferredProvider": ["COMMUNITY"]}""".formatted(recipe, firstStep)).expect(201).createdId();

        bensHelp = ben.post("/cooking-assistance/helps", """
                {"helpRequest": "%s", "answerTitle": "Gently!", "helpProviderType": "COMMUNITY",
                 "answer": {"answerType": "PREPARATION_STEP_EXPLANATION", "recipe": "%s", "howToStep": "%s",
                            "description": "Lift the dough over the buttermilk with a spatula."}}
                """.formatted(communityRequest, recipe, firstStep)).expect(201).createdId();

        await().atMost(GRANDMA_PATIENCE).pollInterval(Duration.ofMillis(200)).until(() ->
                notificationAbout(anna, bensHelp) != null);
    }

    @Test
    @Order(12)
    @DisplayName("12. Anna may name Ben in her thanks only after Ben consented to being mentioned")
    void annaThanksBenByName() {
        String thanksToBen = """
                {"helpId": "%s", "recipients": [{"type": "Cook", "cooks": ["%s"]}],
                 "thanksText": "Thanks for the folding tip, Ben!", "pictures": "%s"}""".formatted(bensHelp, BEN, picture);
        anna.post("/sharing/thanks", thanksToBen).expectError(400, "MENTION_WITHOUT_CONSENT");

        ben.post("/consent-management/consents", """
                {"subject": "%s", "consentTextId": "%s"}""".formatted(BEN, MENTION_AS_HELPER)).expect(201);

        this.thanksToBen = anna.post("/sharing/thanks", thanksToBen).expect(201).createdId();
    }

    @Test
    @Order(13)
    @DisplayName("13. Ben finds Anna's thanks addressed to him, but cannot delete them")
    void benFindsTheThanks() {
        JsonNode thanks = ben.get("/sharing/thanks?recipient=" + BEN).expect(200).path("thanks");
        assertThat(thanks).anyMatch(found -> found.path("thanksId").asText().equals(thanksToBen));

        ben.delete("/sharing/thanks/" + thanksToBen).expectError(403, "NOT_PERMITTED");
        anna.get("/sharing/thanks/" + thanksToBen).expect(200);
    }

    // ---------------------------------------------------------------------------------------------
    // What the contracts promise when things go wrong

    @Test
    @Order(20)
    @DisplayName("20. Rule violations answer 400 with the documented code")
    void ruleViolations() {
        anna.patch("/meal-preparation/preparations/" + preparation + "/how-to-steps/next?stepId=" + firstStep, null)
                .expectError(400, "STEP_NOT_CURRENT");
        anna.patch("/meal-planning/meal-plans/" + mealPlan, """
                {"courses": [{"step": 1, "meal": {"recipe": "%s"}}]}""".formatted(UUID.randomUUID()))
                .expectError(400, "UNKNOWN_RECIPE");
        anna.post("/sharing/thanks", """
                {"helpId": "%s", "thanksText": "For nothing", "pictures": "%s"}""".formatted(UUID.randomUUID(), picture))
                .expectError(400, "UNKNOWN_HELP");
        anna.post("/consent-management/consents", """
                {"subject": "%s", "consentTextId": "%s"}""".formatted(ANNA, UUID.randomUUID())).expect(400);
    }

    @Test
    @Order(21)
    @DisplayName("21. Without a token every API answers 401")
    void withoutToken() {
        for (String path : List.of("/recipe-catalog/recipes", "/meal-planning/meal-plans", "/cooking-assistance/helps",
                "/notifications/notifications", "/sharing/thanks", "/cook-profile/cooks/" + ANNA,
                "/consent-management/consents?subject=" + ANNA, "/media/images?businessObjectId=" + helpRequest
                        + "&businessObjectType=helpRequest", "/meal-preparation/preparations/" + preparation)) {
            api.anonymous().get(path).expect(401);
        }
    }

    @Test
    @Order(22)
    @DisplayName("22. Another cook's resources are forbidden (403), a missing scope too")
    void foreignResources() {
        ben.get("/meal-planning/meal-plans/" + mealPlan).expectError(403, "NOT_PERMITTED");
        ben.patch("/recipe-catalog/recipes/" + recipe, """
                {"name": "Ben's scones now"}""").expectError(403, "NOT_PERMITTED");
        ben.get("/meal-preparation/preparations/" + preparation).expectError(403, "NOT_PERMITTED");
        ben.post("/consent-management/consents", """
                {"subject": "%s", "consentTextId": "%s"}""".formatted(ANNA, MENTION_AS_HELPER))
                .expectError(403, "NOT_PERMITTED");

        api.as(AccessTokens.forCook(ANNA, List.of("sharing:read")))
                .post("/sharing/thanks", """
                        {"helpId": "%s", "thanksText": "No scope", "pictures": "%s"}""".formatted(grandmasHelp, picture))
                .expect(403);
    }

    @Test
    @Order(23)
    @DisplayName("23. Unknown ids answer 404")
    void unknownIds() {
        UUID unknown = UUID.randomUUID();
        anna.get("/recipe-catalog/recipes/" + unknown).expectError(404, "NOT_FOUND");
        anna.get("/cooking-assistance/help-requests/" + unknown).expectError(404, "NOT_FOUND");
        anna.get("/sharing/thanks/" + unknown).expectError(404, "NOT_FOUND");
        anna.get("/media/images/" + unknown).expectError(404, "NOT_FOUND");
        anna.get("/notifications/notifications/" + unknown).expectError(404, "NOT_FOUND");
    }

    // ---------------------------------------------------------------------------------------------
    // The published language

    @Test
    @Order(30)
    @DisplayName("30. Every message on the wire matches its AsyncAPI contract, none was dead-lettered")
    void everyMessageMatchesItsContract() {
        List<String> types = tap.messages().stream().map(m -> m.exchange() + "/" + m.type()).toList();
        assertThat(types).contains("cooking-assistance/HelpRequested", "grandma-avatar/HelpProvided",
                "cooking-assistance/HelpProvided");

        int validated = tap.assertAllMessagesMatchTheirContracts();

        assertThat(validated).isGreaterThanOrEqualTo(5);
        assertThat(LarderInfrastructure.deadLetterQueues()).isNotEmpty();
        assertThat(LarderInfrastructure.deadLetteredMessages()).isZero();
    }

    @AfterAll
    void report() {
        if (tap != null) {
            System.out.printf("%n== Rescue story: %d HTTP responses validated against OpenAPI %s, %d messages against AsyncAPI, %.1f s%n",
                    api.validatedResponses(), api.validatedPerContract(), tap.messages().size(),
                    (System.nanoTime() - startedAt) / 1e9);
            tap.close();
        }
    }

    // ---------------------------------------------------------------------------------------------

    private List<TappedMessage> journeyOf(String helpRequestId) {
        return tap.messages().stream().filter(m -> m.correlationId().equals(helpRequestId)).toList();
    }

    private static JsonNode notificationAbout(ContractHttp.Caller cook, String helpId) {
        JsonNode notifications = cook.get("/notifications/notifications").expect(200).path("notifications");
        return StreamSupport.stream(notifications.spliterator(), false)
                .filter(n -> n.path("link").asText().endsWith("/cooking-assistance/helps/" + helpId))
                .findFirst()
                .orElse(null);
    }
}
