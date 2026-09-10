# Ingest — Impact Map

Four levels (Adzic): **WHY** a measurable goal · **WHO** the actors · **HOW**
the behaviour changes · **WHAT** the deliverables.

The Impact Map is the graph's **spine for traceability**, because it is the only
common artifact that explicitly links a thing you could build to a person whose
behaviour it should change to an outcome you could measure. Once it is in, the
traceability lens works; before it, the lens is mostly gaps.

## Transcribe

The tree, indented, level by level. Preserve the branching exactly — which
impacts hang off which actor, which deliverables off which impact. A deliverable
appearing under two impacts stays under both.

Record the goal's **metric verbatim**, and if there is none, record that there is
none. A goal without a measure is the single most common defect in these maps
and the graph should show it rather than paper over it.

## Map

| Level | Class | Edge |
|---|---|---|
| WHY | `dkg:Goal` | `dkg:metric` as a literal, if stated |
| WHO | `dkg:Actor` | `dkg:impactedActor` from the impact |
| HOW | `dkg:Impact` | `dkg:towardsGoal` → the goal |
| WHAT | `dkg:Deliverable` | `dkg:realises` → the impact |

The full chain reads: `Deliverable —realises→ Impact —impactedActor→ Actor`,
`Impact —towardsGoal→ Goal`.

**Do not repair the map while ingesting.** If a "deliverable" is really a
feature name and the impact is really a feature too, ingest them as drawn and
let the report say so. `impact-mapping-critic` is the skill for judging the map;
this one records it. Mixing the two produces a graph that quietly contains
somebody's opinion.

## Merge hazards

- **Actors are the richest merge point in the whole graph.** Impact-map actors,
  BMC segments, story actors and board actors all land in `dkg:Actor`. Exact
  matches auto-merge; everything else proposes. Expect this ingest to generate
  the longest merge ledger of the set, and do not shorten it.
- **Deliverables against brainstorm ideas.** The trace people most want and the
  one most easily faked. Propose with the reason written out; a deliverable that
  matches no idea is worth reporting, because it means work appeared without a
  session behind it.
- **Deliverables against Wardley components.** A deliverable is a piece of work;
  a component is a thing that exists. `dkg:supports`, not merge, unless the
  labels are identical.
- **Goals against BMC value propositions and Wardley anchors.** Three artifacts,
  three vocabularies, one intent — usually. Propose all three ways and let the
  room collapse them; the shape of that disagreement is a strategy finding.

## Snippet

```turtle
:Art_ImpactMap_2026_02 a dkg:ImpactMap ;
    rdfs:label "Q1 impact map" ; dkg:ingestedAt "2026-02-27"^^xsd:date .

:Goal_RescuedDinners a dkg:Goal ;
    skos:prefLabel "fewer abandoned dinners" ;
    dkg:metric "abandoned cooking sessions per 100 starts, from 18 to 8" ;
    dkg:source :Art_ImpactMap_2026_02 ; dkg:confidence dkg:OnArtifact .

:Imp_CookAsksInsteadOfGivingUp a dkg:Impact ;
    skos:prefLabel "asks for help instead of giving up" ;
    dkg:impactedActor :Act_Cook ; dkg:towardsGoal :Goal_RescuedDinners ;
    dkg:source :Art_ImpactMap_2026_02 ; dkg:confidence dkg:OnArtifact .

:Del_HelpRequestFlow a dkg:Deliverable ;
    skos:prefLabel "in-app help request" ;
    dkg:realises :Imp_CookAsksInsteadOfGivingUp ;
    dkg:source :Art_ImpactMap_2026_02 ; dkg:confidence dkg:OnArtifact .
```

## Report additions

- **Goals with no metric**, named individually.
- **Actors on the map that appear nowhere else in the graph**, and actors
  elsewhere in the graph that the map never impacts. The second list is usually
  the more surprising one.
- **Deliverables with no brainstorm ancestor**, once a brainstorm is in the
  graph — not a defect, but worth a sentence.