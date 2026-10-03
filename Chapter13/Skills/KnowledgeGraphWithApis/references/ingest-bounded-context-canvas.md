# Ingest — Bounded Context Canvas

One context, twelve fields (DDD Crew): name, purpose, strategic classification,
domain roles, inbound communication, outbound communication, ubiquitous
language, business decisions, assumptions, verification metrics, open questions
— and, on canvases written by `bounded-context-canvas-author`, a provenance
marker on every field. Arrives as a Mermaid picture, a rendered PNG, or the
Markdown file behind it. When the Markdown exists, ingest from the Markdown:
the picture caps what fits ("…and 10 more (see below)") and the file has the
rest.

Two things make a canvas different from every other artifact here:

- **It is about a context that almost always already exists in the graph.**
  A canvas is written *after* a board, an Event Model or a context cut named the
  context. The primary act of this ingest is therefore **enriching an existing
  `dkg:BoundedContext` node**, not minting one. A canvas for a context nothing
  else in the graph has ever drawn is unusual, and goes in the report.
- **It carries its own confidence, per field.** The four markers `(given)` /
  `(derived)` / `(proposed)` / `(unknown)` are on the canvas itself. Read them
  off each field and map them; never apply one confidence to the whole document.

| Canvas marker | `dkg:confidence` |
|---|---|
| `(given)` — a source states it | `dkg:OnArtifact` *(of the canvas; see the provenance note below)* |
| `(derived)` — read off a source by a shown argument | `dkg:Implied` |
| `(proposed)` — the author's own, nobody agreed | `dkg:Inferred` |
| `(unknown)` — nothing to derive from | **emit nothing.** The empty box is the finding; say so in the report, do not mint a node with an empty literal |

**Provenance note.** A canvas is a *derived analysis* (SKILL.md, "Ingesting
analyses"), so strictly everything it says is `dkg:Inferred` at best and sourced
to the canvas. The markers soften that rule in one direction only: a `(given)`
field names its own upstream source (a glossary, an invariants sheet), and where
that upstream artifact is **already in the graph**, merge into the upstream node
and let it keep its own confidence — the canvas becomes a second `dkg:source` on
it. Where the upstream is *not* in the graph, `(given)` still means "the canvas
author says a source exists"; record `dkg:OnArtifact` against the canvas and name
the claimed source in `rdfs:comment`, so the next ingest of that source can
confirm or contradict it.

## Transcribe

Field by field, in the canvas's order, verbatim, with its marker:

1. **Name** — exact spelling. If the assumptions say two map bubbles were read as
   this one canvas, both names.
2. **Purpose** — the sentence, whole.
3. **Strategic classification** — three lines: domain, business model,
   evolution. Each with its marker; `(unknown)` written as `(unknown)`.
4. **Domain roles** — the list, verbatim.
5. **Inbound communication** — one row per *collaborator × message*:
   collaborator · type (`cmd`/`qry`/`evt`/`?`) · message · what it carries · any
   annotation (`via ACL`).
6. **Outbound communication** — same shape, other direction.
7. **Ubiquitous language** — every term box with its annotation (`owned`,
   `borrowed — <owner>`, `map: <other name>`, `owned by nobody on the map`), and
   every arrow as *from · verb · to · cardinality*, verbatim.
8. **Business decisions** — each line, keeping the rejection wording and any
   citation in parentheses (`(INV-HELP-03)`, `(given)`).
9. **Assumptions** — each line.
10. **Verification metrics** — the line, or "none supplied".
11. **Open questions** — each line.

Show the transcription. A canvas is dense enough that a missed `qry`/`evt` or
a `0..10` read as `0..1` changes what the graph claims about a border.

## Map

| Canvas field | Class / property |
|---|---|
| Name | `dkg:BoundedContext` — **merge into the existing node** (see hazards); the canvas's spelling as `skos:altLabel` if it differs |
| Purpose | `rdfs:comment` on the context node |
| Strategic classification — domain | `dkg:Assertion` with `rdf:predicate rdfs:comment`, `rdf:object "core"` etc., sourced to the canvas, confidence from the marker — **the same pattern as a Core Domain Chart**, so a chart and a canvas that disagree show up as two dated claims |
| Strategic classification — business model | `dkg:businessModel` literal(s) on the context node |
| Strategic classification — evolution | `dkg:evolution` → `dkg:Genesis` / `dkg:CustomBuilt` / `dkg:Product` / `dkg:Commodity`; omit if `(unknown)` |
| Domain roles | `dkg:domainRole` literal(s), one per role, verbatim |
| Inbound row | `dkg:Message` — `dkg:from` the collaborator, `dkg:to` this context, `dkg:messageKind`, `dkg:carries`, `dkg:mentions` the Concept it is about, `dkg:inContext` this context |
| Outbound row | `dkg:Message` — `dkg:from` this context, `dkg:to` the collaborator, same properties |
| Collaborator that is another context | the existing `dkg:BoundedContext`, or a new one flagged in the report |
| Collaborator marked `via ACL` | `dkg:ExternalSystem` (existing if there is one); `rdfs:comment "via ACL"` on each Message that crosses it |
| Ubiquitous-language term box | `dkg:Term` (subclass of `dkg:Concept`), exactly as in `ingest-visual-glossary.md` |
| … marked `owned` | `dkg:inContext` → this context |
| … marked `borrowed — <owner>` | **no** `dkg:inContext` to this context; `rdfs:comment` holding the local sense; see the one-word-two-concepts hazard |
| … marked `owned by nobody on the map` / `unclaimed` | no `dkg:inContext` at all, and a `dkg:Question` asking who owns it |
| … marked `map: <name>` | an on-artifact identity claim — see hazards; becomes `skos:altLabel` on the node the canvas maps to |
| Arrow with cardinality | `dkg:Relationship`, reified, `dkg:cardinality` verbatim, `dkg:cardinalityGiven` true only if the canvas marks the language field `(given)`; plus a plain `dkg:mentions` |
| Business decision | `dkg:Rule`, `dkg:inContext` this context; `dkg:locator` carries any cited code |
| Assumption | `rdfs:comment` on the `dkg:BoundedContextCanvas` artifact node, one per line |
| Verification metric, if `(proposed)` or `(given)` | `dkg:metric` literal on the context node |
| Open question | `dkg:Question`, `dkg:raises` from the context node |

Every node minted or touched gets the canvas as a `dkg:source` and the field
name as `dkg:locator` (`"Inbound communication, row 3"`, `"Business decisions,
line 2"`).

**Messages are one node per row, deliberately.** The canvas puts the same
message name on two rows when two collaborators send it, and says out loud that
the repetition is a coupling fact. Three inbound rows named *Help response* are
three `dkg:Message` nodes, all `dkg:mentions :Con_HelpResponse`. Do not collapse
them into one "the Help response message"; `check_graph.py` knows to skip
Messages in its near-duplicate scan for exactly this reason.

## Merge hazards

- **The context itself.** Look for an existing `dkg:BoundedContext` first —
  from a board bubble, an Event Model swimlane, a story lane or a context cut.
  Contexts merge on *responsibility*, not name (`merging.md`): check that the
  canvas's messages and owned terms match what the graph already says the
  context does. If the canvas's assumptions say "the board's *Cooking
  Assistance* and *Cooking Help* are this one bubble", that is the canvas
  restating a `dkg:proposedSameAs` the graph should already hold; add the canvas
  as a source on it rather than deciding it. If no existing context matches,
  mint one, mark it `dkg:Inferred`, and put it first in the report.
- **`map: X` annotations.** The canvas is stating, on the artifact, that its
  local box *Help* is the graph's *Help response*. This is an **on-artifact
  identity claim, not the ingester's guess**, and it is the one place in this
  skill where a cross-label match merges without the propose step: use the
  existing node `X` if it exists with that exact label, add the canvas's local
  spelling as `skos:altLabel`, and write the ledger row with evidence "the
  canvas states the mapping itself". Still a ledger row — the room can overturn
  even a first-party claim.
- **Borrowed terms are the one-word-two-concepts test, pre-answered.** A
  borrowed term is the canvas saying "*Meal* here is not *Meal* there". Do not
  `dkg:inContext` it into this context, and do not merge its local sense away.
  If the canvas gives a local sense that differs from the owning context's
  (`"a described situation, never a pan"`), mint a suffixed node
  `:Con_Meal_CA` for the local sense with `dkg:proposedSameAs` to the owner's
  node and the difference in `rdfs:comment`. That pair is boundary evidence.
- **Business decisions that cite a code** — `(INV-HELP-03)` — are almost always
  a `dkg:Rule` already in the graph from an invariants-sheet ingest. Match on the
  cited code in `dkg:locator` before matching on wording; the citation is the
  identity claim. A decision that cites nothing and matches no Rule is new, and
  worth a line in the report.
- **Strategic classification against a Core Domain Chart.** Both produce
  `dkg:Assertion`s on the same subject with the same predicate. Two that agree
  are corroboration; two that differ are `dkg:contradicts`, with dates. A canvas
  that says `core (proposed, hypothesis)` where a chart said `supporting` is not
  an error in either — it is the finding.
- **Message concepts against board objects.** `dkg:mentions` from a Message
  should land on the existing `dkg:BusinessObject` / `dkg:ReadModel` /
  `dkg:Term`, by exact label. A message about a concept the graph does not have
  is a concept the board never wrote down — report it; do not mint a Concept to
  make the edge resolve.
- **Collaborators the graph does not have.** An outbound row to *Notification*
  when no artifact has ever drawn a Notification context means the canvas author
  assumed one. Mint it `dkg:Inferred`, sourced to the canvas, and report it as a
  context that exists only because a canvas needed a receiver.

## Snippet

```turtle
:Art_BCC_CookingAssistance_2026_09 a dkg:BoundedContextCanvas ;
    rdfs:label "Bounded Context Canvas — Cooking Assistance" ;
    rdfs:comment "Assumption: the board's Cooking Assistance and Cooking Help are this one bubble." ,
                 "Assumption: every sync arrow is a query issued by the arrowhead side; every dashed one an event." ;
    dkg:ingestedAt "2026-09-12"^^xsd:date .

# existing node, enriched — not re-minted
:Ctx_CookingAssistance
    rdfs:comment "When a cook is stuck, while planning or at the stove, takes the request, gets it answered by the community, a chef or the Grandma Avatar, and carries the answer back." ;
    dkg:domainRole "Gateway", "Engagement creator" ;
    dkg:businessModel "engagement creator" ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 .
    # evolution: (unknown) on the canvas — nothing emitted

:As_CookingAssistanceCore_BCC a dkg:Assertion ;
    rdf:subject :Ctx_CookingAssistance ; rdf:predicate rdfs:comment ; rdf:object "core" ;
    rdfs:comment "canvas marks it (proposed, hypothesis)" ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 ; dkg:confidence dkg:Inferred .

:Msg_In_MealPlanning_HelpResponse a dkg:Message ;
    skos:prefLabel "Help response" ; dkg:messageKind "qry" ;
    dkg:from :Ctx_MealPlanning ; dkg:to :Ctx_CookingAssistance ;
    dkg:mentions :Con_HelpResponse ; dkg:inContext :Ctx_CookingAssistance ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 ;
    dkg:locator "Inbound communication, row 1" ; dkg:confidence dkg:Implied .

:Msg_Out_GrandmaAvatarAI_HelpRequest a dkg:Message ;
    skos:prefLabel "Help request" ; dkg:messageKind "evt" ;
    dkg:from :Ctx_CookingAssistance ; dkg:to :Act_GrandmaAvatar ;
    rdfs:comment "via ACL" ;
    dkg:mentions :Con_HelpRequest ; dkg:inContext :Ctx_CookingAssistance ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 ;
    dkg:locator "Outbound communication, row 9" ; dkg:confidence dkg:Implied .

# "Help — map: Help response": on-artifact identity claim
:Con_HelpResponse skos:altLabel "Help" ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 .

:Rel_HelpContainsMenuProposal a dkg:Relationship ;
    dkg:from :Con_HelpResponse ; dkg:verb "contains" ; dkg:to :Con_MenuProposal ;
    dkg:cardinality "1..3" ; dkg:cardinalityGiven false ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 ;
    dkg:locator "Ubiquitous language" ; dkg:confidence dkg:Implied .

:Q_HowDoesAssistanceLearnCookIsStuck a dkg:Question ;
    skos:prefLabel "How does this context learn a cook is stuck — no trouble event crosses a border" ;
    dkg:source :Art_BCC_CookingAssistance_2026_09 ;
    dkg:locator "Open questions, line 1" ; dkg:confidence dkg:OnArtifact .
:Ctx_CookingAssistance dkg:raises :Q_HowDoesAssistanceLearnCookIsStuck .
```

## Report additions

- **Whether the context existed before this ingest**, and what it merged into.
  First line of the report for this artifact type.
- **Every `(unknown)` field**, named. Nothing was emitted for them; the report
  is the only place they appear.
- **Inbound and outbound counts, and the collaborators involved** — a context
  with six outbound queries has a wide interface, and the canvas's own open
  questions usually say so; repeat it.
- **Messages about concepts the graph does not hold**, and **collaborators the
  graph did not have** before this canvas.
- **Borrowed terms and unclaimed terms**, each with who the canvas says owns it
  (or that nobody does).
- **Strategic classification against any chart already in the graph** —
  agreement or `dkg:contradicts`, with both dates.
- **Business decisions with no matching Rule** elsewhere in the graph.
