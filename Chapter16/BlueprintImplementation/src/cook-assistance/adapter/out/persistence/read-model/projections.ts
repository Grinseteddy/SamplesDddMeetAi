import { type Kysely, sql, type Transaction } from "kysely";
import type { DomainEvent } from "../../../../domain/domain-events.js";
import type { NewOutboxRow } from "../outbox.js";

// ADR-CA-012 (amended) · the read models, projected from outbox records: in the save transaction, and again on a rebuild.

// the read models' schema (migration 006_read_models), written here and read by the query adapters
export const HELP_REQUEST_VIEW = "cook_assistance.help_request_view";
export const HELP_VIEW = "cook_assistance.help_view";

export interface HelpRequestViewRow {
  id: string;
  requester: string;
  title: string;
  topic: string;
  description: string;
  recipe_id: string | null;
  how_to_step_id: string | null;
  addressees: string[];
  status: string;
  answer_deadline: Date;
  created_at: Date;
  updated_at: Date;
}

export interface HelpViewRow {
  id: string;
  help_request_id: string;
  help_requester: string;
  answer_title: string;
  provider_type: string;
  provider_id: string | null;
  answer: unknown;
  status: string;
  created_at: Date;
  updated_at: Date;
}

export interface ViewTables {
  [HELP_REQUEST_VIEW]: HelpRequestViewRow;
  [HELP_VIEW]: HelpViewRow;
}

/** An outbox row whose event type has no projection decision: a rebuild stops rather than skip it (ADR-CA-012). */
export class UnhandledEvent extends Error {
  constructor(readonly eventType: string) {
    super(`no projection decision for event type ${eventType}`);
    this.name = "UnhandledEvent";
  }
}

/** One outbox record as a projection reads it: the event's type, its payload under the message's field names, when it happened. */
export type ProjectedRecord = Pick<NewOutboxRow, "event_type" | "payload" | "occurred_at">;

type Projection = (views: Transaction<ViewTables>, record: ProjectedRecord) => Promise<void>;

// RM: Help request · evt: HelpRequested (AsyncAPI message HelpRequested)
async function projectHelpRequested(views: Transaction<ViewTables>, { payload, occurred_at }: ProjectedRecord): Promise<void> {
  const message = payload as Record<string, unknown>;
  await views
    .insertInto(HELP_REQUEST_VIEW)
    .values({
      id: String(message.helpRequestId),
      requester: String(message.requester),
      title: String(message.title),
      topic: String(message.topic),
      description: String(message.description),
      recipe_id: String(message.recipeId),
      how_to_step_id: String(message.howToStepId),
      addressees: message.addressees as string[],
      status: String(message.status),
      answer_deadline: new Date(String(message.answerDeadline)),
      created_at: occurred_at,
      updated_at: occurred_at,
    })
    .execute();
}

// RM: Help · evt: HelpProvided (AsyncAPI message HelpProvided)
async function projectHelpProvided(views: Transaction<ViewTables>, { payload, occurred_at }: ProjectedRecord): Promise<void> {
  const message = payload as Record<string, unknown>;
  await views
    .insertInto(HELP_VIEW)
    .values({
      id: String(message.helpId),
      help_request_id: String(message.helpRequestId),
      help_requester: String(message.helpRequester),
      answer_title: String(message.answerTitle),
      provider_type: String(message.helpProviderType),
      provider_id: message.helpProvider as string | null,
      answer: JSON.stringify(message.answer),
      status: String(message.status),
      created_at: occurred_at,
      updated_at: occurred_at,
    })
    .execute();
}

/** Every event type the domain raises: a new one fails type checking until it has a projection decision. */
type DomainEventType = DomainEvent["type"];

/** Domain events whose projection decision a later step of this increment makes — each names that step. */
export const PLANNED_PROJECTIONS = {
  ChefReviewRequested: "planned: increment 1, step 11 (B1-S02)",
  HelpRequestCancelled: "planned: increment 1, step 11 (B1-S06, B1-S07)",
  HelpRequestClosed: "planned: increment 1, step 11 (B1-S08)",
  AnswerDeadlineRestarted: "planned: increment 1, step 11 (B1-S10)",
  HelpAccepted: "planned: increment 1, step 11 (B1-S05)",
  HelpRejected: "planned: increment 1, step 11 (B1-S09)",
} as const satisfies Partial<Record<DomainEventType, string>>;

// ADR-CA-012 (amended) · one projection decision per domain event type, or a planned entry above
export const PROJECTIONS = {
  HelpRequested: projectHelpRequested,
  HelpProvided: projectHelpProvided,
} satisfies Record<Exclude<DomainEventType, keyof typeof PLANNED_PROJECTIONS>, Projection>;

/** A stored event type is just a string: the table, looked up by it. */
const PROJECTION_OF: Readonly<Record<string, Projection>> = PROJECTIONS;

/** Applies the records to the read models in the caller's transaction. */
export async function project<DB>(tx: Transaction<DB>, records: readonly ProjectedRecord[]): Promise<void> {
  const views = tx as unknown as Transaction<ViewTables>;
  for (const record of records) {
    const projection = PROJECTION_OF[record.event_type];
    if (projection === undefined) {
      throw new UnhandledEvent(record.event_type); // never a silent skip
    }
    await projection(views, record);
  }
}

/** ADR-CA-012 (amended) · empties the read models and replays the outbox through the same projections. */
export async function rebuildViews(db: Kysely<unknown>): Promise<void> {
  await db.transaction().execute(async (tx) => {
    await sql`truncate ${sql.table(HELP_REQUEST_VIEW)}, ${sql.table(HELP_VIEW)}`.execute(tx);
    const history = await sql<ProjectedRecord>`select event_type, payload, occurred_at from cook_assistance.outbox order by id`.execute(tx);
    await project(tx, history.rows);
  });
}
