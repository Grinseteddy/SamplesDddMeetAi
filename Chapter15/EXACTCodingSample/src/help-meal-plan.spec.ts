import { describe, expect, it } from "vitest";
import { requestHelp, requestReview, selectableChefs } from "./help-meal-plan.js";
import type { HelpRequest } from "./help-request.js";
import { applyAnswerTimeout, cancelRequest } from "./help-request-lifecycle.js";

// Spec: src/help-meal-plan-example-mapping.md (US-CA-01 Help Meal plan)
//
// Chosen error contract (the spec only says "rejected"): every operation returns
// a result. A rejection is an error result carrying a named reason; it never
// throws and never creates or changes a request.

const premiumCookInHamburg = { name: "Maria", city: "Hamburg", premium: true };
const chefAnna = { name: "Anna", city: "Hamburg", freeOn: ["2026-10-28"] };
const chefBen = { name: "Ben", city: "Munich", freeOn: ["2026-10-28"] };
const chefClara = { name: "Clara", city: "Hamburg", freeOn: ["2026-11-02"] };
const nonPremiumCookInHamburg = { ...premiumCookInHamburg, premium: false };
const grandmaAnswerForMaria = { answeredFor: "Maria", mealPlan: "Dinner plan for Oct 30" };

const dinnerPlanningWithAnna = {
  cook: premiumCookInHamburg,
  chef: chefAnna,
  topic: "meal planning",
  meal: { kind: "dinner", date: "2026-10-30" },
  requestedAt: "2026-10-21T10:00",
};

function openRequestToAnna(requestedAt = dinnerPlanningWithAnna.requestedAt): HelpRequest {
  const result = requestHelp({ ...dinnerPlanningWithAnna, requestedAt });
  if (result.outcome !== "created") {
    throw new Error(`fixture expected a created request, got ${result.outcome}`);
  }
  return result.request;
}

describe("US-CA-01 Help Meal plan", () => {
  describe("Rule 1: a Chef can only be asked for meal planning", () => {
    it("premium Cook in Hamburg asks Chef Anna for help planning a lunch on Oct 30 -- request is created and open", () => {
      const result = requestHelp({ ...dinnerPlanningWithAnna, meal: { ...dinnerPlanningWithAnna.meal, kind: "lunch" } });

      expect(result).toMatchObject({ outcome: "created", request: { chef: "Anna", status: "open" } });
    });
    it("premium Cook asks Chef Anna for help planning a dinner on Oct 30 -- request is created and open", () => {
      const result = requestHelp(dinnerPlanningWithAnna);

      expect(result).toMatchObject({ outcome: "created", request: { chef: "Anna", status: "open" } });
    });
    it("premium Cook asks Chef Anna for help with substitutions -- rejected with reason 'not meal planning'", () => {
      const result = requestHelp({ ...dinnerPlanningWithAnna, topic: "substitutions" });

      expect(result).toEqual({ outcome: "rejected", reason: "not meal planning" });
    });
  });

  describe("Rule 2: only a premium Cook can ask a Chef", () => {
    it("non-premium Cook asks Chef Anna for meal planning -- rejected with reason 'upgrade required', no request is created", () => {
      const result = requestHelp({ ...dinnerPlanningWithAnna, cook: nonPremiumCookInHamburg });

      expect(result).toEqual({ outcome: "rejected", reason: "upgrade required" });
    });
  });

  describe("Rule 3: a Cook selects exactly one available Chef by name", () => {
    it("Cook in Hamburg, dinner on Oct 30, Chefs Anna (Hamburg, free Oct 28), Ben (Munich), Clara (Hamburg, free only Nov 2) -- selectable Chefs are [Anna]", () => {
      const selectable = selectableChefs({
        cook: premiumCookInHamburg,
        mealDate: "2026-10-30",
        chefs: [chefAnna, chefBen, chefClara],
      });

      expect(selectable.map((chef) => chef.name)).toEqual(["Anna"]);
    });
    it("Cook in Hamburg selects Chef Ben (Munich) -- rejected with reason 'Chef not local'", () => {
      const result = requestHelp({ ...dinnerPlanningWithAnna, chef: chefBen });

      expect(result).toEqual({ outcome: "rejected", reason: "Chef not local" });
    });
    it("Cook in Hamburg, dinner on Oct 30, selects Chef Clara (Hamburg, free only Nov 2) -- rejected with reason 'Chef not timely available'", () => {
      const result = requestHelp({ ...dinnerPlanningWithAnna, chef: chefClara });

      expect(result).toEqual({ outcome: "rejected", reason: "Chef not timely available" });
    });
    it("Cook in Hamburg, dinner on Oct 30, selects a Hamburg Chef free only on Oct 30 -- rejected with reason 'Chef not timely available' (reading: 'before' means strictly before the meal date)", () => {
      const chefFreeOnlyOnMealDate = { name: "Dora", city: "Hamburg", freeOn: ["2026-10-30"] };

      const result = requestHelp({ ...dinnerPlanningWithAnna, chef: chefFreeOnlyOnMealDate });

      expect(result).toEqual({ outcome: "rejected", reason: "Chef not timely available" });
    });
    it("Cook in Hamburg, dinner on Oct 30, only Ben and Clara exist -- selectable Chefs are empty, so no request can be created", () => {
      const selectable = selectableChefs({
        cook: premiumCookInHamburg,
        mealDate: "2026-10-30",
        chefs: [chefBen, chefClara],
      });

      expect(selectable).toEqual([]);
    });
  });

  describe("Rule 5: the Cook can cancel an open request at any time", () => {
    it("Cook cancels an open request -- request is cancelled", () => {
      const result = cancelRequest(openRequestToAnna());

      expect(result).toMatchObject({ outcome: "cancelled", request: { chef: "Anna", status: "cancelled" } });
    });
    it("Cook cancels an already cancelled request -- rejected with reason 'not open'", () => {
      const first = cancelRequest(openRequestToAnna());
      if (first.outcome !== "cancelled") {
        throw new Error(`fixture expected a cancelled request, got ${first.outcome}`);
      }

      const result = cancelRequest(first.request);

      expect(result).toEqual({ outcome: "rejected", reason: "not open" });
    });
  });

  describe("Rule 6: the system cancels a Chef request unanswered after exactly 48 hours", () => {
    it("request on Oct 21 10:00, unanswered, checked on Oct 23 09:59 -- request is still open", () => {
      const result = applyAnswerTimeout(openRequestToAnna(), "2026-10-23T09:59");

      expect(result).toMatchObject({ chef: "Anna", status: "open" });
    });
    it("request on Oct 21 10:00, unanswered, checked on Oct 23 10:00 -- request is cancelled by the system", () => {
      const result = applyAnswerTimeout(openRequestToAnna(), "2026-10-23T10:00");

      expect(result).toMatchObject({ chef: "Anna", status: "cancelled", cancelledBy: "system" });
    });
    it("request on Oct 21 10:00 cancelled by the Cook, checked on Oct 23 10:00 -- request stays cancelled by the Cook", () => {
      const cancelled = cancelRequest(openRequestToAnna());
      if (cancelled.outcome !== "cancelled") {
        throw new Error(`fixture expected a cancelled request, got ${cancelled.outcome}`);
      }

      const result = applyAnswerTimeout(cancelled.request, "2026-10-23T10:00");

      expect(result).toEqual(cancelled.request);
    });
    it("request on Oct 22 15:30, unanswered, checked on Oct 23 10:00 -- request is still open (only 18h 30min passed; the deadline depends on the request time)", () => {
      const result = applyAnswerTimeout(openRequestToAnna("2026-10-22T15:30"), "2026-10-23T10:00");

      expect(result).toMatchObject({ chef: "Anna", status: "open" });
    });
    it("Cook cancels a request the system cancelled on Oct 23 10:00 -- rejected with reason 'not open'", () => {
      const cancelledBySystem = applyAnswerTimeout(openRequestToAnna(), "2026-10-23T10:00");

      const result = cancelRequest(cancelledBySystem);

      expect(result).toEqual({ outcome: "rejected", reason: "not open" });
    });
  });

  describe("Rule 4: a Chef can review a Grandma Avatar meal plan of the Cook's own earlier request", () => {
    it("Cook got a Grandma Avatar answer to their own request and asks Chef Anna to review it -- review request is created and open", () => {
      const result = requestReview({
        cook: premiumCookInHamburg,
        chef: chefAnna,
        grandmaAnswer: grandmaAnswerForMaria,
        requestedAt: "2026-10-21T10:00",
      });

      expect(result).toMatchObject({ outcome: "created", request: { chef: "Anna", status: "open" } });
    });
    it("Cook asks Chef Anna to review another Cook's Grandma Avatar answer -- rejected with reason 'not own Grandma answer'", () => {
      const grandmaAnswerForJonas = { answeredFor: "Jonas", mealPlan: "Dinner plan for Oct 30" };

      const result = requestReview({
        cook: premiumCookInHamburg,
        chef: chefAnna,
        grandmaAnswer: grandmaAnswerForJonas,
        requestedAt: "2026-10-21T10:00",
      });

      expect(result).toEqual({ outcome: "rejected", reason: "not own Grandma answer" });
    });
    it("non-premium Cook asks Chef Anna to review their own Grandma Avatar answer -- rejected with reason 'upgrade required' (reading: Rule 2 applies to every request to a Chef)", () => {
      const result = requestReview({
        cook: nonPremiumCookInHamburg,
        chef: chefAnna,
        grandmaAnswer: grandmaAnswerForMaria,
        requestedAt: "2026-10-21T10:00",
      });

      expect(result).toEqual({ outcome: "rejected", reason: "upgrade required" });
    });
  });
});
