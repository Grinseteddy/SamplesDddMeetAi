import { sql } from "kysely";
import { afterAll, beforeAll } from "vitest";
import { helpRequestRepositoryContract } from "../../../test-support/contracts/help-request-repository.contract.js";
import { KyselyHelpRequestRepository } from "./help-request-repository.js";
import { migrateToLatest } from "./migrations.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";

// The Kysely adapter must pass the same contract as the in-memory fake (step 8, unit 1a), on PostgreSQL 16.
let database: TestDatabase;

beforeAll(async () => {
  database = await startPostgres();
  await migrateToLatest(database.db);
});

afterAll(async () => {
  await database.stop();
});

helpRequestRepositoryContract("Kysely adapter on PostgreSQL 16", async () => {
  await sql`truncate cook_assistance.help_request, cook_assistance.outbox`.execute(database.db);
  return {
    repository: new KyselyHelpRequestRepository(database.db),
    savedEventTypes: async () =>
      (await sql<{ event_type: string }>`select event_type from cook_assistance.outbox order by id`.execute(database.db)).rows.map(
        (row) => row.event_type,
      ),
  };
});
