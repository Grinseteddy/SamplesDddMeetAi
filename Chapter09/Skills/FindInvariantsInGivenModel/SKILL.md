---
name: event-model-invariant-finder
description: >-
  Extract business rules (invariants) and Given/When/Then Gherkin from an
  Event Model (Dymitruk: a shared timeline in swimlanes, cut into State
  Change/State View/Automation/Translation slices, each with a happy-path
  Given/When/Then). Mines the guards that make a slice's command fail, the
  state machines the slice order implies, the policies an Automation slice
  already names, and the border contracts a Translation slice already draws
  — then writes the violation scenarios the happy path omits. Use whenever
  someone has an Event Model (from event-model-author, eventmodeling.org, or
  a photo of one) and wants its rules, invariants, guards, preconditions,
  state machines, or acceptance tests — "what would make this command fail",
  "derive the state machine for this slice", "write acceptance tests for
  this model". Trigger even without "invariant" or "Gherkin". Sibling of
  event-storming-invariant-finder (same categories, from a raw discovery
  board instead). Grounded in Dymitruk, Evans, Vernon.
author: Annegret Junker
---

# Event Model Invariant Finder

An **invariant** is a business rule that must be true before and after every
single command, with no window in which it is allowed to be false. It is not a
preference, not a validation, not a workflow step. It is the sentence that
makes the business say *no*.

An Event Model is a **specification**, not a discovery board: every slice
already carries one legal Given/When/Then, in order, on a shared timeline, cut
into swimlanes that are already the consistency boundaries. That is a much
better starting position than an EventStorming board, and it changes the job.
You are not hunting for structure — the structure is drawn. You are hunting
for the **negative space around it**: every other Given the When could need,
every rejection the model doesn't show, every state the slice sequence implies
but nobody named, and the cross-context rules that an Automation or
Translation slice has already half-specified and now needs finishing.

Three commitments carried over unchanged from the sibling method:

- **A rule with no rejection is not a rule.** If you cannot name what the
  business refuses, in its own words, you have written a description.
- **An invariant belongs to one aggregate in one swimlane.** If enforcing it
  needs data that never crossed a Translation slice into that swimlane, it is
  not an invariant there — it is a policy, or a missing border field. §6/§7.
- **Do not invent rules.** A field that never traces to a source event (the
  model's own information-completeness check) is evidence about what the
  model doesn't yet know, not a gap to paper over with a guess.

## What the model already gives you, and what it doesn't

This mapping *is* the method. Work down it slice by slice.

| Event Model evidence | Already given — don't re-derive it | Still has to be derived here |
|---|---|---|
| Swimlanes | Consistency boundaries. Skip the sibling skill's whole Step 2. | Whether a rule reaching across a lane is a real Translation or a smuggled shared kernel |
| A State Change slice's Given/When/Then | One legal transition, already ordered on the timeline | Every *other* legal transition; every guard that would make the When fail; the rejection |
| The slices' shared timeline order | The state machine's transition order, for free — no branch-tracing across a board | Which slices recur (loops) and which are genuinely one-off |
| A State View slice's `Shows` | The read model's shape — what a decision-maker can see before acting | The precondition that decision quietly assumes |
| An Automation slice's `Policy` line | A cross-boundary rule **already named**: *whenever `<event>`, `<command>` fires* | The staleness window, the compensating action, and whether it secretly belongs inside one aggregate after all (still run tests 1–2) |
| A Translation slice's `Crosses` line | A border, already drawn, with both contexts named | The full border contract: what stays upstream, the relationship, the consistency window |
| The information-completeness check (§5 of an Event Model) | Every field that failed to trace — i.e. every precondition the model cannot currently check | Whether that gap is a missing event, a missing Translation field, or an honestly unenforceable rule |
| §6/§7 of the Event Model (gaps, contested calls) | Missing endings, missing responders, invented placeholders already flagged | Turning each into the invariant or policy it blocks, not just repeating it |

## The five tests (unchanged from the sibling — apply per candidate rule)

1. **The always test.** Any moment, however brief, where the rule may be false
   and later repaired? → eventually consistent → §6 as a policy.
2. **The single-owner test.** Exactly one aggregate holds all the data needed
   to decide? Two aggregates in one rule means: merge them, move the rule, or
   accept a lag.
3. **The context test.** Is every noun owned by *this* swimlane, in *this*
   swimlane's meaning of the word? A rule using a field that never crossed a
   Translation slice fails this by construction — check the
   information-completeness table before writing the rule, not after.
4. **The rejection test.** Name the refusal, in the business's words. No
   refusal, no rule.
5. **The decision-data test.** Checked inside the transaction, or read from a
   projection (a State View) that may be stale? A guard on a read model is
   **advisory** — say so and name the real enforcement point.

Routing what fails is identical to the sibling skill: test 1/2 → §6 as a
policy; test 3 → §6 as a cross-context rule, or §7 as an unenforceable gap if
no context owns the missing field yet; test 4 → drop it, with the reason; test
5 → keep it, marked advisory, paired with a compensation.

## Where guards hide in an Event Model

Nine places, mapped to the model's own vocabulary rather than sticky colours:

1. **The Given a State Change slice doesn't show.** The specified Given is the
   *minimum* that makes the happy path true. Ask "what else would this
   command need to be true, or false, to be refused?" — this is the richest
   seam, exactly as it is on a raw board.
2. **The order of State Change/Automation events touching one aggregate.**
   Walk the timeline for every slice that reads or writes the same aggregate;
   the sequence is the state machine, already ordered — you are transcribing,
   not reconstructing.
3. **A rejection already drawn.** Some slices (see `@SLICE-07` in the worked
   example) already carry a second Given/When/Then ending in a rejection. That
   *is* an invariant — promote it into §4 with an id and a category, don't
   re-derive it from nothing.
4. **A State View's `Shows` line.** What a wireframe displays is what a later
   command's actor is trusted to have seen. If a decision downstream depends
   on it, the precondition is real even though no command reads it directly.
5. **An Automation slice's policy.** Already a *whenever X, then Y* rule.
   Formalize it in §6 with a staleness window and compensation; only keep it
   in §4 if it turns out (test 1/2) to be enforceable inside one aggregate
   after all.
6. **A Translation slice's crossing.** Already a border. Write the full
   contract — crosses / stays upstream / relationship / consistency window —
   the way a context-cut skill would, because a Translation slice **is** a
   context map edge that just hasn't been written up as one yet.
7. **Recurring slices.** A slice instantiated more than once on the timeline
   (the same pattern, different data) is a cardinality question: can it
   recur without limit, or is there a rule about how many, or how often?
8. **A produced event nothing downstream reads.** If no later slice's Given,
   command field, or read model ever consumes an event this model produces,
   that is the orphaned-artefact signal — same finding as a pivotal-event cut,
   arrived at from the completeness check instead of the timeline shape.
9. **A slice pattern the model implies but never draws.** No slice ends a
   lifecycle that clearly needs an ending (no `Abandon`, no `Return`, no
   failure exit) — an unowned rule and a missing command, together.

## Workflow

### Step 1 — Take the model as given

If the input is a finished Event Model (output of `event-model-author`, or
equivalent), work directly from its §2 swimlane strip and §3 slices — do not
re-derive contexts or re-cut slices. If the input is a raw photo or sketch of
an Event Model that hasn't been formalized, run it through `event-model-author`
first (or, for a light touch, normalize it yourself into the same swimlane
strip + slice table) before deriving rules on it; a rule sheet built on a
misread slice is expensive to unwind later.

**When the model arrives as more than one image.** A wide whiteboard or a
long digital canvas rarely fits one photo, and an Event Model is exactly the
artifact where that matters most — unlike an EventStorming board, which can
be read cluster by cluster, an Event Model's whole point is **one shared,
chronologically ordered spine**, so getting the images in the right order and
correctly joined is load-bearing, not cosmetic. Before deriving anything:

1. **Get the images in timeline order**, left to right, and say so. If the
   person didn't upload them in order, ask which comes first rather than
   guessing from content.
2. **Confirm the swimlanes recur identically across every image** — same
   names, same top-to-bottom order. If a lane label is cut off or ambiguous
   in one image, resolve it from an image where it's legible rather than
   inferring it from the events sitting in that row.
3. **Classify every seam between adjacent images as overlap, abutment, or
   gap**, explicitly: does the last column of image *N* repeat as the first
   column of image *N+1* (overlap — dedupe it), does *N* end mid-lane and
   *N+1* pick up cleanly (abutment — nothing lost), or is there daylight
   between them (gap — a slice this capture never shows, not a slice with no
   rules)? State which applies at each seam; don't assume abutment by default.
4. **Treat any line that runs off the edge of one image as unresolved, not as
   continuing.** This is the single highest-risk failure mode here. Even
   within one continuous image, tracing a single fan-out arrow through
   several swimlanes can take several rounds of close inspection to get
   right — across genuinely separate uploads there is no pixel continuity to
   check, so a line leaving frame at the right edge of image *N* is a §7 open
   question ("does the connector leaving column X continue to Y in the next
   image, or does it end there?"), never a guess at the nearest plausible
   target in image *N+1*.
5. **Build one stitched swimlane strip and slice table before deriving
   anything**, and mark each row with the image it came from. A misread
   traced to "screen 3, third column" is cheap to fix; the same misread
   buried in an already-merged table is not.
6. **Watch for the same sticky appearing near a seam in two images** — a
   normal artifact of deliberately overlapping photos. Match on sticky text
   and position before treating it as two events; double-counting a slice
   because two photos both caught it inflates §4 with rules for something
   that only happened once.

Show the stitched result and get it confirmed before moving to Step 2 — the
same discipline the sibling skill applies to any busy board, just with an
extra failure mode (the seam) that a single-image board never has.

### Step 2 — Confirm the consistency boundaries

State them, don't re-derive them: one line per swimlane, its aggregates (the
pale/produced nouns in each slice's Then), and its commands. An aggregate that
only appears in a State View's Given, never in a Then, is a read side with no
commands of its own — note it as such rather than skipping it.

### Step 3 — Build the state machines directly from the timeline

For each aggregate that appears in more than one slice, walk every slice that
touches it **in timeline order** and write the transition table and a Mermaid
`stateDiagram-v2` — labelled `Command / Event`, exactly as the sibling skill
does. This step is mechanically easier here than on a board: the order is
already given. What still needs judgement:

- **Which state a slice's Then actually leaves behind**, when the model
  doesn't spell one out — infer the smallest state that makes the next slice's
  Given make sense, and say you inferred it.
- **Recurring slices** (guard #7 above) as loops in the diagram, not repeated
  linear states.
- **Absorbing states.** A state nothing in the model transitions out of is
  terminal — say what the business does instead, or flag the missing exit.

### Step 4 — Derive the rules, guard by guard

Work the nine hiding places per aggregate. For each candidate, run the five
tests, then write it up in the same card the sibling skill uses:

```
INV-PREP-01 · A rescue needs a response
Confirm rescue is accepted only for a preparation In catastrophe, and only
when a Help response has arrived for it.
  Kind        state guard + precondition   Aggregate  Meal preparation
  Slice       @SLICE-07
  Triggered   Confirm rescue
  Rejection   "no response has arrived yet"
  Evidence    the slice's own second Given/When/Then already ends here
  Confidence  on the model
```

Mark confidence exactly as the sibling skill does: `on the model` (the slice's
own Given, Then, or drawn rejection), `implied` (the model makes no sense
otherwise), `inferred` (domain knowledge, needs confirming — and check whether
the information-completeness table already flags the field you're relying on
as untraced, which caps this at `inferred` even if it feels obvious).

### Step 5 — Write the Gherkin, in the model's own style

Match the format `event-model-author` already uses for slices — `Scenario:`
plus `Given` / `When` / `Then`, tagged `@SLICE-NN` — rather than switching to a
more elaborate classic-Gherkin style. Two cases:

- **A rejection scenario is already drawn on the slice.** Reuse it verbatim
  under the invariant's own id (`@INV-PREP-01`, referencing `@SLICE-07`)
  instead of writing a new one — the model already did this work.
- **No rejection is drawn.** Write one: same conventions as the sibling skill
  (one aggregate's data per `Given`, a **command** for `When`, a rejection plus
  the state that did *not* change for `Then`), tagged with the new invariant's
  id and a reference to the slice it guards.

Every invariant gets at least one violation scenario. Add a happy-path
scenario only where it clarifies a guard the slice's own Then doesn't show.

### Step 6 — Formalize what an Automation or Translation slice already named

Non-negotiable, same as the sibling's Step 6, but half the work is already
done:

- **Automation slice → policy.** Write it as: the rule in the business's own
  words (the model's `Policy` line) · which test routed it here (almost always
  test 1) · the policy form, already given · the local data it acts on · the
  staleness window (the model's own number if given, else `<n>` — check
  whether the model already flagged it as invented) · the compensating action
  (ask if the model doesn't say).
- **Translation slice → border contract.** Crosses (the slice's own `Crosses`
  line) · stays upstream (everything else the source aggregate holds) ·
  relationship (customer/supplier, conformist, ACL — infer from whether the
  downstream side negotiates the payload) · consistency window (the gap the
  slices either side tolerate).
- **A cross-context rule with no slice at all.** The model implies it but
  never draws it as either pattern — write it in §6 exactly as an `X-##` on
  the sibling skill, and flag in §7 that no slice specifies its enforcement.

### Step 7 — Cross-check against the completeness table

If the model includes an information-completeness check (§5 of its own
output), read every row that says a field doesn't trace. Each one is either:
(a) evidence that an invariant you were about to write in §4 is currently
**unenforceable** — move it to §7, not §4, with the missing field named; or
(b) evidence that a not-invariant in §6 needs a wider border — say which field
a Translation slice would need to add. Do not silently write a rule the model
has no way to check.

### Step 8 — Say what the model cannot tell you

Same discipline as the sibling skill, reusing the model's own §6/§7 where it
already flagged the finding rather than restating it as new:

- **Missing states / missing endings** — a lifecycle with no exit.
- **Missing slices** — a command the guards imply but no slice specifies.
- **Unowned rules** — a rule with no aggregate able to enforce it; say whether
  it's a missing aggregate or a wrong swimlane.
- **Numbers nobody supplied** — every `<n>` (thresholds, timeouts, limits),
  collected once. Cross-check against the model's own §6 for ones it already
  admitted to inventing.
- **The questions that would settle it** — concrete, one sentence, answerable
  by one domain expert.

## Output: the Invariant & Gherkin Sheet

```
# Invariants & Gherkin — <Event Model name>

## 1. Model as read
Swimlane strip and slice list, taken from the Event Model (or newly
normalized). Assumptions about inferred states flagged.

## 2. Consistency boundaries
Swimlane / aggregate / commands / states / data — restated from the model,
not re-derived.

## 3. Status models
Per aggregate touched by more than one slice: transition table, Mermaid
stateDiagram-v2, and the guard / terminal / no-backward invariants it asserts.

## 4. Invariants by swimlane and aggregate
Per swimlane, per aggregate: id · statement · kind · slice reference ·
triggering command · rejection · evidence · confidence.

## 5. Gherkin scenarios
Given/When/Then per invariant, in the model's own @SLICE-NN style — reusing a
drawn rejection where one exists, writing a new one where it doesn't.

## 6. Not invariants
Automation slices formalized as policies (staleness + compensation);
Translation slices formalized as border contracts; undrawn cross-context
rules as X-## entries; advisory guards; dropped candidates with the test that
killed them.

## 7. Gaps, hotspots and open questions
Fields that don't trace and the invariant they block; missing endings and
slices; unowned rules; unsupplied numbers; the questions that would settle
each — cross-referenced against the model's own §6/§7 rather than repeated
blind.
```

Adapt depth to the ask: "just the state machines" gets §3 and the guard rules
inside it; "just the acceptance tests" gets §5 built straight off §4 without
much prose. Never drop §6 — a sheet that silently omits the demotions reads as
a set of enforceable constraints when it is not, and an Automation slice
already looks enforceable if you don't say otherwise.

## Traps worth checking before you publish

- **Treating the drawn Given as the whole precondition.** It's the happy
  path's Given, not an exhaustive one. Ask what else the When could need.
- **Confusing a State View's `Shows` with an enforced guard.** Displaying
  something doesn't check it; find where, if anywhere, it's actually gated.
- **Re-deriving an Automation slice as if it were undiscovered.** It's already
  a named policy — your job is to finish it (window, compensation), not to
  rediscover that it exists.
- **Writing a rule the completeness table already says can't be checked.**
  Check §7 of the source model before every §4 entry, not after.
- **The cross-context invariant.** Same failure mode as the sibling skill:
  writing a rule that needs data from another swimlane as if it were local.
  It's a Translation border contract or a policy, not an invariant.
- **Tidying the model's own words.** Keep event, command and field names
  exactly as the model states them.
- **The invented threshold.** `<n>` is honest. A number the source model
  itself flagged as a placeholder should stay flagged, not get quietly
  promoted to a real default.
- **The stitched seam.** Picking the nearest plausible target for a line that
  runs off the edge of one image, instead of flagging it in §7. A wrong guess
  here doesn't just miss one guard — it can wire an event to the wrong
  swimlane for every rule downstream of it.
- **The double-counted overlap.** Two photos deliberately shot with overlap
  producing the same slice twice in the stitched table, which then produces
  two invariants for one rule. Dedupe on sticky text and position before
  Step 2, not after.
- **One guard per slice.** A State Change slice with exactly one Given/When/
  Then usually has more guards hiding behind it — ask "what else would make
  this fail" at least twice more before moving on.

## Working with neighbouring skills

- **No Event Model exists yet** → `event-model-author` builds one from a
  Domain Story and a given set of Bounded Contexts; this skill mines what it
  produces.
- **Only a raw EventStorming board, no specified slices** →
  `event-storming-invariant-finder` — same categories and tests, adapted to a
  discovery-phase board instead of a converged model. Run this skill instead
  once the board has been turned into an Event Model, for the sharper,
  timeline-ordered result.
- **No bounded contexts / swimlanes given yet** →
  `domain-story-context-finder` or `pivotal-event-boundary-finder` first;
  rules are only as trustworthy as the cut they're scoped to, and this skill
  does not re-cut swimlanes.
- **The rejections need to become an API contract** → `openapi-spec-author`
  for a command API, `asyncapi-spec-author` for the event stream,
  `protobuf-model-author` for the wire format — §4's rejections become error
  responses, §3's guards become request validation.
- **Checking an existing schema against these rules** →
  `schema-glossary-consistency`.
- **The model itself looks shaky** — an Automation slice hiding a manual step,
  a Translation slice with a too-narrow payload — flag it and point at
  `event-model-author`'s own §7 contested-calls process before deriving rules
  on top of it.

## References

A. Dymitruk, *Event Modeling*, eventmodeling.org — the four-pattern technique
this skill mines: State Change, State View, Automation and Translation, and
the Given/When/Then specification convention its slices already follow.

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003.

V. Vernon, *Implementing Domain-Driven Design.* Boston, MA, USA:
Addison-Wesley, 2013. — "Effective Aggregate Design": model true invariants in
consistency boundaries; use eventual consistency (here: Automation and
Translation slices) outside them.