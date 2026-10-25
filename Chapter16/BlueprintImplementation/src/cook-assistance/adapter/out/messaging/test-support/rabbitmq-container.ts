import { RabbitMQContainer, type StartedRabbitMQContainer } from "@testcontainers/rabbitmq";
import amqp from "amqplib";

export interface TestBroker {
  readonly url: string;
  stop(): Promise<void>;
}

const CONNECT_ATTEMPTS = 40;
const CONNECT_PAUSE_MS = 250;

/**
 * The container reports "started" before its mapped AMQP port accepts connections under parallel load
 * (ECONNREFUSED in setup, step 11): wait until a connection succeeds, at most 10 s, then hand the broker over.
 */
async function untilAcceptingConnections(url: string): Promise<void> {
  for (let attempt = 1; ; attempt += 1) {
    try {
      await (await amqp.connect(url)).close();
      return;
    } catch (error) {
      if (attempt >= CONNECT_ATTEMPTS) {
        throw error;
      }
      await new Promise((resolve) => setTimeout(resolve, CONNECT_PAUSE_MS));
    }
  }
}

/** A throw-away RabbitMQ with quorum-queue support (ADR-CA-004), started per test file. */
export async function startRabbitMq(): Promise<TestBroker> {
  const container: StartedRabbitMQContainer = await new RabbitMQContainer("rabbitmq:3.13.7-alpine").start();
  await untilAcceptingConnections(container.getAmqpUrl());
  return { url: container.getAmqpUrl(), stop: async () => void (await container.stop()) };
}
