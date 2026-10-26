# ADR-CA-014 — Timeout runner: database poller with SKIP LOCKED

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 14

## Context
INV-07/INV-08: the system cancels a REQUESTED help request when its answer deadline passes (request
time or last rejection + topic timeout). QS-13 asks for ≤ 1 min after the deadline; QS-14 for safety
with several instances.

## Decision
- `help_request.answer_deadline` is stored (and indexed with `status`) and moved by the aggregate on
  creation and on *Restart answer deadline* (B1-S10).
- Every instance runs a **poller every 20 s**: in its own transaction it selects up to 50 rows with
  `status = 'REQUESTED' AND answer_deadline <= now()` using **`FOR UPDATE SKIP LOCKED`**, and cancels
  each through the *Cancel help request (time)* use case (B1-S07) — guard re-checked on the loaded
  aggregate with the injected Clock, compare-and-set save, outbox event, up to 3 retries
  (ADR-CA-005).
- Several instances share the work without a leader; a row is never cancelled twice.

## Consequences
- Worst-case precision ≈ 20 s + processing time (QS-13 ≤ 1 min).
- Metric `timeouts_fired_total` and the runner's lag are exposed (QS-20).

## Quality scenarios
Serves QS-13, QS-14, QS-15.
