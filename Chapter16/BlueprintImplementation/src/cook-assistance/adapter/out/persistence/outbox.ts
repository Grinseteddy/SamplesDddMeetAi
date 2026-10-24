import type { Generated, Transaction } from "kysely";

// ADR-CA-004 · the transactional outbox: repositories append their aggregate's events here, in the transaction
// that saves the aggregate; the relay (step 10) owns the delivery columns.
const OUTBOX_TABLE = "cook_assistance.outbox";

interface OutboxRow {
  id: Generated<string>;
  aggregate_type: string;
  aggregate_id: string;
  event_type: string;
  payload: unknown;
  occurred_at: Date;
}

export type NewOutboxRow = Omit<OutboxRow, "id">;

export interface OutboxTables {
  [OUTBOX_TABLE]: OutboxRow;
}

/** Appends rows in the caller's transaction; works for any repository whose table map contains the outbox. */
export async function appendToOutbox<DB extends OutboxTables>(tx: Transaction<DB>, rows: readonly NewOutboxRow[]): Promise<void> {
  const outbox = tx as unknown as Transaction<OutboxTables>; // narrow to the outbox table only (TS < 6 rejects the wider map)
  for (const row of rows) {
    await outbox.insertInto(OUTBOX_TABLE).values(row).execute();
  }
}
