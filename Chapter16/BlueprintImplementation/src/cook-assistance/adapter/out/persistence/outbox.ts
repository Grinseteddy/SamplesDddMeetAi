import type { Generated, Transaction } from "kysely";

// ADR-CA-004 · the transactional outbox: repositories append their aggregate's events here, in the transaction
// that saves the aggregate; the relay (step 10) owns the delivery columns.
export const OUTBOX_TABLE = "cook_assistance.outbox";

export interface OutboxRow {
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

export async function appendToOutbox(tx: Transaction<OutboxTables>, rows: readonly NewOutboxRow[]): Promise<void> {
  for (const row of rows) {
    await tx.insertInto(OUTBOX_TABLE).values(row).execute();
  }
}
