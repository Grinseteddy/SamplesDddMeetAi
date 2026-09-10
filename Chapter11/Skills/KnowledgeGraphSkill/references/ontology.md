# Ontology and emit rules

The vocabulary itself is `assets/dkg.ttl` — read it once; it is commented. This
file is how to *use* it.

## Contents

1. Prologue every graph starts with
2. IRI shape
3. Required properties on every node
4. Provenance
5. Reification — when, and only when
6. Spellings and renames
7. Worked snippets

---

## 1. Prologue

Every `graph.ttl` opens with exactly this, with `<project>` replaced by a short
kebab-case project name:

```turtle
@prefix dkg:  <https://w3id.org/dkg/ns#> .
@prefix :     <https://w3id.org/dkg/graph/community-cooking#> .
@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl:  <http://www.w3.org/2002/07/owl#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .

<https://w3id.org/dkg/graph/community-cooking>
    a owl:Ontology ;
    owl:imports <https://w3id.org/dkg/ns> ;
    rdfs:label "Community Cooking — domain knowledge graph" .
```

Neither IRI needs to resolve. If the organisation has its own namespace, change
the base here and nowhere else.

## 2. IRI shape

`:<Prefix>_<UpperCamelLabel>`, where the prefix marks the spine class. Readable
IRIs matter more than short ones — a person will read this file.

| Prefix | For |
|---|---|
| `Con_` | Concept, Term, WorkObject, Aggregate, ReadModel, BusinessObject |
| `Act_` | Actor, CustomerSegment, KeyPartner, ExternalSystem |
| `Cap_` | Capability, Component, KeyActivity |
| `Ev_`  | Occurrence, DomainEvent |
| `Cmd_` | Activity, Command |
| `Goal_`, `Imp_`, `Del_`, `Idea_` | Goal, Impact, Deliverable, Idea |
| `Ctx_` | BoundedContext, Lane, Swimlane |
| `Q_`   | Question, Hotspot |
| `R_`   | Rule, Policy |
| `Rel_` | glossary Relationship |
| `Art_` | Artifact |
| `As_`  | Assertion |

Slugging: strip punctuation, upper-camel the words, keep the artifact's own
spelling. `Meal plan settled` → `:Ev_MealPlanSettled`. `Catastrophy Pictures` →
`:Con_CatastrophyPictures` — **the misspelling is preserved in the IRI**, because
the IRI records what the artifact said and `skos:prefLabel` records what the team
decided to call it. If the team later agrees a spelling, do not rewrite the IRI;
change the prefLabel and note the rename.

Collisions: if two genuinely different things want one IRI, suffix the second
with the artifact's short code — `:Con_Pictures_ES` — and immediately record a
`dkg:contradicts` or a `dkg:proposedSameAs`. A collision is never resolved by
renaming alone.

## 3. Required on every node

```turtle
:Ev_MealPlanSettled a dkg:DomainEvent ;
    skos:prefLabel "Meal plan settled" ;
    dkg:source :Art_ESBoard_2026_03 ;
    dkg:locator "event 10" ;
    dkg:confidence dkg:OnArtifact .
```

Missing any of these is an **error** in `check_graph.py`, not a warning. A node
without a source cannot be traced back to a wall, and a node without a
confidence lets a guess pass as a transcription.

Confidence, in the same three words the rest of this toolkit uses:

- `dkg:OnArtifact` — a sticky, box, arrow or label says so.
- `dkg:Implied` — not written; the artifact does not make sense otherwise.
  (A command with no actor drawn, when only one actor appears anywhere.)
- `dkg:Inferred` — domain knowledge from outside the artifact. Needs a human.

Everything derived from an *analysis document* is `dkg:Inferred` at best.

## 4. Provenance

One `dkg:Artifact` individual per ingest, minted before the nodes:

```turtle
:Art_ESBoard_2026_03 a dkg:EventStormingBoard ;
    rdfs:label "Community Cooking EventStorming board" ;
    rdfs:comment "Three photos, stitched; 21 events in three rows." ;
    dkg:ingestedAt "2026-03-04"^^xsd:date .
```

Repeat `dkg:source` on a node for every artifact that mentions it. The count is
the browsable measure of corroboration, and `views.py` uses it: a Concept with
four sources is the domain's spine; a Concept with one is a proposal.

`dkg:locator` should be specific enough to point at on the wall: `"sentence 3"`,
`"event 10"`, `"Key Activities, third sticky"`, `"image 2, Cook Assistance lane"`.

## 5. Reification — when, and only when

Direct triples are the default. Reify an edge as a `dkg:Assertion` in exactly
three cases:

1. more than one artifact asserts the same edge, and the corroboration matters;
2. two artifacts disagree;
3. the edge is contested — an interpretation, not a reading.

```turtle
:As_AvatarInsideAssistance a dkg:Assertion ;
    rdf:subject :Act_GrandmaAvatar ; rdf:predicate dkg:inContext ;
    rdf:object :Ctx_CookAssistance ;
    dkg:source :Art_ContextCut_2026_03 ; dkg:confidence dkg:Inferred .

:As_AvatarOwnLane a dkg:Assertion ;
    rdf:subject :Act_GrandmaAvatar ; rdf:predicate dkg:inContext ;
    rdf:object :Ctx_AIAvatarLane ;
    dkg:source :Art_EventModel_2026_03 ; dkg:confidence dkg:OnArtifact ;
    dkg:contradicts :As_AvatarInsideAssistance .
```

Note that both direct triples are *also* asserted. The reification carries the
argument; the plain triples keep the graph traversable. A browsable graph whose
edges all hide inside reifications is not browsable.

Reifying everything is the most common way to ruin this file. Resist it.

## 6. Spellings and renames

Merged nodes keep every spelling, and which artifact used it:

```turtle
:Con_CatastrophePictures a dkg:Concept ;
    skos:prefLabel "Catastrophe Pictures" ;
    skos:altLabel "Catastrophy Pictures", "Pictures" ;
    dkg:source :Art_DomainStoryA, :Art_ESBoard_2026_03 .

[] a dkg:spelling ; dkg:literal "Catastrophy Pictures" ;
   dkg:spelledOn :Art_DomainStoryA ; rdfs:comment "diagram's own spelling" .
```

Use the `dkg:spelling` blank node only where the *disagreement between artifacts
is itself the finding*. For ordinary synonyms, `skos:altLabel` is enough.

A noun re-modelled across a boundary is not a synonym:

```turtle
:Con_User dkg:renamedTo :Con_Cook .
```# Ontology and emit rules

The vocabulary itself is `assets/dkg.ttl` — read it once; it is commented. This
file is how to *use* it.

## Contents

1. Prologue every graph starts with
2. IRI shape
3. Required properties on every node
4. Provenance
5. Reification — when, and only when
6. Spellings and renames
7. Worked snippets

---

## 1. Prologue

Every `graph.ttl` opens with exactly this, with `<project>` replaced by a short
kebab-case project name:

```turtle
@prefix dkg:  <https://w3id.org/dkg/ns#> .
@prefix :     <https://w3id.org/dkg/graph/community-cooking#> .
@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix owl:  <http://www.w3.org/2002/07/owl#> .
@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .

<https://w3id.org/dkg/graph/community-cooking>
    a owl:Ontology ;
    owl:imports <https://w3id.org/dkg/ns> ;
    rdfs:label "Community Cooking — domain knowledge graph" .
```

Neither IRI needs to resolve. If the organisation has its own namespace, change
the base here and nowhere else.

## 2. IRI shape

`:<Prefix>_<UpperCamelLabel>`, where the prefix marks the spine class. Readable
IRIs matter more than short ones — a person will read this file.

| Prefix | For |
|---|---|
| `Con_` | Concept, Term, WorkObject, Aggregate, ReadModel, BusinessObject |
| `Act_` | Actor, CustomerSegment, KeyPartner, ExternalSystem |
| `Cap_` | Capability, Component, KeyActivity |
| `Ev_`  | Occurrence, DomainEvent |
| `Cmd_` | Activity, Command |
| `Goal_`, `Imp_`, `Del_`, `Idea_` | Goal, Impact, Deliverable, Idea |
| `Ctx_` | BoundedContext, Lane, Swimlane |
| `Q_`   | Question, Hotspot |
| `R_`   | Rule, Policy |
| `Rel_` | glossary Relationship |
| `Art_` | Artifact |
| `As_`  | Assertion |

Slugging: strip punctuation, upper-camel the words, keep the artifact's own
spelling. `Meal plan settled` → `:Ev_MealPlanSettled`. `Cooking Assistance` →
`:Ctx_CookingAssistance` — **the minting artifact's wording is preserved in the
IRI**, because the IRI records what was on the wall and `skos:prefLabel` records
what the team decided to call it. If the team later agrees on *Cook Assistance*,
do not rewrite the IRI; change the prefLabel, add the old wording as an
`skos:altLabel`, and note the rename in the report.

Collisions: if two genuinely different things want one IRI, suffix the second
with the artifact's short code — `:Con_Pictures_ES` — and immediately record a
`dkg:contradicts` or a `dkg:proposedSameAs`. A collision is never resolved by
renaming alone.

## 3. Required on every node

```turtle
:Ev_MealPlanSettled a dkg:DomainEvent ;
    skos:prefLabel "Meal plan settled" ;
    dkg:source :Art_ESBoard_2026_03 ;
    dkg:locator "event 10" ;
    dkg:confidence dkg:OnArtifact .
```

Missing any of these is an **error** in `check_graph.py`, not a warning. A node
without a source cannot be traced back to a wall, and a node without a
confidence lets a guess pass as a transcription.

Confidence, in the same three words the rest of this toolkit uses:

- `dkg:OnArtifact` — a sticky, box, arrow or label says so.
- `dkg:Implied` — not written; the artifact does not make sense otherwise.
  (A command with no actor drawn, when only one actor appears anywhere.)
- `dkg:Inferred` — domain knowledge from outside the artifact. Needs a human.

Everything derived from an *analysis document* is `dkg:Inferred` at best.

## 4. Provenance

One `dkg:Artifact` individual per ingest, minted before the nodes:

```turtle
:Art_ESBoard_2026_03 a dkg:EventStormingBoard ;
    rdfs:label "Community Cooking EventStorming board" ;
    rdfs:comment "Three photos, stitched; 21 events in three rows." ;
    dkg:ingestedAt "2026-03-04"^^xsd:date .
```

Repeat `dkg:source` on a node for every artifact that mentions it. The count is
the browsable measure of corroboration, and `views.py` uses it: a Concept with
four sources is the domain's spine; a Concept with one is a proposal.

`dkg:locator` should be specific enough to point at on the wall: `"sentence 3"`,
`"event 10"`, `"Key Activities, third sticky"`, `"image 2, Cook Assistance lane"`.

## 5. Reification — when, and only when

Direct triples are the default. Reify an edge as a `dkg:Assertion` in exactly
three cases:

1. more than one artifact asserts the same edge, and the corroboration matters;
2. two artifacts disagree;
3. the edge is contested — an interpretation, not a reading.

```turtle
:As_AvatarInsideAssistance a dkg:Assertion ;
    rdf:subject :Act_GrandmaAvatar ; rdf:predicate dkg:inContext ;
    rdf:object :Ctx_CookAssistance ;
    dkg:source :Art_ContextCut_2026_03 ; dkg:confidence dkg:Inferred .

:As_AvatarOwnLane a dkg:Assertion ;
    rdf:subject :Act_GrandmaAvatar ; rdf:predicate dkg:inContext ;
    rdf:object :Ctx_AIAvatarLane ;
    dkg:source :Art_EventModel_2026_03 ; dkg:confidence dkg:OnArtifact ;
    dkg:contradicts :As_AvatarInsideAssistance .
```

Note that both direct triples are *also* asserted. The reification carries the
argument; the plain triples keep the graph traversable. A browsable graph whose
edges all hide inside reifications is not browsable.

Reifying everything is the most common way to ruin this file. Resist it.

## 6. Spellings and renames

Merged nodes keep every spelling, and which artifact used it:

```turtle
:Act_GrandmaAvatar a dkg:ExternalSystem ;
    skos:prefLabel "Grandma Avatar" ;
    skos:altLabel "AI Avatar" ;
    dkg:source :Art_DomainStoryA, :Art_EventModel_2026_03 .

[] a dkg:spelling ; dkg:literal "AI Avatar" ;
   dkg:spelledOn :Art_EventModel_2026_03 ;
   rdfs:comment "the Event Model's swimlane name; all four stories say Grandma Avatar" .
```

Use the `dkg:spelling` blank node only where the *disagreement between artifacts
is itself the finding*. For ordinary synonyms, `skos:altLabel` is enough.

A noun re-modelled across a boundary is not a synonym:

```turtle
:Con_User dkg:renamedTo :Con_Cook .
```

That edge is boundary evidence. `views.py` draws it in the language lens and
`pivotal-event-boundary-finder` would call it a language shift.

## 7. Worked snippets

An idea that survived into a deliverable and then into an event — the trace the
whole graph exists to make walkable:

```turtle
:Idea_AskAStranger a dkg:Idea ;
    skos:prefLabel "ask a stranger mid-cook" ;
    dkg:inCluster :Cluster_Rescue ; dkg:votes 6 ;
    dkg:source :Art_Brainstorm_2026_01 ; dkg:locator "cluster 3, sticky 4" ;
    dkg:confidence dkg:OnArtifact .

:Del_HelpRequestFlow a dkg:Deliverable ;
    skos:prefLabel "in-app help request" ;
    dkg:realises :Imp_CookAsksInsteadOfGivingUp ;
    dkg:source :Art_ImpactMap_2026_02 ; dkg:confidence dkg:OnArtifact .

:Idea_AskAStranger dkg:proposedSameAs :Del_HelpRequestFlow .
```

The last line is the honest form. The brainstorm sticky and the impact-map
deliverable are *probably* the same intent, and until someone says so, the graph
records a proposal rather than a fact.

That edge is boundary evidence. `views.py` draws it in the language lens and
`pivotal-event-boundary-finder` would call it a language shift.

## 7. Worked snippets

An idea that survived into a deliverable and then into an event — the trace the
whole graph exists to make walkable:

```turtle
:Idea_AskAStranger a dkg:Idea ;
    skos:prefLabel "ask a stranger mid-cook" ;
    dkg:inCluster :Cluster_Rescue ; dkg:votes 6 ;
    dkg:source :Art_Brainstorm_2026_01 ; dkg:locator "cluster 3, sticky 4" ;
    dkg:confidence dkg:OnArtifact .

:Del_HelpRequestFlow a dkg:Deliverable ;
    skos:prefLabel "in-app help request" ;
    dkg:realises :Imp_CookAsksInsteadOfGivingUp ;
    dkg:source :Art_ImpactMap_2026_02 ; dkg:confidence dkg:OnArtifact .

:Idea_AskAStranger dkg:proposedSameAs :Del_HelpRequestFlow .
```

The last line is the honest form. The brainstorm sticky and the impact-map
deliverable are *probably* the same intent, and until someone says so, the graph
records a proposal rather than a fact.