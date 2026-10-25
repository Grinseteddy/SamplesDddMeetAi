import { HowToStepId, RecipeId } from "../../domain/shared/ids.js";
import { sundaySconesStep3 } from "../../test-support/fixtures/sunday-scones.js";

// Example Mapping "Help for a preparation step": Sunday scones has 8 steps; step 3 is "Let dough rest in a warm place".

export const SUNDAY_SCONES: RecipeId = sundaySconesStep3.recipeId;

/** The ids of the recipe's 8 steps, step 3 being the fixture's; a step the recipe does not have gets an id too. */
const STEP_IDS = Array.from({ length: 10 }, (_, index) =>
  HowToStepId.of(`7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9${String(index + 1).padStart(3, "0")}`),
);
STEP_IDS[2] = sundaySconesStep3.howToStepId;

export function stepOfSundayScones(number: number): HowToStepId {
  return STEP_IDS[number - 1] as HowToStepId;
}

/** Recipe Catalog's path of a step (contracts/upstream/recipe-catalog.openapi.yaml, server path /recipe-catalog). */
export function stepPath(number: number): string {
  return `/recipe-catalog/recipes/${SUNDAY_SCONES}/how-to-steps/${stepOfSundayScones(number)}`;
}
