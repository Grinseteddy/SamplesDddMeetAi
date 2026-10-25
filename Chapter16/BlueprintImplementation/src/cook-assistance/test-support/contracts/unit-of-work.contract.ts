import { beforeEach, describe, expect, it } from "vitest";
import { MessageId } from "../../application/ports/processed-messages.js";
import type { UnitOfWork } from "../../application/ports/unit-of-work.js";
import { HelpRequest } from "../../domain/help-request/help-request.js";
import { Help } from "../../domain/help/help.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../fixtures/sunday-scones.js";

// Shared unit-of-work contract (step 8, unit 1d): run against the in-memory fake AND the Kysely adapter,
// so the policy handler's idempotency is tested against the same transaction semantics (ADR-CA-004).
export function unitOfWorkContract(name: string, setUp: () => Promise<UnitOfWork>): void {
  describe(`UnitOfWork contract — ${name}`, () => {
    let unitOfWork: UnitOfWork;

    beforeEach(async () => {
      unitOfWork = await setUp();
    });

    it("transactionally returns what the work returns -- B1-S04 contract ADR-CA-004", async () => {
      const result = await unitOfWork.transactionally(async () => "help provided");

      expect(result).toBe("help provided");
    });
    it("work that returns is committed: a help saved in the scope is found again in a later unit of work -- B1-S04 contract ADR-CA-004", async () => {
      await unitOfWork.transactionally((scope) => scope.helps.save(Help.provide(grandmaExplainsWarm)));

      const found = await unitOfWork.transactionally((scope) => scope.helps.findById(grandmaExplainsWarm.id));

      expect(found?.id).toBe("0192f1a0-0000-7000-8000-0000000000a1");
    });
    it("work that throws is rolled back: the help request, the help and the processed-message record written in it are absent afterwards, and the error reaches the caller -- B2-S01 B1-S04 contract ADR-CA-004", async () => {
      const messageId = MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c04");
      const failure = new Error("handling the message failed after writing");

      const outcome = unitOfWork.transactionally(async (scope) => {
        await scope.helpRequests.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
        await scope.helps.save(Help.provide(grandmaExplainsWarm));
        await scope.processedMessages.record(messageId);
        throw failure;
      });

      await expect(outcome).rejects.toBe(failure);
      const afterwards = await unitOfWork.transactionally(async (scope) => ({
        helpRequest: await scope.helpRequests.findById(sundaySconesStep3.id),
        help: await scope.helps.findById(grandmaExplainsWarm.id),
        processed: await scope.processedMessages.hasBeenProcessed(messageId),
      }));
      expect(afterwards).toEqual({ helpRequest: undefined, help: undefined, processed: false });
    });
  });
}
