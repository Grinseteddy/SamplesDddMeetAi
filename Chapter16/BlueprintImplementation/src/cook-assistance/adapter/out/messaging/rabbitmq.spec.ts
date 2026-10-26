import amqp, { type ChannelModel, type ConfirmChannel } from "amqplib";
import { afterAll, beforeAll, describe, expect, it } from "vitest";
import { connect } from "amqp-connection-manager";
import { AmqpPublisher, declareTopology, type MessageConsumer, subscribeConsumer, TOPOLOGY } from "./rabbitmq.js";
import { startRabbitMq, type TestBroker } from "./test-support/rabbitmq-container.js";

// Broker adapters — increment 0, step 10, unit 10.3: against a real RabbitMQ (Testcontainers).

/** Polls until the condition holds (deliveries arrive asynchronously), at most two seconds. */
async function eventually(condition: () => Promise<boolean> | boolean): Promise<void> {
  for (let tries = 0; tries < 40 && !(await condition()); tries += 1) {
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
}

describe("RabbitMQ adapters", () => {
  let broker: TestBroker;
  let connection: ChannelModel;
  let channel: ConfirmChannel;

  beforeAll(async () => {
    broker = await startRabbitMq();
    connection = await amqp.connect(broker.url);
    channel = await connection.createConfirmChannel();
    // the Grandma Avatar AI context owns and declares its exchange; the test stands in for it
    await channel.assertExchange(TOPOLOGY.grandmaAvatarExchange, "topic", { durable: true });
  }, 120_000);

  afterAll(async () => {
    await connection.close();
    await broker.stop();
  });

  it("topology: topic exchange cook-assistance (durable); quorum queue cook-assistance.grandma-avatar-help-provided with x-delivery-limit 5 and a dead-letter exchange, bound to grandma-avatar / grandma-avatar.help.provided; a dead-letter queue -- ADR-CA-004", async () => {
    await declareTopology(channel);

    await channel.checkExchange(TOPOLOGY.publishExchange);
    await channel.checkQueue(TOPOLOGY.deadLetterQueue);
    channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from("{}"), { persistent: true });
    await channel.waitForConfirms();
    const delivered = await channel.get(TOPOLOGY.grandmaAvatarQueue, { noAck: true });
    expect(delivered && delivered.content.toString()).toBe("{}");
  });
  it("publisher: a message arrives on a queue bound to help.requested — persistent, JSON, with its headers — and publish resolves only after the broker's confirm -- ADR-CA-004", async () => {
    await declareTopology(channel);
    const { queue } = await channel.assertQueue("", { exclusive: true });
    await channel.bindQueue(queue, TOPOLOGY.publishExchange, "help.requested");
    const manager = connect([broker.url]);
    const wrapper = manager.createChannel({ json: false });
    await wrapper.waitForConnect();
    const message = {
      exchange: "cook-assistance",
      routingKey: "help.requested",
      headers: {
        messageId: "5d7e9f1a-2b3c-4d5e-8f6a-7b8c9d0e1f2a",
        correlationId: "0192f1a0-0000-7000-8000-000000000001",
        source: "cook-assistance",
      },
      payload: { helpRequestId: "0192f1a0-0000-7000-8000-000000000001", title: "Sunday scones, step 3" },
    };

    let delivered: Awaited<ReturnType<ConfirmChannel["get"]>>;
    try {
      await new AmqpPublisher(wrapper).publish(message);
      delivered = await channel.get(queue, { noAck: true });
    } finally {
      await manager.close();
    }

    expect(delivered && JSON.parse(delivered.content.toString())).toEqual(message.payload);
    expect(delivered && delivered.properties.deliveryMode).toBe(2);
    expect(delivered && delivered.properties.messageId).toBe("5d7e9f1a-2b3c-4d5e-8f6a-7b8c9d0e1f2a");
    expect(delivered && delivered.properties.headers).toMatchObject(message.headers);
  });
  it("publisher: while the broker does not answer, publish rejects after its timeout instead of staying pending -- ADR-CA-004 QS-07 (inspection m-10)", async () => {
    await declareTopology(channel);
    const manager = connect([broker.url]);
    const wrapper = manager.createChannel({ json: false });
    await wrapper.waitForConnect();
    const message = {
      exchange: "cook-assistance",
      routingKey: "help.requested",
      headers: {
        messageId: "6e8f0a2b-3c4d-4e5f-8a6b-7c8d9e0f1a2b",
        correlationId: "0192f1a0-0000-7000-8000-000000000001",
        source: "cook-assistance",
      },
      payload: { helpRequestId: "0192f1a0-0000-7000-8000-000000000001" },
    };

    await broker.pause();
    try {
      const outcome = await Promise.race([
        new AmqpPublisher(wrapper).publish(message).then(
          () => "confirmed",
          () => "rejected",
        ),
        new Promise((resolve) => setTimeout(() => resolve("still pending"), 10_000)),
      ]);
      expect(outcome).toBe("rejected");
    } finally {
      await broker.resume();
      await manager.close();
    }
  }, 30_000);
  it("consumer binding: a message on the queue reaches the consumer and an ack removes it -- op receiveGrandmaAvatarHelpProvided ADR-CA-004", async () => {
    await declareTopology(channel);
    await channel.purgeQueue(TOPOLOGY.grandmaAvatarQueue);
    const received: unknown[] = [];
    const acking: MessageConsumer = {
      consume: async (message, delivery) => {
        received.push(message.payload);
        delivery.ack();
      },
    };
    const manager = connect([broker.url]);
    try {
      const wrapper = manager.createChannel({ json: false });
      await wrapper.waitForConnect();
      await subscribeConsumer(wrapper, acking);

      channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from('{"helpId":"a1"}'), {
        headers: {
          messageId: "8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d",
          correlationId: "0b1c2d3e-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
          source: "grandma-avatar",
        },
      });
      await channel.waitForConfirms();
      await eventually(() => received.length === 1);
      await eventually(async () => (await channel.checkQueue(TOPOLOGY.grandmaAvatarQueue)).messageCount === 0);

      expect(received).toEqual([{ helpId: "a1" }]);
      expect((await channel.checkQueue(TOPOLOGY.grandmaAvatarQueue)).messageCount).toBe(0);
    } finally {
      await manager.close();
    }
  });
  it("consumer binding: a message without AMQP headers reaches the consumer with empty headers (its validation refuses it) -- ADR-CA-004 (AP0005 coverage)", async () => {
    await declareTopology(channel);
    await channel.purgeQueue(TOPOLOGY.grandmaAvatarQueue);
    const received: unknown[] = [];
    const recording: MessageConsumer = {
      consume: async (message, delivery) => {
        received.push(message.headers);
        delivery.ack();
      },
    };
    const manager = connect([broker.url]);
    try {
      const wrapper = manager.createChannel({ json: false });
      await wrapper.waitForConnect();
      await subscribeConsumer(wrapper, recording);

      channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from("{}"));
      await channel.waitForConfirms();
      await eventually(() => received.length === 1);

      expect(received).toEqual([{}]);
    } finally {
      await manager.close();
    }
  });
  it("consumer binding: deadLetter moves the message to the dead-letter queue -- ADR-CA-004", async () => {
    await declareTopology(channel);
    await channel.purgeQueue(TOPOLOGY.grandmaAvatarQueue);
    await channel.purgeQueue(TOPOLOGY.deadLetterQueue);
    const refusing: MessageConsumer = { consume: async (_message, delivery) => delivery.deadLetter() };
    const manager = connect([broker.url]);
    try {
      const wrapper = manager.createChannel({ json: false });
      await wrapper.waitForConnect();
      await subscribeConsumer(wrapper, refusing);

      channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from("not json"));
      await channel.waitForConfirms();
      await eventually(async () => (await channel.checkQueue(TOPOLOGY.deadLetterQueue)).messageCount === 1);

      expect((await channel.checkQueue(TOPOLOGY.deadLetterQueue)).messageCount).toBe(1);
      expect((await channel.checkQueue(TOPOLOGY.grandmaAvatarQueue)).messageCount).toBe(0);
    } finally {
      await manager.close();
    }
  });
  it("consumer binding: retry redelivers; after 5 deliveries the quorum queue dead-letters it -- ADR-CA-004", async () => {
    await declareTopology(channel);
    await channel.purgeQueue(TOPOLOGY.grandmaAvatarQueue);
    await channel.purgeQueue(TOPOLOGY.deadLetterQueue);
    let deliveries = 0;
    const failing: MessageConsumer = {
      consume: async (_message, delivery) => {
        deliveries += 1;
        delivery.retry();
      },
    };
    const manager = connect([broker.url]);
    try {
      const wrapper = manager.createChannel({ json: false });
      await wrapper.waitForConnect();
      await subscribeConsumer(wrapper, failing);

      channel.publish(TOPOLOGY.grandmaAvatarExchange, TOPOLOGY.grandmaAvatarRoutingKey, Buffer.from("{}"));
      await channel.waitForConfirms();
      await eventually(async () => (await channel.checkQueue(TOPOLOGY.deadLetterQueue)).messageCount === 1);

      expect(deliveries).toBe(5);
      expect((await channel.checkQueue(TOPOLOGY.deadLetterQueue)).messageCount).toBe(1);
    } finally {
      await manager.close();
    }
  });
});
