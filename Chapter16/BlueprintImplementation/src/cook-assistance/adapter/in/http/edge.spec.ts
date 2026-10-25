import { readdir, readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import type { INestApplication } from "@nestjs/common";
import request from "supertest";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { type HttpTestApp, startHttpTestApp } from "../../../test-support/http/start-http-test-app.js";
import { SERVED_OPERATIONS } from "./served-operations.js";

// Inbound HTTP — increment 0, step 9, unit 9.2: what the whole API shares (ADR-CA-006, ADR-CA-010, ADR-CA-011).

describe("HTTP edge", () => {
  let app: INestApplication;
  let asCook: HttpTestApp["asCook"];

  beforeEach(async () => {
    ({ app, asCook } = await startHttpTestApp());
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
