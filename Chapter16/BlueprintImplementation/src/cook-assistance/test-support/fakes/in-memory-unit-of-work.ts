import type { TransactionScope, UnitOfWork } from "../../application/ports/unit-of-work.js";
import { InMemoryHelpRepository } from "./in-memory-help-repository.js";
import { InMemoryHelpRequestRepository } from "./in-memory-help-request-repository.js";
import { InMemoryProcessedMessages } from "./in-memory-processed-messages.js";

// In-memory fake of UnitOfWork for use-case and policy-handler tests; must pass the shared contract.
export class InMemoryUnitOfWork implements UnitOfWork {
  readonly helpRequests = new InMemoryHelpRequestRepository();
  readonly helps = new InMemoryHelpRepository();
  readonly processedMessages = new InMemoryProcessedMessages();

  async transactionally<T>(work: (scope: TransactionScope) => Promise<T>): Promise<T> {
    // ADR-CA-004 · all or nothing, like the database transaction
    const undos = [this.helpRequests, this.helps, this.processedMessages].map((store) => store.checkpoint());
    try {
      return await work(this);
    } catch (error) {
      undos.forEach((undo) => undo());
      throw error;
    }
  }
}
