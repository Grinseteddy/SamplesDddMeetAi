# ADR-CA-005 — Optimistic locking by version compare-and-set

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 5

## Context
Concurrent changes to one aggregate are rare but real (two helpers; the cook cancels while the timeout
fires). QS-15 forbids lost updates.

## Decision
Every aggregate carries a `version`. Repositories save by compare-and-set; a mismatch raises
`ConcurrencyConflict`, an error declared **next to the repository port** (domain/application side).
The HTTP adapter answers `409` (`code: ConcurrencyConflict`); policy handlers, the Grandma Avatar
consumer and the timeout runner reload and retry **up to 3 times**, then let the message or job fail
(and, for messages, follow ADR-CA-004's retry rules).

## Consequences
No locks held across requests; retries must be idempotent (they are — commands re-check guards on reload).

## Quality scenarios
Serves QS-15, QS-14.
