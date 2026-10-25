import { mkdir, writeFile } from "node:fs/promises";
import amqp from "amqplib";
import { TOPOLOGY } from "../adapter/out/messaging/rabbitmq.js";
import { startRabbitMq } from "../adapter/out/messaging/test-support/rabbitmq-container.js";
import { startPostgres } from "../adapter/out/persistence/test-support/postgres-container.js";
import { startService } from "../service.js";
import { sundaySconesStep3 } from "../test-support/fixtures/sunday-scones.js";
import { AUDIENCE, ISSUER, LocalIssuer } from "../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../test-support/http/start-http-test-app.js";
import { StubRecipeCatalog } from "../test-support/http/stub-recipe-catalog.js";

// Contract run, OpenAPI (step 12, unit 12.4): the running service for Schemathesis — real PostgreSQL 16 and RabbitMQ
// 3.13 (Testcontainers), a Recipe Catalog stub that knows every step, a local token issuer. Writes a token for a COOK
// with read and write scope, prints "ready <url>", and stops everything on SIGTERM or SIGINT.

const port = Number(process.env.CONTRACT_PORT ?? 8099);
const [database, broker, upstream, issuer] = await Promise.all([
  startPostgres(),
  startRabbitMq(),
  StubRecipeCatalog.start(),
  LocalIssuer.start(),
]);
const connection = await amqp.connect(broker.url);
// the Grandma Avatar AI context owns and declares its exchange; the contract run stands in for it
await (await connection.createChannel()).assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
upstream.respondWith(200, {
  howToStepId: sundaySconesStep3.howToStepId,
  sequenceNumber: 3,
  description: "Let dough rest in a warm place.",
});

const service = await startService(
  {
    databaseUrl: database.url,
    amqpUrl: broker.url,
    oidcIssuer: ISSUER,
    oidcAudience: AUDIENCE,
    publicBaseUrl: PUBLIC_BASE_URL,
    port,
    recipeCatalogUrl: upstream.baseUrl,
  },
  { tokenVerifier: issuer.verifier, recipeCatalogTokens: { accessToken: async () => "token" }, log: () => {} },
);
const token = await issuer.tokenFor({
  sub: sundaySconesStep3.requester,
  scope: "cooking-assistance:read cooking-assistance:write",
  expiresIn: 3600,
});
await mkdir("reports", { recursive: true });
await writeFile("reports/contract-token.txt", token);
process.stdout.write(`ready ${service.url}\n`);

async function stop(): Promise<void> {
  await service.stop();
  await connection.close();
  await Promise.all([database.stop(), broker.stop(), upstream.stop()]);
  process.exit(0);
}
process.once("SIGTERM", () => void stop());
process.once("SIGINT", () => void stop());
