import type { Kysely } from "kysely";
import { KyselyHelpRequestQueries } from "./adapter/out/persistence/read-model/kysely-help-request-queries.js";
import { KyselyScopedUnitOfWork } from "./adapter/out/persistence/scoped-unit-of-work.js";
import { SystemClock } from "./adapter/out/system/system-clock.js";
import { UuidV7Generator } from "./adapter/out/system/uuid-v7-generator.js";
import type { Clock } from "./application/ports/clock.js";
import type { IdGenerator } from "./application/ports/id-generator.js";
import type { RecipeCatalog } from "./application/ports/recipe-catalog.js";
import type { HelpRequestQueries } from "./application/queries/help-request-queries.js";
import { ProvideHelpWhenGrandmaAvatarAnswers } from "./application/provide-help-when-grandma-avatar-answers.js";
import { RequestPreparationStepHelp } from "./application/request-preparation-step-help.js";

/** What Cook Assistance needs from outside; clock and id generator default to the production adapters. */
export interface CookAssistanceDependencies {
  readonly db: Kysely<unknown>;
  readonly recipeCatalog: RecipeCatalog;
  readonly clock?: Clock;
  readonly idGenerator?: IdGenerator;
}

/** The use cases and policy handlers, wired to their adapters (ADR-CA-002); step 9's NestJS module wraps this. */
export interface CookAssistance {
  readonly requestPreparationStepHelp: RequestPreparationStepHelp;
  readonly provideHelpWhenGrandmaAvatarAnswers: ProvideHelpWhenGrandmaAvatarAnswers;
  /** ADR-CA-012 · the query side: GET operations read the views, never the aggregates. */
  readonly helpRequestQueries: HelpRequestQueries;
}

export function composeCookAssistance({
  db,
  recipeCatalog,
  clock = new SystemClock(),
  idGenerator = new UuidV7Generator(),
}: CookAssistanceDependencies): CookAssistance {
  const unitOfWork = new KyselyScopedUnitOfWork(db);
  return {
    requestPreparationStepHelp: new RequestPreparationStepHelp({ unitOfWork, recipeCatalog, idGenerator, clock }),
    provideHelpWhenGrandmaAvatarAnswers: new ProvideHelpWhenGrandmaAvatarAnswers({ unitOfWork, clock }),
    helpRequestQueries: new KyselyHelpRequestQueries(db),
  };
}
