# Ingest — Capability Map and Core Domain Chart

Two artifacts, one file, because they share a vocabulary: both say what the
business can do and how much of it is worth doing ourselves.

## Capability Map

A hierarchy of capabilities, usually two or three levels, sometimes marked
core / supporting / generic.

| On the map | Class / property |
|---|---|
| A capability | `dkg:Capability` |
| Level-2 under level-1 | `skos:broader` |
| core / supporting / generic marking | `dkg:evolution` is **wrong here** — use `rdfs:comment` plus the Core Domain Chart classes below if the marking is drawn |

Capability maps are frequently drawn without markings. Ingest exactly what is
there; the classification is `capability-map-critic`'s work, and a marking that
arrives from an analysis is `dkg:Inferred` and sourced to the analysis.

Merge hazards: capabilities collide with Wardley components and BMC key
activities, all three being `dkg:Capability`. Auto-merge on exact label; check
the level first, because a level-1 capability is usually a bundle of what a
Wardley map draws as several components — `skos:broader`, not merge.

## Core Domain Chart

Positions on two axes — business differentiation × model complexity — with
core / supporting / generic types, and sometimes arrows from current to target
position.

| On the chart | How to record |
|---|---|
| A plotted (sub)domain or capability | `dkg:Capability`, or merge into the existing node |
| core / supporting / generic | `rdfs:comment "core"` and a `dkg:Assertion` carrying the claim, its source and confidence |
| Current → target arrow | two `dkg:Assertion`s, the second commented as target |
| Axis positions, if measured | `dkg:visibility` only if genuinely measured; otherwise omit |

Use assertions rather than a bare property because a chart's classification is a
**claim with a date on it**, and the whole point of holding two charts in one
graph is being able to see that a capability was called supporting in March and
core in September. A plain property would overwrite that.

```turtle
:As_AssistanceCore a dkg:Assertion ;
    rdf:subject :Cap_StrangerAnswersFast ; rdf:predicate rdfs:comment ;
    rdf:object "core" ;
    dkg:source :Art_CoreDomainChart_2026_03 ; dkg:confidence dkg:OnArtifact .
```

## Report additions

- **Capabilities marked core that no Impact Map deliverable touches**, and
  capabilities marked generic that the EventStorming board models in detail.
  Both are contradictions between what the business says it differentiates on
  and where the modelling effort went, and both are only visible once these
  artifacts share a graph. This is one of the strongest reasons to ingest a
  chart at all.
- **Any capability whose classification changed** between two charts, with both
  dates.