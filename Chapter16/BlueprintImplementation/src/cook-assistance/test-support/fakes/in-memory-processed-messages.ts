import { MessageAlreadyProcessed, type MessageId, type ProcessedMessages } from "../../application/ports/processed-messages.js";

// In-memory fake of ProcessedMessages for policy-handler tests; must pass the shared contract.
export class InMemoryProcessedMessages implements ProcessedMessages {
  private readonly recorded = new Set<MessageId>();

  async hasBeenProcessed(messageId: MessageId): Promise<boolean> {
    return this.recorded.has(messageId);
  }

  async record(messageId: MessageId): Promise<void> {
    // ADR-CA-004 · the fake's counterpart of the primary-key violation on processed_message
    if (this.recorded.has(messageId)) {
      throw new MessageAlreadyProcessed(messageId);
    }
    this.recorded.add(messageId);
  }
}
