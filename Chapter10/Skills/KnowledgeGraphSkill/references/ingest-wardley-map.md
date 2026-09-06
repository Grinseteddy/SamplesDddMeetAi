# Ingest — Wardley Map

An anchor (user need) at the top, a value chain of components hanging beneath
it, each placed left-to-right on the evolution axis: Genesis · Custom-Built ·
Product · Commodity. Movement arrows optional.

A Wardley Map is the only artifact in the usual set that carries **positions**,
and positions are claims. Ingest them as claims: the value of having the map in
the graph is being able to ask, a year later, whether the component that was
going to commoditise did.

## Transcribe

1. The **anchor** — verbatim. If more than one, transcribe all; a map with two
   anchors is a finding worth reporting, not a thing to pick between.
2. The **value chain**, top to bottom, as a dependency list: what needs what.
3. Each component's **evolution stage**, and its horizontal position if the map
   is measured rather than sketched.
4. **Movement arrows**, with their target stage.
5. **Annotations** — inertia markers, climatic notes, pipeline boxes — as
   comments; there is no ontology term for them and inventing one would let a
   marginal note pass as structure.

## Map

| On the map | Class / property |
|---|---|
| Anchor / user need | `dkg:UserNeed` (subclass of `dkg:Goal`) |
| Component | `dkg:Component` (subclass of `dkg:Capability`) |
| Value-chain edge | `dkg:dependsOn` (higher → lower) |
| Evolution stage | `dkg:evolution` → `dkg:Genesis` / `dkg:CustomBuilt` / `dkg:Product` / `dkg:Commodity` |
| Horizontal position, if measured | `dkg:visibility` — decimal, only if actually measured |
| Movement arrow | `dkg:movesTo` → target stage |
| Component serving the anchor | `dkg:supports` → the user need |

Stage is `dkg:OnArtifact` when the component sits clearly inside a band, and
`dkg:Inferred` when it straddles a line — and it will straddle a line, because
people draw these by hand. Record which; a stage read off a straddling sticky
should not carry the same weight as one drawn in the middle of a band.

## Merge hazards

- **Components against Key Activities.** Both are `dkg:Capability`. Same label →
  auto-merge is allowed by the rule, but check first that the canvas activity is
  not a broader bundle; if it is, `dkg:dependsOn`, not merge.
- **Components against bounded contexts.** Tempting and usually wrong. A
  component is something you build or buy; a context is a boundary around a
  model. Where the names coincide, use `dkg:supports` and let the coincidence
  show in the report.
- **The anchor against BMC value propositions.** Related; not the same. A user
  need is the user's; a value proposition is the business's claim about it.
  Propose, and let someone look at the pair — the gap between them is often the
  most interesting sentence in a strategy review.

## Snippet

```turtle
:Art_Wardley_2026_02 a dkg:WardleyMap ;
    rdfs:label "Community Cooking value chain, v2" ;
    dkg:ingestedAt "2026-02-20"^^xsd:date .

:Goal_GetDinnerOnTheTable a dkg:UserNeed ;
    skos:prefLabel "get dinner on the table" ;
    dkg:source :Art_Wardley_2026_02 ; dkg:confidence dkg:OnArtifact .

:Cap_StrangerAnswersFast a dkg:Component ;
    skos:prefLabel "a stranger who answers fast" ;
    dkg:evolution dkg:Genesis ; dkg:movesTo dkg:CustomBuilt ;
    dkg:supports :Goal_GetDinnerOnTheTable ;
    dkg:dependsOn :Cap_Notification, :Cap_Identity ;
    dkg:source :Art_Wardley_2026_02 ; dkg:confidence dkg:OnArtifact .

:Cap_RecipeCatalogue a dkg:Component ;
    skos:prefLabel "recipe catalogue" ; dkg:evolution dkg:Commodity ;
    dkg:source :Art_Wardley_2026_02 ; dkg:confidence dkg:OnArtifact .
```

## Report additions

- **Components with no `dependsOn` in either direction** — floating, and either
  mis-drawn or genuinely detached.
- **Anything at Genesis that a later artifact treats as solved**, and anything at
  Commodity that the board models in detail. Both are contradictions between a
  strategy claim and a build; both belong in the report the moment the second
  artifact lands.
- **Movement arrows** listed on their own, as predictions with a date on them.