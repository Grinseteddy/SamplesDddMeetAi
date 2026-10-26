import { beforeEach, describe, expect, it } from "vitest";
import { HelpRequest, HelpRequestId } from "../../domain/help-request/help-request.js";
import { CookId } from "../../domain/shared/ids.js";
import { ConcurrencyConflict } from "../../domain/shared/concurrency-conflict.js";
import type { HelpRequestRepository } from "../../domain/help-request/help-request-repository.js";
import { sundaySconesStep3 } from "../fixtures/sunday-scones.js";

// Shared repository contract (step 8, unit 1a): run against the in-memory fake AND the Kysely adapter,
// so the fake the use cases are tested with behaves like the real one (ADR-CA-003, ADR-CA-004, ADR-CA-005).
export interface HelpRequestRepositoryUnderTest {
  readonly repository: HelpRequestRepository;
  /** The event types the repository recorded with its saves (the fake's list or the outbox rows), in order. */
  savedEventTypes(): Promise<string[]>;
}

/** The repository set up for the running test. */
type Current = () => HelpRequestRepositoryUnderTest;

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
        chefId: helpRequest.chefId,
        plannedMealDate: helpRequest.plannedMealDate,
        status: helpRequest.status,
        answerDeadline: helpRequest.answerDeadline,
      };
}

const newSundaySconesRequest = () => HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

function lookupAndRoundTrip(current: Current): void {
  it("an unknown help request id is absent -- B2-S01 contract", async () => {
    const found = await current().repository.findById(HelpRequestId.of("0192f1a0-0000-7000-8000-0000000000ff"));

    expect(found).toBeUndefined();
  });
  it("a saved help request is found again equal, with no pending events -- B2-S01 contract ADR-CA-003", async () => {
    const helpRequest = newSundaySconesRequest();
    const expected = snapshotOf(helpRequest);
    await current().repository.save(helpRequest);

    const found = await current().repository.findById(sundaySconesStep3.id);

    expect(snapshotOf(found)).toEqual(expected);
    expect(found?.pullEvents()).toEqual([]);
  });
}

function menuRequests(current: Current): void {
  it("a saved Menu request for Chef Anna is found again equal — topic, Chef and meal date included -- B1-S01 contract ADR-CA-003 (increment 1)", async () => {
    const lunchWithChefAnna = HelpRequest.requestMealPlanHelp({
      id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000102"),
      requester: sundaySconesStep3.requester,
      title: "Lunch planning",
      question: "A light lunch for two — what fits a warm day?",
      addressees: ["CHEF"],
      chefId: CookId.of("1c2d3e4f-5a6b-4c7d-8e9f-0a1b2c3d4e5f"),
      plannedMealDate: "2026-10-30",
      requestedAt: new Date("2026-10-21T10:00:00Z"),
    });
    const expected = snapshotOf(lunchWithChefAnna);
    await current().repository.save(lunchWithChefAnna);

    const found = await current().repository.findById(HelpRequestId.of("0192f1a0-0000-7000-8000-000000000102"));

    expect(snapshotOf(found)).toEqual(expected);
  });
}

function versionAndOptimisticLocking(current: Current): void {
  it("saving a new help request stores version 1; saving the loaded one stores version 2 -- B2-S01 contract ADR-CA-005", async () => {
    await current().repository.save(newSundaySconesRequest());
    const loaded = (await current().repository.findById(sundaySconesStep3.id)) as HelpRequest;
    expect(loaded.version).toBe(1);

    await current().repository.save(loaded);

    expect((await current().repository.findById(sundaySconesStep3.id))?.version).toBe(2);
  });
  it("of two saves of the same loaded help request, the second fails with ConcurrencyConflict -- B2-S01 contract ADR-CA-005", async () => {
    await current().repository.save(newSundaySconesRequest());
    const first = (await current().repository.findById(sundaySconesStep3.id)) as HelpRequest;
    const second = (await current().repository.findById(sundaySconesStep3.id)) as HelpRequest;
    await current().repository.save(first);

    await expect(current().repository.save(second)).rejects.toBeInstanceOf(ConcurrencyConflict);
  });
}

function savingANewHelpRequest(current: Current): void {
  it("saving records the help request's pending events (HelpRequested) -- B2-S01 contract ADR-CA-004", async () => {
    await current().repository.save(newSundaySconesRequest());

    expect(await current().savedEventTypes()).toEqual(["HelpRequested"]);
  });
  it("saving a new help request under an id that is already stored fails with ConcurrencyConflict -- B2-S01 contract ADR-CA-005", async () => {
    await current().repository.save(newSundaySconesRequest());

    await expect(current().repository.save(newSundaySconesRequest())).rejects.toBeInstanceOf(ConcurrencyConflict);
  });
}

export function helpRequestRepositoryContract(name: string, setUp: () => Promise<HelpRequestRepositoryUnderTest>): void {
  describe(`HelpRequestRepository contract — ${name}`, () => {
    let underTest: HelpRequestRepositoryUnderTest;

    beforeEach(async () => {
      underTest = await setUp();
    });

    const current: Current = () => underTest;
    lookupAndRoundTrip(current);
    menuRequests(current);
    versionAndOptimisticLocking(current);
    savingANewHelpRequest(current);
  });
}
