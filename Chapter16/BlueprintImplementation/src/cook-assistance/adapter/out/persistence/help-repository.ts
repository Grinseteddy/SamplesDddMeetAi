import type { Kysely, Transaction } from "kysely";
import type { HelpProviderType } from "../../../domain/help-request/help-request.js";
import { HelpRequestId } from "../../../domain/help-request/help-request.js";
import { Help, HelpId, type HelpStatus, type PreparationStepExplanation } from "../../../domain/help/help.js";
import type { HelpEvent, HelpProvided } from "../../../domain/help/help-events.js";
import type { HelpRepository } from "../../../domain/help/help-repository.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { CookId } from "../../../domain/shared/ids.js";
import { appendToOutbox, type NewOutboxRow, type OutboxTables } from "./outbox.js";
import { withinTransaction } from "./unit-of-work.js";

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
    answer: row.answer as PreparationStepExplanation,
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

// AsyncAPI message HelpProvided: timestamps are ISO strings; helpLink and helpRequestLink are added by the relay.
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

// mapping: Help event → outbox row (ADR-CA-004)
function toOutboxRow(event: HelpEvent): NewOutboxRow {
  return {
    aggregate_type: "Help",
    aggregate_id: event.helpId,
    event_type: event.type,
    payload: toHelpProvidedMessage(event),
    occurred_at: event.providedAt,
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

  // ADR-CA-005 · a new aggregate (version 0) is inserted as version 1; a loaded one is updated by compare-and-set.
  // ADR-CA-004 · state and outbox rows in one transaction, joining a unit of work when there is one.
  async save(help: Help): Promise<void> {
    await withinTransaction(this.db, async (tx) => {
      await this.writeState(tx, help);
      await appendToOutbox(tx, help.pullEvents().map(toOutboxRow));
    });
  }

  private async writeState(tx: Transaction<CookAssistanceTables>, help: Help): Promise<void> {
    const row = toHelpRow(help);
    if (help.version === 0) {
      await this.insertNew(tx, row);
    } else {
      await this.updateUnlessChangedMeanwhile(tx, help, row);
    }
  }

  private async insertNew(tx: Transaction<CookAssistanceTables>, row: HelpRow): Promise<void> {
    await tx.insertInto(HELP_TABLE).values({ ...row, version: 1 }).execute();
  }

  private async updateUnlessChangedMeanwhile(
    tx: Transaction<CookAssistanceTables>,
    help: Help,
    row: HelpRow,
  ): Promise<void> {
    const updated = await tx
      .updateTable(HELP_TABLE)
      .set({ ...row, version: help.version + 1 })
      .where("id", "=", help.id)
      .where("version", "=", help.version)
      .executeTakeFirstOrThrow(); // an UPDATE always yields one result (possibly 0 rows)
    if (updated.numUpdatedRows === 0n) {
      throw new ConcurrencyConflict("Help", help.id);
    }
  }
}
