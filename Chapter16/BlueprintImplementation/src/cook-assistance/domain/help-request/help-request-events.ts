import type { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import type { HelpProviderType, HelpRequestId, HelpRequestStatus } from "./help-request.js";

// evt: Help requested · slices B2-S01 · AsyncAPI message HelpRequested (question → description, mapped in step 10)
export interface HelpRequested {
  readonly type: "HelpRequested";
  readonly helpRequestId: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly topic: "PREPARATION_STEP";
  readonly question: string;
  readonly recipeId: RecipeId;
  readonly howToStepId: HowToStepId;
  readonly addressees: readonly HelpProviderType[];
  readonly status: HelpRequestStatus;
  readonly requestedAt: Date;
  readonly answerDeadline: Date;
}

export type HelpRequestEvent = HelpRequested;
