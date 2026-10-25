import type { INestApplication } from "@nestjs/common";
import request from "supertest";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { composeCookAssistance } from "../../../composition.js";
import { HelpRequest } from "../../../domain/help-request/help-request.js";
import { Help } from "../../../domain/help/help.js";
import { InMemoryRecipeCatalog } from "../../../test-support/fakes/in-memory-recipe-catalog.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import { LocalIssuer, type TokenClaims } from "../../../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../../../test-support/http/start-http-test-app.js";
import { KyselyHelpRepository } from "../../out/persistence/help-repository.js";
import { KyselyHelpRequestRepository } from "../../out/persistence/help-request-repository.js";
import { migrateToLatest } from "../../out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "../../out/persistence/test-support/postgres-container.js";
import { resetSchema } from "../../out/persistence/test-support/reset-schema.js";
import { createHttpApp } from "./http-app.js";
import { SERVED_OPERATIONS } from "./served-operations.js";

// Query side — increment 0, step 11, unit 11.3: ops getHelpById, listHelpsForHelpRequest (cook-assistance.openapi.yaml 2.0.0)
// through the HTTP adapter, the query port and the real query adapter on PostgreSQL 16; visibility per ADR-CA-009.

/** A cook who neither asked for nor gave this help. */
const ANOTHER_COOK = "5f6a7b8c-9d0e-4f1a-8b2c-3d4e5f6a7b8c";

describe("ops getHelpById, listHelpsForHelpRequest — read model Help", () => {
  let database: TestDatabase;
  let issuer: LocalIssuer;
  let app: INestApplication;

  beforeAll(async () => {
    database = await startPostgres();
    await migrateToLatest(database.db);
    issuer = await LocalIssuer.start();
  }, 120_000);

  beforeEach(async () => {
    await resetSchema(database.db);
    const cookAssistance = composeCookAssistance({ db: database.db, recipeCatalog: new InMemoryRecipeCatalog() });
    app = await createHttpApp({ cookAssistance, tokenVerifier: issuer.verifier, publicBaseUrl: PUBLIC_BASE_URL });
  });

  afterEach(async () => {
    await app.close();
  });

  afterAll(async () => {
    await database.stop();
  });

  /** Headers of a caller with read scope (ADR-CA-009) and the contract version. */
  async function as(claims: TokenClaims): Promise<Record<string, string>> {
    return { Authorization: `Bearer ${await issuer.tokenFor({ scope: "cooking-assistance:read", ...claims })}`, version: "2.0.0" };
  }

  /** Sunday scones step 3, requested at 10:00, and Grandma's answer at 12:00 — saved as the use cases would. */
  async function saveRequestAndGrandmasHelp(): Promise<void> {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
    await new KyselyHelpRepository(database.db).save(Help.provide(grandmaExplainsWarm));
  }

  it("GET /helps/{id} as the requester: 200 Help (answer PREPARATION_STEP with images [], helpProviderType GRANDMA_AVATAR, helpProvider null, PROVIDED, createdAt = updatedAt, selfLink, helpRequestLink) -- op getHelpById", async () => {
    await saveRequestAndGrandmasHelp();

    const response = await request(app.getHttpServer())
      .get(`/cooking-assistance/helps/${grandmaExplainsWarm.id}`)
      .set(await as({ sub: sundaySconesStep3.requester }));

    expect(response.status).toBe(200);
    expect(response.body).toEqual({
      helpId: grandmaExplainsWarm.id,
      helpRequestId: sundaySconesStep3.id,
      helpRequester: sundaySconesStep3.requester,
      answerTitle: "Warm means about 30 °C",
      helpProviderType: "GRANDMA_AVATAR",
      helpProvider: null,
      answer: {
        topic: "PREPARATION_STEP",
        recipeId: sundaySconesStep3.recipeId,
        howToStepId: sundaySconesStep3.howToStepId,
        description: "Put the bowl next to the heater or in the oven with only the light on.",
        images: [],
      },
      status: "PROVIDED",
      createdAt: "2026-10-24T12:00:00.000Z",
      updatedAt: "2026-10-24T12:00:00.000Z",
      selfLink: `${PUBLIC_BASE_URL}/helps/${grandmaExplainsWarm.id}`,
      helpRequestLink: `${PUBLIC_BASE_URL}/help-requests/${sundaySconesStep3.id}`,
    });
  });
  it("an unknown id, or a cook who is neither requester nor provider: 404 Problem -- op getHelpById ADR-CA-009", async () => {
    const unknown = await request(app.getHttpServer())
      .get(`/cooking-assistance/helps/${grandmaExplainsWarm.id}`)
      .set(await as({ sub: sundaySconesStep3.requester }));
    await saveRequestAndGrandmasHelp();
    const otherCook = await request(app.getHttpServer())
      .get(`/cooking-assistance/helps/${grandmaExplainsWarm.id}`)
      .set(await as({ sub: ANOTHER_COOK, roles: ["COOK", "COMMUNITY_COOK"] }));

    expect([unknown.status, otherCook.status]).toEqual([404, 404]);
  });
  it("GET /help-requests/{id}/helps as the requester: a page with Grandma's help -- op listHelpsForHelpRequest", async () => {
    await saveRequestAndGrandmasHelp();
    const headers = await as({ sub: sundaySconesStep3.requester });

    const response = await request(app.getHttpServer()).get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}/helps`).set(headers);
    const single = await request(app.getHttpServer()).get(`/cooking-assistance/helps/${grandmaExplainsWarm.id}`).set(headers);

    expect(response.status).toBe(200);
    expect(response.body).toEqual({ items: [single.body], page: { number: 0, size: 20, totalElements: 1, totalPages: 1 } });
  });
  it("filter status ACCEPTED: 200 with no items -- op listHelpsForHelpRequest", async () => {
    await saveRequestAndGrandmasHelp();

    const response = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}/helps?status=ACCEPTED`)
      .set(await as({ sub: sundaySconesStep3.requester }));

    expect(response.status).toBe(200);
    expect(response.body.items).toEqual([]);
  });
  it("an unknown help request, or one the caller may not see: 404 Problem -- op listHelpsForHelpRequest ADR-CA-009", async () => {
    const unknown = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}/helps`)
      .set(await as({ sub: sundaySconesStep3.requester }));
    await saveRequestAndGrandmasHelp();
    const hidden = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}/helps`)
      .set(await as({ sub: ANOTHER_COOK }));

    expect([unknown.status, hidden.status]).toEqual([404, 404]);
  });
  it("gate: all four GET operations are served; five stay planned for increment 1 -- ADR-CA-011", () => {
    const planned = Object.entries(SERVED_OPERATIONS)
      .filter(([, how]) => how.startsWith("planned"))
      .map(([operation]) => operation)
      .sort();

    expect(planned).toEqual(["acceptHelp", "cancelHelpRequest", "provideHelp", "rejectHelp", "requestChefReview"]);
  });
});
