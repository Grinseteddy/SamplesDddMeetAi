import type { CookId } from "../shared/ids.js";
import type { HelpProviderType, HelpRequestId } from "../help-request/help-request.js";
import type { HelpId, HelpStatus, PreparationStepExplanation } from "./help.js";

// evt: Help provided · slices B1-S03, B1-S04 · AsyncAPI message HelpProvided (links added by the mapper in step 10)
export interface HelpProvided {
  readonly type: "HelpProvided";
  readonly helpId: HelpId;
  readonly helpRequestId: HelpRequestId;
  readonly helpRequester: CookId;
  readonly answerTitle: string;
  readonly helpProviderType: HelpProviderType;
  readonly helpProvider: CookId | null;
  readonly answer: PreparationStepExplanation;
  readonly status: HelpStatus;
  readonly providedAt: Date;
}

export type HelpEvent = HelpProvided;
