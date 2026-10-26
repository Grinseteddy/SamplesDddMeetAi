import type { ProvideHelpWhenGrandmaAvatarAnswers } from "./provide-help-when-grandma-avatar-answers.js";
import type { HelpQueries } from "./queries/help-queries.js";
import type { HelpRequestQueries } from "./queries/help-request-queries.js";
import type { RequestPreparationStepHelp } from "./request-preparation-step-help.js";

/** What Cook Assistance offers its inbound adapters: the use cases, the policy handlers and the query ports (ADR-CA-002). */
export interface CookAssistance {
  readonly requestPreparationStepHelp: RequestPreparationStepHelp;
  readonly provideHelpWhenGrandmaAvatarAnswers: ProvideHelpWhenGrandmaAvatarAnswers;
  /** ADR-CA-012 · the query side: GET operations read the views, never the aggregates. */
  readonly helpRequestQueries: HelpRequestQueries;
  readonly helpQueries: HelpQueries;
}
