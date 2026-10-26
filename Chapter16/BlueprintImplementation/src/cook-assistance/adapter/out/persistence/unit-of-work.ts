import type { Kysely, Transaction } from "kysely";

// One database transaction around everything a use case writes (ADR-CA-004: the aggregate, its outbox
// rows and, for a consumed message, the processed-message record).
export class KyselyUnitOfWork {
  constructor(private readonly db: Kysely<unknown>) {}

  async transactionally<T>(work: (tx: Transaction<unknown>) => Promise<T>): Promise<T> {
    return this.db.transaction().execute(work);
  }
}

/** Runs work in the caller's transaction when there is one (inside a unit of work), otherwise in a new one. */
export async function withinTransaction<DB>(db: Kysely<DB>, work: (tx: Transaction<DB>) => Promise<void>): Promise<void> {
  if (db.isTransaction) {
    await work(db as Transaction<DB>);
  } else {
    await db.transaction().execute(work);
  }
}
