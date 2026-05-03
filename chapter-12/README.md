# Chapter 12 — Services, APIs, and the Bounded Context Canvas

**Blueprint step:** Step 8 — Explore the API and user journey / define services

## Context

Chapter 12 closes Solution Design by producing service definitions, API Product Canvases, and AsyncAPI event schemas from all upstream artifacts.
Every field is traceable to a sticky note in the EventStorming output or a term in the Visual Glossary.

The AI was given the Context Map, the Bounded Context Canvases, and the OpenAPI stubs from Chapter 9.
It produced AsyncAPI event schema definitions for the four domain events that cross bounded-context boundaries.

## Samples

| File | Description |
|---|---|
| `events/recipe-shared.yaml` | AsyncAPI schema for the `RecipeShared` event |
| `events/recipe-rated.yaml` | AsyncAPI schema for the `RecipeRated` event |
| `events/ingredient-ordered.yaml` | AsyncAPI schema for the `IngredientOrdered` event |
| `events/supplier-notified.yaml` | AsyncAPI schema for the `SupplierNotified` event |

## AI Prompt Used

> You are a DDD expert specialising in event-driven architecture.
> I will provide the Context Map and the EventStorming sticky notes for CookWithUs.
> For each event that crosses a bounded-context boundary, produce an AsyncAPI 3.0 schema.
> Use only terms from the Ubiquitous Language.
> Include the payload schema, the channel, and the producing and consuming bounded contexts.
