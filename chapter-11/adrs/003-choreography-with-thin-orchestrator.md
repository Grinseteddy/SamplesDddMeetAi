# ADR 003 — Choreography with a Thin Orchestrator for Cross-Context Flows

**Status:** Accepted  
**Date:** (see book Chapter 11)  
**Deciders:** CookWithUs architecture team

---

## Context and Problem Statement

CookWithUs has cross-context flows: when a Recipe is published, the Rating context must initialise a rating record, and the Ordering context must make the ingredient list available for shopping.
The team must choose how to coordinate these flows: pure choreography (event-driven, no central coordinator) or orchestration (a coordinator service drives each step).

## Decision Drivers

* The team values loose coupling between bounded contexts.
* Cross-context failure scenarios must be observable and recoverable.
* The Modular Monolith decision (ADR 001) means all modules share one process; a separate orchestrator service is premature.

## Considered Options

1. **Pure choreography** — each context subscribes to events and reacts independently; no coordinator.
2. **Central orchestration** — a dedicated Saga or Process Manager coordinates every cross-context flow.
3. **Choreography with a thin orchestrator** — events drive the happy path; a lightweight Saga handles compensation and failure recovery.

## Decision Outcome

**Chosen option: Choreography with a thin orchestrator.**

The happy path for recipe publication uses choreography: `RecipePublished` triggers independent reactions in Rating and Ordering.
A thin Saga in the Sharing module handles compensation when a downstream reaction fails (e.g., if the Ordering context cannot process the ingredient list, the Recipe publication is not rolled back but the failure is recorded and surfaced for manual resolution).

### Positive Consequences

* Bounded contexts remain loosely coupled on the happy path.
* Failure paths are explicit, observable, and recoverable.
* The Saga lives inside the Sharing module; no new deployable artefact is needed.

### Negative Consequences

* Developers must understand both choreography and Saga patterns.
* The Saga adds coordination logic that must be tested for all failure scenarios.

## AI Recommendation Summary

> Pure choreography distributes failure handling across all contexts, making failures invisible.
> Central orchestration creates a coupling point that contradicts the bounded-context model.
> A thin orchestrator scoped to the Sharing module handles failure recovery without introducing a new deployable component.
> I recommend choreography on the happy path with a Saga in Sharing for compensation.
