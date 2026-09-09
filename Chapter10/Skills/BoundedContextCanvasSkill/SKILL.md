---
name: bounded-context-canvas-author
description: >-
  Write a Bounded Context Canvas (DDD Crew) for every bounded context on a
  context map — one Markdown file per context, each carrying the canvas as a
  Mermaid flowchart: inbound collaborators and their messages on the left,
  purpose, classification, domain roles, ubiquitous language and business
  decisions in the middle, outbound messages on the right, assumptions, metrics
  and open questions beneath. Derives inbound/outbound from the map's own border
  contracts and business decisions from an invariants sheet — never inventing a
  field, and reporting every outbound message no other canvas claims as inbound. Use whenever someone has a
  context map, a list of bounded contexts, an EventStorming board with bubbles or
  a domain-story cut and wants canvases, a canvas per context, "one file per
  bounded context", or "turn our context map into bounded context canvases" — and
  whenever someone asks to document what each bounded context owns, accepts and
  emits. Trigger even when nobody says "Bounded Context Canvas" or "Mermaid".
author: Annegret Junker
---

# Bounded Context Canvas Author

A context map says how contexts **relate**. A Bounded Context Canvas says what
one context **is**: what it is for, what it accepts, what it emits, what words
it owns, and what it refuses. The map is the network; the canvas is the node,
written out.

This skill turns a context map into **one canvas file per bounded context**,
each with the canvas drawn as a Mermaid flowchart. The set is the deliverable —
canvases are only trustworthy as a set, because the thing that makes them true
is that **A's outbound messages appear on B's canvas as inbound**. A folder of
canvases that don't reconcile is a folder of wishes.

Four commitments make the output worth taking into a room:

- **Every cell traces to something.** A field derived from the source is marked
  `(derived)`, a field the source states is `(given)`, a field you are proposing
  is `(proposed)`, and a field with nothing behind it stays **empty and labelled
  `(unknown)`**. A canvas with all twelve boxes plausibly filled and no
  provenance is the failure mode of this technique: it reads as agreed, and
  nobody agreed to any of it.
- **Messages come from border contracts, not from imagination.** Each crossing
  on the map produces exactly two canvas entries — outbound on one side,
  inbound on the other. If the map records no crossing, the canvas records no
  message, and the silence is a finding.
- **One canvas per context, however often the context was drawn.** A context
  that recurs along a timeline is one node and one file. Recurrence belongs in
  the purpose line ("serves both booking and return"), not in a second file.
- **Canvases reconcile or the mismatch gets reported.** Never quietly add an
  inbound entry to make a pair line up. An unmatched message is either a missing
  edge on the map or a misread producer, and both are worth more than a tidy
  set.

## What goes on a canvas

Twelve fields, in the DDD Crew v5 layout. `references/canvas-fields.md` has each
one in full — the vocabulary it draws on, where to source it, and how it fails.

| Field | One line | Usual source |
|---|---|---|
| **Name** | the context, spelled exactly as the map spells it | context map node |
| **Purpose** | why this context exists, in a sentence a domain expert would say | map's per-context responsibility |
| **Strategic classification** | domain type · business model · evolution | Core Domain Chart, else `(unknown)` |
| **Domain roles** | what kind of context it behaves like | derived from its own behaviour |
| **Inbound communication** | collaborator → message it handles | border contracts, incoming |
| **Outbound communication** | message it emits → collaborator | border contracts, outgoing |
| **Ubiquitous language** | the terms it owns, plus terms it borrows differently — drawn as a term graph | Visual Glossary, partitioned |
| **Business decisions** | the rules it enforces, in business words | invariants sheet, policies |
| **Assumptions** | reading decisions you made deriving this canvas | your own workings |
| **Verification metrics** | how anyone would know it works | almost never supplied — say so |
| **Open questions** | what would change this canvas | source's open questions, filtered |

## Workflow

### Step 1 — Gather what exists, and refuse to invent a cut

Two inputs, and **ask for both before starting**:

- **The context map — required.** Node names, and for each border, direction and
  what crosses.
- **A Visual Glossary — always ask.** The canvas has a whole field for the
  team's agreed vocabulary, and a glossary is the only artifact that actually
  holds it. Ask even when the user has not mentioned one; it is cheap to ask and
  the field is unusable without it. If none exists, say what the ubiquitous
  language field will be instead — terms reconstructed from the map's term
  ledger, marked `(derived)` rather than `(given)` — and offer
  `visual-glossary-interpreter` if they have a picture of one.

Anything else is a bonus that fills specific fields:

| If you also have | It fills |
|---|---|
| an invariants sheet (`event-storming-invariant-finder`, `event-model-invariant-finder`) | business decisions, and the states behind them |
| a Core Domain Chart | strategic classification — the field guessed most often and known least |
| the EventStorming board or Event Model behind the map | message types, actors, policies |
| a pivotal-event cut or domain-story cut | purpose lines, and the contested calls to carry into open questions |

**If there is no context map**, stop and produce one first —
`event-storming-context-mapper` from a board, `domain-story-context-finder` from
stories, `pivotal-event-boundary-finder` from a timeline. Canvases drawn on a cut
invented inside this skill will be argued about as canvases when the argument is
really about the cut. If the user wants to proceed anyway, mark every canvas
`Mode: provisional cut` in its header.

### Step 2 — Roster the contexts

One row per **distinct** context, collapsing recurrence. Include off-board and
external contexts the map drew dashed — they get a canvas only if the user wants
one, and if they do, most fields will be `(unknown)`, which is itself the
message. Say in the roster which contexts will get files.

### Step 3 — Build the message ledger before writing any canvas

This is the step that makes the set reconcile. One row per crossing, across the
whole map:

| From | To | Message | Type | Mechanism | Evidence |
|---|---|---|---|---|---|
| Rack management | Bicycle distribution | Bicycle unlocked | evt | event, via ACL | sticky on the dashed arrow |
| Bicycle distribution | Rack management | Reserve a bicycle | cmd | *(undrawn)* | **gap** — nothing crosses on the map |
| Accounting | Member Management | Monthly fee payed | evt | event, via ACL | sticky on the dashed arrow |
| *off-board* | Riding | Geo data | qry | *(unknown)* | read model with no writer |

Then **type each message**, because the type is what tells a reader whether this
context decides or merely reacts:

- **cmd** — an instruction this context may refuse. Someone is asking it to
  change something.
- **qry** — a question it answers with data and no state change.
- **evt** — a fact already true. It cannot be refused, only reacted to.

Rules that settle most cases: a crossing named in the past tense is an event; a
crossing that the downstream *reads on demand* is a query it issues (outbound
for the reader, inbound for the owner); a crossing the map calls "published" is
an event. When the map genuinely doesn't say, write `?` and put it in open
questions. A guessed message type quietly decides the coupling.

**Direction trap.** Message direction is not dependency direction. An event
published by upstream A and consumed by downstream B is **outbound for A and
inbound for B** — even though every arrow on the context map points A → B. A
query B issues to A is **outbound for B**, though B is the downstream. Derive
message direction from who emits, never from the map's arrowheads.

### Step 4 — Partition the glossary by context

A Visual Glossary describes the whole domain. A canvas needs the slice of it
that belongs to **one** context, so partition it before writing anything, using
the map's term ledger as the authority on ownership:

| Glossary term | Owner (writer, per the ledger) | On this canvas |
|---|---|---|
| written by this context | this context | **owned** — exact spelling, plus its cardinalities |
| read here, written elsewhere | another context | **borrowed** — with the owner's sense stated beside this one |
| written by nobody on the map | *nobody* | **a finding**, not a term — an off-board owner, or a context missing |
| never referenced by this context | elsewhere | not on this canvas at all |

Three things fall out of the partition and none should be skipped:

- **Cardinalities are candidate business decisions.** A glossary edge saying a
  Meal Plan selects `1..*` Recipe is the rule "a plan cannot settle empty" in
  another notation. Carry it into the business decisions field, marked with the
  glossary's own confidence — `(given)` if the glossary states the cardinality,
  `(inferred)` if the glossary itself marked it inferred.
- **Terms this context uses that the glossary does not define** go in the patch
  list for the glossary, and in open questions. They are usually the most
  interesting words on the board.
- **Glossary terms no context owns** are a finding about the map, not a gap in
  the canvas. Report them once for the set rather than on every canvas.

**Do not paste the glossary onto every canvas.** A ubiquitous language field
that lists all the domain's terms has skipped this step, and it quietly asserts
that every context shares one model — the exact claim bounded contexts exist to
deny.

### Step 5 — Fill the other fields, per context

Work `references/canvas-fields.md` field by field. Two habits do most of the
work:

- **Copy the source's spelling exactly**, including its inconsistencies. If the
  map says *Bicycle* in one place and *Bicylce* in another,
  either the map already resolved it or the canvas records both and asks.
- **Leave the box empty when the source is silent.** Verification metrics are
  almost never on a context map; write `(unknown) — none supplied` and, if you
  have a candidate worth proposing, mark it `(proposed)` on its own line. The
  same goes for strategic classification without a Core Domain Chart.

### Step 6 — Render the canvas

Use the canonical template in `references/mermaid-template.md` — it fixes the
layout, the class definitions and the label conventions so a folder of canvases
looks like one artifact rather than twelve. Read it before writing the first
diagram; it also carries the Mermaid syntax traps (label quoting, reserved node
ids, `direction` inside subgraphs with external edges) that cause most render
failures.

Shape, in one sentence: three columns left to right — inbound, the context's
stacked panels, outbound — with the meta panels beneath, and each collaborator
box listing its own messages one per line (`evt · Bicycle unlocked`), so the
ledger from Step 3 is readable off the picture. The **ubiquitous language panel
is itself a small term graph** — the glossary slice from Step 4, drawn with its
cardinalities on the edges rather than flattened into a list. The reference
explains why the wrapper subgraph and the subgraph-level column arrows are
load-bearing; a plain `flowchart LR` produces this shape in source and something
else on screen.

**Keep the frontmatter config block the template opens with.** It pins the font
Mermaid measures with and adds node padding, which is what stops a viewer whose
CSS font is taller from clipping the last line of every label. Two habits go
with it: never hand-break prose — write it as one sentence and let it wrap — and
cap any panel at four lines, moving the overflow into the field table.

### Step 7 — Write one file per context

Default naming, in an output folder named for the map:

```
bounded-context-canvases/
├── bicycle-distribution.canvas.md
├── rack-management.canvas.md
├── accounting.canvas.md
└── README.md              # optional index: roster + the message ledger
```

The slug is the context name in kebab-case. The index is optional and is **not**
a canvas — it holds the roster, the whole message ledger and the reconciliation
report, which belong to the set rather than to any one context. Offer it; skip
it if the user asked strictly for one file per context.

Each file follows the template at the end of this skill.

### Step 8 — Check, then report what doesn't line up

```bash
python scripts/check_canvases.py bounded-context-canvases/
```

Needs only Python 3. It parses each file's Mermaid block and checks two
different things: **syntax** (fence present, `subgraph`/`end` balanced, all four
columns present, labels quoted, no reserved node ids, messages well-formed
inside the collaborator boxes) and **symmetry** (every
outbound message matched by an inbound message with the same name and type on
the named collaborator's canvas). Pass `--partial` when checking a subset, or a
collaborator whose canvas simply isn't in the folder reads as an error.

If a canvas still fails in the target tool, run the real parser — it is the last
word, and needs `npm install mermaid jsdom`:

```bash
node scripts/parse_with_mermaid.mjs bounded-context-canvases/*.canvas.md
```

Fix every syntax error. **Then look at one rendered canvas** in the tool the team
will read it in, the first time a new shape appears: clipped labels are invisible
to every check that reads source, and a canvas whose boxes are a line short is
wrong on screen while being right in the file.

Do **not** fix asymmetries by editing a canvas — write them up. Each one is one of:

- **a missing edge on the context map** — the most valuable output this skill
  produces, because the map looked complete;
- **a misread producer** — the crossing is real and pointed the wrong way, which
  means at least one border contract is backwards;
- **an off-board collaborator** — legitimate, if the canvas says so;
- **a naming slip** — same message, two spellings. Report both spellings; fix
  the wall, not the file.

### Step 9 — Present

The files, then a short covering note: how many canvases, which fields came out
`(unknown)` across the set, the asymmetries, and the three questions that would
fill the most boxes. Resist summarizing each canvas in prose — the files are the
deliverable and a summary of twelve canvases is read by nobody.

## Output: one canvas file

````markdown
# Bounded Context Canvas — <Name>

> **Source:** <artifacts> · **Mode:** derived from the team's cut | provisional cut
> **Provenance:** `(given)` stated by a source · `(derived)` read off one ·
> `(proposed)` mine, confirm it · `(unknown)` nothing to derive from.

## Canvas

```mermaid
<the canonical template, filled>
```

## Purpose  (derived)
One or two sentences. What this context is for, in the business's own words.

## Strategic classification  (unknown / given)
Domain · Business model · Evolution — each with the evidence or `(unknown)`.

## Domain roles  (derived)
One to three roles, each with the behaviour that argues for it.

## Inbound communication  (derived)
| Collaborator | Message | Type | What it carries | Evidence |

## Outbound communication  (derived)
| Message | Type | Collaborator | What it carries | Evidence |

## Ubiquitous language  (given / derived)
Drawn as a term graph on the canvas; repeated here in words, because the
picture caps what fits and a borrowed sense needs a sentence.
| Term | What it means *here* | Owned or borrowed | Cardinality |
Borrowed terms name the other context's sense too — that difference is the
reason the border exists.

## Business decisions  (derived)
The rules this context enforces, in business words, each traceable. Rules that
need another context's data are **not** business decisions here — they are
policies, and they belong in open questions or on the map.

## Assumptions  (derived)
The reading decisions behind this canvas, so the room can overturn them.

## Verification metrics  (unknown)
`none supplied` — plus any `(proposed)` candidate, marked as such.

## Open questions
One sentence each, naming the field it would settle.

## Border reconciliation
Which outbound messages are claimed as inbound by which canvas, and which are
unmatched — with the reason.
````

Adapt depth to the request. "Just draw them" gets the canvas and the two
communication tables — but never drop **provenance markers** or **border
reconciliation**, because a canvas without provenance reads as agreement, and a
set without reconciliation reads as a working system.

**Review mode.** If the team already has canvases, do not overwrite them.
Reconstruct each from the map, then report agreement, disagreement and omission
separately, and say which of their fields the map cannot support.

## Traps worth checking before you publish

- **The full canvas.** Twelve boxes filled, nothing marked unknown. Verification
  metrics and strategic classification are the tells — almost no context map
  supplies either.
- **Inbound = "things upstream of me".** No: inbound is *messages I handle*. A
  query you issue is outbound even though the answer flows back to you.
- **The CRUD canvas.** Inbound messages called *Create X*, *Update X*, *Get X*.
  That is a table with an HTTP interface, not a bounded context. Go back to the
  board for the business's own verbs; if there genuinely aren't any, say the
  context may be a component rather than a context.
- **Business decisions that aren't decisions.** "Sends a notification" is
  behaviour. A business decision is something the context **refuses**, or a
  choice it makes on the business's behalf.
- **Ubiquitous language as a noun dump.** The value is in the terms this context
  models *differently* from its neighbour. A term list with no borrowed terms
  usually means the borrowing was never looked for.
- **The glossary pasted whole onto every canvas.** If two canvases carry the
  same term list, the partition in Step 4 did not happen. Every term on a canvas
  is either owned here or borrowed from a named owner.
- **A canvas per timeline appearance.** Three files for a context drawn three
  times. One context, one file.
- **Ubiquitous language normalized across canvases.** Reconciling *Member* and
  *Commuter* into "User" deletes the translation the border exists for.
- **Domain roles as decoration.** Every context tagged *Gateway*. A role must
  predict something about the context's behaviour, or leave it off.
- **The canvas that is really a context map.** If the middle panels are thin and
  all the content is in the two message columns, you have redrawn the map. The
  canvas's own contribution is language, decisions and purpose.
- **Diagram and text disagreeing.** The Mermaid block and the field tables are
  two renderings of one canvas. Generate the tables first, then the diagram from
  them, so the diagram can't drift.

## Working with the neighboring skills

- **No context map yet** → `event-storming-context-mapper`,
  `domain-story-context-finder`, or `pivotal-event-boundary-finder`.
- **Business decisions field is thin** → `event-storming-invariant-finder` or
  `event-model-invariant-finder`. Their per-aggregate invariants are business
  decisions already written; their §6 demoted cross-context rules are the ones
  that must *not* go in this field.
- **Strategic classification is `(unknown)`** → `core-domain-chart-critic` /
  `core-domain-chart-author`. The canvas is the best input a chart can get, and
  the chart is the only honest source for this field.
- **No Visual Glossary, or only a picture of one** → `visual-glossary-interpreter`
  turns it into a term catalogue with cardinalities, which is what Step 4 needs.
  `domain-story-glossary-consistency` is worth running first if the glossary and
  the stories behind the map have drifted, because the canvases will inherit the
  drift.
- **A canvas is about to become an interface** → `openapi-spec-author` for the
  cmd/qry rows, `asyncapi-spec-author` for the evt rows. The two communication
  tables are a spec outline in another notation.

## Bundled resources

- `references/canvas-fields.md` — every field in full: what belongs in it, the
  DDD Crew vocabularies for domain roles, business model and evolution, where to
  source it from, and how each field is typically got wrong. **Read this before
  filling the first canvas.**
- `references/mermaid-template.md` — the canonical Mermaid canvas: full
  template, class definitions, label conventions, the dense-canvas and
  diagram-only variants, and the Mermaid syntax traps. **Read this before
  writing the first diagram.**
- `references/worked-example.md` — a five-context bike-sharing map worked end
  to end: the map as read, the message ledger, one canvas in full, the other four
  in outline, and the reconciliation report where three clean pairings still
  expose three undrawn edges. Read this first when unsure how deep to go.
- `scripts/check_canvases.py` — syntax and cross-canvas symmetry checker.
  Python 3, no dependencies.
- `scripts/parse_with_mermaid.mjs` — optional authoritative parse against the
  real Mermaid engine, for when a canvas passes the checker and still won't
  render.

## References

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003 — bounded contexts and the strategic
patterns the canvas records.

N. Tune and DDD Crew, *Bounded Context Canvas* (CC BY 4.0).
https://github.com/ddd-crew/bounded-context-canvas — the twelve fields, the v5
layout, and the domain-role vocabulary used here.

V. Vernon, *Implementing Domain-Driven Design.* Boston, MA, USA:
Addison-Wesley, 2013 — context relationships and the contracts across them.

S. Wardley, *Wardley Maps* — the evolution vocabulary (genesis, custom built,
product, commodity) the classification field borrows.