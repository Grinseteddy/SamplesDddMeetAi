import { afterAll, beforeAll } from "vitest";
import { unitOfWorkContract } from "../../../test-support/contracts/unit-of-work.contract.js";
import { migrateToLatest } from "./migrations.js";
import { KyselyScopedUnitOfWork } from "./scoped-unit-of-work.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";
import { resetSchema } from "./test-support/reset-schema.js";

// The Kysely adapter must pass the same contract as the in-memory fake (step 8, unit 1d), on PostgreSQL 16.
let database: TestDatabase;

beforeAll(async () => {
  database = await startPostgres();
  await migrateToLatest(database.db);
});

afterAll(async () => {
  await database.stop();
});

unitOfWorkContract("Kysely adapter on PostgreSQL 16", async () => {
  await resetSchema(database.db);
  return new KyselyScopedUnitOfWork(database.db);
});
