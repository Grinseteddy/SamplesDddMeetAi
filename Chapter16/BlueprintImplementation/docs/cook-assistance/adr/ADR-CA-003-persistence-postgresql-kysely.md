# ADR-CA-003 — State-stored tables on PostgreSQL 16 with Kysely

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 3

## Context
ADR0002 fixes one database instance for Larder; Chapter 13 gives each bounded context its own schema
and user. The aggregates Help request and Help need optimistic locking (QS-15), an outbox written in
the same transaction (QS-04), and deletion/anonymization by cook (QS-17, QS-18).

## Decision
- **State-stored**: one table per aggregate (`help_request`, `help`) holding the current state and a
  `version` column; no event store.
- **PostgreSQL 16**, schema `cook_assistance`, owned by its own database user, on the shared Larder
  instance.
- **Kysely** as the typed query builder (no ORM entities); repositories in
  `adapter/out/persistence` map rows to domain objects and back.
- **Migrations**: Kysely's migrator with versioned, forward-only migrations in
  `adapter/out/persistence/migrations`, applied at startup and in tests.

## Consequences
- Compare-and-set on `version` is explicit SQL (`UPDATE … WHERE id = ? AND version = ?`).
- No framework types reach the domain (QS-01).
- Unique constraints only where an invariant demands one, named after the rule.
- Tests run against a real PostgreSQL 16 (question 16).

## Principles and quality scenarios
Honors ADR0002 and AP0003. Serves QS-01, QS-04, QS-15, QS-17, QS-18.
