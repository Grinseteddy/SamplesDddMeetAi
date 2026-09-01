---
name: event-model-author
description: >-
  Build an Event Model — Adam Dymitruk's technique — from one or more Domain
  Stories and given Bounded Contexts: one left-to-right timeline of events
  organized into swimlanes per context, cut into workflow slices (state
  change, state view, automation, translation), each with a wireframe and a
  Given/When/Then spec, plus an information-completeness check. Use whenever
  someone wants to "build an event model", "do event modeling", mentions
  Dymitruk or eventmodeling.org, wants a "big picture" timeline or
  workflow/vertical slices tying a UI to a command, event and read model, or
  wants a Domain Story plus its contexts turned into something buildable on
  one page — even unnamed. Needs at least one Domain Story (ask if none
  supplied) and Bounded Contexts for swimlanes (ask if missing; offer
  domain-story-context-finder). Found events are optional — ask if the user
  has some (EventStorming, domain-story-event-seeder) before deriving them.
author: Annegret Junker
---

# Event Model Author

Event Modeling (Adam Dymitruk, eventmodeling.org) is a specification
technique, not a discovery one. Where EventStorming diverges — many people,
many stickies, disagreement as the point — Event Modeling **converges**: it
takes a scenario that is already understood (a Domain Story) and a set of
boundaries that are already drawn (Bounded Contexts) and lays out, on one
page, exactly what a builder needs — a single ordered timeline of facts,
organized into swimlanes, cut into buildable slices, each one specified
precisely enough to write an acceptance test from. This skill produces that
page.

The job is synthesis, not invention. Every event, every field, every slice
should be traceable to a domain story sentence or an explicit assumption you
flagged — never a silent guess.

## Core mental model

**One timeline, many swimlanes.** Unlike an EventStorming board (which can
show each bounded context as its own local sequence), an Event Model has
exactly one shared, chronologically ordered spine of events running
left-to-right across the whole page. Bounded contexts are **horizontal bands**
under that spine, not separate boards — an event belongs to the context that
produced it, but its position on the timeline is absolute, shared by every
lane. This is what makes the model answer "what did the system know, and
when" — the question a discovery-phase board doesn't have to answer.

**Every step is one of four patterns.** Dymitruk names four recurring shapes;
almost everything on the page is one of them (full detail, with more examples
of each: `references/event-modeling-method.md`):

| Pattern | Shape | Answers |
|---|---|---|
| **State Change** | wireframe → Command (blue) → Event(s) (orange) | How does information get *into* the system? |
| **State View** | Event(s) (orange) → Read Model (green) → wireframe | How is that information *shown back*? |
| **Automation** | Event or elapsed state → Command, via a policy | How does the system *react on its own*? |
| **Translation** | Event in context A → Command in context B | How does one context's language *cross into* another's? |

**Events are facts, named in the past tense, and immutable.** A Read Model is
a projection built from events, never a source of truth in its own right. A
Command is a request that may be refused; an Event is what actually happened.

**Every slice earns a Given/When/Then.** For a State Change, the Given is the
prior events that establish the starting state, the When is the command with
concrete example data, and the Then is the event(s) it produces — literally an
acceptance test, not just documentation. For a State View, the check is
lighter: given these events, the read model shows this.

**Information completeness is the model's main discipline.** No field may
appear in a Command or a Read Model unless some earlier event on the timeline
actually captured it. This single check — walk every field backward to its
source event — surfaces missing events, missing Translation slices, and
boundaries drawn in the wrong place faster than anything else in the method.

**The best model is boring.** A domain expert with no technical background
should be able to walk the page left to right and recognize their own process.
Resolve ambiguity into an explicit question in §6/§7 of the output, never into
a clever guess that makes the page look more finished than the inputs justify.

## Inputs this skill needs

Three things feed an Event Model. Check the conversation and any uploads for
each before asking — the user may already have supplied it, possibly as the
output of a sibling skill.

**1. At least one Domain Story — required.** A numbered
actor→activity→work-object story (Hofer & Schwentner), an egon.io export, or a
photo of one. Without a concrete scenario there is nothing to put on the
timeline. If none is present anywhere in context:

> Do you have a **Domain Story** for this process — numbered
> `Actor · verb · work object` sentences, an egon.io export, or a photo of the
> board? Several stories covering different paths (happy path plus a failure
> or two) produce a much richer model than one.

**2. The Bounded Contexts to use as swimlanes — required.** Event Modeling
does not discover contexts; it is handed them. If the user hasn't given you a
list of context names (e.g. from `domain-story-context-finder`, a Visual
Glossary, or their own team's cut), ask:

> What **Bounded Contexts** should the swimlanes be? If you don't have a cut
> yet, I can run `domain-story-context-finder` on the story first and use its
> proposal — or you can just give me the names you already work with.

Take a given cut as given — don't quietly re-derive or rename it (see Step 3).

**3. Found/candidate events — optional, but ask.** If the user already has a
domain-event list (an EventStorming board, `domain-story-event-seeder` output,
a pivotal-event cut), reusing it keeps this model consistent with everything
else the team has produced. If nothing is offered, ask once:

> Do you already have a list of **domain events** for this process — from an
> EventStorming board, `domain-story-event-seeder`, or similar? If not, I'll
> derive the timeline directly from the domain story as the first step below.

A "no" is a fine answer — proceed to Step 2 and derive them. Don't ask twice.

## Workflow

### Step 1 — Transcribe the domain story (or stories)

If any input is an image, read it into numbered sentences before interpreting
anything — a misread arrow poisons everything downstream. Use the notation
from `domain-story-interpreter` (number sits at the arrow's origin; unnumbered
continuation arrows — `to`, `via`, `with`, `and` — extend the same sentence).
For a non-trivial diagram, show the transcription and get it confirmed before
continuing.

### Step 2 — Establish the Big Picture: one merged timeline

This produces the spine everything else hangs off. Two cases:

- **Found events were supplied.** Line them up against the story sentences
  they came from (tag each `(A3)`, `(B1)`, …). Where a supplied event has no
  matching sentence, keep it and note that it isn't traceable to *this* story.
  Where a sentence has no matching event, that's a gap — flag it rather than
  inventing a name to fill the row.
- **No found events.** Derive them yourself, the way `domain-story-event-seeder`
  does: translate each sentence into a past-tense fact (never one-to-one —
  drop pure UI/navigation steps, collapse a sentence pair that is one fact,
  split a sentence that hides several), then sweep once for the events a
  single concrete story cannot contain by construction: rejections, timeouts,
  reversals, and anything the outside world contributes.

With **more than one story**, decide out loud whether they are variants of one
process, segments of a longer one, or genuinely separate processes sharing
some nouns (same three cases as `domain-story-event-seeder` §2) — an Event
Model needs exactly one merged spine, so state how you merged before the
timeline appears.

Present the result as a plain, ordered list — no swimlanes yet. This is the
Big Picture: the whole story, checkable against the source before you start
assigning ownership.

### Step 3 — Assign each event to a swimlane

For every event on the spine, decide which **given** bounded context produced
it, using the same evidence a context cut relies on: whose language names the
fact, which actor's activity produced it, which noun's model it belongs to.
**Do not re-cut the contexts.** If an event genuinely doesn't fit any of the
given ones, that is a finding, not a problem to solve quietly — note it as
either a missing context or a sign the given cut needs revisiting, and keep
going with your best placement.

Produce a swimlane strip: `# | Event | Bounded Context | Source sentence(s)`.

### Step 4 — Cut the timeline into workflow slices

Walk the spine and group consecutive or related steps into **slices** — one
meaningful interaction each — and tag every slice with one of the four
patterns from the table above. A slice can straddle two swimlanes (Automation
and Translation usually do); that's normal and worth drawing as a connector
between rows rather than forcing it into one lane.

Rules of thumb, expanded with worked examples in
`references/event-modeling-method.md`:

- A sentence where a person acts on a form/screen and something happens →
  **State Change**.
- A sentence (or an inferred moment) where someone needs to see something
  before they can act → **State View**. If the story implies a decision but
  never shows what informed it, that view is a gap — name it rather than
  skip it.
- A sentence with no human actor, or a system/policy reacting to a prior
  event or to elapsed time → **Automation**.
- A moment where an event's meaning changes as it crosses from the context
  that produced it into the one that consumes it → **Translation**. This is
  usually where a Domain Story's compound sentences (`X and takes Y`) split
  across two lanes.

### Step 5 — Specify every slice

**State Change and Automation slices** get a full Given/When/Then, in the
Gherkin style already used across this project's DDD skills:

```
@SLICE-04 [State Change] Meal Preparation
Scenario: A cook reports a catastrophe mid-recipe
  Given Meal preparation started (recipe: "Tomato Risotto", started_at: 18:02)
   When Report catastrophe (step: 4, description: "rice has scorched")
   Then Catastrophe happened (step: 4, description: "rice has scorched",
        reported_at: 18:11)
```

**State View slices** get the lighter form — no command to test, just a
projection check:

```
@SLICE-06 [State View] Community Help
Given  Help requested (…) · Help provided (…, responder: "Grandma Avatar")
Shows  the response text, the responder's name, and whether it is
       machine-generated
```

Every field in a When or a Shows must appear somewhere in a Given — that
constraint *is* Step 6.

### Step 6 — Wireframe sketch, kept light

For each slice with a human-facing UI, sketch the screen as a short field
list rather than pixels: form fields mapped to the command's fields (State
Change), or display fields mapped to the read model's fields (State View),
plus the action that fires the next step. Offer to render an actual mockup
(via the Visualizer or `frontend-design`) only if the user wants one — the
field list is what the specification needs. Automation and Translation slices
have no human wireframe; note the system or policy boundary instead (matching
the pink-external-system / lilac-policy convention already used in this
project's EventStorming work).

### Step 7 — Run the information-completeness check

For every field named in a Command or a Read Model, trace it backward to the
event that supplied it. Anything that doesn't trace is one of three things:
a missing upstream event, a missing Translation slice from another context, or
an assumption worth flagging. This is the single highest-value check in the
method — spend real effort here rather than treating it as a formality.

### Step 8 — Surface gaps, contested calls, and open questions

Same discipline as the rest of this project's skills: name what's missing
rather than filling it in. Events assigned to no context; contexts that
produced no events; State Views the story implies but never shows; slices
that could plausibly use a different pattern (a manual State Change today that
is really an Automation candidate); places where two stories disagreed. Each
gets the concrete question that would settle it.

### Step 9 — Draw the grid

Once the slices are specified, render output §4 as **both** a table and a
full color-coded Mermaid flowchart: one subgraph per bounded context, with
wireframes, commands, events, read models and policies all drawn and classed,
and sequence/guard/translation/automation-crossing edges labeled. This is the
default now, not an optional extra — a picture surfaces a self-consistency
error (an edge that should cross a subgraph and doesn't, a read model nothing
ever writes) in a way a table alone won't. Follow the node shapes, colors and
edge conventions in `references/mermaid-grid.md` exactly, so every model this
skill produces looks the same and matches the color legend this project's
EventStorming skills already use (blue commands, orange events, green read
models, lilac policies).

## Output: the Event Model

```
# Event Model — <process name>

## 0. Inputs
Domain stories used (labeled A, B, …); bounded contexts as given (verbatim);
found events (supplied vs. derived here).

## 1. Big Picture
The merged, ordered event list — the whole story, no swimlanes yet.

## 2. Swimlane strip
# | Event | Bounded Context | Source sentence(s)

## 3. Workflow slices
One subsection per slice, timeline order, each headed by its pattern tag,
its swimlane(s), a wireframe sketch, and its Given/When/Then (or Given/Shows).

## 4. The grid — redraw spec
Rows = swimlanes (contexts); columns = slices in timeline order, as a table —
plus a full color-coded Mermaid flowchart built per `references/mermaid-grid.md`
(one subgraph per context; wireframes, commands, events, read models and
policies all drawn and classed; sequence/guard/translation edges labeled).
Both, always — not the diagram only on request.

## 5. Information-completeness check
Every Command/Read-Model field, its source event, and any that don't trace.

## 6. Gaps & open questions
Unassigned events, empty lanes, missing views/automations, contradictions.

## 7. Contested calls & alternatives
Slices that could use a different pattern, disagreements between stories, and
the question that would settle each.
```

Adapt depth to the ask: "just give me the timeline" gets §1–2 and a short §4;
a build-ready spec gets everything, especially §5 and §3's Given/When/Thens.
Always keep §1 and §2 even in a short answer — they're what makes the rest
checkable.

For a full worked example (a six-sentence rescue story, three given
contexts, and found events reused from elsewhere in this project) end to end
through every section, see `references/worked-example.md` — read it first
when unsure how much detail a section needs.

## Working with neighbouring skills

- **No domain story yet** → `domain-story-seeder` drafts one; hand its output
  straight into Step 1 here.
- **No bounded contexts yet** → `domain-story-context-finder` proposes a cut
  from the same story; use its output as this skill's required input.
- **No candidate events, and the user has no EventStorming board either** →
  derive them yourself in Step 2 the way `domain-story-event-seeder` does
  (this skill borrows that method rather than re-implementing it — read it if
  a translation call in Step 2 feels ambiguous).
- **The story itself looks shaky** (CRUD verbs, collapsed actors, hidden
  branches) → say so briefly and point at `domain-story-critic` before
  building a model on top of it.
- **A live EventStorming board exists instead of a story** →
  `event-storming-interpreter` reads it into the same kind of buildable brief;
  its aggregates, policies and read models are good source material for this
  skill's slices if the user has both.
- **Rules the commands must enforce** → `event-storming-invariant-finder`
  derives the guards that belong in each State Change's Given.
- **Publishing the events or commands as a contract** → `asyncapi-spec-author`
  for the event stream, `openapi-spec-author` for a command API, or
  `protobuf-model-author` if the wire format is protobuf.
- **Which context is worth building well** → `core-domain-chart-author` /
  `core-domain-chart-critic` place the contexts this model organizes by.

## Reference files

- `references/event-modeling-method.md` — the four patterns in depth with
  more examples of each, the Given/When/Then and Given/Shows formats, the
  information-completeness check worked in detail, and common pitfalls
  (query/command confusion, present-tense events, over-built wireframes,
  timeline slicing that ignores recurrence).
- `references/mermaid-grid.md` — the exact node shapes, colors, subgraph
  layout, and edge conventions (sequence, guard, read-model write,
  translation, cross-context automation trigger) for rendering output §4 as a
  full Mermaid flowchart. Read this every time you reach Step 9 — it's what
  keeps every Event Model this skill produces looking the same.
- `references/worked-example.md` — a complete Event Model built end to end
  from a short domain story, a given three-context cut, and a found-event
  list, through every output section including the completeness check, the
  contested calls, and a full Mermaid rendering of the grid.

## References

A. Dymitruk, *Event Modeling*, eventmodeling.org — the technique's home,
including the four-pattern breakdown (State Change, State View, Automation,
Translation) and the Given/When/Then specification convention this skill
follows.

S. Hofer and H. Schwentner, *Domain Storytelling: A Collaborative, Visual, and
Agile Way to Build Domain-Driven Software.* Boston, MA, USA: Addison-Wesley,
2022. ISBN 978-0-13-745891-2.

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003.