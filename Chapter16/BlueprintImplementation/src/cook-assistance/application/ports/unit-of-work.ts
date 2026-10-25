import type { HelpRequestRepository } from "../../domain/help-request/help-request-repository.js";
import type { HelpRepository } from "../../domain/help/help-repository.js";
import type { ProcessedMessages } from "./processed-messages.js";

/** What a use case or policy handler may write inside one transaction. */
export interface TransactionScope {
  readonly helpRequests: HelpRequestRepository;
  readonly helps: HelpRepository;
  readonly processedMessages: ProcessedMessages;
}

// ADR-CA-004 · one transaction around the aggregate, its outbox rows and, for a consumed message,
// the processed-message record: everything is committed together or nothing is.
export interface UnitOfWork {
  transactionally<T>(work: (scope: TransactionScope) => Promise<T>): Promise<T>;
}
