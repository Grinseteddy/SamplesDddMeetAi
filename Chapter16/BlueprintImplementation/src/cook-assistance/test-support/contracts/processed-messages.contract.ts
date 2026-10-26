import { beforeEach, describe, expect, it } from "vitest";
import { MessageAlreadyProcessed, MessageId, type ProcessedMessages } from "../../application/ports/processed-messages.js";

// Shared processed-messages contract (step 8, unit 1c): run against the in-memory fake AND the Kysely adapter,
// so the fake the policy handler is tested with behaves like the real one (ADR-CA-004, idempotent consumers).
export function processedMessagesContract(name: string, setUp: () => Promise<ProcessedMessages>): void {
  describe(`ProcessedMessages contract — ${name}`, () => {
    let processedMessages: ProcessedMessages;

    beforeEach(async () => {
      processedMessages = await setUp();
    });

    it("a message id that was never recorded has not been processed -- B1-S04 contract ADR-CA-004", async () => {
      expect(await processedMessages.hasBeenProcessed(MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c01"))).toBe(false);
    });
    it("a recorded message id has been processed -- B1-S04 contract ADR-CA-004", async () => {
      const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c02");

      await processedMessages.record(messageId);

      expect(await processedMessages.hasBeenProcessed(messageId)).toBe(true);
    });
    it("recording the same message id a second time is refused with MessageAlreadyProcessed -- B1-S04 contract ADR-CA-004", async () => {
      const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c03");
      await processedMessages.record(messageId);

      await expect(processedMessages.record(messageId)).rejects.toBeInstanceOf(MessageAlreadyProcessed);
    });
  });
}
