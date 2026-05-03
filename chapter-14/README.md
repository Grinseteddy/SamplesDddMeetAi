# Chapter 14 — AI-Enhanced REST API and Event Design

**Blueprint step:** Step 12 — Define REST APIs

## Context

Chapter 14 derives the external API surface directly from the domain model produced in Chapter 13.
The AI was given the Recipe Aggregate, the Ingredient Value Object, and the confirmed invariants, and asked to generate an OpenAPI specification that reflects those domain boundaries exactly.
It was then asked to verify the AsyncAPI event schemas (from Chapter 12) for consistency with the Ubiquitous Language and the domain model.

The AI found two inconsistencies:
1. The `sharing.yaml` stub from Chapter 9 used `authorId` in the request body; the domain model makes it clear that the author is determined from the authenticated Cook, not passed in the request.
2. The `RecipeShared` AsyncAPI schema from Chapter 12 was missing the `steps` field, which is part of the domain model.

Both were corrected before sign-off.

## Samples

| File | Description |
|---|---|
| `sharing-api.yaml` | Refined OpenAPI 3.1 for the Sharing context (domain-model-aligned) |
| `events/recipe-shared-v2.yaml` | Corrected AsyncAPI schema for `RecipeShared` (adds `steps`) |

## AI Prompt Used

> You are a DDD expert.
> I will provide the Recipe Aggregate, the Ingredient Value Object, and the confirmed invariants.
> Generate an OpenAPI 3.1 specification for the Sharing bounded context.
> Every endpoint and schema field must be traceable to an aggregate attribute or invariant.
> Do not add fields that are not part of the domain model.
> After generating the OpenAPI spec, review the AsyncAPI schemas from Chapter 12 for consistency with the domain model and list any discrepancies.
