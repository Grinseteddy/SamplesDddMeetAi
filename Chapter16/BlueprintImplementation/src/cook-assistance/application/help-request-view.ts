import type { HelpProviderType, HelpRequestId, HelpRequestStatus, HelpRequestTopic } from "../domain/help-request/help-request.js";
import type { CookId, HowToStepId, RecipeId } from "../domain/shared/ids.js";

/** A help request as the application hands it to an inbound adapter: plain values, no aggregate behavior. */
export interface HelpRequestView {
  readonly id: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly topic: HelpRequestTopic;
  readonly question: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
  readonly status: HelpRequestStatus;
  readonly requestedAt: Date;
  readonly answerDeadline: Date;
}
