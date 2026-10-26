# ADR-CA-020 — AP0005 measured as line and branch coverage ≥ 95 %

Status: accepted (2026-10-26) · Context: Cook Assistance · Step 13 (inspection increment 0, defect m-16)

## Context
Principle AP0005 "We can test our system by 95 % automatically" is marked testable in the decision graph, but no
gate measured it: the intent-coverage report counts rules, examples, operations and quality scenarios, not code. The
increment 0 inspection reported the principle as unmeasured (m-16).

## Decision
- **Measure:** line coverage and branch coverage of the hand-written service code (`src/cook-assistance/**`, without
  specs, test support, acceptance, contract and quality harness code; generated code is outside `src/cook-assistance`),
  each **≥ 95 %**, from the whole Vitest suite on real PostgreSQL and RabbitMQ.
- **Tool:** `@vitest/coverage-v8` 5.0.5 (pinned to Vitest's version), configured in `vitest.config.ts`; thresholds
  fail the run. `npm run verify` runs Vitest with `--coverage` (also in CI); `npm run test:coverage` alone.
- **Defensive fallbacks** that cannot happen are removed where the types allow it; those the library types require
  (e.g. amqplib's optional `headers`) stay and count against the 5 %.

## Consequences
At adoption: lines 99.4 %, branches 95.2 % (236/248) — reached by tests for behavior that had none (key-fetch
failure → 500, a token without role and scope claims, deny by default for an undeclared operation, a non-Error thrown,
an unknown AsyncAPI operation, a message without headers, a failing migration, non-duplicate database errors, a help
with a community provider, a request without a recipe, a row the relay cannot map) and two simplifications.
Intent coverage stays the measure of *what* is tested; this one of *how much* of the code runs under test.

## Principles and quality scenarios
Honors AP0005. Supports QS-02 (the build stays one command) — the coverage run adds no separate job.
