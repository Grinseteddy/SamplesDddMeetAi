import type { OutgoingMessage } from "../../adapter/out/messaging/outbound-messages.js";
import type { Publisher } from "../../adapter/out/messaging/outbox-relay.js";

/** A broker stand-in: records what was confirmed, can fail the next n publishes and delay each one. */
export class FakePublisher implements Publisher {
  readonly published: OutgoingMessage[] = [];
  private failures = 0;

  constructor(private readonly delayMs = 0) {}

  failNext(times: number): void {
    this.failures = times;
  }

  async publish(message: OutgoingMessage): Promise<void> {
    if (this.delayMs > 0) {
      await new Promise((resolve) => setTimeout(resolve, this.delayMs));
    }
    if (this.failures > 0) {
      this.failures -= 1;
      throw new Error("broker unreachable");
    }
    this.published.push(message);
  }
}
