import { readFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { Parser } from "@asyncapi/parser";
import amqp, { type Channel, type ChannelModel } from "amqplib";
import { afterAll, beforeAll, beforeEach, describe, expect, it } from "vitest";
import { eventually } from "../acceptance/support/driver.js";
import { TOPOLOGY } from "../adapter/out/messaging/rabbitmq.js";
import { startRabbitMq, type TestBroker } from "../adapter/out/messaging/test-support/rabbitmq-container.js";
import { startPostgres, type TestDatabase } from "../adapter/out/persistence/test-support/postgres-container.js";
import { type RunningService, type ServiceLogEvent, startService } from "../service.js";
import { AUDIENCE, ISSUER, LocalIssuer } from "../test-support/http/local-issuer.js";
import { PUBLIC_BASE_URL } from "../test-support/http/start-http-test-app.js";
import { StubRecipeCatalog } from "../test-support/http/stub-recipe-catalog.js";

// Contract, AsyncAPI — increment 0, step 12, unit 12.3: generated from the approved spec, per receive operation, against
// the running service on real PostgreSQL 16 and RabbitMQ 3.13 (ADR-CA-011, ADR-CA-016).

const ASYNCAPI_SPEC = fileURLToPath(new URL("../../../contracts/asyncapi/cook-assistance.asyncapi.yaml", import.meta.url));
const { document } = await new Parser().parse(await readFile(ASYNCAPI_SPEC, "utf8"));
const RECEIVE_OPERATIONS = (document?.operations().filterByReceive() ?? []).map((operation) => operation.id() ?? "");

/** The examples of the operation's messages, as the spec gives them. */
function examplesOf(operationId: string) {
  return (document?.operations().get(operationId)?.messages().all() ?? []).flatMap((message) => message.examples().all());
}

let database: TestDatabase;
let broker: TestBroker;
let upstream: StubRecipeCatalog;
let connection: ChannelModel;
let channel: Channel;
let service: RunningService;
const logged: ServiceLogEvent[] = [];

beforeAll(async () => {
  let issuer: LocalIssuer;
  [database, broker, upstream, issuer] = await Promise.all([
    startPostgres(),
    startRabbitMq(),
    StubRecipeCatalog.start(),
    LocalIssuer.start(),
  ]);
  connection = await amqp.connect(broker.url);
  channel = await connection.createChannel();
  // the Grandma Avatar AI context owns and declares its exchange; the contract run stands in for it
  await channel.assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
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
    { tokenVerifier: issuer.verifier, recipeCatalogTokens: { accessToken: async () => "token" }, log: (event) => logged.push(event) },
  );
}, 180_000);

beforeEach(async () => {
  await channel.purgeQueue(TOPOLOGY.deadLetterQueue);
});

afterAll(async () => {
  await service.stop();
  await connection.close();
  await Promise.all([database.stop(), broker.stop(), upstream.stop()]);
});

function publishAsGrandmaAvatar(headers: Record<string, string>, payload: unknown): void {
  channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from(JSON.stringify(payload)), {
    persistent: true,
    contentType: "application/json",
    headers,
  });
}

/** Messages ready on a queue (not yet delivered, or given back). */
async function messagesOn(queue: string): Promise<number> {
  return (await channel.checkQueue(queue)).messageCount;
}

describe.each(RECEIVE_OPERATIONS)("receive operation %s", (operationId) => {
  it("has at least one message example -- ADR-CA-011", () => {
    expect(examplesOf(operationId).length).toBeGreaterThan(0);
  });
  it("every example is accepted: its outcome is reported, nothing is left on the queue, nothing is dead-lettered -- ADR-CA-004", async () => {
    for (const example of examplesOf(operationId)) {
      const headers = example.headers() as Record<string, string>;
      publishAsGrandmaAvatar(headers, example.payload());

      expect(await eventually(() => logged.some((event) => event.messageId === headers.messageId))).toBe(true);
      expect(await messagesOn(TOPOLOGY.grandmaAvatarQueue)).toBe(0);
      expect(await messagesOn(TOPOLOGY.deadLetterQueue)).toBe(0);
    }
  }, 15_000); // more than one 5 s polling deadline per example
  it("the first example with its first required payload field removed is dead-lettered -- ADR-CA-004 ADR-CA-011", async () => {
    const [message] = document?.operations().get(operationId)?.messages().all() ?? [];
    const [example] = examplesOf(operationId);
    const [firstRequired] = (message?.payload()?.required() ?? []) as string[];
    const { [String(firstRequired)]: _removed, ...incomplete } = example?.payload() as Record<string, unknown>;

    publishAsGrandmaAvatar(example?.headers() as Record<string, string>, incomplete);

    expect(await eventually(async () => (await messagesOn(TOPOLOGY.deadLetterQueue)) === 1)).toBe(true);
    const deadLetter = await channel.get(TOPOLOGY.deadLetterQueue, { noAck: true });
    const deaths = (deadLetter && deadLetter.properties.headers?.["x-death"]) as { reason: string }[];
    // refused at once as invalid (nack without requeue), not dead-lettered after five deliveries
    expect(deaths.map((death) => death.reason)).toEqual(["rejected"]);
  }, 15_000);
});
