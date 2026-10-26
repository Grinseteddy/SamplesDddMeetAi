import { HelpRequestId } from "../../domain/help-request/help-request.js";
import { HelpId } from "../../domain/help/help.js";
import { CookId, HowToStepId, RecipeId } from "../../domain/shared/ids.js";

// Example Mapping Preparation step: Sunday scones, step 3 "Let dough rest in a warm place".

/** The Cook asks what "warm" means — the command that requests the help (B2-S01). */
export const sundaySconesStep3 = {
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000001"),
  requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
  title: "Sunday scones, step 3",
  question: "What means 'warm'?",
  recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
  howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
  addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
  requestedAt: new Date("2026-10-24T10:00:00Z"),
} as const;

/** Grandma Avatar explains what "warm" means — the command that provides the help (B1-S04). */
export const grandmaExplainsWarm = {
  id: HelpId.of("0192f1a0-0000-7000-8000-0000000000a1"),
  helpRequest: {
    id: sundaySconesStep3.id,
    requester: sundaySconesStep3.requester,
    topic: "PREPARATION_STEP",
    recipeId: sundaySconesStep3.recipeId,
    howToStepId: sundaySconesStep3.howToStepId,
  },
  answerTitle: "Warm means about 30 °C",
  provider: { type: "GRANDMA_AVATAR", id: null },
  answer: {
    topic: "PREPARATION_STEP",
    recipeId: sundaySconesStep3.recipeId,
    howToStepId: sundaySconesStep3.howToStepId,
    description: "Put the bowl next to the heater or in the oven with only the light on.",
    images: [],
  },
  providedAt: new Date("2026-10-24T12:00:00Z"),
} as const;
