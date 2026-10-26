# Test List: US-CA-01 Help with Menu

- **Spec:** `EXACTCodingSampleII/src/help-with-menu-example-mapping.md` (board `fig6-llm-review-q2.png`, Q1, Q2 and scope clarified 2026-10-26)
- **Test file:** `EXACTCodingSampleII/src/help-with-menu.spec.ts`
- **Tests:** 36, all inactive (`it.todo`). Suite: 1 file skipped, 36 todo, exit 0.
- **Status:** waiting for approval at the Test List checkpoint.

## Calendar

The examples happen in the week of Mon 2026-11-02. The Occasion "Mother-in-law visiting" is Sat 2026-11-07 19:00 CET (+01:00), 4 guests, vegetarian, budget 60 €. New York is on EST (−05:00) that week, so the board's "CET−6" holds.

## Readings

- Every operation returns a result. A refusal is `{ outcome: "rejected", reason }` with a named reason. It never throws and never creates or changes a request.
- "Marco told" means the result carries a notice addressed to Marco.
- Times are instants with an explicit offset. A Chef's working times are given in the Chef's own local time with its offset.
- Status names: open, answered, closed (accepted, or ended by the Cook or by the end of premium), withdrawn (cancelled before any answer), timed out.

## Test cases (simple → complex)

| # | Rule | Example | Expected |
|---|---|---|---|
| 1 | R1 | Lena asks Marco on Mon 18:00 for a Menu for "Mother-in-law visiting", Sat 19:00, 4 guests, vegetarian, 60 € | Open request to Marco carrying that Occasion and no courses |
| 2 | R1 | Lena needs Menus for Saturday dinner and Sunday lunch | Two separate requests, each carrying one Occasion |
| 3 | R4 | Lena picks Marco | Only Marco is told (one notice, addressed to Marco) |
| 4 | R8 | Asked Mon 18:00 for Sat 19:00 | Deadline Wed 18:00 |
| 5 | R8 | Asked Thu 10:00 for Sat 19:00 | Deadline Fri 19:00 |
| 6 | R9 | Asked Thu 19:00 for Sat 19:00 | Request is sent, deadline Fri 19:00 |
| 7 | R9 | Asked Thu 20:00 for Sat 19:00 | Rejected: `48 h needed` |
| 8 | R3 | Tom (free plan) asks Marco | Rejected: `upgrade required`, no request |
| 9 | R3 | Lena's premium ended yesterday (Sun), she asks Marco on Mon 18:00 | Rejected: `upgrade required`, no request |
| 10 | R2 | Lena asks Marco for a crème fraîche substitute | Rejected: `Chefs never handle substitutions`; nothing goes to Marco; Community and Grandma Avatar are suggested |
| 11 | R5 | Deadline Fri 19:00 CET, Sam in New York works Thu until 18:00 local | Sam is listed |
| 12 | R5 | Deadline Fri 19:00 CET, Sam is away until Saturday | Sam is not listed |
| 13 | R5 | Deadline Fri 19:00 CET, Sam starts work Fri 14:00 New York time (Fri 20:00 CET) | Sam is not listed (reading: the Chef's local time is compared as an instant) |
| 14 | R5 | Lena asks Sam, who is away until Saturday, on Mon 18:00 | Rejected: `Chef cannot answer in time` (reading: R5 also guards the request itself) |
| 15 | R2 | Marco proposes starter, main and dessert with a recipe each | Request is answered; Menu with 3 courses and 3 recipes reaches Lena |
| 16 | R2 | Marco proposes main and dessert only | Request is answered with a valid 2-course Menu |
| 17 | R2 | Marco proposes 3 courses but the dessert has no recipe | Rejected: `every course needs a recipe`; request stays open |
| 18 | Q1 | Marco proposes a beef main for the vegetarian Occasion | Rejected: `Menu breaks dietary needs`; request stays open |
| 19 | Q1 | Marco proposes a Menu costing 75 € for the 60 € budget | Rejected: `Menu over budget`; request stays open (reading: the budget covers the whole Menu, the sum of its course costs) |
| 20 | R8 | Asked Mon 18:00, Marco answers Wed 17:58, checked Wed 18:00 | Answer counts; request stays answered; no timeout |
| 21 | R8 | Asked Mon 18:00, Marco silent, checked Wed 17:59 | Request is still open |
| 22 | R8 | Asked Mon 18:00, Marco silent until Wed 18:00 | Request times out; Ana is suggested (Marco and Sam, away next week, are not) |
| 23 | R8 | Lena takes Ana after the timeout on Wed 18:00 | Request goes to Ana, Ana is told, Ana's deadline Fri 18:00, no note |
| 24 | R8 | Lena takes Ana on Fri 19:00 | Deadline Sun 19:00 with note "24 h to the Occasion" (Q2: no cap) |
| 25 | R7 | Lena accepts Marco's Menu | Request is closed |
| 26 | R7, R4 | Lena asks Marco and Ana and accepts Marco's Menu | Marco's request is closed, Ana's stays open |
| 27 | R7 | Lena never responds to Marco's Menu, checked the following Monday | Request stays answered; the Menu stays with her; nothing expires |
| 28 | R7 | Lena rejects Marco's Menu on Wed 18:00 | Request is open again; Marco may answer until Fri 18:00; no note |
| 29 | R7 | Lena rejects Marco's Menu on Fri 21:00, Occasion Sat 19:00 | Deadline Sun 21:00 with note "22 h to the Occasion" (Q2: no cap) |
| 30 | R7 | Lena rejects Marco's Menu on Wed 18:00 and passes it to Ana | Request goes to Ana, Ana is told, Ana sees the Occasion but not Marco's Menu, deadline Fri 18:00 |
| 31 | R7 | Lena rejects Marco's Menu and tries to pass it to Sam, away next week | Rejected: `Chef cannot answer in time`; Sam is not offered |
| 32 | R7 | Lena cancels her request to Marco before any answer | Request is withdrawn, Marco is told |
| 33 | R4 | Lena closes her open request to Ana herself | Request is closed, Ana is told |
| 34 | R3 | Lena's premium ends Tue, her request to Marco is open | Request is closed on Tue, Marco is told |
| 35 | R6 | Grandma proposed "Lasagne and salad", Lena asks Marco to review it | Open request to Marco with Grandma's Menu attached |
| 36 | R6 | Lena has no Grandma Menu | Review option is not offered |

## Coverage cross-check

- **Operations:** each has its own tests: ask, list Chefs, answer, timeout, take another Chef, accept, reject, pass on, withdraw, close, premium end, and review.
- **Deadlines (R8):** the 48 h limit (#4) and the "24 h before the Occasion" limit (#5, #6) each decide one example. The two resets (taking another Chef, rejecting a Menu) are each tested with and without the note.
- **Answer validity (R2, Q1):** courses, recipes, diet and budget each have a separate refusal.
- **Time boundaries:** 17:58, 17:59 and 18:00 around the deadline; Thu 19:00 vs 20:00 for R9.

## Readings to confirm

1. **Withdrawn vs closed:** #32 says a request cancelled before any answer is "withdrawn". #33 says Lena closing Ana's unanswered request is "closed". These look like the same situation, and they are treated here as two different Cook actions. Should they be one action with one status?
2. **When the note appears:** only when the reset deadline falls after the Occasion. That is why Wed 18:00 → Fri 18:00 has no note.
3. **Budget:** it covers the whole Menu, so the course costs are added up.
4. **R5 when asking:** the R5 check also applies when Lena asks a Chef directly (#14), not only to the list.
5. **Review request:** it is an ordinary request for an Occasion with Grandma's Menu attached, so R3 and R9 apply to it too. There are no separate tests for that.
