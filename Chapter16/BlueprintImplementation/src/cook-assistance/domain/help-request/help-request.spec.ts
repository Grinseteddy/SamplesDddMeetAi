import fc from "fast-check";
import { describe, expect, it } from "vitest";
import { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import { HelpId } from "../help/help.js";
import { HelpRequest, HelpRequestId, type HelpRequestStatus } from "./help-request.js";
import {
  AddresseeRequired,
  ChefOnlyForMenu,
  ChefReviewOnlyForMenu,
  ExactlyOneChef,
  HelpRequestNotRequested,
  MealDateRequired,
  QuestionRequired,
  RecipeRequired,
  StepRequired,
} from "./help-request-rejections.js";

// Aggregate Help request — increment 0 (walking skeleton), slice B2-S01
// Request help preparation step → Help request → Help requested.
// Rejections are thrown as typed domain rejections carrying their rule ID (ADR-CA-006).

// Sunday scones, step 3 "Let dough rest in a warm place" (Example Mapping Preparation step).
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

// Property runs (ADR-CA-017): 200 runs (1000 in the nightly job through FC_RUNS), seed fixed through FC_SEED in CI.
const propertyRuns = {
  numRuns: Number(process.env["FC_RUNS"] ?? 200),
  ...(process.env["FC_SEED"] ? { seed: Number(process.env["FC_SEED"]) } : {}),
};
const fiveHoursInMs = 5 * 60 * 60 * 1000;

function rejectionOf(command: () => unknown): unknown {
  try {
    command();
  } catch (rejection) {
    return rejection;
  }
  return undefined;
}

describe("Help request — B2-S01 Request help preparation step", () => {
  describe("creation", () => {
    it("requesting help for Sunday scones step 3 with the question \"What means 'warm'?\" creates a help request in status REQUESTED -- B2-S01 EM-P R1 ex.1", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      expect(helpRequest.status).toBe("REQUESTED");
    });
    it("the created help request keeps requester, title, topic PREPARATION_STEP, question, recipe and step as given -- B2-S01", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      expect(helpRequest.id).toBe("0192f1a0-0000-7000-8000-000000000001");
      expect(helpRequest.requester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
      expect(helpRequest.title).toBe("Sunday scones, step 3");
      expect(helpRequest.topic).toBe("PREPARATION_STEP");
      expect(helpRequest.question).toBe("What means 'warm'?");
      expect(helpRequest.recipeId).toBe("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f");
      expect(helpRequest.howToStepId).toBe("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d");
    });
    it("a new help request has version 0 -- B2-S01 ADR-CA-005", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      expect(helpRequest.version).toBe(0);
    });
  });

  describe("answer deadline", () => {
    it("requested at 10:00, the answer deadline is 15:00 (5 h for a preparation step) -- B2-S01 INV-07 EM-P R7 ex.1", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp({
        ...sundaySconesStep3,
        requestedAt: new Date("2026-10-24T10:00:00Z"),
      });

      expect(helpRequest.answerDeadline).toEqual(new Date("2026-10-24T15:00:00Z"));
    });
    it("for any requested-at instant, the answer deadline is exactly 5 h later -- B2-S01 INV-07 property", () => {
      fc.assert(
        fc.property(
          fc.date({ min: new Date("2000-01-01T00:00:00Z"), max: new Date("2100-01-01T00:00:00Z"), noInvalidDate: true }),
          (requestedAt) => {
            const helpRequest = HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, requestedAt });

            expect(helpRequest.answerDeadline.getTime() - requestedAt.getTime()).toBe(fiveHoursInMs);
          },
        ),
        propertyRuns,
      );
    });
  });

  describe("addressees", () => {
    it("addressed to COMMUNITY and GRANDMA_AVATAR, the request is created with both addressees -- B2-S01 INV-20 EM-P R3 ex.1", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp({
        ...sundaySconesStep3,
        addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
      });

      expect(helpRequest.addressees).toEqual(["COMMUNITY", "GRANDMA_AVATAR"]);
    });
    it("addressed to GRANDMA_AVATAR only, the request is created with that one addressee -- B2-S01 INV-20", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp({
        ...sundaySconesStep3,
        addressees: ["GRANDMA_AVATAR"],
      });

      expect(helpRequest.addressees).toEqual(["GRANDMA_AVATAR"]);
    });
    it("addressed to COMMUNITY only, the request is created with that one addressee -- B2-S01 INV-20", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp({
        ...sundaySconesStep3,
        addressees: ["COMMUNITY"],
      });

      expect(helpRequest.addressees).toEqual(["COMMUNITY"]);
    });
    it("addressed to nobody, the request is rejected with AddresseeRequired (rule INV-20) -- B2-S01 INV-20 EM-P R3 ex.3", () => {
      const rejection = rejectionOf(() => HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, addressees: [] }));

      expect(rejection).toBeInstanceOf(AddresseeRequired);
      expect(rejection).toMatchObject({ rule: "INV-20" });
    });
    it("addressed to Chef A, the request is rejected with ChefOnlyForMenu (rule INV-20) -- B2-S01 INV-20 EM-P R3 ex.2", () => {
      const rejection = rejectionOf(() => HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, addressees: ["CHEF"] }));

      expect(rejection).toBeInstanceOf(ChefOnlyForMenu);
      expect(rejection).toMatchObject({ rule: "INV-20" });
    });
    it("for any set of addressees, a preparation-step request is accepted exactly when it names at least one addressee and no CHEF -- B2-S01 INV-20 property", () => {
      fc.assert(
        fc.property(fc.uniqueArray(fc.constantFrom("COMMUNITY", "GRANDMA_AVATAR", "CHEF" as const), { maxLength: 3 }), (addressees) => {
          const acceptable = addressees.length >= 1 && !addressees.includes("CHEF");
          const rejection = rejectionOf(() => HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, addressees }));

          if (acceptable) {
            expect(rejection).toBeUndefined();
          } else {
            expect(rejection).toMatchObject({ rule: "INV-20" });
          }
        }),
        propertyRuns,
      );
    });
  });

  describe("question, recipe and step", () => {
    it("without a question (empty or only whitespace — read as missing), the request is rejected with QuestionRequired (rule INV-19) -- B2-S01 INV-19 EM-P R8 ex.1", () => {
      for (const question of ["", "   "]) {
        const rejection = rejectionOf(() => HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, question }));

        expect(rejection).toBeInstanceOf(QuestionRequired);
        expect(rejection).toMatchObject({ rule: "INV-19" });
      }
    });
    it("Sunday scones without a step is rejected with StepRequired (rule INV-17) -- B2-S01 INV-17 EM-P R1 ex.2", () => {
      const rejection = rejectionOf(() => HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, howToStepId: null }));

      expect(rejection).toBeInstanceOf(StepRequired);
      expect(rejection).toMatchObject({ rule: "INV-17" });
    });
    it("a step without a recipe is rejected with RecipeRequired (rule INV-17) -- B2-S01 INV-17", () => {
      const rejection = rejectionOf(() => HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, recipeId: null }));

      expect(rejection).toBeInstanceOf(RecipeRequired);
      expect(rejection).toMatchObject({ rule: "INV-17" });
    });
    it("for any combination of a present or missing recipe and step, a preparation-step request is accepted exactly when both are present, otherwise rejected with rule INV-17 -- B2-S01 INV-17 property", () => {
      fc.assert(
        fc.property(fc.option(fc.uuid(), { nil: null }), fc.option(fc.uuid(), { nil: null }), (recipe, step) => {
          const rejection = rejectionOf(() =>
            HelpRequest.requestPreparationStepHelp({
              ...sundaySconesStep3,
              recipeId: recipe === null ? null : RecipeId.of(recipe),
              howToStepId: step === null ? null : HowToStepId.of(step),
            }),
          );

          if (recipe !== null && step !== null) {
            expect(rejection).toBeUndefined();
          } else {
            expect(rejection).toMatchObject({ rule: "INV-17" });
          }
        }),
        propertyRuns,
      );
    });
  });

  describe("events", () => {
    it("creation records exactly one HelpRequested event carrying helpRequestId, requester, title, topic PREPARATION_STEP, description, recipeId, howToStepId, addressees, status REQUESTED, requestedAt and answerDeadline -- B2-S01 AsyncAPI HelpRequested", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "HelpRequested",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
          requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          title: "Sunday scones, step 3",
          topic: "PREPARATION_STEP",
          question: "What means 'warm'?",
          recipeId: "c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f",
          howToStepId: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d",
          addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
          chefId: null,
          plannedMealDate: null,
          status: "REQUESTED",
          requestedAt: new Date("2026-10-24T10:00:00Z"),
          answerDeadline: new Date("2026-10-24T15:00:00Z"),
        },
      ]);
    });
    it("pulling the events returns HelpRequested once and leaves no pending events -- B2-S01", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      expect(helpRequest.pullEvents().map((event) => event.type)).toEqual(["HelpRequested"]);
      expect(helpRequest.pullEvents()).toEqual([]);
    });
    it("a rejected request throws: no help request is returned, so there is none to record an event -- B2-S01 INV-17 INV-19 INV-20 (inspection m-5)", () => {
      const rejectedCommands = [
        { ...sundaySconesStep3, howToStepId: null },
        { ...sundaySconesStep3, question: "" },
        { ...sundaySconesStep3, addressees: [] },
      ];

      for (const command of rejectedCommands) {
        let created: HelpRequest | undefined;
        const rejection = rejectionOf(() => {
          created = HelpRequest.requestPreparationStepHelp(command);
        });

        expect(rejection).toBeDefined();
        expect(created).toBeUndefined();
      }
    });
  });

  describe("reconstitution", () => {
    it("reconstituting a stored REQUESTED help request with version 3 restores its state and version and records no event -- B2-S01 ADR-CA-003", () => {
      const stored = {
        id: sundaySconesStep3.id,
        topic: "PREPARATION_STEP",
        chefId: null,
        plannedMealDate: null,
        requester: sundaySconesStep3.requester,
        title: sundaySconesStep3.title,
        question: sundaySconesStep3.question,
        recipeId: sundaySconesStep3.recipeId,
        howToStepId: sundaySconesStep3.howToStepId,
        addressees: ["GRANDMA_AVATAR"],
        status: "REQUESTED",
        answerDeadline: new Date("2026-10-24T15:00:00Z"),
        version: 3,
      } as const;

      const helpRequest = HelpRequest.reconstitute(stored);

      expect(helpRequest.id).toBe(stored.id);
      expect(helpRequest.addressees).toEqual(["GRANDMA_AVATAR"]);
      expect(helpRequest.status).toBe("REQUESTED");
      expect(helpRequest.answerDeadline).toEqual(new Date("2026-10-24T15:00:00Z"));
      expect(helpRequest.version).toBe(3);
      expect(helpRequest.pullEvents()).toEqual([]);
    });
    it("reconstitution runs no guards: stored state without addressees is restored as stored -- B2-S01 ADR-CA-003", () => {
      const storedWithoutAddressees = {
        id: sundaySconesStep3.id,
        topic: "PREPARATION_STEP",
        chefId: null,
        plannedMealDate: null,
        requester: sundaySconesStep3.requester,
        title: sundaySconesStep3.title,
        question: sundaySconesStep3.question,
        recipeId: sundaySconesStep3.recipeId,
        howToStepId: sundaySconesStep3.howToStepId,
        addressees: [],
        status: "REQUESTED",
        answerDeadline: new Date("2026-10-24T15:00:00Z"),
        version: 1,
      } as const;

      const rejection = rejectionOf(() => HelpRequest.reconstitute(storedWithoutAddressees));

      expect(rejection).toBeUndefined();
      expect(HelpRequest.reconstitute(storedWithoutAddressees).addressees).toEqual([]);
    });
  });
});

// Increment 1 — Board: Meal plan. Menu requests (B1-S01) and the lifecycle every topic shares: Chef review (B1-S02),
// cancel by the cook (B1-S06) and by time (B1-S07), close on an accepted help (B1-S08), restart of the answer deadline
// on a rejected help (B1-S10). The requester and premium checks are the application's (ADR-CA-009); Chef availability
// (INV-03: city, calendar) waits for its owning context (Q3-4).
// Menu Example Mapping (US-CA-01): a cook plans a meal with the community and Grandma Avatar, or with one Chef.
const dinnerForSix = {
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000101"),
  requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
  title: "Dinner planning",
  question: "A dinner for six on Friday — what should I cook?",
  addressees: ["COMMUNITY", "GRANDMA_AVATAR"],
  chefId: null,
  plannedMealDate: null,
  requestedAt: new Date("2026-10-21T10:00:00Z"),
} as const;

const chefAnna = CookId.of("1c2d3e4f-5a6b-4c7d-8e9f-0a1b2c3d4e5f");
/** Grandma Avatar's menu for the dinner, the help a Chef is asked to review (EM-M R4). */
const grandmasMenu = HelpId.of("0192f1a0-0000-7000-8000-0000000001a1");
const lunchWithChefAnna = {
  ...dinnerForSix,
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000102"),
  title: "Lunch planning",
  question: "A light lunch for two — what fits a warm day?",
  addressees: ["CHEF"],
  chefId: chefAnna,
  plannedMealDate: "2026-10-30",
} as const;

/** The dinner request as stored in a given status (ADR-CA-003: reconstitution runs no guards). */
function storedMenuRequest(status: HelpRequestStatus): HelpRequest {
  return HelpRequest.reconstitute({
    ...dinnerForSix,
    topic: "MENU",
    recipeId: null,
    howToStepId: null,
    status,
    answerDeadline: new Date("2026-10-23T10:00:00Z"),
    version: 1,
  });
}

describe("Help request — increment 1, Board: Meal plan", () => {
  describe("B1-S01 Request help meal plan", () => {
    it("a Menu request addressed to COMMUNITY and GRANDMA_AVATAR is created in status REQUESTED with topic MENU, no recipe and no step -- B1-S01 INV-01", () => {
      const helpRequest = HelpRequest.requestMealPlanHelp(dinnerForSix);

      expect([helpRequest.status, helpRequest.topic, helpRequest.recipeId, helpRequest.howToStepId]).toEqual([
        "REQUESTED",
        "MENU",
        null,
        null,
      ]);
    });
    it("a cook asks Chef Anna to plan a lunch on 2026-10-30: the request is created addressed to CHEF only, with Anna's id and the meal date -- B1-S01 INV-01 INV-03 EM-M R1 ex.1", () => {
      const helpRequest = HelpRequest.requestMealPlanHelp(lunchWithChefAnna);

      expect([helpRequest.addressees, helpRequest.chefId, helpRequest.plannedMealDate]).toEqual([["CHEF"], chefAnna, "2026-10-30"]);
    });
    it("requested on 21 Oct 10:00, a Menu request's answer deadline is 23 Oct 10:00 (48 h, per topic) -- B1-S01 INV-07 EM-M R6", () => {
      const helpRequest = HelpRequest.requestMealPlanHelp(dinnerForSix);

      expect(helpRequest.answerDeadline).toEqual(new Date("2026-10-23T10:00:00Z"));
    });
    it("for any requested-at instant, a Menu request's answer deadline is exactly 48 h later -- B1-S01 INV-07 property", () => {
      fc.assert(
        fc.property(
          fc.date({ min: new Date("2000-01-01T00:00:00Z"), max: new Date("2100-01-01T00:00:00Z"), noInvalidDate: true }),
          (requestedAt) => {
            const helpRequest = HelpRequest.requestMealPlanHelp({ ...dinnerForSix, requestedAt });

            expect(helpRequest.answerDeadline.getTime() - requestedAt.getTime()).toBe(48 * 60 * 60 * 1000);
          },
        ),
        propertyRuns,
      );
    });
    it("a Menu request without a question is rejected with QuestionRequired (rule INV-19) -- B1-S01 INV-19", () => {
      const rejection = rejectionOf(() => HelpRequest.requestMealPlanHelp({ ...dinnerForSix, question: " " }));

      expect(rejection).toBeInstanceOf(QuestionRequired);
    });
    it("a Menu request addressed to nobody is rejected with AddresseeRequired -- B1-S01 INV-03", () => {
      const rejection = rejectionOf(() => HelpRequest.requestMealPlanHelp({ ...dinnerForSix, addressees: [] }));

      expect(rejection).toBeInstanceOf(AddresseeRequired);
    });
    it("CHEF together with COMMUNITY is rejected with ExactlyOneChef (rule INV-03) -- B1-S01 INV-03", () => {
      const rejection = rejectionOf(() => HelpRequest.requestMealPlanHelp({ ...lunchWithChefAnna, addressees: ["CHEF", "COMMUNITY"] }));

      expect(rejection).toBeInstanceOf(ExactlyOneChef);
    });
    it("CHEF without a Chef id is rejected with ExactlyOneChef (rule INV-03) -- B1-S01 INV-03", () => {
      const rejection = rejectionOf(() => HelpRequest.requestMealPlanHelp({ ...lunchWithChefAnna, chefId: null }));

      expect(rejection).toBeInstanceOf(ExactlyOneChef);
    });
    it("a Chef id without CHEF among the addressees is rejected with ExactlyOneChef (rule INV-03) -- B1-S01 INV-03", () => {
      const rejection = rejectionOf(() => HelpRequest.requestMealPlanHelp({ ...dinnerForSix, chefId: chefAnna }));

      expect(rejection).toBeInstanceOf(ExactlyOneChef);
    });
    it("CHEF with a Chef id but no meal date is rejected with MealDateRequired (rule INV-03) -- B1-S01 INV-03", () => {
      const rejection = rejectionOf(() => HelpRequest.requestMealPlanHelp({ ...lunchWithChefAnna, plannedMealDate: null }));

      expect(rejection).toBeInstanceOf(MealDateRequired);
    });
    it("for any addressees, Chef id and meal date, a Menu request is accepted exactly when it names community and/or Grandma Avatar without a Chef, or CHEF alone with a Chef id and a meal date -- B1-S01 INV-03 property", () => {
      const addressees = fc
        .subarray(["COMMUNITY", "GRANDMA_AVATAR", "CHEF"] as const)
        .chain((chosen) => fc.shuffledSubarray(chosen, { minLength: chosen.length }));
      fc.assert(
        fc.property(
          addressees,
          fc.option(fc.constant(chefAnna)),
          fc.option(fc.constant("2026-10-30")),
          (asked, chefId, plannedMealDate) => {
            const withoutChef = asked.length > 0 && !asked.includes("CHEF") && chefId === null;
            const chefAlone = asked.length === 1 && asked[0] === "CHEF" && chefId !== null && plannedMealDate !== null;

            const rejection = rejectionOf(() =>
              HelpRequest.requestMealPlanHelp({ ...dinnerForSix, addressees: asked, chefId, plannedMealDate }),
            );

            expect(rejection === undefined).toBe(withoutChef || chefAlone);
          },
        ),
        propertyRuns,
      );
    });
    it("creating a Menu request records one HelpRequested with topic MENU, recipe and step null, the addressees, the Chef id and the meal date -- B1-S01", () => {
      const helpRequest = HelpRequest.requestMealPlanHelp(lunchWithChefAnna);

      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "HelpRequested",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000102",
          requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          title: "Lunch planning",
          topic: "MENU",
          question: "A light lunch for two — what fits a warm day?",
          recipeId: null,
          howToStepId: null,
          addressees: ["CHEF"],
          chefId: "1c2d3e4f-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
          plannedMealDate: "2026-10-30",
          status: "REQUESTED",
          requestedAt: new Date("2026-10-21T10:00:00Z"),
          answerDeadline: new Date("2026-10-23T10:00:00Z"),
        },
      ]);
    });
  });

  describe("B1-S02 Request Chef review", () => {
    it("on a REQUESTED Menu request, asking Chef Anna to review Grandma Avatar's help records ChefReviewRequested with help request, requester, help, Chef and time; the request stays REQUESTED -- B1-S02 INV-04 EM-M R4 ex.1", () => {
      const helpRequest = HelpRequest.requestMealPlanHelp(dinnerForSix);
      helpRequest.pullEvents();

      helpRequest.requestChefReview({ helpId: grandmasMenu, chefId: chefAnna, requestedAt: new Date("2026-10-22T09:00:00Z") });

      expect(helpRequest.status).toBe("REQUESTED");
      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "ChefReviewRequested",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          helpId: grandmasMenu,
          chefId: chefAnna,
          requestedAt: new Date("2026-10-22T09:00:00Z"),
        },
      ]);
    });
    it("a Chef review on a preparation-step request is rejected with ChefReviewOnlyForMenu (rule INV-01) -- B1-S02 INV-01", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      const rejection = rejectionOf(() =>
        helpRequest.requestChefReview({ helpId: grandmasMenu, chefId: chefAnna, requestedAt: new Date("2026-10-24T11:00:00Z") }),
      );

      expect(rejection).toBeInstanceOf(ChefReviewOnlyForMenu);
    });
    it("a Chef review on a CANCELLED request is rejected with HelpRequestNotRequested (rule INV-05) -- B1-S02 INV-05", () => {
      const cancelled = storedMenuRequest("CANCELLED");

      const rejection = rejectionOf(() =>
        cancelled.requestChefReview({ helpId: grandmasMenu, chefId: chefAnna, requestedAt: new Date("2026-10-22T09:00:00Z") }),
      );

      expect(rejection).toBeInstanceOf(HelpRequestNotRequested);
    });
  });

  describe("B1-S06 Cancel help request (cook)", () => {
    it("the cook cancels a REQUESTED request: status CANCELLED and one HelpRequestCancelled with cancelledBy REQUESTER and the time -- B1-S06 INV-06 EM-M R5 ex.1", () => {
      const helpRequest = storedMenuRequest("REQUESTED");

      helpRequest.cancel(new Date("2026-10-22T08:00:00Z"));

      expect(helpRequest.status).toBe("CANCELLED");
      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "HelpRequestCancelled",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          cancelledBy: "REQUESTER",
          status: "CANCELLED",
          cancelledAt: new Date("2026-10-22T08:00:00Z"),
        },
      ]);
    });
    it("cancelling an already cancelled request is rejected with HelpRequestNotRequested (rule INV-05) -- B1-S06 INV-05 EM-M R5 ex.2", () => {
      const rejection = rejectionOf(() => storedMenuRequest("CANCELLED").cancel(new Date("2026-10-22T08:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpRequestNotRequested);
    });
    it("cancelling a CLOSED request is rejected with HelpRequestNotRequested (rule INV-05) -- B1-S06 INV-05", () => {
      const rejection = rejectionOf(() => storedMenuRequest("CLOSED").cancel(new Date("2026-10-22T08:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpRequestNotRequested);
    });
  });

  describe("B1-S07 Cancel help request (time)", () => {
    it("requested 21 Oct 10:00, still unanswered at 23 Oct 09:59: the deadline check leaves it REQUESTED, no event -- B1-S07 INV-07 EM-M R6 ex.1", () => {
      const helpRequest = storedMenuRequest("REQUESTED");

      helpRequest.cancelIfDeadlinePassed(new Date("2026-10-23T09:59:00Z"));

      expect([helpRequest.status, helpRequest.pullEvents()]).toEqual(["REQUESTED", []]);
    });
    it("requested 21 Oct 10:00, at 23 Oct 10:00: CANCELLED, one HelpRequestCancelled with cancelledBy SYSTEM -- B1-S07 INV-07 EM-M R6 ex.2", () => {
      const helpRequest = storedMenuRequest("REQUESTED");

      helpRequest.cancelIfDeadlinePassed(new Date("2026-10-23T10:00:00Z"));

      expect(helpRequest.status).toBe("CANCELLED");
      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "HelpRequestCancelled",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          cancelledBy: "SYSTEM",
          status: "CANCELLED",
          cancelledAt: new Date("2026-10-23T10:00:00Z"),
        },
      ]);
    });
    it("requested 22 Oct 15:30, at 23 Oct 10:00: still REQUESTED (the deadline depends on the request time) -- B1-S07 INV-07 EM-M R6 ex.3", () => {
      const helpRequest = HelpRequest.requestMealPlanHelp({ ...dinnerForSix, requestedAt: new Date("2026-10-22T15:30:00Z") });
      helpRequest.pullEvents();

      helpRequest.cancelIfDeadlinePassed(new Date("2026-10-23T10:00:00Z"));

      expect([helpRequest.status, helpRequest.pullEvents()]).toEqual(["REQUESTED", []]);
    });
    it("a CLOSED request past its deadline: the check changes nothing and records no event -- B1-S07 INV-05 INV-07", () => {
      const closed = storedMenuRequest("CLOSED");

      closed.cancelIfDeadlinePassed(new Date("2026-10-24T10:00:00Z"));

      expect([closed.status, closed.pullEvents()]).toEqual(["CLOSED", []]);
    });
    it("for any request time and check time, a REQUESTED request is cancelled by the check exactly when the check is at or after its deadline -- B1-S07 INV-07 property", () => {
      const instants = fc.date({ min: new Date("2000-01-01T00:00:00Z"), max: new Date("2100-01-01T00:00:00Z"), noInvalidDate: true });
      const aroundTheDeadline = fc.oneof(fc.constant(0), fc.integer({ min: -60 * 60 * 1000, max: 60 * 60 * 1000 }));
      fc.assert(
        fc.property(instants, aroundTheDeadline, (requestedAt, offsetMs) => {
          const helpRequest = HelpRequest.requestMealPlanHelp({ ...dinnerForSix, requestedAt });
          const checkedAt = new Date(helpRequest.answerDeadline.getTime() + offsetMs);

          helpRequest.cancelIfDeadlinePassed(checkedAt);

          expect(helpRequest.status).toBe(offsetMs >= 0 ? "CANCELLED" : "REQUESTED");
        }),
        propertyRuns,
      );
    });
  });

  describe("B1-S08 Close help request", () => {
    it("closing a REQUESTED request on an accepted help: status CLOSED and one HelpRequestClosed with the accepted help and the time -- B1-S08 INV-14", () => {
      const helpRequest = storedMenuRequest("REQUESTED");

      helpRequest.close(grandmasMenu, new Date("2026-10-22T12:00:00Z"));

      expect(helpRequest.status).toBe("CLOSED");
      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "HelpRequestClosed",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          requester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          acceptedHelpId: grandmasMenu,
          status: "CLOSED",
          closedAt: new Date("2026-10-22T12:00:00Z"),
        },
      ]);
    });
    it("closing a CANCELLED request is rejected with HelpRequestNotRequested (rule INV-05) -- B1-S08 INV-05", () => {
      const rejection = rejectionOf(() => storedMenuRequest("CANCELLED").close(grandmasMenu, new Date("2026-10-22T12:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpRequestNotRequested);
    });
  });

  describe("B1-S10 Restart answer deadline", () => {
    it("a Menu request of 21 Oct 10:00 whose help is rejected on 22 Oct 09:00 gets the deadline 24 Oct 09:00 and records AnswerDeadlineRestarted -- B1-S10 INV-08", () => {
      const helpRequest = storedMenuRequest("REQUESTED");

      helpRequest.restartAnswerDeadline(new Date("2026-10-22T09:00:00Z"));

      expect(helpRequest.answerDeadline).toEqual(new Date("2026-10-24T09:00:00Z"));
      expect(helpRequest.pullEvents()).toEqual([
        {
          type: "AnswerDeadlineRestarted",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          answerDeadline: new Date("2026-10-24T09:00:00Z"),
          restartedAt: new Date("2026-10-22T09:00:00Z"),
        },
      ]);
    });
    it("a preparation-step request of 10:00 whose help is rejected at 13:00 gets the deadline 18:00 -- B1-S10 INV-08 EM-P R7 ex.3", () => {
      const helpRequest = HelpRequest.requestPreparationStepHelp(sundaySconesStep3);

      helpRequest.restartAnswerDeadline(new Date("2026-10-24T13:00:00Z"));

      expect(helpRequest.answerDeadline).toEqual(new Date("2026-10-24T18:00:00Z"));
    });
    it("restarting the deadline of a CANCELLED request is rejected with HelpRequestNotRequested (rule INV-05) -- B1-S10 INV-05", () => {
      const rejection = rejectionOf(() => storedMenuRequest("CANCELLED").restartAnswerDeadline(new Date("2026-10-22T09:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpRequestNotRequested);
    });
    it("for any topic and rejection instant, the restarted deadline is the rejection instant plus the topic's answer time -- B1-S10 INV-08 property", () => {
      // F6, Q3-2: the answer time per topic
      const hours = 60 * 60 * 1000;
      const requestOf = {
        PREPARATION_STEP: () => ({ helpRequest: HelpRequest.requestPreparationStepHelp(sundaySconesStep3), answerTimeMs: 5 * hours }),
        MENU: () => ({ helpRequest: HelpRequest.requestMealPlanHelp(dinnerForSix), answerTimeMs: 48 * hours }),
      };
      fc.assert(
        fc.property(
          fc.constantFrom("PREPARATION_STEP", "MENU" as const),
          fc.date({ min: new Date("2026-10-24T10:00:00Z"), max: new Date("2100-01-01T00:00:00Z"), noInvalidDate: true }),
          (topic, rejectedAt) => {
            const { helpRequest, answerTimeMs } = requestOf[topic]();

            helpRequest.restartAnswerDeadline(rejectedAt);

            expect(helpRequest.answerDeadline.getTime() - rejectedAt.getTime()).toBe(answerTimeMs);
          },
        ),
        propertyRuns,
      );
    });
  });

  describe("reconstitution", () => {
    it("reconstituting a stored CANCELLED Menu request with a Chef and a meal date restores them as stored -- B1-S01 ADR-CA-003", () => {
      const stored = HelpRequest.reconstitute({
        ...lunchWithChefAnna,
        topic: "MENU",
        recipeId: null,
        howToStepId: null,
        status: "CANCELLED",
        answerDeadline: new Date("2026-10-23T10:00:00Z"),
        version: 2,
      });

      expect([stored.topic, stored.status, stored.chefId, stored.plannedMealDate, stored.version, stored.pullEvents()]).toEqual([
        "MENU",
        "CANCELLED",
        chefAnna,
        "2026-10-30",
        2,
        [],
      ]);
    });
  });
});
