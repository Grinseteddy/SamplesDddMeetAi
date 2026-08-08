---
name: prototype-from-domain-story-and-glossary
description: >-
  Build a clickable web-app prototype from up to three inputs, re-asked for until each
  is supplied or declined: a Domain Story (required — STRUCTURE: screens, flows, roles, state machines), a Visual
  Glossary (optional — VOCABULARY and DATA SHAPE: term names, entity vs value object,
  cardinalities as validation), and a formatting example screenshot (optional — VISUAL
  STYLE). Enhances styled-prototype-from-domain-story by adding the glossary lane and a
  story-vs-glossary reconciliation pass, so the prototype speaks the team's agreed
  ubiquitous language and enforces the cardinalities the glossary asserts. Use whenever
  someone wants a prototype, clickable app, or mockup from a domain story — especially
  when a visual glossary, term diagram, or ubiquitous language artifact is also in play,
  e.g. "build a prototype from this domain story and our glossary". Trigger even when
  only the domain story is given: then ask for the other two before building.
---

# Prototype from a Domain Story + Visual Glossary

Produce a **clickable web-app prototype** from up to three inputs, each of which
answers a different question:

| Input | Answers | Required? |
|---|---|---|
| **Domain Story** | *What does it do?* — screens, navigation, roles, state machines | **yes** |
| **Visual Glossary** | *What are the things called, and what shape are they?* — exact terms, entity vs value object, cardinalities | optional |
| **Screenshot / mockup** | *What does it look like?* — colors, typography, spacing, components | optional |

This is the glossary-aware sibling of `styled-prototype-from-domain-story`. Use this
one whenever a glossary is or might be in play; use the other only when there is
definitely no glossary. Never run both.

## The one idea that makes this work: three lanes, three authorities

Each input owns a lane, and **lanes must not leak into each other**:

- **The story is the behaviour authority.** Which screens exist, what buttons do,
  what order things happen in, which statuses an entity can hold, who may do what.
  A glossary cannot supply a flow and a screenshot cannot supply a feature.
- **The glossary is the vocabulary and structure authority.** What each thing is
  *called*, whether it is a thing of its own or a field on something else, how many
  of it there may be. Whatever the glossary calls something is what the prototype
  calls it — in labels, in headings, in variable names.
- **The screenshot is the visual authority.** Every hex value, radius, font size and
  padding derives from sampled tokens, never from taste.

When the lanes disagree, resolve by authority, then **surface the disagreement** —
never silently pick a winner and move on. Conflicts between a story and a glossary
are usually a real finding about the domain, and the team wants to see them.

When you catch yourself adding a screen because the glossary has a term for it, or a
validation rule because it "feels right", or a color because it would look nice —
stop. That is a lane violation, and it makes the prototype untrustworthy as a
conversation piece.

## Step 0 — Keep an input inventory, and re-check it every time

The three inputs rarely arrive together. Someone typically starts with nothing, sends
the story, then sends the rest — so treat input-gathering as a **checkpoint you return
to**, not a question you ask once at the beginning.

Hold three slots, each in one of three states — **have it**, **missing**, or
**declined** (the user said to proceed without it). Re-run this checkpoint **before
every response in which new material has arrived**, and before starting Step 1.

Look for material in `/mnt/user-data/uploads/`, in pasted text, and in already-
interpreted briefs. Then, if any slot is still `missing`, show the inventory and ask —
**always naming all three slots with their status, including the ones you already
have**, because the visible checklist is what stops a slot being forgotten:

> Here's where I am:
> - **Domain story** — ✅ got it
> - **Visual glossary** — ❓ still missing
> - **Formatting example** — ❓ still missing
>
> The **glossary** (a term/concept diagram with cardinalities) settles what things are
> called and what the forms and validation look like; without it I'll infer attributes
> from the story and mark them as guesses. The **formatting example** (a screenshot of
> a UI whose look you want matched) fixes the visual identity; without it I'll design a
> plain one of my own. Both are optional — send either, or say "go ahead" and I'll
> build with what I have.

The distinction that matters: **supplying an input is not permission to start.** When
someone answers a request for the story by sending the story, they have filled one
slot — the other two are still `missing`, and the next response asks about them. Only
an explicit go-ahead (or an explicit "there is no glossary") moves a slot to
`declined`.

Then respect that: never ask again for a slot marked `declined`, and never ask twice in
a row for the same missing slot. One reminder per slot is helpful; two is nagging. If
the user goes quiet on a slot after one reminder, treat it as declined and build.

What changes with each combination:

| You have | You do | You must flag |
|---|---|---|
| Story only | Infer entity attributes from story usage; invent a restrained identity via the **frontend-design** skill | every attribute and every constraint as inferred; vocabulary unratified |
| Story + glossary | Fields, names, and validation come from the glossary | the design identity is yours, not the team's |
| Story + screenshot | Same as `styled-prototype-from-domain-story` | attributes inferred; no vocabulary authority |
| All three | Full workflow below | only genuine gaps |

### The shape this conversation usually takes

**Turn 1** — user says "build me a prototype", no files attached. All three slots
`missing`. Ask for all three; the story is the one you can't start without.

**Turn 2** — user sends the domain story. Story now `have it`; glossary and formatting
example still `missing`. Acknowledge the story, say what you'll do with it, and **ask
again for the other two**, showing the inventory. Do not start Step 1 yet — this is the
turn that gets skipped most often, and skipping it silently downgrades the prototype
to story-only.

**Turn 3** — user sends the glossary and says "no screenshot, go ahead". Glossary
`have it`, formatting example `declined`. Start building; never raise the screenshot
again; note in the readme that the visual identity is yours.

If instead Turn 3 is just "go ahead", both optional slots become `declined` — same
thing, fewer inputs.

## Workflow

Do not enter Step 1 until the Step 0 checkpoint has been run against the current turn
and every slot is either `have it` or `declined`.

Steps 1, 2 and 4 are independent interpretation tracks — run them in any order (or
together). Steps 3, 5, 6, 7 depend on them.

### Step 1 — Interpret the domain story (always)

Consult the **domain-story-interpreter** skill
(`/mnt/skills/user/domain-story-interpreter/SKILL.md`) and produce the **Prototype
Brief**. You need all of it, but especially: actors/roles (§2), modules (§3), domain
model (§4), **state machines (§5)**, use cases & journey (§6), screens & navigation
(§7), open questions (§8).

If the story is a non-trivial image, follow the interpreter's rule and **confirm your
transcription before building**. A misread arrow is cheap to fix now and expensive to
fix in code.

### Step 2 — Interpret the visual glossary (if provided)

Consult the **visual-glossary-interpreter** skill
(`/mnt/skills/user/visual-glossary-interpreter/SKILL.md`) and produce the **Glossary
Brief**. You need especially: the term catalogue (§2), the relationship table with
cardinalities (§3), the derived domain model — entities, value objects, aggregates,
identity (§4), bounded contexts (§6), and open questions (§7).

Tell the interpreter that the accompanying dynamic artifact is this domain story
(its Step 0 asks). Confirm the transcription for anything non-trivial.

### Step 3 — Reconcile story against glossary (if both)

This is the step that makes this skill worth having. Walk both briefs side by side
and produce a short reconciliation table before writing any code:

- **Term fidelity.** For every work object in the story, find its glossary term. Where
  they differ (`Bike` vs `Bicycle`, `Job` vs `Task`), **the glossary wins** — in UI
  labels, headings, entity names, and identifiers — and the rename goes in the readme.
- **Coverage, both directions.** Story work objects with no glossary term are
  *undefined vocabulary*: keep the story's name and list them as open questions.
  Glossary terms no story activity ever touches are *unexercised structure*: they may
  become fields on entities a screen already shows, but **do not invent a screen for
  them** — no story sentence justifies one.
- **Contradicted cardinality.** The story shows a Task with several Assignees while
  the glossary says `0..1`. Build the glossary's rule (it is the structure authority),
  make the conflict a prominent open question, and do not quietly relax the constraint.
- **Module agreement.** Glossary groups (§6) and story groups (§3) should describe the
  same boundaries. Where they don't, follow the story for navigation (it knows the
  flow), and report the mismatch.

If the disagreements are more than a handful, stop hand-rolling this and run the
**domain-story-glossary-consistency** skill
(`/mnt/skills/user/domain-story-glossary-consistency/SKILL.md`) properly, then build
from its Consistency Report.

### Step 4 — Extract the visual style (if a screenshot was provided)

Consult the **webapp-style-extractor** skill
(`/mnt/skills/user/webapp-style-extractor/SKILL.md`) and run it against the
screenshot. You want its JSON: `colors` (roles + palette), `typography` (named
styles), `spacing` (base unit + scale), and `components` (button, input, card, badge,
nav item… with sampled fills, radii, paddings, fonts, shadows). Sample from pixels —
never eyeball. Keep the `meta.notes` caveats; the honest ones go into the readme.

**With no screenshot**, consult the **frontend-design** skill instead and commit to
one coherent, restrained identity — and say in the readme that the look is yours and
therefore the cheapest thing in the prototype to change.

### Step 5 — Reconcile everything into a build plan

Write this table out; it is the spec the build follows.

| Source | Element | Becomes in the prototype |
|---|---|---|
| Story §3 | Modules | Top-level nav sections |
| Story §7 | Screens | Routes/views |
| Story §5 | State machines | Status badges + the buttons that transition them |
| Story §6 | Journey order | The click-through path |
| Story §2 | Actors & roles | Role gating + a role switcher affordance |
| Glossary §2 | Term spellings | Every visible label and every identifier |
| Glossary §4 | Entities / value objects / aggregates | Records with screens vs. fields on a parent form vs. the unit a screen saves |
| Glossary §3 | Cardinalities | Required/optional fields, repeaters with min/max, pickers vs. subforms |
| Glossary §4 | Identity terms | Read-only keys, shown but not edited after create |
| Style spec | tokens + components | The CSS variable layer and the component library |
| Story §8 + Glossary §7 + Step 3 findings | Open questions | Flagged in the readme, **not** silently filled |

`references/glossary-to-ui.md` has the full mapping — each cardinality form, `is-a`
edges, aggregate boundaries, unlabeled edges, and how to seed data that exercises the
bounds. **Read it whenever a glossary is in play**; the table above is only the
headline.

Two standing rules carried over from the sibling skill:

1. **When a screenshot exists, fidelity beats flair.** Use frontend-design for build
   *craft* — focus states, responsiveness, reduced motion, clear copy — but not for
   its "invent a distinctive identity" latitude. The identity is fixed by the
   screenshot.
2. **Screenshots show a subset of components.** When the story or the glossary needs
   something the screenshot never displayed — a modal, an empty state, a repeater row,
   a validation message — synthesize it from the established tokens so it feels
   native, and note it as extrapolated.

### Step 6 — Build the clickable prototype

Build a **single, self-contained app** (a React artifact or a standalone HTML file —
whichever renders inline in this environment) with:

- **A token layer first.** Color roles, named typography styles, spacing scale as CSS
  variables or a theme object. No hex or px scattered through the markup.
- **Reusable components built to the extracted specs** — Button, Card, Badge, Input,
  NavItem — each matching its `components` entry.
- **A navigable spine.** Every screen from §7 reachable, following the §6 order.
- **A live state machine.** Each §5 transition button updates in-memory state and
  re-renders: badge and available actions change. This is what separates a prototype
  from a picture.
- **Glossary-true forms.** Field labels use glossary spellings; required/optional and
  min/max follow the cardinalities; value objects are fields, not screens; entities at
  the far end of a relationship are pickers, not nested forms.
- **Seed data that exercises the bounds.** A few entities across the interesting
  statuses, including at least one that sits at a cardinality edge (an entity with the
  maximum allowed children, one with zero optional parts) so the constraints are
  visible rather than theoretical.
- **A role switcher.** A prototype has no auth; add a clearly-labelled persona
  selector so a reviewer can see the role-gated screens from §2.

Environment constraints: keep all state **in memory** — no `localStorage` /
`sessionStorage` (they fail in claude.ai artifacts) — and use mock data, no backend.

### Step 7 — Validate and deliver

Check before handing over:

- Every §7 screen is reachable; no dead links.
- Every §5 transition has a working control that updates badge and actions.
- Every user-visible noun matches its glossary spelling. Grep the source for the
  story-only synonyms you renamed in Step 3 — leftovers are the most common defect.
- Every validation rule traces to a glossary cardinality (or is absent, deliberately).
- Every visual value traces to the style spec or to a token-derived extrapolation you
  noted; nothing eyeballed.
- No screen exists that no story sentence justifies.

Save to `/mnt/user-data/outputs/`, present it, and include a short **readme** that
gives: (a) a traceability table — each screen/field back to its story sentence,
glossary term, or style token; (b) the renames applied in Step 3; (c) style caveats
carried from `meta.notes` (font families and shadows are usually guesses) plus
anything extrapolated; and (d) the merged open questions from story §8, glossary §7,
and the reconciliation. Keep it tight — the prototype is the deliverable.

## Worked sketch (illustrative)

Inputs: a library domain story (*Librarian catalogs Book via Catalog app; Librarian
publishes Catalog entry*), a book-catalog visual glossary (`Book —has→ 1 Title`,
`—has→ 1..10 Author`, `—has→ 0..25 Tag`, `—identified by→ 1 ISBN`, `—published by→
0..1 Publisher`), and a screenshot of an admin dashboard.

- Step 1 → screens *Catalog list*, *Catalog a book*, *Catalog entry detail*; states
  Draft → Published; role Librarian.
- Step 2 → `Book` is the aggregate root, `ISBN` its identity, `Title` a value object,
  `Author` and `Publisher` entities, `Tag` capped at 25.
- Step 3 → the story says "book record", the glossary says `Book` — every label
  becomes **Book**. `Publisher` appears in the glossary but no story activity touches
  it: it becomes an optional field on the Book form, **not** a Publishers screen.
- Step 4 → tokens: near-white surfaces, one indigo `primary`, 8px base unit, 8px card
  radius, 13px medium nav labels.
- Step 6 → the *Catalog a book* form has a required Title, a required ISBN shown
  read-only once saved, an Authors repeater with min 1 / max 10, a Tags input that
  refuses the 26th tag, and an optional Publisher picker. Seed data includes one Book
  with ten Authors and no Publisher.
- Step 7 → the readme flags that the 25-tag cap may be a remembered screen limit
  rather than a business rule (straight from the glossary's open questions).

The screens come from the **story**, the fields and their limits from the
**glossary**, the looks from the **screenshot**. No lane invents another's content.

## Common pitfalls

- **Treating a supplied input as a green light.** Someone sending the story after you
  asked for it has filled one slot, not authorised the build. Re-run the Step 0
  checkpoint and ask about the other two before interpreting anything.
- **A screen per glossary term.** The glossary is nouns, not navigation. Only the
  story justifies a screen.
- **Value objects promoted to CRUD.** `Title` and `Surname` are fields on a parent
  form, not entities with their own list-and-detail screens.
- **Story vocabulary leaking into the UI** after the glossary settled the name. Grep
  for it.
- **Baking arbitrary bounds as hard rules without flagging.** A `0..25` cap may be a
  real rule or a screen limit someone remembered. Enforce it *and* ask.
- **Silently resolving a story/glossary conflict.** The conflict is a finding the
  team wants; report it.
- **A pretty but dead mock.** If the transition buttons don't change state, it isn't a
  prototype.
- **Empty screens.** Seed data in the states and at the bounds each screen exists to
  show.

## References

- `references/glossary-to-ui.md` — the full mapping from glossary constructs to UI and
  data decisions: every cardinality form as a form control and validation rule, value
  objects vs entities vs aggregates, identity terms, `is-a` edges, bounded contexts,
  what to do with unlabeled edges and missing cardinalities, plus seed-data recipes
  that exercise the bounds. Read it whenever a glossary is in play, before building.