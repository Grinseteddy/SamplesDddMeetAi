# ADR & Principles — Larder

**Mode: seed.** One principles file, five full-form ADRs and one seven-row small-ADR log were ingested. There was no existing graph in the supplied workspace.

## What went in
7 source artifacts; 9 principles; 12 decisions; 11 questions.

## Principles Register

| ID | Principle | Statement | Testable |
|---|---|---|---|
| AP0001 | Monolith before Microservices | We will always start with a deployment monolith before cutting services out. However, the modules are modular regarding DDD principles. | true |
| AP0002 | Asynchronous Communication before Synchronous | For communication between Bounded Contexts asynchronous communication is preferred. | true |
| AP0003 | Using DDD | For design of our functional architecture, we will apply the principles of DDD. | true |
| AP0004 | Using Residuality | For design of our technical architecture, we will apply the principles of Residuality Theory. | true |
| AP0005 | Micro-UIs | The Bounded Context publish their own UI as micro-UIs. | true |
| AP0005 | Automatic Testing | We can test our system by 95% automatically. | true |
| AP0006 | Synchronous communication for UIS | UI communication is done synchronously. | true |
| AP0007 | Monitoring | The system can be monitored to identify system-critical states reliable and fast. | true |
| AP0008 | Fine grained access rights | Domain artifacts are controlled by a fine-grained access control. | true |

## Decision Ledger

| ID | Title | Status | Date | Author | Decision | Source |
|---|---|---|---|---|---|---|
| ADR0001 | Using Monolith | Adopted | 2026-09-10 | Annegret Junker, Architect | We will implement Larder as Monolith. | full-form |
| ADR0002 | Using One Database Instance | Adopted | 2026-09-10 | Annegret Junker, Architect | We will use one database instance for Larder. | full-form |
| ADR0003 | Using Asynchronous Communication | Adopted | 2026-09-10 | Annegret Junker, Architect | We will use asynchronous communication with RabbitMQ. | full-form |
| ADR0004 | Synchronous Communication per UI | Adopted | 2026-09-10 | Annegret Junker, Architect | We will use synchronous communication for User Interfaces. | full-form |
| ADR0005 | Orchestrator UI | Adopted | 2026-09-10 | Annegret Junker, Architect | We will use an orchestrator for the UI or Larder. It ingests the micro UIs of the Bounded Context and provides an AppShell. | full-form |
| SADR0001 | Chef needs to be accessed exclusively | Adopted | 2026-09-07 | Junker | Chef needs to be accessed exclusively | small-ADR |
| SADR0002 | Meal Plan | Superseded | 2026-09-08 | Junker | Meal plan is used for Menu and Meal plan | small-ADR |
| SADR0003 | Menu | Adopted | 2026-09-09 | Junker | Menu is used for Menu and Meal plan | small-ADR |
| SADR0004 | Help | Adopted | 2026-09-11 | Junker | Help response / Help request | small-ADR |
| SADR0005 | Visual Glossary with Legend | Adopted | 2026-09-11 | Junker | Each Visual Glossary needs a legend about the meaning of icons and colors | small-ADR |
| SADR0006 | Brainstorming with Legend | Adopted | 2026-09-11 | Junker | Each Brainstorming result needs a legend about the meaning of icons and colors | small-ADR |
| SADR0007 | Capability Map with Legend | Adopted | 2026-09-11 | Junker | The Capability Map needs a legend | small-ADR |

SADR0003 explicitly supersedes SADR0002; they share a question, but only SADR0003 is active. The full-form ADRs answer distinct technical questions, so no pair of active decisions answers the same question differently.

## Compliance Matrix

| Decision | Honors | Overrides | Cites |
|---|---|---|---|
| D_ADR0001 | Prin_AP0003 (incidental), Prin_AP0001 (explicit) | — | Prin_AP0001 |
| D_ADR0002 | Prin_AP0003 (incidental), Prin_AP0001 (explicit) | — | Prin_AP0001 |
| D_ADR0003 | Prin_AP0002 (explicit) | — | Prin_AP0002 |
| D_ADR0004 | Prin_AP0006 (explicit), Prin_AP0008 (by-elimination) | — | Prin_AP0008, Prin_AP0006 |
| D_ADR0005 | Prin_AP0003 (incidental), Prin_MicroUIs (incidental) | — | — |
| D_SADR0001 | — | — | — |
| D_SADR0002 | — | — | — |
| D_SADR0003 | — | — | — |
| D_SADR0004 | Prin_AP0003 (incidental) | — | — |
| D_SADR0005 | — | — | — |
| D_SADR0006 | — | — | — |
| D_SADR0007 | — | — | — |

Silent and out-of-scope pairs have no assertion in the graph; absence is not a conflict.

## Conflicts, in full

No principle override is evidenced by these decisions. ADR0001 is ambiguous about *which kind* of monolith it selected; resolve its operative sentence before treating modular boundaries as settled.

## Governance gaps

**No decision engages (3):** Prin_AP0004; Prin_AP0007; Prin_Testing.

**Honored without an explicit citation (2):** Prin_AP0003; Prin_MicroUIs.

**Decisions with no principle citation (8):** D_ADR0005 (engages principles incidentally); D_SADR0001 (engages no principle at all); D_SADR0002 (engages no principle at all); D_SADR0003 (engages no principle at all); D_SADR0004 (engages principles incidentally); D_SADR0005 (engages no principle at all); D_SADR0006 (engages no principle at all); D_SADR0007 (engages no principle at all).

**Source defects:**

- Prin_AP0007: The Monitoring implications line is identical to Micro-UIs implications: "Using a large monolithic frontend would us slow down." It does not explain monitoring.
- D_ADR0001: Decision says "We will implement Larder as Monolith", but Context defines Bounded Context modules and the Modular monolith option specifies schema isolation; the Monolith option explicitly says enhancements are difficult. The chosen deployment type is ambiguous.
- D_ADR0002: Context says "modular monolith" although ADR0001 operative decision says "Monolith"; the Consequences columns are copied from deployment alternatives and do not correspond to the database options.
- D_ADR0005: The Choreography option describes "Centralizing interactive user flows through a UI orchestrator" and thus does not describe choreography as a distinct alternative.
- Art_Principles_Larder: Two distinct entries carry the exact ID AP0005: Automatic Testing and Micro-UIs. Both retain dkg:principleId "AP0005"; graph IRIs disambiguate them.

The duplicate AP0005 identifiers and ADR0001 option ambiguity merit correction before relying on these decisions as unambiguous design instructions. AP0002 concerns cross-context communication; AP0006 concerns UI communication, so their scopes do not create an asserted principle tension. SADR0004 uses terse terminology that should be confirmed in its glossary.

## What newly connects

Each full-form ADR now answers a question minted from its context; all seven small-log rows answer their recorded questions. SADR0002 and SADR0003 share the naming question and are linked by supersession. No earlier domain graph or open questions were supplied, so these links cannot be joined to prior artifact questions.
