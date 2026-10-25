import { HelpRequest, type HelpRequestId, type HelpRequestState } from "../../domain/help-request/help-request.js";
import type { HelpRequestEvent } from "../../domain/help-request/help-request-events.js";
import type { HelpRequestRepository } from "../../domain/help-request/help-request-repository.js";
import { InMemoryVersionedStore } from "./in-memory-versioned-store.js";

// In-memory fake of HelpRequestRepository for use-case tests; must pass the shared contract.
// Stores state snapshots and rebuilds through reconstitute — never hands out the object it stored.
export class InMemoryHelpRequestRepository implements HelpRequestRepository {
  private readonly stored = new InMemoryVersionedStore<HelpRequestState>("Help request");

  /** The events handed over with each save — the fake's stand-in for the outbox. */
  readonly savedEvents: HelpRequestEvent[] = [];

  async findById(id: HelpRequestId): Promise<HelpRequest | undefined> {
    const state = this.stored.find(id);
    return state === undefined ? undefined : HelpRequest.reconstitute(state);
  }

  async save(helpRequest: HelpRequest): Promise<void> {
    this.stored.write({
      id: helpRequest.id,
      requester: helpRequest.requester,
      title: helpRequest.title,
      question: helpRequest.question,
      recipeId: helpRequest.recipeId,
      howToStepId: helpRequest.howToStepId,
      addressees: [...helpRequest.addressees],
      status: helpRequest.status,
      answerDeadline: helpRequest.answerDeadline,
      version: helpRequest.version,
    });
    this.savedEvents.push(...helpRequest.pullEvents());
  }
}
