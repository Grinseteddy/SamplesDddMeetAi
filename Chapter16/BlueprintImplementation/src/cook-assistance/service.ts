import type { AddressInfo } from "node:net";
import type { INestApplication } from "@nestjs/common";
import { connect } from "amqp-connection-manager";
import { Kysely, PostgresDialect } from "kysely";
import pg from "pg";
import { createHttpApp } from "./adapter/in/http/http-app.js";
import type { JoseTokenVerifier } from "./adapter/in/http/token-verifier.js";
import { createGrandmaAvatarHelpProvidedConsumer } from "./adapter/in/messaging/grandma-avatar-help-provided.consumer.js";
import { OutboxRelay } from "./adapter/out/messaging/outbox-relay.js";
import { AmqpPublisher, declareTopology, subscribeConsumer } from "./adapter/out/messaging/rabbitmq.js";
import { RelayPoller } from "./adapter/out/messaging/relay-poller.js";
import { loadSendCheck } from "./adapter/out/messaging/send-check.js";
import { migrateToLatest } from "./adapter/out/persistence/migrations.js";
import { CircuitBreaker } from "./adapter/out/recipe-catalog/circuit-breaker.js";
import { HttpRecipeCatalog } from "./adapter/out/recipe-catalog/http-recipe-catalog.js";
import { SystemClock } from "./adapter/out/system/system-clock.js";
import { composeCookAssistance } from "./composition.js";
import type { TokenProvider } from "./adapter/out/recipe-catalog/token-provider.js";
import type { CookAssistance } from "./application/cook-assistance.js";
import type { Configuration } from "./configuration.js";

/** A structured event the running service reports — names and IDs, never question or answer text (ADR-CA-010). */
export interface ServiceLogEvent {
  readonly event: string;
  readonly [field: string]: unknown;
}

/** What main.ts provides and tests replace: token checks, Recipe Catalog's tokens, where events go. */
export interface Platform {
  readonly tokenVerifier: JoseTokenVerifier;
  readonly recipeCatalogTokens: TokenProvider;
  readonly log: (event: ServiceLogEvent) => void;
}

export interface RunningService {
  /** Where the HTTP API listens, e.g. http://127.0.0.1:8080 (the spec's server path follows). */
  readonly url: string;
  /** Stops polling, then closes HTTP, the broker connection and the database pool. */
  stop(): Promise<void>;
}

/** ADR-CA-004 · the relay polls the outbox every 500 ms. */
const RELAY_INTERVAL_MS = 500;

/** ADR-CA-008, ADR-CA-010 · the HTTP adapter on the configured port, logging through the platform. */
async function startHttp(configuration: Configuration, platform: Platform, cookAssistance: CookAssistance): Promise<INestApplication> {
  const app = await createHttpApp({
    cookAssistance,
    tokenVerifier: platform.tokenVerifier,
    publicBaseUrl: configuration.publicBaseUrl,
    log: platform.log,
  });
  await app.listen(configuration.port);
  return app;
}

// The walking skeleton as one process (step 10, unit 10.4): database, HTTP, broker, relay.
export async function startService(configuration: Configuration, platform: Platform): Promise<RunningService> {
  // ADR-CA-003 · PostgreSQL through Kysely; forward-only migrations applied at startup
  const db = new Kysely<unknown>({ dialect: new PostgresDialect({ pool: new pg.Pool({ connectionString: configuration.databaseUrl }) }) });
  await migrateToLatest(db);

  // ADR-CA-015 · Recipe Catalog over HTTP, behind a circuit breaker; ADR-CA-007 · one clock for the process
  const clock = new SystemClock();
  const recipeCatalog = new HttpRecipeCatalog({
    baseUrl: configuration.recipeCatalogUrl,
    tokenProvider: platform.recipeCatalogTokens,
    breaker: new CircuitBreaker(clock),
  });
  const cookAssistance = composeCookAssistance({ db, recipeCatalog, clock });

  const app = await startHttp(configuration, platform, cookAssistance);

  // ADR-CA-004 · RabbitMQ: our topology, the Grandma Avatar consumer, a confirm channel for the relay
  const broker = connect([configuration.amqpUrl]);
  const channel = broker.createChannel({ json: false, setup: declareTopology });
  await channel.waitForConnect();
  const consumer = await createGrandmaAvatarHelpProvidedConsumer({
    handler: cookAssistance.provideHelpWhenGrandmaAvatarAnswers,
    log: (event) => platform.log({ ...event }),
  });
  await subscribeConsumer(channel, consumer);

  const relay = new OutboxRelay({
    db,
    publisher: new AmqpPublisher(channel),
    clock,
    check: await loadSendCheck(),
    publicBaseUrl: configuration.publicBaseUrl,
    reportDeadLetter: (row) => platform.log({ event: "outbox-dead-lettered", ...row }),
  });
  const poller = new RelayPoller(relay, RELAY_INTERVAL_MS, (error) => platform.log({ event: "relay-run-failed", error: String(error) }));
  poller.start();

  return {
    url: `http://127.0.0.1:${(app.getHttpServer().address() as AddressInfo).port}`,
    async stop() {
      await poller.stop();
      await app.close();
      await channel.close();
      await broker.close();
      await db.destroy();
    },
  };
}
