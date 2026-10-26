import type { Transaction } from "kysely";
import { appendToOutbox, type NewOutboxRow, type OutboxTables } from "./outbox.js";
import { project } from "./read-model/projections.js";

/** An aggregate's new events, in its save transaction: to the outbox (ADR-CA-004) and into the read models (ADR-CA-012). */
export async function recordEvents<DB extends OutboxTables>(tx: Transaction<DB>, rows: readonly NewOutboxRow[]): Promise<void> {
  await appendToOutbox(tx, rows);
  await project(tx, rows);
}
