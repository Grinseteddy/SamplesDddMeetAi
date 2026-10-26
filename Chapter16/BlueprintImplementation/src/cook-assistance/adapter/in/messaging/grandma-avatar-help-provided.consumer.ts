import type { GrandmaAvatarHelpProvidedPayload } from "../../../../generated/asyncapi/cook-assistance.js";
import type {
  GrandmaAvatarAnswered,
  GrandmaAvatarAnswerOutcome,
  ProvideHelpWhenGrandmaAvatarAnswers,
} from "../../../application/provide-help-when-grandma-avatar-answers.js";
import { TopicNotYetSupported } from "../../../application/topic-not-yet-supported.js";
import { toGrandmaAvatarAnswered } from "./grandma-avatar.acl.js";
import type { Delivery, RawMessage } from "../../messaging/delivery.js";
import { loadMessageSchemas, type MessageSchemas } from "../../messaging/message-schemas.js";

/** A structured log event without personal data (ADR-CA-010). */
export interface ConsumerLogEvent {
  readonly event: "grandma-avatar-help-provided";
  readonly messageId: string | null;
  readonly outcome: GrandmaAvatarAnswerOutcome["outcome"];
  readonly rule: string | null;
  /** Why a message was refused before the policy ran (F5: TopicNotYetSupported). */
  readonly reason?: string;
}

export interface GrandmaAvatarConsumerPorts {
  readonly handler: Pick<ProvideHelpWhenGrandmaAvatarAnswers, "handle">;
  readonly log: (event: ConsumerLogEvent) => void;
}

const EXPECTED_SOURCE = "grandma-avatar";

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
      this.settleFailure(message, error, delivery);
    }
  }

  /** How a trusted message that could not be handled is settled. */
  private settleFailure(message: RawMessage, error: unknown, delivery: Delivery): void {
    // F5 · a topic not served yet is refused for good: acknowledged, never redelivered — and logged, so the refusal
    // is visible (ADR-CA-004, inspection m-11)
    if (error instanceof TopicNotYetSupported) {
      this.ports.log({
        event: "grandma-avatar-help-provided",
        messageId: String(message.headers.messageId),
        outcome: "REFUSED",
        rule: null,
        reason: error.name,
      });
      delivery.ack();
      return;
    }
    // ADR-CA-004 · anything else is redelivered; the quorum queue dead-letters it after 5 deliveries
    delivery.retry();
  }

  /** ADR-CA-011 · valid per the spec; ADR-CA-008 · published by the Grandma Avatar AI. Anything else is never retried. */
  private isTrusted(message: RawMessage): boolean {
    return this.schemas.headers(message.headers) && this.schemas.payload(message.payload) && message.headers.source === EXPECTED_SOURCE;
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
export async function createGrandmaAvatarHelpProvidedConsumer(
  ports: GrandmaAvatarConsumerPorts,
): Promise<GrandmaAvatarHelpProvidedConsumer> {
  return new GrandmaAvatarHelpProvidedConsumer(ports, await loadMessageSchemas("receiveGrandmaAvatarHelpProvided"));
}
