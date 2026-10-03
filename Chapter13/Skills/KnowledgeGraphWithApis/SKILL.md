---
name: domain-knowledge-graph
description: >-
  Build and grow ONE browsable knowledge graph (Turtle/RDF) from DDD modelling
  artifacts — brainstorm, BMC, Wardley/Impact/Capability maps, Core Domain
  Chart, Domain Stories, Visual Glossaries (incl. refined per-context redraws),
  EventStorming boards, Event Models, Bounded Context Canvases, API Product
  Canvases, ADR logs — and each context's OpenAPI (sync) and AsyncAPI (async)
  specs. Keeps every spelling and provenance; reports what connects,
  contradicts, is orphaned, and which commands/events have a contract. Use for
  "make a knowledge graph of…", "add our canvas / API spec / glossary to the
  graph", "does our API match the board" — any request to combine, reconcile
  or navigate two or more artifacts, even without the words "knowledge graph".
author: Annegret Junker
---

# Domain Knowledge Graph

A workshop leaves behind a pile of pictures. A brainstorm photo, a canvas, a
map, a wall of stickies, a glossary — each one true, each one partial, and
nothing holding them together except whoever was in the room. Six weeks later
nobody can answer *"whatever happened to that idea?"* or *"does the thing we are
building still serve the segment we drew?"*

This skill keeps one graph that all of them feed. It is a **map to browse**, not
a database to query blind: the deliverable is a `.ttl` file that happens to be
queryable, plus rendered views that let a person walk from a goal to a sticky.

Three commitments make it worth trusting.

- **Never invent a node.** A gap between two artifacts is the finding. A graph
  that quietly bridges it has destroyed the only evidence that anyone noticed.
- **Never merge silently.** Two artifacts using one word is a *hypothesis*.
  Merges go in a ledger the room confirms; the graph keeps every spelling and
  which artifact used it.
- **Never resolve a contradiction on your own.** When the canvas and the board
  disagree, both statements stay, joined by `dkg:contradicts`. That edge is
  usually the most valuable thing in the file.

## Ordering

Any artifact may arrive first, and none is required. The usual ladder runs
brainstorm → BMC / Wardley / Impact Map → Domain Stories / Visual Glossary →
EventStorming board or Event Model, then — once a context has a name — a
Bounded Context Canvas per context, an API Product Canvas per context (the
interface sketched on stickies: aggregates with their operations, events in and
out, protocols, quality requirements), and finally that context's API
specifications: an OpenAPI document for what can be called, an AsyncAPI
document for what it publishes and consumes — after which the context's Visual
Glossary is usually redrawn from the spec, one refined glossary per context.
A small ADR log accumulates
alongside the whole ladder. The graph gets more useful as it descends, because
each layer says where the layer above landed — and the specs are the first
layer that says what will be *built* rather than what was modelled, which is
why the graph keeps them linked to the model rather than merged into it. But the skill
works from whatever exists. If someone starts with a board and adds a canvas
three months later, that is a normal ingest, and the interesting output is the
same: what connected, what did not.

---

## Workflow

### Step 0 — Seed or grow?

Look for an existing graph: a `.ttl` in the project, the working directory, or
the uploads. Say which mode you are in, out loud, in one line.

- **Seed** — no graph yet. Copy `assets/dkg.ttl` next to the new
  `graph.ttl`; the project graph starts with `owl:imports` of it and nothing
  else. Ask for the project's short name; it sets the base IRI.
- **Grow** — a graph exists. Load it. Read the artifact register (§Register
  below) before looking at the new artifact, so you know what the graph already
  calls things. **Do not rewrite existing statements.** Growth is additive; a
  correction is an explicit, named, reported act.
- **Revise** — a graph exists *and* the new artifact is a redraw of one already
  in the register (glossary v2, the board after the next session, the ADR log
  with new rows). Neither seed nor grow fits: grow would duplicate the whole
  vocabulary, and rewriting would lose what v1 said. Read
  `references/reingest-revision.md`; the new artifact gets `dkg:revisionOf`,
  and the report is a diff — added, removed, changed.

**Revisions are detected, not assumed.** Whenever the register already holds an
artifact of the same subclass as the one arriving (a second `dkg:VisualGlossary`,
a second `dkg:EventStormingBoard`, a second canvas for the same context, an ADR
log with the same ID prefix), run the overlap test before choosing a mode:
transcribe first (Step 2), then run

```bash
python3 scripts/check_graph.py graph.ttl --overlap "<existing artifact>" labels.txt
```

with one transcribed label per line (or `--overlap all labels.txt` when the
register holds several artifacts of the type, to find which one to compare
against). An API specification has a shortcut: an OpenAPI `x-api-id` already
on a `dkg:Api` in the graph is a revision whatever the overlap says;
`api_inventory.py --graph` reports it. It reports what share of the new labels already exist as nodes
sourced to that artifact. It reads the overlap
both ways — *share* of the new already held, *cover* of the held present in
the new — and says **revision, partition, superset, ask, or new**; the table is
in `reingest-revision.md` §0. Say which mode you
chose and why, in one line, before emitting anything. Grow mode on a revision is
the single most expensive mistake this skill can make — it doubles the
vocabulary and every later merge ledger inherits the duplicates. Brainstorms are
the exception: a second session is always a new artifact. A third case, a
domain-wide artifact split into one per context, is a **partition** —
`reingest-revision.md` §0b — and ownership between per-context glossaries
follows `ingest-visual-glossary.md`.

Before any of these, if the user's question is *"what does artifact X need to
change?"*, that is not an ingest at all — run `views.py --patches X` (Step 6)
and hand the patch list to the skill that redraws that artifact type.

If several artifacts are supplied at once, ingest them **one at a time, in the
order given**, and report after each. A batch ingest that reports once has
hidden every merge decision inside it.

### Step 1 — Identify the artifact and read its reference file

| Artifact | Reference file |
|---|---|
| Brainstorm / ideation board / sticky photo | `references/ingest-brainstorm.md` |
| Business Model Canvas | `references/ingest-business-model-canvas.md` |
| Wardley Map | `references/ingest-wardley-map.md` |
| Impact Map | `references/ingest-impact-map.md` |
| Capability Map, Core Domain Chart | `references/ingest-capability-and-core-domain.md` |
| Domain Story (picture, egon.io export, numbered sentences) | `references/ingest-domain-story.md` |
| Visual Glossary | `references/ingest-visual-glossary.md` |
| EventStorming board | `references/ingest-event-storming.md` |
| Event Model | `references/ingest-event-model.md` |
| Bounded Context Canvas (DDD Crew; Mermaid, PNG or the Markdown behind it) | `references/ingest-bounded-context-canvas.md` |
| Small ADR log (a decision table: date · id · status · question · options · decision) | `references/ingest-small-adr.md` |
| API Product Canvas (one context: sync side, async side, quality requirements, notes) | `references/ingest-api-product-canvas.md` |
| API specification for one context — OpenAPI (sync), AsyncAPI (async), gRPC `.proto` or GraphQL SDL (sync) | `references/ingest-api-specification.md` |
| Refined Visual Glossary — one context, redrawn from its schema by `visual-glossary-updater` (model YAML + changelog + SVG) | `references/ingest-visual-glossary.md`, section "A refined glossary" — always a revision |

Read the one that applies. Read two if the input is two things. If the input is
a *derived analysis* rather than a workshop artifact — an invariant sheet, a
pivotal-event cut, a context cut — see "Ingesting analyses" below. A Bounded
Context Canvas is a derived analysis that carries its own per-field provenance
markers; its reference file says how those markers relax the rule.

If the artifact type is unclear, ask. Guessing wrong quietly mis-types thirty
nodes.

### Step 2 — Transcribe before you model

Write out what is on the artifact, verbatim, in its own spelling, before any
IRI is minted. Numbered lists for stories, tables for boards, block by block for
a canvas. Show this transcription to the user. Two reasons: the graph inherits
every transcription error silently, and the user is the only one who can catch
them.

Flag illegible stickies as illegible. Do not complete a half-visible word.

An API specification is machine-readable, so its transcription is a script:
`python3 scripts/api_inventory.py spec.yaml` prints the operations, channels,
messages and schemas as tables. So is a refined glossary's model file:
`python3 scripts/glossary_inventory.py model.yaml` prints its terms by colour
group and its edges. Show those instead of retyping them; the judgment in
either ingest is all in Step 3. An API Product Canvas is a photo of stickies
and is transcribed by hand like any canvas.

### Step 3 — Resolve identity, and write the merge ledger

For every label in the transcription, decide: **new node, or an existing one?**
Full method in `references/merging.md` — read it on the first grow-mode ingest
of any session. The short version:

- **Auto-merge** only on *exact label match, same spine class, and no
  contradicting property*. `Cook` (story actor) into `Cook` (board actor): yes.
- **Propose, never auto-merge**, on: near-matches (`Mise en place` /
  `Mise-en-place`),
  singular/plural, one word inside another (`Pictures` / `Catastrophe
  Pictures`), and anything crossing spine classes (a BMC *Key Activity* named
  the same as a board *Command*). Write `dkg:proposedSameAs` and list it.
- **Never merge** two nodes that carry contradicting properties. Write both,
  join with `dkg:contradicts`, and put it in the report.

The **merge ledger** is a table in the report, one row per decision:
`label seen · existing node · decision · evidence · what changes if wrong`.
Auto-merges are listed too — a silent merge is not auditable.

Watch particularly for **one word, two concepts** (the same label doing
different work in two artifacts) and **two words, one concept** (a rename across
a boundary). The first is a `dkg:contradicts` waiting to happen; the second is
`dkg:renamedTo` and is boundary evidence. Both are findings, not bookkeeping.

### Step 4 — Emit Turtle

Follow `references/ontology.md` for IRI shape, required properties, and the
provenance pattern. Non-negotiable per node:

- one `rdf:type` from the spine, plus the artifact-specific subclass;
- `skos:prefLabel`, and `skos:altLabel` for every other spelling seen;
- at least one `dkg:source`, with `dkg:locator` where the artifact has positions;
- exactly one `dkg:confidence` — `dkg:OnArtifact`, `dkg:Implied` or
  `dkg:Inferred`. If you are unsure which, it is `dkg:Inferred`.

Append to `graph.ttl` under a comment banner naming the artifact and date. Keep
ingests in ingest order in the file: the file is read by humans too, and its
order is a history.

For a spec, `api_inventory.py --ttl --graph graph.ttl` drafts the skeleton —
nodes, verbatim literals, and `dkg:specifies` links for exact matches only —
with every looser match left as a `# PROPOSE` comment; when an API Product
Canvas already minted the context's Api of that style, the draft enriches that
node and merges operations by exact label. For a refined glossary,
`glossary_inventory.py --ttl --graph graph.ttl --revision-of <earlier>` drafts
the revision diff — unchanged / changed / added / removed — and leaves renames
to the ledger. Decide those before appending; neither script writes to the graph.

### Step 5 — Check

```bash
python3 scripts/check_graph.py graph.ttl
```

It parses, then reports: untyped nodes, nodes with no source or no confidence,
dangling references, contradictions, proposed merges awaiting a decision,
orphans (nodes nothing points at) and near-duplicate labels the ingest may have
missed. Once a spec is in, it also reports API coverage per context (which
commands and events have a contract), unlinked operations / messages / schemas,
style mismatches (an event behind a synchronous call, a query on a channel) and
borders a `receive` operation draws. **Fix the errors; report the warnings — do not fix them by inventing
edges.** An orphan is a finding. Requires `rdflib` (`pip install rdflib
--break-system-packages`).

### Step 6 — Render the browsable views

```bash
python3 scripts/views.py graph.ttl --out views/
python3 scripts/views.py graph.ttl --patches "Visual Glossary Enhanced 08"
```

The second form is the **reverse direction**: for one artifact, what the rest
of the graph says it should change — decisions that answer questions about its
terms, spellings other artifacts settled on, relationships drawn elsewhere
between terms it holds, and questions about its terms nobody has closed. It is
a patch *list*, with provenance per row. Redrawing is the job of
`visual-glossary-updater`, `bounded-context-canvas-author` or the room; this
skill says *what* and *why*, and stops there, because a graph that edits its own
sources can no longer be checked against them.

Writes an `index.md` plus one Mermaid lens per perspective — strategy, capability,
language, flow, interface, traceability — and an orphan report. The interface
lens is the per-context view of the APIs: sync operations, async channels and
messages, send/receive direction, and dotted edges to the model elements each
contract specifies, with a coverage table underneath. Details and hand-written
SPARQL recipes in `references/browsing.md`. Present the `.ttl` **and** the views;
the Turtle is the source of truth and the views are how anyone actually reads it.

### Step 7 — Report

Short, and in this order. This is the part people read.

1. **How corroborated the graph now is** — "n of m nodes rest on a single
   artifact". First, always, before anything that sounds like a finding. Four
   sessions in, most graphs are still several opinions stapled together, and
   every line below should be read in that light. `views.py` prints this in the
   register; repeat it in the report rather than pointing at the file.
2. **What went in** — n nodes, m edges, by class.
3. **The merge ledger** — every identity decision.
4. **What newly connects** — the point of the whole exercise. Name the paths
   this artifact created that did not exist before: *"idea #7 from the brainstorm
   is now reachable from `Meal plan settled` via the Impact Map's deliverable."*
5. **What contradicts** — verbatim, both sides, with sources. Include any claim
   that *changed*: a capability called supporting in March and core in September
   is two dated assertions, and the change is the finding.
6. **What is orphaned** — nodes nothing else references, in either direction,
   and what each absence would mean.
7. **Open questions** — merges awaiting a decision, and anything the artifact
   raised. Never answer these yourself. Once an ADR log is in the graph, add
   which of the graph's open questions a Decision now `dkg:answers`, and which
   still have none.
8. **Interface coverage** — once an API Product Canvas or a spec is in the
   graph. Per context with an API: how many of its commands and inbound rows
   have an operation, how many of its events and outbound rows have a message,
   which contracts the model never named, every `receive` that draws a border
   no canvas drew, and — once both canvas and spec are in — where they
   disagree (protocol, an operation on one side only). `check_graph.py` prints
   these; repeat them rather than pointing at the file.
9. **Glossary against schema** — once a refined glossary is in: terms the
   redraw lifted from the schema, terms it kept despite the schema's silence,
   cardinalities that changed and whether they were drawn or lifted.

---

## The register

Keep a `dkg:Artifact` individual per ingest, with its type, date, a one-line
description and, where the source is a file, its name. The register is what
makes the graph auditable: every node traces to an artifact, every artifact to a
session. `scripts/views.py` renders it first in `index.md`.

## Ingesting analyses, not artifacts

Derived documents — an invariant sheet, a pivotal-event cut, a context cut, a
critic's report — may be ingested, with one rule: **everything from an analysis
is `dkg:Inferred` at best**, and its `dkg:source` is the analysis, never the
board the analysis read. Otherwise a proposal quietly acquires the standing of
something a team drew. The useful parts are usually `dkg:Question` nodes,
`dkg:proposedSameAs` candidates, and proposed `dkg:BoundedContext` nodes — all
of which are honest as proposals and dangerous as facts.

Two derived documents get their own reference files because they are structured
enough to ingest field by field: a **Bounded Context Canvas** (which marks each
field `(given)`/`(derived)`/`(proposed)`/`(unknown)`, and those markers map onto
`dkg:confidence`) and a **small ADR log** (whose rows are the only nodes in the
graph that *close* questions rather than raise them — `dkg:Decision`,
`dkg:answers`).

An **API specification** is neither a workshop artifact nor an analysis: it is
a design the team will build. What it says, it says on-artifact; what it says
about the model — that this operation *is* that command — is a claim, held as
`dkg:specifies` (`dkg:Implied` on an exact match, a reified `dkg:Inferred`
assertion otherwise) and never a merge. `ingest-api-specification.md` has the
rules; the short version is that a command and the endpoint that carries it are
two nodes from two worlds, and the graph's value here is being able to say
which commands have no endpoint and which endpoints have no command.

## What this graph cannot do

Say it, once, when handing over. A graph shows what the artifacts *said*. It
cannot tell you whether they were right, it has no opinion on whether a boundary
is well drawn, and a densely connected node is popular rather than important.
When the user's next question is an evaluative one, the answer is one of the
critic skills — `business-model-canvas-critic`, `wardley-map-critic`,
`impact-mapping-critic`, `domain-story-critic`, `core-domain-chart-critic` — and
the graph is the input, not the answer.

## Failure modes to resist

- **Tidying.** The temptation to make the graph coherent by dropping the
  awkward node. The awkward node is the deliverable.
- **Over-reification.** Reify an edge only when it carries provenance from more
  than one artifact, or is contested. Reifying everything makes the file
  unbrowsable and browsing is the stated purpose.
- **Inventing the spine.** If the brainstorm never named a goal, the graph has
  no goal. Do not promote an idea to a Goal because the shape looks nicer.
- **Silent normalisation.** `0..*` is not `*`, and an `AI Avatar` swimlane is
  not a `Grandma Avatar` sticky. Keep what each artifact wrote; put the agreed
  term in `skos:prefLabel` and every other spelling in `skos:altLabel`.
- **Answering the open questions.** The graph collects them. The room settles
  them — and when it has, the settlement arrives as an ADR row and a
  `dkg:answers` edge, not as a quiet edit to the Question.
- **Letting a canvas re-mint its context.** A canvas is about a context the
  graph almost certainly already has. Enrich the existing node; a second
  `:Ctx_CookingAssistance_BCC` is a duplicate with a nicer name. The same for
  a spec: *Rack Management API* is a `dkg:Api` inside `:Ctx_RackManagement`,
  not a new context.
- **Merging a contract into the model.** `reserveBicycle` is not the board's
  *Reserve a bicycle*; it is the POST that carries it. One `dkg:specifies`
  edge keeps both nodes and the question "which commands have no endpoint?"
  answerable. A merge would answer it wrongly forever.
- **Resolving a style mismatch.** When an OpenAPI operation specifies what the
  canvas typed `evt`, or an AsyncAPI message specifies a `qry` row, the two
  artifacts disagree about the nature of the crossing. Report it; do not retype
  the canvas, and do not drop the link.

---

## Reference files

- `references/ontology.md` — IRI shape, required properties, the provenance and
  reification patterns, worked Turtle snippets. Read before the first emit.
- `references/merging.md` — identity resolution, the merge ledger, the
  one-word-two-concepts and two-words-one-concept tests. Read on the first
  grow-mode ingest.
- `references/browsing.md` — the five lenses, what each is for, SPARQL recipes
  for the questions people actually ask.
- `references/ingest-*.md` — one per artifact type, per the table in Step 1.
  `ingest-api-specification.md` covers OpenAPI, AsyncAPI and the hand-transcribed
  sync formats, and documents `scripts/api_inventory.py`.
- `references/worked-example.md` — **optional, and not shipped.** If the project
  has one, it is a graph grown across several of its own artifacts, with the
  merge ledger and the contradictions it surfaced; read it when unsure how deep
  to go or how firmly to merge. Absent, the snippets in `ontology.md` and the
  per-artifact files carry the same guidance in smaller pieces.

`assets/dkg.ttl` — the vocabulary. Copy it beside every new project graph.

`scripts/api_inventory.py` — transcribes an OpenAPI / AsyncAPI file to tables,
writes its labels for the overlap test, and drafts the Turtle skeleton with
exact-match links against an existing graph. Needs `pyyaml`; `rdflib` for
`--graph`.

`scripts/glossary_inventory.py` — the same for a Visual Glossary model file
(the format `visual-glossary-updater` renders from): transcription, labels,
and with `--revision-of` the binned revision diff. Same dependencies.

## Working with neighbouring skills

- An API Product Canvas is drawn in a room (the Miro template); it is the
  sketch the spec authors work from, and the first artifact that mints a
  context's `dkg:Api` nodes.
- A refined glossary comes from `visual-glossary-updater` — hand it the
  context's earlier glossary and the spec the graph holds; ingest its model
  file, not a transcription of its picture.
- A spec to ingest usually comes from `openapi-spec-author` or
  `asyncapi-spec-author`, scoped to one context; `context-map-api-proposer`
  reads this graph to propose which borders need which style, and once the
  specs are ingested the graph can answer that question from the contracts
  themselves (the `interface` lens, and `dkg:protocol` on each Api).
- Field-level drift between a spec's schemas and the glossary is
  `schema-glossary-consistency`'s job; this graph holds schema *names* only.
- A spec that should change — an operation for a command it lacks, a message
  for an event it never publishes — is `views.py --patches "<spec>"`, handed
  to the spec's own author skill.
