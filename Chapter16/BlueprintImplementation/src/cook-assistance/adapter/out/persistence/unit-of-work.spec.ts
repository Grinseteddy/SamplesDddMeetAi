import { sql } from "kysely";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { migrateToLatest } from "./migrations.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";
import { KyselyUnitOfWork } from "./unit-of-work.js";

// Persistence — increment 0 (walking skeleton), step 7, unit 1: migrations and unit of work.
// Real PostgreSQL 16 through Testcontainers (ADR-CA-016). Slices B2-S01, B1-S04.

const outboxRowOf = (aggregateId: string) => sql`
  insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
  values ('HelpRequest', ${aggregateId}, 'HelpRequested', '{}', now())`;

describe("Migrations and unit of work — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  });

  afterAll(async () => {
    await database.stop();
  });

  describe("migrations", () => {
    it("running the migrations on an empty database creates the schema cook_assistance with the table outbox -- ADR-CA-003 ADR-CA-004", async () => {
      const tables = await sql<{ table_name: string }>`
        select table_name from information_schema.tables
        where table_schema = 'cook_assistance' and table_name = 'outbox'`.execute(database.db);

      expect(tables.rows).toEqual([{ table_name: "outbox" }]);
    });
    it("the outbox has the event columns id (ascending), aggregate_type, aggregate_id, event_type, payload, occurred_at, published_at -- ADR-CA-004", async () => {
      const columns = await sql<{ column_name: string; data_type: string; is_nullable: string }>`
        select column_name, data_type, is_nullable from information_schema.columns
        where table_schema = 'cook_assistance' and table_name = 'outbox'
          and column_name in ('id', 'aggregate_type', 'aggregate_id', 'event_type', 'payload', 'occurred_at', 'published_at')
        order by column_name`.execute(database.db);

      expect(columns.rows).toEqual([
        { column_name: "aggregate_id", data_type: "uuid", is_nullable: "NO" },
        { column_name: "aggregate_type", data_type: "text", is_nullable: "NO" },
        { column_name: "event_type", data_type: "text", is_nullable: "NO" },
        { column_name: "id", data_type: "bigint", is_nullable: "NO" },
        { column_name: "occurred_at", data_type: "timestamp with time zone", is_nullable: "NO" },
        { column_name: "payload", data_type: "jsonb", is_nullable: "NO" },
        { column_name: "published_at", data_type: "timestamp with time zone", is_nullable: "YES" },
      ]);
    });
    it("a new outbox row starts with attempts 0, next_attempt_at due at once (-infinity), no last_error, no dead_lettered_at and no published_at -- ADR-CA-004", async () => {
      const inserted = await sql<{ id: string }>`
        insert into cook_assistance.outbox (aggregate_type, aggregate_id, event_type, payload, occurred_at)
        values ('HelpRequest', '0192f1a0-0000-7000-8000-000000000001', 'HelpRequested', '{}', now())
        returning id`.execute(database.db);

      const row = await sql<{
        attempts: number;
        due_at_once: boolean;
        last_error: string | null;
        dead_lettered_at: Date | null;
        published_at: Date | null;
      }>`
        select attempts, next_attempt_at = '-infinity'::timestamptz as due_at_once,
               last_error, dead_lettered_at, published_at
        from cook_assistance.outbox where id = ${inserted.rows[0]?.id}`.execute(database.db);

      expect(row.rows).toEqual([
        { attempts: 0, due_at_once: true, last_error: null, dead_lettered_at: null, published_at: null },
      ]);
    });
    it("the outbox has the relay's claim index on (aggregate_id, id) for rows neither published nor dead-lettered -- ADR-CA-004", async () => {
      const indexes = await sql<{ indexdef: string }>`
        select indexdef from pg_indexes
        where schemaname = 'cook_assistance' and tablename = 'outbox' and indexname = 'outbox_claim_idx'`.execute(database.db);

      expect(indexes.rows).toHaveLength(1);
      expect(indexes.rows[0]?.indexdef).toContain("(aggregate_id, id)");
      expect(indexes.rows[0]?.indexdef).toContain("WHERE ((published_at IS NULL) AND (dead_lettered_at IS NULL))");
    });
    it("running the migrations a second time applies nothing -- ADR-CA-003", async () => {
      const countApplied = async () =>
        (await sql<{ applied: string }>`select count(*) as applied from cook_assistance.kysely_migration`.execute(database.db))
          .rows[0]?.applied;
      const before = await countApplied();

      await migrateToLatest(database.db);

      expect(await countApplied()).toBe(before);
    });
  });

  describe("unit of work", () => {
    it("work done inside transactionally is committed when it returns -- B1-S04 ADR-CA-004", async () => {
      const aggregateId = "0192f1a0-0000-7000-8000-0000000000c1";

      await new KyselyUnitOfWork(database.db).transactionally(async (tx) => {
        await outboxRowOf(aggregateId).execute(tx);
      });

      const rows = await sql`select id from cook_assistance.outbox where aggregate_id = ${aggregateId}`.execute(database.db);
      expect(rows.rows).toHaveLength(1);
    });
    it("work that throws inside transactionally is rolled back — nothing of it is visible afterwards, and the error reaches the caller -- B1-S04 ADR-CA-004", async () => {
      const aggregateId = "0192f1a0-0000-7000-8000-0000000000c2";
      const failure = new Error("the use case failed after writing");

      const outcome = new KyselyUnitOfWork(database.db).transactionally(async (tx) => {
        await outboxRowOf(aggregateId).execute(tx);
        throw failure;
      });

      await expect(outcome).rejects.toBe(failure);
      const rows = await sql`select id from cook_assistance.outbox where aggregate_id = ${aggregateId}`.execute(database.db);
      expect(rows.rows).toHaveLength(0);
    });
  });
});
