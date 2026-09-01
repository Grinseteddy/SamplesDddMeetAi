# Pivotal Event Cut — Community Cooking board

**Mode: propose.** No dividers, no phase labels and no context bubbles are drawn
on the board, so this proposes them rather than reviewing a cut. No scope
exclusion was claimed with the board, and there are **no red hotspot stickies**
anywhere on it — worth noting, because on a board about catastrophes, stalls and
asking strangers for help, the absence of recorded disagreement is more likely
"not captured" than "nobody disagreed".

---

## 1. Event line as read

21 domain events, read left-to-right and top-to-bottom in three rows.
`RM` = green read model (eye), `BO` = pale-yellow business object (briefcase).

| # | Event | Command | Actor(s) | RM / BO | Notes |
|---|-------|---------|----------|---------|-------|
| 1 | Cook registered | Register cook | User | RM User · BO Cook | Board edge |
| 2 | Dinner planned | Plan dinner | Cook | RM Guests · BO Menu | |
| 3 | Recipes searched ✨ | Search recipes | Cook | RM Recipes | **loop** — searched repeatedly |
| 4 | Recipe selected ✨ | Search recipes | Cook | RM Recipe | loop with 3 |
| 5 | Ingredients missing | Search Ingredients | Cook | RM Recipe · RM Ingredients | **branch** |
| 6 | Meal planning stalled 🤖 | Search Ingredients | Cook | RM Guests · RM Menu | **branch** — no-progress event |
| 7 | Help requested | Request help | Cook | RM Menu · BO Help request | branch continuation of 5/6 |
| 8 | Help provided | Provide help | Community Cook · Chef · *Grandma Avatar* (pink) | RM Help request · BO Help response | **repeat of this cluster at 16** |
| 9 | Ingredients substituted | Substitute ingredients | Cook | RM Recipe · RM Help Response · BO Ingredients | branch resolution |
| 10 | Meal plan settled | Plan meal | Cook | RM Menu · RM Help Response · BO Meal plan | |
| 11 | Meal preparation started ✨ | Prepare meal | Cook | RM Recipe | |
| 12 | Step unclear ✨ | Prepare meal | Cook | RM Recipe | **loop** — per step, many times |
| 13 | Catastrophe happened | Prepare meal | Cook | RM Catastrophe · RM Recipe | **branch** |
| 14 | Pictures taken | Take pictures | Cook | RM Catastrophe · BO Pictures | **repeat of this cluster at 20** |
| 15 | Help requested | Request help | Cook | RM Recipe · RM Pictures · BO Help request | repeat of 7 |
| 16 | Help provided | Request help | Community Cook · *Grandma Avatar* (pink) | RM Help request · BO Help response | repeat of 8 |
| 17 | Step completed ✨ | Prepare meal | Cook | RM Help response | **loop** — per step |
| 18 | Meal rescued 🤖 | Prepare meal | Cook | RM Help response | branch resolution of 13 |
| 19 | Meal prepared ✨ | Prepare meal | Cook | RM Recipe | |
| 20 | Pictures taken | Take pictures | Cook | BO Pictures | repeat of 14 |
| 21 | Thanks given | Provide thanks | Cook | RM Help provider · RM Pictures · BO Thanks | Board edge |

**Assumptions flagged.** Events 5–9 are drawn as a detour off the planning line
and are read as one branch (ingredients turn out to be missing → help → a
substitution → the plan settles). Events 12–18 are read as an inner loop around
`Prepare meal`, not as a straight sequence. Loose stickies at the top right — a
green *Ingredients*, a blue *Search Ingredients*, a yellow *Cook* — are read as
belonging to the cluster at 5/6.

**Icons ✨ and 🤖: provenance or semantics?** ✨ sits on 3, 4, 11, 12, 17, 19 and
🤖 on 6 and 18. If 🤖 means "raised by the system without anyone pressing
anything", then 6 and 18 are the two events where software observes the cook
rather than records their action, which is a real domain statement. If it means
"this sticky was proposed by an assistant during the session", it says nothing
about the domain. **No divider below rests on either icon.** Confirming their
meaning is question 3 in §8.

---

## 2. Screening

| Removed | Why |
|---------|-----|
| 1, 21 | Board edges. 1 is picked back up below — its language change is too strong to bin silently. |
| 3, 4 | Search-and-select repeats until something is chosen; a divider is crossed once. |
| 5, 13 | Branch outcomes ("it went wrong"), not transitions of the whole process. |
| 6, 18 | Branch outcomes too, and the only structural argument for them is the unconfirmed 🤖 marker. |
| 7, 8, 15, 16 | The *same two clusters drawn twice*, once in planning and once in cooking. That is a recurring capability, not a point on the line — see §7. |
| 12, 17 | Inside the per-step loop. |
| 9 | Resolution of the 5–8 detour; the plan settling at 10 is the fact the business names. |
| 14, 20 | `Take pictures` appears twice for two different purposes; recurring. |

Survivors scored: **1, 2, 10, 11, 19**.

---

## 3. Scoring

| # | Event | Phase | Irrev. | Handover | Language | Commit | Clock | Narrow interface | Verdict |
|---|-------|-------|--------|----------|----------|--------|-------|------------------|---------|
| 1 | Cook registered | joining the service → using it | deregistration is its own named action, not an edit | — same person | **RM *User* → BO *Cook*; every actor sticky downstream says Cook, none says User** | terms accepted; identity now attributable | once, then months | cook id, name, kitchen/skill profile, dietary defaults — 4 stable fields, forever | **Divider** (edge-of-board; see §5) |
| 2 | Dinner planned | wanting to host → working out what to cook | replan freely | — Cook throughout | *Menu* minted; *Guests* appears | intention only, nobody is owed anything | none | search reads guest count + constraints + menu — narrow, but the same person keeps working | **Milestone** — phase + language, no second hard test |
| 10 | Meal plan settled | deciding what will be cooked → cooking it | going back means re-planning, a differently named activity, and usually re-shopping | Community Cook / Chef / Avatar leave; the cook is alone at the stove | *Recipes*, *Ingredients*, *substitution*, *Help response* stop; *step*, *catastrophe*, *rescue* start. **BO *Meal plan* minted** | to the guests: a date and a menu they are expecting | **hours to days** between settling and cooking | recipe ref, portions, substituted ingredients, guests, when — 5 fields; cooking never needs the search history, the rejected recipes or the help thread | **Divider** |
| 11 | Meal preparation started | same transition as 10, seen from the other side | — | — | — | — | — | — | **Contested** — the mirror of 10; see §8 |
| 19 | Meal prepared | cooking → being seen to have cooked | **you cannot un-cook a meal**; the compensation is ordering a takeaway | audience changes: from cook-alone to guests + the people who helped | *steps*, *catastrophe*, *rescue* stop; *Pictures*, *Help provider*, *Thanks* start | the guests eat it | minutes of pressure → an unhurried evening | the meal identity, the recipe ref, and who helped — the sharing side never needs step progress, catastrophes or substitutions | **Divider** |

Three dividers on 21 events is the middle of the expected band (16–30 events →
2–4). Nothing here is being chopped per noteworthy event.

---

## 4. The dividers

**After #1 — Cook registered.** The only unambiguous re-modelling of a noun on
the whole board: a *User* goes in, a *Cook* comes out, and the word *User* never
appears again. Convention: the event is produced upstream, so registration owns
its schema and the rest of the board consumes it.

**After #10 — Meal plan settled.** The strongest divider here. Four tests fire
and the interface is genuinely a plan, not a case file. It is also the cheapest
border on the board: the business already tolerates a gap of hours or days
between settling the plan and standing at the stove, so eventual consistency
across it costs nothing. Caveat in §7 — the artefact it mints is currently read
by nobody on the board.

**After #19 — Meal prepared.** Irreversibility at its most literal, plus a clean
vocabulary swap and a change of audience. Weaker than #10 on interface evidence
because *Meal prepared* mints no business object at all, and weaker on segment
size — only two events sit downstream of it. Accepted, with the reservation in
§7 and the coarser-cut option in §8.

---

## 5. Segments as candidate contexts

**Cook Membership** — event 1. Establishes who a cook is and keeps them being
that. Owns: *Cook*, *User*. Actors: User. Terms: *user*, *cook*, *registration*.
Formally the first segment, but it is really a **cross-cutting context** (§7):
it serves every other segment continuously and never hands the process on.

**Meal Planning** — events 2–10. Turns an intention to host into a plan somebody
could actually cook. Owns: *Menu*, *Guests*, *Meal plan*, *Ingredients*
(the substituted list). Reads but does not own: *Recipe*. Actors: Cook.
Terms: *guests*, *menu*, *recipe*, *ingredients*, *substitution*, *meal plan*,
*stalled*.

**Cooking Session** — events 11–19. Carries the cook through the plan in real
time and gets them out of trouble. Owns: **nothing on the board** — see §7,
this is the sharpest gap in the cut. Should own a *Cooking session* with a
current step, progress and incidents. Reads: *Recipe*, *Help response*.
Actors: Cook. Terms: *step*, *unclear*, *catastrophe*, *rescue*, *prepared*.

**Meal Recognition** — events 20–21. Turns a finished meal into something shown
and someone thanked. Owns: *Pictures*, *Thanks*. Reads: *Help provider*.
Actors: Cook. Terms: *pictures*, *thanks*, *help provider*.
Thin at two events — flagged in §8 as the likeliest thing to fold away.

Divider strip (the redraw spec):

| # | Event | Segment | Divider after? |
|---|-------|---------|----------------|
| 1 | Cook registered | Cook Membership | **yes** |
| 2–9 | … | Meal Planning | |
| 10 | Meal plan settled | Meal Planning | **yes** |
| 11–18 | … | Cooking Session | |
| 19 | Meal prepared | Cooking Session | **yes** |
| 20–21 | … | Meal Recognition | |

---

## 6. Border contracts

**Cook Membership → everything, on *Cook registered*.**
Crosses: cook id, display name, dietary defaults, kitchen/skill profile.
Stays upstream: credentials, verification, contact details, account history,
anything a registration flow accumulates.
Relationship: **open host service** — every segment consumes it, none negotiates
with it.
Consistency: seconds. A cook who just registered expects to plan immediately.

**Meal Planning → Cooking Session, on *Meal plan settled*.**
Crosses: recipe reference(s), portions/guest count, the ingredient list *after*
substitution, the occasion, the intended time.
Stays upstream: rejected recipes, the search history, guest preferences and the
reasoning behind the menu, the whole help thread that produced the substitution.
Relationship: **customer/supplier** — cooking negotiates back what a plan must
contain to be cookable (portions, timings), and planning owns the schema.
Consistency: hours. The gap between settling a plan and cooking it is the
domain's own slack.

**Cooking Session → Meal Recognition, on *Meal prepared*.**
Crosses: meal identity, recipe reference, who helped (help-provider refs),
finished-at.
Stays upstream: step-by-step progress, every *Step unclear*, the catastrophe and
the rescue, the pictures taken *during* trouble.
Relationship: **customer/supplier**, but see §7 — recognition and community help
may be the same context, in which case this is an internal boundary and the
divider should be reconsidered.
Consistency: minutes to hours. Nobody is upset if thanks lag the meal.

---

## 7. What the timeline cannot see

**Missing-consumer test, per divider — the most important result in this cut.**

- *Cook registered* mints **Cook**. Read by every actor sticky on the board.
  **Passes.**
- *Meal plan settled* mints **Meal plan**. **Nothing on the board reads it.**
  Every cooking event reads *Recipe*, not *Meal plan*; `Prepare meal` goes
  straight to the recipe. This is the orphaned-artefact signal, and it means one
  of three things: (a) the read model is simply undrawn and cooking really does
  start from the plan — most likely, and cheap to fix on the wall; (b) a
  **Shopping / Mise-en-place context is missing** between the two, which is where
  a meal plan is actually consumed; or (c) the meal plan is a report, in which
  case the divider is in the wrong place. **Do not build on divider 2 until the
  team says which.**
- *Meal prepared* mints **nothing** — no business object at all. The pictures
  come one command later. That is why divider 3 is the weakest of the three.

**Stretches with no aggregate.** Events 11–19 — the entire Cooking Session — has
**not one pale-yellow business object**. Only green *Recipe* and *Help response*
read models and four repetitions of the same `Prepare meal` command. Yet
*Step unclear*, *Step completed*, *Catastrophe happened* and *Meal rescued* all
presuppose something with state: which step am I on, what has gone wrong, what
is still outstanding. Read as **undiscovered, not absent** — and corroborated,
because that stretch produces *Pictures* (14) and *Help request* (15) which
another context demonstrably consumes. Something in there is real and unnamed.
Naming it *Cooking Session* is the single highest-value change to this board.

**Cross-cutting contexts** — no pivotal event of their own, visible in the
actors, systems and read models:

- **Community Help.** *Help requested* → *Help provided* is drawn **twice**,
  once in planning and once in cooking, with the same commands, the same
  *Help request* / *Help response* objects and overlapping responders (Community
  Cook, Chef, and the pink *Grandma Avatar*). The team has already discovered
  this context and drawn it as two points on a line. It is one context serving
  both segments. *Thanks given* (21) reads *Help provider* and almost certainly
  belongs to it as well.
- **Recipe Catalogue.** *Recipe* / *Recipes* appears in seven clusters spanning
  every segment, and **no event on the board ever creates, edits or retires a
  recipe.** Recipes arrive from somewhere off-board. Supplier relationship;
  conformist unless the team wants its own recipe model.
- **Cook Membership**, as above.
- **Media.** `Take pictures` appears twice for two unrelated reasons — evidence
  for a help request (14) and a trophy shot (20). Same word, two meanings; a
  language smell worth resolving before either use is built.
- **AI Assistance**, *conditional on the icon question*. If 🤖 means
  system-raised, then *Meal planning stalled* (6) and *Meal rescued* (18) are the
  same capability watching two different segments and intervening — plus the
  *Grandma Avatar* answering help requests in both. That is a cross-cutting
  assistance context, not a segment, and it has no pivotal event by construction.
- **Notification.** Not on the board, but *Help requested* only works if someone
  is told. Implied, unmodelled.

**Recurring contexts.** Community Help appears on both sides of divider 2 and
touches divider 3 through *Thanks given*. It is a supplier to all three segments,
not a fourth segment.

**Straddling aggregates.**

- ***Recipe*** — read at 4, 5, 9 (planning) and 11, 12, 13, 19 (cooking). Crosses
  divider 2. Resolved by placing it **outside both**, in Recipe Catalogue; each
  segment holds a reference plus whatever it copied at the border. If instead the
  team wants to own recipes, divider 2 gets expensive fast.
- ***Pictures*** — created at 14 (cooking, as evidence) and at 20 (recognition,
  as a result). Crosses divider 3. **Split it:** an attachment on the help
  request upstream, a distinct shared-meal picture downstream. One name for both
  will pull the two contexts back together.
- ***Help request* / *Help response*** — created and consumed on both sides of
  divider 2. Owned by Community Help; neither segment should hold their
  lifecycle.
- ***Menu*** — created at 2, read at 6, 7, 10, never mentioned after. Clean,
  stays in Meal Planning.

---

## 8. Contested calls & alternatives

**#10 *Meal plan settled* vs #11 *Meal preparation started*.** The same
transition seen from two sides; only one is the border. Chose **10**, by the
ownership convention: the pivotal event is produced upstream and consumed
downstream, and *Meal plan settled* is planning's own vocabulary while
*Meal preparation started* is cooking's. If the team finds that a cook routinely
skips planning and starts cooking from a bare recipe, 11 becomes the better
border — and the existence of a second entry path into cooking is itself the more
interesting finding.

**#2 *Dinner planned* — rejected.** Phase and language both fire (*Menu* is
minted, *Guests* appears), but nothing else does: the same cook keeps working,
a plan can be torn up freely, and there is no wait. A milestone that opens the
planning phase, not a border into it.

**#4 *Recipe selected* — rejected, twice over.** It sits inside the
search-and-select loop, and the narrow-interface test vetoes it anyway:
everything downstream — ingredients, substitutions, steps, the finished meal —
reads the whole *Recipe*. That is depth inside a context, not a border.

**#18 *Meal rescued* — rejected.** A branch outcome inside the cooking loop.
The only argument for it is the 🤖 marker, and the skill's rule is not to let an
unconfirmed marker carry a boundary.

**Coarser cut — drop divider 3.** Merge Meal Recognition back into the Cooking
Session. Cheap to do: the segment is only two events and *Meal prepared* mints
nothing. What is lost is the distinction between *cooking* and *being seen to
have cooked* — which is exactly where the reciprocity loop lives, since thanks
flow back to the people who helped. **If the community is the point of this
product, this is the wrong economy.** The honest alternative is not to merge it
into cooking but to move *Pictures taken* and *Thanks given* into **Community
Help**, which already owns *Help provider* — leaving a two-divider cut of
Planning / Cooking, with Membership, Recipe Catalogue and Community Help as
cross-cutting contexts around them.

**Finer cut — split Meal Planning at #4.** Recipe Discovery (2–4) and Meal
Composition (5–10). Rejected: same cook, same vocabulary, and composition reads
the recipe constantly. Revisit if discovery grows its own ranking, personalisation
or recommendation model — that is the point where it stops being a search box.

**What would settle it:**

1. **Who reads a settled meal plan, and what happens between settling it and
   standing at the stove?** Shopping? A calendar entry? Prep the night before?
   (Decides whether divider 2 is sound and whether a Shopping context is missing.)
2. **Can a cook prepare a meal without planning one — just pick a recipe and
   start?** (If yes, cooking has a second entry path and 11 beats 10.)
3. **What do ✨ and 🤖 mean on these stickies — the system raising the event, or
   an assistant proposing the sticky during the session?** (Decides whether AI
   Assistance is a domain capability or a provenance mark.)
4. **Is help during cooking the same thing as help during planning?** Same
   request type, same responders, same urgency? (Confirms Community Help as one
   context; if the cooking one is urgent and the planning one is not, they are
   two.)
5. **Can thanks be given to the Grandma Avatar, or only to human helpers?**
   (Decides whether the AI is an actor inside Community Help or an external
   system it wraps.)
6. **What is the cook holding while cooking?** A session? A checklist? Nothing?
   (Names the missing aggregate in 11–19.)

**Hotspots verbatim:** none — the board carries no red stickies. On a domain
whose events include *stalled*, *unclear* and *catastrophe*, that is worth a
minute in the next session.

---

## Reconciling with the other cut

This is the fast, timeline-first cut. It finds borders that run *across* the
flow and is blind by construction to contexts that run *underneath* it — which
is why Community Help, Recipe Catalogue, Membership and Media had to be
recovered in §7 rather than falling out of the scoring. The board's full grammar
(commands, aggregates, read models, policies) supports a second, independent cut
via `event-storming-context-finder`. Where the two agree, the evidence is strong;
where they disagree, divider 3 is the one to expect an argument about.