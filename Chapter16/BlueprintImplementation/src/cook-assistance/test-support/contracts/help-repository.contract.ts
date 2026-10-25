import { beforeEach, describe, expect, it } from "vitest";
import { HelpRequestId } from "../../domain/help-request/help-request.js";
import { Help, HelpId } from "../../domain/help/help.js";
import { ConcurrencyConflict } from "../../domain/shared/concurrency-conflict.js";
import { CookId, HowToStepId, RecipeId } from "../../domain/shared/ids.js";
import type { HelpRepository } from "../../domain/help/help-repository.js";

// Shared repository contract (step 8, unit 1b): run against the in-memory fake AND the Kysely adapter,
// so the fake the policy handler is tested with behaves like the real one (ADR-CA-003, ADR-CA-004, ADR-CA-005).
export interface HelpRepositoryUnderTest {
  readonly repository: HelpRepository;
  /** The event types the repository recorded with its saves (the fake's list or the outbox rows), in order. */
  savedEventTypes(): Promise<string[]>;
}

// Grandma Avatar explains what "warm" means in Sunday scones, step 3 (Example Mapping Preparation step).
const grandmaExplainsWarm = {
  id: HelpId.of("0192f1a0-0000-7000-8000-0000000000a1"),
  helpRequest: {
    id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000001"),
    requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
    topic: "PREPARATION_STEP",
    recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
    howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
  },
  answerTitle: "Warm means about 30 °C",
  provider: { type: "GRANDMA_AVATAR", id: null },
  answer: {
    topic: "PREPARATION_STEP",
    recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
    howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
    description: "Put the bowl next to the heater or in the oven with only the light on.",
    images: [],
  },
  providedAt: new Date("2026-10-24T12:00:00Z"),
} as const;

/** Everything a help exposes except its version, for equality checks. */
function snapshotOf(help: Help | undefined) {
  return help === undefined
    ? undefined
    : {
        id: help.id,
        helpRequestId: help.helpRequestId,
        helpRequester: help.helpRequester,
        answerTitle: help.answerTitle,
        provider: help.provider,
        answer: help.answer,
        status: help.status,
      };
}

export function helpRepositoryContract(name: string, setUp: () => Promise<HelpRepositoryUnderTest>): void {
  describe(`HelpRepository contract — ${name}`, () => {
    let underTest: HelpRepositoryUnderTest;

    beforeEach(async () => {
      underTest = await setUp();
    });

    it("an unknown help id is absent -- B1-S04 contract", async () => {
      const found = await underTest.repository.findById(HelpId.of("0192f1a0-0000-7000-8000-0000000000ff"));

      expect(found).toBeUndefined();
    });
    it("a saved help is found again equal, with no pending events -- B1-S04 contract ADR-CA-003", async () => {
      const help = Help.provide(grandmaExplainsWarm);
      const expected = snapshotOf(help);
      await underTest.repository.save(help);

      const found = await underTest.repository.findById(grandmaExplainsWarm.id);

      expect(snapshotOf(found)).toEqual(expected);
      expect(found?.pullEvents()).toEqual([]);
    });
    it("saving a new help stores version 1; saving the loaded one stores version 2 -- B1-S04 contract ADR-CA-005", async () => {
      await underTest.repository.save(Help.provide(grandmaExplainsWarm));
      const loaded = (await underTest.repository.findById(grandmaExplainsWarm.id)) as Help;
      expect(loaded.version).toBe(1);

      await underTest.repository.save(loaded);

      expect((await underTest.repository.findById(grandmaExplainsWarm.id))?.version).toBe(2);
    });
    it("of two saves of the same loaded help, the second fails with ConcurrencyConflict -- B1-S04 contract ADR-CA-005", async () => {
      await underTest.repository.save(Help.provide(grandmaExplainsWarm));
      const first = (await underTest.repository.findById(grandmaExplainsWarm.id)) as Help;
      const second = (await underTest.repository.findById(grandmaExplainsWarm.id)) as Help;
      await underTest.repository.save(first);

      await expect(underTest.repository.save(second)).rejects.toBeInstanceOf(ConcurrencyConflict);
    });
    it("saving records the help's pending events (HelpProvided) -- B1-S04 contract ADR-CA-004", async () => {
      await underTest.repository.save(Help.provide(grandmaExplainsWarm));

      expect(await underTest.savedEventTypes()).toEqual(["HelpProvided"]);
    });
  });
}
