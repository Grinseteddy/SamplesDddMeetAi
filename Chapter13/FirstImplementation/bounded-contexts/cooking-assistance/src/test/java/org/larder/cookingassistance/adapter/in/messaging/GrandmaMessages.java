package org.larder.cookingassistance.adapter.in.messaging;

import java.util.UUID;

/** HelpProvided messages as the Grandma Avatar publishes them (grandma-avatar.asyncapi.yaml). */
final class GrandmaMessages {

    static final String STAY_CALM = """
            {"helpId":"a9caf90d-00b4-4a66-8184-7d02152e8d6a",
             "helpRequest":"23a8eeed-35f6-460b-892e-7bb458a8fded",
             "helpRequester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074",
             "answerTitle":"Stay calm",
             "helpProviderType":"GRANDMA_AVATAR",
             "answer":{"answerType":"STEPS_TO_MITIGATE_CATASTROPHE",
                       "catastropheMitigation":{"answerType":"STEPS_TO_MITIGATE_CATASTROPHE",
                                                "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
                                                "explanation":"use a new, cold pan"}}}
            """;

    static final String YOGHURT = """
            {"helpId":"%s",
             "helpRequest":"0b6f3c1e-8f3a-4f0e-9a51-3c2d1e7f9a10",
             "helpRequester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074",
             "answerTitle":"Use yoghurt",
             "helpProviderType":"GRANDMA_AVATAR",
             "answer":{"answerType":"INGREDIENT_SUBSTITUTE",
                       "substitutes":{"answerType":"INGREDIENT_SUBSTITUTE",
                                      "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
                                      "substitute":[{"ingredient":"8956b2e5-8d0c-470e-ae9c-6071bd7c5b0d",
                                                     "substituteIngredient":{"name":"Yoghurt","value":250,"unit":"MILLILITER"}}]}}}
            """.formatted(UUID.randomUUID());

    static final String COLD_PAN = """
            {"helpId":"%s",
             "helpRequest":"5e2d7a90-1c4b-4e8d-b3f6-9a0c2d4e6f81",
             "helpRequester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074",
             "answerTitle":"Fold gently",
             "helpProviderType":"GRANDMA_AVATAR",
             "answer":{"answerType":"PREPARATION_STEP_EXPLANATION",
                       "preparationStepExplanation":{"answerType":"PREPARATION_STEP_EXPLANATION",
                                                     "recipe":"7cf09822-77a1-46bb-812f-b7852bca0913",
                                                     "howToStep":"65610dee-fb83-4341-a730-26a4a99a621a",
                                                     "description":"Use a cold pan",
                                                     "images":["https://larder.org/media/images/b009a5d1-0205-4b0c-af82-822229cf243a"]}}}
            """.formatted(UUID.randomUUID());

    static final String MENU = """
            {"helpId":"%s",
             "helpRequest":"9c1a4e57-2b8d-4f63-a0e9-7d5b3c1f2e48",
             "helpRequester":"f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074",
             "answerTitle":"A menu",
             "helpProviderType":"GRANDMA_AVATAR",
             "answer":{"answerType":"MENU_PROPOSAL",
                       "menuProposal":{"answerType":"MENU_PROPOSAL","note":"Be careful, prepare everything","servings":6,
                                       "meal":"DINNER","howToServe":"hold course 2 warm while serving soup",
                                       "course":[{"step":1,"meal":{"recipe":"7cf09822-77a1-46bb-812f-b7852bca0913"}}]}}}
            """.formatted(UUID.randomUUID());

    private GrandmaMessages() {
    }
}
