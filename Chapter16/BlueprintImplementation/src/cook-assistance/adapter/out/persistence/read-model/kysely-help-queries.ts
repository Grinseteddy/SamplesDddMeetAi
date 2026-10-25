import { type Kysely, sql } from "kysely";
import type { Actor } from "../../../../application/actor.js";
import type { HelpFilter, HelpQueries, HelpReadModel } from "../../../../application/queries/help-queries.js";
import type { Page, PageRequest } from "../../../../application/queries/page.js";
import type { HelpProviderType, HelpRequestId } from "../../../../domain/help-request/help-request.js";
import type { HelpId, HelpStatus, PreparationStepExplanation } from "../../../../domain/help/help.js";
import type { CookId } from "../../../../domain/shared/ids.js";
import { HELP_VIEW, type HelpViewRow, type ViewTables } from "./projections.js";

// mapping: view row → read model (the view holds only values the domain validated)
function toReadModel(row: HelpViewRow): HelpReadModel {
  return {
    id: row.id as HelpId,
    helpRequestId: row.help_request_id as HelpRequestId,
    helpRequester: row.help_requester as CookId,
    answerTitle: row.answer_title,
    helpProviderType: row.provider_type as HelpProviderType,
    helpProvider: row.provider_id as CookId | null,
    answer: row.answer as PreparationStepExplanation,
    status: row.status as HelpStatus,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
  };
}

/** ADR-CA-009 · a help is visible to the requester of its request and to its own provider (a Chef's help: only those two). */
function visibleTo(viewer: Actor) {
  return sql<boolean>`(help_requester = ${viewer.cookId} or provider_id = ${viewer.cookId})`;
}

// RM: Help · ops getHelpById, listHelpsForHelpRequest (ADR-CA-012)
export class KyselyHelpQueries implements HelpQueries {
  private readonly db: Kysely<ViewTables>;

  constructor(db: Kysely<unknown>) {
    this.db = db as Kysely<ViewTables>;
  }

  async get(id: HelpId, viewer: Actor): Promise<HelpReadModel | undefined> {
    const row = await this.db.selectFrom(HELP_VIEW).selectAll().where("id", "=", id).where(visibleTo(viewer)).executeTakeFirst();
    return row === undefined ? undefined : toReadModel(row);
  }

  async listForHelpRequest(filter: HelpFilter, { number, size }: PageRequest, viewer: Actor): Promise<Page<HelpReadModel>> {
    const matching = this.db
      .selectFrom(HELP_VIEW)
      .where("help_request_id", "=", filter.helpRequestId)
      .where(visibleTo(viewer))
      .$if(filter.status !== undefined, (query) => query.where("status", "=", filter.status as string));
    const { count } = await matching.select(sql<string>`count(*)`.as("count")).executeTakeFirstOrThrow();
    const rows = await matching.selectAll().orderBy("created_at").orderBy("id").limit(size).offset(number * size).execute();
    const totalElements = Number(count);
    return { items: rows.map(toReadModel), number, size, totalElements, totalPages: Math.ceil(totalElements / size) };
  }
}
