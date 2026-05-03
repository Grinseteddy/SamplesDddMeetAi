# Chapter 11 — AI-Driven Architecture Decisions

**Blueprint step:** Step 9 — Define services and architectural patterns

## Context

Chapter 11 shows how AI makes architecture trade-offs explicit.
The team fed the Context Map and EventStorming output to the AI and asked it to reason through three key decisions: deployment topology, CQRS / Event Sourcing scope, and inter-service communication style.
The AI produced structured trade-off analyses and drafted Architecture Decision Records (ADRs).
The team accepted two AI recommendations and rejected one (Event Sourcing for the Registration context).

## Samples

| File | Decision |
|---|---|
| `adrs/001-modular-monolith.md` | Deploy CookWithUs as a Modular Monolith |
| `adrs/002-event-sourcing-for-sharing.md` | Use Event Sourcing in the Sharing context |
| `adrs/003-choreography-with-thin-orchestrator.md` | Choreography with a thin orchestrator for cross-context flows |

## AI Prompt Used

> You are a software architect with deep DDD experience.
> I will provide the Context Map and EventStorming output for CookWithUs.
> For each of the following decisions, present a structured trade-off analysis and then recommend a course of action with explicit reasoning:
> 1. Modular Monolith vs. distributed microservices
> 2. Where to apply CQRS and Event Sourcing
> 3. Choreography vs. Orchestration for cross-context workflows
> Format each recommendation as an Architecture Decision Record in MADR format.
