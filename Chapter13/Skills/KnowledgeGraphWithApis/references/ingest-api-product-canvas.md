# Ingest — API Product Canvas

One bounded context's interface, sketched on stickies before anyone writes a
spec (Junker / Lazzaretti; the Miro template). Two halves and a footer:

- **Synchronous API** — name of the bounded context · version · contact · sync
  protocol (`HTTP`) · architecture pattern (`RESTful`) · server · *aggregates
  and entities, with the operations applied to them* · parameters · requests ·
  responses.
- **Asynchronous API** — async protocol (`Kafka`) · architecture pattern
  (`event-driven`) · server · *events receive* with payload · *events send*
  with payload.
- **Quality requirements** · **Notes**.

It sits between the Bounded Context Canvas and the specs: the BCC says *what
crosses the border and with whom*; this canvas says *how the context intends
to offer it* — which operations, on which aggregates, which events in and out,
over which protocol. Three things make it different from the artifacts around
it:

- **It is a workshop artifact, so it is on-artifact.** Unlike a BCC it carries
  no `(given)/(derived)` markers and unlike a spec it is not a design file.
  What the sticky says, the team said.
- **It mints the context's `dkg:Api` nodes.** One per side that is filled in —
  a sync Api and/or an async Api — identified by *context × interaction style*.
  When an OpenAPI or AsyncAPI document for the same context arrives later, it
  **enriches those nodes** (`api_inventory.py --graph` finds them) instead of
  minting a second pair. Canvas sticky and spec operation with one label become
  one `dkg:Operation` with two sources; a protocol the canvas and the spec
  spell differently becomes a finding.
- **Its "events receive" field is border evidence.** Every received event is a
  claim that some other context emits it. Matched to a board event in another
  context, it is a crossing the canvases may never have drawn.

## Transcribe

Field by field, verbatim, in the canvas's order. One row per sticky.

1. **Bounded context name**, version, contact.
2. **Sync protocol**, **architecture pattern**, server.
3. **Aggregates and entities** — each box, and under it every operation sticky
   as written (`Reserve bicycle`, `GET free bikes`, `cancel`). Keep the
   nesting: which operation sits on which aggregate.
4. **Parameters** — each sticky, and the operation it is pinned to if the
   board shows that; otherwise "unattached".
5. **Requests** / **Responses** — each sticky; the operation it belongs to if
   pinned.
6. **Async protocol**, pattern, server.
7. **Events receive** — each sticky, with its payload sticky(s).
8. **Events send** — each sticky, with its payload sticky(s).
9. **Quality requirements** — each line.
10. **Notes** — each line.

Show it. A canvas photo is where `Get bike` and `Get bikes` get read as one
sticky and a payload lands under the wrong event.

A sticky that is only a noun (`Reservation`) under *operations* is not an
operation; transcribe it where it is and flag it — the room may have meant the
read model.

## Map

| Canvas field | Class / property |
|---|---|
| Bounded context name | the existing `dkg:BoundedContext` — **never re-minted**; the canvas's spelling as `skos:altLabel` if it differs; `dkg:source` added |
| Sync side present | `dkg:Api` — `skos:prefLabel "<Context> — synchronous API"` unless the canvas names it; `dkg:interactionStyle "sync"`; `dkg:inContext` the context; `dkg:protocol`, `dkg:architecturePattern`, `dkg:apiVersion`, `dkg:contact` verbatim |
| Async side present | a second `dkg:Api`, `"async"`, same properties from its own fields |
| Server (either side) | **not emitted** — deployment; mention in the report |
| Aggregate / entity box | `dkg:Aggregate` / `dkg:Entity` (⊂ `dkg:Concept`) — **auto-merge on exact label** with the glossary's term (same spine class); `dkg:inContext` this context |
| Operation sticky under a box | `dkg:Operation` — `skos:prefLabel` verbatim; `dkg:exposedBy` the sync Api; `dkg:mentions` the box's Concept; `dkg:httpMethod` / `dkg:path` only if the sticky writes them; `dkg:messageKind` `qry` if the verb is get/list/find/search, `cmd` otherwise, `dkg:Implied` |
| Parameter sticky | `dkg:parameter` literal on the Operation it is pinned to, else on the sync Api |
| Request / response sticky | `dkg:Schema` — role `request` / `response`, `dkg:exposedBy` the sync Api; `dkg:requestSchema` / `dkg:responseSchema` from the Operation when pinned; `dkg:specifies` the Concept on an exact (normalised) match |
| Event receive sticky | `dkg:ApiMessage` + a `dkg:Operation` with `dkg:action "receive"`, both `dkg:exposedBy` the async Api; `dkg:carriesMessage`; `dkg:specifies` the `dkg:DomainEvent` on exact match — usually in *another* context |
| Event send sticky | the same with `dkg:action "send"`; `dkg:specifies` this context's `dkg:DomainEvent` and/or the BCC's outbound `evt` Message |
| Payload sticky | `dkg:Schema` role `payload`, `dkg:payloadSchema` from the ApiMessage; the field list, if written, verbatim in `rdfs:comment` |
| Quality requirement line | `dkg:QualityRequirement` — `skos:prefLabel` verbatim; `dkg:constrains` the Api it names (`"sync p95 < 200 ms"`) or the context when it names neither |
| Note line | `rdfs:comment` on the `dkg:ApiProductCanvas` artifact node, one per line |

No channels: a canvas names events, not topics. `dkg:Channel` nodes arrive with
the AsyncAPI. Everything gets the canvas as `dkg:source` and the field as
`dkg:locator` (`"Aggregates and Entities, Rack, op 2"`, `"Events send, sticky 1"`).

`dkg:specifies` follows exactly the rule in `ingest-api-specification.md`:
direct and `dkg:Implied` on an exact match after folding case, spaces and
camel boundaries; a reified `dkg:Inferred` assertion on anything looser, via
the ledger. The canvas is handwritten, so expect fewer exact matches and more
proposals than a spec produces.

## Merge hazards

- **The Api is the context's, not the canvas's.** Look for an existing
  `dkg:Api` with the same `dkg:inContext` and `dkg:interactionStyle` first —
  a spec may have been ingested before the canvas (it happens: teams write the
  OpenAPI, then hold the workshop). Then the canvas enriches the spec's Api
  node, adds `dkg:architecturePattern` and `dkg:contact`, and its operation
  stickies merge into the spec's operations by label. The order does not
  matter; the identity rule does.
- **Operations merge by exact label, same class, no contradicting property** —
  the standard auto-merge, because canvas operation and spec operation are
  both `dkg:Operation`. A canvas `Reserve bicycle` and a spec summary `Reserve
  a bicycle` are a *proposal* (`dkg:proposedSameAs`), not a merge; a canvas
  `GET free bikes` and a spec `GET /racks/{rackId}/free-bikes` match on method
  only — propose. Once the room confirms, collapse onto the spec's IRI (it has
  the path) and keep the canvas's wording as `skos:altLabel`.
- **Aggregates collide with the glossary — good.** `Rack` on the canvas and
  `Rack` in the glossary are one `dkg:Concept`; the canvas adds itself as a
  source. If the glossary marks it a value object and the canvas puts it under
  *aggregates*, that is a class disagreement: keep the glossary's class, add
  `rdfs:comment "canvas treats it as an aggregate"`, and report it.
- **A received event the graph holds in no context.** The producer is unknown:
  mint nothing for it, leave the ApiMessage unlinked, and report "receives *X*
  — nobody in the graph emits it". A received event that matches an event in
  *this* context is a canvas error or an internal event on the bus; ask.
- **Protocol words.** `HTTP` / `HTTPS` / `REST` on the canvas against `https`
  in a spec are one protocol; `Kafka` against `amqp` are not. The checker folds
  the first family and reports the second as a *protocol disagreement* on the
  Api. Keep both literals; never edit the canvas's word.
- **Quality requirements against Decisions and Principles.** `"async-first"`
  as a quality requirement and a Principle `P1 Async-first` are the same
  statement in two artifacts — `dkg:proposedSameAs`, and a note that the
  canvas restates a principle. A requirement that contradicts an Adopted
  Decision (`"sync p95 < 200 ms"` where the ADR chose an event) is
  `dkg:contradicts` via two assertions, and leads the report.
- **Letting the canvas mint the context.** Same rule as the BCC: a canvas is
  about a context the graph has. If it does not, mint it `dkg:Inferred` and put
  it first in the report.

## Snippet

```turtle
########## API Product Canvas — Rack management — ingested 2026-10-05 ##########

:Art_APC_RackManagement_2026_10 a dkg:ApiProductCanvas ;
    rdfs:label "API Product Canvas — Rack management" ;
    rdfs:comment "Note: distribution needs the free-bike count within a second — hence the sync read." ;
    dkg:ingestedAt "2026-10-05"^^xsd:date .

:Ctx_RackManagement dkg:source :Art_APC_RackManagement_2026_10 .

:Api_RackManagement_Sync a dkg:Api ;
    skos:prefLabel "Rack management — synchronous API" ;
    dkg:interactionStyle "sync" ; dkg:protocol "HTTP" ; dkg:architecturePattern "RESTful" ;
    dkg:apiVersion "1" ; dkg:contact "Team Racks" ;
    dkg:parameter "rackId" ;
    dkg:inContext :Ctx_RackManagement ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Synchronous API header" ;
    dkg:confidence dkg:OnArtifact .

# existing glossary term, corroborated — not re-minted
:Con_Rack dkg:source :Art_APC_RackManagement_2026_10 ; dkg:inContext :Ctx_RackManagement .

:Op_ReserveBicycle_APC a dkg:Operation ; skos:prefLabel "Reserve bicycle" ;
    dkg:messageKind "cmd" ;
    dkg:mentions :Con_Rack ; dkg:exposedBy :Api_RackManagement_Sync ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Aggregates and Entities, Rack, op 1" ;
    dkg:confidence dkg:OnArtifact ;
    dkg:proposedSameAs :Op_ReserveBicycle ;        # the spec's "Reserve a bicycle"
    rdfs:comment "same verb and object; the spec adds the article — confirm" .

:Api_RackManagement_Async a dkg:Api ;
    skos:prefLabel "Rack management — asynchronous API" ;
    dkg:interactionStyle "async" ; dkg:protocol "Kafka" ; dkg:architecturePattern "event-driven" ;
    dkg:inContext :Ctx_RackManagement ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Asynchronous API header" ;
    dkg:confidence dkg:OnArtifact .

:Amsg_BicycleReturned_APC a dkg:ApiMessage ; skos:prefLabel "Bicycle returned" ;
    dkg:payloadSchema :Sch_BicycleReturnedPayload_APC ;
    dkg:exposedBy :Api_RackManagement_Async ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Events send, sticky 2" ;
    dkg:confidence dkg:OnArtifact ;
    dkg:specifies :Ev_BicycleReturned .            # exact: the board's event

:Sch_BicycleReturnedPayload_APC a dkg:Schema ; skos:prefLabel "Bicycle returned payload" ;
    dkg:schemaRole "payload" ; rdfs:comment "bicycleId, rackId, returnedAt" ;
    dkg:exposedBy :Api_RackManagement_Async ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Events send, sticky 2, payload" ;
    dkg:confidence dkg:OnArtifact .

:Op_SendBicycleReturned_APC a dkg:Operation ; skos:prefLabel "send Bicycle returned" ;
    dkg:action "send" ; dkg:messageKind "evt" ;
    dkg:carriesMessage :Amsg_BicycleReturned_APC ; dkg:exposedBy :Api_RackManagement_Async ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Events send, sticky 2" ;
    dkg:confidence dkg:Implied .                   # the canvas lists the event; the send is what listing it means

:QR_FreeBikeCountWithinOneSecond a dkg:QualityRequirement ;
    skos:prefLabel "Free-bike count available to distribution within 1 s" ;
    dkg:constrains :Api_RackManagement_Sync ;
    dkg:source :Art_APC_RackManagement_2026_10 ; dkg:locator "Quality requirements, line 1" ;
    dkg:confidence dkg:OnArtifact .
```

When the AsyncAPI for Rack management arrives afterwards,
`api_inventory.py --graph` reuses `:Api_RackManagement_Async` (same context,
same style), adds `dkg:protocol "kafka"` and the spec as a second source, and
offers `:Amsg_BicycleReturned_APC` as the node for its `BicycleReturned`
message — exact after normalisation, so a direct merge: one ApiMessage, two
sources, the spec's payload schema linked beside the canvas's payload sticky.

## Report additions

- **Which context, and whether its Api nodes existed** (from a spec) or were
  minted here. First line.
- **Sync side** — n operations on m aggregates; operations with no aggregate;
  aggregates with no operation; requests/responses unattached to any operation.
- **Async side** — events sent, events received; for each received event, the
  context the graph says emits it, or *nobody*.
- **Against the BCC**, if one is in: inbound rows with no operation, outbound
  `evt` rows with no sent event, and canvas operations/events the BCC never
  listed as crossing the border (internal, or a border the BCC missed).
- **Against the spec**, if one is in: operations merged, operations proposed,
  operations on one side only; protocol agreement or disagreement.
- **Quality requirements** — each, what it constrains, and any Principle or
  Decision it restates or contradicts.
- **Servers and anything else not emitted.**
