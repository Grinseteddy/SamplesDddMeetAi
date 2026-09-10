# Ingest — Business Model Canvas

Nine blocks (Osterwalder & Pigneur). The canvas is where the graph first learns
who the customer is and what the business claims to be worth, so it supplies
`dkg:Actor` and `dkg:ValueProposition` nodes that everything downstream merges
against.

## Transcribe

Block by block, in canvas order, each sticky on its own line, verbatim. Record
**empty blocks as empty** — a canvas with no Key Partners is a finding, and
filling it from context destroys it.

Canvas order for the transcription: Customer Segments · Value Propositions ·
Channels · Customer Relationships · Revenue Streams · Key Resources ·
Key Activities · Key Partnerships · Cost Structure.

## Map

| Block | Class | Also type as |
|---|---|---|
| Customer Segments | `dkg:CustomerSegment` | `dkg:Actor` (by subclass) |
| Value Propositions | `dkg:ValueProposition` | — |
| Channels | `dkg:Channel` | — |
| Customer Relationships | `dkg:CustomerRelationship` | — |
| Revenue Streams | `dkg:RevenueStream` | — |
| Key Resources | `dkg:KeyResource` | `dkg:Concept` if it is a domain noun |
| Key Activities | `dkg:KeyActivity` | `dkg:Capability` (by subclass) |
| Key Partnerships | `dkg:KeyPartner` | `dkg:Actor` (by subclass) |
| Cost Structure | `dkg:CostStructure` | — |

Edges — **only where the canvas draws or states them.** Most canvases do not,
and inventing the fit between blocks is exactly the analysis the
`business-model-canvas-critic` skill exists to do properly.

- `dkg:servesSegment` — VP → segment, when lines are drawn or the sticky says so
- `dkg:deliveredVia` — VP → channel
- `dkg:paidBy` — VP → revenue stream
- `dkg:supports` — key activity/resource → VP

Where the canvas does *not* connect blocks, leave them unconnected and let the
orphan report say so. An unconnected Value Proposition in the report is a real
question; a fabricated `dkg:servesSegment` edge is a lie with a source on it.

## Merge hazards

- **Segments are the big one.** Canvas segments are named in marketing language
  ("busy home cooks"); board and story actors are named in operational language
  ("Cook"). These are usually the same person and never the same string.
  Propose, do not merge, and put the pair in the ledger — because if they are
  *not* the same, the product is being built for someone the business did not
  name.
- **Key Activities against Commands.** "match cooks to helpers" is strategy;
  `Provide help` is implementation. Related, not identical. `dkg:supports`, not
  `dkg:proposedSameAs`, unless the wording really is the same.
- **Value Propositions against Goals.** A VP is a claim of worth; a Goal has a
  measure. Do not type a VP as a Goal even when it sounds like one.

## Snippet

```turtle
:Art_BMC_2026_02 a dkg:BusinessModelCanvas ;
    rdfs:label "Community Cooking BMC, session 2" ;
    rdfs:comment "Key Partnerships block empty." ;
    dkg:ingestedAt "2026-02-06"^^xsd:date .

:Act_BusyHomeCooks a dkg:CustomerSegment ;
    skos:prefLabel "busy home cooks" ;
    dkg:source :Art_BMC_2026_02 ; dkg:locator "Customer Segments, sticky 1" ;
    dkg:confidence dkg:OnArtifact .

:VP_NeverRuinDinner a dkg:ValueProposition ;
    skos:prefLabel "never ruin dinner again" ;
    dkg:servesSegment :Act_BusyHomeCooks ;
    dkg:source :Art_BMC_2026_02 ; dkg:confidence dkg:OnArtifact .

:Act_BusyHomeCooks dkg:proposedSameAs :Act_Cook ;
    rdfs:comment "marketing segment vs operational actor on the ES board" .
```

## Report additions

**Empty blocks, and every block whose stickies connect to nothing else in the
graph.** Also worth naming: any segment with no actor anywhere downstream, and
any revenue stream with no capability supporting it. Both are cheap to spot here
and expensive to discover later.