import fc from "fast-check";
import { describe, expect, it } from "vitest";
import { CookId, HowToStepId, RecipeId } from "../shared/ids.js";
import { HelpRequestId } from "../help-request/help-request.js";
import { Help, HelpId, type HelpStatus } from "./help.js";
import { HelpNotProvided, OwnRequestNotAnswerable, RecipeNotAsInRequest, StepNotAsInRequest } from "./help-rejections.js";

// Aggregate Help — increment 0 (walking skeleton), slice B1-S04 (Preparation step variant)
// Help provided message from Grandma Avatar AI → Provide Help → Help → Help provided.
// The application service passes the immutable facts of the answered help request (N-06).
// Rejections are thrown as typed domain rejections carrying their rule ID (ADR-CA-006).

// The help request it answers: Sunday scones, step 3 (Example Mapping Preparation step).
const sundaySconesStep3Request = {
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000001"),
  requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
  topic: "PREPARATION_STEP",
  recipeId: RecipeId.of("c4d5e6f7-8a9b-4c0d-9e1f-2a3b4c5d6e7f"),
  howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"),
} as const;

// Grandma Avatar's answer: what "warm" means in step 3.
const grandmaExplainsWarm = {
  id: HelpId.of("0192f1a0-0000-7000-8000-0000000000a1"),
  helpRequest: sundaySconesStep3Request,
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

// Property runs (ADR-CA-017): 200 runs (1000 in the nightly job through FC_RUNS), seed fixed through FC_SEED in CI.
const propertyRuns = {
  numRuns: Number(process.env["FC_RUNS"] ?? 200),
  ...(process.env["FC_SEED"] ? { seed: Number(process.env["FC_SEED"]) } : {}),
};

function rejectionOf(command: () => unknown): unknown {
  try {
    command();
  } catch (rejection) {
    return rejection;
  }
  return undefined;
}

describe("Help — B1-S04 Provide Help (Preparation step)", () => {
  describe("creation", () => {
    it("Grandma Avatar answering Sunday scones step 3 creates a help in status PROVIDED -- B1-S04 EM-P R5 ex.1", () => {
      const help = Help.provide(grandmaExplainsWarm);

      expect(help.status).toBe("PROVIDED");
    });
    it("the provided help keeps its id, the help request id, the requester, the answer title, provider type GRANDMA_AVATAR without a provider id, and the answer as given -- B1-S04", () => {
      const help = Help.provide(grandmaExplainsWarm);

      expect(help.id).toBe("0192f1a0-0000-7000-8000-0000000000a1");
      expect(help.helpRequestId).toBe("0192f1a0-0000-7000-8000-000000000001");
      expect(help.helpRequester).toBe("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d");
      expect(help.answerTitle).toBe("Warm means about 30 °C");
      expect(help.provider).toEqual({ type: "GRANDMA_AVATAR", id: null });
      expect(help.answer).toEqual(grandmaExplainsWarm.answer);
    });
    it("a new help has version 0 -- B1-S04 ADR-CA-005", () => {
      const help = Help.provide(grandmaExplainsWarm);

      expect(help.version).toBe(0);
    });
  });

  describe("same recipe and step as the request", () => {
    it("an answer about step 4 of Sunday scones for a request about step 3 is rejected with StepNotAsInRequest (rule INV-10) -- B1-S04 INV-10 EM-P R5 ex.2", () => {
      const rejection = rejectionOf(() =>
        Help.provide({
          ...grandmaExplainsWarm,
          answer: { ...grandmaExplainsWarm.answer, howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c04") },
        }),
      );

      expect(rejection).toBeInstanceOf(StepNotAsInRequest);
      expect(rejection).toMatchObject({ rule: "INV-10" });
    });
    it("an answer for another recipe is rejected with RecipeNotAsInRequest (rule INV-10) -- B1-S04 INV-10", () => {
      const rejection = rejectionOf(() =>
        Help.provide({
          ...grandmaExplainsWarm,
          answer: { ...grandmaExplainsWarm.answer, recipeId: RecipeId.of("d5e6f7a8-9b0c-4d1e-8f2a-3b4c5d6e7f80") },
        }),
      );

      expect(rejection).toBeInstanceOf(RecipeNotAsInRequest);
      expect(rejection).toMatchObject({ rule: "INV-10" });
    });
  });

  describe("one help request", () => {
    it("for any help request a help is provided for, the help refers to exactly that one request -- B1-S04 INV-16 property", () => {
      fc.assert(
        fc.property(fc.uuid(), fc.uuid(), fc.uuid(), fc.uuid(), (requestId, requester, recipe, step) => {
          const helpRequest = {
            ...sundaySconesStep3Request,
            id: HelpRequestId.of(requestId),
            requester: CookId.of(requester),
            recipeId: RecipeId.of(recipe),
            howToStepId: HowToStepId.of(step),
          };
          const answer = { ...grandmaExplainsWarm.answer, recipeId: helpRequest.recipeId, howToStepId: helpRequest.howToStepId };

          const help = Help.provide({ ...grandmaExplainsWarm, helpRequest, answer });

          expect(help.helpRequestId).toBe(requestId);
          expect(help.helpRequester).toBe(requester);
        }),
        propertyRuns,
      );
    });
  });

  describe("events", () => {
    it("providing records exactly one HelpProvided event carrying helpId, helpRequestId, helpRequester, answerTitle, helpProviderType GRANDMA_AVATAR, helpProvider null, the answer, status PROVIDED and providedAt -- B1-S04 AsyncAPI HelpProvided", () => {
      const help = Help.provide(grandmaExplainsWarm);

      expect(help.pullEvents()).toEqual([
        {
          type: "HelpProvided",
          helpId: "0192f1a0-0000-7000-8000-0000000000a1",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000001",
          helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          answerTitle: "Warm means about 30 °C",
          helpProviderType: "GRANDMA_AVATAR",
          helpProvider: null,
          answer: grandmaExplainsWarm.answer,
          status: "PROVIDED",
          providedAt: new Date("2026-10-24T12:00:00Z"),
        },
      ]);
    });
    it("pulling the events returns HelpProvided once and leaves no pending events -- B1-S04", () => {
      const help = Help.provide(grandmaExplainsWarm);

      expect(help.pullEvents().map((event) => event.type)).toEqual(["HelpProvided"]);
      expect(help.pullEvents()).toEqual([]);
    });
    it("a rejected help records no event and creates no help -- B1-S04 INV-10", () => {
      const rejectedCommands = [
        {
          ...grandmaExplainsWarm,
          answer: { ...grandmaExplainsWarm.answer, howToStepId: HowToStepId.of("7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c04") },
        },
        {
          ...grandmaExplainsWarm,
          answer: { ...grandmaExplainsWarm.answer, recipeId: RecipeId.of("d5e6f7a8-9b0c-4d1e-8f2a-3b4c5d6e7f80") },
        },
      ];

      for (const command of rejectedCommands) {
        let created: Help | undefined;
        const rejection = rejectionOf(() => {
          created = Help.provide(command);
        });

        expect(rejection).toBeDefined();
        expect(created).toBeUndefined();
      }
    });
  });

  describe("reconstitution", () => {
    it("reconstituting a stored PROVIDED help with version 2 restores its state and version and records no event -- B1-S04 ADR-CA-003", () => {
      const stored = {
        id: grandmaExplainsWarm.id,
        helpRequestId: sundaySconesStep3Request.id,
        helpRequester: sundaySconesStep3Request.requester,
        answerTitle: grandmaExplainsWarm.answerTitle,
        provider: grandmaExplainsWarm.provider,
        answer: grandmaExplainsWarm.answer,
        status: "PROVIDED",
        version: 2,
      } as const;

      const help = Help.reconstitute(stored);

      expect(help.id).toBe(stored.id);
      expect(help.helpRequestId).toBe(stored.helpRequestId);
      expect(help.answer).toEqual(grandmaExplainsWarm.answer);
      expect(help.status).toBe("PROVIDED");
      expect(help.version).toBe(2);
      expect(help.pullEvents()).toEqual([]);
    });
    // No "reconstitution runs no guards" test here: the only guards (INV-10) compare the answer with the
    // request's facts, which a stored help does not hold — such a test could not fail.
  });
});

// Increment 1 — Board: Meal plan. Helps for Menu requests (B1-S03 by a community cook or the selected Chef through the
// API, B1-S04 Menu variant by Grandma Avatar) and the cook's decision on any help: accept (B1-S05), reject (B1-S09).
// Addressee, selected Chef (INV-09), requester (INV-13, INV-15) and an open request (INV-11, Q3-6) are the
// application's checks (ADR-CA-009); a topic mismatch between answer and request is planned for increment 3.
// Menu Example Mapping (US-CA-01): the dinner for six, answered with a menu proposal.
const dinnerForSixRequest = {
  id: HelpRequestId.of("0192f1a0-0000-7000-8000-000000000101"),
  requester: CookId.of("8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d"),
  topic: "MENU",
  recipeId: null,
  howToStepId: null,
} as const;
const communityCook = CookId.of("2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f6a");
const chefAnna = CookId.of("1c2d3e4f-5a6b-4c7d-8e9f-0a1b2c3d4e5f");
const twoCourseDinner = {
  topic: "MENU",
  note: "A light start, then something warm.",
  servings: 6,
  meal: "DINNER",
  howToServe: null,
  courses: [
    { dish: 1, recipeId: RecipeId.of("3e4f5a6b-7c8d-4e9f-8a0b-1c2d3e4f5a6b") },
    { dish: 2, recipeId: RecipeId.of("4f5a6b7c-8d9e-4f0a-9b1c-2d3e4f5a6b7c") },
  ],
} as const;
const communityMenu = {
  id: HelpId.of("0192f1a0-0000-7000-8000-0000000001b1"),
  helpRequest: dinnerForSixRequest,
  answerTitle: "Soup, then lasagna",
  provider: { type: "COMMUNITY", id: communityCook },
  answer: twoCourseDinner,
  providedAt: new Date("2026-10-21T18:00:00Z"),
} as const;

/** The community cook's menu as stored in a given status (ADR-CA-003: reconstitution runs no guards). */
function storedCommunityMenu(status: HelpStatus): Help {
  return Help.reconstitute({
    id: communityMenu.id,
    helpRequestId: dinnerForSixRequest.id,
    helpRequester: dinnerForSixRequest.requester,
    answerTitle: communityMenu.answerTitle,
    provider: communityMenu.provider,
    answer: twoCourseDinner,
    status,
    version: 1,
  });
}

describe("Help — increment 1, Board: Meal plan", () => {
  describe("B1-S03 Provide Help (actor) and B1-S04 Menu variant", () => {
    it("a community cook answers the dinner request with a two-course menu proposal: the help is PROVIDED by COMMUNITY with the cook's id and keeps the proposal -- B1-S03", () => {
      const help = Help.provide(communityMenu);

      expect([help.status, help.provider, help.answer]).toEqual(["PROVIDED", { type: "COMMUNITY", id: communityCook }, twoCourseDinner]);
    });
    it("Chef Anna answers the lunch request: the help is PROVIDED by CHEF with Anna's id -- B1-S03 INV-09", () => {
      const help = Help.provide({ ...communityMenu, provider: { type: "CHEF", id: chefAnna } });

      expect([help.status, help.provider]).toEqual(["PROVIDED", { type: "CHEF", id: chefAnna }]);
    });
    it("Grandma Avatar answers the dinner request with a menu proposal: provider GRANDMA_AVATAR without an id -- B1-S04 (Menu variant)", () => {
      const help = Help.provide({ ...communityMenu, provider: { type: "GRANDMA_AVATAR", id: null } });

      expect([help.provider, help.answer]).toEqual([{ type: "GRANDMA_AVATAR", id: null }, twoCourseDinner]);
    });
    it("the requester answering their own request is rejected with OwnRequestNotAnswerable (rule INV-25) -- B1-S03 INV-25", () => {
      const rejection = rejectionOf(() =>
        Help.provide({ ...communityMenu, provider: { type: "COMMUNITY", id: dinnerForSixRequest.requester } }),
      );

      expect(rejection).toBeInstanceOf(OwnRequestNotAnswerable);
    });
    it("providing a menu proposal records one HelpProvided with the provider, the proposal and status PROVIDED -- B1-S03 AsyncAPI HelpProvided", () => {
      const help = Help.provide(communityMenu);

      expect(help.pullEvents()).toEqual([
        {
          type: "HelpProvided",
          helpId: "0192f1a0-0000-7000-8000-0000000001b1",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          answerTitle: "Soup, then lasagna",
          helpProviderType: "COMMUNITY",
          helpProvider: communityCook,
          answer: twoCourseDinner,
          status: "PROVIDED",
          providedAt: new Date("2026-10-21T18:00:00Z"),
        },
      ]);
    });
  });

  describe("B1-S05 Accept help", () => {
    it("accepting a PROVIDED help: status ACCEPTED and one HelpAccepted with the help, its request, requester, provider type and id, status and time -- B1-S05 INV-13 EM-P R9 ex.1", () => {
      const help = Help.provide(communityMenu);
      help.pullEvents();

      help.accept(new Date("2026-10-21T19:00:00Z"));

      expect(help.status).toBe("ACCEPTED");
      expect(help.pullEvents()).toEqual([
        {
          type: "HelpAccepted",
          helpId: "0192f1a0-0000-7000-8000-0000000001b1",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          helpProviderType: "COMMUNITY",
          helpProvider: communityCook,
          status: "ACCEPTED",
          decidedAt: new Date("2026-10-21T19:00:00Z"),
        },
      ]);
    });
    it("accepting an ACCEPTED help is rejected with HelpNotProvided (rule INV-13) -- B1-S05 INV-13", () => {
      const rejection = rejectionOf(() => storedCommunityMenu("ACCEPTED").accept(new Date("2026-10-21T20:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpNotProvided);
    });
    it("accepting a REJECTED help is rejected with HelpNotProvided (rule INV-15: rejected cannot be accepted later) -- B1-S05 INV-15 EM-P R10 ex.1", () => {
      const rejection = rejectionOf(() => storedCommunityMenu("REJECTED").accept(new Date("2026-10-21T20:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpNotProvided);
    });
  });

  describe("B1-S09 Reject help", () => {
    it("rejecting a PROVIDED help: status REJECTED and one HelpRejected with the same fields as an acceptance -- B1-S09 INV-15 EM-P R10 ex.1", () => {
      const help = storedCommunityMenu("PROVIDED");

      help.reject(new Date("2026-10-21T19:00:00Z"));

      expect(help.status).toBe("REJECTED");
      expect(help.pullEvents()).toEqual([
        {
          type: "HelpRejected",
          helpId: "0192f1a0-0000-7000-8000-0000000001b1",
          helpRequestId: "0192f1a0-0000-7000-8000-000000000101",
          helpRequester: "8a2b4c6d-1e3f-4a5b-8c7d-9e0f1a2b3c4d",
          helpProviderType: "COMMUNITY",
          helpProvider: communityCook,
          status: "REJECTED",
          decidedAt: new Date("2026-10-21T19:00:00Z"),
        },
      ]);
    });
    it("rejecting an ACCEPTED help is rejected with HelpNotProvided (rule INV-13) -- B1-S09 INV-13", () => {
      const rejection = rejectionOf(() => storedCommunityMenu("ACCEPTED").reject(new Date("2026-10-21T20:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpNotProvided);
    });
    it("rejecting a REJECTED help is rejected with HelpNotProvided (rule INV-15) -- B1-S09 INV-15", () => {
      const rejection = rejectionOf(() => storedCommunityMenu("REJECTED").reject(new Date("2026-10-21T20:00:00Z")));

      expect(rejection).toBeInstanceOf(HelpNotProvided);
    });
    it("for any sequence of accept and reject on a PROVIDED help, only the first decision is taken and every later one is refused with HelpNotProvided -- B1-S05 B1-S09 INV-13 INV-15 property", () => {
      const decisions = fc.array(fc.constantFrom("accept", "reject" as const), { minLength: 1, maxLength: 6 });
      fc.assert(
        fc.property(decisions, (sequence) => {
          const help = storedCommunityMenu("PROVIDED");

          const outcomes = sequence.map((decision, at) => rejectionOf(() => help[decision](new Date(Date.UTC(2026, 9, 21, 19, at)))));

          expect(outcomes[0]).toBeUndefined();
          expect(outcomes.slice(1).every((outcome) => outcome instanceof HelpNotProvided)).toBe(true);
          expect(help.status).toBe(sequence[0] === "accept" ? "ACCEPTED" : "REJECTED");
        }),
        propertyRuns,
      );
    });
  });

  describe("reconstitution", () => {
    it("reconstituting a stored ACCEPTED help with a menu proposal by Chef Anna restores it as stored -- B1-S05 ADR-CA-003", () => {
      const stored = Help.reconstitute({
        id: communityMenu.id,
        helpRequestId: dinnerForSixRequest.id,
        helpRequester: dinnerForSixRequest.requester,
        answerTitle: "Anna's dinner",
        provider: { type: "CHEF", id: chefAnna },
        answer: twoCourseDinner,
        status: "ACCEPTED",
        version: 2,
      });

      expect([stored.status, stored.provider, stored.answer, stored.version, stored.pullEvents()]).toEqual([
        "ACCEPTED",
        { type: "CHEF", id: chefAnna },
        twoCourseDinner,
        2,
        [],
      ]);
    });
  });
});
