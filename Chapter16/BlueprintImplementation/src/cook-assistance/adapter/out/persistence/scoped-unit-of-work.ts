import type { Kysely } from "kysely";
import type { TransactionScope, UnitOfWork } from "../../../application/ports/unit-of-work.js";
import { KyselyHelpRepository } from "./help-repository.js";
import { KyselyHelpRequestRepository } from "./help-request-repository.js";
import { KyselyProcessedMessages } from "./processed-messages.js";
import { KyselyUnitOfWork } from "./unit-of-work.js";

// The application's UnitOfWork port on PostgreSQL: one Kysely transaction, the repositories bound to it.
export class KyselyScopedUnitOfWork implements UnitOfWork {
  private readonly unitOfWork: KyselyUnitOfWork;

  constructor(db: Kysely<unknown>) {
    this.unitOfWork = new KyselyUnitOfWork(db);
  }

  async transactionally<T>(work: (scope: TransactionScope) => Promise<T>): Promise<T> {
    return this.unitOfWork.transactionally((tx) =>
      work({
        helpRequests: new KyselyHelpRequestRepository(tx),
        helps: new KyselyHelpRepository(tx),
        processedMessages: new KyselyProcessedMessages(tx),
      }),
    );
  }
}
