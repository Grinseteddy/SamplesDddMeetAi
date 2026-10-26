// What the inbound consumers and the outbound RabbitMQ binding share (ADR-CA-002, ADR-CA-004): neither side imports the
// other; both import this module.

/** A message as the transport hands it over: AMQP headers and the JSON payload, not yet trusted. */
export interface RawMessage {
  readonly headers: Readonly<Record<string, unknown>>;
  readonly payload: unknown;
}

/** How the transport settles a message (ADR-CA-004): ack, dead letter, or redeliver. */
export interface Delivery {
  ack(): void;
  deadLetter(): void;
  retry(): void;
}
