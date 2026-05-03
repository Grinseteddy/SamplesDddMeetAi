# Beyond the Model — Code Samples

> **Companion repository for the book *Beyond the Model: AI-Enhanced Software Design and Architecture* by Annegret Junker.**

All samples are collected here. They will go public upon the book's finalization.

---

## About the Book

*Beyond the Model* shows how Domain-Driven Design and AI can work together to guide a software team from business intent to running software.
The process is structured around the **Synergetic Blueprint** — a fourteen-step workflow that spans Strategic Design, Solution Design, and Tactical Design — and is illustrated end-to-end using **CookWithUs**, a fictional recipe-sharing platform.

Each chapter introduces a new Blueprint step and shows how an AI assistant can support it:
suggesting domain events, proposing bounded contexts, drafting OpenAPI and AsyncAPI contracts, co-creating the domain model, and generating service and repository scaffolding — while the human team retains every architectural and domain decision.

---

## Running Example — CookWithUs

CookWithUs lets home cooks share and discover recipes.
Home cooks can publish recipes, browse a community feed, rate and comment on recipes they have tried, and receive ingredient suggestions.
The platform is intentionally rich enough to surface real DDD questions: authorship, attribution, moderation, and community trust.

The bounded contexts developed through the book are:

| Bounded Context | Responsibility |
|---|---|
| **Registration** | Cook sign-up, identity, and profile management |
| **Sharing** | Recipe authorship, publication, and revision |
| **Rating** | Community ratings, comments, and photo/video uploads |
| **Ordering** | Ingredient shopping-list generation and supplier integration |

---

## Repository Structure

Samples are organized by the chapter in which they are introduced.
Chapters that produce only discussion artifacts (diagrams, canvases, workshop outputs) are not represented here; only chapters that produce artefacts you can run, import, or extend.

```
chapter-09/          OpenAPI specifications drafted in Chapter 9
chapter-11/          Architecture Decision Records from Chapter 11
chapter-12/          AsyncAPI event schemas from Chapter 12
chapter-13/          Domain model (aggregates, entities, value objects) from Chapter 13
chapter-14/          Refined OpenAPI + AsyncAPI contracts from Chapter 14
chapter-15/          EXACT coding examples and tests from Chapter 15
chapter-16/          Service and repository implementations from Chapter 16
```

---

## Chapter Overview

### Part III — Domain Discovery

| Chapter | Title | Samples |
|---|---|---|
| 9 | Augmented Event Modeling and API Journey | OpenAPI stubs for Registration, Sharing, and Rating |

### Part IV — Solution Design

| Chapter | Title | Samples |
|---|---|---|
| 11 | AI-Driven Architecture Decisions | Architecture Decision Records |
| 12 | Services, APIs, and the Bounded Context Canvas | AsyncAPI event schemas |

### Part V — Tactical Design and Implementation

| Chapter | Title | Samples |
|---|---|---|
| 13 | Domain Model Co-creation with AI | Java domain model |
| 14 | AI-Enhanced REST API and Event Design | Refined OpenAPI and AsyncAPI contracts |
| 15 | From Example Mapping to Exact Coding | EXACT test suite |
| 16 | Service and Repository Implementation Using AI | Sharing service and RecipeRepository |

---

## How to Use the Samples

Each chapter folder contains a `README.md` that explains the context, the AI prompts used, and how the artefact fits into the Blueprint.
Code samples are self-contained and use standard tooling:

* **OpenAPI / AsyncAPI** — import into any compatible tool (Swagger UI, AsyncAPI Studio, Postman, …)
* **Java** — standard Maven project layout; requires Java 21+
* **ADRs** — plain Markdown; compatible with [adr-tools](https://github.com/npryce/adr-tools)

---

## License

© Annegret Junker. All rights reserved until publication.
The samples in this repository will be released under an open-source licence upon book finalization.
