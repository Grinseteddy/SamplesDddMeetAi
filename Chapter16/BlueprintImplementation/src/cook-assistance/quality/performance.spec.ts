import { execFile } from "node:child_process";
import { randomUUID } from "node:crypto";
import { appendFile, chmod, mkdir, readFile, writeFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { promisify } from "node:util";
import amqp, { type Channel, type ChannelModel } from "amqplib";
import { sql } from "kysely";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { stepOfSundayScones, stepPath, SUNDAY_SCONES } from "../acceptance/support/sunday-scones-recipe.js";
import { TOPOLOGY } from "../adapter/out/messaging/rabbitmq.js";
import { startRabbitMq, type TestBroker } from "../adapter/out/messaging/test-support/rabbitmq-container.js";
import { migrateToLatest } from "../adapter/out/persistence/migrations.js";
import { rebuildViews } from "../adapter/out/persistence/read-model/projections.js";
import { resetSchema } from "../adapter/out/persistence/test-support/reset-schema.js";
import { startPostgres, type TestDatabase } from "../adapter/out/persistence/test-support/postgres-container.js";
import { eventually } from "../acceptance/support/driver.js";
import { type RunningService, startService } from "../service.js";
import { AUDIENCE, ISSUER, LocalIssuer } from "../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../test-support/http/start-http-test-app.js";
import { StubRecipeCatalog } from "../test-support/http/stub-recipe-catalog.js";
import { fingerprintOfViews, seedOneYearOfData } from "./support/one-year-of-data.js";

// Quality scenarios — increment 0, step 12, unit 12.5d: performance and capacity on the running service, real
// PostgreSQL 16 and RabbitMQ 3.13 (Testcontainers), load from k6 2.3.0 (Docker). Results are indicative: a laptop,
// runs of minutes instead of a peak hour.

const K6 = "grafana/k6:2.3.0";
const LOAD_SCRIPTS = fileURLToPath(new URL("../../../tools/load", import.meta.url));
const OUT = fileURLToPath(new URL("../../../reports/load", import.meta.url));
/** ADR-CA-010 allows 60 requests per minute and cook: 10 requests/s come from many cooks, as in QS-08. */
const COOK_COUNT = 100;

/** What the spec reads from k6's end-of-test summary. */
interface K6Summary {
  metrics: Record<string, { values: Record<string, number> }>;
}

let database: TestDatabase;
let broker: TestBroker;
let upstream: StubRecipeCatalog;
let issuer: LocalIssuer;
let connection: ChannelModel;
let channel: Channel;
let service: RunningService;
let cooks: { cookId: string; token: string }[];

beforeAll(async () => {
  [database, broker, upstream, issuer] = await Promise.all([
    startPostgres(),
    startRabbitMq(),
    StubRecipeCatalog.start(),
    LocalIssuer.start(),
  ]);
  await migrateToLatest(database.db);
  connection = await amqp.connect(broker.url);
  // the Grandma Avatar AI context owns and declares its exchange; the test stands in for it
  channel = await connection.createChannel();
  await channel.assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
  upstream.answerAt(stepPath(3), 200, {
    howToStepId: stepOfSundayScones(3),
    sequenceNumber: 3,
    description: "Let dough rest in a warm place.",
  });
  service = await startService(
    {
      databaseUrl: database.url,
      amqpUrl: broker.url,
      oidcIssuer: ISSUER,
      oidcAudience: AUDIENCE,
      publicBaseUrl: PUBLIC_BASE_URL,
      port: 0,
      recipeCatalogUrl: upstream.baseUrl,
    },
    { tokenVerifier: issuer.verifier, recipeCatalogTokens: { accessToken: async () => "token" }, log: () => {} },
  );
  cooks = await Promise.all(
    Array.from({ length: COOK_COUNT }, async () => {
      const cookId = randomUUID();
      const scope = "cooking-assistance:read cooking-assistance:write";
      return { cookId, token: await issuer.tokenFor({ sub: cookId, scope, expiresIn: 3600 }) };
    }),
  );
  await mkdir(OUT, { recursive: true });
  // k6 runs as its own non-root user: on a Linux runner the checkout belongs to another user, so k6 could not write
  // its summaries here (the first load job on GitHub failed with ENOENT on every summary)
  await chmod(OUT, 0o777);
  await writeFile(`${OUT}/cooks.json`, JSON.stringify(cooks));
  // QS-09's other source: helpers (COMMUNITY_COOK) who read the open requests addressed to the community (ADR-CA-009)
  const helpers = await Promise.all(
    Array.from({ length: COOK_COUNT }, async () => {
      const cookId = randomUUID();
      return {
        cookId,
        token: await issuer.tokenFor({ sub: cookId, roles: ["COMMUNITY_COOK"], scope: "cooking-assistance:read", expiresIn: 3600 }),
      };
    }),
  );
  await writeFile(`${OUT}/helpers.json`, JSON.stringify(helpers));
}, 180_000);

afterAll(async () => {
  await service.stop();
  await connection.close();
  await Promise.all([database.stop(), broker.stop(), upstream.stop()]);
});

/** Indicative measurements of this run, one JSON line each, for the quality-scenario report (step 12). */
async function recordMeasure(measure: { scenario: string; measure: string; value: number; samples: number }): Promise<void> {
  const environment = "laptop, Docker Desktop, Testcontainers PostgreSQL 16 / RabbitMQ 3.13, k6 2.3.0 in Docker — indicative";
  await appendFile("reports/quality-results.ndjson", `${JSON.stringify({ ...measure, environment, at: new Date().toISOString() })}\n`);
}

const ONE_YEAR = 120_000;

/** The first cook asks about step 3 of Sunday scones through the API (op requestHelp); returns the request's id. */
async function askAboutStep3(): Promise<string> {
  const [cook] = cooks;
  const response = await fetch(`${service.url}/cooking-assistance/help-requests`, {
    method: "POST",
    headers: { authorization: `Bearer ${cook?.token}`, version: "2.0.0", "content-type": "application/json" },
    body: JSON.stringify({
      title: "Sunday scones, step 3",
      topic: "PREPARATION_STEP",
      description: "What means 'warm'?",
      recipeId: SUNDAY_SCONES,
      howToStepId: stepOfSundayScones(3),
      addressees: ["COMMUNITY", "GRANDMA_AVATAR"], // community helpers see the open requests too (QS-09, inspection m-14)
    }),
  });
  return String(((await response.json()) as { helpRequestId?: string }).helpRequestId);
}

/** Grandma Avatar explains step 3 for the request (AsyncAPI receive operation receiveGrandmaAvatarHelpProvided). */
function grandmaExplainsStep3(helpRequestId: string, helpRequester = cooks[0]?.cookId): void {
  const payload = {
    helpId: randomUUID(),
    helpRequest: helpRequestId,
    helpRequester,
    answerTitle: "About step 3",
    helpProviderType: "GRANDMA_AVATAR",
    answer: {
      answerType: "PREPARATION_STEP_EXPLANATION",
      recipeId: SUNDAY_SCONES,
      howToStepId: stepOfSundayScones(3),
      description: "Warm means about 30 °C.",
    },
  };
  const headers = { correlationId: helpRequestId, messageId: randomUUID(), source: "grandma-avatar" };
  channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from(JSON.stringify(payload)), {
    persistent: true,
    contentType: "application/json",
    headers,
  });
}

async function countOf(query: ReturnType<typeof sql<{ count: string }>>): Promise<number> {
  return Number((await query.execute(database.db)).rows[0]?.count);
}

/**
 * Runs a constant-arrival-rate script and checks what the scenario asks: the rate was kept (no iteration dropped; k6
 * starts one at 0 s and one at the 60 s boundary, so 600 or 601 requests), every answer passed the script's check, and
 * p95 is within the budget. The measure goes to the quality-results report first, also when it misses.
 */
async function expectUnderLoad(script: string, { scenario, measure, budgetMs }: { scenario: string; measure: string; budgetMs: number }) {
  const summary = await k6(script);

  const p95 = summary.metrics.http_req_duration?.values["p(95)"] as number;
  const requests = summary.metrics.http_reqs?.values.count as number;
  await recordMeasure({ scenario, measure, value: p95, samples: requests });
  expect(requests).toBeGreaterThanOrEqual(600);
  expect(summary.metrics.dropped_iterations?.values.count ?? 0).toBe(0);
  expect(summary.metrics.checks?.values.rate).toBe(1);
  expect(p95).toBeLessThanOrEqual(budgetMs);
}

/** Runs one k6 script from tools/load against the running service (from inside Docker) and returns its summary. */
async function k6(script: string): Promise<K6Summary> {
  const baseUrl = service.url.replace(/\/\/(127\.0\.0\.1|localhost|\[::1?\])/, "//host.docker.internal");
  await promisify(execFile)("docker", [
    "run",
    "--rm",
    "--add-host=host.docker.internal:host-gateway",
    "-v",
    `${LOAD_SCRIPTS}:/scripts:ro`,
    "-v",
    `${OUT}:/out`,
    "-e",
    `BASE_URL=${baseUrl}`,
    "-e",
    `RECIPE_ID=${SUNDAY_SCONES}`,
    "-e",
    `HOW_TO_STEP_ID=${stepOfSundayScones(3)}`,
    K6,
    "run",
    "--quiet",
    `/scripts/${script}.js`,
  ]);
  return JSON.parse(await readFile(`${OUT}/${script}.summary.json`, "utf8")) as K6Summary;
}

describe("load harness -- QS-08 QS-09", () => {
  it("k6 2.3.0 runs a smoke script (1 POST, 1 GET) against the running service: 2 requests, 0 failed -- ADR-CA-016", async () => {
    const summary = await k6("smoke");

    expect(summary.metrics.http_reqs?.values.count).toBe(2);
    expect(summary.metrics.http_req_failed?.values.rate).toBe(0);
  });
});

describe("command latency -- QS-08", () => {
  it("10 requests/s on POST /help-requests for 60 s (600 requests): every answer 201 and p95 ≤ 300 ms, Recipe Catalog lookup included -- B2-S01 QS-08 (indicative)", async () => {
    await expectUnderLoad("commands", { scenario: "QS-08", measure: "POST /help-requests p95 (ms) at 10/s", budgetMs: 300 });
  });
});

describe("one year of data -- QS-16", () => {
  it("seeding one year of data writes 120 000 help requests and their helps into state, outbox (published) and both views; the views count 120 000 requests -- QS-16", async () => {
    await resetSchema(database.db);
    grandmaExplainsStep3(await askAboutStep3());
    const unpublished = () => countOf(sql`select count(*) from cook_assistance.outbox where published_at is null`);
    const helps = () => countOf(sql`select count(*) from cook_assistance.help_view`);
    expect(await eventually(async () => (await helps()) === 1 && (await unpublished()) === 0, 10_000)).toBe(true);

    await seedOneYearOfData(
      database.db,
      cooks.map((cook) => cook.cookId),
      ONE_YEAR,
    );

    expect(await countOf(sql`select count(*) from cook_assistance.help_request_view`)).toBe(ONE_YEAR);
    expect(await helps()).toBe(ONE_YEAR);
    expect(await countOf(sql`select count(*) from cook_assistance.help_request`)).toBe(ONE_YEAR);
    expect(await countOf(sql`select count(*) from cook_assistance.help`)).toBe(ONE_YEAR);
    expect(await countOf(sql`select count(*) from cook_assistance.outbox`)).toBe(2 * ONE_YEAR);
    expect(await unpublished()).toBe(0);
    const seeded = await fingerprintOfViews(database.db);
    await rebuildViews(database.db);
    expect(await fingerprintOfViews(database.db)).toBe(seeded);
  });
});

describe("query latency on one year of data -- QS-09 QS-16", () => {
  it("10 requests/s on the four served GETs (getHelpRequestById, listHelpRequests, listHelpsForHelpRequest, getHelpById) for 60 s on the seeded data: every answer 200 and p95 ≤ 200 ms -- B2-S01 B1-S04 QS-09 QS-16 (indicative)", async () => {
    const { rows } = await sql<{ requester: string; helpRequestId: string; helpId: string }>`
      select distinct on (r.requester) r.requester, r.id as "helpRequestId", h.id as "helpId"
      from cook_assistance.help_request_view r join cook_assistance.help_view h on h.help_request_id = r.id
      order by r.requester, r.created_at`.execute(database.db);
    const sampleOf = new Map(rows.map((row) => [row.requester, row]));
    await writeFile(`${OUT}/samples.json`, JSON.stringify(cooks.map((cook) => sampleOf.get(cook.cookId))));

    await expectUnderLoad("queries", { scenario: "QS-09", measure: "four GETs p95 (ms) at 10/s on one year of data", budgetMs: 200 });
  });
  it("listHelpRequests on its last page of the seeded data (deep offset) for 60 s at 10 requests/s: every answer 200 and p95 ≤ 200 ms -- B2-S01 QS-09 QS-16 (indicative)", async () => {
    await expectUnderLoad("last-page", {
      scenario: "QS-09",
      measure: "listHelpRequests last page p95 (ms) at 10/s on one year of data",
      budgetMs: 200,
    });
  });
  it("10 requests/s on listHelpRequests by 100 community helpers for 60 s, over the open requests of every cook in the seeded data: every answer 200 and p95 ≤ 200 ms -- B2-S01 QS-09 QS-16 ADR-CA-009 (indicative; inspection m-14)", async () => {
    await expectUnderLoad("helpers", {
      scenario: "QS-09",
      measure: "listHelpRequests by community helpers p95 (ms) at 10/s on one year of data",
      budgetMs: 200,
    });
  });
});

describe("consumer capacity -- QS-10", () => {
  it("5 Grandma Avatar answers/s on grandma-avatar.help.provided, one per open help request, for 120 s (600 messages): the queue depth sampled every 5 s never exceeds 5, all 600 consumed, dead-letter queue empty -- B1-S04 QS-10 (indicative)", async () => {
    const { rows: open } = await sql<{ id: string; requester: string }>`
      select id, requester from cook_assistance.help_request_view where status = 'REQUESTED' order by created_at desc limit 600`.execute(
      database.db,
    );
    const helpsBefore = await countOf(sql`select count(*) from cook_assistance.help_view`);
    const depths: number[] = [];
    const sampling = setInterval(() => {
      void channel.checkQueue(TOPOLOGY.grandmaAvatarQueue).then(({ messageCount }) => depths.push(messageCount));
    }, 5_000);
    try {
      for (const request of open) {
        grandmaExplainsStep3(request.id, request.requester);
        await new Promise((resolve) => setTimeout(resolve, 200)); // the load itself: 5 messages per second
      }
      const helps = () => countOf(sql`select count(*) from cook_assistance.help_view`);
      expect(await eventually(async () => (await helps()) === helpsBefore + 600, 30_000)).toBe(true);
    } finally {
      clearInterval(sampling);
    }

    await recordMeasure({
      scenario: "QS-10",
      measure: "queue depth max at 5 messages/s",
      value: Math.max(...depths),
      samples: depths.length,
    });
    expect(depths.length).toBeGreaterThanOrEqual(23);
    expect(Math.max(...depths)).toBeLessThanOrEqual(5);
    expect((await channel.checkQueue(TOPOLOGY.deadLetterQueue)).messageCount).toBe(0);
  });
});
