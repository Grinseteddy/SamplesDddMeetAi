# ADR-CA-007 — UUIDv7 identifiers and an injected clock

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 7

## Context
IDs are server-generated (`format: uuid` in both specs). Deadlines (INV-07, INV-08) and the timeout
runner (QS-13) depend on time; tests must control it.

## Decision
- Identifiers are **UUIDv7**, generated in the application layer through an `IdGenerator` port, wrapped
  in typed IDs (`HelpRequestId`, `HelpId`) in the domain.
- Time comes only from a **`Clock` port** returning UTC instants; production uses the system clock,
  tests a controllable fake. Domain code never calls `Date.now()` or `new Date()`.

## Consequences
Time-ordered IDs index well; deadlines are deterministic in tests and property tests.

## Quality scenarios
Serves QS-13, QS-16, QS-01 (testability).
