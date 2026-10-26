import type { Kysely } from "kysely";
import {
  HelpRequest,
  HelpRequestId,
  type HelpProviderType,
  type HelpRequestStatus,
  type HelpRequestTopic,
} from "../../../domain/help-request/help-request.js";
import type {
  AnswerDeadlineRestarted,
  ChefReviewRequested,
  HelpRequestCancelled,
  HelpRequestClosed,
  HelpRequestEvent,
  HelpRequested,
} from "../../../domain/help-request/help-request-events.js";
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
  chef_id: string | null;
  /** pg reads a SQL date as a Date at local midnight; it is written as the calendar date "YYYY-MM-DD" */
  planned_meal_date: Date | string | null;
  status: string;
  answer_deadline: Date;
  version: number;
}

const HELP_REQUEST_TABLE = "cook_assistance.help_request";

interface CookAssistanceTables extends OutboxTables {
  [HELP_REQUEST_TABLE]: HelpRequestRow;
}

/** The calendar date of a SQL date as pg reads it (a Date at local midnight), in the domain's "YYYY-MM-DD". */
function calendarDateOf(stored: Date | string | null): string | null {
  if (!(stored instanceof Date)) {
    return stored;
  }
  const pad = (part: number) => String(part).padStart(2, "0");
  return `${stored.getFullYear()}-${pad(stored.getMonth() + 1)}-${pad(stored.getDate())}`;
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
    chefId: row.chef_id as CookId | null,
    plannedMealDate: calendarDateOf(row.planned_meal_date),
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
    chef_id: helpRequest.chefId,
    planned_meal_date: helpRequest.plannedMealDate,
    status: helpRequest.status,
    answer_deadline: helpRequest.answerDeadline,
    version: helpRequest.version,
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
    chefId: event.chefId,
    plannedMealDate: event.plannedMealDate,
    pictures: [],
    status: event.status,
    answerDeadline: event.answerDeadline.toISOString(),
    requestedAt: event.requestedAt.toISOString(),
  };
}

// AsyncAPI message HelpRequestCancelled · B1-S06 (cook), B1-S07 (time)
function toHelpRequestCancelledMessage(event: HelpRequestCancelled): Record<string, unknown> {
  return {
    helpRequestId: event.helpRequestId,
    requester: event.requester,
    cancelledBy: event.cancelledBy,
    status: event.status,
    cancelledAt: event.cancelledAt.toISOString(),
  };
}

// AsyncAPI message ChefReviewRequested · B1-S02
function toChefReviewRequestedMessage(event: ChefReviewRequested): Record<string, unknown> {
  return {
    helpRequestId: event.helpRequestId,
    requester: event.requester,
    helpId: event.helpId,
    chefId: event.chefId,
    requestedAt: event.requestedAt.toISOString(),
  };
}

// internal event (no AsyncAPI message) · B1-S10: the read model and a rebuild need the new deadline
function toAnswerDeadlineRestartedPayload(event: AnswerDeadlineRestarted): Record<string, unknown> {
  return {
    helpRequestId: event.helpRequestId,
    answerDeadline: event.answerDeadline.toISOString(),
    restartedAt: event.restartedAt.toISOString(),
  };
}

// AsyncAPI message HelpRequestClosed · B1-S08 (acceptedHelpLink and helpRequestLink are added by the relay)
function toHelpRequestClosedMessage(event: HelpRequestClosed): Record<string, unknown> {
  return {
    helpRequestId: event.helpRequestId,
    requester: event.requester,
    acceptedHelpId: event.acceptedHelpId,
    status: event.status,
    closedAt: event.closedAt.toISOString(),
  };
}

/** An event's payload under the AsyncAPI message's field names, and when it happened. */
interface StoredMessage {
  readonly payload: Record<string, unknown>;
  readonly occurredAt: Date;
}

// one mapping per event type of the Help request (the switch is exhaustive: a new event fails type checking)
function messageOf(event: HelpRequestEvent): StoredMessage {
  switch (event.type) {
    case "HelpRequested":
      return { payload: toHelpRequestedMessage(event), occurredAt: event.requestedAt };
    case "HelpRequestCancelled":
      return { payload: toHelpRequestCancelledMessage(event), occurredAt: event.cancelledAt };
    case "ChefReviewRequested":
      return { payload: toChefReviewRequestedMessage(event), occurredAt: event.requestedAt };
    case "AnswerDeadlineRestarted":
      return { payload: toAnswerDeadlineRestartedPayload(event), occurredAt: event.restartedAt };
    case "HelpRequestClosed":
      return { payload: toHelpRequestClosedMessage(event), occurredAt: event.closedAt };
  }
}

// mapping: Help request event → outbox row, payload under the AsyncAPI message's field names (ADR-CA-004)
function toOutboxRow(event: HelpRequestEvent): NewOutboxRow {
  const { payload, occurredAt } = messageOf(event);
  return {
    aggregate_type: "HelpRequest",
    aggregate_id: event.helpRequestId,
    event_type: event.type,
    payload,
    occurred_at: occurredAt,
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
