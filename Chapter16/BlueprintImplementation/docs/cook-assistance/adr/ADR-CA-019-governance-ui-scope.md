# ADR-CA-019 — Governance findings and UI scope

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, questions 19–20

## Context
Step 1 found governance issues in the decision graph: G1 ADR0005 (Orchestrator UI) overrides AP0005
(Micro-UIs) without acknowledgment; G2 the ID AP0005 is used for two principles; G3 AP0002
(asynchronous between bounded contexts) against the synchronous arrows of context map v2 from Meal
Planning and Meal Preparation into Cook Assistance.

## Decision
- **G1 — acknowledged, no conflict:** the orchestrator (AppShell) ingests the micro-UIs of the bounded
  contexts, so ADR0005 keeps AP0005 (Micro-UIs) rather than overriding it.
- **G3 — no override:** the synchronous POST/GET calls from Meal Planning and Meal Preparation are made
  by their **micro-UIs on behalf of the cook**, covered by ADR0004 (synchronous communication for UIs).
  No other service calls the Help API synchronously; Cook Assistance's only synchronous call to another
  context is the Recipe Catalog lookup (ADR-CA-015, acknowledged override).
- **G2 — logged** for the owner of the decision graph: renumber one of the two AP0005 principles.
- **UI scope:** this run builds the Cook Assistance **service and its APIs only**; the Cook Assistance
  micro-UI (AP0005, ADR0005) is a later run.

## Consequences
The decision-graph owner may ingest G1 as an acknowledged relation and fix G2; nothing in this service
depends on either.
