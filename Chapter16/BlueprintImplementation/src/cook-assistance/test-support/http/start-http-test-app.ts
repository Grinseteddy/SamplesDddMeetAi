import type { INestApplication } from "@nestjs/common";
import { createHttpApp } from "../../adapter/in/http/http-app.js";
import type { HelpRequestQueries } from "../../application/queries/help-request-queries.js";
import { RequestPreparationStepHelp } from "../../application/request-preparation-step-help.js";
import { FixedClock } from "../fakes/fixed-clock.js";
import { InMemoryRecipeCatalog } from "../fakes/in-memory-recipe-catalog.js";
import { InMemoryUnitOfWork } from "../fakes/in-memory-unit-of-work.js";
import { SequentialIdGenerator } from "../fakes/sequential-id-generator.js";
import { sundaySconesStep3 } from "../fixtures/sunday-scones.js";
import { LocalIssuer } from "./local-issuer.js";

export const PUBLIC_BASE_URL = "https://larder.org/cooking-assistance";

/** The command side's tests never query: any call is a visible bug (step 11). */
const NO_QUERIES: HelpRequestQueries = {
  get: () => {
    throw new Error("a command test called the query side");
  },
  list: () => {
    throw new Error("a command test called the query side");
  },
};

export interface HttpTestApp {
  readonly app: INestApplication;
  readonly issuer: LocalIssuer;
  readonly unitOfWork: InMemoryUnitOfWork;
  readonly recipeCatalog: InMemoryRecipeCatalog;
  /** Headers of the Sunday scones cook: a COOK token with write scope, and the contract version. */
  asCook(): Promise<Record<string, string>>;
}

/** The real HTTP adapter over the step 8 use case and its contract-checked fakes, with a local token issuer. */
export async function startHttpTestApp(): Promise<HttpTestApp> {
  const issuer = await LocalIssuer.start();
  const unitOfWork = new InMemoryUnitOfWork();
  const recipeCatalog = new InMemoryRecipeCatalog();
  recipeCatalog.addHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
  const requestPreparationStepHelp = new RequestPreparationStepHelp({
    unitOfWork,
    recipeCatalog,
    idGenerator: new SequentialIdGenerator([sundaySconesStep3.id]),
    clock: new FixedClock(sundaySconesStep3.requestedAt),
  });
  const app = await createHttpApp({
    cookAssistance: { requestPreparationStepHelp, helpRequestQueries: NO_QUERIES },
    tokenVerifier: issuer.verifier,
    publicBaseUrl: PUBLIC_BASE_URL,
  });
  return {
    app,
    issuer,
    unitOfWork,
    recipeCatalog,
    asCook: async () => ({ Authorization: `Bearer ${await issuer.tokenFor({ sub: sundaySconesStep3.requester })}`, version: "2.0.0" }),
  };
}
