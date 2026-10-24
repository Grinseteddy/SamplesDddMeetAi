import fc from "fast-check";
import { describe, expect, it } from "vitest";
import { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import { HelpRequest, HelpRequestId } from "./help-request.js";
import {
  AddresseeRequired,
  ChefOnlyForMenu,
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

// Property runs (ADR-CA-017): 200 runs, seed fixed through FC_SEED in CI.
const propertyRuns = {
  numRuns: 200,
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
      const rejection = rejectionOf(() =>
        HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, addressees: [] }),
      );

      expect(rejection).toBeInstanceOf(AddresseeRequired);
      expect(rejection).toMatchObject({ rule: "INV-20" });
    });
    it("addressed to Chef A, the request is rejected with ChefOnlyForMenu (rule INV-20) -- B2-S01 INV-20 EM-P R3 ex.2", () => {
      const rejection = rejectionOf(() =>
        HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, addressees: ["CHEF"] }),
      );

      expect(rejection).toBeInstanceOf(ChefOnlyForMenu);
      expect(rejection).toMatchObject({ rule: "INV-20" });
    });
    it("for any set of addressees, a preparation-step request is accepted exactly when it names at least one addressee and no CHEF -- B2-S01 INV-20 property", () => {
      fc.assert(
        fc.property(
          fc.uniqueArray(fc.constantFrom("COMMUNITY", "GRANDMA_AVATAR", "CHEF" as const), { maxLength: 3 }),
          (addressees) => {
            const acceptable = addressees.length >= 1 && !addressees.includes("CHEF");
            const rejection = rejectionOf(() =>
              HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, addressees }),
            );

            if (acceptable) {
              expect(rejection).toBeUndefined();
            } else {
              expect(rejection).toMatchObject({ rule: "INV-20" });
            }
          },
        ),
        propertyRuns,
      );
    });
  });

  describe("question, recipe and step", () => {
    it("without a question (empty or only whitespace — read as missing), the request is rejected with QuestionRequired (rule INV-19) -- B2-S01 INV-19 EM-P R8 ex.1", () => {
      for (const question of ["", "   "]) {
        const rejection = rejectionOf(() =>
          HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, question }),
        );

        expect(rejection).toBeInstanceOf(QuestionRequired);
        expect(rejection).toMatchObject({ rule: "INV-19" });
      }
    });
    it("Sunday scones without a step is rejected with StepRequired (rule INV-17) -- B2-S01 INV-17 EM-P R1 ex.2", () => {
      const rejection = rejectionOf(() =>
        HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, howToStepId: null }),
      );

      expect(rejection).toBeInstanceOf(StepRequired);
      expect(rejection).toMatchObject({ rule: "INV-17" });
    });
    it("a step without a recipe is rejected with RecipeRequired (rule INV-17) -- B2-S01 INV-17", () => {
      const rejection = rejectionOf(() =>
        HelpRequest.requestPreparationStepHelp({ ...sundaySconesStep3, recipeId: null }),
      );

      expect(rejection).toBeInstanceOf(RecipeRequired);
      expect(rejection).toMatchObject({ rule: "INV-17" });
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
    it("a rejected request records no event and creates no help request -- B2-S01 INV-17 INV-19 INV-20", () => {
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
