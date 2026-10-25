import { sql } from "kysely";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { migrateToLatest } from "./migrations.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";
import { resetSchema } from "./test-support/reset-schema.js";

// Persistence — increment 0, step 10, unit 10.0: the outbox row's stable message id (migration 005, ADR-CA-004 as amended).
// Real PostgreSQL 16 through Testcontainers.

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;

const outboxRow = (aggregateId: string) => sql<{ message_id: string }>`
  insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
  values ('HelpRequest', ${aggregateId}, 'HelpRequested', '{}', now())
  returning message_id::text`;

describe("outbox message_id — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  });

  beforeEach(async () => {
    await resetSchema(database.db);
  });

  afterAll(async () => {
    await database.stop();
  });

  it("migration 005 adds outbox.message_id uuid NOT NULL, and a new row gets one generated -- G2 ADR-CA-004", async () => {
    const inserted = await outboxRow("0192f1a0-0000-7000-8000-000000000001").execute(database.db);

    expect(inserted.rows[0]?.message_id).toMatch(UUID);
    const column = await sql<{ data_type: string; is_nullable: string }>`
      select data_type, is_nullable from information_schema.columns
      where table_schema = 'cook_assistance' and table_name = 'outbox' and column_name = 'message_id'`.execute(database.db);
    expect(column.rows).toEqual([{ data_type: "uuid", is_nullable: "NO" }]);
  });
  it("two outbox rows get different message ids -- G2 ADR-CA-004", async () => {
    const first = await outboxRow("0192f1a0-0000-7000-8000-000000000001").execute(database.db);
    const second = await outboxRow("0192f1a0-0000-7000-8000-000000000001").execute(database.db);

    expect(first.rows[0]?.message_id).not.toBe(second.rows[0]?.message_id);
  });
});
