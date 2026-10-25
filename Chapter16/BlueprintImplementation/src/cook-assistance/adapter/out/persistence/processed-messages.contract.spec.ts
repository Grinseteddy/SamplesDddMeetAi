import { sql } from "kysely";
import { afterAll, beforeAll } from "vitest";
import { processedMessagesContract } from "../../../test-support/contracts/processed-messages.contract.js";
import { migrateToLatest } from "./migrations.js";
import { KyselyProcessedMessages } from "./processed-messages.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";

// The Kysely adapter must pass the same contract as the in-memory fake (step 8, unit 1c), on PostgreSQL 16.
let database: TestDatabase;

beforeAll(async () => {
  database = await startPostgres();
  await migrateToLatest(database.db);
});

afterAll(async () => {
  await database.stop();
});

processedMessagesContract("Kysely adapter on PostgreSQL 16", async () => {
  await sql`truncate cook_assistance.processed_message`.execute(database.db);
  return new KyselyProcessedMessages(database.db);
});
