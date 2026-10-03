# Ingest — Visual Glossary

Concept boxes joined by verb-labelled arrows carrying multiplicities (`1`,
`0..1`, `1..*`, `0..25`). The glossary is the graph's **authority on vocabulary
and shape**: once it is in, every other artifact's terms are checked against it,
and the language lens becomes worth opening.

## Transcribe

1. Every term, exactly as spelled, with its box colour or grouping if the
   glossary uses colour for bounded contexts.
2. Every relationship as a triple: *from · verb · to*, plus the cardinality **at
   each end**, verbatim.
3. Any is-a / specialisation edges.
4. Any term referenced by an arrow but never given its own box — these matter,
   because they are usually a whole context living off-diagram.

## Map

| In the glossary | Class / property |
|---|---|
| A term box | `dkg:Term` (subclass of `dkg:Concept`) |
| Marked as entity / value object / aggregate root | `dkg:Entity` / `dkg:ValueObject` / `dkg:AggregateRoot` |
| A verb-labelled arrow | `dkg:Relationship`, reified |
| Multiplicity | `dkg:cardinality`, **verbatim string** |
| Drawn vs derived multiplicity | `dkg:cardinalityGiven` true/false |
| is-a | `rdfs:subClassOf` between the two terms |
| Colour group / context | `dkg:inContext` → `dkg:BoundedContext` |

Relationships are reified because both the verb and the counts carry meaning:

```turtle
:Rel_MealPlanSelectsRecipe a dkg:Relationship ;
    dkg:from :Con_MealPlan ; dkg:verb "selects" ; dkg:to :Con_Recipe ;
    dkg:cardinality "1..*" ; dkg:cardinalityGiven true ;
    dkg:source :Art_Glossary_2026_03 ; dkg:confidence dkg:OnArtifact .

:Con_MealPlan dkg:mentions :Con_Recipe .
```

The plain `dkg:mentions` alongside it keeps the graph traversable; without it the
language lens has to walk through reifications to find neighbours.

**`dkg:cardinalityGiven` is not optional.** A cardinality the glossary drew is a
business rule the team agreed; one you worked out from a screenshot is a guess.
The difference is the entire reason schema-vs-glossary checks find anything, and
collapsing it turns guesses into requirements.

## Merge hazards

- **This ingest will collide with everything**, because a glossary is nothing
  but nouns. That is fine and expected: the terms *should* merge with story work
  objects and board business objects. Auto-merge on exact match, propose on
  near-miss, and let the ledger be long.
- **A glossary term matching a board read model** is usually a real merge and
  occasionally a trap: `Recipe` the term and `Recipe` the read model may be one
  concept projected, or a catalogue entry versus a whole catalogue. Check what
  the arrows say before merging.
- **Terms the glossary defines that no other artifact uses.** Not a merge
  problem; a finding. Report them.
- **Where the glossary contradicts an already-ingested cardinality** — a story
  showing one cook with several plans against a glossary saying `0..1` — do not
  reconcile. Two reified relationships, `dkg:contradicts` between them, and into
  the report.

## Several glossaries, one per bounded context

Once the vocabulary is split by context, every glossary is partial by design,
and the questions change from *"is this term defined?"* to *"whose is it?"*

**Ownership rule.** A term is minted by the glossary of the context that
**owns** it — drawn in that glossary's own colour, per the legend the team
adopted (SADR0005 in this project: every glossary carries a legend). Any other
glossary that shows the same term as borrowed (dashed, grey, another context's
colour) does **not** mint it: it adds itself as a `dkg:source` on the owner's
node, adds `rdfs:comment "borrowed in <context>: <local sense>"` if the sense
differs, and does **not** add `dkg:inContext` for its own context. The owner's
`inContext` is the only one; a term with two `inContext`s is a straddler the
flow lens will flag, and here that is a modelling error, not a finding.

**When the legend does not say.** Two glossaries both draw the term solid, or
neither has a legend: mint in the first ingested, propose `dkg:proposedSameAs`
from the second, and raise a `dkg:Question` — *"which context owns X?"* — with
both glossaries as sources. Do not decide it. This is precisely the case the
per-context canvas's *borrowed / owned by nobody* annotations exist to answer,
so if a canvas for either context is in the graph, cite it in the question.

**Relationships across glossaries.** An arrow from an owned term to a borrowed
one (*Help Request belongs 1 Meal*, where Meal is owned elsewhere) is a
`dkg:Relationship` sourced to the glossary that drew it, `dkg:inContext` that
glossary's context — the arrow is the border contract, and its cardinality is
the drawing context's claim about someone else's term. If the owner's glossary
draws the same arrow with a different cardinality, that is `dkg:contradicts`
with both sources, and it is the most valuable thing a per-context split can
surface.

**Ingest order.** Owners before borrowers where you can tell; otherwise any
order, because the borrower's node is only ever a proposal until the owner
arrives and confirms it. Report, per glossary: terms it owns, terms it borrows
and from whom, terms nobody owns yet.

**Revision detection with several glossaries** — `check_graph.py --overlap
all labels.txt` scores every artifact in the register; a new per-context
glossary scoring a third or more against the *domain-wide* glossary is a
partition, not a revision — `reingest-revision.md` §0b.

## When the glossary is derived rather than given

Sometimes the only glossary is one reconstructed backwards from a schema or a
prototype. Ingest it, but mark **every** node `dkg:Inferred` and every
`dkg:cardinalityGiven` false, and name it as derived in the artifact's
`rdfs:comment`. A derived glossary that later gets treated as the agreed
vocabulary is one of the more expensive mistakes available here.

## Report additions

- **Terms with no counterpart elsewhere in the graph**, and graph concepts the
  glossary does not define. Both lists, both directions.
- **Every cardinality that contradicts another artifact.**
- **Terms referenced by an arrow but never boxed** — the off-diagram contexts.

## A refined glossary — redrawn from a schema, one per bounded context

`visual-glossary-updater` takes an earlier glossary and the context's schema
(the OpenAPI / AsyncAPI the graph may already hold) and redraws the glossary
for that one context. It hands back three things, and the ingest wants all of
them:

- the **model file** (`model.yaml`) — terms with a colour-group key, edges with
  verb, cardinality and `kind` (`assoc` / `is-a` / `cross`), a `contexts`
  legend saying which colour is *owned and described here* and which is
  *referenced only*. Machine-readable: `scripts/glossary_inventory.py` reads it;
- the **changelog** — removed / changed / added, each naming the schema
  construct that forced it, and a *keep despite absence* list;
- the **SVG**, which is what the room looks at and what you check a locator
  against.

**Mode is always revise.** The refined glossary is a later drawing of a glossary
already in the register — `dkg:revisionOf` it — or, when the earlier one was
domain-wide, a partition of it (`reingest-revision.md` §0b: one piece per
context, removal computed once all pieces are in). Run the overlap test
anyway; it is cheap and it catches the case where the "earlier glossary" the
updater was given was never ingested.

**Confidence is split, and the changelog is what splits it.** This is not the
*"derived rather than given"* case above — nobody reconstructed a vocabulary
from code; a team's glossary was brought up to date against a schema the same
team wrote. So:

| Changelog bin | Term / edge | `dkg:confidence` | plus |
|---|---|---|---|
| unchanged (not in the changelog) | existing node | unchanged | revision added as `dkg:source` |
| renamed / reshaped | existing node | unchanged | new `skos:prefLabel`, old as `skos:altLabel`; a changed cardinality is a new Relationship `dkg:supersedes` the old |
| added, *from the schema* | new node | `dkg:Implied` | `dkg:derivedFrom` → the `dkg:Schema` node when the spec is in the graph, else the spec artifact; `dkg:cardinalityGiven false` on its edges — an array is `0..*` because JSON says so, not because the business does |
| added, *because the room said so* (the changelog cites a conversation, not a construct) | new node | `dkg:OnArtifact` | — |
| kept despite absence | existing node | unchanged | `dkg:unrepresentedIn` → the spec artifact |
| removed | existing node stays | unchanged | `dkg:absentFrom` → the refined glossary; `rdfs:comment` quoting the changelog's reason |

If the user says the redraw was reviewed and accepted in a session, every
*added* becomes `dkg:OnArtifact` and every schema-lifted cardinality
`dkg:cardinalityGiven true` — say so in the artifact's `rdfs:comment`, with the
date, because that sentence is what turns lifted structure into agreed
vocabulary and somebody will want to know when it happened.

**Colour is ownership.** The model's *owned* colour → `dkg:inContext` this
context, exactly as the per-context rule above. The *referenced* colour →
the term is somebody else's: no `dkg:inContext` here, `dkg:source` added on
the owner's existing node, or a `dkg:Question` "who owns X?" when the graph
has no owner. A term the changelog says *moved* from referenced to owned
(`Ingredient`, now described locally by value) is the one glossary event that
changes ownership: emit a dated `dkg:Assertion` (`rdf:predicate
dkg:inContext`) sourced to the refined glossary, keep the owner's own
`inContext`, and report it as the anti-corruption layer it is.

**`cross` edges are border contracts.** An edge of `kind: cross` is this
context's claim about a relationship to a term it does not own — the same
`dkg:Relationship`, `dkg:inContext` this context, and the usual
`dkg:contradicts` if the owner's glossary draws it with another cardinality.

**`is-a` edges** are `rdfs:subClassOf` between the two terms, as always; a
subtype family the schema's `allOf` produced is `dkg:derivedFrom` the base
schema.

**Against the spec.** The refined glossary and the spec are now two
descriptions of one context. A term `dkg:derivedFrom` a Schema and a Schema
`dkg:specifies` a term are the same link from two ends; `check_graph.py` does
not require both, but a refined-glossary term whose Schema specifies a
*different* term is a near-duplicate it will flag. And a Schema with no term
at all after the refined glossary is in — the updater decided it was a
wrapper, an envelope, an identifier — stays *unlinked* in the checker's
report, which is correct: the glossary looked at it and declined.

```bash
python3 scripts/glossary_inventory.py model.yaml                           # transcription
python3 scripts/glossary_inventory.py model.yaml --labels labels.txt      # for --overlap
python3 scripts/glossary_inventory.py model.yaml --ttl \
    --artifact Art_Glossary_RackManagement_2026_10 --context Ctx_RackManagement \
    --owned rack --date 2026-10-06 --revision-of Art_Glossary --graph graph.ttl \
    [--spec Art_OpenAPI_RackManagement_2026_10] [--reviewed] > ingest.ttl
```

With `--graph` and `--revision-of`, the skeleton is the revision diff: every
term and edge binned *unchanged / changed / added / removed* against the
nodes sourced to the earlier glossary (lineage-loose matching: case,
punctuation, singular/plural fold), the right triples per bin, `derivedFrom`
drafted for added terms whose stem matches a Schema of `--spec`, and the
removed/added lists printed side by side on stderr because a pair there is
usually one rename — the one bin the script will not decide. Renames go in the
ledger by hand. `--partition` suppresses `absentFrom` and prints *removal
pending* instead.

### Report additions for a refined glossary

- **Lineage** — revision of which glossary, partition or not, and whether the
  earlier one was in the graph.
- **The diff** — counts per bin, then the removed/added pairing with the
  rename decisions.
- **Terms derived from the schema** vs **terms kept despite the schema** —
  the two lists the changelog exists for; the second is the business
  knowledge the API does not carry.
- **Ownership moves** (referenced → owned) and borrowed terms with no owner.
- **Cardinalities that changed** — each with old, new, and whether the new one
  was drawn or lifted.
- **Schemas the refined glossary declined to name** — the updater's "don't
  import" list, as the checker's remaining unlinked schemas.
