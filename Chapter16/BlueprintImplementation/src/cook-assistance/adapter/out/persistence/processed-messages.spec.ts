import { sql } from "kysely";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { migrateToLatest } from "./migrations.js";
import { MessageAlreadyProcessed, MessageId } from "../../../application/ports/processed-messages.js";
import { KyselyProcessedMessages } from "./processed-messages.js";
import { KyselyUnitOfWork } from "./unit-of-work.js";
import { startPostgres, type TestDatabase } from "./test-support/postgres-container.js";

// Persistence — increment 0 (walking skeleton), step 7, unit 4: processed messages (consumer idempotency).
// Real PostgreSQL 16 through Testcontainers (ADR-CA-016). Slice B1-S04, ADR-CA-004.

describe("KyselyProcessedMessages — PostgreSQL 16", () => {
  let database: TestDatabase;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
  });

  afterAll(async () => {
    await database.stop();
  });

  it("the migrations create the table cook_assistance.processed_message -- B1-S04 ADR-CA-004", async () => {
    const tables = await sql<{ table_name: string }>`
      select table_name from information_schema.tables
      where table_schema = 'cook_assistance' and table_name = 'processed_message'`.execute(database.db);

    expect(tables.rows).toEqual([{ table_name: "processed_message" }]);
  });
  it("a message id that was never recorded has not been processed -- B1-S04 ADR-CA-004", async () => {
    const processed = await new KyselyProcessedMessages(database.db).hasBeenProcessed(MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d"));

    expect(processed).toBe(false);
  });
  it("a recorded message id has been processed -- B1-S04 ADR-CA-004", async () => {
    const processedMessages = new KyselyProcessedMessages(database.db);
    const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c02");

    await processedMessages.record(messageId);

    expect(await processedMessages.hasBeenProcessed(messageId)).toBe(true);
  });
  it("recording the same message id a second time is refused with MessageAlreadyProcessed — the database's unique violation is translated -- B1-S04 ADR-CA-004", async () => {
    const processedMessages = new KyselyProcessedMessages(database.db);
    const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c03");
    await processedMessages.record(messageId);

    await expect(processedMessages.record(messageId)).rejects.toBeInstanceOf(MessageAlreadyProcessed);
  });
  it("a record inside a unit of work joins its transaction: when the work throws afterwards, the message counts as not processed -- B1-S04 ADR-CA-004", async () => {
    const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c05");
    const failure = new Error("handling the message failed after recording it");

    const outcome = new KyselyUnitOfWork(database.db).transactionally(async (tx) => {
      await new KyselyProcessedMessages(tx).record(messageId);
      throw failure;
    });

    await expect(outcome).rejects.toBe(failure);
    expect(await new KyselyProcessedMessages(database.db).hasBeenProcessed(messageId)).toBe(false);
  });
});
