ADR0005

# Orchestrator UI

### Adopted 2026-09-10

### Annegret Junker, Architect

## Decision
**We will use an orchestrator for the UI or Larder. It ingests the micro UIs of the Bounded Context and provides an AppShell.**

## Context

**Key Architectural Drivers**

* **Guided Cooking Workflows**: Interactive capabilities like *Step-by-Step Cooking Mode* and the *Dinner Party Planner* depend on strict sequential progression. An orchestrator explicitly drives state transitions, active timers, and hands-free voice controls to ensure UI components remain synchronized while cooking.
* **Catastrophe & Rescue Management**: Responding to a cooking failure (`StepUnclear` or `CatastropheHappened` $\rightarrow$ `TakePictures` $\rightarrow$ `RequestHelp` $\rightarrow$ `MealRescued`) requires conditional modal branching across `Meal Preparation`, `Cooking Assistance`, `Media`, and `Grandma Avatar AI`. A UI orchestrator dictates this multi-context dialog flow and manages real-time fallbacks seamlessly.
* **Unified View Aggregation & Gating**: The primary cooking UI synthesizes live data from the `Recipe Catalog` (ingredients and steps), `Meal Preparation` (active state), and `Cooking Assistance` (AI or Chef responses). Central orchestration aggregates these views cleanly while enforcing rules from `Consent Management` and the `Paywall for Premium Content`.
* **Choreography for Decoupled Extras**: Passive UI elements—such as toast messages from `Notification`, dynamic community feed updates (`Sharing`), and targeted promotions from `Ads Management`—subscribe to events like `MealPrepared` or `ThanksGiven` via lightweight UI choreography to avoid coupling with the main cooking pipeline.

## Options considered

### Orchestrator

We will use an orchestrator so that the AppShell can hold the process status between the micro-UIs, whereas the micro-UIs can be independent.

### Choreography

Centralizing interactive user flows through a UI orchestrator while leveraging event-driven choreography for ambient widgets gives Larder a deterministic, hands-free kitchen experience without compromising the modularity of its social and community features.

## Consequences

| Consequence       | Orchestration                         | Choreography                                               | 
|-------------------|---------------------------------------|------------------------------------------------------------|
| Implementation    | ! Larger effort in AppShell           | ‼️ Extrem high effort to hold the process in each micro UI | 
| Team independence | ‼️ All teams depend on AppShell team. | Fine                                                       | 

## Advice

See meeting protocol
