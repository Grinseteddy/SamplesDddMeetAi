import type { Channel } from "amqplib";
import type { OutgoingMessage } from "../../adapter/out/messaging/outbound-messages.js";
import { TOPOLOGY } from "../../adapter/out/messaging/rabbitmq.js";
import type { SendCheck } from "../../adapter/out/messaging/send-check.js";
import type { LocalIssuer } from "../../test-support/http/local-issuer.js";

/** What the service published on one routing key, each message checked against the AsyncAPI spec on arrival. */
export interface Published {
  readonly messages: OutgoingMessage[];
  readonly invalid: OutgoingMessage[];
}

/** Where the driver reaches the running service and the broker, and how it checks what it reads. */
export interface DriverConnection {
  readonly baseUrl: string;
  readonly issuer: LocalIssuer;
  readonly channel: Channel;
  readonly check: SendCheck;
}

/** Polls until the condition holds or the deadline passes (asynchronous outcomes are never slept for). */
export async function eventually(condition: () => boolean | Promise<boolean>, withinMs = 5_000): Promise<boolean> {
  const deadline = Date.now() + withinMs;
  while (!(await condition())) {
    if (Date.now() > deadline) {
      return false;
    }
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
  return true;
}

/**
 * The acceptance driver: it talks to the running service only through its contracts — HTTP on the OpenAPI paths,
 * messages on the AsyncAPI channels — never through its classes or tables.
 */
export class CookAssistanceDriver {
  constructor(private readonly connection: DriverConnection) {}

  /** A test queue bound to one of Cook Assistance's routing keys (the service declares the exchange). */
  async listenTo(routingKey: string): Promise<Published> {
    const published: Published = { messages: [], invalid: [] };
    const { queue } = await this.connection.channel.assertQueue("", { exclusive: true, autoDelete: true });
    await this.connection.channel.bindQueue(queue, TOPOLOGY.publishExchange, routingKey);
    await this.connection.channel.consume(
      queue,
      (delivered) => {
        if (delivered === null) {
          return;
        }
        const message: OutgoingMessage = {
          exchange: delivered.fields.exchange,
          routingKey: delivered.fields.routingKey,
          headers: delivered.properties.headers as OutgoingMessage["headers"],
          payload: JSON.parse(delivered.content.toString()),
        };
        (this.connection.check(message) ? published.messages : published.invalid).push(message);
      },
      { noAck: true },
    );
    return published;
  }

  async post(path: string, cookId: string, body: unknown): Promise<Response> {
    return fetch(`${this.connection.baseUrl}/cooking-assistance${path}`, {
      method: "POST",
      headers: { ...(await this.headersOf(cookId, "cooking-assistance:write")), "content-type": "application/json" },
      body: JSON.stringify(body),
    });
  }

  async get(path: string, cookId: string): Promise<Response> {
    return fetch(`${this.connection.baseUrl}/cooking-assistance${path}`, {
      headers: await this.headersOf(cookId, "cooking-assistance:read"),
    });
  }

  /** A message of the Grandma Avatar AI on its own exchange (AsyncAPI receive operation receiveGrandmaAvatarHelpProvided). */
  answerAsGrandmaAvatar(headers: Record<string, string>, payload: unknown): void {
    this.connection.channel.publish(
      TOPOLOGY.grandmaAvatarExchange,
      TOPOLOGY.grandmaAvatarRoutingKey,
      Buffer.from(JSON.stringify(payload)),
      {
        persistent: true,
        contentType: "application/json",
        headers,
      },
    );
  }

  private async headersOf(cookId: string, scope: string): Promise<Record<string, string>> {
    return { authorization: `Bearer ${await this.connection.issuer.tokenFor({ sub: cookId, scope })}`, version: "2.0.0" };
  }
}
