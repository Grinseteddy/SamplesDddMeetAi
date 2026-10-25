import type { GrandmaAvatarHelpProvidedPayload } from "../../../../generated/asyncapi/cook-assistance.js";
import type {
  GrandmaAvatarAnswered,
  GrandmaAvatarAnswerOutcome,
  ProvideHelpWhenGrandmaAvatarAnswers,
} from "../../../application/provide-help-when-grandma-avatar-answers.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { toGrandmaAvatarAnswered } from "./grandma-avatar.acl.js";
import { loadMessageSchemas, type MessageSchemas } from "./message-schemas.js";

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

/** A structured log event without personal data (ADR-CA-010). */
export interface ConsumerLogEvent {
  readonly event: "grandma-avatar-help-provided";
  readonly messageId: string | null;
  readonly outcome: GrandmaAvatarAnswerOutcome["outcome"];
  readonly rule: string | null;
}

export interface GrandmaAvatarConsumerPorts {
  readonly handler: Pick<ProvideHelpWhenGrandmaAvatarAnswers, "handle">;
  readonly log: (event: ConsumerLogEvent) => void;
}

const EXPECTED_SOURCE = "grandma-avatar";

/** How a trusted message that could not be handled is settled. */
function settleFailure(error: unknown, delivery: Delivery): void {
  // F5 · a topic not served yet is refused for good: acknowledged, never redelivered
  if (error instanceof TopicNotYetSupported) {
    delivery.ack();
    return;
  }
  // ADR-CA-004 · anything else is redelivered; the quorum queue dead-letters it after 5 deliveries
  delivery.retry();
}

// op: receiveGrandmaAvatarHelpProvided (channel cook-assistance.grandma-avatar-help-provided) · policy: P2
export class GrandmaAvatarHelpProvidedConsumer {
  constructor(
    private readonly ports: GrandmaAvatarConsumerPorts,
    private readonly schemas: MessageSchemas,
  ) {}

  async consume(message: RawMessage, delivery: Delivery): Promise<void> {
    if (!this.isTrusted(message)) {
      delivery.deadLetter();
      return;
    }
    try {
      const input = toGrandmaAvatarAnswered(String(message.headers.messageId), message.payload as GrandmaAvatarHelpProvidedPayload);
      this.logOutcome(input, await this.ports.handler.handle(input));
      delivery.ack();
    } catch (error) {
      settleFailure(error, delivery);
    }
  }

  /** ADR-CA-011 · valid per the spec; ADR-CA-008 · published by the Grandma Avatar AI. Anything else is never retried. */
  private isTrusted(message: RawMessage): boolean {
    return (
      this.schemas.headers(message.headers) &&
      this.schemas.payload(message.payload) &&
      message.headers.source === EXPECTED_SOURCE
    );
  }

  /** ADR-CA-010 · the named outcome, without question or answer text. */
  private logOutcome(input: GrandmaAvatarAnswered, result: GrandmaAvatarAnswerOutcome): void {
    this.ports.log({
      event: "grandma-avatar-help-provided",
      messageId: input.messageId,
      outcome: result.outcome,
      rule: result.outcome === "REFUSED" ? result.rule : null,
    });
  }
}

/** Builds the consumer once the spec's schemas are loaded. */
export async function createGrandmaAvatarHelpProvidedConsumer(ports: GrandmaAvatarConsumerPorts): Promise<GrandmaAvatarHelpProvidedConsumer> {
  return new GrandmaAvatarHelpProvidedConsumer(ports, await loadMessageSchemas("receiveGrandmaAvatarHelpProvided"));
}
