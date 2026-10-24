# Cook Assistance — slice plan (proposal)

Step 1 (4b), 2026-10-24. Derived from `01-boards-and-slice-tables.md` and the decisions F1–F6.
Aggregates: **Help request** and **Help**, discriminator *Menu* (board "Meal plan"), *Preparation step*,
*Ingredients*, *Catastrophe*. *inferred* = command/event not on a board, derived from a rule sticky and
confirmed by the user (F2).

## Merges

A slice with the same command and aggregate on several boards is merged into one slice, owned by the
board that is built first. Each later board adds only its **discriminator variant** — its own guards
and timeout — to the merged slice:

| Merged slice | Boards | Variant per discriminator |
|---|---|---|
| B1-S03 Provide Help (actor) | B1, B2, B3, B4 | Menu: Chef, community or Grandma · others: community or Grandma (F4); Preparation step / Catastrophe: one step in one recipe; Ingredients: one ingredient in one recipe |
| B1-S04 Provide Help (Grandma Avatar message) | B1, B2, B3, B4 | as B1-S03 |
| B1-S05 Accept help | B1, B2, B3, B4 | — |
| B1-S06 Cancel help request (Cook) | B1, B2, B3, B4 | threshold per discriminator (open: same as S07?) |
| B1-S07 Cancel help request (time) | B1, B2, B3, B4 | Menu 48 h · Ingredients 24 h · Catastrophe 10 min · Preparation step 5 h |

The four "Request help <topic>" commands stay separate slices: one aggregate (F1), but each board has its own guards.
*Provide Help* is split by trigger: actors through the API (S03) and the Grandma Avatar AI message (S04, F3).

## Slices

| Slice | Trigger | Command | Aggregate | Event(s) | RM consulted | RM updated | Depends on | Source |
|---|---|---|---|---|---|---|---|---|
| **B1-S01** | actor Cook | Request help meal plan | Help request (Menu) | Help requested | — | Help request (Menu) | — | board B1 |
| **B1-S02** | actor Cook | Request Chef review | Help request (Menu) | Chef review requested | Help (Menu, by Grandma Avatar) | Help request (Menu) | B1-S01, B1-S04 | rule B1, inferred |
| **B1-S03** | actor Cook, Chef | Provide Help | Help | Help provided | Help request | Help | B1-S01 | boards B1–B4 (merged) |
| **B1-S04** | message *Help provided* from Grandma Avatar AI | Provide Help | Help | Help provided | Help request | Help | B1-S01 (its *Help requested* reaches Grandma Avatar AI) | boards B1–B4 (merged), F3 |
| **B1-S05** | actor Cook | Accept help | Help | Help accepted | Help | Help | B1-S03 or B1-S04 | rule B1–B4, inferred |
| **B1-S06** | actor Cook | Cancel help request | Help request | Help request cancelled | Help request | Help request | B1-S01 | rule B1–B4, inferred |
| **B1-S07** | time: answer takes too long | Cancel help request | Help request | Help request cancelled | Help request | Help request | B1-S01 | rule B1–B4, inferred |
| **B1-S08** | policy: whenever *Help accepted* | Close help request | Help request | Help request closed | — | Help request | B1-S05 | user decision 2026-10-24 (step 2, E2) |
| **B1-S09** | actor Cook | Reject help | Help | Help rejected | Help | Help | B1-S03 or B1-S04 | Example Mapping Preparation step R10, 2026-10-24 (merged B1–B4) |
| **B1-S10** | policy: whenever *Help rejected* | Restart answer deadline | Help request | Answer deadline restarted | — | Help request | B1-S09 | Example Mapping R10, 2026-10-24 |
| **B2-S01** | actor Cook | Request help preparation step | Help request (Preparation step) | Help requested | — | Help request (Preparation step) | — (variants on B1-S03…S07) | board B2 |
| **B3-S01** | actor Cook | Request help ingredients | Help request (Ingredients) | Help requested | — | Help request (Ingredients) | — (variants on B1-S03…S07) | board B3 |
| **B4-S01** | actor Cook | Request help catastrophe | Help request (Catastrophe) | Help requested | — | Help request (Catastrophe) | — (variants on B1-S03…S07) | board B4 |

Published events (context map v2): *Help requested* → Grandma Avatar AI, Notification; *Help provided* → Notification.
Consumed: *Help provided* from Grandma Avatar AI (B1-S04).

## Board order (proposal)

1. **B1 — Meal plan** — most commands (3 of its own plus the 5 merged slices); every other board depends on its merged slices.
2. **B2 — Preparation step** — adds the "recipe and step" guards and the 5 h timeout.
3. **B4 — Catastrophe** — reuses the B2 guard shape ("recipe and step"), adds 10 min.
4. **B3 — Ingredients** — new guard shape ("recipe and ingredient"), 24 h.

No cycles between boards.

**Accepted by the user, 2026-10-24** (slices, merges, board order B1 → B2 → B4 → B3). Revised in step 2 (2026-10-24): + B1-S08 Close help request (11 slices). Revised in step 3 (2026-10-24, Example Mapping Preparation step): + B1-S09 Reject help, + B1-S10 Restart answer deadline (13 slices); new border Cook Assistance → Recipe Catalog (sync lookup of a recipe's steps, B2-S01 guard).

## Walking skeleton (accepted 2026-10-24)

**B2-S01** Request help preparation step + **B1-S04** Provide Help from the Grandma Avatar AI message
(Preparation step variant). Together they reach HTTP in, domain (both aggregates), database, outbox,
broker out, broker in (ACL consumer) and both read models behind GET. Layers not reached: none.

## Increments

| Increment | Scope |
|---|---|
| 0 — Walking skeleton | B2-S01; B1-S04 (Preparation step variant) |
| 1 — Board: Meal plan | B1-S01, B1-S02, B1-S03, B1-S05, B1-S06, B1-S07, B1-S08, B1-S09, B1-S10 (Menu variants), B1-S04 Menu variant |
| 2 — Board: Preparation step | Preparation step variants of B1-S03, B1-S05, B1-S06, B1-S07 |
| 3 — Board: Catastrophe | B4-S01; Catastrophe variants of B1-S03…S07 |
| 4 — Board: Ingredients | B3-S01; Ingredients variants of B1-S03…S07 |
