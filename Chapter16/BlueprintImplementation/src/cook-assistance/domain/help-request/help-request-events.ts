import type { HelpId } from "../help/help.js";
import type { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import type { HelpProviderType, HelpRequestId, HelpRequestStatus, HelpRequestTopic } from "./help-request.js";

// evt: Help requested · slices B2-S01, B1-S01 · AsyncAPI message HelpRequested (question → description, mapped in step 10)
export interface HelpRequested {
  readonly type: "HelpRequested";
  readonly helpRequestId: HelpRequestId;
  readonly requester: CookId;
  readonly title: string;
  readonly topic: HelpRequestTopic;
  readonly question: string;
  readonly recipeId: RecipeId | null;
  readonly howToStepId: HowToStepId | null;
  readonly addressees: readonly HelpProviderType[];
  readonly chefId: CookId | null;
  readonly plannedMealDate: string | null;
  readonly status: HelpRequestStatus;
  readonly requestedAt: Date;
  readonly answerDeadline: Date;
}

// evt: Chef review requested · slice B1-S02 · AsyncAPI message ChefReviewRequested
export interface ChefReviewRequested {
  readonly type: "ChefReviewRequested";
  readonly helpRequestId: HelpRequestId;
  readonly requester: CookId;
  readonly helpId: HelpId;
  readonly chefId: CookId;
  readonly requestedAt: Date;
}

// evt: Help request cancelled · slices B1-S06 (cook), B1-S07 (time) · AsyncAPI message HelpRequestCancelled
export interface HelpRequestCancelled {
  readonly type: "HelpRequestCancelled";
  readonly helpRequestId: HelpRequestId;
  readonly requester: CookId;
  readonly cancelledBy: "REQUESTER" | "SYSTEM";
  readonly status: HelpRequestStatus;
  readonly cancelledAt: Date;
}

// evt: Help request closed · slice B1-S08 (policy on Help accepted) · AsyncAPI message HelpRequestClosed
export interface HelpRequestClosed {
  readonly type: "HelpRequestClosed";
  readonly helpRequestId: HelpRequestId;
  readonly requester: CookId;
  readonly acceptedHelpId: HelpId;
  readonly status: HelpRequestStatus;
  readonly closedAt: Date;
}

// evt: Answer deadline restarted · slice B1-S10 (policy on Help rejected) · internal: no AsyncAPI message
export interface AnswerDeadlineRestarted {
  readonly type: "AnswerDeadlineRestarted";
  readonly helpRequestId: HelpRequestId;
  readonly answerDeadline: Date;
  readonly restartedAt: Date;
}

export type HelpRequestEvent = HelpRequested | ChefReviewRequested | HelpRequestCancelled | HelpRequestClosed | AnswerDeadlineRestarted;
