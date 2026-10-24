# ADR-CA-012 — Query side: projections in the command's transaction, page/size paging

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 12

## Context
The read models "Help request" and "Help" back the GET operations. QS-11 allows ≤ 5 s lag; QS-09 asks for
p95 ≤ 200 ms on about one year of data (QS-16, ~120 000 requests). ADR-CA-009 filters visibility per caller.

## Decision
- Projection tables `help_request_view` and `help_view` (schema `cook_assistance`) are updated **in the
  same transaction** as the command that changes the aggregate — one projection function per event
  type; consistency is immediate, so read-your-writes holds for every actor.
- GET operations read only through a **query port** on the views — never by loading aggregates — and
  apply the visibility rules of ADR-CA-009 in the query.
- Views are **rebuildable** from the aggregate tables by a migration-safe rebuild command.
- Paging stays **page/size** as approved in spec 2.0.0, ordered by `createdAt, id`; indexes on the
  filter columns (`status`, `topic`, `requester`, addressees).

## Consequences
- No projector runner, no lag measurement needed beyond QS-11's test (lag = 0).
- Write transactions touch the view rows too; acceptable at 10 req/s (QS-08).

## Quality scenarios
Serves QS-09, QS-11, QS-16; keeps QS-01 (query port).
