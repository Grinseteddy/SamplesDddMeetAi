import { afterAll, beforeAll, beforeEach, describe, expect, expectTypeOf, it } from "vitest";
import type { operations, paths } from "../../../../generated/openapi/recipe-catalog.js";
import { RecipeCatalogUnavailable } from "../../../application/ports/recipe-catalog.js";
import { FixedClock } from "../../../test-support/fakes/fixed-clock.js";
import { sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import { StubRecipeCatalog } from "../../../test-support/http/stub-recipe-catalog.js";
import { CircuitBreaker } from "./circuit-breaker.js";
import { HttpRecipeCatalog } from "./http-recipe-catalog.js";
import { NoTokenYet } from "./no-token-yet.js";

// Recipe Catalog lookup — increment 0, step 10, unit 10.5b: the RecipeCatalog port over HTTP (ADR-CA-015, amended),
// built-in fetch typed by the generated upstream paths; a stub HTTP server plays Recipe Catalog 1.0.0
// (contracts/upstream/recipe-catalog.openapi.yaml), a fixed TokenProvider, a settable clock.

const NOW = new Date("2026-10-25T10:00:00Z");
const SERVICE_TOKEN = "cook-assistance-service-token";
/** Step 3 of Sunday scones as Recipe Catalog returns it (HowToStep). */
const STEP_3 = {
  howToStepId: sundaySconesStep3.howToStepId,
  sequenceNumber: 3,
  description: "Leave the dough somewhere warm for 30 minutes.",
};

function lookUpStep3(catalog: HttpRecipeCatalog): Promise<unknown> {
  return catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
}

describe("op getRecipeHowToStepById (Recipe Catalog 1.0.0) — HttpRecipeCatalog", () => {
  let upstream: StubRecipeCatalog;
  let catalog: HttpRecipeCatalog;

  beforeAll(async () => {
    upstream = await StubRecipeCatalog.start();
  });

  beforeEach(() => {
    upstream.reset();
    catalog = new HttpRecipeCatalog({
      baseUrl: upstream.baseUrl,
      tokenProvider: { accessToken: async () => SERVICE_TOKEN },
      breaker: new CircuitBreaker(new FixedClock(NOW)),
    });
  });

  afterAll(async () => {
    await upstream.stop();
  });

  it("200 HowToStep: findHowToStep returns Sunday scones step 3 (recipeId, howToStepId) -- ADR-CA-015 INV-18", async () => {
    upstream.respondWith(200, STEP_3);

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).resolves.toEqual({
      recipeId: sundaySconesStep3.recipeId,
      howToStepId: sundaySconesStep3.howToStepId,
    });
  });
  it("the request: GET {RECIPE_CATALOG_URL}/recipes/{recipeId}/how-to-steps/{howToStepId} with version 1.0.0, Accept application/json and Authorization Bearer from the TokenProvider -- ADR-CA-015", async () => {
    upstream.respondWith(200, STEP_3);

    await catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);

    expect(upstream.requests).toEqual([
      {
        method: "GET",
        url: `/recipe-catalog/recipes/${sundaySconesStep3.recipeId}/how-to-steps/${sundaySconesStep3.howToStepId}`,
        headers: expect.objectContaining({
          version: "1.0.0",
          accept: "application/json",
          authorization: `Bearer ${SERVICE_TOKEN}`,
        }),
      },
    ]);
  });
  it("404: undefined, so the use case rejects with StepNotInRecipe -- ADR-CA-015 INV-18", async () => {
    upstream.respondWith(404, { code: "NOT_FOUND", message: "No such how-to step" });

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).resolves.toBeUndefined();
  });
  it("500: RecipeCatalogUnavailable after exactly one request (only network errors are retried) -- ADR-CA-015", async () => {
    upstream.respondWith(500, { code: "SERVICE_NOT_AVAILABLE", message: "Try again later" });

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).rejects.toThrow(
      RecipeCatalogUnavailable,
    );
    expect(upstream.requests).toHaveLength(1);
  });
  it.each([401, 403])("%i (our token refused): RecipeCatalogUnavailable, never undefined -- ADR-CA-015 ADR-CA-008", async (status) => {
    upstream.respondWith(status, { code: "NOT_AUTHORIZED", message: "Token refused" });

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).rejects.toThrow(
      RecipeCatalogUnavailable,
    );
  });
  it("no answer within 150 ms: RecipeCatalogUnavailable, not retried (the retry would spend the p95 budget) -- ADR-CA-015 QS-08", async () => {
    upstream.respondWith(200, STEP_3);
    upstream.delayBy(300);

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).rejects.toThrow(
      RecipeCatalogUnavailable,
    );
    expect(upstream.requests).toHaveLength(1);
  });
  it("a network error, then 200: the step, after exactly 2 requests (one retry) -- ADR-CA-015", async () => {
    upstream.respondWith(200, STEP_3);
    upstream.dropNext(1);

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).resolves.toEqual({
      recipeId: sundaySconesStep3.recipeId,
      howToStepId: sundaySconesStep3.howToStepId,
    });
    expect(upstream.requests).toHaveLength(2);
  });
  it("a network error twice: RecipeCatalogUnavailable after exactly 2 requests -- ADR-CA-015", async () => {
    upstream.respondWith(200, STEP_3);
    upstream.dropNext(2);

    await expect(catalog.findHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId)).rejects.toThrow(
      RecipeCatalogUnavailable,
    );
    expect(upstream.requests).toHaveLength(2);
  });
  it("5 failed lookups within 30 s: the 6th makes no request and fails with RecipeCatalogUnavailable; 404s do not count as failures -- ADR-CA-015", async () => {
    upstream.respondWith(500, { code: "SERVICE_NOT_AVAILABLE", message: "Try again later" });
    for (let lookup = 1; lookup <= 6; lookup += 1) {
      await expect(lookUpStep3(catalog)).rejects.toThrow(RecipeCatalogUnavailable);
    }
    expect(upstream.requests).toHaveLength(5);

    upstream.reset();
    upstream.respondWith(404, { code: "NOT_FOUND", message: "No such how-to step" });
    const unopened = new HttpRecipeCatalog({
      baseUrl: upstream.baseUrl,
      tokenProvider: { accessToken: async () => SERVICE_TOKEN },
      breaker: new CircuitBreaker(new FixedClock(NOW)),
    });
    for (let lookup = 1; lookup <= 6; lookup += 1) {
      await expect(lookUpStep3(unopened)).resolves.toBeUndefined();
    }
    expect(upstream.requests).toHaveLength(6);
  });
  it("with NoTokenYet, every Recipe Catalog lookup is RecipeCatalogUnavailable and no request is sent -- ADR-CA-015 amendment", async () => {
    upstream.respondWith(200, STEP_3);
    const withoutToken = new HttpRecipeCatalog({
      baseUrl: upstream.baseUrl,
      tokenProvider: new NoTokenYet(),
      breaker: new CircuitBreaker(new FixedClock(NOW)),
    });

    await expect(lookUpStep3(withoutToken)).rejects.toThrow(RecipeCatalogUnavailable);
    expect(upstream.requests).toEqual([]);
  });
  it("gate: getRecipeHowToStepById exists in the pinned upstream spec with the path and version header the adapter uses -- ADR-CA-015", () => {
    // checked by tsc against the types generated from contracts/upstream/recipe-catalog.openapi.yaml
    expectTypeOf<paths["/recipes/{recipeId}/how-to-steps/{howToStepId}"]["get"]>().toEqualTypeOf<operations["getRecipeHowToStepById"]>();
    expectTypeOf<operations["getRecipeHowToStepById"]["parameters"]["header"]["version"]>().toEqualTypeOf<"1.0.0">();
  });
});
