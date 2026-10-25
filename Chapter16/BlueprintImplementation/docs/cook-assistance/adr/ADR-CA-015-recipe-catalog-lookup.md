# ADR-CA-015 — Synchronous Recipe Catalog lookup (acknowledged override of AP0002)

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 15

## Context
INV-18, INV-22 and INV-23 require that a named step or ingredient exists in the recipe. Recipes belong
to Recipe Catalog, which offers an OHS REST API (Chapter 13 `recipe-catalog.openapi.yaml`) and publishes
no recipe events. Principle AP0002 prefers asynchronous communication between bounded contexts.
QS-08 gives commands a p95 of 300 ms.

## Decision
- An outbound adapter `adapter/out/recipe-catalog` behind a `RecipeCatalog` port
  (`findHowToStep(recipeId, howToStepId)`, `findIngredient(recipeId, ingredientId)`).
- Typed client generated with `openapi-typescript` + `openapi-fetch` from Recipe Catalog's OpenAPI
  (pinned copy in `contracts/upstream/recipe-catalog.openapi.yaml`); calls
  `GET /recipes/{recipeId}/how-to-steps/{howToStepId}` and `GET /recipes/{recipeId}/ingredients/{ingredientId}`.
- **Timeout 150 ms**, **1 retry** on network errors, **circuit breaker** (opens after 5 failures in 30 s,
  half-open after 30 s). 404 from Recipe Catalog → the rule rejection (`StepNotInRecipe`,
  `IngredientNotInRecipe`); unavailable/open breaker → **503** Problem with `Retry-After`.
- The request keeps the recipe, step and ingredient IDs as asked; later recipe edits do not change it.

## Consequences
- **Overrides AP0002 — acknowledged** for this lookup: the cook waits for the answer, and Recipe Catalog
  offers no events to replicate. Revisit when Recipe Catalog publishes recipe events (then a local
  replica honors AP0002). Offered for ingest into the decision graph as an acknowledged override.
- requestHelp depends on Recipe Catalog's availability for the three non-Menu topics.

## Quality scenarios
Serves INV-18/22/23 and QS-08; trades off QS-05-style resilience (no fallback for this check).

## Amendment — 2026-10-25 (step 10, unit 10.5, user decisions)
- **Client:** Node's built-in `fetch` instead of `openapi-fetch` — no new runtime dependency. Types come from
  `openapi-typescript` 7.13.0 (`tools/codegen`, script `generate:upstream`) into
  `src/generated/openapi/recipe-catalog.ts`; the adapter types its request and response against the generated
  `paths`. The pinned copy `contracts/upstream/recipe-catalog.openapi.yaml` is byte-identical to Chapter 13's
  1.0.0 (sha256 `9e3e695c…f8f3b3cc`); a new upstream version is copied deliberately, never fetched.
- **Upstream token:** Recipe Catalog requires OAuth2 (`recipe:read`). The cook's token is issued for the
  `cook-assistance` audience and is **not** forwarded. The adapter asks a `TokenProvider` port for a bearer
  token; the production provider (Keycloak client-credentials grant, client ID and secret from the
  environment) is an **open item for steps 13/14** — until then the walking skeleton cannot call a secured
  Recipe Catalog in production.
- **Circuit breaker:** hand-written, time from the `Clock` port (ADR-CA-007), so its tests are deterministic.
- **Header:** every call sends `version: 1.0.0` (required by the upstream spec).
