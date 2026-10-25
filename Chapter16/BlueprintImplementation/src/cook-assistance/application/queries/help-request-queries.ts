import type { HelpProviderType, HelpRequestId, HelpRequestStatus, HelpRequestTopic } from "../../domain/help-request/help-request.js";
import type { CookId, HowToStepId, RecipeId } from "../../domain/shared/ids.js";
import type { Actor } from "../actor.js";
import type { Page, PageRequest } from "./page.js";

/** Read model "Help request" (ADR-CA-012): what a cook or a helper sees of a help request — plain values, no behavior. */
export interface HelpRequestReadModel {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly topic: HelpRequestTopic;
  readonly description: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
  readonly status: HelpRequestStatus;
  readonly answerDeadline: Date;
  readonly createdAt: Date;
  readonly updatedAt: Date;
}

/**
 * The spec's filters of listHelpRequests; an absent one does not filter. Status and topic take every value the spec
 * allows — one the domain does not produce yet (CANCELLED, MENU in increment 0) matches nothing.
 */
export interface HelpRequestFilter {
  readonly status?: string | undefined;
  readonly topic?: string | undefined;
  readonly addressee?: HelpProviderType | undefined;
  readonly requester?: CookId | undefined;
}

/** The help request does not exist (or the caller may not see it, ADR-CA-009): answered with 404. */
export class HelpRequestNotFound extends Error {
  constructor(readonly helpRequestId: string) {
    super(`help request ${helpRequestId} not found`);
    this.name = "HelpRequestNotFound";
  }
}

// ADR-CA-012 · the query port: GET operations read here, never by loading an aggregate.
export interface HelpRequestQueries {
  /** The help request as the viewer may see it (ADR-CA-009); undefined when it does not exist or is hidden from them. */
  get(id: HelpRequestId, viewer: Actor): Promise<HelpRequestReadModel | undefined>;
  /** One page of the help requests the viewer may see, matching the filter, ordered by createdAt, id. */
  list(filter: HelpRequestFilter, page: PageRequest, viewer: Actor): Promise<Page<HelpRequestReadModel>>;
}

/** ADR-CA-009 · the help request the viewer may see; one that is hidden from them is not found, hiding its existence. */
export async function visibleHelpRequest(queries: HelpRequestQueries, id: HelpRequestId, viewer: Actor): Promise<HelpRequestReadModel> {
  const readModel = await queries.get(id, viewer);
  if (readModel === undefined) {
    throw new HelpRequestNotFound(id);
  }
  return readModel;
}
