import type { HelpProviderType, HelpRequestId } from "../../domain/help-request/help-request.js";
import type { HelpId, HelpStatus, PreparationStepExplanation } from "../../domain/help/help.js";
import type { CookId } from "../../domain/shared/ids.js";
import type { Actor } from "../actor.js";
import type { Page, PageRequest } from "./page.js";

/** Read model "Help" (ADR-CA-012): what the requester (or the provider) sees of a help — plain values, no behavior. */
export interface HelpReadModel {
  readonly id: HelpId;
  readonly helpRequestId: HelpRequestId;
  readonly helpRequester: CookId;
  readonly answerTitle: string;
  readonly helpProviderType: HelpProviderType;
  readonly helpProvider: CookId | null;
  readonly answer: PreparationStepExplanation;
  readonly status: HelpStatus;
  readonly createdAt: Date;
  readonly updatedAt: Date;
}

/** Which helps listHelpsForHelpRequest asks for: those of one help request, by any status the spec allows (absent: all). */
export interface HelpFilter {
  readonly helpRequestId: HelpRequestId;
  readonly status?: string | undefined;
}

/** The help does not exist (or the caller may not see it, ADR-CA-009): answered with 404. */
export class HelpNotFound extends Error {
  constructor(readonly helpId: string) {
    super(`help ${helpId} not found`);
    this.name = "HelpNotFound";
  }
}

// ADR-CA-012 · the query port for the read model Help: never loads the aggregate.
export interface HelpQueries {
  /** The help as the viewer may see it (ADR-CA-009); undefined when it does not exist or is hidden from them. */
  get(id: HelpId, viewer: Actor): Promise<HelpReadModel | undefined>;
  /** One page of the helps given to a help request that the viewer may see, ordered by createdAt, id. */
  listForHelpRequest(filter: HelpFilter, page: PageRequest, viewer: Actor): Promise<Page<HelpReadModel>>;
}
