# ADR-CA-004 — RabbitMQ with amqplib, transactional outbox, bounded retries

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 4

## Context
ADR0003 fixes RabbitMQ. QS-04 requires commands to succeed while the broker is down and their events to
be published after recovery; QS-07 bounds retries and forbids silent loss; QS-12 asks for ≤ 2 s p95 from
commit to publication; the Grandma Avatar AI consumer must be idempotent (AsyncAPI 2.0.0).

## Decision
- **Client:** `amqplib`, wrapped by `amqp-connection-manager` for reconnects, behind a publisher port
  and a consumer adapter.
- **Transactional outbox** (`cook_assistance.outbox`): events are written in the same transaction as
  the aggregate; complete from the first migration — `message_id`, `aggregate_id`, `aggregate_version`,
  `routing_key`, `payload`, `headers`, `created_at`, `attempts`, `next_attempt_at`, `last_error`,
  `published_at`, `dead_lettered_at`, with a claim index.
- **Relay:** polls every **500 ms**, claims rows with `FOR UPDATE SKIP LOCKED` (safe with several
  relays, QS-14), publishes with publisher confirms and persistent delivery, **in order per aggregate**.
- **Delivery:** at-least-once with a stable `messageId`.
- **Retries:** **5 attempts**, exponential backoff 1 s → 1 min; then the row is marked dead-lettered,
  a metric is raised and the team is alerted (QS-07, QS-22).
- **Consumer** (Grandma Avatar AI): manual ack; idempotent by `messageId` through a
  `processed_message` record written in the same transaction as the Help (unit of work); a message that
  fails 5 times goes to a dead-letter queue.

## Consequences
- Commands never depend on broker availability (QS-04).
- Duplicates are possible; every consumer de-duplicates by `messageId`.
- Event loss only as a visible dead letter.

## Principles and quality scenarios
Honors ADR0003, AP0002, AP0004 (residuality), AP0007. Serves QS-04, QS-07, QS-12, QS-14, QS-22.

## Amendment — 2026-10-25 (step 10, as built; user decisions in step 10)
- **Outbox columns as built** (step 7, migration 001, plus 005): `id` (bigserial, the relay's order), `aggregate_type`,
  `aggregate_id`, `event_type`, `payload` (jsonb, wire-shaped — G6), `occurred_at`, `published_at`, `attempts`,
  `next_attempt_at`, `last_error`, `dead_lettered_at`, and **`message_id`** (uuid, `gen_random_uuid()` default,
  migration 005 — G2), stable across retries. `routing_key`, `headers` and `aggregate_version` are **not stored**:
  the relay derives exchange, routing key and headers from the publish decision per event type (`EVENT_MESSAGES`,
  checked against the domain's event union at compile time); `correlationId` is the help request's ID (G3).
- **Relay:** claim rounds until nothing is due (capped at 100), each in its own transaction, oldest due pending row
  per aggregate with `FOR UPDATE OF o SKIP LOCKED`; a `RelayPoller` runs it every 500 ms, never overlapping, waits
  for the running pass on shutdown and reports a failed run (`relay-run-failed`).
- **Retries as built:** 5 attempts; the n-th failure waits 2^(n−1) s (1, 2, 4, 8 s) — the 1 min cap is never
  reached with 5 attempts (user decision: keep 5 attempts). A message that breaks the AsyncAPI send schema, or an
  event type without a publish decision (`UnmappedEvent`), is dead-lettered at once. A dead-lettered row unblocks
  its aggregate's later rows. Dead letters are **reported** after commit (`outbox-dead-lettered` log event with
  message ID, event type, last error); the **metric and alert** (QS-07, QS-22) are open items for steps 13/14.
- **Consumer queue:** quorum queue `cook-assistance.grandma-avatar-help-provided` with `x-delivery-limit` **4** —
  RabbitMQ dead-letters once the limit is *exceeded*, so 4 gives the decided 5 deliveries — and a dead-letter
  exchange `cook-assistance.dead-letter` (fanout) with its queue. **Queue arguments cannot change on an existing
  queue:** a different limit later needs a new queue (or moving the old one's messages).
- **Ownership:** the `grandma-avatar` exchange belongs to the Grandma Avatar AI context and is declared there;
  Cook Assistance only binds its queue to it (ADR-CA-008: our broker user publishes only on `cook-assistance`).
- **Client wiring:** one `amqp-connection-manager` confirm channel; topology and the consumer are channel setups,
  so they are restored after every reconnect; `prefetch 1`, manual ack.
