import type { Transaction } from "kysely";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";

/** Every state-stored aggregate row carries its id and its optimistic-locking version. */
export interface VersionedRow {
  id: string;
  version: number;
}

type VersionedTables = Record<string, VersionedRow>;

/** What to write: the aggregate's table and name, its row, and the version it was loaded with (0 = new). */
export interface VersionedWrite {
  readonly table: string;
  readonly aggregateName: string;
  readonly row: VersionedRow;
  readonly loadedVersion: number;
}

/**
 * ADR-CA-005 · a new aggregate (loaded version 0) is inserted as version 1; a loaded one is updated by
 * compare-and-set (WHERE id AND version = loaded version, setting version + 1); zero rows → ConcurrencyConflict.
 */
export async function writeVersioned<DB>(
  tx: Transaction<DB>,
  { table, aggregateName, row, loadedVersion }: VersionedWrite,
): Promise<void> {
  const versioned = tx as unknown as Transaction<VersionedTables>; // one table, known only by name here
  if (loadedVersion === 0) {
    await versioned.insertInto(table).values({ ...row, version: 1 }).execute();
    return;
  }
  const updated = await versioned
    .updateTable(table)
    .set({ ...row, version: loadedVersion + 1 })
    .where("id", "=", row.id)
    .where("version", "=", loadedVersion)
    .executeTakeFirstOrThrow(); // an UPDATE always yields one result (possibly 0 rows)
  if (updated.numUpdatedRows === 0n) {
    throw new ConcurrencyConflict(aggregateName, row.id);
  }
}
