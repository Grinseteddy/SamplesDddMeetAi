import { describe, it } from "vitest";

// Persistence — increment 0, step 10, unit 10.0: the outbox row's stable message id (migration 005, ADR-CA-004 as amended).
// Real PostgreSQL 16 through Testcontainers.

describe("outbox message_id \u2014 PostgreSQL 16", () => {
  it.todo("migration 005 adds outbox.message_id uuid NOT NULL, and a new row gets one generated -- G2 ADR-CA-004");
  it.todo("two outbox rows get different message ids -- G2 ADR-CA-004");
});
