import assert from "node:assert/strict";
import { randomUUID } from "node:crypto";
import { defineParameterType, Given, Then, When } from "@cucumber/cucumber";
import { sundaySconesStep3 } from "../../test-support/fixtures/sunday-scones.js";
import { eventually } from "../support/driver.js";
import { stepOfSundayScones, stepPath, SUNDAY_SCONES } from "../support/sunday-scones-recipe.js";
import type { CookAssistanceWorld } from "../support/world.js";

// Steps of features/preparation-step.feature — board B2, slices B2-S01 and B1-S04, in the domain's language.

const THE_COOK = sundaySconesStep3.requester;

/** Who the cook addresses, as the API names them. */
const ADDRESSEES_OF: Readonly<Record<string, readonly string[]>> = {
  "the community and Grandma Avatar": ["COMMUNITY", "GRANDMA_AVATAR"],
  "Grandma Avatar": ["GRANDMA_AVATAR"],
  "Chef A": ["CHEF"],
  nobody: [],
};

/** The Chef the cook selects (Chef requests name one Chef). */
const CHEF_A = "3c4d5e6f-7a8b-4c9d-8e0f-1a2b3c4d5e6f";

defineParameterType({
  name: "addressees",
  regexp: /the community and Grandma Avatar|Grandma Avatar|Chef A|nobody/,
  transformer: (named) => ADDRESSEES_OF[named],
});

/** The message each published event is announced with (AsyncAPI routing keys). */
const ANNOUNCEMENTS = { "Help requested": "helpRequested", "Help provided": "helpProvided" } as const;

/** What the cook sends to request help with Sunday scones (op requestHelp); a missing part is left out of the body. */
function helpRequestBody({ addressees, step, question }: { addressees: readonly string[]; step?: number; question?: string }) {
  return {
    title: step === undefined ? "Sunday scones" : `Sunday scones, step ${step}`,
    topic: "PREPARATION_STEP",
    recipeId: SUNDAY_SCONES,
    addressees,
    ...(step === undefined ? {} : { howToStepId: stepOfSundayScones(step) }),
    ...(question === undefined ? {} : { description: question }),
    ...(addressees.includes("CHEF") ? { chefId: CHEF_A } : {}),
  };
}

/** The cook's request over HTTP; the answer is kept for the Then steps. */
async function ask(world: CookAssistanceWorld, body: ReturnType<typeof helpRequestBody>): Promise<void> {
  const response = await world.driver.post("/help-requests", THE_COOK, body);
  world.lastResponse = { status: response.status, body: (await response.json()) as Record<string, unknown> };
}

/** Grandma Avatar's answer about a step, as the Grandma Avatar AI publishes it (AsyncAPI receiveGrandmaAvatarHelpProvided). */
function grandmaAnswers(world: CookAssistanceWorld, helpId: string, step: number, explanation: string): void {
  world.driver.answerAsGrandmaAvatar(
    { correlationId: String(world.helpRequestId), messageId: randomUUID(), source: "grandma-avatar" },
    {
      helpId,
      helpRequest: world.helpRequestId,
      helpRequester: THE_COOK,
      answerTitle: `About step ${step}`,
      helpProviderType: "GRANDMA_AVATAR",
      answer: {
        answerType: "PREPARATION_STEP_EXPLANATION",
        recipeId: SUNDAY_SCONES,
        howToStepId: stepOfSundayScones(step),
        description: explanation,
      },
    },
  );
}

/** The ids of the helps the cook sees for the request (op listHelpsForHelpRequest). */
async function recordedHelps(world: CookAssistanceWorld): Promise<string[]> {
  const response = await world.driver.get(`/help-requests/${world.helpRequestId}/helps`, THE_COOK);
  return ((await response.json()) as { items: { helpId: string }[] }).items.map((help) => help.helpId);
}

Given("Recipe Catalog knows {string} with {int} steps", function (this: CookAssistanceWorld, _recipe: string, steps: number) {
  for (let number = 1; number <= steps; number += 1) {
    this.upstream.answerAt(stepPath(number), 200, {
      howToStepId: stepOfSundayScones(number),
      sequenceNumber: number,
      description: `Step ${number}`,
    });
  }
});

When(
  "the cook asks {addressees} about step {int} of {string}: {string}",
  async function (this: CookAssistanceWorld, addressees: readonly string[], step: number, _recipe: string, question: string) {
    await ask(this, helpRequestBody({ addressees, step, question }));
  },
);

Then("the help request is created", function (this: CookAssistanceWorld) {
  assert.equal(this.lastResponse?.status, 201);
  this.helpRequestId = String(this.lastResponse?.body.helpRequestId);
});

Then("{string} is published for it", async function (this: CookAssistanceWorld, event: keyof typeof ANNOUNCEMENTS) {
  const published = this[ANNOUNCEMENTS[event]];
  const ofThisRequest = () => published.messages.filter((message) => message.headers.correlationId === this.helpRequestId);
  assert.ok(await eventually(() => ofThisRequest().length === 1), `${event} was not published for ${this.helpRequestId}`);
  assert.deepEqual(published.invalid, [], `${event}: a message broke the AsyncAPI spec`);
});

When(
  "the cook asks {addressees} about {string} without naming a step",
  async function (this: CookAssistanceWorld, addressees: readonly string[], _recipe: string) {
    await ask(this, helpRequestBody({ addressees, question: "What means 'warm'?" }));
  },
);

Then("the request is rejected", function (this: CookAssistanceWorld) {
  assert.equal(this.lastResponse?.status, 400);
});

Then("the cook has no help request", async function (this: CookAssistanceWorld) {
  const response = await this.driver.get(`/help-requests?requester=${THE_COOK}`, THE_COOK);
  assert.deepEqual(((await response.json()) as { items: unknown[] }).items, []);
});

Then("the request is rejected because the step is not in the recipe", function (this: CookAssistanceWorld) {
  assert.equal(this.lastResponse?.status, 422);
  assert.equal(this.lastResponse?.body.rule, "INV-18");
});

Then("the help request is created, addressed to {addressees}", function (this: CookAssistanceWorld, addressees: readonly string[]) {
  assert.equal(this.lastResponse?.status, 201);
  assert.deepEqual(this.lastResponse?.body.addressees, addressees);
});

Then("the request is refused: Chefs are only allowed for Meal plan", function (this: CookAssistanceWorld) {
  assert.equal(this.lastResponse?.status, 400); // the spec's Chef rule (r5): a Chef only for a Menu request
});

When(
  "the cook asks {addressees} about step {int} of {string} without a question",
  async function (this: CookAssistanceWorld, addressees: readonly string[], step: number, _recipe: string) {
    await ask(this, helpRequestBody({ addressees, step }));
  },
);

Given(
  "the cook asked {addressees} about step {int} of {string}: {string}",
  async function (this: CookAssistanceWorld, addressees: readonly string[], step: number, _recipe: string, question: string) {
    await ask(this, helpRequestBody({ addressees, step, question }));
    assert.equal(this.lastResponse?.status, 201);
    this.helpRequestId = String(this.lastResponse?.body.helpRequestId);
  },
);

Then("the cook has {int} open help requests", async function (this: CookAssistanceWorld, open: number) {
  const response = await this.driver.get(`/help-requests?requester=${THE_COOK}&status=REQUESTED`, THE_COOK);
  assert.equal(((await response.json()) as { items: unknown[] }).items.length, open);
});

When(
  "Grandma Avatar answers about step {int} of {string}: {string}",
  function (this: CookAssistanceWorld, step: number, _recipe: string, explanation: string) {
    this.helpId = randomUUID();
    grandmaAnswers(this, this.helpId, step, explanation);
  },
);

Then("the help is recorded for the request", async function (this: CookAssistanceWorld) {
  const recorded = async () => (await recordedHelps(this)).includes(String(this.helpId));
  assert.ok(await eventually(recorded), `help ${this.helpId} was not recorded for ${this.helpRequestId}`);
});

/**
 * "Nothing recorded" needs an end to the wait without sleeping: a valid answer from Grandma follows as a marker. One
 * consumer with prefetch 1 handles the queue in order (ADR-CA-004), so once the marker is recorded the refused answer
 * has been handled — and only the marker may be there.
 */
Then("the answer is refused and no help is recorded", async function (this: CookAssistanceWorld) {
  const refused = this.helpId;
  const marker = randomUUID();
  grandmaAnswers(this, marker, 3, "Warm is 30 °C.");
  assert.ok(await eventually(async () => (await recordedHelps(this)).includes(marker)), "the marker answer was not recorded");
  assert.deepEqual(await recordedHelps(this), [marker]);
  assert.equal(this.helpProvided.messages.filter((message) => message.payload.helpId === refused).length, 0);
});
