import type { Kysely } from "kysely";
import {
  HelpRequest,
  HelpRequestId,
  type HelpProviderType,
  type HelpRequestStatus,
  type HelpRequestTopic,
} from "../../../domain/help-request/help-request.js";
import type { HelpRequestEvent, HelpRequested } from "../../../domain/help-request/help-request-events.js";
import type { HelpRequestRepository } from "../../../domain/help-request/help-request-repository.js";
import { CookId, HowToStepId, RecipeId } from "../../../domain/shared/ids.js";
import { recordEvents } from "./events.js";
import type { NewOutboxRow, OutboxTables } from "./outbox.js";
import { withinTransaction } from "./unit-of-work.js";
import { writeVersioned } from "./versioned-state.js";

interface HelpRequestRow {
  id: string;
  requester: string;
  title: string;
  topic: string;
  question: string;
  recipe_id: string | null;
  how_to_step_id: string | null;
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
    topic: row.topic as HelpRequestTopic,
    requester: CookId.of(row.requester),
    title: row.title,
    question: row.question,
    recipeId: row.recipe_id as RecipeId | null,
    howToStepId: row.how_to_step_id as HowToStepId | null,
    addressees: row.addressees as HelpProviderType[],
    // the Chef and the meal date get their columns in step 7 of increment 1 (Menu requests are not stored yet)
    chefId: null,
    plannedMealDate: null,
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

/** An event whose outbox mapping step 7 of this increment adds: refused, so nothing is stored half. */
export class EventNotStoredYet extends Error {
  constructor(readonly eventType: string) {
    super(`${eventType} is not stored yet (planned: increment 1, step 7)`);
    this.name = "EventNotStoredYet";
  }
}

// mapping: Help request event → outbox row, payload under the AsyncAPI message's field names (ADR-CA-004)
function toOutboxRow(event: HelpRequestEvent): NewOutboxRow {
  if (event.type !== "HelpRequested") {
    throw new EventNotStoredYet(event.type);
  }
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

  // ADR-CA-005 · versioned write (writeVersioned).
  // ADR-CA-004 · state, outbox rows and read models (ADR-CA-012) in one transaction.
  async save(helpRequest: HelpRequest): Promise<void> {
    // mapped before anything is written: an event that cannot be stored yet leaves the database untouched
    const outboxRows = helpRequest.pullEvents().map(toOutboxRow);
    await withinTransaction(this.db, async (tx) => {
      await writeVersioned(tx, {
        table: HELP_REQUEST_TABLE,
        aggregateName: "Help request",
        row: toHelpRequestRow(helpRequest),
        loadedVersion: helpRequest.version,
      });
      await recordEvents(tx, outboxRows);
    });
  }
}
