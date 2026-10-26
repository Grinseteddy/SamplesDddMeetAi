import { After, AfterAll, Before, BeforeAll, setDefaultTimeout, setWorldConstructor, World } from "@cucumber/cucumber";
import amqp, { type Channel, type ChannelModel } from "amqplib";
import { TOPOLOGY } from "../../adapter/out/messaging/rabbitmq.js";
import { loadSendCheck, type SendCheck } from "../../adapter/out/messaging/send-check.js";
import { startRabbitMq, type TestBroker } from "../../adapter/out/messaging/test-support/rabbitmq-container.js";
import { migrateToLatest } from "../../adapter/out/persistence/migrations.js";
import { startPostgres, type TestDatabase } from "../../adapter/out/persistence/test-support/postgres-container.js";
import { resetSchema } from "../../adapter/out/persistence/test-support/reset-schema.js";
import { type RunningService, startService } from "../../service.js";
import { AUDIENCE, ISSUER, LocalIssuer } from "../../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../../test-support/http/start-http-test-app.js";
import { StubRecipeCatalog } from "../../test-support/http/stub-recipe-catalog.js";
import { CookAssistanceDriver, type Published } from "./driver.js";

// Acceptance (step 12, ADR-CA-016): the running service — real PostgreSQL 16 and RabbitMQ 3.13 (Testcontainers),
// the Recipe Catalog stub, a local token issuer. Only this setup touches the database: to reset it.

setDefaultTimeout(30_000);

interface Infrastructure {
  readonly database: TestDatabase;
  readonly broker: TestBroker;
  readonly upstream: StubRecipeCatalog;
  readonly issuer: LocalIssuer;
  readonly connection: ChannelModel;
  readonly channel: Channel;
  readonly check: SendCheck;
}

let infrastructure: Infrastructure;

BeforeAll({ timeout: 180_000 }, async () => {
  const [database, broker, upstream, issuer] = await Promise.all([
    startPostgres(),
    startRabbitMq(),
    StubRecipeCatalog.start(),
    LocalIssuer.start(),
  ]);
  await migrateToLatest(database.db);
  const connection = await amqp.connect(broker.url);
  const channel = await connection.createChannel();
  // the Grandma Avatar AI context owns and declares its exchange; the acceptance setup stands in for it
  await channel.assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
  infrastructure = { database, broker, upstream, issuer, connection, channel, check: await loadSendCheck() };
});

AfterAll(async () => {
  await infrastructure.connection.close();
  await Promise.all([infrastructure.database.stop(), infrastructure.broker.stop(), infrastructure.upstream.stop()]);
});

/** One scenario: a fresh service on empty state, and what it publishes. */
export class CookAssistanceWorld extends World {
  service!: RunningService;
  driver!: CookAssistanceDriver;
  helpRequested!: Published;
  helpProvided!: Published;
  /** The cook's last answer from the API, and the help request it created, if any. */
  lastResponse: { status: number; body: Record<string, unknown> } | undefined;
  helpRequestId: string | undefined;
  /** The help Grandma Avatar's last answer would create. */
  helpId: string | undefined;

  get upstream(): StubRecipeCatalog {
    return infrastructure.upstream;
  }
}

setWorldConstructor(CookAssistanceWorld);

Before(async function (this: CookAssistanceWorld) {
  await resetSchema(infrastructure.database.db);
  infrastructure.upstream.reset();
  this.service = await startService(
    {
      databaseUrl: infrastructure.database.url,
      amqpUrl: infrastructure.broker.url,
      oidcIssuer: ISSUER,
      oidcAudience: AUDIENCE,
      publicBaseUrl: PUBLIC_BASE_URL,
      port: 0,
      recipeCatalogUrl: infrastructure.upstream.baseUrl,
    },
    {
      tokenVerifier: infrastructure.issuer.verifier,
      recipeCatalogTokens: { accessToken: async () => "cook-assistance-service-token" },
      log: () => {},
    },
  );
  this.driver = new CookAssistanceDriver({
    baseUrl: this.service.url,
    issuer: infrastructure.issuer,
    channel: infrastructure.channel,
    check: infrastructure.check,
  });
  this.helpRequested = await this.driver.listenTo("help.requested");
  this.helpProvided = await this.driver.listenTo("help.provided");
});

After(async function (this: CookAssistanceWorld) {
  await this.service.stop();
});
