import type { INestApplication } from "@nestjs/common";
import request from "supertest";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { composeCookAssistance } from "../../../composition.js";
import { HelpRequest, HelpRequestId, type HelpProviderType } from "../../../domain/help-request/help-request.js";
import { CookId } from "../../../domain/shared/ids.js";
import { InMemoryRecipeCatalog } from "../../../test-support/fakes/in-memory-recipe-catalog.js";
import { sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import { LocalIssuer, type TokenClaims } from "../../../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../../../test-support/http/start-http-test-app.js";
import { KyselyHelpRequestRepository } from "../../out/persistence/help-request-repository.js";
import { migrateToLatest } from "../../out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "../../out/persistence/test-support/postgres-container.js";
import { resetSchema } from "../../out/persistence/test-support/reset-schema.js";
import { createHttpApp } from "./http-app.js";

// Query side — increment 0, step 11, unit 11.2: ops getHelpRequestById, listHelpRequests (cook-assistance.openapi.yaml 2.0.0)
// through the HTTP adapter, the query port and the real query adapter on PostgreSQL 16; visibility per ADR-CA-009.

/** Further help requests for the lists. */
const SECOND = "0192f1a0-0000-7000-8000-000000000002";
const OF_ANOTHER_COOK = "0192f1a0-0000-7000-8000-000000000003";
const THIRD = "0192f1a0-0000-7000-8000-000000000004";

/** A cook who did not ask for this help. */
const ANOTHER_COOK = "5f6a7b8c-9d0e-4f1a-8b2c-3d4e5f6a7b8c";
/** A community cook who helps others. */
const A_HELPER = "6a7b8c9d-0e1f-4a2b-8c3d-4e5f6a7b8c9d";

describe("ops getHelpRequestById, listHelpRequests — read model Help request", () => {
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
    const recipeCatalog = new InMemoryRecipeCatalog();
    recipeCatalog.addHowToStep(sundaySconesStep3.recipeId, sundaySconesStep3.howToStepId);
    const cookAssistance = composeCookAssistance({ db: database.db, recipeCatalog });
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

  /** Another Sunday scones step 3 request, by the given cook, at the given time. */
  async function saveRequest(
    id: string,
    requestedAt: string,
    requester: string = sundaySconesStep3.requester,
    addressees: readonly HelpProviderType[] = sundaySconesStep3.addressees,
  ): Promise<void> {
    await new KyselyHelpRequestRepository(database.db).save(
      HelpRequest.requestPreparationStepHelp({
        ...sundaySconesStep3,
        id: HelpRequestId.of(id),
        requester: CookId.of(requester),
        requestedAt: new Date(requestedAt),
        addressees,
      }),
    );
  }

  /** The cook's request, saved as the use case would (requested 10:00, deadline 15:00). */
  async function saveSundaySconesStep3(): Promise<void> {
    await new KyselyHelpRequestRepository(database.db).save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
  }

  it("GET /help-requests/{id} as the requester: 200 HelpRequest (all fields, pictures [], ingredientId/chefId/plannedMealDate null, REQUESTED, answerDeadline 15:00, createdAt = updatedAt = 10:00, selfLink, helpsLink) -- op getHelpRequestById", async () => {
    await saveSundaySconesStep3();

    const response = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}`)
      .set(await as({ sub: sundaySconesStep3.requester }));

    expect(response.status).toBe(200);
    expect(response.body).toEqual({
      helpRequestId: sundaySconesStep3.id,
      requester: sundaySconesStep3.requester,
      title: "Sunday scones, step 3",
      topic: "PREPARATION_STEP",
      description: "What means 'warm'?",
      recipeId: sundaySconesStep3.recipeId,
      howToStepId: sundaySconesStep3.howToStepId,
      ingredientId: null,
      addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
      chefId: null,
      plannedMealDate: null,
      pictures: [],
      status: "REQUESTED",
      answerDeadline: "2026-10-24T15:00:00.000Z",
      createdAt: "2026-10-24T10:00:00.000Z",
      updatedAt: "2026-10-24T10:00:00.000Z",
      selfLink: `${PUBLIC_BASE_URL}/help-requests/${sundaySconesStep3.id}`,
      helpsLink: `${PUBLIC_BASE_URL}/help-requests/${sundaySconesStep3.id}/helps`,
    });
  });
  it("an unknown id: 404 Problem -- op getHelpRequestById", async () => {
    const response = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}`)
      .set(await as({ sub: sundaySconesStep3.requester }));

    expect(response.status).toBe(404);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 404, title: "NotFound" });
  });
  it("another COOK who is not the requester: 404, the request stays hidden -- op getHelpRequestById ADR-CA-009", async () => {
    await saveSundaySconesStep3();

    const response = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests/${sundaySconesStep3.id}`)
      .set(await as({ sub: ANOTHER_COOK }));

    expect(response.status).toBe(404);
  });
  it("a COMMUNITY_COOK sees a REQUESTED request addressed to COMMUNITY (200); a CHEF not addressed gets 404 -- op getHelpRequestById ADR-CA-009", async () => {
    await saveSundaySconesStep3();
    const path = `/cooking-assistance/help-requests/${sundaySconesStep3.id}`;

    const communityCook = await request(app.getHttpServer())
      .get(path)
      .set(await as({ sub: ANOTHER_COOK, roles: ["COMMUNITY_COOK"] }));
    const chef = await request(app.getHttpServer())
      .get(path)
      .set(await as({ sub: ANOTHER_COOK, roles: ["CHEF"] }));

    expect([communityCook.status, chef.status]).toEqual([200, 404]);
  });
  it("no token: 401; a token without cooking-assistance:read: 403 -- op getHelpRequestById ADR-CA-009", async () => {
    await saveSundaySconesStep3();
    const path = `/cooking-assistance/help-requests/${sundaySconesStep3.id}`;

    const anonymous = await request(app.getHttpServer()).get(path).set({ version: "2.0.0" });
    const writeOnly = await request(app.getHttpServer())
      .get(path)
      .set({ Authorization: `Bearer ${await issuer.tokenFor({ sub: sundaySconesStep3.requester })}`, version: "2.0.0" });

    expect([anonymous.status, writeOnly.status]).toEqual([401, 403]);
  });
  it("GET /help-requests?requester={self}: the cook's requests ordered by createdAt, id; page { number 0, size 20, totalElements, totalPages } -- op listHelpRequests", async () => {
    await saveRequest(SECOND, "2026-10-24T11:00:00Z");
    await saveSundaySconesStep3();
    await saveRequest(OF_ANOTHER_COOK, "2026-10-24T10:30:00Z", ANOTHER_COOK);

    const response = await request(app.getHttpServer())
      .get(`/cooking-assistance/help-requests?requester=${sundaySconesStep3.requester}`)
      .set(await as({ sub: sundaySconesStep3.requester }));

    expect(response.status).toBe(200);
    expect(response.body.items.map((item: { helpRequestId: string }) => item.helpRequestId)).toEqual([sundaySconesStep3.id, SECOND]);
    expect(response.body.page).toEqual({ number: 0, size: 20, totalElements: 2, totalPages: 1 });
  });
  it("filters status, topic and addressee return only matching requests -- op listHelpRequests", async () => {
    await saveRequest(SECOND, "2026-10-24T11:00:00Z", sundaySconesStep3.requester, ["GRANDMA_AVATAR"]);
    await saveSundaySconesStep3();
    const headers = await as({ sub: sundaySconesStep3.requester });
    const idsFor = async (filter: string): Promise<string[]> => {
      const response = await request(app.getHttpServer()).get(`/cooking-assistance/help-requests?${filter}`).set(headers);
      return response.body.items.map((item: { helpRequestId: string }) => item.helpRequestId);
    };

    expect([await idsFor("addressee=COMMUNITY"), await idsFor("status=CANCELLED"), await idsFor("topic=MENU")]).toEqual([
      [sundaySconesStep3.id],
      [],
      [],
    ]);
  });
  it("size 2 over 3 requests: page 0 has 2, page 1 has 1, totalPages 2; a page past the end is 200 with no items -- op listHelpRequests", async () => {
    await saveSundaySconesStep3();
    await saveRequest(SECOND, "2026-10-24T11:00:00Z");
    await saveRequest(THIRD, "2026-10-24T12:00:00Z");
    const headers = await as({ sub: sundaySconesStep3.requester });
    const pageOf = async (number: number) => {
      const response = await request(app.getHttpServer()).get(`/cooking-assistance/help-requests?page=${number}&size=2`).set(headers);
      return {
        status: response.status,
        ids: response.body.items.map((item: { helpRequestId: string }) => item.helpRequestId),
        page: response.body.page,
      };
    };

    expect(await pageOf(0)).toEqual({
      status: 200,
      ids: [sundaySconesStep3.id, SECOND],
      page: { number: 0, size: 2, totalElements: 3, totalPages: 2 },
    });
    expect(await pageOf(1)).toEqual({ status: 200, ids: [THIRD], page: { number: 1, size: 2, totalElements: 3, totalPages: 2 } });
    expect(await pageOf(5)).toEqual({ status: 200, ids: [], page: { number: 5, size: 2, totalElements: 3, totalPages: 2 } });
  });
  it("a list shows a caller only what they may see: a COOK their own, a COMMUNITY_COOK the REQUESTED ones addressed to COMMUNITY -- op listHelpRequests ADR-CA-009", async () => {
    await saveSundaySconesStep3();
    await saveRequest(OF_ANOTHER_COOK, "2026-10-24T10:30:00Z", ANOTHER_COOK);
    await saveRequest(THIRD, "2026-10-24T12:00:00Z", ANOTHER_COOK, ["GRANDMA_AVATAR"]);
    const idsSeenBy = async (claims: TokenClaims): Promise<string[]> => {
      const response = await request(app.getHttpServer())
        .get("/cooking-assistance/help-requests")
        .set(await as(claims));
      return response.body.items.map((item: { helpRequestId: string }) => item.helpRequestId);
    };

    expect(await idsSeenBy({ sub: sundaySconesStep3.requester })).toEqual([sundaySconesStep3.id]);
    expect(await idsSeenBy({ sub: A_HELPER, roles: ["COMMUNITY_COOK"] })).toEqual([sundaySconesStep3.id, OF_ANOTHER_COOK]);
  });
  it("status OPEN or size 101: 400 Problem from the spec -- op listHelpRequests", async () => {
    const headers = await as({ sub: sundaySconesStep3.requester });

    const unknownStatus = await request(app.getHttpServer()).get("/cooking-assistance/help-requests?status=OPEN").set(headers);
    const tooLarge = await request(app.getHttpServer()).get("/cooking-assistance/help-requests?size=101").set(headers);

    expect([unknownStatus.status, tooLarge.status]).toEqual([400, 400]);
    expect(unknownStatus.headers["content-type"]).toMatch(/^application\/problem\+json/);
  });
  it("wiring: POST /help-requests, then GET /help-requests/{id} at once shows it (read-your-writes, lag 0) -- ADR-CA-012 QS-11", async () => {
    const created = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${await issuer.tokenFor({ sub: sundaySconesStep3.requester })}`, version: "2.0.0" })
      .send({
        title: sundaySconesStep3.title,
        topic: "PREPARATION_STEP",
        description: sundaySconesStep3.question,
        recipeId: sundaySconesStep3.recipeId,
        howToStepId: sundaySconesStep3.howToStepId,
        addressees: [...sundaySconesStep3.addressees],
      });

    const read = await request(app.getHttpServer())
      .get(new URL(created.body.selfLink).pathname)
      .set(await as({ sub: sundaySconesStep3.requester }));

    expect(created.status).toBe(201);
    expect(read.status).toBe(200);
    expect(read.body).toEqual(created.body);
  });
});
