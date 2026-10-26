import type { CookId } from "../shared/ids.js";
import type { HelpProviderType, HelpRequestId } from "../help-request/help-request.js";
import type { Answer, HelpId, HelpStatus } from "./help.js";

// evt: Help provided · slices B1-S03, B1-S04 · AsyncAPI message HelpProvided (links added by the mapper in step 10)
export interface HelpProvided {
  readonly type: "HelpProvided";
  readonly helpId: HelpId;
  readonly helpRequestId: HelpRequestId;
  readonly helpRequester: CookId;
  readonly answerTitle: string;
  readonly helpProviderType: HelpProviderType;
  readonly helpProvider: CookId | null;
  readonly answer: Answer;
  readonly status: HelpStatus;
  readonly providedAt: Date;
}

/** The requester's decision on a help, shared by acceptance and rejection (AsyncAPI HelpDecidedPayload). */
interface HelpDecided {
  readonly helpId: HelpId;
  readonly helpRequestId: HelpRequestId;
  readonly helpRequester: CookId;
  readonly helpProviderType: HelpProviderType;
  readonly helpProvider: CookId | null;
  readonly status: HelpStatus;
  readonly decidedAt: Date;
}

// evt: Help accepted · slice B1-S05 · AsyncAPI message HelpAccepted (HelpDecidedPayload)
export interface HelpAccepted extends HelpDecided {
  readonly type: "HelpAccepted";
}

// evt: Help rejected · slice B1-S09 · AsyncAPI message HelpRejected (HelpDecidedPayload)
export interface HelpRejected extends HelpDecided {
  readonly type: "HelpRejected";
}

export type HelpEvent = HelpProvided | HelpAccepted | HelpRejected;
