import { describe, it } from "vitest";

// Inbound HTTP — increment 0, step 9, unit 9.1: op requestHelp (cook-assistance.openapi.yaml 2.0.0 r3) → RequestPreparationStepHelp.
// In-process HTTP against the real NestJS app with the step 8 fakes and a local JWKS; no database.

describe("op requestHelp", () => {
  it.todo("a COOK with write scope asks about Sunday scones step 3: 201 with the HelpRequest body (generated id, requester = token sub, description = the question, pictures [], ingredientId/chefId/plannedMealDate null, REQUESTED, answerDeadline 15:00, createdAt = updatedAt = 10:00, selfLink, helpsLink) and Location /help-requests/{id} -- op requestHelp B2-S01");
  it.todo("the use case receives the requester from the token's sub, never from the body -- op requestHelp ADR-CA-009");
  it.todo("title missing: 400 InvalidRequest Problem, use case not called, nothing saved -- op requestHelp");
  it.todo("topic DESSERT (not in the enum): 400 InvalidRequest Problem, nothing saved -- op requestHelp");
  it.todo("recipeId not-a-uuid (format): 400 InvalidRequest Problem, nothing saved -- op requestHelp ADR-CA-011");
  it.todo("version header missing: 400 InvalidRequest Problem, nothing saved -- op requestHelp");
  it.todo("PREPARATION_STEP without howToStepId (conditional rule): 400 InvalidRequest Problem, use case not called -- op requestHelp INV-17 F9");
  it.todo("addressees []: 400 InvalidRequest Problem, use case not called -- op requestHelp INV-20 F9");
  it.todo("a question of blanks: 422 Problem code QuestionRequired rule INV-19 -- op requestHelp INV-19");
  it.todo("step 9 of Sunday scones: 422 Problem code StepNotInRecipe rule INV-18 -- op requestHelp INV-18");
  it.todo("addressees [CHEF]: 422 Problem code ChefOnlyForMenu rule INV-20 -- op requestHelp INV-20");
  it.todo("topic MENU: 422 Problem code TopicNotYetSupported rule null, use case not called -- op requestHelp F5");
  it.todo("a generated id that is already taken: 409 Problem code ConcurrencyConflict -- op requestHelp ADR-CA-005");
  it.todo("Recipe Catalog unavailable: 503 Problem code ServiceUnavailable with Retry-After -- op requestHelp ADR-CA-015");
  it.todo("no token: 401 Problem code Unauthenticated, use case not called -- op requestHelp ADR-CA-008");
  it.todo("a token signed by another key: 401 Problem code Unauthenticated -- op requestHelp ADR-CA-008");
  it.todo("an expired token: 401 Problem code Unauthenticated -- op requestHelp ADR-CA-008");
  it.todo("a token without cooking-assistance:write: 403 Problem, use case not called -- op requestHelp ADR-CA-009");
  it.todo("a token with role COMMUNITY_COOK only: 403 Problem, use case not called -- op requestHelp ADR-CA-009");
  it.todo("an unexpected error: 500 Problem code InternalError without the internal message -- op requestHelp ADR-CA-006");
});
