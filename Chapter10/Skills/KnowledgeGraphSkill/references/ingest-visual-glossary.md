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