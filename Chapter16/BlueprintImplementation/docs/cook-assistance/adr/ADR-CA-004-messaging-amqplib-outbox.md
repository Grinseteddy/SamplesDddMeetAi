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
