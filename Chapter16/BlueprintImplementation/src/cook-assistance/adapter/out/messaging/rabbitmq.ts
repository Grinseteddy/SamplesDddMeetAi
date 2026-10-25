import type { ChannelWrapper } from "amqp-connection-manager";
import type { ConfirmChannel } from "amqplib";
import type { Delivery, RawMessage } from "../../in/messaging/grandma-avatar-help-provided.consumer.js";
import { type OutgoingMessage, PUBLISH_EXCHANGE } from "./outbound-messages.js";
import type { Publisher } from "./outbox-relay.js";

// ADR-CA-004 · RabbitMQ: names of the exchanges and queues Cook Assistance uses.
export const TOPOLOGY = {
  publishExchange: PUBLISH_EXCHANGE,
  grandmaAvatarExchange: "grandma-avatar",
  grandmaAvatarQueue: "cook-assistance.grandma-avatar-help-provided",
  grandmaAvatarRoutingKey: "grandma-avatar.help.provided",
  deadLetterExchange: "cook-assistance.dead-letter",
  deadLetterQueue: "cook-assistance.grandma-avatar-help-provided.dead-letter",
} as const;

/**
 * ADR-CA-004 · a Grandma Avatar message is delivered at most 5 times. RabbitMQ dead-letters a quorum-queue message
 * once its delivery limit is exceeded, so the limit is one less. Queue arguments cannot change on an existing
 * queue: a different limit later needs a new queue (or moving the old one's messages).
 */
const MAX_DELIVERIES = 5;

/**
 * Declares what Cook Assistance owns and binds its queue to the Grandma Avatar AI's exchange — which that
 * context owns and declares (ADR-CA-008: our broker user may only publish on our exchange and use our queues).
 */
export async function declareTopology(channel: ConfirmChannel): Promise<void> {
  await channel.assertExchange(TOPOLOGY.publishExchange, "topic", { durable: true });
  await channel.assertExchange(TOPOLOGY.deadLetterExchange, "fanout", { durable: true });
  await channel.assertQueue(TOPOLOGY.deadLetterQueue, { durable: true });
  await channel.bindQueue(TOPOLOGY.deadLetterQueue, TOPOLOGY.deadLetterExchange, "");
  await channel.assertQueue(TOPOLOGY.grandmaAvatarQueue, {
    durable: true,
    arguments: {
      "x-queue-type": "quorum",
      "x-delivery-limit": MAX_DELIVERIES - 1,
      "x-dead-letter-exchange": TOPOLOGY.deadLetterExchange,
    },
  });
  await channel.bindQueue(TOPOLOGY.grandmaAvatarQueue, TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey);
}

// op: publishHelpRequested, publishHelpProvided · exchange cook-assistance (ADR-CA-004: persistent, publisher confirms)
export class AmqpPublisher implements Publisher {
  constructor(private readonly channel: ChannelWrapper) {}

  /** Resolves once the broker confirmed the message (amqp-connection-manager opens confirm channels). */
  async publish(message: OutgoingMessage): Promise<void> {
    await this.channel.publish(message.exchange, message.routingKey, Buffer.from(JSON.stringify(message.payload)), {
      persistent: true,
      contentType: "application/json",
      messageId: message.headers.messageId,
      correlationId: message.headers.correlationId,
      headers: { ...message.headers },
    });
  }
}

/** What the binding hands deliveries to: the step 9 consumer, or anything with its shape. */
export interface MessageConsumer {
  consume(message: RawMessage, delivery: Delivery): Promise<void>;
}

/** The message body as JSON; a body that is not JSON becomes undefined, which the consumer's validation dead-letters. */
function jsonOrUndefined(content: Buffer): unknown {
  try {
    return JSON.parse(content.toString());
  } catch {
    return undefined;
  }
}

// op: receiveGrandmaAvatarHelpProvided · queue cook-assistance.grandma-avatar-help-provided (ADR-CA-004: manual ack)
export async function subscribeConsumer(channel: ChannelWrapper, consumer: MessageConsumer): Promise<void> {
  // a setup runs again after every reconnect
  await channel.addSetup(async (amqpChannel: ConfirmChannel) => {
    await amqpChannel.prefetch(1);
    await amqpChannel.consume(
      TOPOLOGY.grandmaAvatarQueue,
      (delivered) => {
        if (delivered === null) {
          return; // the broker cancelled the consumer; amqp-connection-manager sets it up again
        }
        const delivery: Delivery = {
          ack: () => amqpChannel.ack(delivered),
          deadLetter: () => amqpChannel.nack(delivered, false, false),
          retry: () => amqpChannel.nack(delivered, false, true),
        };
        consumer
          .consume({ headers: delivered.properties.headers ?? {}, payload: jsonOrUndefined(delivered.content) }, delivery)
          .catch(() => delivery.retry());
      },
      { noAck: false },
    );
  });
}
