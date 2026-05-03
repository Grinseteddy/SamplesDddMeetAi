# ADR 001 — Deploy CookWithUs as a Modular Monolith

**Status:** Accepted  
**Date:** (see book Chapter 11)  
**Deciders:** CookWithUs architecture team

---

## Context and Problem Statement

CookWithUs has four bounded contexts (Registration, Sharing, Rating, Ordering).
The team must choose a deployment topology before proceeding to service implementation.
The two realistic options are a Modular Monolith and an immediately distributed microservice architecture.

## Decision Drivers

* The team size is small (< 10 engineers).
* Operational complexity must stay low at launch.
* The bounded contexts are well-defined but their interaction volumes are unknown.
* Time-to-market is a primary constraint.

## Considered Options

1. **Modular Monolith** — one deployable artefact, modules enforced by package/module boundaries and tested independently.
2. **Distributed Microservices** — one deployable artefact per bounded context from day one.

## Decision Outcome

**Chosen option: Modular Monolith.**

The bounded contexts are implemented as independent modules within a single deployable unit.
Each module owns its own package hierarchy, its own persistence schema, and communicates with other modules exclusively via published interfaces — never by reaching into another module's internals.
Module boundaries are enforced by ArchUnit rules in the test suite.

When load or organisational data justifies extraction, any module can be promoted to a standalone service with minimal change because its boundaries are already clean.

### Positive Consequences

* Deployment pipeline stays simple; one artefact, one database, one observability setup.
* Developers can run the entire application locally without orchestration tooling.
* Refactoring across module boundaries remains cheap.

### Negative Consequences

* A poorly scoped module could create coupling that is hard to untangle later.
* All modules must be deployed together; a bug in one module requires redeploying all.

## AI Recommendation Summary

> The team is small, the domain is new, and inter-context call volumes are unknown.
> Distributing prematurely adds operational overhead without a clear benefit.
> A Modular Monolith with enforced internal boundaries preserves the option to extract later.
> I recommend starting with a Modular Monolith and revisiting when a bounded context shows distinct scaling or deployment requirements.
