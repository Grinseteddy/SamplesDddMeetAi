# Ingest — EventStorming detail board (Process-Level / Design-Level)

A Big Picture board explores a whole business. A **detail board** zooms into
*one* process or *one* bounded context and makes it rigorous: every orange
event gets its blue command, the command lands on a large-yellow aggregate (or
a yellow "system" sticky, or a pink external system), lilac policies say
*whenever … then …*, green read models say what the actor looked at before
deciding, and red hotspots say where the room disagreed. Brandolini's two
flavours:

- **Process-Level** — commands, actors, policies, read models and external
  systems around the events; no aggregates yet.
- **Design-Level** — additionally aggregates, sometimes with invariants
  written on them and state stickies beside them.

It arrives in one of two shapes, and often both:

1. **The board itself** — photo(s), Miro/Mural export, or a sticky list.
2. **A Detail Design Brief** — the nine-section output of
   `event-storming-detail-interpreter` (legend & flavour, transcribed flow,
   slice table, aggregates & command handlers, lifecycles, policies, read
   models, completeness findings, hotspots).

Read this file *instead of* `ingest-event-storming.md` when commands land on
aggregates or systems for one process. If the board is really Big Picture —
few or no commands, a whole business on one timeline — use that file.

---

## 0. The context gate — ask before anything else

A detail board is, by definition, about **one** bounded context (or one
process the team placed inside named contexts). That scope is the single fact
every `dkg:inContext` edge of this ingest depends on, and it is almost never
written on the board. So:

**If the user has not named the bounded context explicitly in the request,
ask for it — before transcribing.** Do not take it from the file name, the
board's title, the brief's heading, the context bubble a Big Picture board drew
around similar stickies, or the context the graph's other artifacts put these
labels in. Those are *candidates to offer*, not answers.

Explicit means the user's own words name a context: *"add our Rack Management
detail board"*, *"this is the Billing context"*, *"scope: Cook Assistance"*.
A board titled "Rental — design level" is not explicit; "Rental" may be a
process crossing three contexts.

Ask with `AskUserQuestion` (one question, plain text if the tool is absent):

- **Options** — up to three `dkg:BoundedContext` nodes already in the graph
  whose labels best match the board (the context most of its labels already sit
  in first), plus the board's own title if it names something the graph does
  not hold. "Other" lets the user type a new name.
- **One more option when the board looks like it crosses lanes** — *"A process
  across several contexts"*. If chosen, ask which region / swimlane / sticky
  range belongs to which context (§3, *Multi-context boards*).

Then:

| Answer | What happens |
|---|---|
| an existing context | `dkg:scopedTo` that node; every scoped node gets `dkg:inContext` it |
| a name the graph does not hold | mint `:Ctx_<Name>`, `dkg:confidence dkg:Implied`, sourced to this board, comment *"named by the user at ingest"* — **report it first**: a context that enters the graph through a detail board was never drawn on a map |
| a name that is a near-match of an existing context (`Rack Mgmt` / `Rack Management`) | do not mint; ask once more, offering the existing one. A second context node with a shorter name is the most expensive duplicate in the graph |
| several contexts | one `dkg:scopedTo` per context, and the region map from the user |
| "I don't know" | stop. Transcription may be shown, but nothing is emitted — a detail board without its context would fill the graph with commands and events that belong nowhere, and `check_graph.py` fails on it |

When the session is unattended and nobody can answer: do the transcription,
set out the candidate contexts with the evidence for each, and stop there. The
context is cheap to supply later and expensive to guess wrong.

Confidence of the scope itself: the user's explicit naming is recorded on the
artifact (`dkg:scopedTo`, `rdfs:comment "context named by the user"`). Each
node's `dkg:inContext` edge is then `dkg:Implied` in the node's own confidence
sense — the board is about one context, and the room said which. If a
context bubble **on the board** names the same context, the scope is on-artifact
and the comment says so.

---

## 1. Revision, refinement, or new?

Before transcribing, check the register.

| Register already holds | Arriving | Mode |
|---|---|---|
| a Big Picture `dkg:EventStormingBoard` covering this process | detail board | **grow**, and `dkg:refines` that board. It is the zoom, not v2: the Big Picture board stays the whole-business picture |
| a `dkg:EventStormingDetailBoard` scoped to **the same context** | detail board | run `check_graph.py --overlap` on its labels; ≥ ⅔ both ways → **revise** (`reingest-revision.md`) |
| a detail board scoped to a **different** context | detail board | **grow**. Shared labels across the two are border evidence, not lineage |
| the board itself, ingested | its Detail Design Brief | **grow**: the brief is a second, derived artifact about the same board (§5) |
| nothing | a brief without its board | ingest the brief (§5); report that the board is not in the register |

A Process-Level board redrawn at Design-Level after the next session is a
**revision**, not a new artifact: same scope, same events, aggregates added.
Its diff is mostly *added* aggregates and `handledBy` edges, plus the commands
whose receiving system became an aggregate — `dkg:supersedes` on those edges.

---

## 2. Transcribe

Slices, not events. A slice is one unit of the grammar:

```
read model → actor (or policy / clock / external system) → command
  → aggregate / system → event(s) → read models updated
```

One table, in board order, left-to-right:

`# · trigger kind · trigger (actor / policy / time / external) · command ·
receiver (aggregate / system / external) · event(s) · outcome per event ·
read models consulted · read models updated · marks`

- **trigger kind** — `actor`, `policy`, `time`, `external`, or blank when the
  board shows none. Never fill a blank because only one actor appears
  elsewhere; that is an `Implied` you write down, not a blank you hide.
- **outcome** — `success` / `failure` per event, read off the board's own
  branching (two events below one command, a red-edged sticky, a
  `… declined` / `… rejected` wording). Wording alone makes it `Implied`.
- **marks** — `[?]` illegible, `recurs at #n`, `branch of #n`, clock icon,
  rotated sticky.

Then, outside the table:

- **legend as read**, and **flavour** — *Process-Level* when no large-yellow
  aggregate appears, *Design-Level* when one does; yellow "system" stickies
  that may or may not be aggregates are **aggregate candidates**, said so;
- **policies**, verbatim, as `whenever <event> [and <condition>] then
  <command>`, and the read model each condition consults if the board draws
  one;
- **invariants / constraint notes** written on or beside an aggregate,
  verbatim, with which command they sit beside;
- **state stickies** per aggregate, verbatim;
- **UI sketches** (white) and which command they precede;
- **hotspots**, verbatim, and which sticky each is attached to;
- **every sticky the grammar cannot place** — a command with no receiver, an
  event with nothing before it. These are the board's own gaps; they stay
  gaps.

If a Detail Design Brief is supplied with the board, its §2 and §3 are this
transcription already. Show them, have the user confirm them against the
board, and correct the brief's reading where the board disagrees — the board
wins, and the disagreement is a line in the report.

Show the transcription before emitting.

---

## 3. Map

### Nodes

| On the board | Class | Notes |
|---|---|---|
| Orange event | `dkg:DomainEvent` | `dkg:sequence` = slice number; `dkg:outcome` `"success"` / `"failure"` |
| Blue command | `dkg:Command` | `dkg:triggerKind`; UI sketch as `rdfs:comment "UI: …"` |
| Large yellow aggregate | `dkg:Aggregate` | and `dkg:AggregateRoot` only if a glossary already says so |
| Yellow "system" sticky, not clearly an aggregate | `dkg:AggregateCandidate` | never promoted to `dkg:Aggregate` here |
| Pink external system | `dkg:ExternalSystem` | |
| Small yellow actor | `dkg:Actor` | |
| Lilac policy | `dkg:Policy` | label verbatim; `dkg:condition` for the *and …* clause |
| Green read model | `dkg:ReadModel` | |
| Constraint note on an aggregate | `dkg:Invariant` | label = the note, verbatim |
| State sticky | `dkg:hasState` literal on the aggregate | as on a Big Picture board — only from a sticky |
| Red hotspot | `dkg:Hotspot` | `dkg:raises` from the sticky it is attached to; `dkg:mentions` what it is about |
| The scope | `dkg:BoundedContext` | from §0 — enrich, never re-mint |

### Edges

| Grammar step | Edge |
|---|---|
| actor issues command | `Command dkg:performedBy Actor` |
| policy issues command | `Policy dkg:triggers Command` |
| policy reacts to event | `Policy dkg:reactsTo DomainEvent` |
| command lands on receiver | `Command dkg:handledBy Aggregate / AggregateCandidate / ExternalSystem` — **exactly one** |
| command results in event | `Command dkg:triggers DomainEvent` (every branch) |
| aggregate emits event | `DomainEvent dkg:emittedBy Aggregate` |
| read model consulted for a decision | `Command dkg:reads ReadModel`; `Policy dkg:reads ReadModel` for its condition |
| event updates read model | `DomainEvent dkg:updates ReadModel` |
| invariant guards command | `Invariant dkg:guards Command`, `Invariant dkg:enforcedBy Aggregate`, `Invariant dkg:rejectsWith DomainEvent` when the board shows the failure branch |
| state change | `dkg:Transition` — only when state stickies are on the board (below) |
| sequence | `DomainEvent dkg:precedes DomainEvent`, board order |
| scope | `dkg:inContext` the scoped context, on every Command, DomainEvent, Aggregate, AggregateCandidate, Policy, ReadModel and Invariant |
| the artifact | `dkg:scopedTo` the context; `dkg:flavour`; `dkg:refines` the Big Picture board when there is one |

Actors and external systems get `dkg:inContext` only if the board places them
inside the boundary. An external system is, almost by definition, outside it.

### Lifecycles — only from stickies

If the board carries state stickies, each drawn transition becomes one node:

```turtle
:Tr_Reservation_Requested_Confirmed a dkg:Transition ;
    skos:prefLabel "Reservation: requested → confirmed" ;
    dkg:ofAggregate :Con_Reservation ;
    dkg:fromState "requested" ; dkg:toState "confirmed" ;
    dkg:via :Ev_ReservationConfirmed ;
    dkg:source :Art_ESDetail_RackMgmt_2026_10 ; dkg:locator "state stickies above slice 4" ;
    dkg:confidence dkg:OnArtifact .
```

No state stickies → no transitions from the board. A lifecycle the brief
*reconstructed* is §5 material: `dkg:Inferred`, sourced to the brief.

### Multi-context boards

When §0 came back with several contexts and a region map, every scoped node
gets the context of its region, and the artifact one `dkg:scopedTo` per
context. A command in one region landing on an aggregate in another is
**border evidence**: keep both `dkg:inContext` edges as drawn, and report it —
either the command crosses the border (an integration the context map must
show) or the region map is wrong. A sticky the user's region map does not
cover gets no `dkg:inContext` and a `dkg:Question` asking where it lives.

---

## 4. The detail-board traps

- **Guessing the context.** §0. The most expensive mistake this ingest can
  make, because it is invisible afterwards: every `inContext` looks equally
  confident.
- **Inventing a receiver.** A command with no aggregate after it stays without
  `dkg:handledBy`. `check_graph.py` reports it; that is the point.
- **A command landing on two aggregates.** Emit both `dkg:handledBy` edges as
  drawn and report it. One command, two consistency boundaries, means either a
  wrong boundary or a policy step the board compressed. Do not pick one.
- **Promoting a candidate.** A yellow "system" sticky is an
  `dkg:AggregateCandidate` until a later board or the room says otherwise; the
  later revision adds `dkg:Aggregate` and the report says so.
- **Normalising invariants.** "max 2 bikes per rider" is not "a rider may hold
  at most two reservations". Keep the note's words. The Given/When/Then form
  belongs to `event-storming-invariant-finder`, and if its sheet is ingested
  later it arrives as `dkg:Inferred` rules beside these, possibly
  `dkg:proposedSameAs` them.
- **Happy-path boards.** A command with only success events is ingested as
  drawn; the missing rejection is a finding, not an event to add.
- **Policies without a reaction or a command.** `whenever X then …` with the
  *then* blank is a Policy with no `dkg:triggers`. Ingest it; report it.
- **Recurring slices.** As on a Big Picture board: the same command/event pair
  at two points on the line is **one** node with two locators.
- **Re-minting Big Picture events.** The detail board usually repeats the Big
  Picture board's events for this stretch. Exact label, same class → merge
  (the detail board becomes a second `dkg:source`, which is exactly the
  corroboration the register should show). A detail board that renames a Big
  Picture event (*Bike returned* → *Bicycle returned at rack*) is a ledger row
  and an `skos:altLabel`, never a second event.

---

## 5. Ingesting the Detail Design Brief

The brief is a **derived analysis** (SKILL.md, "Ingesting analyses"): it gets
its own `dkg:DetailDesignBrief` artifact, with `dkg:refines` the board it
reads and `dkg:scopedTo` the same context. Its sections split cleanly:

| Brief section | Treatment |
|---|---|
| §1 Legend & flavour | `dkg:flavour` on the **board** artifact if the board is in the register; a disagreement with what the board shows goes in the report |
| §2–3 Transcribed flow, slice table | the transcription (§2 above). Ingest from it **as the board**: nodes sourced to the board artifact, not the brief. If the board itself is not available, register the board anyway (`rdfs:comment "known only through <brief>"`) and lower every node to `dkg:Implied` |
| §4 Invariants marked *on board* | `dkg:Invariant`, sourced to the board, `dkg:OnArtifact` |
| §4 Invariants marked *inferred* | `dkg:Invariant`, sourced to the brief, `dkg:Inferred` |
| §4 Minimal state | `rdfs:comment` on the aggregate, sourced to the brief — never `dkg:hasState` |
| §5 Lifecycles | `dkg:Transition` nodes, sourced to the brief, `dkg:Inferred` (unless the state is a sticky on the board — then from the board, §3) |
| §6 Policy classification | `dkg:policyScope` `"within-aggregate"` / `"cross-aggregate"` / `"cross-context"`, reified as a `dkg:Assertion` sourced to the brief, because it is a reading, not a sticky. A *cross-context* policy is border evidence — report it with both contexts |
| §6 Sagas / compensations | `rdfs:comment` on the policy, sourced to the brief |
| §7 Read-model fields | stay out; `read-model-author` reads them from the brief |
| §8 Completeness findings | one `dkg:Question` each, `dkg:Inferred`, sourced to the brief, `dkg:mentions` the stickies it cites |
| §9 Hotspots | already `dkg:Hotspot`s from the board; the brief's *open questions* become `dkg:Question`s sourced to the brief |

The brief's markers — *on board* / *inferred* — map onto confidence one to one,
exactly as a canvas's `(given)` / `(proposed)` do.

---

## 6. Merge hazards

- **Aggregates against glossary terms.** As on a Big Picture board: an
  aggregate is a term with a consistency boundary around it. Exact match →
  merge, keep both types. A glossary `Entity` named like a board aggregate is
  the common, clean case; a glossary `ValueObject` named like an aggregate is a
  `dkg:contradicts` — one artifact says it has identity and a lifecycle, the
  other says it has neither.
- **Commands against canvas inbound rows and API operations.** Never merge.
  A canvas `cmd` row is linked to the command with `dkg:mentions`; an operation
  with `dkg:specifies` (`ingest-api-specification.md`). The detail board is
  usually where those links first become exact, because its command names are
  the most precise the model has.
- **Read models against Big Picture read models.** Often the same green sticky
  with more detail. Exact match → merge; a detail read model splitting one Big
  Picture read model into two is a ledger row and two `dkg:proposedSameAs`.
- **Invariants against ADR decisions.** A decision whose chosen option *is* an
  invariant gets a proposed link, never an automatic one (`ingest-small-adr.md`).
- **The same label in another context's detail board.** Two contexts' boards
  each holding `Reservation` is not a merge candidate on label alone: two
  aggregates in two contexts are two concepts until a canvas or context map
  says one is upstream. Suffix the second IRI with its context
  (`:Con_Reservation_Billing`), write `dkg:proposedSameAs`, and put it in the
  ledger as *border term*.

---

## 7. Snippet

```turtle
:Art_ESDetail_RackMgmt_2026_10 a dkg:EventStormingDetailBoard ;
    rdfs:label "Rack Management — design-level EventStorming" ;
    rdfs:comment "Two photos; 9 slices. Context named by the user." ;
    dkg:flavour "Design-Level" ;
    dkg:scopedTo :Ctx_RackManagement ;
    dkg:refines :Art_ESBoard_2026_06 ;
    dkg:ingestedAt "2026-10-23"^^xsd:date .

:Cmd_ReserveABicycle a dkg:Command ;               # merged: Big Picture board + detail board
    skos:prefLabel "Reserve a bicycle" ;
    dkg:triggerKind "actor" ;
    dkg:performedBy :Act_Rider ;
    dkg:reads :Con_AvailableBicyclesAtRack ;
    dkg:handledBy :Con_Reservation ;
    dkg:triggers :Ev_BicycleReserved, :Ev_ReservationRejected ;
    dkg:inContext :Ctx_RackManagement ;
    rdfs:comment "UI: rack map with free slots" ;
    dkg:source :Art_ESBoard_2026_06, :Art_ESDetail_RackMgmt_2026_10 ;
    dkg:locator "blue sticky 4", "slice 3" ;
    dkg:confidence dkg:OnArtifact .

:Ev_ReservationRejected a dkg:DomainEvent ;
    skos:prefLabel "Reservation rejected" ;
    dkg:outcome "failure" ; dkg:sequence 4 ;
    dkg:emittedBy :Con_Reservation ;
    dkg:updates :Con_MyReservations ;
    dkg:inContext :Ctx_RackManagement ;
    dkg:source :Art_ESDetail_RackMgmt_2026_10 ; dkg:locator "slice 3, lower branch" ;
    dkg:confidence dkg:OnArtifact .

:R_MaxTwoBikesPerRider a dkg:Invariant ;
    skos:prefLabel "max 2 bikes per rider" ;
    dkg:guards :Cmd_ReserveABicycle ;
    dkg:enforcedBy :Con_Reservation ;
    dkg:rejectsWith :Ev_ReservationRejected ;
    dkg:inContext :Ctx_RackManagement ;
    dkg:source :Art_ESDetail_RackMgmt_2026_10 ; dkg:locator "note on Reservation, slice 3" ;
    dkg:confidence dkg:OnArtifact .

:R_WheneverBicycleReturnedReleaseSlot a dkg:Policy ;
    skos:prefLabel "whenever bicycle returned then release slot" ;
    dkg:reactsTo :Ev_BicycleReturned ;
    dkg:triggers :Cmd_ReleaseSlot ;
    dkg:inContext :Ctx_RackManagement ;
    dkg:source :Art_ESDetail_RackMgmt_2026_10 ; dkg:locator "lilac, slice 7" ;
    dkg:confidence dkg:OnArtifact .
```

---

## 8. Check and report additions

`check_graph.py` runs the detail grammar automatically on every node sourced
to a `dkg:EventStormingDetailBoard`:

- **error** — a detail board with no `dkg:scopedTo` (§0 was skipped);
- findings — command without receiver, command on two receivers, command
  without trigger, event without cause, happy path only, aggregate without
  invariant (Design-Level only), policy without reaction or command, policy
  condition without a read model, actor decision without a read model, read
  model nothing updates, read model nobody reads, failure event nobody reacts
  to (dead end), scoped node outside the board's context, and scoped nodes with
  no `dkg:inContext`.

`views.py` renders the **design lens**: per context, per aggregate, what it
accepts, what it protects and what it emits, with policies looping back and a
lifecycle table where transitions exist.

Add to the report, after the standard seven items:

- **The scope** — which context, who named it, and whether it existed before
  this ingest. First line if it was minted.
- **Flavour**, and if it is a revision from Process- to Design-Level, which
  receivers became aggregates.
- **What the zoom added to the Big Picture** — events and commands the detail
  board introduced for this stretch, and Big Picture stickies for this stretch
  the detail board does *not* show (dropped on purpose, or out of scope?).
- **The grammar findings**, grouped by aggregate, each with its locator.
- **Borders the board draws** — commands landing across a region boundary,
  cross-context policies, external systems the command flow depends on.
- **What is now buildable** — aggregates with a receiver, an invariant and a
  failure branch for every command are ready for `aggregate-behavior-author`;
  name them, and name the ones that are not and why.
