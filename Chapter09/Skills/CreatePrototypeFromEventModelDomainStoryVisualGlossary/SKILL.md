---
name: prototype-from-event-model-and-domain-story
description: >-
  Build a clickable web-app prototype from an Event Model (Dymitruk:
  swimlaned timeline cut into State Change/State View/Automation/Translation
  slices — may arrive as several images needing stitching) plus a required
  Visual Glossary (term spellings, entity/value-object/aggregate shape,
  cardinalities) and an optional Domain Story whose lanes should match the
  Event Model's swimlanes. Use whenever someone has an Event Model — from
  event-model-author, eventmodeling.org, or a photo of a board — and wants
  it turned into a clickable, navigable prototype or app. The Visual
  Glossary is required: ask for it if missing, and offer to derive a
  provisional one from the model's own fields if none exists. Ask for a
  Domain Story too; if still absent, build from the Event Model and
  Glossary alone. If Bounded Contexts from the glossary or story don't
  match the Event Model's grid, ask whether to reconcile them. A screenshot
  is optional: ask for one, and absent that, pick a coloring matching the
  Event Model's own topic.
author: Annegret Junker
---

# Prototype from an Event Model + Visual Glossary (+ Domain Story)

Produce a **clickable web-app prototype** from up to four inputs, each
answering a different question:

| Input | Answers | Required? |
|---|---|---|
| **Event Model** (1+ images, or an already-transcribed model) | *What screens, flows and state already exist?* — it is already the spec | **yes** |
| **Visual Glossary** | *What are things called, and what shape are they?* — exact terms, entity vs value object vs aggregate, cardinalities | **yes** |
| **Domain Story** (1+) | *Do the bounded contexts check out? Who are the actors, and what's a real scenario?* | ask; if still absent, build from the Event Model + Glossary alone |
| **Screenshot / mockup** | *What does it look like?* | ask; if still absent, choose a theme from the model's own topic |

## The one idea that makes this work: three authorities, one converged spec

A Domain Story is raw material you interpret into structure. An Event Model
is not — it **is** the structure already. Dymitruk's method converges rather
than discovers: swimlanes are already the consistency boundaries, slices are
already the screens in navigation order, and each slice's Given/When/Then is
close to an acceptance test. Building a prototype from it is closer to
compiling than inventing. **The Event Model is the behavior/structure
authority** — don't re-derive a screen or a flow it already draws.

An Event Model is excellent at verbs and weak at nouns: it tells you an event
happened without ever pinning down what the thing it happened to *is*, what
it must contain, or how many of it may exist. That's what the **Visual
Glossary supplies — the vocabulary and data-shape authority**: exact term
spellings, whether something is a whole entity or a mere attribute, and every
cardinality as a business rule. It's also what makes the model's own
information-completeness check actually checkable — a field only traces back
to a source event if both call it the same thing, and the glossary is what
tells you whether they do. **When the model and the glossary name the same
thing differently, the glossary's spelling wins** in every visible label; the
model still owns which screens and transitions exist.

What a Domain Story adds, when one is given, is narrower and specific: a
**check** on the Bounded Contexts (its grouped lanes should draw the same
lines as the Event Model's swimlanes — and the glossary's, if it groups
terms too), real **actors and roles**, and a **concrete scenario** — real
seed data walking the whole happy path instead of fabricated placeholders.

Three lanes, three authorities, and none of them invents another's content.
When you catch yourself adding a screen the model has no slice for, renaming
something the glossary didn't touch, or overriding swimlanes with a fresh cut
nobody asked for — stop, that's a lane violation.

## Step 0 — Input inventory, re-checked every turn

Hold four slots. The **Event Model and the Visual Glossary cannot be
declined** — both must reach **have it** before you build; the **Domain
Story and the screenshot** can be **have it**, **missing**, or **declined**
(the user said to proceed without it). Re-run this checkpoint before every
response where new material arrived, and before Step 1.

> Here's where I am:
> - **Event Model** — ✅ got it (n images) / ❓ still missing
> - **Visual Glossary** — ✅ got it / ❓ still missing
> - **Domain Story** — ✅ got it / ❓ still missing
> - **Formatting example** — ✅ got it / ❓ still missing
>
> The **glossary** is what settles exact terms, field shapes and validation
> rules — without one I can't responsibly build forms, so I do need it, but
> if you don't have one to upload I can sketch a provisional one straight
> from the fields already named in the model and get you to confirm it.
> The **domain story** lets me check that its bounded contexts line up with
> the model's, and gives me real actors and a real scenario instead of
> inferred ones — optional. The **formatting example** (a screenshot of a UI
> whose look you want matched) fixes the visual identity — also optional;
> without it I'll pick a theme that fits the model's own topic.

Respect the answer on the two optional slots: never ask twice in a row for
the same missing one — one reminder is a checkpoint, a second is nagging. If
the user moves on without answering after one reminder, treat that slot as
declined and build, flagging what that costs in the readme. The glossary
slot works differently: if the user tries to wave it off, don't drop the
requirement — offer the provisional-derivation path from the message above
as the fast alternative, since the glossary is what keeps every label and
every cardinality in the build honest. A derived-and-confirmed glossary
counts as "provided"; skipping it entirely does not.

| You have | You do | You must flag |
|---|---|---|
| Event Model + Glossary only | Screens and flows straight from the model's wireframes; labels, field shapes and validation from the glossary; actors inferred from any actor named on a command, else a generic persona per swimlane; fabricated seed data that exercises every state | every inferred actor, every fabricated seed instance, and that there's no independently-sourced scenario |
| + Domain Story, contexts correspond | All of the above, replaced by real actors/roles and a real seed scenario | nothing extra — this is the full-confidence case |
| + Domain Story, contexts undrawn or mismatched | Same, once Step 4 below resolves how to assume or reconcile them | the reconciliation itself, prominently — it's a domain finding, not a footnote |
| + screenshot | Visual identity is measured from pixels | — |
| no screenshot | Visual identity is authored from the model's vocabulary | that the identity is chosen, not extracted, and is the cheapest thing in the prototype to change |

## Step 1 — Take (and if needed stitch) the Event Model as given

**If the input is a raw sketch that hasn't been formalized into swimlanes and
slices yet**, normalize it first — either point at `event-model-author` if
the gap is large (no bounded contexts given, no clear State/View/Automation/
Translation cut), or do a light normalization yourself for a lighter gap.
Building screens on top of a shaky read compounds every mistake downstream.

**When the model arrives as more than one image** — common, since an Event
Model's whole point is one shared, chronologically ordered spine across a
canvas too wide for one photo — stitching correctly is load-bearing, not
cosmetic:

1. **Get the images in timeline order**, left to right, and say so; ask
   rather than guess if the order isn't obvious from what was uploaded.
2. **Confirm the swimlanes recur identically across every image** — same
   names, same top-to-bottom order. Resolve a cut-off or ambiguous label from
   an image where it's legible, never from the events sitting in that row.
3. **Classify every seam** between adjacent images as **overlap** (the last
   column of image *N* repeats as the first of *N+1* — dedupe it),
   **abutment** (*N* ends mid-lane and *N+1* picks up cleanly — nothing
   lost), or **gap** (daylight between them — a slice this capture never
   shows, not a slice with no rules). State which applies at each seam.
4. **Treat any line running off the edge of one image as unresolved**, never
   as continuing. A connector leaving frame at the right edge of image *N* is
   an open question ("does this continue to Y in the next image, or end
   there?"), never a guess at the nearest plausible target.
5. **Build one stitched swimlane strip and slice table before building
   anything**, marking which image each row came from.
6. **Watch for the same sticky appearing near a seam in two images** — match
   on content and position before treating it as two slices; a deliberately
   overlapping photo pair should not double a screen in the prototype.

Show the stitched result and get it confirmed before Step 2 for anything
non-trivial — a misread here is cheap to fix now and expensive once it's
code.

From the (possibly stitched) model, extract: the **swimlane strip** (context
names, in order — these are candidate Bounded Contexts, subject to Step 4);
the **slice list** per swimlane in timeline order, each tagged with its
pattern (State Change / State View / Automation / Translation); each slice's
**wireframe field list**, its command/event/read-model names, and its
Given/When/Then (or Given/Shows); and, if the model already carries them, its
own **information-completeness check** and **gaps/contested-calls** sections
— reuse these directly rather than re-deriving them.

## Step 2 — Interpret the Visual Glossary (required)

Consult the **visual-glossary-interpreter** skill
(`/mnt/skills/user/visual-glossary-interpreter/SKILL.md`). Its Step 0 asks
which dynamic artifact the glossary accompanies — tell it this Event Model.
Produce the **Glossary Brief**; you need especially: the term catalogue
(§2), the relationship table with cardinalities (§3), the derived domain
model — entities, value objects, aggregates, identity (§4), bounded
contexts / grouping (§6), and open questions (§7). Confirm the transcription
for anything non-trivial, same rule as Step 1.

**If the glossary has no §6 grouping** ("none shown" is a legitimate answer
from that skill), that's fine — there's simply nothing on the glossary side
to reconcile in Step 4; move on.

**If there is genuinely no glossary to upload**, offer the fallback rather
than blocking indefinitely: derive a provisional one straight from the
fields already named across the Event Model's slices — every command field,
event field and read-model `Shows` field becomes a candidate term, with a
best-guess relationship and cardinality sketch built the way
`visual-glossary-interpreter` describes deriving a glossary backwards from a
board. Mark every cardinality `(inferred)` rather than `(given)`, show the
draft, and get it confirmed before treating it as settled — a confirmed
derived glossary satisfies the requirement; an unconfirmed guess does not.

## Step 3 — Interpret the Domain Story (if provided)

Consult the **domain-story-interpreter** skill
(`/mnt/skills/user/domain-story-interpreter/SKILL.md`) and produce the
**Prototype Brief**. You specifically need: actors & roles (§2), modules /
bounded contexts (§3), domain model (§4), use cases & user journey (§6), and
open questions (§8). If the story is a non-trivial image, confirm your
transcription before continuing.

If more than one Domain Story is supplied, interpret each and merge — flag
where two stories disagree about an actor, a state, or a bounded context
rather than silently picking one.

## Step 4 — Reconcile vocabulary and Bounded Contexts

This is the step this skill exists to do well. Two passes; run vocabulary
first, since a mislabeled context is often just an unreconciled term.

### 4a. Term fidelity

For every event, command, aggregate and read-model name in the Event Model
(and every work object in the Domain Story, if present), find its Visual
Glossary term:

- **Where they differ** (`Bike` vs `Bicycle`, `Help provider` vs
  `Responder`), **the glossary wins** — in UI labels, headings, entity
  names, and identifiers — and the rename goes in the readme.
- **Where the model or the story names something no glossary term covers**,
  that's undefined vocabulary: keep the source's name and list it as an open
  question rather than inventing a glossary entry for it.
- **Where a glossary term nothing in the model or the story ever touches**,
  that's unexercised structure: it may become a field on an entity a screen
  already shows, but **do not invent a screen for it** — no slice justifies
  one.
- **Where a slice implies a cardinality the glossary states differently**
  (a wireframe shows one response field; the glossary asserts `1..*` Help
  response) — **build to the glossary's rule**, since it's the data-shape
  authority, and make the conflict a prominent open question rather than
  quietly relaxing the constraint either way.

### 4b. Bounded Context reconciliation

Compare up to two secondary sources — the **Visual Glossary's §6 grouping**
(if it drew one) and the **Domain Story's §3 modules** (if a story was
given) — against the **Event Model's swimlane strip** from Step 1. Handle
both together in one pass, and ask one consolidated question, rather than
interrupting the user twice in the same turn.

**A secondary source has no grouping drawn** — ask, don't assume silently:

> `<Source>` doesn't have bounded-context groups drawn. I can (a) assume the
> Event Model's own swimlanes for it — fast, and keeps everything consistent
> with the model — or (b) run `domain-story-context-finder` (or the
> equivalent independent cut) first and then compare. Which do you want?

Default recommendation, if asked, is (a): the Event Model has already
converged with a spec, and a fresh independent cut risks disagreeing with it
for no reason a reviewer would find interesting.

**A secondary source has a grouping that doesn't correspond** — different
count, different names with no obvious mapping, a swimlane it never touches,
or one of its groups whose contents split across two swimlanes — name the
mismatch precisely (which lanes, which terms or sentences) and ask:

> `<Source>`'s contexts don't line up with the Event Model's swimlanes:
> `<specifics>`. Want me to match them — treating the Event Model's grid as
> the boundary and folding the mismatched grouping into it — or keep both
> and flag every place they disagree?

**They already correspond** — proceed, and say so in one line.

If both the glossary and the story are present and disagree with the model
in different ways (or with each other), say precisely which agrees and which
doesn't — e.g. "the glossary's grouping matches the model; the domain
story's doesn't" — in the same consolidated question.

Whichever way it resolves, write a short **reconciliation table** (Event
Model swimlane ↔ Glossary grouping ↔ Domain Story module ↔ verdict) and
carry it into the final readme. A mismatch nobody's noticed yet is exactly
the kind of thing this project's other skills treat as a finding, not an
error to hide.

If the Event Model itself has no swimlane grid at all (a flat, single-lane
timeline), there is nothing to reconcile — say so and treat the whole model
as one module.

## Step 5 — Extract or choose the visual style

**With a screenshot**, consult the **webapp-style-extractor** skill
(`/mnt/skills/user/webapp-style-extractor/SKILL.md`). You want its JSON:
`colors`, `typography`, `spacing`, `components` — sampled from pixels, never
eyeballed.

**With no screenshot**, don't reach for a generic identity. Read the Event
Model's own vocabulary — the nouns in its events, commands, aggregates and
read models, as spelled by the glossary — and let the domain's own topic
pick the mood: a catastrophe/rescue/help vocabulary suggests something warm
with an emphatic "help" accent; a lending/reading vocabulary suggests
something calmer and literary; a payments/ledger vocabulary suggests
something restrained and precise. Use the **frontend-design** skill for
craft — accessible contrast, a real type scale, restrained motion — but let
the topic choose the palette role, not a template default. State the
one-sentence reasoning in the readme so the choice is checkable, not just
asserted.

## Step 6 — Build plan (the mapping table)

Write this table out before building; it's the spec the build follows.

| Source | Element | Becomes in the prototype |
|---|---|---|
| Swimlane strip (Step 1, reconciled in Step 4b) | Bounded contexts | Top-level module tabs / nav sections |
| Slice list, in order | Slices | Screens/routes, in slice order within each module |
| State Change slice | wireframe + command + Given/When/Then | A form; submitting fires the event(s) in `Then` and updates in-memory state; a stated rejection in the slice's own second Given/When/Then is wired as a real refusal |
| State View slice | wireframe + `Shows` | A list/detail/dashboard view computed from exactly the `Given` events, showing exactly the `Shows` fields |
| Automation slice | `Policy` line | **Not** a control a person clicks — a clearly-labelled "system" trigger (a small simulate-policy affordance, or an auto-fire after a short delay) so a reviewer can see it happen without it masquerading as a user action |
| Translation slice | `Crosses` line | Data flowing between modules automatically once the source event fires — a value appearing in the target module's screen, never an action the user takes |
| Completeness check, if the model has one | untraced fields | Cross-checked against the glossary first (Step 4a) — a field with no glossary term is a naming gap, already caught; a field with a glossary term that still never traces to a source event is a genuine gap, rendered as a disabled/placeholder field and flagged |
| Model's own gaps/open questions, if present | — | Carried into the readme verbatim |
| Glossary §2 | term spellings | Every visible label, heading and identifier |
| Glossary §4 | entities / value objects / aggregates | Records with their own screens vs. fields on a parent form vs. the unit a screen saves |
| Glossary §3 | cardinalities | Required/optional fields, repeaters with min/max, pickers vs. subforms |
| Glossary §4 | identity terms | Read-only keys, shown but not edited after create |
| Glossary §7 | open questions | Merged into the readme |
| Domain Story §2 | actors & roles | Role gating + a role switcher |
| Domain Story §6 | concrete scenario | Seed data — at least one instance walking the whole happy path |
| Domain Story §8 | open questions | Merged into the readme, not silently resolved |
| Style spec or chosen theme | tokens + components | CSS variable layer + component library |

The model wins on structure, the glossary wins on vocabulary and data shape,
and the story — when present — only ever supplies actor identity and seed
data, never a competing layout or a competing field shape.

## Step 7 — Build the clickable prototype

Build a **single, self-contained app** (a React artifact or a standalone HTML
file — whichever renders inline in this environment) with:

- **A token layer first.** Colors, named type styles, spacing scale as CSS
  variables. No stray hex or px in the markup.
- **Reusable components** built to the extracted or authored spec — Button,
  Card, Badge, Input, NavItem.
- **A navigable spine organized by module.** One tab or nav section per
  reconciled bounded context, each showing its slices in timeline order.
- **A live state machine per aggregate.** For every aggregate touched by more
  than one slice, walk every slice that touches it in timeline order to get
  its transition table (the order is already given by the model — you're
  transcribing, not reconstructing), then wire each transition's control to
  actually update state and re-render, badge and available actions included.
  This is what separates a prototype from a picture.
- **Glossary-true forms.** Field labels use glossary spellings;
  required/optional and min/max follow the cardinalities; value objects are
  fields, not screens; entities at the far end of a relationship are
  pickers, not nested forms.
- **Automations and translations visibly distinct from user actions**, per
  Step 6 — never disguise a policy as a button.
- **Seed data that exercises the bounds.** The Domain Story's concrete
  scenario if you have one, plus at least one instance sitting at a
  cardinality edge (the maximum allowed repeats, one with zero optional
  parts) so the glossary's constraints are visible, not theoretical; where
  there's no story, fabricate the rest and flag it as invented.
- **A role switcher**, since a prototype has no real auth — labelled clearly
  as a prototype affordance.

Environment constraints: all state **in memory** — no `localStorage` /
`sessionStorage` (they fail in claude.ai artifacts) — mock data, no backend.

## Step 8 — Validate and deliver

Before handing over, check:

- Every slice is reachable; no dead links.
- Every State Change's transition is wired, including any drawn rejection.
- Every State View is computed from its own `Given`, showing exactly its
  `Shows` fields.
- Every Automation and Translation is visibly distinct from something a
  person clicks.
- Every visible noun matches its glossary spelling — grep the source for the
  model-only or story-only synonyms renamed in Step 4a; leftovers are the
  most common defect.
- Every validation rule traces to a glossary cardinality, or is absent
  deliberately.
- No screen exists that no slice justifies, and no screen exists that only a
  glossary term justifies.

Save to `/mnt/user-data/outputs/`, present it, and include a short
**readme** with: (a) a traceability table — each screen/field back to its
slice, glossary term, or story sentence; (b) the Step 4 **vocabulary
renames** and **bounded-context reconciliation**, both kept visible even
when they resolved cleanly; (c) style caveats — measured (from `meta.notes`)
or authored (the one-sentence theme reasoning); and (d) the merged open
questions from the Event Model, the Glossary, and the Domain Story. Keep it
tight — the prototype is the deliverable.

## Worked sketch (illustrative)

Inputs: a two-swimlane Event Model (`Meal Planning` / `Cook Assistance`) with
a State Change slice `Request help` (wireframe: recipe ref, what went wrong →
command → event `Help requested`), a State View slice showing the response, and
an Automation slice whose policy reads *whenever a request sits open past
`<n>` minutes, the avatar answers*; a Visual Glossary asserting
`Help request —has→ 1..* Help response` and spelling the responder term
`Responder` rather than the model's `Help provider`; and a domain story that
groups the same scenario into `Cooking Help` only, never separating planning
from cooking.

- Step 1 → swimlane strip `Meal Planning`, `Cook Assistance`; three slices in
  order.
- Step 2 → glossary confirms `1..*` responses per request and the `Responder`
  spelling; no §6 grouping drawn.
- Step 4a → every "Help provider" label in the model becomes "Responder" in
  the build; flagged as a rename.
- Step 4b → mismatch: the story's one `Cooking Help` lane covers sentences the
  model splits across both swimlanes; the glossary drew no grouping at all.
  Asked one consolidated question; team said match the story to the model's
  grid — the story's lane is folded into both, and the reconciliation table
  records it.
- Step 5 → no screenshot; the model's own vocabulary (help, rescue, stranger)
  suggests a warm palette with one emphatic "you're not alone" accent color
  on the response card.
- Step 7 → the request screen shows a repeater of Responder cards (respecting
  `1..*`), the Automation slice renders as a small
  "⏱ simulate: avatar answers after timeout" control in a dev corner, and
  seed data includes one request with several responses to exercise the
  unbounded upper end.

## Common pitfalls

- **Building without the glossary in hand.** Even a derived, unconfirmed
  guess at field shapes is not the same as a confirmed one — get the
  provisional glossary confirmed before treating any cardinality as real.
- **Treating a supplied Domain Story as permission to start** before the
  Step 4 reconciliation happened.
- **Re-deriving the model's own screens from scratch** instead of building
  directly from its wireframe field lists.
- **A screen per glossary term.** The glossary is nouns, not navigation —
  only a slice justifies a screen.
- **Value objects promoted to CRUD.** A glossary leaf at `1` or `0..1` is a
  field on a parent form, not its own list-and-detail screen.
- **Letting model or story vocabulary leak into the UI** after the glossary
  settled the name — grep for it.
- **Baking an arbitrary-looking cardinality bound as a hard rule** without
  flagging that it might be a remembered screen limit rather than a business
  rule.
- **Letting an Automation or Translation slice masquerade as a button** a
  person clicks — that erases the exact distinction the model draws.
- **Silently picking a side** when the model, the glossary, or the story
  disagree about a bounded context, instead of asking and then recording it.
- **Defaulting to a generic identity** when no screenshot exists, instead of
  reading it off the model's own (glossary-spelled) vocabulary.
- **A pretty but dead mock.** If the transition controls don't change state,
  it isn't a clickable prototype.

## Working with neighbouring skills

- **No Event Model yet** → `event-model-author` builds one from a Domain
  Story and a given set of Bounded Contexts; hand its output straight into
  Step 1 here.
- **Only a raw EventStorming board, no specified slices** →
  `event-storming-interpreter` first, then `event-model-author`, then this
  skill.
- **No Visual Glossary at all, and nothing to sketch** → Step 2's fallback
  derives a provisional one straight from the model's own fields; get it
  confirmed rather than treating it as settled.
- **No Bounded Contexts anywhere yet** (neither the model, the glossary, nor
  the story has them) → `domain-story-context-finder` proposes a cut from
  the story; use its output the way the Event Model's swimlanes were meant
  to be used, per Step 4b's option (b).
- **No Domain Story either, and the user wants one** → `domain-story-seeder`
  drafts a strawman from a rough description; hand it into Step 3 here.
- **The rules the built commands should enforce, beyond the glossary's own
  cardinalities** → `event-model-invariant-finder` derives the guards and
  rejections; wire its output into the Step 7 forms as the visible refusals,
  rather than inventing generic validation.
- **The model itself looks shaky** (an Automation slice hiding a manual step,
  a Translation slice with a too-narrow payload) → say so briefly and point
  at `event-model-author`'s own gaps/contested-calls process before building
  a prototype on top of it.