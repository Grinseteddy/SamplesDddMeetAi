---
name: arazzo-from-event-model
description: >-
  Turn an Event Model (Dymitruk: a swimlaned timeline of State Change / State
  View / Automation / Translation slices, often several board photos) into an
  Arazzo Specification file — machine-readable API workflows describing what
  the USER does: one workflow per user goal, one step per user-facing slice,
  inputs and outputs chained with runtime expressions, successCriteria from the
  read models, failure actions from the guards. Use whenever someone has an
  Event Model, event-model-author output, or a board photo and wants Arazzo, an
  arazzo.yaml, an API workflow description, or runnable user journeys — "turn
  our board into workflows the tooling can execute". Also use when extending an
  existing Arazzo document so it matches the house style, and when asked to
  validate or lint one. Trigger even when nobody says "Arazzo" but the ask is a
  board's user interactions as a sequence of API calls. Produces a single
  .arazzo.yaml that passes Redocly lint and the bundled cross-reference check.
author: Annegret Junker
---

# Arazzo from an Event Model

An Event Model already contains the user's journey — it just draws it as
stickies on a timeline instead of as calls against an API. The wireframes are
the screens, the commands under them are the requests, the events are the facts
those requests produce, and the read models are what the user looks at
afterwards. Arazzo is the machine-readable form of exactly that: an ordered
sequence of API calls, with the output of one call feeding the input of the
next.

This skill does that translation and nothing more. **Every step traces to a
slice on the board.** A step the board doesn't draw doesn't go in the file — it
goes in the report as an open question. Inventing the login step, the pagination
call and the "get current user" the model forgot is the single most common way
this output stops being trustworthy: once one step is fabricated, the reader can
no longer tell which of the others weren't.

## Scope: user interactions only

The ask is the *user's* interactions, and that is a real constraint on what
becomes a step:

- **A user-facing slice becomes a step.** State Change (the user acts) and
  State View (the user looks) are the two that carry a wireframe.
- **An Automation slice is a wait, never a button.** A policy fires without the
  user touching anything. Modelling it as a step the workflow *calls* puts the
  user's finger on a machine's trigger and produces a workflow that lies. Model
  it as waiting for the result — an async `receive` step, or a poll on the read
  model with `onFailure: retry`.
- **A Translation slice is usually invisible.** It is the reason a later step
  reads a *different* `sourceDescription`, not a step of its own. Draw a step
  only when the user visibly waits for the crossing, and then treat it as a wait.
- **One workflow has one runner.** When the board hands the work to a different
  actor (a cook asks, a stranger answers), that is two workflows, not one
  workflow with a change of person halfway down.

Say in the report which slices produced no step and why. A thin lane is a
finding, not an omission to paper over.

## Inputs

**Required — the Event Model.** Board images (several is normal), a Miro
export, or an already-transcribed model. Output from `event-model-author` skips
most of Step 1. If there is no model in context:

> Upload the **Event Model** — board photos are fine, and several are normal
> since the timeline is usually wider than one screenshot. Tell me the
> left-to-right order if the filenames don't.

**Strongly wanted — the API descriptions.** Arazzo requires at least one entry
in `sourceDescriptions`, and every step points into one. Ask once:

> Do you have **OpenAPI** (and, for the events, **AsyncAPI**) descriptions for
> these contexts? If not I'll write the workflows against proposed operation
> names and mark each one as unresolved.

Three honest situations, all supported — see `references/mapping.md` §6:
existing specs (best), no specs yet (proposed operations, flagged), or specs for
some lanes only (mix, and say which is which).

**Optional, worth one ask:** a **Visual Glossary** (fixes the vocabulary in
inputs, outputs and payload fields), **invariants** from
`event-model-invariant-finder` (become `onFailure` actions and the criteria on
them), and a **screen flow** from `screen-flow-from-event-model` (confirms which
slices are one screen and therefore one step). Accept a no and proceed, saying
what the naming rests on.

**If the board isn't an Event Model** — a raw EventStorming wall, no wireframe
row, no swimlanes — say so and offer `event-model-author` first. Arazzo derived
from a board with no commands is a guess about an API dressed up as a spec.

## Step 1 — Read and stitch the board

Reading errors compound: a misread command becomes an operation that doesn't
exist, and every runtime expression downstream inherits it. The stitching
procedure is the same one `screen-flow-from-event-model` uses; its
`references/reading-the-board.md` is the fuller treatment. Short form:

1. **Put the images in timeline order** and say which order you used.
2. **Confirm the swimlanes recur identically** across images — same names, same
   order. A lane is a bounded context and will become a `sourceDescription`.
3. **Classify every seam:** *overlap* (dedupe), *abutment* (nothing lost), or
   *gap* (a stretch this capture never shows — a hole in the input, not an empty
   stretch of the domain).
4. **The same workflow drawn twice is recurrence, not duplication.** Two
   triggers reaching one help flow is **one** Arazzo workflow with two callers.
   This is the mistake that doubles a file.
5. **A line running off a frame edge is unresolved**, never continuing. Ask.

Then transcribe before interpreting: per slice, its type, lane, wireframe,
command, event, read model, policy, and actor. On a non-trivial board, show this
strip and get it confirmed — cheap now, expensive after the YAML is written.

## Step 2 — Choose the workflows

A workflow is **one actor pursuing one outcome**. Cut them where the board does:

| Signal on the board | Workflow decision |
|---|---|
| A run of slices ending in an event the business names as done (`Meal plan settled`, `Order placed`) | one workflow, ending there |
| The same slice set reached from two triggers | **one** workflow, called from both by a `workflowId` step |
| The actor changes | a second workflow; the first calls it or `dependsOn` it |
| A trouble branch (`Step unclear`, `Catastrophe happened`) | a branch inside the workflow via `onSuccess: goto`, or a called sub-workflow if it recurs |
| An outcome that can only start once another has completed | `dependsOn` at workflow level, with the identifier passed as an input |

Aim for **3–8 steps per workflow**. Past a dozen the file stops being readable
and the outcome is probably two outcomes. Name workflows for the user's goal in
the board's own vocabulary — `planDinner`, `getHelpWhenStuck` — not for the
service (`mealPlanningService`) and not for the screen.

## Step 3 — Map slices to steps

The full table, with the ambiguous cases worked through, is in
`references/mapping.md`. Read it before writing YAML. The core:

| Slice | Step |
|---|---|
| **State Change** | `operationId` of the command's operation; `requestBody` built from the command's fields; `successCriteria` on the status code; `outputs` = the identifiers the *event* carries |
| **State View** | `operationId` of the read model's GET; `successCriteria` asserting the previous event is visible (a `jsonpath` criterion, not just `200`); `outputs` = what a later step or the user needs |
| **Automation** | a wait: an async `receive` step (Arazzo 1.1 + AsyncAPI), or a poll on the read model with `onFailure: {type: retry, retryAfter, retryLimit}` |
| **Translation** | normally nothing; it explains why the next step uses another `sourceDescription` |
| **A guard / invariant** | an `onFailure` action with `criteria` on the refusal's status code, named for the refusal (`planNotComplete`) |
| **A recurring sub-flow** | a step with `workflowId`, its `parameters` mapping to that workflow's inputs |

**Wire the data flow from the board, not from imagination.** Whatever the
command's sticky says the user supplies becomes a workflow `input`; whatever the
event's sticky carries becomes a step `output`; a later step reaches back with
`$steps.<stepId>.outputs.<name>`. If the board never says where a value comes
from, that is an open question, not a field to invent.

## Step 4 — Traceability

The signature of this skill's output: **every workflow and every step carries an
`x-event-model` extension** saying where it came from. It is what makes the file
checkable by someone holding the board photos, and it is what keeps the next
person from having to trust you.

```yaml
x-event-model:
  lane: Cook Assistance          # the swimlane
  slice: Request help            # the slice's own name on the board
  sliceType: state-change        # state-change | state-view | automation | translation
  image: 2                       # which board image, if several
  stickies: [Request help, Help requested, Help request]
  status: on-board               # on-board | inferred | proposed-operation
```

`status` is the honesty field. `on-board` means the board draws it.
`inferred` means the board implies it and you reconstructed it (say what from,
in the step `description`). `proposed-operation` means the operation does not
exist in any supplied source description — the workflow reads correctly but will
not run until someone builds or names it.

## Step 5 — Write the document

Start from `assets/skeleton.arazzo.yaml`; mirror `assets/example-community-cooking.arazzo.yaml`,
which is the gold standard for structure and formatting. Write in this order:

1. `arazzo` version — **`1.1.0`** when the board's automations or translations
   are worth modelling as async `receive` steps and an AsyncAPI exists;
   **`1.0.1`** otherwise, which every tool supports. Say which you chose and why.
2. `info` — `title` from the board, `summary`, `description` naming the board
   and images it came from, `version: 1.0.0`.
3. `sourceDescriptions` — one per bounded-context lane that owns operations the
   workflows call, plus AsyncAPI entries for the lanes that carry events.
4. `workflows` — in the board's left-to-right order, each with `summary`,
   `inputs`, `steps`, `outputs`, and `dependsOn` where the board demands it.
5. `components` — anything used twice: `inputs` schemas, `parameters` (an auth
   header belongs here, and **only if the board draws authentication or the user
   says the API needs it**), `failureActions` reused across workflows.

House style at a glance, with the full list in `references/mapping.md` §7:

- `workflowId`, `stepId`, `sourceDescriptions[].name`: `camelCase`, matching
  `[A-Za-z0-9_-]+` — **no dots**, which breaks runtime expressions.
- Step ids read as the user's action: `settleMealPlan`, `viewSelectedRecipes`,
  `awaitHelpResponse`. Prefix waits with `await`, views with `view`.
- Output names come from the event's own field names, `camelCase`.
- `operationId` written as `$sourceDescriptions.<name>.<operationId>` — required
  whenever there is more than one non-Arazzo source, and harmless when there
  isn't, so always use it.
- Every step gets a `description` in one line of the board's language.
- Prefer `operationId` over `operationPath`; use `operationPath` only when the
  OpenAPI operation genuinely has no `operationId`.

## Step 6 — Validate

Never ship an unvalidated Arazzo file. Two layers, both in
`references/validation.md`:

```bash
npm install -g @redocly/cli
redocly lint <file>.arazzo.yaml           # structure against the Arazzo schema

python3 scripts/check_arazzo.py <file>.arazzo.yaml   # everything redocly won't
```

**Redocly does not resolve `operationId`s against your OpenAPI**, does not check
that `$steps.x.outputs.y` exists, and does not notice a forward reference. The
bundled checker does: unresolved operations, undeclared inputs, unknown step
outputs, references to later steps, missing `dependsOn` targets, async steps
without `action`, and missing `x-event-model` traceability. Fix every error and
re-run until both are clean. If npm is unavailable, the checker alone still
catches most of it — say in the report that the schema layer was skipped.

## Output

Two artifacts: the file, and a short report so a human can check it against the
board.

```
# Arazzo — <board name>

## 0. Inputs
Images and their order, seam classifications, which source descriptions were
supplied and which were declined, the Arazzo version chosen and why.

## 1. Workflows
workflowId · goal · actor · steps · dependsOn · what it outputs

## 2. Slice coverage
Slice · lane · became (step / wait / nothing) · why
Every slice on the board appears in this table exactly once.

## 3. Unresolved references
Every step with status `proposed-operation`: what the operation would need to
do, and which lane should own it.

## 4. Validation
The commands run and their result.

## 5. Open questions
Values with no stated source, automations with no visible trigger, branches
with no exit, actors the board never names. Each with the question that would
settle it.
```

Adapt to the ask — "just give me the arazzo file" gets the file plus §2 and §3,
because those are what make it checkable. A handover gets all of it. Save the
`.arazzo.yaml` to the outputs directory and present it.

## Working with neighbouring skills

- **No Event Model yet, only a domain story and contexts** → `event-model-author`.
- **The board is a raw EventStorming wall** → `event-storming-interpreter`, then
  `event-model-author`.
- **No OpenAPI for a lane** → `openapi-spec-author` writes one from the same
  slices; the operations then resolve and `proposed-operation` disappears.
- **The events need describing** → `asyncapi-spec-author`, which is what an
  Arazzo 1.1 `asyncapi` source description points at.
- **"What would refuse this command?"** → `event-model-invariant-finder`; its
  guards become the `onFailure` criteria here.
- **The user wants the picture, not the spec** → `screen-flow-from-event-model`.
- **The user wants something clickable** → `prototype-from-event-model-and-domain-story`.
- **Vocabulary disagreements** → `visual-glossary-interpreter`.

## Bundled resources

- `references/mapping.md` — the full slice→step mapping, worked ambiguous cases
  (recurrence, automation waits, branches, multi-actor handoff), the three
  source-description situations, and the complete naming conventions.
- `references/arazzo-reference.md` — Arazzo 1.0.1 / 1.1.0 field reference,
  runtime expressions, criteria, success/failure actions, async steps. Read it
  instead of guessing field names.
- `references/validation.md` — Redocly lint, the bundled checker, `redocly
  respect`, and what each layer does and doesn't catch.
- `scripts/check_arazzo.py` — cross-reference and traceability checker.
- `assets/skeleton.arazzo.yaml` — minimal correct starting point.
- `assets/example-community-cooking.arazzo.yaml` — gold-standard example, with
  its companion `example-meal-planning.openapi.yaml`,
  `example-meal-preparation.openapi.yaml`,
  `example-cook-assistance.openapi.yaml` and `example-cooking.asyncapi.yaml`.
  It demonstrates recurrence as a called workflow, an automation modelled two
  ways (async wait and poll-with-retry), a branch via `goto`, invariants as
  named failure actions, and traceability on every step. It passes both
  validation layers as it stands, so it doubles as a smoke test.

## References

A. Dymitruk, *Event Modeling*, eventmodeling.org — the four slice patterns.

*The Arazzo Specification*, OpenAPI Initiative — v1.0.1 (2025-01-16) and v1.1.0
(2026-05-17), https://spec.openapis.org/arazzo/latest.html