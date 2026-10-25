import { sql } from "kysely";
import { afterAll, beforeAll } from "vitest";
import { helpRepositoryContract } from "../../../test-support/contracts/help-repository.contract.js";
import { KyselyHelpRepository } from "./help-repository.js";
import { migrateToLatest } from "./migrations.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";
import { resetSchema } from "./test-support/reset-schema.js";

// The Kysely adapter must pass the same contract as the in-memory fake (step 8, unit 1b), on PostgreSQL 16.
let database: TestDatabase;

beforeAll(async () => {
  database = await startPostgres();
  await migrateToLatest(database.db);
});

afterAll(async () => {
  await database.stop();
});

helpRepositoryContract("Kysely adapter on PostgreSQL 16", async () => {
  await resetSchema(database.db);
  return {
    repository: new KyselyHelpRepository(database.db),
    savedEventTypes: async () =>
      (await sql<{ event_type: string }>`select event_type from cook_assistance.outbox order by id`.execute(database.db)).rows.map(
        (row) => row.event_type,
      ),
  };
});
