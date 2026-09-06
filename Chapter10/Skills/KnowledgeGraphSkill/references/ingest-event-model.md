# Ingest — Event Model

Dymitruk's technique: one left-to-right timeline in **swimlanes**, cut into
vertical **slices** — state change, state view, automation, translation — each
with a wireframe and a Given/When/Then. Often several board photos that need
stitching.

An Event Model differs from an EventStorming board in two ways the graph cares
about: the swimlanes are already bounded contexts, and the slices are already
units of work. Both are `dkg:OnArtifact` here, where on an ES board they would
be proposals.

## Stitch first

If several images arrive, stitch before ingesting, and classify every seam:

- **Abutment** — one image ends where the next begins. Nothing lost.
- **Overlap** — the same stickies appear in both. Emit once, two locators.
- **Recurrence** — the *same workflow* appears at two points on the line,
  triggered differently. **One** set of nodes, reachable from both triggers.
  Getting this wrong duplicates a whole context.
- **Gap** — a connector runs off a frame edge unresolved. Report it; never
  bridge it.

State the swimlane order and confirm it is identical in every image. If it is
not, that is the first thing in the report.

## Map

| On the model | Class / property |
|---|---|
| Swimlane | `dkg:Swimlane` (subclass of `dkg:BoundedContext`) |
| Vertical slice | `dkg:Slice`, with `dkg:sliceKind` `"state change"` / `"state view"` / `"automation"` / `"translation"` |
| Event | `dkg:DomainEvent` |
| Command | `dkg:Command` |
| Read model | `dkg:ReadModel` |
| Automation / policy | `dkg:Policy`, `dkg:reactsTo` |
| Translation across lanes | `dkg:Assertion` on the crossing, sourced to the model |
| Wireframe | not a node — record as `rdfs:comment` on the slice |
| Given/When/Then | `dkg:Rule`, `dkg:confidence dkg:OnArtifact`, linked by `dkg:mentions` |

Every node gets `dkg:inContext` from its swimlane, and every slice's members
point at the slice via `dkg:mentions`. Slices are the unit people plan by, so
keeping them as nodes is what lets someone ask "what does this slice touch"
later.

## Traps

- **A command sticky in a machine lane.** A command implies an actor who
  decides; if the actor is a machine, the board may mean a policy. Ingest what is
  drawn, emit a `dkg:Question`, and report it. Do not silently retype it.
- **An event with no command or policy above it.** Ingest the event; emit a
  `dkg:Question` asking what emits it. This is the most common real gap in these
  models and quietly inventing a trigger hides it.
- **A command with no event below it.** Same treatment in reverse.
- **A machine marker on an event** — record it as a marker with its meaning
  unconfirmed, exactly as on an ES board.
- **Swimlanes versus contexts from other artifacts.** The model's lanes are
  drawn; a story's lanes are drawn; an analysis's contexts are proposed. Where
  they disagree, that disagreement is a genuine architectural finding — reify
  both, `dkg:contradicts`, and report verbatim. Do not let the newest artifact
  win by arriving last.

## Merge hazards

Everything that applies to an EventStorming board applies here. Additionally:

- **A model built from the same domain as an already-ingested board** will
  duplicate most of it. Merge event-by-event on exact label; where the model
  renames an event, that is `skos:altLabel` plus a ledger row, because a rename
  between a discovery board and a planning model is usually deliberate.
- **Read models appearing in three lanes** — one projection viewed three ways,
  or three read models? Emit as drawn, propose the merge, ask.

## Snippet

```turtle
:Slice_RequestHelp a dkg:Slice ; dkg:sliceKind "state change" ;
    skos:prefLabel "request help" ;
    dkg:inContext :Ctx_CookAssistance ;
    dkg:mentions :Cmd_RequestHelp, :Ev_HelpRequested, :Con_HelpRequest ;
    rdfs:comment "wireframe: trouble picker + photo attach" ;
    dkg:source :Art_EventModel_2026_03 ; dkg:locator "image 1 & 2, recurring" ;
    dkg:confidence dkg:OnArtifact .
```

## Report additions

- **Every seam and its classification.**
- **Slices with no wireframe**, and **wireframes with no slice**.
- **Events with no trigger and commands with no event**, listed.
- **Lane disagreements** with any context already in the graph.