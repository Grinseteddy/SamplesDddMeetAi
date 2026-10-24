# ADR-CA-017 — Property-based testing with fast-check

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 17

## Context
Step 6 writes one property test per quantitative or cardinality invariant: INV-07, INV-08
(quantitative: deadlines), INV-03, INV-12, INV-16, INV-24 (cardinality). They must be reproducible.

## Decision
- **fast-check 4.10.2** with Vitest; properties are pure unit tests (no database, no HTTP), time only
  through the fake `Clock` (ADR-CA-007).
- **200 runs** per property in the normal suite; **1000 runs** in a nightly CI job.
- The **seed is fixed** in CI through `FC_SEED` and logged with every run; a failing run prints seed and
  path for replay.
- A shrunk counterexample that exposed a gap is kept as a plain example test and logged as a candidate
  example for the Example Mapping.

## Quality scenarios
Serves QS-01 (testability), QS-02 (build budget), QS-13.
