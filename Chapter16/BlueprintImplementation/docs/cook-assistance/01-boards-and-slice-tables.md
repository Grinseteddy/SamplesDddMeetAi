# Cook Assistance — design-level boards: legend, transcription, slice tables

Step 1 (4a) of the implementation blueprint, 2026-10-24. Source: the four detail boards in
`Chapter16/assets/` and their nodes in `FullGraphWithHistory_ESDetail.ttl` (cross-checked: every
sticky on the photos has a graph node and vice versa; the purple "Help REQUESTED" sticky is held
in the graph as a state assertion, not a rule).

## 1. Legend and flavour (all four boards)

| Sticky as drawn | Icon | Read as | Deviation from the canonical palette |
|---|---|---|---|
| yellow, large text | person | actor | — |
| blue | ⌘ | command | — |
| pale yellow | briefcase | aggregate (graph: business object) | — |
| orange | sparkle | domain event | — |
| green | eye | read model | — |
| white/grey | monitor | UI sketch | — |
| pink | gear | system "Grandma Avatar" (graph: actor + external system) | drawn **below the aggregate, in the handler column** of *Provide Help*, not as a separate external system after an event |
| purple | shield | **invariant / rule** (constraint note) | purple normally means *policy*; here it carries rules — there are **no policy stickies** on any board |

Flavour: **Design-Level** — every board has aggregates (briefcase stickies) receiving commands.
No hotspot (red) stickies on any board.

## 2. Transcription (board order, verbatim)

All four boards have the same two-slice shape; only the topic and the rules differ.

### B1 — Meal plan (`EventStormingDetailsMealPlan.jpg`)
- Slice 1: **Cook** → *Request help meal plan* → **Help request Meal plan** → *Help requested* → RM *Help request Meal plan*, UI *Show help request*.
  Rules: "Help REQUESTED" · "Chef needs to be selected" · "Only premium users allowed to ask Chef" · "Chef can only be asked when cook is premium user." · "Chef can review a dinner plan of Grandma Avartar." · "Cook can cancel their request when answer takes too long" · "System cancels request when answer takes too long"
- Slice 2: **Cook**, **Chef** → *Provide Help* → **Help Meal plan** (+ system **Grandma Avatar** beneath) → *Help provided* → RM *Help Meal plan*, UI *Show help*.
  Rules: "Help Meal plan can be provided by Chef, community, or Grandma" · "Cook cannot answer their own request" · "Cook can accept the answer"

### B2 — Preparation step (`EventStormingDetailPreparationStep.jpg`)
- Slice 1: **Cook** → *Request help preparation step* → **Help request preparation step** → *Help requested* → RM *Help request preparation step*, UI *Show help request*.
  Rules: "Help REQUESTED" · "Chef is not allowed - only community or Grandma" · "System cancels request when answer takes too long" · "Cook can cancel their request when answer takes too long" · "Recipe and step in recipe must be provided"
- Slice 2: **Cook** → *Provide Help* → **Help preparation step** (+ **Grandma Avatar**) → *Help provided* → RM *Help preparation step*, UI *Show help*.
  Rules: "Help Meal plan can be provided by Chef, community, or Grandma" · "Cook cannot answer their own request" · "Cook can accept the answer" · "Help is provided for one step in one recipe"

### B3 — Ingredients (`EventStormingDetailIngredients.jpg`)
- Slice 1: **Cook** → *Request help ingredients* → **Help request ingredients** → *Help requested* → RM *Help request ingredients*, UI *Show help request*.
  Rules: "Help REQUESTED" · "Chef is not allowed - only community or Grandma" · "System cancels request when answer takes too long" · "Cook can cancel their request when answer takes too long" · "Recipe and ingredient in recipe must be provided"
- Slice 2: **Cook** → *Provide Help* → **Help Substitutes** (+ **Grandma Avatar**) → *Help provided* → RM *Help Ingredients*, UI *Show help*.
  Rules: "Help Meal plan can be provided by Chef, community, or Grandma" · "Cook cannot answer their own request" · "Cook can accept the answer" · "Help is provided for one ingredient in one recipe"

### B4 — Catastrophe (`EventStormingDetailCatastrophe.jpg`)
- Slice 1: **Cook** → *Request help catastrophe* → **Help request catastrophe** → *Help requested* → RM *Help request catastrophe*, UI *Show help request*.
  Rules: "Help REQUESTED" · "Chef is not allowed - only community or Grandma" · "System cancels request when answer takes too long" · "Cook can cancel their request when answer takes too long" · "Recipe and step in recipe must be provided"
- Slice 2: **Cook** → *Provide Help* → **Help catastrophe** (+ **Grandma Avatar**) → *Help provided* → RM *Help catastrophe*, UI *Show help*.
  Rules: "Help Meal plan can be provided by Chef, community, or Grandma" · "Cook cannot answer their own request" · "Cook can accept the answer" · "Help is provided for one step in one recipe"

## 3. Slice tables

| Board | # | Trigger | Command | Aggregate / system | Event(s) | RM consulted | RM updated |
|---|---|---|---|---|---|---|---|
| B1 Meal plan | 1 | actor Cook | Request help meal plan | Help request Meal plan | Help requested | — | Help request Meal plan |
| B1 Meal plan | 2 | actor Cook, Chef | Provide Help | Help Meal plan; Grandma Avatar | Help provided | Help request Meal plan *(by position: UI "Show help request" precedes the actor)* | Help Meal plan |
| B2 Preparation step | 1 | actor Cook | Request help preparation step | Help request preparation step | Help requested | — | Help request preparation step |
| B2 Preparation step | 2 | actor Cook | Provide Help | Help preparation step; Grandma Avatar | Help provided | Help request preparation step *(by position)* | Help preparation step |
| B3 Ingredients | 1 | actor Cook | Request help ingredients | Help request ingredients | Help requested | — | Help request ingredients |
| B3 Ingredients | 2 | actor Cook | Provide Help | Help Substitutes; Grandma Avatar | Help provided | Help request ingredients *(by position)* | Help Ingredients |
| B4 Catastrophe | 1 | actor Cook | Request help catastrophe | Help request catastrophe | Help requested | — | Help request catastrophe |
| B4 Catastrophe | 2 | actor Cook | Provide Help | Help catastrophe; Grandma Avatar | Help provided | Help request catastrophe *(by position)* | Help catastrophe |

## 4. Early findings (for the slice plan; the full check follows in step 2)

- F1 — **Resolved by the user, 2026-10-24:** two aggregates across all four boards — **Help request** and **Help** — each with a discriminator whose values are *Catastrophe*, *Ingredients*, *Meal plan* and *Preparation step* (one per board). The per-board aggregate names above ("Help request catastrophe", "Help Meal plan", …) are Help request / Help with that discriminator.
- F2 — **Resolved by the user, 2026-10-24:** the four behaviors present only as rules get slices. Command, aggregate and event are **inferred from the rule wording and confirmed by the user** (not on the boards):

  | Rule sticky (boards) | Trigger | Command | Aggregate | Event |
  |---|---|---|---|---|
  | "Cook can cancel their request when answer takes too long" (B1–B4) | actor Cook | Cancel help request | Help request | Help request cancelled |
  | "System cancels request when answer takes too long" (B1–B4) | time: answer takes too long | Cancel help request (by the system) | Help request | Help request cancelled |
  | "Cook can accept the answer" (B1–B4) | actor Cook | Accept help | Help | Help accepted |
  | "Chef can review a dinner plan of Grandma Avartar." (B1 only) | actor Cook | Request Chef review | Help request (Meal plan) | Chef review requested — the Chef then answers through *Provide Help* |
- F3 — **Resolved, 2026-10-24:** Grandma Avatar is a **provider via async message**. Grandma Avatar AI (another context) answers asynchronously; its *Help provided* message triggers *Provide Help* on **Help** in Cook Assistance (a consumer/policy; ACL at Grandma Avatar AI per context map v2). Cook and Chef trigger *Provide Help* as actors.
- F4 — **Resolved, 2026-10-24:** copy slip. "Help Meal plan can be provided by Chef, community, or Grandma" applies to the Meal plan discriminator only; for Ingredients, Preparation step and Catastrophe, "Chef is not allowed - only community or Grandma" holds.
- F5 — **Resolved, 2026-10-24:** code and specs use discriminator **Ingredients** (not "Substitutes"), **Menu** instead of "Meal plan" (SADR0003), and **Grandma Avatar**. Board titles keep their original wording.
- F6 — **Resolved, 2026-10-24:** the system cancels a help request when no answer arrives within: **Menu (Meal plan) 48 h · Ingredients 24 h · Catastrophe 10 min · Preparation step 5 h**. Open for step 3: does the cook's "Cook can cancel … when answer takes too long" use the same thresholds?
