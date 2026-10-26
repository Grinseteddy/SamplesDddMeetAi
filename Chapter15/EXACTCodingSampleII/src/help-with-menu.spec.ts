import { describe, expect, it } from "vitest";
import {
  acceptMenu,
  answerRequest,
  applyTimeout,
  askChef,
  closeRequest,
  passToChef,
  premiumEnded,
  rejectMenu,
  reviewOptionOffered,
  selectableChefs,
  withdrawRequest,
  type MenuRequest,
} from "./help-with-menu.js";

// Spec: src/help-with-menu-example-mapping.md (US-CA-01 Help with Menu)
//
// Calendar of the examples: the week of Mon 2026-11-02; the Occasion
// "Mother-in-law visiting" is Sat 2026-11-07 19:00 CET (+01:00), 4 guests,
// vegetarian, budget 60 €. New York is on EST (-05:00) that week, so CET-6 holds.
//
// Readings chosen where the board leaves the contract open:
// - Every operation returns a result. A refusal is { outcome: "rejected", reason }
//   with a named reason; it never throws and never creates or changes a request.
// - "Marco told" means the result carries a notice addressed to Marco.
// - Times are instants with an explicit offset; a Chef's working times are given in
//   the Chef's own local time with its offset.
// - Status names: open, answered, closed (accepted, or ended by the Cook or by the
//   end of premium), withdrawn (cancelled before any answer), timed out.

const MON_18_00 = "2026-11-02T18:00+01:00";
const WED_18_00 = "2026-11-04T18:00+01:00";
const FRI_19_00 = new Date("2026-11-06T19:00+01:00");

const lena = { name: "Lena", premiumUntil: "2026-12-31T23:59+01:00" };
const marco = {
  name: "Marco",
  workingTimes: [{ from: "2026-11-02T09:00+01:00", until: "2026-11-08T18:00+01:00" }],
};
const ana = { ...marco, name: "Ana" };
const samAwayNextWeek = {
  name: "Sam",
  workingTimes: [{ from: "2026-11-11T09:00-05:00", until: "2026-11-11T18:00-05:00" }],
};
const samAwayUntilSaturday = {
  name: "Sam",
  workingTimes: [{ from: "2026-11-07T09:00-05:00", until: "2026-11-07T18:00-05:00" }],
};
const motherInLawVisiting = {
  title: "Mother-in-law visiting",
  at: "2026-11-07T19:00+01:00",
  guests: 4,
  dietaryNeeds: ["vegetarian"],
  budget: 60,
};

const starterMainDessert = {
  courses: [
    { name: "Pumpkin soup", recipe: "Roast the pumpkin, blend with stock", suitableFor: ["vegetarian"], cost: 12 },
    { name: "Mushroom risotto", recipe: "Toast the rice, add stock ladle by ladle", suitableFor: ["vegetarian"], cost: 28 },
    { name: "Apple crumble", recipe: "Bake apples under a butter crumble", suitableFor: ["vegetarian"], cost: 10 },
  ],
};

function openRequestToMarco(askedAt = MON_18_00): MenuRequest {
  const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt });
  if (result.outcome !== "sent") {
    throw new Error(`fixture expected a sent request, got ${result.reason}`);
  }
  return result.request;
}

function menuFromMarco(answeredAt = "2026-11-03T12:00+01:00"): MenuRequest {
  const answer = answerRequest(openRequestToMarco(), starterMainDessert, answeredAt);
  if (answer.outcome !== "answered") {
    throw new Error(`fixture expected an answered request, got ${answer.reason}`);
  }
  return answer.request;
}

describe("US-CA-01 Help with Menu", () => {
  describe("R1: a request covers exactly one Occasion", () => {
    it("Lena asks Marco on Mon 18:00 for a Menu for 'Mother-in-law visiting', Sat 19:00, 4 guests, vegetarian, 60 € -- open request to Marco carrying that Occasion and no courses", () => {
      const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      expect(result).toMatchObject({
        outcome: "sent",
        request: { chef: "Marco", status: "open", occasion: motherInLawVisiting },
      });
      expect(result.outcome === "sent" && result.request.menu).toBeUndefined();
    });
    it("Lena needs Menus for Saturday dinner and Sunday lunch -- two separate requests, each carrying one Occasion", () => {
      const sundayLunch = { ...motherInLawVisiting, title: "Sunday lunch", at: "2026-11-08T12:30+01:00" };

      const dinner = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: MON_18_00 });
      const lunch = askChef({ cook: lena, chef: marco, occasion: sundayLunch, askedAt: MON_18_00 });

      expect(dinner).toMatchObject({ outcome: "sent", request: { occasion: motherInLawVisiting } });
      expect(lunch).toMatchObject({ outcome: "sent", request: { occasion: sundayLunch } });
    });
  });

  describe("R4: a Cook picks a Chef by name", () => {
    it("Lena picks Marco -- only Marco is told (one notice, addressed to Marco)", () => {
      const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      expect(result).toMatchObject({ outcome: "sent", notices: [{ to: "Marco" }] });
    });
  });

  describe("R8: the first request times out after 48 h or 24 h before the Occasion, whichever is earlier", () => {
    it("asked Mon 18:00 for Sat 19:00 -- deadline Wed 18:00", () => {
      const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      expect(result).toMatchObject({ outcome: "sent", request: { deadline: new Date("2026-11-04T18:00+01:00") } });
    });
    it("asked Thu 10:00 for Sat 19:00 -- deadline Fri 19:00", () => {
      const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: "2026-11-05T10:00+01:00" });

      expect(result).toMatchObject({ outcome: "sent", request: { deadline: new Date("2026-11-06T19:00+01:00") } });
    });
  });

  describe("R9: a new request needs at least 48 h before the Occasion", () => {
    it("asked Thu 19:00 for Sat 19:00 -- request is sent, deadline Fri 19:00", () => {
      const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: "2026-11-05T19:00+01:00" });

      expect(result).toMatchObject({ outcome: "sent", request: { deadline: new Date("2026-11-06T19:00+01:00") } });
    });
    it("asked Thu 20:00 for Sat 19:00 -- rejected with reason '48 h needed'", () => {
      const result = askChef({ cook: lena, chef: marco, occasion: motherInLawVisiting, askedAt: "2026-11-05T20:00+01:00" });

      expect(result).toEqual({ outcome: "rejected", reason: "48 h needed" });
    });
  });

  describe("R3: only a premium Cook can request", () => {
    it("Tom (free plan) asks Marco -- rejected with reason 'upgrade required', no request", () => {
      const tom = { name: "Tom" };

      const result = askChef({ cook: tom, chef: marco, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      expect(result).toEqual({ outcome: "rejected", reason: "upgrade required" });
    });
    it("Lena's premium ended yesterday (Sun), she asks Marco on Mon 18:00 -- rejected with reason 'upgrade required', no request", () => {
      const lenaAfterPremium = { ...lena, premiumUntil: "2026-11-01T12:00+01:00" };

      const result = askChef({ cook: lenaAfterPremium, chef: marco, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      expect(result).toEqual({ outcome: "rejected", reason: "upgrade required" });
    });
  });

  describe("R2: Chefs never handle substitutions", () => {
    it("Lena asks Marco for a crème fraîche substitute -- rejected with reason 'Chefs never handle substitutions', nothing goes to Marco, Community and Grandma Avatar are suggested", () => {
      const result = askChef({
        cook: lena,
        chef: marco,
        occasion: motherInLawVisiting,
        askedAt: MON_18_00,
        asksFor: "substitution",
      });

      expect(result).toEqual({
        outcome: "rejected",
        reason: "Chefs never handle substitutions",
        suggested: ["Community", "Grandma Avatar"],
      });
    });
  });

  describe("R5: only Chefs who can answer in time can be selected, in their own time zone", () => {
    it("deadline Fri 19:00 CET, Sam in New York works Thu until 18:00 local -- Sam is listed", () => {
      const samWorkingThursday = {
        name: "Sam",
        workingTimes: [{ from: "2026-11-05T09:00-05:00", until: "2026-11-05T18:00-05:00" }],
      };

      expect(selectableChefs([samWorkingThursday], FRI_19_00)).toEqual(["Sam"]);
    });
    it("deadline Fri 19:00 CET, Sam is away until Saturday -- Sam is not listed", () => {
      expect(selectableChefs([samAwayUntilSaturday], FRI_19_00)).toEqual([]);
    });
    it("deadline Fri 19:00 CET, Sam starts work Fri 14:00 New York time (Fri 20:00 CET) -- Sam is not listed (reading: the Chef's local time is compared as an instant)", () => {
      const samStartingFridayAfternoon = {
        name: "Sam",
        workingTimes: [{ from: "2026-11-06T14:00-05:00", until: "2026-11-06T18:00-05:00" }],
      };

      expect(selectableChefs([samStartingFridayAfternoon], FRI_19_00)).toEqual([]);
    });
    it("Lena asks Sam, who is away until Saturday, on Mon 18:00 -- rejected with reason 'Chef cannot answer in time' (reading: R5 also guards the request itself)", () => {
      const result = askChef({ cook: lena, chef: samAwayUntilSaturday, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      expect(result).toEqual({ outcome: "rejected", reason: "Chef cannot answer in time" });
    });
  });

  describe("R2 and Q1: the Answer is a complete Menu with a recipe for each course, within the Occasion's diet and budget", () => {
    it("Marco proposes starter, main and dessert with a recipe each -- request is answered, Menu with 3 courses and 3 recipes reaches Lena", () => {
      const result = answerRequest(openRequestToMarco(), starterMainDessert, "2026-11-03T12:00+01:00");

      expect(result).toMatchObject({ outcome: "answered", request: { status: "answered", menu: starterMainDessert } });
    });
    it("Marco proposes main and dessert only -- request is answered with a valid 2-course Menu", () => {
      const mainAndDessert = { courses: starterMainDessert.courses.slice(1) };

      const result = answerRequest(openRequestToMarco(), mainAndDessert, "2026-11-03T12:00+01:00");

      expect(result).toMatchObject({ outcome: "answered", request: { status: "answered", menu: mainAndDessert } });
    });
    it("Marco proposes 3 courses but the dessert has no recipe -- rejected with reason 'every course needs a recipe', request stays open", () => {
      const request = openRequestToMarco();
      const dessertWithoutRecipe = {
        courses: [...starterMainDessert.courses.slice(0, 2), { name: "Apple crumble" }],
      };

      const result = answerRequest(request, dessertWithoutRecipe, "2026-11-03T12:00+01:00");

      expect(result).toEqual({ outcome: "rejected", reason: "every course needs a recipe" });
      expect(request.status).toBe("open");
    });
    it("Marco proposes a beef main for the vegetarian Occasion -- rejected with reason 'Menu breaks dietary needs', request stays open (reading: a course meets a dietary need only when it lists it)", () => {
      const request = openRequestToMarco();
      const beefMain = { name: "Beef Wellington", recipe: "Wrap the fillet in pastry, bake", suitableFor: [] };
      const menuWithBeefMain = {
        courses: [starterMainDessert.courses[0], beefMain, starterMainDessert.courses[2]],
      };

      const result = answerRequest(request, menuWithBeefMain, "2026-11-03T12:00+01:00");

      expect(result).toEqual({ outcome: "rejected", reason: "Menu breaks dietary needs" });
      expect(request.status).toBe("open");
    });
    it("Marco proposes a Menu costing 75 € for the 60 € budget -- rejected with reason 'Menu over budget', request stays open (reading: the budget covers the whole Menu, the sum of its course costs)", () => {
      const request = openRequestToMarco();
      const truffleRisotto = { ...starterMainDessert.courses[1], name: "Truffle risotto", cost: 53 };
      const menuFor75Euros = {
        courses: [starterMainDessert.courses[0], truffleRisotto, starterMainDessert.courses[2]],
      };

      const result = answerRequest(request, menuFor75Euros, "2026-11-03T12:00+01:00");

      expect(result).toEqual({ outcome: "rejected", reason: "Menu over budget" });
      expect(request.status).toBe("open");
    });
  });

  describe("R8: timeout of an unanswered request", () => {
    it("asked Mon 18:00, Marco answers Wed 17:58, checked Wed 18:00 -- answer counts, request stays answered, no timeout", () => {
      const answer = answerRequest(openRequestToMarco(), starterMainDessert, "2026-11-04T17:58+01:00");
      if (answer.outcome !== "answered") {
        throw new Error(`fixture expected an answered request, got ${answer.reason}`);
      }

      const result = applyTimeout(answer.request, "2026-11-04T18:00+01:00", [marco]);

      expect(result).toMatchObject({ request: { status: "answered", menu: starterMainDessert } });
    });
    it("asked Mon 18:00, Marco silent, checked Wed 17:59 -- request is still open", () => {
      const result = applyTimeout(openRequestToMarco(), "2026-11-04T17:59+01:00", [marco]);

      expect(result).toMatchObject({ request: { chef: "Marco", status: "open" } });
    });
    it("asked Mon 18:00, Marco silent until Wed 18:00 -- request times out, Ana is suggested (Marco and Sam, away next week, are not)", () => {
      const result = applyTimeout(openRequestToMarco(), "2026-11-04T18:00+01:00", [marco, ana, samAwayNextWeek]);

      expect(result).toMatchObject({ request: { chef: "Marco", status: "timed out" }, suggested: ["Ana"] });
    });
    it("Lena takes Ana after the timeout on Wed 18:00 -- request goes to Ana, Ana is told, Ana's deadline Fri 18:00, no note", () => {
      const timedOut = applyTimeout(openRequestToMarco(), WED_18_00, [marco, ana]).request;

      const result = passToChef(timedOut, ana, WED_18_00);

      expect(result).toMatchObject({
        outcome: "passed",
        request: { chef: "Ana", status: "open", deadline: new Date("2026-11-06T18:00+01:00") },
        notices: [{ to: "Ana" }],
      });
      expect(result.outcome === "passed" && result.request.note).toBeUndefined();
    });
    it("Lena takes Ana on Fri 19:00 -- deadline Sun 19:00 with note '24 h to the Occasion' (Q2: no cap)", () => {
      const timedOut = applyTimeout(openRequestToMarco(), WED_18_00, [marco, ana]).request;

      const result = passToChef(timedOut, ana, "2026-11-06T19:00+01:00");

      expect(result).toMatchObject({
        outcome: "passed",
        request: { chef: "Ana", deadline: new Date("2026-11-08T19:00+01:00"), note: "24 h to the Occasion" },
      });
    });
  });

  describe("R7 and R4: the Cook accepts, rejects or leaves the Answer", () => {
    it("Lena accepts Marco's Menu -- request is closed", () => {
      const result = acceptMenu(menuFromMarco());

      expect(result).toMatchObject({ chef: "Marco", status: "closed", menu: starterMainDessert });
    });
    it("Lena asks Marco and Ana and accepts Marco's Menu -- Marco's request is closed, Ana's stays open", () => {
      const toAna = askChef({ cook: lena, chef: ana, occasion: motherInLawVisiting, askedAt: MON_18_00 });

      const marcosRequest = acceptMenu(menuFromMarco());

      expect(marcosRequest).toMatchObject({ chef: "Marco", status: "closed" });
      expect(toAna).toMatchObject({ outcome: "sent", request: { chef: "Ana", status: "open" } });
    });
    it("Lena never responds to Marco's Menu, checked the following Monday -- request stays answered, the Menu stays with her, nothing expires", () => {
      const result = applyTimeout(menuFromMarco(), "2026-11-09T18:00+01:00", [marco, ana]);

      expect(result).toMatchObject({
        request: { chef: "Marco", status: "answered", menu: starterMainDessert },
        suggested: [],
      });
    });
    it("Lena rejects Marco's Menu on Wed 18:00 -- request is open again, Marco may answer until Fri 18:00, no note", () => {
      const result = rejectMenu(menuFromMarco(), WED_18_00);

      expect(result).toMatchObject({ chef: "Marco", status: "open", deadline: new Date("2026-11-06T18:00+01:00") });
      expect(result.note).toBeUndefined();
    });
    it("Lena rejects Marco's Menu on Fri 21:00, Occasion Sat 19:00 -- deadline Sun 21:00 with note '22 h to the Occasion' (Q2: no cap)", () => {
      const askedThursday = openRequestToMarco("2026-11-05T10:00+01:00");
      const answer = answerRequest(askedThursday, starterMainDessert, "2026-11-06T12:00+01:00");
      if (answer.outcome !== "answered") {
        throw new Error(`fixture expected an answered request, got ${answer.reason}`);
      }

      const result = rejectMenu(answer.request, "2026-11-06T21:00+01:00");

      expect(result).toMatchObject({
        status: "open",
        deadline: new Date("2026-11-08T21:00+01:00"),
        note: "22 h to the Occasion",
      });
    });
    it("Lena rejects Marco's Menu on Wed 18:00 and passes it to Ana -- request goes to Ana, Ana is told, Ana sees the Occasion but not Marco's Menu, deadline Fri 18:00", () => {
      const rejected = rejectMenu(menuFromMarco(), WED_18_00);

      const result = passToChef(rejected, ana, WED_18_00);

      expect(result).toMatchObject({
        outcome: "passed",
        request: { chef: "Ana", status: "open", occasion: motherInLawVisiting, deadline: new Date("2026-11-06T18:00+01:00") },
        notices: [{ to: "Ana" }],
      });
      expect(result.outcome === "passed" && result.request.menu).toBeUndefined();
    });
    it("Lena rejects Marco's Menu and tries to pass it to Sam, away next week -- rejected with reason 'Chef cannot answer in time', Sam is not offered", () => {
      const rejected = rejectMenu(menuFromMarco(), WED_18_00);

      const result = passToChef(rejected, samAwayNextWeek, WED_18_00);

      expect(result).toEqual({ outcome: "rejected", reason: "Chef cannot answer in time" });
    });
    it("Lena cancels her request to Marco before any answer -- request is withdrawn, Marco is told", () => {
      const result = withdrawRequest(openRequestToMarco());

      expect(result).toMatchObject({ request: { chef: "Marco", status: "withdrawn" }, notices: [{ to: "Marco" }] });
    });
    it("Lena closes her open request to Ana herself -- request is closed, Ana is told", () => {
      const toAna = askChef({ cook: lena, chef: ana, occasion: motherInLawVisiting, askedAt: MON_18_00 });
      if (toAna.outcome !== "sent") {
        throw new Error(`fixture expected a sent request, got ${toAna.reason}`);
      }

      const result = closeRequest(toAna.request);

      expect(result).toMatchObject({ request: { chef: "Ana", status: "closed" }, notices: [{ to: "Ana" }] });
    });
  });

  describe("R3: an open request closes when premium ends", () => {
    it("Lena's premium ends Tue, her request to Marco is open -- request is closed on Tue, Marco is told", () => {
      const result = premiumEnded([openRequestToMarco()]);

      expect(result).toMatchObject({ requests: [{ chef: "Marco", status: "closed" }], notices: [{ to: "Marco" }] });
    });
  });

  describe("R6: a Chef can review any Menu from the Grandma Avatar", () => {
    it("Grandma proposed 'Lasagne and salad', Lena asks Marco to review it -- open request to Marco with Grandma's Menu attached", () => {
      const lasagneAndSalad = { courses: [{ name: "Lasagne" }, { name: "Salad" }] };

      const result = askChef({
        cook: lena,
        chef: marco,
        occasion: motherInLawVisiting,
        askedAt: MON_18_00,
        grandmaMenu: lasagneAndSalad,
      });

      expect(result).toMatchObject({
        outcome: "sent",
        request: { chef: "Marco", status: "open", grandmaMenu: lasagneAndSalad },
      });
    });
    it("Lena has no Grandma Menu -- review option is not offered", () => {
      expect(reviewOptionOffered([])).toBe(false);
    });
  });
});
