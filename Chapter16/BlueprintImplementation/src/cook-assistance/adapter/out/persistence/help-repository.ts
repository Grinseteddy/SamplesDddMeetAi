import type { Kysely } from "kysely";
import type { HelpProviderType } from "../../../domain/help-request/help-request.js";
import { HelpRequestId } from "../../../domain/help-request/help-request.js";
import { type Answer, Help, HelpId, type HelpStatus } from "../../../domain/help/help.js";
import type { HelpAccepted, HelpEvent, HelpProvided, HelpRejected } from "../../../domain/help/help-events.js";
import type { HelpRepository } from "../../../domain/help/help-repository.js";
import { CookId } from "../../../domain/shared/ids.js";
import { recordEvents } from "./events.js";
import type { NewOutboxRow, OutboxTables } from "./outbox.js";
import { withinTransaction } from "./unit-of-work.js";
import { writeVersioned } from "./versioned-state.js";

interface HelpRow {
  id: string;
  help_request_id: string;
  help_requester: string;
  answer_title: string;
  provider_type: string;
  provider_id: string | null;
  answer: unknown;
  status: string;
  version: number;
}

const HELP_TABLE = "cook_assistance.help";

interface CookAssistanceTables extends OutboxTables {
  [HELP_TABLE]: HelpRow;
}

// mapping: stored row → Help (always through reconstitute, ADR-CA-003)
function toHelp(row: HelpRow): Help {
  return Help.reconstitute({
    id: HelpId.of(row.id),
    helpRequestId: HelpRequestId.of(row.help_request_id),
    helpRequester: CookId.of(row.help_requester),
    answerTitle: row.answer_title,
    provider: {
      type: row.provider_type as HelpProviderType,
      id: row.provider_id === null ? null : CookId.of(row.provider_id),
    },
    answer: row.answer as Answer,
    status: row.status as HelpStatus,
    version: row.version,
  });
}

// mapping: Help → stored row
function toHelpRow(help: Help): HelpRow {
  return {
    id: help.id,
    help_request_id: help.helpRequestId,
    help_requester: help.helpRequester,
    answer_title: help.answerTitle,
    provider_type: help.provider.type,
    provider_id: help.provider.id,
    answer: JSON.stringify(help.answer),
    status: help.status,
    version: help.version,
  };
}

// AsyncAPI message HelpProvided · B1-S03 (menu proposal), B1-S04 (preparation step): timestamps are ISO strings;
// helpLink and helpRequestLink are added by the relay.
function toHelpProvidedMessage(event: HelpProvided): Record<string, unknown> {
  return {
    helpId: event.helpId,
    helpRequestId: event.helpRequestId,
    helpRequester: event.helpRequester,
    answerTitle: event.answerTitle,
    helpProviderType: event.helpProviderType,
    helpProvider: event.helpProvider,
    answer: event.answer,
    status: event.status,
    providedAt: event.providedAt.toISOString(),
  };
}

// AsyncAPI HelpDecidedPayload (messages HelpAccepted, HelpRejected) · B1-S05, B1-S09: the requester's decision;
// links added by the relay
function toHelpDecidedMessage(event: HelpAccepted | HelpRejected): Record<string, unknown> {
  return {
    helpId: event.helpId,
    helpRequestId: event.helpRequestId,
    helpRequester: event.helpRequester,
    helpProviderType: event.helpProviderType,
    helpProvider: event.helpProvider,
    status: event.status,
    decidedAt: event.decidedAt.toISOString(),
  };
}

/** An event's payload under the AsyncAPI message's field names, and when it happened. */
interface StoredMessage {
  readonly payload: Record<string, unknown>;
  readonly occurredAt: Date;
}

// one mapping per event type of the Help (the switch is exhaustive: a new event fails type checking)
function messageOf(event: HelpEvent): StoredMessage {
  switch (event.type) {
    case "HelpProvided":
      return { payload: toHelpProvidedMessage(event), occurredAt: event.providedAt };
    case "HelpAccepted":
    case "HelpRejected":
      return { payload: toHelpDecidedMessage(event), occurredAt: event.decidedAt };
  }
}

// mapping: Help event → outbox row (ADR-CA-004)
function toOutboxRow(event: HelpEvent): NewOutboxRow {
  const { payload, occurredAt } = messageOf(event);
  return {
    aggregate_type: "Help",
    aggregate_id: event.helpId,
    event_type: event.type,
    payload,
    occurred_at: occurredAt,
  };
}

export class KyselyHelpRepository implements HelpRepository {
  private readonly db: Kysely<CookAssistanceTables>;

  constructor(db: Kysely<unknown>) {
    this.db = db as Kysely<CookAssistanceTables>;
  }

  async findById(id: HelpId): Promise<Help | undefined> {
    const row = await this.db.selectFrom(HELP_TABLE).selectAll().where("id", "=", id).executeTakeFirst();
    return row === undefined ? undefined : toHelp(row);
  }

  // ADR-CA-005 · versioned write (writeVersioned).
  // ADR-CA-004 · state, outbox rows and read models (ADR-CA-012) in one transaction, joining a unit of work when there is one.
  async save(help: Help): Promise<void> {
    // mapped before anything is written: an event that cannot be stored yet leaves the database untouched
    const outboxRows = help.pullEvents().map(toOutboxRow);
    await withinTransaction(this.db, async (tx) => {
      await writeVersioned(tx, {
        table: HELP_TABLE,
        aggregateName: "Help",
        row: toHelpRow(help),
        loadedVersion: help.version,
      });
      await recordEvents(tx, outboxRows);
    });
  }
}
