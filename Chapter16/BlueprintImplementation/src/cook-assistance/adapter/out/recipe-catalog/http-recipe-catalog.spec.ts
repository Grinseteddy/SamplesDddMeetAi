import { describe, it } from "vitest";

// Recipe Catalog lookup — increment 0, step 10, unit 10.5b: the RecipeCatalog port over HTTP (ADR-CA-015, amended),
// built-in fetch typed by the generated upstream paths; a stub HTTP server plays Recipe Catalog 1.0.0
// (contracts/upstream/recipe-catalog.openapi.yaml), a fixed TokenProvider, a settable clock.

describe("op getRecipeHowToStepById (Recipe Catalog 1.0.0) — HttpRecipeCatalog", () => {
  it.todo(
    "200 HowToStep: findHowToStep returns Sunday scones step 3 (recipeId, howToStepId) -- ADR-CA-015 INV-18",
  );
  it.todo(
    "the request: GET {RECIPE_CATALOG_URL}/recipes/{recipeId}/how-to-steps/{howToStepId} with version 1.0.0, Accept application/json and Authorization Bearer from the TokenProvider -- ADR-CA-015",
  );
  it.todo(
    "404: undefined, so the use case rejects with StepNotInRecipe -- ADR-CA-015 INV-18",
  );
  it.todo(
    "500: RecipeCatalogUnavailable after exactly one request (only network errors are retried) -- ADR-CA-015",
  );
  it.todo(
    "401 or 403 (our token refused): RecipeCatalogUnavailable, never undefined -- ADR-CA-015 ADR-CA-008",
  );
  it.todo(
    "no answer within 150 ms: RecipeCatalogUnavailable, not retried (the retry would spend the p95 budget) -- ADR-CA-015 QS-08",
  );
  it.todo(
    "a network error, then 200: the step, after exactly 2 requests (one retry) -- ADR-CA-015",
  );
  it.todo(
    "a network error twice: RecipeCatalogUnavailable after exactly 2 requests -- ADR-CA-015",
  );
  it.todo(
    "5 failed lookups within 30 s: the 6th makes no request and fails with RecipeCatalogUnavailable; 404s do not count as failures -- ADR-CA-015",
  );
  it.todo(
    "gate: getRecipeHowToStepById exists in the pinned upstream spec with the path and version header the adapter uses -- ADR-CA-015",
  );
});
