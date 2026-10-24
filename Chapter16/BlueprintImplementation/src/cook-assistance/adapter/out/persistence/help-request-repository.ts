import type { Generated, Kysely, Transaction } from "kysely";
import {
  HelpRequest,
  HelpRequestId,
  type HelpProviderType,
  type HelpRequestStatus,
} from "../../../domain/help-request/help-request.js";
import type { HelpRequestEvent } from "../../../domain/help-request/help-request-events.js";
import type { HelpRequestRepository } from "../../../domain/help-request/help-request-repository.js";
import { ConcurrencyConflict } from "../../../domain/shared/concurrency-conflict.js";
import { CookId, HowToStepId, RecipeId } from "../../../domain/shared/ids.js";
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

interface OutboxRow {
  id: Generated<string>;
  aggregate_type: string;
  aggregate_id: string;
  event_type: string;
  payload: unknown;
  occurred_at: Date;
}

const HELP_REQUEST_TABLE = "cook_assistance.help_request";
const OUTBOX_TABLE = "cook_assistance.outbox";

interface Tables {
  [HELP_REQUEST_TABLE]: HelpRequestRow;
  [OUTBOX_TABLE]: OutboxRow;
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
function toOutboxRow(event: HelpRequestEvent): Omit<OutboxRow, "id"> {
  return {
    aggregate_type: "HelpRequest",
    aggregate_id: event.helpRequestId,
    event_type: event.type,
    payload: {
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
    },
    occurred_at: event.requestedAt,
  };
}

async function appendToOutbox(tx: Transaction<Tables>, events: readonly HelpRequestEvent[]): Promise<void> {
  for (const event of events) {
    await tx.insertInto(OUTBOX_TABLE).values(toOutboxRow(event)).execute();
  }
}

export class KyselyHelpRequestRepository implements HelpRequestRepository {
  private readonly db: Kysely<Tables>;

  constructor(db: Kysely<unknown>) {
    this.db = db as Kysely<Tables>;
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
      await appendToOutbox(tx, helpRequest.pullEvents());
    });
  }

  private async writeState(tx: Transaction<Tables>, helpRequest: HelpRequest): Promise<void> {
    const row = toHelpRequestRow(helpRequest);
    if (helpRequest.version === 0) {
      await this.insertNew(tx, row);
    } else {
      await this.updateUnlessChangedMeanwhile(tx, helpRequest, row);
    }
  }

  private async insertNew(tx: Transaction<Tables>, row: HelpRequestRow): Promise<void> {
    await tx.insertInto(HELP_REQUEST_TABLE).values({ ...row, version: 1 }).execute();
  }

  private async updateUnlessChangedMeanwhile(
    tx: Transaction<Tables>,
    helpRequest: HelpRequest,
    row: HelpRequestRow,
  ): Promise<void> {
    const updated = await tx
      .updateTable(HELP_REQUEST_TABLE)
      .set({ ...row, version: helpRequest.version + 1 })
      .where("id", "=", helpRequest.id)
      .where("version", "=", helpRequest.version)
      .executeTakeFirst();
    if (updated.numUpdatedRows === 0n) {
      throw new ConcurrencyConflict("Help request", helpRequest.id);
    }
  }
}
