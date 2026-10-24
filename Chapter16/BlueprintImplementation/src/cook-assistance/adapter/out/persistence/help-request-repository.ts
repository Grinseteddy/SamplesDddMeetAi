import type { Kysely, Transaction } from "kysely";
import {
  HelpRequest,
  HelpRequestId,
  type HelpProviderType,
  type HelpRequestStatus,
} from "../../../domain/help-request/help-request.js";
import type { HelpRequestEvent, HelpRequested } from "../../../domain/help-request/help-request-events.js";
import type { HelpRequestRepository } from "../../../domain/help-request/help-request-repository.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { CookId, HowToStepId, RecipeId } from "../../../domain/shared/ids.js";
import { appendToOutbox, type NewOutboxRow, type OutboxTables } from "./outbox.js";
import { withinTransaction } from "./unit-of-work.js";

interface HelpRequestRow {
  id: string;
  requester: string;
  title: string;
  topic: string;
  question: string;
  recipe_id: string;
  how_to_step_id: string;
  addressees: string[];
  status: string;
  answer_deadline: Date;
  version: number;
}

const HELP_REQUEST_TABLE = "cook_assistance.help_request";

interface CookAssistanceTables extends OutboxTables {
  [HELP_REQUEST_TABLE]: HelpRequestRow;
}

// mapping: stored row → Help request (always through reconstitute, ADR-CA-003)
function toHelpRequest(row: HelpRequestRow): HelpRequest {
  return HelpRequest.reconstitute({
    id: HelpRequestId.of(row.id),
    requester: CookId.of(row.requester),
    title: row.title,
    question: row.question,
    recipeId: RecipeId.of(row.recipe_id),
    howToStepId: HowToStepId.of(row.how_to_step_id),
    addressees: row.addressees as HelpProviderType[],
    status: row.status as HelpRequestStatus,
    answerDeadline: row.answer_deadline,
    version: row.version,
  });
}

// mapping: Help request → stored row
function toHelpRequestRow(helpRequest: HelpRequest): HelpRequestRow {
  return {
    id: helpRequest.id,
    requester: helpRequest.requester,
    title: helpRequest.title,
    topic: helpRequest.topic,
    question: helpRequest.question,
    recipe_id: helpRequest.recipeId,
    how_to_step_id: helpRequest.howToStepId,
    addressees: [...helpRequest.addressees],
    status: helpRequest.status,
    answer_deadline: helpRequest.answerDeadline,
    version: helpRequest.version,
  };
}

// mapping: Help request event → outbox row, payload under the AsyncAPI message's field names (ADR-CA-004)
function toOutboxRow(event: HelpRequestEvent): NewOutboxRow {
  return {
    aggregate_type: "HelpRequest",
    aggregate_id: event.helpRequestId,
    event_type: event.type,
    payload: toHelpRequestedMessage(event),
    occurred_at: event.requestedAt,
  };
}

// AsyncAPI message HelpRequested: the question is its description, timestamps are ISO strings, a preparation
// step has no pictures; helpRequestLink is added by the relay.
function toHelpRequestedMessage(event: HelpRequested): Record<string, unknown> {
  return {
    helpRequestId: event.helpRequestId,
    requester: event.requester,
    title: event.title,
    topic: event.topic,
    description: event.question,
    recipeId: event.recipeId,
    howToStepId: event.howToStepId,
    addressees: event.addressees,
    pictures: [],
    status: event.status,
    answerDeadline: event.answerDeadline.toISOString(),
    requestedAt: event.requestedAt.toISOString(),
  };
}

export class KyselyHelpRequestRepository implements HelpRequestRepository {
  private readonly db: Kysely<CookAssistanceTables>;

  constructor(db: Kysely<unknown>) {
    this.db = db as Kysely<CookAssistanceTables>;
  }

  async findById(id: HelpRequestId): Promise<HelpRequest | undefined> {
    const row = await this.db.selectFrom(HELP_REQUEST_TABLE).selectAll().where("id", "=", id).executeTakeFirst();
    if (row === undefined) {
      return undefined;
    }
    return toHelpRequest(row);
  }

  // ADR-CA-005 · a new aggregate (version 0) is inserted as version 1; a loaded one is updated by compare-and-set.
  // ADR-CA-004 · state and outbox rows in one transaction.
  async save(helpRequest: HelpRequest): Promise<void> {
    await withinTransaction(this.db, async (tx) => {
      await this.writeState(tx, helpRequest);
      await appendToOutbox(tx, helpRequest.pullEvents().map(toOutboxRow));
    });
  }

  private async writeState(tx: Transaction<CookAssistanceTables>, helpRequest: HelpRequest): Promise<void> {
    const row = toHelpRequestRow(helpRequest);
    if (helpRequest.version === 0) {
      await this.insertNew(tx, row);
    } else {
      await this.updateUnlessChangedMeanwhile(tx, helpRequest, row);
    }
  }

  private async insertNew(tx: Transaction<CookAssistanceTables>, row: HelpRequestRow): Promise<void> {
    await tx.insertInto(HELP_REQUEST_TABLE).values({ ...row, version: 1 }).execute();
  }

  private async updateUnlessChangedMeanwhile(
    tx: Transaction<CookAssistanceTables>,
    helpRequest: HelpRequest,
    row: HelpRequestRow,
  ): Promise<void> {
    const updated = await tx
      .updateTable(HELP_REQUEST_TABLE)
      .set({ ...row, version: helpRequest.version + 1 })
      .where("id", "=", helpRequest.id)
      .where("version", "=", helpRequest.version)
      .executeTakeFirstOrThrow(); // an UPDATE always yields one result (possibly 0 rows)
    if (updated.numUpdatedRows === 0n) {
      throw new ConcurrencyConflict("Help request", helpRequest.id);
    }
  }
}
