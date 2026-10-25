import type { INestApplication } from "@nestjs/common";
import request from "supertest";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { RequestPreparationStepHelp } from "../../../application/request-preparation-step-help.js";
import { HelpRequest } from "../../../domain/help-request/help-request.js";
import { FixedClock } from "../../../test-support/fakes/fixed-clock.js";
import { InMemoryRecipeCatalog } from "../../../test-support/fakes/in-memory-recipe-catalog.js";
import { InMemoryUnitOfWork } from "../../../test-support/fakes/in-memory-unit-of-work.js";
import { SequentialIdGenerator } from "../../../test-support/fakes/sequential-id-generator.js";
import { sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import { LocalIssuer } from "../../../test-support/http/local-issuer.js";
import { createHttpApp } from "./http-app.js";

// Inbound HTTP — increment 0, step 9, unit 9.1: op requestHelp (cook-assistance.openapi.yaml 2.0.0 r3) → RequestPreparationStepHelp.
// In-process HTTP against the real NestJS app with the step 8 fakes and a local JWKS; no database.

const BASE_URL = "https://larder.org/cooking-assistance";

/** The Cook asks what "warm" means in Sunday scones, step 3 — as the Larder app sends it. */
const askWhatWarmMeans = {
  title: sundaySconesStep3.title,
  topic: "PREPARATION_STEP",
  description: sundaySconesStep3.question,
  recipeId: sundaySconesStep3.recipeId,
  howToStepId: sundaySconesStep3.howToStepId,
  addressees: [...sundaySconesStep3.addressees],
};

describe("op requestHelp", () => {
  let app: INestApplication;
  let issuer: LocalIssuer;
  let unitOfWork: InMemoryUnitOfWork;
  let recipeCatalog: InMemoryRecipeCatalog;

  beforeEach(async () => {
    issuer = await LocalIssuer.start();
    unitOfWork = new InMemoryUnitOfWork();
    recipeCatalog = new InMemoryRecipeCatalog();
    recipeCatalog.addHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
    const requestPreparationStepHelp = new RequestPreparationStepHelp({
      unitOfWork,
      recipeCatalog,
      idGenerator: new SequentialIdGenerator([sundaySconesStep3.id]),
      clock: new FixedClock(sundaySconesStep3.requestedAt),
    });
    app = await createHttpApp({ cookAssistance: { requestPreparationStepHelp }, tokenVerifier: issuer.verifier, publicBaseUrl: BASE_URL });
  });

  afterEach(async () => {
    await app.close();
  });

  /** An RFC 9457 Problem as ADR-CA-006 defines it: type from the code, title = code, status, code, rule. */
  function expectProblem(response: request.Response, status: number, code: string, rule: string | null = null): void {
    expect(response.status).toBe(status);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({
      type: `https://larder.org/problems/cook-assistance/${code.replace(/(?<!^)([A-Z])/g, "-$1").toLowerCase()}`,
      title: code,
      status,
      code,
    });
    expect(response.body.rule ?? null).toBe(rule);
  }

  async function expectNothingSaved(): Promise<void> {
    expect(await unitOfWork.helpRequests.findById(sundaySconesStep3.id)).toBeUndefined();
    expect(unitOfWork.helpRequests.savedEvents).toEqual([]);
  }

  async function asCook() {
    return { Authorization: `Bearer ${await issuer.tokenFor({ sub: sundaySconesStep3.requester })}`, version: "2.0.0" };
  }

  it("a COOK with write scope asks about Sunday scones step 3: 201 with the HelpRequest body (generated id, requester = token sub, description = the question, pictures [], ingredientId/chefId/plannedMealDate null, REQUESTED, answerDeadline 15:00, createdAt = updatedAt = 10:00, selfLink, helpsLink) and Location /help-requests/{id} -- op requestHelp B2-S01", async () => {
    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(askWhatWarmMeans);

    expect(response.status).toBe(201);
    expect(response.headers.location).toBe(`${BASE_URL}/help-requests/0192f1a0-0000-7000-8000-000000000001`);
    expect(response.body).toEqual({
      helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
      requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
      title: "Sunday scones, step 3",
      topic: "PREPARATION_STEP",
      description: "What means 'warm'?",
      recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
      howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
      ingredientId: null,
      addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
      chefId: null,
      plannedMealDate: null,
      pictures: [],
      status: "REQUESTED",
      answerDeadline: "2026-10-24T15:00:00.000Z",
      createdAt: "2026-10-24T10:00:00.000Z",
      updatedAt: "2026-10-24T10:00:00.000Z",
      selfLink: `${BASE_URL}/help-requests/0192f1a0-0000-7000-8000-000000000001`,
      helpsLink: `${BASE_URL}/help-requests/0192f1a0-0000-7000-8000-000000000001/helps`,
    });
  });
  it("the use case receives the requester from the token's sub, never from the body -- op requestHelp ADR-CA-009", async () => {
    const someoneElse = "5f4e3d2c-1b0a-4f9e-8d7c-6b5a4f3e2d1c";

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, requester: someoneElse });

    expect(response.body.requester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
    expect((await unitOfWork.helpRequests.findById(sundaySconesStep3.id))?.requester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
  });
  it("title missing: 400 InvalidRequest Problem, use case not called, nothing saved -- op requestHelp", async () => {
    const { title: _omitted, ...withoutTitle } = askWhatWarmMeans;

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(withoutTitle);

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("topic DESSERT (not in the enum): 400 InvalidRequest Problem, nothing saved -- op requestHelp", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, topic: "DESSERT" });

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("recipeId not-a-uuid (format): 400 InvalidRequest Problem, nothing saved -- op requestHelp ADR-CA-011", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, recipeId: "not-a-uuid" });

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("version header missing: 400 InvalidRequest Problem, nothing saved -- op requestHelp", async () => {
    const { version: _omitted, ...withoutVersion } = await asCook();

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(withoutVersion).send(askWhatWarmMeans);

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("PREPARATION_STEP without howToStepId (conditional rule): 400 InvalidRequest Problem, use case not called -- op requestHelp INV-17 F9", async () => {
    const { howToStepId: _omitted, ...withoutStep } = askWhatWarmMeans;

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(withoutStep);

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("addressees []: 400 InvalidRequest Problem, use case not called -- op requestHelp INV-20 F9", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, addressees: [] });

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("a question of blanks: 422 Problem code QuestionRequired rule INV-19 -- op requestHelp INV-19", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, description: "   " });

    expectProblem(response, 422, "QuestionRequired", "INV-19");
    await expectNothingSaved();
  });
  it("step 9 of Sunday scones: 422 Problem code StepNotInRecipe rule INV-18 -- op requestHelp INV-18", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c09" });

    expectProblem(response, 422, "StepNotInRecipe", "INV-18");
    await expectNothingSaved();
  });
  it("addressees [CHEF] (schema: a Chef only for Menu): 400 InvalidRequest Problem, use case not called -- op requestHelp INV-01 INV-20 SADR0001 F9", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ ...askWhatWarmMeans, addressees: ["CHEF"] });

    expectProblem(response, 400, "InvalidRequest");
    await expectNothingSaved();
  });
  it("topic MENU: 422 Problem code TopicNotYetSupported rule null, use case not called -- op requestHelp F5", async () => {
    const dinnerForSix = {
      title: "Dinner planning",
      topic: "MENU",
      description: "Dinner for six on 30 October, one guest is vegetarian.",
      addressees: ["COMMUNITY"],
    };

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(dinnerForSix);

    expectProblem(response, 422, "TopicNotYetSupported");
    await expectNothingSaved();
  });
  it("a generated id that is already taken: 409 Problem code ConcurrencyConflict -- op requestHelp ADR-CA-005", async () => {
    await unitOfWork.helpRequests.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(askWhatWarmMeans);

    expectProblem(response, 409, "ConcurrencyConflict");
  });
  it("Recipe Catalog unavailable: 503 Problem code ServiceUnavailable with Retry-After -- op requestHelp ADR-CA-015", async () => {
    recipeCatalog.becomeUnavailable();

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(askWhatWarmMeans);

    expectProblem(response, 503, "ServiceUnavailable");
    expect(Number(response.headers["retry-after"])).toBeGreaterThanOrEqual(1);
    await expectNothingSaved();
  });
  it("no token: 401 Problem code Unauthenticated, use case not called -- op requestHelp ADR-CA-008", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ version: "2.0.0" })
      .send(askWhatWarmMeans);

    expectProblem(response, 401, "Unauthenticated");
    await expectNothingSaved();
  });
  it("no token and an invalid body (title missing): 401, not 400 — the caller is authenticated before the request is validated -- op requestHelp ADR-CA-009", async () => {
    const { title: _omitted, ...withoutTitle } = askWhatWarmMeans;

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ version: "2.0.0" })
      .send(withoutTitle);

    expectProblem(response, 401, "Unauthenticated");
  });
  it("a token signed by another key: 401 Problem code Unauthenticated -- op requestHelp ADR-CA-008", async () => {
    const impostor = await LocalIssuer.start();
    const forged = await impostor.tokenFor({ sub: sundaySconesStep3.requester });

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${forged}`, version: "2.0.0" })
      .send(askWhatWarmMeans);

    expectProblem(response, 401, "Unauthenticated");
    await expectNothingSaved();
  });
  it("an expired token: 401 Problem code Unauthenticated -- op requestHelp ADR-CA-008", async () => {
    const expired = await issuer.tokenFor({ sub: sundaySconesStep3.requester, expiresIn: -120 });

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${expired}`, version: "2.0.0" })
      .send(askWhatWarmMeans);

    expectProblem(response, 401, "Unauthenticated");
    await expectNothingSaved();
  });
  it("a token without cooking-assistance:write: 403 Problem code NotAuthorized, use case not called -- op requestHelp ADR-CA-009", async () => {
    const readOnly = await issuer.tokenFor({ sub: sundaySconesStep3.requester, scope: "cooking-assistance:read" });

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${readOnly}`, version: "2.0.0" })
      .send(askWhatWarmMeans);

    expectProblem(response, 403, "NotAuthorized");
    await expectNothingSaved();
  });
  it("a token with role COMMUNITY_COOK only: 403 Problem code NotAuthorized, use case not called -- op requestHelp ADR-CA-009", async () => {
    const helperOnly = await issuer.tokenFor({ sub: sundaySconesStep3.requester, roles: ["COMMUNITY_COOK"] });

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${helperOnly}`, version: "2.0.0" })
      .send(askWhatWarmMeans);

    expectProblem(response, 403, "NotAuthorized");
    await expectNothingSaved();
  });
  it("an unexpected error: 500 Problem code InternalError without the internal message -- op requestHelp ADR-CA-006", async () => {
    recipeCatalog.findHowToStep = async () => {
      throw new Error("connect ECONNREFUSED 10.0.0.5:5432 password=not-for-clients");
    };

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(await asCook()).send(askWhatWarmMeans);

    expectProblem(response, 500, "InternalError");
    expect(JSON.stringify(response.body)).not.toMatch(/ECONNREFUSED|10\.0\.0\.5|password/);
    await expectNothingSaved();
  });
});
