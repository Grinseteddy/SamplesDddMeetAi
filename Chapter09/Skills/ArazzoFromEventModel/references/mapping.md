# Mapping an Event Model to Arazzo

Contents:

1. The four slices, in detail
2. Workflow boundaries and recurrence
3. Branches, trouble and refusals
4. Data flow: inputs, outputs, criteria
5. Multi-actor boards
6. The three source-description situations
7. Naming conventions
8. Things that are never a step

---

## 1. The four slices, in detail

### State Change — the user acts

The wireframe, the command under it, the event to its right. This is the
archetypal Arazzo step.

```yaml
- stepId: settleMealPlan
  description: The cook settles the plan for the dinner.
  operationId: $sourceDescriptions.mealPlanningApi.settleMealPlan
  parameters:
    - name: planId
      in: path
      value: $steps.planDinner.outputs.planId
  requestBody:
    contentType: application/json
    payload:
      selectedRecipeIds: $steps.selectRecipe.outputs.selectedRecipeIds
  successCriteria:
    - condition: $statusCode == 200
  outputs:
    planId: $response.body#/planId
    settledAt: $response.body#/settledAt
  x-event-model:
    lane: Meal Planning
    slice: Settle meal plan
    sliceType: state-change
    stickies: [Settle meal plan, Meal plan settled, Meal Plan]
    status: on-board
```

Rules that matter:

- **The outputs come from the event, not from the command.** The event sticky is
  what the system asserts happened; those are the fields other slices read.
- **`successCriteria` on a State Change is the status code.** Asserting on the
  body here duplicates the State View that follows.
- **A command with no event drawn above it is a gap.** Write the step, mark
  `status: inferred`, and put the missing event in the report's open questions.

### State View — the user looks

The read model feeding a wireframe. A State View is worth a step precisely
because it is the assertion that the previous command actually landed.

```yaml
- stepId: viewSelectedRecipes
  description: The cook sees the recipes now on the menu.
  operationId: $sourceDescriptions.mealPlanningApi.getSelectedRecipes
  parameters:
    - name: planId
      in: path
      value: $steps.planDinner.outputs.planId
  successCriteria:
    - condition: $statusCode == 200
    - context: $response.body
      condition: $.recipes[*]
      type: jsonpath
  outputs:
    selectedRecipeIds: $response.body#/recipes
```

- **Assert the effect, not just the code.** A `200` with an empty projection is
  the commonest silent failure in an event-sourced system; a `jsonpath`
  criterion catches it and is the single most useful thing this file can do.
- **Consecutive views of the same screen are one step.** If a State Change and
  the State View next to it show the same page, that is one screen and — unless
  the read is what proves the write — one step.
- **A read model with no wireframe is not a user interaction.** It is internal.
  No step; note it in the report.

### Automation — the system acts, the user waits

A policy reading a to-do list and issuing a command by itself. The user is not
in this slice, so the workflow never *calls* it. It **waits for its result**, in
one of two shapes:

**Async wait (Arazzo 1.1 + an AsyncAPI source).** The honest shape when the
events are actually published on a broker:

```yaml
- stepId: awaitMealRescued
  description: Wait for the assistant to confirm the meal is back on track.
  operationId: $sourceDescriptions.cookingEvents.mealRescued
  action: receive
  correlationId: $steps.startPreparation.outputs.preparationId
  timeout: 120000
  successCriteria:
    - context: $message.payload
      condition: $.status == 'RESCUED'
      type: jsonpath
  outputs:
    rescuedAt: $message.payload#/rescuedAt
```

**Poll (Arazzo 1.0.1, or no AsyncAPI).** Re-read the read model until the
automation's effect shows up:

```yaml
- stepId: awaitMealRescued
  description: Poll the preparation until the rescue is recorded.
  operationId: $sourceDescriptions.mealPreparationApi.getPreparation
  parameters:
    - name: preparationId
      in: path
      value: $steps.startPreparation.outputs.preparationId
  successCriteria:
    - context: $response.body
      condition: $.status == 'RESCUED'
      type: jsonpath
  onFailure:
    - name: keepWaitingForRescue
      type: retry
      retryAfter: 5
      retryLimit: 12
```

Either way the step id starts with `await`, and the `x-event-model.sliceType` is
`automation` so a reader can see that no human pressed anything.

**Never** turn a policy's command into a step the workflow issues. If the
workflow can call it, the automation was not an automation, and that is worth
raising rather than encoding.

### Translation — a fact crossing a boundary

Usually no step at all: it is the explanation for why the next step points at a
different `sourceDescription`. Record it on the *following* step:

```yaml
  x-event-model:
    lane: Notification
    slice: Thanks given → Notification
    sliceType: translation
    status: on-board
```

Give a translation its own step only when the user visibly waits for the
crossing — and then it is a wait, exactly as above.

---

## 2. Workflow boundaries and recurrence

**One actor, one outcome, 3–8 steps.** Cut a workflow where the board names a
completed fact.

**Recurrence is the trap.** A board draws the help flow twice because two
triggers reach it — once from planning trouble, once from cooking trouble. That
is *one* workflow with two callers, and building it twice produces two
implementations of one thing:

```yaml
- stepId: getHelp
  description: Ask the community and wait for an answer.
  workflowId: getHelpWhenStuck
  parameters:
    - name: situation
      value: $steps.reportCatastrophe.outputs.catastropheId
  outputs:
    helpResponseId: $workflows.getHelpWhenStuck.outputs.helpResponseId
```

When a step specifies `workflowId`, its `parameters` map to that workflow's
`inputs` and **must not carry `in`**. The called workflow's outputs are the
step's outputs.

**`dependsOn` at workflow level** expresses "you cannot cook a meal you have not
planned" — but only if the board actually says so. If the board shows a second
entry path into cooking, there is no dependency, and that second path is a more
interesting finding than the dependency would have been.

---

## 3. Branches, trouble and refusals

| On the board | In Arazzo |
|---|---|
| A trouble event that diverts the user (`Step unclear`) | `onSuccess: {type: goto, stepId: ...}` with `criteria` on what the read model shows |
| A trouble branch that recurs in two places | a called sub-workflow (§2) |
| A guard that makes a command fail (from `event-model-invariant-finder`) | `onFailure` action, `criteria` on the refusal's status code, `name` in the domain's words |
| A branch the board draws with no way out | **no step** — an open question. A branch you can only leave by succeeding is a modelling gap, and inventing the exit hides it |

```yaml
  onFailure:
    - name: stepsStillOpen
      type: end
      criteria:
        - condition: $statusCode == 409
```

Name failure actions for the *domain* refusal (`stepsStillOpen`,
`planNotSettled`), never for the transport (`http409`). The name is what a
reader sees when the workflow stops.

---

## 4. Data flow: inputs, outputs, criteria

- **Workflow `inputs`** is a JSON Schema 2020-12 object: exactly the values the
  user supplies to the *first* command, plus identifiers handed in by a caller.
  Name properties from the glossary if one was supplied, otherwise from the
  command sticky.
- **Step `outputs`** carry the identifiers the event asserts. Name them from the
  event's own fields. An output nobody reads downstream and that isn't in the
  workflow `outputs` is noise — delete it.
- **Reach backwards only.** `$steps.<id>.outputs.<name>` must refer to a step
  *earlier* in the array; a forward reference cannot be satisfied by a
  sequential runner and the bundled checker rejects it.
- **Workflow `outputs`** are what a caller needs — usually one or two
  identifiers, expressed as `$steps.<id>.outputs.<name>`.
- **JSON Pointer for body access:** `$response.body#/planId`, not
  `$response.body.planId`, whenever you are addressing into a body.
- **Criteria types:** `simple` (default) for status codes and scalar
  comparisons; `jsonpath` with a `context` for anything inside a body; string
  literals in `simple` conditions use single quotes and compare
  case-insensitively.

---

## 5. Multi-actor boards

An Event Model routinely shows two people. Arazzo workflows have one runner, so:

- **One workflow per actor**, each named for that actor's goal.
- The requester's workflow **waits** for the responder's effect (§1, Automation
  wait) — it does not call the responder's command.
- Put the actor in `x-event-model.actor` on the workflow, and say in the report
  which credentials each workflow would run under. Do **not** add an auth step
  the board does not draw; note it as an open question instead.

---

## 6. The three source-description situations

**(a) Specs exist.** Point `sourceDescriptions[].url` at them (relative paths
are fine and are what the checker resolves), use real `operationId`s, and mark
every step `status: on-board`.

**(b) No specs.** Arazzo still requires at least one `sourceDescriptions` entry,
so declare the intended file per lane:

```yaml
sourceDescriptions:
  - name: mealPlanningApi
    url: ./meal-planning.openapi.yaml   # does not exist yet
    type: openapi
```

Write `operationId`s in the house convention — the command sticky in camelCase,
`settleMealPlan` — mark every affected step `status: proposed-operation`, and
list them in report §3 with what each operation would have to do. Offer
`openapi-spec-author` to make them real. The file is a design document until
they exist, and it should say so in `info.description`.

**(c) Some lanes only.** Mix, and make the report's coverage table show which
lane each step's operation came from. Do not quietly invent operations in the
covered specs to make the uncovered lanes look resolved.

---

## 7. Naming conventions

| Thing | Convention | Example |
|---|---|---|
| File | `<domain>.arazzo.yaml` | `community-cooking.arazzo.yaml` |
| `info.title` | the board's name, title case | `Community Cooking` |
| `info.version` | the document's own version, semver | `1.0.0` |
| `sourceDescriptions[].name` | lane in camelCase + `Api`/`Events` | `cookAssistanceApi`, `cookingEvents` |
| `workflowId` | camelCase verb phrase, the user's goal | `planDinner`, `getHelpWhenStuck` |
| `stepId` | camelCase verb phrase, the user's action | `settleMealPlan`, `viewHelpResponses` |
| wait steps | prefixed `await` | `awaitMealRescued` |
| view steps | prefixed `view` | `viewSelectedRecipes` |
| `outputs` keys | the event's field name, camelCase | `planId`, `helpResponseId` |
| failure action `name` | the domain refusal, camelCase | `stepsStillOpen` |
| success action `name` | where it goes, camelCase | `divertToHelp` |
| extension | `x-event-model` on every workflow and step | see SKILL.md Step 4 |

`workflowId`, `stepId` and source names must match `[A-Za-z0-9_-]+`. **No dots
and no spaces** — runtime expressions parse on dots, so `meal.planning` silently
breaks `$steps.meal.planning.outputs.x`.

---

## 8. Things that are never a step

- **Authentication the board doesn't draw.** Common, necessary, and still not
  yours to invent. Report it.
- **A read model with no wireframe.** Internal data, not a user interaction.
- **A policy's own command.** See §1, Automation.
- **Navigation.** Opening a screen is not an API call unless a read model backs
  it; the screen flow belongs in `screen-flow-from-event-model`.
- **Setup and teardown the board never shows** — creating the test user, seeding
  the catalogue, cleaning up afterwards. An Arazzo file that quietly becomes a
  test fixture stops being a description of the domain.