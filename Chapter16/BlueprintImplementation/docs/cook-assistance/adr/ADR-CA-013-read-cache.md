# ADR-CA-013 — In-process read cache as fallback during database outages

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 13

## Context
QS-05: while the database is unavailable, GETs on cached resources are answered from a cache and marked
as possibly stale; commands answer 503.

## Decision
- An **in-process LRU cache** (`lru-cache`, max **10 000** entries per instance) behind the query port,
  filled on every successful GET — single resources and pages, keyed **per caller visibility**
  (ADR-CA-009), so a cached entry never shows a caller more than the database would.
- Used **only** when the database is unreachable (connection error or timeout); otherwise every GET
  reads the database.
- A response served from the cache carries the standard **`Age`** header (seconds since it was read) —
  an additive change to OpenAPI 2.0.0 (changelog).
- No cache for commands; they answer 503 Problem with `Retry-After` (ADR-CA-006).

## Consequences
- Each instance has its own cache (QS-14): after a restart during an outage, nothing is cached.
- Erasure (QS-18) and retention (QS-17) must also evict cache entries of the affected cook.

## Quality scenarios
Serves QS-05; trades off memory per instance.
