import { beforeEach, describe, expect, it } from "vitest";
import { Help, HelpId } from "../../domain/help/help.js";
import { ConcurrencyConflict } from "../../domain/shared/concurrency-conflict.js";
import type { HelpRepository } from "../../domain/help/help-repository.js";
import { grandmaExplainsWarm } from "../fixtures/sunday-scones.js";

// Shared repository contract (step 8, unit 1b): run against the in-memory fake AND the Kysely adapter,
// so the fake the policy handler is tested with behaves like the real one (ADR-CA-003, ADR-CA-004, ADR-CA-005).
export interface HelpRepositoryUnderTest {
  readonly repository: HelpRepository;
  /** The event types the repository recorded with its saves (the fake's list or the outbox rows), in order. */
  savedEventTypes(): Promise<string[]>;
}

/** The repository set up for the running test. */
type Current = () => HelpRepositoryUnderTest;

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

const newGrandmaHelp = () => Help.provide(grandmaExplainsWarm);

function lookupAndRoundTrip(current: Current): void {
  it("an unknown help id is absent -- B1-S04 contract", async () => {
    const found = await current().repository.findById(HelpId.of("0192f1a0-0000-7000-8000-0000000000ff"));

    expect(found).toBeUndefined();
  });
  it("a saved help is found again equal, with no pending events -- B1-S04 contract ADR-CA-003", async () => {
    const help = newGrandmaHelp();
    const expected = snapshotOf(help);
    await current().repository.save(help);

    const found = await current().repository.findById(grandmaExplainsWarm.id);

    expect(snapshotOf(found)).toEqual(expected);
    expect(found?.pullEvents()).toEqual([]);
  });
}

function versionAndOptimisticLocking(current: Current): void {
  it("saving a new help stores version 1; saving the loaded one stores version 2 -- B1-S04 contract ADR-CA-005", async () => {
    await current().repository.save(newGrandmaHelp());
    const loaded = (await current().repository.findById(grandmaExplainsWarm.id)) as Help;
    expect(loaded.version).toBe(1);

    await current().repository.save(loaded);

    expect((await current().repository.findById(grandmaExplainsWarm.id))?.version).toBe(2);
  });
  it("of two saves of the same loaded help, the second fails with ConcurrencyConflict -- B1-S04 contract ADR-CA-005", async () => {
    await current().repository.save(newGrandmaHelp());
    const first = (await current().repository.findById(grandmaExplainsWarm.id)) as Help;
    const second = (await current().repository.findById(grandmaExplainsWarm.id)) as Help;
    await current().repository.save(first);

    await expect(current().repository.save(second)).rejects.toBeInstanceOf(ConcurrencyConflict);
  });
}

function savingANewHelp(current: Current): void {
  it("saving records the help's pending events (HelpProvided) -- B1-S04 contract ADR-CA-004", async () => {
    await current().repository.save(newGrandmaHelp());

    expect(await current().savedEventTypes()).toEqual(["HelpProvided"]);
  });
  it("saving a new help under an id that is already stored fails with ConcurrencyConflict -- B1-S04 contract ADR-CA-005", async () => {
    await current().repository.save(newGrandmaHelp());

    await expect(current().repository.save(newGrandmaHelp())).rejects.toBeInstanceOf(ConcurrencyConflict);
  });
}

export function helpRepositoryContract(name: string, setUp: () => Promise<HelpRepositoryUnderTest>): void {
  describe(`HelpRepository contract — ${name}`, () => {
    let underTest: HelpRepositoryUnderTest;

    beforeEach(async () => {
      underTest = await setUp();
    });

    const current: Current = () => underTest;
    lookupAndRoundTrip(current);
    versionAndOptimisticLocking(current);
    savingANewHelp(current);
  });
}
