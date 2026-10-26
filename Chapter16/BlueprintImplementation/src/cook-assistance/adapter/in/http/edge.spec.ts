import { readdir, readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import type { INestApplication } from "@nestjs/common";
import request from "supertest";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { type HttpTestApp, startHttpTestApp } from "../../../test-support/http/start-http-test-app.js";
import { sundaySconesStep3 } from "../../../test-support/fixtures/sunday-scones.js";
import { AUDIENCE, ISSUER } from "../../../test-support/http/local-issuer.js";
import { ensurePermitted, NotAuthorized } from "./authorization.js";
import { SERVED_OPERATIONS } from "./served-operations.js";
import { JoseTokenVerifier } from "./token-verifier.js";

/** Sunday scones step 3, as the cook sends it (op requestHelp). */
const sundaySconesBody = {
  title: sundaySconesStep3.title,
  topic: "PREPARATION_STEP",
  description: sundaySconesStep3.question,
  recipeId: sundaySconesStep3.recipeId,
  howToStepId: sundaySconesStep3.howToStepId,
  addressees: [...sundaySconesStep3.addressees],
};

// Inbound HTTP — increment 0, step 9, unit 9.2: what the whole API shares (ADR-CA-006, ADR-CA-010, ADR-CA-011).

describe("HTTP edge", () => {
  let app: INestApplication;
  let asCook: HttpTestApp["asCook"];
  let testApp: HttpTestApp;

  beforeEach(async () => {
    testApp = await startHttpTestApp();
    ({ app, asCook } = testApp);
  });

  afterEach(async () => {
    await app.close();
  });

  it("an undeclared path: 404 Problem code NotFound -- ADR-CA-006", async () => {
    const response = await request(app.getHttpServer())
      .get("/cooking-assistance/recipes")
      .set(await asCook());

    expect(response.status).toBe(404);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 404, code: "NotFound", title: "NotFound" });
  });
  it("DELETE /help-requests (undeclared method): 405 Problem with Allow: POST, GET -- ADR-CA-006 RFC 9110", async () => {
    const response = await request(app.getHttpServer())
      .delete("/cooking-assistance/help-requests")
      .set(await asCook());

    expect(response.status).toBe(405);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 405, code: "MethodNotAllowed" });
    expect(response.headers.allow?.split(/,\s*/).sort()).toEqual(["GET", "POST"]);
  });
  it("Content-Type text/plain: 415 Problem -- ADR-CA-006", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ ...(await asCook()), "Content-Type": "text/plain" })
      .send("What means 'warm'?");

    expect(response.status).toBe(415);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 415, code: "UnsupportedMediaType" });
  });
  it("a body that is not valid JSON: 400 Problem code InvalidRequest, not 500 -- ADR-CA-006", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ ...(await asCook()), "Content-Type": "application/json" })
      .send('{"title": "Sunday scones, step 3",');

    expect(response.status).toBe(400);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 400, code: "InvalidRequest" });
  });
  it("a body larger than 1 MB: 413 Problem -- ADR-CA-010", async () => {
    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send({ title: "Sunday scones, step 3", description: "x".repeat(1_100_000) });

    expect(response.status).toBe(413);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 413, code: "PayloadTooLarge" });
  });
  it("the 61st request within a minute from one cook: 429 Problem with Retry-After -- ADR-CA-010", async () => {
    // Menu requests reach the controller and change nothing (422 TopicNotYetSupported in increment 0)
    const dinnerForSix = { title: "Dinner planning", topic: "MENU", description: "Dinner for six.", addressees: ["COMMUNITY"] };
    const headers = await asCook();
    for (let sent = 0; sent < 60; sent += 1) {
      const allowed = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(headers).send(dinnerForSix);
      expect(allowed.status).toBe(422);
    }

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(headers).send(dinnerForSix);

    expect(response.status).toBe(429);
    expect(response.headers["content-type"]).toMatch(/^application\/problem\+json/);
    expect(response.body).toMatchObject({ status: 429, code: "TooManyRequests" });
    expect(Number(response.headers["retry-after"])).toBeGreaterThanOrEqual(1);
  });
  it("a request without a token: 401, and a security event access-refused with status, code, method, path and reason — no token, no cook id -- ADR-CA-010 QS-19 (inspection M-3)", async () => {
    const response = await request(app.getHttpServer()).get("/cooking-assistance/help-requests").set({ version: "2.0.0" });

    expect(response.status).toBe(401);
    expect(testApp.logged).toEqual([
      {
        event: "access-refused",
        status: 401,
        code: "Unauthenticated",
        method: "GET",
        path: "/cooking-assistance/help-requests",
        reason: "a bearer token is required",
      },
    ]);
  });
  it("a cook without the write scope on POST /help-requests: 403, and a security event access-refused naming the missing scope — no cook id -- ADR-CA-010 ADR-CA-009 QS-19 (inspection M-3)", async () => {
    const token = await testApp.issuer.tokenFor({ sub: sundaySconesStep3.requester, scope: "cooking-assistance:read" });

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${token}`, version: "2.0.0" })
      .send(sundaySconesBody);

    expect(response.status).toBe(403);
    expect(testApp.logged).toEqual([
      {
        event: "access-refused",
        status: 403,
        code: "NotAuthorized",
        method: "POST",
        path: "/cooking-assistance/help-requests",
        reason: "requestHelp needs the scope cooking-assistance:write",
      },
    ]);
    expect(JSON.stringify(testApp.logged)).not.toContain(sundaySconesStep3.requester);
  });
  it("an unexpected error: 500 Problem InternalError with a detail, and an internal-error event with method, path and the error's name -- ADR-CA-006 AP0007 (inspection m-6)", async () => {
    vi.spyOn(testApp.unitOfWork, "transactionally").mockRejectedValueOnce(new TypeError("the disk is on fire"));

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send(sundaySconesBody);

    expect(response.status).toBe(500);
    expect(response.body).toMatchObject({ status: 500, code: "InternalError", detail: "an unexpected error occurred" });
    expect(testApp.logged).toEqual([
      { event: "internal-error", method: "POST", path: "/cooking-assistance/help-requests", error: "TypeError" },
    ]);
  });
  it("every Problem names the request it is about: instance is the request's path -- ADR-CA-006 (inspection m-7)", async () => {
    const notFound = await request(app.getHttpServer())
      .get("/cooking-assistance/recipes")
      .set(await asCook());
    const unauthenticated = await request(app.getHttpServer()).get("/cooking-assistance/help-requests?status=REQUESTED");

    expect(notFound.body.instance).toBe("/cooking-assistance/recipes");
    expect(unauthenticated.body.instance).toBe("/cooking-assistance/help-requests");
  });
  it("a validly signed token without sub: 401 Unauthenticated, not an actor with an empty cook id -- ADR-CA-008 (inspection m-8)", async () => {
    const token = await testApp.issuer.tokenFor({ sub: undefined, scope: "cooking-assistance:read" });

    const response = await request(app.getHttpServer())
      .get("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${token}`, version: "2.0.0" });

    expect(response.status).toBe(401);
    expect(response.body).toMatchObject({ status: 401, code: "Unauthenticated" });
  });
  it("the 61st request within a minute from one address without a token: 429, not 401 — the limit counts before authentication -- ADR-CA-010 (inspection m-9)", async () => {
    for (let sent = 0; sent < 60; sent += 1) {
      const refused = await request(app.getHttpServer()).get("/cooking-assistance/help-requests");
      expect(refused.status).toBe(401);
    }

    const response = await request(app.getHttpServer()).get("/cooking-assistance/help-requests");

    expect(response.status).toBe(429);
    expect(Number(response.headers["retry-after"])).toBeGreaterThanOrEqual(1);
  });
  it("the 61st request within a minute from one cook with a body the spec refuses: 429, not 400 — the limit counts before validation -- ADR-CA-010 (inspection m-9)", async () => {
    const headers = await asCook();
    for (let sent = 0; sent < 60; sent += 1) {
      const invalid = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(headers).send({ title: 7 });
      expect(invalid.status).toBe(400);
    }

    const response = await request(app.getHttpServer()).post("/cooking-assistance/help-requests").set(headers).send({ title: 7 });

    expect(response.status).toBe(429);
  });
  it("signing keys that cannot be fetched are the service's problem: 500 InternalError and an internal-error event, not 401 -- ADR-CA-008 (AP0005 coverage)", async () => {
    const unreachable = new JoseTokenVerifier({
      issuer: ISSUER,
      audience: AUDIENCE,
      keys: async () => {
        throw new Error("JWKS unreachable");
      },
    });
    const withoutKeys = await startHttpTestApp({ tokenVerifier: unreachable });
    try {
      const response = await request(withoutKeys.app.getHttpServer())
        .get("/cooking-assistance/help-requests")
        .set(await withoutKeys.asCook());

      expect(response.status).toBe(500);
      expect(withoutKeys.logged).toEqual([
        { event: "internal-error", method: "GET", path: "/cooking-assistance/help-requests", error: "Error" },
      ]);
    } finally {
      await withoutKeys.app.close();
    }
  });
  it("a valid token without role and scope claims: no roles, no scopes — 403 on a read -- ADR-CA-008 ADR-CA-009 (AP0005 coverage)", async () => {
    const token = await testApp.issuer.tokenFor({ sub: sundaySconesStep3.requester, withoutClaims: true });

    const response = await request(app.getHttpServer())
      .get("/cooking-assistance/help-requests")
      .set({ Authorization: `Bearer ${token}`, version: "2.0.0" });

    expect(response.status).toBe(403);
    expect(response.body).toMatchObject({ code: "NotAuthorized", detail: "listHelpRequests needs the scope cooking-assistance:read" });
  });
  it("something thrown that is no Error: 500, and the event names its type -- ADR-CA-006 AP0007 (AP0005 coverage)", async () => {
    vi.spyOn(testApp.unitOfWork, "transactionally").mockRejectedValueOnce("a plain string");

    const response = await request(app.getHttpServer())
      .post("/cooking-assistance/help-requests")
      .set(await asCook())
      .send(sundaySconesBody);

    expect(response.status).toBe(500);
    expect(testApp.logged).toEqual([
      { event: "internal-error", method: "POST", path: "/cooking-assistance/help-requests", error: "string" },
    ]);
  });
  it("an operation without a declared permission is refused (deny by default): NotAuthorized naming no scope -- ADR-CA-009 (AP0005 coverage)", () => {
    const anyone = { cookId: sundaySconesStep3.requester, roles: ["COOK" as const], premium: false, scopes: ["cooking-assistance:write"] };

    expect(() => ensurePermitted(anyone, "cancelHelpRequest")).toThrow(
      new NotAuthorized("cancelHelpRequest needs the scope (none declared)"),
    );
  });
  it("security headers: no X-Powered-By, Content-Security-Policy default-src 'none', Strict-Transport-Security -- ADR-CA-010", async () => {
    const response = await request(app.getHttpServer())
      .get("/cooking-assistance/recipes")
      .set(await asCook());

    expect(response.headers["x-powered-by"]).toBeUndefined();
    expect(response.headers["content-security-policy"]).toBe("default-src 'none'");
    expect(response.headers["strict-transport-security"]).toMatch(/max-age=\d+/);
  });
  it("gate: every OpenAPI operation is served or on the planned list (SERVED_OPERATIONS satisfies the generated operations) -- ADR-CA-011", async () => {
    const spec = await readFile(
      fileURLToPath(new URL("../../../../../contracts/openapi/cook-assistance.openapi.yaml", import.meta.url)),
      "utf8",
    );
    const paths = spec.slice(0, spec.indexOf("\ncomponents:"));
    const operationIds = [...paths.matchAll(/^\s+operationId: (\w+)$/gm)].map((match) => match[1]);

    expect(Object.keys(SERVED_OPERATIONS).sort()).toEqual(operationIds.sort());
  });
  it("gate: no multipart handling or FileInterceptor in src (accepted multer risk GHSA-q7qp-rjg7-q26m) -- ADR-CA-010", async () => {
    const src = fileURLToPath(new URL("../../../../", import.meta.url));
    const files = (await readdir(src, { recursive: true })).filter((file) => file.endsWith(".ts") && !file.endsWith(".spec.ts"));
    const offending: string[] = [];
    for (const file of files) {
      if (
        /FileInterceptor|FilesInterceptor|AnyFilesInterceptor|\bmulter\b|multipart\/form-data/.test(await readFile(`${src}${file}`, "utf8"))
      ) {
        offending.push(file);
      }
    }

    expect(offending).toEqual([]);
  });
});
