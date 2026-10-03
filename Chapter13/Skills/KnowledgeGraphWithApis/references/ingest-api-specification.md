# Ingest — API specification (OpenAPI, AsyncAPI) per Bounded Context

The last rung of the ladder. Every artifact above it says what the team
*modelled*; a specification says what will be *built* — the synchronous side as
an OpenAPI document (or a gRPC `.proto`, a GraphQL schema), the asynchronous
side as an AsyncAPI document. One spec per context per style is the shape the
sibling skills `openapi-spec-author` and `asyncapi-spec-author` produce, and
the shape this ingest expects: **a specification belongs to exactly one
`dkg:BoundedContext`**, and the graph's job is to say, per context, what can be
called, what is published, what is consumed from across a border — and where
the contract and the model disagree.

Three things make a spec different from every other artifact here:

- **It is machine-readable, so transcription is mechanical.** Run
  `scripts/api_inventory.py` (below) and show its tables; do not retype a
  twenty-operation spec by hand. The judgment is all in the *linking*.
- **It is not a derived analysis.** What the YAML says, it says on-artifact.
  But what it says *about the domain* — that `reserveBicycle` is the board's
  *Reserve a bicycle*, that `BicycleReturned` is the event sticky — is a claim.
  Every link into the model is `dkg:specifies`, `dkg:Implied` at best, and never
  a merge. A `dkg:Command` and the `POST` that carries it stay two nodes.
- **It has a built-in revision key.** OpenAPI's `x-api-id` is stable across
  versions by house convention. Same `apiId` already on a `dkg:Api` in the
  graph → *revision* (`reingest-revision.md`), whatever the title says. AsyncAPI
  has no such id; use the label-overlap test on operations and messages.

## Mode

Before anything: is there already a `dkg:Api` for this context with this
`dkg:interactionStyle`?

| Graph already holds | Arriving | Mode |
|---|---|---|
| nothing for this context | any spec | **grow** |
| a sync Api with the same `x-api-id` | OpenAPI | **revise** — `dkg:revisionOf`; diff operations and schemas |
| a sync Api, different `x-api-id`, same context | OpenAPI | ask: a second API of the context (grow, report it), or a re-keyed revision? |
| an async Api for this context | AsyncAPI | `--overlap` on message + operation labels; ≥ ⅔ both ways → revise |
| no `dkg:BoundedContext` with this name | any | the spec names a context nobody drew — mint it `dkg:Inferred`, report it **first** |

`api_inventory.py --graph graph.ttl` prints a `REVISION` line in its ledger
when it finds the `x-api-id`; stop there and switch mode.

## Transcribe

```bash
python3 scripts/api_inventory.py spec.yaml                      # the transcription
python3 scripts/api_inventory.py spec.yaml --labels labels.txt  # for --overlap
```

The inventory is: the `info` block (title, version, `x-api-id`, `x-audience`,
protocol), then per style:

- **OpenAPI** — one row per operation: method · path · `operationId` · summary
  · tag · scopes · request schema · response schema · *kind* (`qry` for GET,
  `cmd` otherwise — implied, not read). Then every `components.schemas` key with
  its implied *role* (read / create / update / payload / header / error) and
  *stem* (`CatalogEntryUpdate` → `CatalogEntry`).
- **AsyncAPI** — channels (key · address · messages), operations (key ·
  **action** `send`/`receive` · channel · messages · *kind* `evt` when every
  message name is past tense, else blank), messages (key · name · title ·
  payload schema), schemas as above.

Show the tables. The two things a reader catches here that the script cannot:
a `receive` that should be a `send` (the author wrote the consumer's
perspective — the single most common AsyncAPI 3 mistake), and a message named
in the present tense (`RackFull`) that is a state, not a fact.

**Other synchronous formats.** A gRPC `.proto` or a GraphQL schema is
transcribed by hand into the same OpenAPI-shaped table: each `rpc` / each
`Query`·`Mutation` field is an operation (`dkg:operationId` the rpc or field
name; no `dkg:httpMethod`, `dkg:path` left off; kind `qry` for a `Query` field
or an rpc whose name starts Get/List/Find, `cmd` otherwise — say so); each
`message` / `type` is a Schema. `dkg:specFormat "proto3"` or `"GraphQL SDL"`,
`dkg:protocol "grpc"` / `"https"`. The Turtle shape below is identical.

## Map

| Spec element | Class / property |
|---|---|
| the file | `dkg:SyncApiSpec` or `dkg:AsyncApiSpec` (both ⊂ `dkg:ApiSpecification` ⊂ `dkg:Artifact`); `rdfs:label` = title + format; `rdfs:comment` = file name; `dkg:specFormat` |
| `info` | `dkg:Api` — `skos:prefLabel` title, `rdfs:comment` description, `dkg:interactionStyle` `"sync"`/`"async"`, `dkg:apiVersion`, `dkg:apiId`, `dkg:audience`, `dkg:protocol` (one per server protocol) |
| which context | `dkg:inContext` on the Api — **exactly one** (see confidence below) |
| OpenAPI operation | `dkg:Operation` — `dkg:operationId`, `dkg:httpMethod`, `dkg:path`, `dkg:messageKind` (implied), `dkg:requiresScope` ×n, `dkg:requestSchema`, `dkg:responseSchema`, `dkg:exposedBy` the Api |
| AsyncAPI operation | `dkg:Operation` — `dkg:operationId`, `dkg:action`, `dkg:onChannel`, `dkg:carriesMessage` ×n, `dkg:messageKind "evt"` only when implied by tense, `dkg:exposedBy` |
| AsyncAPI channel | `dkg:Channel` — `dkg:address`, `dkg:carriesMessage` ×n, `dkg:exposedBy` |
| `components.messages.X` | `dkg:ApiMessage` — `skos:prefLabel` = `name`, `skos:altLabel` = `title`, `dkg:payloadSchema`, `dkg:exposedBy` |
| `components.schemas.X` | `dkg:Schema` — `skos:prefLabel` the key verbatim, `dkg:schemaRole` (implied), `dkg:exposedBy` |
| operation ↔ model | `dkg:specifies` → the `dkg:Command` / story `dkg:Activity` / canvas `dkg:Message` it carries; `dkg:mentions` → the Concept its schemas specify (mechanical closure) |
| message ↔ model | `dkg:specifies` → the `dkg:DomainEvent`, and/or the canvas `dkg:Message` typed `evt` |
| schema ↔ model | `dkg:specifies` → the `dkg:Concept` (Term, Aggregate, BusinessObject, ReadModel) whose label equals the schema's *stem* |
| scopes | literals only. A scope that spells an Actor (`librarian:write`) is a ledger row proposing `dkg:performedBy`, never an automatic edge |
| servers, bindings, headers, fields | **not in the graph.** Hosts, partitions, retention, `MessageHeader`, every property under a schema — the graph holds the domain-facing surface. `schema-glossary-consistency` reads fields against the glossary; this ingest does not |

Every node gets the spec as `dkg:source` and a `dkg:locator` that a reader can
find in the file: `"POST /reservations"`, `"components.messages.bicycleReturned"`,
`"operations.onBicycleReturned"`, `"info"`.

### Confidence

| What | `dkg:confidence` | Why |
|---|---|---|
| every node, every verbatim literal | `dkg:OnArtifact` | the YAML says so |
| `dkg:messageKind` on an Operation, `dkg:schemaRole` | `dkg:Implied` | read off the method / tense / suffix |
| `dkg:specifies`, `dkg:mentions` on an exact normalised match | `dkg:Implied` | a camelCase identifier is a spelling of a phrase; but the spec never cites the sticky |
| `dkg:specifies` on anything looser | a `dkg:Assertion`, `dkg:Inferred` | the ingester's reading — ledger row, room decides |
| `dkg:inContext` on the Api, when `info.title`/`description` names the context | `dkg:Implied` | comment: *"info names the context"* |
| `dkg:inContext` on the Api, when the ingester or the file name assigns it | `dkg:Inferred` | comment who said so. Record it on the node's comment; if the room disputes the context, reify that one edge, do not weaken the node |

### Normalisation for `dkg:specifies` — a deliberate relaxation

`merging.md` forbids stripping punctuation before an auto-merge. `specifies` is
not a merge, and specs cannot write spaces: `BicycleReturned`, `bicycle-returned`
and `Bicycle returned` are one phrase in three encodings. So the **exact** test
here folds case, spaces, hyphens, underscores and camel boundaries — and still
lands at `dkg:Implied`. What it does *not* fold: singular/plural (`/reservations`
vs *Reservation* is a proposal), role suffixes on an operation (`createX` vs
*Create X* matches; `createX` vs *X* does not), and synonyms. The script
applies exactly this rule; everything it is unsure about comes out as a
`# PROPOSE` line and a ledger row.

## Draft the Turtle

```bash
python3 scripts/api_inventory.py spec.yaml --ttl \
    --artifact Art_OpenAPI_RackManagement_2026_10 \
    --context Ctx_RackManagement --date 2026-10-03 \
    --graph graph.ttl > ingest.ttl
```

Stdout is the skeleton — every node, every literal, and the exact-match links
as direct triples with a trailing comment saying what matched. Stderr is the
ledger draft: `LINK` rows (exact, already emitted), `PROPOSE` rows (not
emitted; a `# PROPOSE` comment sits in the Turtle where the triple would go),
`NONE` rows (the model never named it), `TENSE`, `IRI` (a collision with
another spec's node, suffixed with the context) and `REVISION`.

Then **read the skeleton before appending it**, and do four things by hand:

1. **Decide every `PROPOSE`.** Confirmed → replace the comment with a reified
   `dkg:Assertion` (`rdf:predicate dkg:specifies`, `dkg:Inferred`, the reason
   in `rdfs:comment`). Rejected → delete the comment and say why in the ledger.
   Not a direct triple either way: a proposal that becomes a plain edge has
   laundered itself.
2. **Check every `LINK` whose note says ANOTHER CONTEXT or NEITHER END.** A
   `receive` operation matching an event in another context is border evidence
   and correct. A `send` or a sync operation matching another context's
   command or event is an API speaking for a model it does not own — keep the
   edge (the spec does say it) and put it in the report as a straddle.
3. **Look at every `NONE`.** An operation or schema the model never named is
   either the first the workshops hear of it (report it, leave it unlinked —
   `check_graph.py` will keep listing it until a sticky exists) or a spelling
   the normalisation could not bridge (`Booking` for *Reservation*: a proposal
   you add yourself, with the glossary's `altLabel` as evidence).
4. **Do not touch the model side.** No `skos:altLabel` on a Concept because the
   schema spells it differently — the spec's spelling lives on the Schema node;
   the language lens shows both through `specifies`. No new Concept for an
   unmatched schema. No `dkg:inContext` added to a board event because an API
   publishes it.

Append under a banner, in ingest order, as always.

## Merge hazards

- **Operation vs Command: never merge.** Same label, different spine class
  (`dkg:Operation` is not a `dkg:Activity`). `specifies` is the only edge. The
  temptation is strongest when the board command and the operation summary are
  word-for-word identical; that is the *best* case for a direct `specifies`
  and still not a merge.
- **ApiMessage vs DomainEvent vs canvas Message: three nodes.** The event is
  the fact (board), the canvas Message is the border row (who sends it to
  whom), the ApiMessage is the contract (name, payload, channel). An AsyncAPI
  message matching both a board event and a canvas `evt` row gets two
  `specifies` edges. Collapsing any pair loses one of: the event the API never
  publishes, the border nobody drew a canvas for, the contract nobody modelled.
- **The same message in two specs.** Rack management's AsyncAPI *sends*
  `BicycleReturned`; Billing's *receives* it. Two `dkg:ApiMessage` nodes
  (the script suffixes the second IRI with the context), both `specifies` the
  one `dkg:DomainEvent`. Add `dkg:proposedSameAs` between the two ApiMessages
  only if their payload schemas are the same schema — otherwise the
  difference is a contract drift finding, not a merge.
- **A `qry` in an AsyncAPI, an `evt` in an OpenAPI.** A sync operation whose
  `specifies` target is an event or a canvas `evt` row; an async message whose
  target is a canvas `qry` row. `check_graph.py` reports these as **style
  mismatch**. Do not resolve them: the canvas author and the spec author
  disagree, and either may be right (a command named in the past tense; a
  query answered from a replicated read model). Report both sources, verbatim.
- **Style against a Decision.** An ADR that decided *"synchronous request/reply
  for Bicycle distribution → Rack management"* and a spec that exposes that
  crossing only on a Kafka topic are a contradiction — but matching a decision's
  prose to a border is a human reading (`ingest-small-adr.md`). Make it a
  `dkg:Assertion` pair joined by `dkg:contradicts`, sourced to the ADR log and
  the spec, and lead the report with it. Do not expect the script to find it.
- **Schemas named for roles, not nouns.** `BookCreate` → stem `Book`;
  `CatalogEntryUpdate` → `CatalogEntry`; `TaskCreatedPayload` → the *event*,
  not a concept — payload schemas are deliberately not linked to Concepts (the
  ApiMessage carries the event link). `Error` and `MessageHeader` are never
  linked and never reported unlinked.
- **Resource nouns are plural; terms are singular.** `/reservations` matching
  *Reservation* is singular/plural → proposal, as `merging.md` says. The script
  gets the `dkg:mentions` another way — through the operation's request or
  response Schema, which *is* singular — so the proposal usually becomes
  redundant. Keep it in the ledger anyway if the path noun and the schema
  stem differ (`/racks/{rackId}/free-bikes` returning `Bike`): the path says
  what the API thinks the resource is called.
- **Scopes that name actors.** `catalog:write` names nothing; `librarian:write`
  names an Actor the board probably has. Ledger row, `dkg:proposedSameAs` is
  wrong (a scope is not an actor) — propose `:Op_X dkg:performedBy :Act_Librarian`
  as a `dkg:Assertion`, `dkg:Inferred`.
- **A spec for a context the graph does not have.** Mint the context
  `dkg:Inferred`, sourced to the spec, and make it the first line of the report:
  a context that exists only because a spec needed one is a boundary nobody
  modelled. Do the same for a receive from a context the graph does not have —
  the producer is an `dkg:ExternalSystem` candidate, not a context, until a
  board says otherwise.
- **Letting the spec re-mint the context.** `Rack Management API` is not a
  context; `:Ctx_RackManagement` already exists. Enrich with `dkg:inContext`;
  the Api node carries the title.

## Snippet

```turtle
########## OpenAPI 3.1.0 — Rack Management API — ingested 2026-10-03 ##########

:Art_OpenAPI_RackManagement_2026_10 a dkg:SyncApiSpec ;
    rdfs:label "Rack Management API — OpenAPI 3.1.0" ;
    rdfs:comment "rack-management.openapi.yaml" ;
    dkg:specFormat "OpenAPI 3.1.0" ;
    dkg:ingestedAt "2026-10-03"^^xsd:date .

:Api_RackManagementAPI a dkg:Api ;
    skos:prefLabel "Rack Management API" ;
    dkg:interactionStyle "sync" ; dkg:apiVersion "1.0.0" ;
    dkg:apiId "7b1c0e2a-5f3d-4c8e-9a1b-2d3e4f5a6b7c" ; dkg:audience "company-internal" ;
    dkg:protocol "https" ;
    dkg:inContext :Ctx_RackManagement ;        # Implied: info names the context
    dkg:source :Art_OpenAPI_RackManagement_2026_10 ; dkg:locator "info" ;
    dkg:confidence dkg:OnArtifact .

:Sch_ReservationCreate a dkg:Schema ; skos:prefLabel "ReservationCreate" ; dkg:schemaRole "create" ;
    dkg:exposedBy :Api_RackManagementAPI ;
    dkg:source :Art_OpenAPI_RackManagement_2026_10 ;
    dkg:locator "components.schemas.ReservationCreate" ; dkg:confidence dkg:OnArtifact .
:Sch_ReservationCreate dkg:specifies :Con_Reservation .      # stem = glossary term, exact

:Op_ReserveBicycle a dkg:Operation ; skos:prefLabel "Reserve a bicycle" ; skos:altLabel "reserveBicycle" ;
    dkg:operationId "reserveBicycle" ; dkg:httpMethod "POST" ; dkg:path "/reservations" ;
    dkg:messageKind "cmd" ; dkg:requiresScope "rack:write" ;
    dkg:requestSchema :Sch_ReservationCreate ; dkg:responseSchema :Sch_Reservation ;
    dkg:exposedBy :Api_RackManagementAPI ;
    dkg:source :Art_OpenAPI_RackManagement_2026_10 ; dkg:locator "POST /reservations" ;
    dkg:confidence dkg:OnArtifact .
# both canvases drew this crossing; the operation is the contract for both rows
:Op_ReserveBicycle dkg:specifies :Msg_In_BicycleDistribution_ReserveABicycle ,
                                 :Msg_Out_RackManagement_ReserveABicycle ;
    dkg:mentions :Con_Reservation .

# a near-match the ledger confirmed: reified, Inferred, never a plain edge
:As_GetRackSpecifiesLookUpRack a dkg:Assertion ;
    rdf:subject :Op_GetRackById ; rdf:predicate dkg:specifies ; rdf:object :Cmd_LookUpRack ;
    rdfs:comment "summary 'Get rack by ID' vs board 'Look up rack' — confirmed by the room 2026-10-03" ;
    dkg:source :Art_OpenAPI_RackManagement_2026_10 ; dkg:confidence dkg:Inferred .

########## AsyncAPI 3.1.0 — Billing Consumers — ingested 2026-10-03 ##########

:Api_BillingConsumers a dkg:Api ; skos:prefLabel "Billing Consumers" ;
    dkg:interactionStyle "async" ; dkg:protocol "kafka" ;
    dkg:inContext :Ctx_Billing ;
    dkg:source :Art_AsyncAPI_Billing_2026_10 ; dkg:locator "info" ; dkg:confidence dkg:OnArtifact .

:Ch_RackBicycleEvents a dkg:Channel ; skos:prefLabel "rackBicycleEvents" ;
    dkg:address "rack-management/bicycle-events" ; dkg:carriesMessage :Amsg_BicycleReturned_Billing ;
    dkg:exposedBy :Api_BillingConsumers ;
    dkg:source :Art_AsyncAPI_Billing_2026_10 ; dkg:locator "channels.rackBicycleEvents" ;
    dkg:confidence dkg:OnArtifact .

# IRI suffixed: Rack management's spec already minted :Amsg_BicycleReturned
:Amsg_BicycleReturned_Billing a dkg:ApiMessage ; skos:prefLabel "BicycleReturned" ;
    dkg:exposedBy :Api_BillingConsumers ;
    dkg:source :Art_AsyncAPI_Billing_2026_10 ; dkg:locator "components.messages.bicycleReturned" ;
    dkg:confidence dkg:OnArtifact ;
    dkg:specifies :Ev_BicycleReturned .                     # Rack management's event: a border

:Op_OnBicycleReturned a dkg:Operation ; skos:prefLabel "onBicycleReturned" ;
    dkg:operationId "onBicycleReturned" ; dkg:action "receive" ; dkg:messageKind "evt" ;
    dkg:onChannel :Ch_RackBicycleEvents ; dkg:carriesMessage :Amsg_BicycleReturned_Billing ;
    dkg:exposedBy :Api_BillingConsumers ;
    dkg:source :Art_AsyncAPI_Billing_2026_10 ; dkg:locator "operations.onBicycleReturned" ;
    dkg:confidence dkg:OnArtifact .
```

## Check and browse

`check_graph.py` adds, for specs:

- **errors** — an Api with no `dkg:inContext` or no `dkg:interactionStyle`; an
  Operation, Channel, ApiMessage or Schema with no `dkg:exposedBy`.
- **findings** — `style mismatch` (sync operation over an event; query on a
  channel), `border` (a `receive` of another context's event), `foreign model`
  (a `send` or sync operation specifying another context's element), `unlinked
  operation / message / schema` (the model never named it), `API straddles
  contexts`, and one `API coverage of <context>` line per context: how many of
  its commands and inbound rows have an operation, how many of its events and
  outbound `evt` rows have a message, and which are missing.

`views.py` adds the **interface lens**: per context, each Api as a box with its
style and protocol, sync operations as `METHOD /path`, channels as hexagons
with their messages, `send`/`receive` arrows, and dotted `specifies` edges out
to the model elements — followed by the coverage table. `views.py --patches
"<spec>"` adds two sections: *model elements of its context this spec does
not cover* and *its own elements the model never named*; `--patches` on a
canvas or board adds *contracts already written for its elements*, so a
redraw knows which renames break a published interface.

## Report additions

In this order, after the standard seven:

- **Which context it attached to, and how sure** — `Implied` from the title,
  or `Inferred` from the file name / the ingester. First line for this type.
  If the context did not exist, say the spec minted it.
- **Mode** — grow, or revision of which Api (by `x-api-id` or overlap).
- **Counts, per style** — n operations (k `cmd`, m `qry`) or n channels, k
  messages, j `send`, i `receive`; n schemas by role.
- **Coverage, both directions** — repeat the `API coverage` line, then the
  unlinked operations / messages / schemas by name. Say plainly whether
  the gaps look like internals (a board command that never leaves the
  context) or like silence (an operation no workshop asked for).
- **Style mismatches**, verbatim, both sources — these are the contradictions
  of this artifact type and belong under *What contradicts*.
- **Borders the spec draws** — every `receive`, with the producing context, and
  whether a canvas or board already drew that crossing. A border that exists
  only in a spec is the API-side twin of "a context that exists only because a
  canvas needed a receiver".
- **Decisions this spec honours or contradicts** — any Adopted Decision whose
  text names this context's crossings or technology, and whether the spec
  agrees. If the ADR log is not in the graph yet, say the question is open.
- **Spellings** — every Schema or Operation whose label differs from the
  Concept / Command it specifies (`Bike` / *Bicycle*): the spec has fixed a
  spelling the glossary did not choose, or the glossary moved on after the
  spec was written. Either way a patch for one of them.
