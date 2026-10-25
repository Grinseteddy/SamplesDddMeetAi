import { describe, it } from "vitest";

// Broker adapters — increment 0, step 10, unit 10.3: amqp-connection-manager against a real RabbitMQ (Testcontainers).

describe("RabbitMQ adapters", () => {
  it.todo("topology: topic exchange cook-assistance (durable); quorum queue cook-assistance.grandma-avatar-help-provided with x-delivery-limit 5 and a dead-letter exchange, bound to grandma-avatar / grandma-avatar.help.provided; a dead-letter queue -- ADR-CA-004");
  it.todo("publisher: a message arrives on a queue bound to help.requested — persistent, JSON, with its headers — and publish resolves only after the broker's confirm -- ADR-CA-004");
  it.todo("consumer binding: a message on the queue reaches the consumer and an ack removes it -- op receiveGrandmaAvatarHelpProvided ADR-CA-004");
  it.todo("consumer binding: deadLetter moves the message to the dead-letter queue -- ADR-CA-004");
  it.todo("consumer binding: retry redelivers; after 5 deliveries the quorum queue dead-letters it -- ADR-CA-004");
});
