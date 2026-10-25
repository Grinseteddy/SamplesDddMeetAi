import { Help, type HelpId, type HelpState } from "../../domain/help/help.js";
import type { HelpEvent } from "../../domain/help/help-events.js";
import type { HelpRepository } from "../../domain/help/help-repository.js";
import { InMemoryVersionedStore } from "./in-memory-versioned-store.js";

// In-memory fake of HelpRepository for policy-handler tests; must pass the shared contract.
// Stores state snapshots and rebuilds through reconstitute — never hands out the object it stored.
export class InMemoryHelpRepository implements HelpRepository {
  private readonly stored = new InMemoryVersionedStore<HelpState>("Help");

  /** The events handed over with each save — the fake's stand-in for the outbox. */
  readonly savedEvents: HelpEvent[] = [];

  async findById(id: HelpId): Promise<Help | undefined> {
    const state = this.stored.find(id);
    return state === undefined ? undefined : Help.reconstitute(state);
  }

  async save(help: Help): Promise<void> {
    this.stored.write({
      id: help.id,
      helpRequestId: help.helpRequestId,
      helpRequester: help.helpRequester,
      answerTitle: help.answerTitle,
      provider: help.provider,
      answer: help.answer,
      status: help.status,
      version: help.version,
    });
    this.savedEvents.push(...help.pullEvents());
  }
}
