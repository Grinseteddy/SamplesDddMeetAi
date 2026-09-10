# Ingest — EventStorming board

Orange events on a timeline, with blue commands, yellow actors, large yellow
aggregates, green read models, lilac policies and external systems, red
hotspots, and sometimes context bubbles drawn around clusters. Usually a photo,
often several photos of one wall.

The board is the densest artifact in the set and will contribute more nodes than
anything else. That is a hazard: read the register warning in
`references/browsing.md` and say plainly in the report if the board now
dominates the graph.

## Transcribe

A table, one row per event, left-to-right and top-to-bottom:

`# · event · command · actor(s) · read models consulted · business objects
produced · context bubble · marks`

Also record, outside the table:

- the **colour convention as read** — state it explicitly; boards differ, and
  every downstream reading depends on it;
- **state stickies** and what they say;
- **hotspots**, verbatim;
- **loops and branches** — which events repeat, which are alternatives;
- **clusters drawn twice** — the same command/event pair appearing at two points
  on the line is one capability, not two events, and this is the single most
  consequential reading on most boards;
- **icons or markers whose meaning is unconfirmed**, as unconfirmed. Never let a
  marker carry a claim on its own.

Show the table to the user before emitting anything.

## Map

| Sticky | Class |
|---|---|
| Orange event | `dkg:DomainEvent` |
| Blue command | `dkg:Command` |
| Small yellow actor | `dkg:Actor` |
| Pink / lilac external system | `dkg:ExternalSystem` |
| Large yellow aggregate | `dkg:Aggregate` |
| Green read model | `dkg:ReadModel` |
| Pale yellow business object | `dkg:BusinessObject` |
| Lilac policy | `dkg:Policy` |
| Red hotspot | `dkg:Hotspot` |
| Context bubble | `dkg:BoundedContext` |
| State sticky | `dkg:hasState` on the aggregate |

Edges: `dkg:triggers` (command → event), `dkg:performedBy`, `dkg:reads`,
`dkg:produces`, `dkg:emittedBy`, `dkg:reactsTo` (policy → event),
`dkg:precedes` (event n → n+1), `dkg:inContext`.

Sequence numbers go on the events as `dkg:sequence`, matching the transcription
table, so that later analyses can be laid beside the graph by number.

## The board-specific traps

- **The same cluster drawn twice.** Emit **one** node per repeated command,
  event and object, with two `dkg:locator` values and a comment saying it recurs.
  Emitting two makes a recurring capability look like two points on a line, and
  every downstream reading inherits the error.
- **A command written on six different events.** Emit one `dkg:Command` node
  with all its locators, and report it: either it is a placeholder for six
  commands, or the board is claiming the whole phase is one interaction. Do not
  invent the six names.
- **Reconstructed state machines.** If the board has no state stickies, the
  graph has no `dkg:hasState`. Say so in the report — an absence of states on a
  board whose events include *stalled* and *catastrophe* is a finding, and the
  reconstruction belongs to `event-storming-invariant-finder`, not here.
- **Context bubbles the team drew** are `dkg:OnArtifact`. Contexts *you* would
  draw are not ingested at all; that is `pivotal-event-boundary-finder`'s job,
  and if its output is ingested later it comes in as `dkg:Inferred` from that
  analysis (see "Ingesting analyses" in SKILL.md).
- **No hotspots at all** on a board about failure is worth a line in the report.
  It usually means disagreement was never captured, not that there was none.

## Merge hazards

- **Business objects and read models against glossary terms** — the main
  merging work, and mostly clean.
- **Actors against BMC segments and impact-map actors** — propose; the
  vocabularies differ by design.
- **Aggregates against terms** — an aggregate is a term with a consistency
  boundary around it. Merge, and keep both types on the node.
- **One label, two stickies of different colours** — `Pictures` as a business
  object in two places serving two purposes. Split, suffix, report.

## Report additions

- **Stretches of timeline with no business object**, named. A run of events
  producing nothing usually means an undiscovered aggregate, and saying which
  events are affected is the useful form.
- **Read models nobody writes** — read by n events, produced by none. Each one
  is a missing context.
- **Business objects nobody reads** — produced and never consumed.
- **Whether the board now dominates the graph**, with the node counts.