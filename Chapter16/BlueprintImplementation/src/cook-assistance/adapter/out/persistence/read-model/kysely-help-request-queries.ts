import { type Kysely, sql } from "kysely";
import type { Actor } from "../../../../application/actor.js";
import type { HelpRequestFilter, HelpRequestQueries, HelpRequestReadModel } from "../../../../application/queries/help-request-queries.js";
import type { Page, PageRequest } from "../../../../application/queries/page.js";
import { helperTypesOf } from "../../../../application/queries/visibility.js";
import {
  type HelpProviderType,
  HelpRequestId,
  type HelpRequestStatus,
  type HelpRequestTopic,
} from "../../../../domain/help-request/help-request.js";
import { CookId, type HowToStepId, type RecipeId } from "../../../../domain/shared/ids.js";

const HELP_REQUEST_VIEW = "cook_assistance.help_request_view";

interface HelpRequestViewRow {
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

interface ViewTables {
  [HELP_REQUEST_VIEW]: HelpRequestViewRow;
}

// mapping: view row → read model
function toReadModel(row: HelpRequestViewRow): HelpRequestReadModel {
  return {
    id: HelpRequestId.of(row.id),
    requester: CookId.of(row.requester),
    title: row.title,
    topic: row.topic as HelpRequestTopic,
    description: row.description,
    recipeId: row.recipe_id as RecipeId | null, // the view holds only ids the domain validated
    howToStepId: row.how_to_step_id as HowToStepId | null,
    addressees: row.addressees as HelpProviderType[],
    status: row.status as HelpRequestStatus,
    answerDeadline: row.answer_deadline,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
  };
}

/** ADR-CA-009 · a help request is visible to its requester and, while REQUESTED, to the helpers it addresses. */
function visibleTo(viewer: Actor) {
  return sql<boolean>`(requester = ${viewer.cookId} or (status = 'REQUESTED' and addressees && ${helperTypesOf(viewer)}::text[]))`;
}

// RM: Help request · ops getHelpRequestById, listHelpRequests (ADR-CA-012)
export class KyselyHelpRequestQueries implements HelpRequestQueries {
  private readonly db: Kysely<ViewTables>;

  constructor(db: Kysely<unknown>) {
    this.db = db as Kysely<ViewTables>;
  }

  async get(id: HelpRequestId, viewer: Actor): Promise<HelpRequestReadModel | undefined> {
    const row = await this.db
      .selectFrom(HELP_REQUEST_VIEW)
      .selectAll()
      .where("id", "=", id)
      .where(visibleTo(viewer))
      .executeTakeFirst();
    return row === undefined ? undefined : toReadModel(row);
  }

  async list(filter: HelpRequestFilter, { number, size }: PageRequest, viewer: Actor): Promise<Page<HelpRequestReadModel>> {
    // an absent filter does not filter
    const matching = this.db
      .selectFrom(HELP_REQUEST_VIEW)
      .where(visibleTo(viewer))
      .$if(filter.status !== undefined, (query) => query.where("status", "=", filter.status as string))
      .$if(filter.topic !== undefined, (query) => query.where("topic", "=", filter.topic as string))
      .$if(filter.addressee !== undefined, (query) => query.where(sql<boolean>`addressees @> ${[filter.addressee]}::text[]`)) // GIN index
      .$if(filter.requester !== undefined, (query) => query.where("requester", "=", filter.requester as string));
    const { count } = await matching.select(sql<string>`count(*)`.as("count")).executeTakeFirstOrThrow();
    const rows = await matching.selectAll().orderBy("created_at").orderBy("id").limit(size).offset(number * size).execute();
    const totalElements = Number(count);
    return { items: rows.map(toReadModel), number, size, totalElements, totalPages: Math.ceil(totalElements / size) };
  }
}
