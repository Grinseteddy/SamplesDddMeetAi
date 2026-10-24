import { beforeEach, describe, expect, it } from "vitest";
import { HelpRequest, HelpRequestId } from "../../domain/help-request/help-request.js";
import { ConcurrencyConflict } from "../../domain/shared/concurrency-conflict.js";
import { CookId, HowToStepId, RecipeId } from "../../domain/shared/ids.js";
import type { HelpRequestRepository } from "../../domain/help-request/help-request-repository.js";

// Shared repository contract (step 8, unit 1a): run against the in-memory fake AND the Kysely adapter,
// so the fake the use cases are tested with behaves like the real one (ADR-CA-003, ADR-CA-004, ADR-CA-005).
export interface HelpRequestRepositoryUnderTest {
  readonly repository: HelpRequestRepository;
  /** The event types the repository recorded with its saves (the fake's list or the outbox rows), in order. */
  savedEventTypes(): Promise<string[]>;
}

// Sunday scones, step 3 (Example Mapping Preparation step).
const sundaySconesStep3 = {
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000001"),
  requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
  title: "Sunday scones, step 3",
  question: "What means 'warm'?",
  recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
  howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
  addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
  requestedAt: new Date("2026-10-24T10:00:00Z"),
} as const;

/** Everything a help request exposes except its version, for equality checks. */
function snapshotOf(helpRequest: HelpRequest | undefined) {
  return helpRequest === undefined
    ? undefined
    : {
        id: helpRequest.id,
        requester: helpRequest.requester,
        title: helpRequest.title,
        topic: helpRequest.topic,
        question: helpRequest.question,
        recipeId: helpRequest.recipeId,
        howToStepId: helpRequest.howToStepId,
        addressees: [...helpRequest.addressees],
        status: helpRequest.status,
        answerDeadline: helpRequest.answerDeadline,
      };
}

export function helpRequestRepositoryContract(name: string, setUp: () => Promise<HelpRequestRepositoryUnderTest>): void {
  describe(`HelpRequestRepository contract — ${name}`, () => {
    let underTest: HelpRequestRepositoryUnderTest;

    beforeEach(async () => {
      underTest = await setUp();
    });

    it("an unknown help request id is absent -- B2-S01 contract", async () => {
      const found = await underTest.repository.findById(HelpRequestId.of("0192f1a0-0000-7000-8000-0000000000ff"));

      expect(found).toBeUndefined();
    });
    it("a saved help request is found again equal, with no pending events -- B2-S01 contract ADR-CA-003", async () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);
      const expected = snapshotOf(helpRequest);
      await underTest.repository.save(helpRequest);

      const found = await underTest.repository.findById(sundaySconesStep3.id);

      expect(snapshotOf(found)).toEqual(expected);
      expect(found?.pullEvents()).toEqual([]);
    });
    it("saving a new help request stores version 1; saving the loaded one stores version 2 -- B2-S01 contract ADR-CA-005", async () => {
      await underTest.repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
      const loaded = (await underTest.repository.findById(sundaySconesStep3.id)) as HelpRequest;
      expect(loaded.version).toBe(1);

      await underTest.repository.save(loaded);

      expect((await underTest.repository.findById(sundaySconesStep3.id))?.version).toBe(2);
    });
    it("of two saves of the same loaded help request, the second fails with ConcurrencyConflict -- B2-S01 contract ADR-CA-005", async () => {
      await underTest.repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));
      const first = (await underTest.repository.findById(sundaySconesStep3.id)) as HelpRequest;
      const second = (await underTest.repository.findById(sundaySconesStep3.id)) as HelpRequest;
      await underTest.repository.save(first);

      await expect(underTest.repository.save(second)).rejects.toBeInstanceOf(ConcurrencyConflict);
    });
    it("saving records the help request's pending events (HelpRequested) -- B2-S01 contract ADR-CA-004", async () => {
      await underTest.repository.save(HelpRequest.requestPreparationStepHelp(sundaySconesStep3));

      expect(await underTest.savedEventTypes()).toEqual(["HelpRequested"]);
    });
  });
}
