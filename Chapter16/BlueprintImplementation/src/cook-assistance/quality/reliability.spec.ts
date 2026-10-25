import { appendFile, mkdir } from "node:fs/promises";
import amqp, { type Channel, type ChannelModel } from "amqplib";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { CookAssistanceDriver, eventually } from "../acceptance/support/driver.js";
import { stepOfSundayScones, stepPath, SUNDAY_SCONES } from "../acceptance/support/sunday-scones-recipe.js";
import { TOPOLOGY } from "../adapter/out/messaging/rabbitmq.js";
import { loadSendCheck, type SendCheck } from "../adapter/out/messaging/send-check.js";
import { startRabbitMq, type TestBroker } from "../adapter/out/messaging/test-support/rabbitmq-container.js";
import { migrateToLatest } from "../adapter/out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "../adapter/out/persistence/test-support/postgres-container.js";
import { resetSchema } from "../adapter/out/persistence/test-support/reset-schema.js";
import { type RunningService, startService } from "../service.js";
import { sundaySconesStep3 } from "../test-support/fixtures/sunday-scones.js";
import { AUDIENCE, ISSUER, LocalIssuer } from "../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../test-support/http/start-http-test-app.js";
import { StubRecipeCatalog } from "../test-support/http/stub-recipe-catalog.js";

// Quality scenarios — increment 0, step 12, unit 12.5c: reliability on the running service, real PostgreSQL 16 and
// RabbitMQ 3.13 (Testcontainers). Results are indicative: a laptop, an outage of seconds instead of minutes.

const OUTAGE_MS = 20_000;

/** Indicative measurements of this run, one JSON line each, for the quality-scenario report (step 12). */
async function recordMeasure(measure: { scenario: string; measure: string; value: number; samples: number }): Promise<void> {
  await mkdir("reports", { recursive: true });
  const environment = "laptop, Docker Desktop, Testcontainers PostgreSQL 16 / RabbitMQ 3.13 — indicative";
  await appendFile("reports/quality-results.ndjson", `${JSON.stringify({ ...measure, environment, at: new Date().toISOString() })}\n`);
}

let database: TestDatabase;
let broker: TestBroker;
let upstream: StubRecipeCatalog;
let issuer: LocalIssuer;
let connection: ChannelModel;
let channel: Channel;
let check: SendCheck;

beforeAll(async () => {
  [database, broker, upstream, issuer] = await Promise.all([
    startPostgres(),
    startRabbitMq(),
    StubRecipeCatalog.start(),
    LocalIssuer.start(),
  ]);
  await migrateToLatest(database.db);
  connection = await amqp.connect(broker.url);
  channel = await connection.createChannel();
  // the Grandma Avatar AI context owns and declares its exchange; the test stands in for it
  await channel.assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
  check = await loadSendCheck();
  upstream.answerAt(stepPath(3), 200, {
    howToStepId: stepOfSundayScones(3),
    sequenceNumber: 3,
    description: "Let dough rest in a warm place.",
  });
}, 180_000);

beforeEach(async () => {
  await resetSchema(database.db);
});

afterAll(async () => {
  await connection.close();
  await Promise.all([database.stop(), broker.stop(), upstream.stop()]);
});

/** One running instance of the service, and a driver that speaks only its contracts. */
async function startInstance(): Promise<{ service: RunningService; driver: CookAssistanceDriver }> {
  const service = await startService(
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
  return { service, driver: new CookAssistanceDriver({ baseUrl: service.url, issuer, channel, check }) };
}

/** The cook asks about step 3 of Sunday scones; returns the status and the new request's id. */
async function askAboutStep3(driver: CookAssistanceDriver): Promise<{ status: number; id: string }> {
  const response = await driver.post("/help-requests", sundaySconesStep3.requester, {
    title: "Sunday scones, step 3",
    topic: "PREPARATION_STEP",
    description: "What means 'warm'?",
    recipeId: SUNDAY_SCONES,
    howToStepId: stepOfSundayScones(3),
    addressees: ["GRANDMA_AVATAR"],
  });
  return { status: response.status, id: String(((await response.json()) as { helpRequestId?: string }).helpRequestId) };
}

describe("reliability", () => {
  it("broker paused: every command is accepted (201); after recovery every stored event is published -- QS-04", async () => {
    const { service, driver } = await startInstance();
    try {
      const requested = await driver.listenTo("help.requested");

      await broker.pause();
      const answers: { status: number; id: string }[] = [];
      for (let command = 0; command < 5; command += 1) {
        answers.push(await askAboutStep3(driver));
      }
      await new Promise((resolve) => setTimeout(resolve, OUTAGE_MS)); // the outage itself: the stimulus, not a wait for an outcome
      await broker.resume();

      expect(answers.map((answer) => answer.status)).toEqual([201, 201, 201, 201, 201]);
      const published = () => answers.every((answer) => requested.messages.some((message) => message.headers.correlationId === answer.id));
      expect(await eventually(published, 30_000)).toBe(true);
    } finally {
      await service.stop();
    }
  }, 120_000);
  it("from commit to publication: p95 ≤ 2 s over 20 commands -- QS-12", async () => {
    const { service, driver } = await startInstance();
    try {
      const requested = await driver.listenTo("help.requested");
      const latencies: number[] = [];
      for (let command = 0; command < 20; command += 1) {
        const { id } = await askAboutStep3(driver);
        const committed = Date.now(); // the 201 arrives after the commit
        const arrived = await eventually(() => requested.messages.some((message) => message.headers.correlationId === id), 5_000);
        expect(arrived).toBe(true);
        latencies.push(Date.now() - committed);
      }
      const p95 = [...latencies].sort((a, b) => a - b)[Math.ceil(latencies.length * 0.95) - 1] as number;
      await recordMeasure({ scenario: "QS-12", measure: "commit → publication p95 (ms)", value: p95, samples: latencies.length });
      expect(p95).toBeLessThanOrEqual(2_000);
    } finally {
      await service.stop();
    }
  }, 120_000);
  it("two instances on one database and broker publish every event exactly once -- QS-14", async () => {
    const [first, second] = [await startInstance(), await startInstance()];
    try {
      const requested = await first.driver.listenTo("help.requested");
      const ids: string[] = [];
      for (let command = 0; command < 10; command += 1) {
        ids.push((await askAboutStep3(command % 2 === 0 ? first.driver : second.driver)).id);
      }
      // a marker after them: once it is published, both relays have finished the passes that published the ten
      const marker = (await askAboutStep3(first.driver)).id;
      const isPublished = (id: string) => requested.messages.some((message) => message.headers.correlationId === id);
      expect(await eventually(() => [...ids, marker].every(isPublished), 15_000)).toBe(true);

      const messageIds = requested.messages.map((message) => message.headers.messageId);
      expect(messageIds).toHaveLength(11);
      expect(new Set(messageIds).size).toBe(11);
    } finally {
      await first.service.stop();
      await second.service.stop();
    }
  }, 120_000);
});
