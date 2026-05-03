# ADR 002 — Use Event Sourcing in the Sharing Context

**Status:** Accepted  
**Date:** (see book Chapter 11)  
**Deciders:** CookWithUs architecture team

---

## Context and Problem Statement

The Sharing bounded context manages the lifecycle of a Recipe: draft, published, revised, and (eventually) archived.
Each state transition is driven by a domain event.
The team must decide whether to store current state only (state-based persistence) or to store the full sequence of events (Event Sourcing).

## Decision Drivers

* Recipe revision history is a stated business requirement ("a recipe is a living artefact").
* The team wants an audit trail for moderation purposes.
* The Sharing context produces events consumed by the Rating and Ordering contexts.
* The Registration context has no such history requirement.

## Considered Options

1. **State-based persistence** — store the latest Recipe state in a relational table.
2. **Event Sourcing** — store every `RecipeDrafted`, `RecipePublished`, `RecipeRevised` event; derive current state by replaying.

## Decision Outcome

**Chosen option: Event Sourcing for the Sharing context only.**

Event Sourcing is adopted in the Sharing context because the full revision history is a first-class business requirement and because Sharing is the primary producer of domain events for downstream contexts.
Registration uses state-based persistence; its history requirements do not justify Event Sourcing overhead.

### Positive Consequences

* Full audit trail for recipe lifecycle at no extra modelling cost.
* Downstream contexts (Rating, Ordering) receive events directly from the event store.
* Temporal queries ("what did this recipe look like last Tuesday?") are answerable without extra tables.

### Negative Consequences

* Event schema evolution requires explicit versioning and upcasting.
* Querying current state requires a read model (CQRS projection).
* The team needs to learn Event Sourcing patterns before implementation begins.

## AI Recommendation Summary

> Recipe revision history and downstream event consumption make Event Sourcing a natural fit for the Sharing context.
> The Registration context has no analogous requirement; applying Event Sourcing there would add complexity without benefit.
> I recommend scoping Event Sourcing to Sharing and using standard state-based persistence everywhere else.
