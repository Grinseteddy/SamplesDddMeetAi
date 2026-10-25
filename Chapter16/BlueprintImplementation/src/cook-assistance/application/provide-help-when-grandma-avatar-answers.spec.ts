import { describe, expect, it } from "vitest";
import { HelpRequest, HelpRequestId, type RequestPreparationStepHelp } from "../domain/help-request/help-request.js";
import { Help } from "../domain/help/help.js";
import { ConcurrencyConflict } from "../domain/shared/concurrency-conflict.js";
import { HowToStepId, RecipeId } from "../domain/shared/ids.js";
import { FixedClock } from "../test-support/fakes/fixed-clock.js";
import { InMemoryUnitOfWork } from "../test-support/fakes/in-memory-unit-of-work.js";
import { grandmaExplainsWarm, sundaySconesStep3 } from "../test-support/fixtures/sunday-scones.js";
import { MessageId } from "./ports/processed-messages.js";
import type { UnitOfWork } from "./ports/unit-of-work.js";
import { type GrandmaAvatarAnswered, ProvideHelpWhenGrandmaAvatarAnswers } from "./provide-help-when-grandma-avatar-answers.js";

// Application — increment 0 (walking skeleton), step 8, unit 3: policy handler ProvideHelpWhenGrandmaAvatarAnswers.
// policy: P2 · on: Grandma Avatar AI HelpProvided (translated by the ACL, step 9) · agg: Help · slice B1-S04.
// In-memory unit of work proven by the shared contracts, a fixed clock (12:00).

/** Grandma Avatar's answer to Sunday scones step 3, as the ACL hands it over (AsyncAPI example warmExplainedByAvatar). */
const grandmaAnswersWarm: GrandmaAvatarAnswered = {
  messageId: MessageId.of("8a9b0c1d-2e3f-4a4b-8c5d-6e7f8a9b0c1d"),
  helpId: grandmaExplainsWarm.id,
  helpRequestId: sundaySconesStep3.id,
  answerTitle: grandmaExplainsWarm.answerTitle,
  answer: grandmaExplainsWarm.answer,
};

/** The Cook's Sunday scones request (or a variant of it) is stored and waits for an answer. */
async function setUp(request: RequestPreparationStepHelp = sundaySconesStep3) {
  const unitOfWork = new InMemoryUnitOfWork();
  await unitOfWork.helpRequests.save(HelpRequest.requestPreparationStepHelp(request));
  const handler = new ProvideHelpWhenGrandmaAvatarAnswers({ unitOfWork, clock: new FixedClock(grandmaExplainsWarm.providedAt) });
  return { handler, unitOfWork };
}

/** A refused help leaves no help and no HelpProvided behind, but the message counts as handled (step 8 decision). */
async function expectRefusedAndRecorded(unitOfWork: InMemoryUnitOfWork): Promise<void> {
  expect(await unitOfWork.helps.findById(grandmaAnswersWarm.helpId)).toBeUndefined();
  expect(unitOfWork.helps.savedEvents).toEqual([]);
  expect(await unitOfWork.processedMessages.hasBeenProcessed(grandmaAnswersWarm.messageId)).toBe(true);
}

/**
 * Two deliveries of one message at once: the rival commits first, yet this delivery's first transaction
 * still reads the message as unprocessed (a stale read); later attempts see the truth.
 */
function racedBy(unitOfWork: InMemoryUnitOfWork, rival: () => Promise<unknown>): UnitOfWork {
  let raced = false;
  return {
    transactionally: async (work) => {
      if (raced) {
        return unitOfWork.transactionally(work);
      }
      raced = true;
      await rival();
      return unitOfWork.transactionally((scope) =>
        work({
          ...scope,
          processedMessages: {
            hasBeenProcessed: async () => false,
            record: (messageId) => scope.processedMessages.record(messageId),
          },
        }),
      );
    },
  };
}

describe("ProvideHelpWhenGrandmaAvatarAnswers", () => {
  it("Grandma Avatar explains 'warm' for Sunday scones step 3: outcome HELP_PROVIDED with the message's help id -- B1-S04 policy P2", async () => {
    const { handler } = await setUp();

    expect(await handler.handle(grandmaAnswersWarm)).toEqual({
      outcome: "HELP_PROVIDED",
      helpId: "0192f1a0-0000-7000-8000-0000000000a1",
    });
  });
  it("the saved help is PROVIDED for help request …0001 with that request's requester, provider GRANDMA_AVATAR without an id, the answer as sent, and HelpProvided saved -- B1-S04 INV-16 ADR-CA-004", async () => {
    const { handler, unitOfWork } = await setUp();

    await handler.handle(grandmaAnswersWarm);

    const saved = await unitOfWork.helps.findById(grandmaAnswersWarm.helpId);
    expect(saved?.status).toBe("PROVIDED");
    expect(saved?.helpRequestId).toBe("0192f1a0-0000-7000-8000-000000000001");
    expect(saved?.helpRequester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
    expect(saved?.answerTitle).toBe("Warm means about 30 °C");
    expect(saved?.provider).toEqual({ type: "GRANDMA_AVATAR", id: null });
    expect(saved?.answer).toEqual(grandmaAnswersWarm.answer);
    expect(unitOfWork.helps.savedEvents.map((event) => event.type)).toEqual(["HelpProvided"]);
  });
  it("the message id is recorded as processed -- B1-S04 ADR-CA-004", async () => {
    const { handler, unitOfWork } = await setUp();

    await handler.handle(grandmaAnswersWarm);

    expect(await unitOfWork.processedMessages.hasBeenProcessed(grandmaAnswersWarm.messageId)).toBe(true);
  });
  it("the same message twice: outcome ALREADY_PROCESSED, one help and one HelpProvided -- B1-S04 ADR-CA-004", async () => {
    const { handler, unitOfWork } = await setUp();
    await handler.handle(grandmaAnswersWarm);

    expect(await handler.handle(grandmaAnswersWarm)).toEqual({ outcome: "ALREADY_PROCESSED" });
    expect(unitOfWork.helps.savedEvents.map((event) => event.type)).toEqual(["HelpProvided"]);
  });
  it("an unknown help request: outcome REFUSED 'help request not found', no help, the message recorded -- B1-S04 step 8 decision", async () => {
    const { handler, unitOfWork } = await setUp();
    const unknownRequest = HelpRequestId.of("0192f1a0-0000-7000-8000-0000000000ff");

    const outcome = await handler.handle({ ...grandmaAnswersWarm, helpRequestId: unknownRequest });

    expect(outcome).toEqual({ outcome: "REFUSED", because: "help request not found", rule: null });
    await expectRefusedAndRecorded(unitOfWork);
  });
  it("a request addressed only to COMMUNITY: outcome REFUSED by INV-09 (not an addressee), no help, the message recorded -- B1-S04 INV-09 ADR-CA-009", async () => {
    const { handler, unitOfWork } = await setUp({ ...sundaySconesStep3, addressees: ["COMMUNITY"] });

    const outcome = await handler.handle(grandmaAnswersWarm);

    expect(outcome).toEqual({ outcome: "REFUSED", because: "not an addressee", rule: "INV-09" });
    await expectRefusedAndRecorded(unitOfWork);
  });
  it("an answer for another step: outcome REFUSED by INV-10 StepNotAsInRequest, no help, the message recorded -- B1-S04 INV-10", async () => {
    const { handler, unitOfWork } = await setUp();
    const sundaySconesStep4 = HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c04");

    const outcome = await handler.handle({
      ...grandmaAnswersWarm,
      answer: { ...grandmaAnswersWarm.answer, howToStepId: sundaySconesStep4 },
    });

    expect(outcome).toEqual({ outcome: "REFUSED", because: "StepNotAsInRequest", rule: "INV-10" });
    await expectRefusedAndRecorded(unitOfWork);
  });
  it("an answer for another recipe: outcome REFUSED by INV-10 RecipeNotAsInRequest, no help, the message recorded -- B1-S04 INV-10", async () => {
    const { handler, unitOfWork } = await setUp();
    const mondayMuffins = RecipeId.of("d5e6f7a8-9b0c-4d1e-8f2a-3b4c5d6e7f80");

    const outcome = await handler.handle({
      ...grandmaAnswersWarm,
      answer: { ...grandmaAnswersWarm.answer, recipeId: mondayMuffins },
    });

    expect(outcome).toEqual({ outcome: "REFUSED", because: "RecipeNotAsInRequest", rule: "INV-10" });
    await expectRefusedAndRecorded(unitOfWork);
  });
  it("a help id that is already stored: ConcurrencyConflict propagates after 3 attempts and the message is not recorded, so it can be redelivered -- B1-S04 ADR-CA-004 ADR-CA-005", async () => {
    const { unitOfWork } = await setUp();
    await unitOfWork.helps.save(Help.provide(grandmaExplainsWarm));
    let attempts = 0;
    const countingUnitOfWork: UnitOfWork = {
      transactionally: (work) => {
        attempts += 1;
        return unitOfWork.transactionally(work);
      },
    };
    const handler = new ProvideHelpWhenGrandmaAvatarAnswers({
      unitOfWork: countingUnitOfWork,
      clock: new FixedClock(grandmaExplainsWarm.providedAt),
    });

    await expect(handler.handle(grandmaAnswersWarm)).rejects.toBeInstanceOf(ConcurrencyConflict);
    expect(attempts).toBe(3);
    expect(await unitOfWork.processedMessages.hasBeenProcessed(grandmaAnswersWarm.messageId)).toBe(false);
  });
  it("a concurrent duplicate of a provided help: the second delivery hits ConcurrencyConflict on the help, is retried and ends ALREADY_PROCESSED with one help -- B1-S04 ADR-CA-004 ADR-CA-005", async () => {
    const { unitOfWork } = await setUp();
    const clock = new FixedClock(grandmaExplainsWarm.providedAt);
    const rival = new ProvideHelpWhenGrandmaAvatarAnswers({ unitOfWork, clock });
    const handler = new ProvideHelpWhenGrandmaAvatarAnswers({
      unitOfWork: racedBy(unitOfWork, () => rival.handle(grandmaAnswersWarm)),
      clock,
    });

    expect(await handler.handle(grandmaAnswersWarm)).toEqual({ outcome: "ALREADY_PROCESSED" });
    expect(unitOfWork.helps.savedEvents.map((event) => event.type)).toEqual(["HelpProvided"]);
  });
  it("a concurrent duplicate of a refused help: the second delivery hits MessageAlreadyProcessed on the record, is retried and ends ALREADY_PROCESSED -- B1-S04 ADR-CA-004 ADR-CA-005", async () => {
    const { unitOfWork } = await setUp({ ...sundaySconesStep3, addressees: ["COMMUNITY"] });
    const clock = new FixedClock(grandmaExplainsWarm.providedAt);
    const rival = new ProvideHelpWhenGrandmaAvatarAnswers({ unitOfWork, clock });
    const handler = new ProvideHelpWhenGrandmaAvatarAnswers({
      unitOfWork: racedBy(unitOfWork, () => rival.handle(grandmaAnswersWarm)),
      clock,
    });

    expect(await handler.handle(grandmaAnswersWarm)).toEqual({ outcome: "ALREADY_PROCESSED" });
  });
  // Planned for increment 1 (Board: Meal plan): only cancel and close give a help request another status than REQUESTED.
  it.todo("a cancelled or closed help request: outcome REFUSED by INV-11, no help -- B1-S04 INV-11 (planned: increment 1)");
});
