import type { Transaction } from "kysely";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { isUniqueViolation } from "./postgres-errors.js";

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

/** ADR-CA-005 · a new aggregate is inserted as version 1; an id that is already taken → ConcurrencyConflict. */
async function insertFirstVersion(
  versioned: Transaction<VersionedTables>,
  { table, aggregateName, row }: VersionedWrite,
): Promise<void> {
  try {
    await versioned.insertInto(table).values({ ...row, version: 1 }).execute();
  } catch (error) {
    // constraint translation: the id is the primary key, so a unique violation means it is already taken
    if (isUniqueViolation(error)) {
      throw new ConcurrencyConflict(aggregateName, row.id);
    }
    throw error;
  }
}

/**
 * ADR-CA-005 · a loaded aggregate is updated WHERE id AND version = loaded version, setting version + 1;
 * zero rows → ConcurrencyConflict.
 */
async function updateByCompareAndSet(
  versioned: Transaction<VersionedTables>,
  { table, aggregateName, row, loadedVersion }: VersionedWrite,
): Promise<void> {
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

/** Writes an aggregate's row: loaded version 0 means new, anything else was loaded and is compared-and-set. */
export async function writeVersioned<DB>(tx: Transaction<DB>, write: VersionedWrite): Promise<void> {
  const versioned = tx as unknown as Transaction<VersionedTables>; // one table, known only by name here
  return write.loadedVersion === 0 ? insertFirstVersion(versioned, write) : updateByCompareAndSet(versioned, write);
}
