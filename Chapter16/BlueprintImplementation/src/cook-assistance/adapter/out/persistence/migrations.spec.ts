import { sql } from "kysely";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { migrateToLatest } from "./migrations.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";

// Persistence — ADR-CA-003 (forward-only migrations at startup): a migration that fails stops the start.

describe("migrations — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
  });

  afterAll(async () => {
    await database.stop();
  });

  it("a migration that cannot apply (a table of that name exists already) rejects instead of starting half-migrated -- ADR-CA-003 (AP0005 coverage)", async () => {
    await sql`create schema cook_assistance`.execute(database.db);
    await sql`create table cook_assistance.outbox (id integer)`.execute(database.db);

    await expect(migrateToLatest(database.db)).rejects.toThrow(/already exists/);
  });
});
