import { RabbitMQContainer, type StartedRabbitMQContainer } from "@testcontainers/rabbitmq";

export interface TestBroker {
  readonly url: string;
  stop(): Promise<void>;
}

/** A throw-away RabbitMQ with quorum-queue support (ADR-CA-004), started per test file. */
export async function startRabbitMq(): Promise<TestBroker> {
  const container: StartedRabbitMQContainer = await new RabbitMQContainer("rabbitmq:3.13.7-alpine").start();
  return { url: container.getAmqpUrl(), stop: async () => void (await container.stop()) };
}
