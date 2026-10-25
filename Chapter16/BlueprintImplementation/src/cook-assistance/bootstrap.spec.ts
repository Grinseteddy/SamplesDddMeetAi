import amqp, { type Channel, type ChannelModel } from "amqplib";
import { sql } from "kysely";
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from "vitest";
import type { OutgoingMessage } from "./adapter/out/messaging/outbound-messages.js";
import { RelayPoller } from "./adapter/out/messaging/relay-poller.js";
import { InvalidConfiguration, loadConfiguration, MissingConfiguration } from "./configuration.js";
import { TOPOLOGY } from "./adapter/out/messaging/rabbitmq.js";
import { loadSendCheck, type SendCheck } from "./adapter/out/messaging/send-check.js";
import { startRabbitMq, type TestBroker } from "./adapter/out/messaging/test-support/rabbitmq-container.js";
import { startPostgres, type TestDatabase } from "./adapter/out/persistence/test-support/postgres-container.js";
import { type RunningService, startService } from "./service.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "./test-support/fixtures/sunday-scones.js";
import { AUDIENCE, ISSUER, LocalIssuer } from "./test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "./test-support/http/start-http-test-app.js";
import { StubRecipeCatalog } from "./test-support/http/stub-recipe-catalog.js";

// Bootstrap — increment 0, step 10, unit 10.4: poller, configuration and one run end to end (real PostgreSQL and RabbitMQ).

/** A complete environment as the deployment sets it (no credentials). */
const ENVIRONMENT = {
  DATABASE_URL: "postgres://db.internal:5432/cook_assistance",
  AMQP_URL: "amqp://rabbitmq.internal:5672",
  OIDC_ISSUER: "https://keycloak.internal/realms/cooking",
  OIDC_AUDIENCE: "cook-assistance",
  PUBLIC_BASE_URL: "https://api.cooking.example",
  PORT: "8080",
  RECIPE_CATALOG_URL: "https://larder.internal/recipe-catalog",
};

/** Step 3 of Sunday scones as Recipe Catalog returns it (HowToStep). */
const STEP_3 = { howToStepId: sundaySconesStep3.howToStepId, sequenceNumber: 3, description: "Leave the dough somewhere warm." };

/** Polls until the condition holds (deliveries arrive asynchronously). */
async function eventually(condition: () => boolean, withinMs: number): Promise<void> {
  for (let waited = 0; waited < withinMs && !condition(); waited += 50) {
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
}

describe("bootstrap", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("the poller runs the relay every 500 ms and waits for the running pass on shutdown -- ADR-CA-004", async () => {
    vi.useFakeTimers();
    let runs = 0;
    let finishPass: () => void = () => {};
    const relay = {
      runOnce: () =>
        new Promise<void>((resolve) => {
          runs += 1;
          finishPass = resolve;
          if (runs < 4) resolve();
        }),
    };
    const poller = new RelayPoller(relay, 500, (error) => expect.fail(String(error)));

    poller.start();
    await vi.advanceTimersByTimeAsync(1_600);
    expect(runs).toBe(3);

    await vi.advanceTimersByTimeAsync(500); // the 4th pass starts and stays in flight
    let stopped = false;
    const stopping = poller.stop().then(() => {
      stopped = true;
    });
    await vi.advanceTimersByTimeAsync(0);
    expect(stopped).toBe(false);
    finishPass();
    await stopping;
    await vi.advanceTimersByTimeAsync(2_000);
    expect([stopped, runs]).toEqual([true, 4]);
  });
  it("a failing relay run is reported and the poller keeps running -- ADR-CA-004", async () => {
    vi.useFakeTimers();
    let runs = 0;
    const failure = new Error("database unreachable");
    const failures: unknown[] = [];
    const relay = {
      runOnce: async () => {
        runs += 1;
        if (runs === 1) throw failure;
      },
    };
    const poller = new RelayPoller(relay, 500, (error) => failures.push(error));

    poller.start();
    await vi.advanceTimersByTimeAsync(1_100);
    await poller.stop();

    expect(failures).toEqual([failure]);
    expect(runs).toBe(2);
  });
  it("configuration from the environment (DATABASE_URL, AMQP_URL, OIDC_ISSUER, OIDC_AUDIENCE, PUBLIC_BASE_URL, PORT, RECIPE_CATALOG_URL); a missing variable stops the start with a named error -- ADR-CA-010", () => {
    const { AMQP_URL: _omitted, ...withoutAmqpUrl } = ENVIRONMENT;

    expect(loadConfiguration(ENVIRONMENT)).toEqual({
      databaseUrl: "postgres://db.internal:5432/cook_assistance",
      amqpUrl: "amqp://rabbitmq.internal:5672",
      oidcIssuer: "https://keycloak.internal/realms/cooking",
      oidcAudience: "cook-assistance",
      publicBaseUrl: "https://api.cooking.example",
      port: 8080,
      recipeCatalogUrl: "https://larder.internal/recipe-catalog",
    });
    expect(() => loadConfiguration(withoutAmqpUrl)).toThrow(new MissingConfiguration("AMQP_URL"));
  });
  it("an empty variable or a PORT that is not a port number stops the start with a named error -- ADR-CA-010", () => {
    expect(() => loadConfiguration({ ...ENVIRONMENT, OIDC_AUDIENCE: "" })).toThrow(new MissingConfiguration("OIDC_AUDIENCE"));
    expect(() => loadConfiguration({ ...ENVIRONMENT, PORT: "eighty" })).toThrow(new InvalidConfiguration("PORT"));
  });
  it("RECIPE_CATALOG_URL is part of the configuration; missing → MissingConfiguration -- ADR-CA-015 ADR-CA-010", () => {
    const { RECIPE_CATALOG_URL: _omitted, ...withoutRecipeCatalogUrl } = ENVIRONMENT;

    expect(loadConfiguration(ENVIRONMENT).recipeCatalogUrl).toBe("https://larder.internal/recipe-catalog");
    expect(() => loadConfiguration(withoutRecipeCatalogUrl)).toThrow(new MissingConfiguration("RECIPE_CATALOG_URL"));
  });
});

describe("bootstrap — end to end (PostgreSQL 16, RabbitMQ 3.13, stub Recipe Catalog)", () => {
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
    connection = await amqp.connect(broker.url);
    channel = await connection.createChannel();
    // the Grandma Avatar AI context owns and declares its exchange; the test stands in for it
    await channel.assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
    check = await loadSendCheck();
  }, 120_000);

  afterAll(async () => {
    await connection.close();
    await Promise.all([database.stop(), broker.stop(), upstream.stop()]);
  });

  /** A running service against the test containers, as main.ts would start it. */
  function start(): Promise<RunningService> {
    return startService(
      {
        databaseUrl: database.url,
        amqpUrl: broker.url,
        oidcIssuer: ISSUER,
        oidcAudience: AUDIENCE,
        publicBaseUrl: PUBLIC_BASE_URL,
        port: 0,
        recipeCatalogUrl: upstream.baseUrl,
      },
      { tokenVerifier: issuer.verifier, recipeCatalogTokens: { accessToken: async () => "cook-assistance-service-token" }, log: () => {} },
    );
  }

  /** Messages Cook Assistance publishes with this routing key, collected on a test queue (bind after start: the service declares the exchange). */
  async function collect(routingKey: string): Promise<OutgoingMessage[]> {
    const { queue } = await channel.assertQueue("", { exclusive: true });
    await channel.bindQueue(queue, TOPOLOGY.publishExchange, routingKey);
    const received: OutgoingMessage[] = [];
    await channel.consume(
      queue,
      (delivered) => {
        if (delivered !== null) {
          received.push({
            exchange: delivered.fields.exchange,
            routingKey: delivered.fields.routingKey,
            headers: delivered.properties.headers as OutgoingMessage["headers"],
            payload: JSON.parse(delivered.content.toString()),
          });
        }
      },
      { noAck: true },
    );
    return received;
  }

  /** The cook asks for help with Sunday scones step 3 over HTTP; returns the new help request's id. */
  async function requestHelpForStep3(service: RunningService): Promise<string> {
    const response = await fetch(`${service.url}/cooking-assistance/help-requests`, {
      method: "POST",
      headers: {
        authorization: `Bearer ${await issuer.tokenFor({ sub: sundaySconesStep3.requester })}`,
        version: "2.0.0",
        "content-type": "application/json",
      },
      body: JSON.stringify({
        title: sundaySconesStep3.title,
        topic: "PREPARATION_STEP",
        description: sundaySconesStep3.question,
        recipeId: sundaySconesStep3.recipeId,
        howToStepId: sundaySconesStep3.howToStepId,
        addressees: [...sundaySconesStep3.addressees],
      }),
    });
    expect(response.status).toBe(201);
    return ((await response.json()) as { helpRequestId: string }).helpRequestId;
  }

  /** Waits for the first message, then long enough that a second one would have arrived. */
  async function settle(received: OutgoingMessage[]): Promise<void> {
    await eventually(() => received.length > 0, 5_000);
    await new Promise((resolve) => setTimeout(resolve, 1_000));
  }

  it("end to end: POST /help-requests, one relay run → exactly the promised message on help.requested, no pending rows left -- B2-S01 wiring", async () => {
    upstream.respondWith(200, STEP_3);
    const service = await start();
    try {
      const requested = await collect("help.requested");

      const helpRequestId = await requestHelpForStep3(service);
      await settle(requested);

      expect(requested).toHaveLength(1);
      expect(requested[0]).toMatchObject({
        routingKey: "help.requested",
        headers: { correlationId: helpRequestId },
        payload: { helpRequestId },
      });
      expect(check(requested[0] as OutgoingMessage)).toBe(true);
      const pending = await sql<{ count: string }>`select count(*) from cook_assistance.outbox where published_at is null`.execute(
        database.db,
      );
      expect(pending.rows).toEqual([{ count: "0" }]);
    } finally {
      await service.stop();
    }
  });
  it("end to end: a Grandma Avatar message on its queue → the help is stored and HelpProvided is published on help.provided -- B1-S04 wiring", async () => {
    upstream.respondWith(200, STEP_3);
    const service = await start();
    try {
      const provided = await collect("help.provided");
      const helpRequestId = await requestHelpForStep3(service);

      const answer = {
        helpId: grandmaExplainsWarm.id,
        helpRequest: helpRequestId,
        helpRequester: sundaySconesStep3.requester,
        answerTitle: grandmaExplainsWarm.answerTitle,
        helpProviderType: "GRANDMA_AVATAR",
        answer: {
          answerType: "PREPARATION_STEP_EXPLANATION",
          recipeId: sundaySconesStep3.recipeId,
          howToStepId: sundaySconesStep3.howToStepId,
          description: grandmaExplainsWarm.answer.description,
        },
      };
      channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from(JSON.stringify(answer)), {
        persistent: true,
        contentType: "application/json",
        headers: { correlationId: helpRequestId, messageId: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d", source: "grandma-avatar" },
      });
      await settle(provided);

      expect(provided).toHaveLength(1);
      expect(provided[0]).toMatchObject({ routingKey: "help.provided", headers: { correlationId: helpRequestId } });
      expect(check(provided[0] as OutgoingMessage)).toBe(true);
      const help = await sql<{ help_request_id: string }>`
        select help_request_id from cook_assistance.help where id = ${grandmaExplainsWarm.id}`.execute(database.db);
      expect(help.rows).toEqual([{ help_request_id: helpRequestId }]);
    } finally {
      await service.stop();
    }
  });
});
