# Browsing the graph

The graph is for reading. `scripts/views.py` renders five lenses plus a register
and an orphan report into `views/index.md`; this file says what each lens is
*for*, and gives the queries for the questions people actually ask out loud.

Run all of it with `python3 scripts/views.py graph.ttl --out views/`, or one
lens with `--lens language`.

## The five lenses

**1. Strategy** — `Goal ← Impact ← Actor` and `Deliverable → Impact`, plus BMC
value propositions and segments. Answers *"what are we trying to change, in
whom, and what would do it?"* Read it when someone proposes work: if a
deliverable has no path to a goal, the graph says so at a glance.

**2. Capability** — Wardley components by evolution stage, capability-map
capabilities, core/supporting/generic markings, `dkg:dependsOn` chains.
Answers *"what do we build, buy, and depend on?"* Movement arrows
(`dkg:movesTo`) are drawn dashed; they are predictions the graph can be
re-checked against later, which is most of the value of keeping a map at all.

**3. Language** — Concepts and Terms with every `skos:altLabel`, glossary
relationships with their verbatim cardinalities, and `dkg:renamedTo` edges.
Answers *"what do we call things, and where does the word change?"* This is the
lens that catches drift, and the one to open before naming anything new.

**4. Flow** — events in sequence, per bounded context, with commands, actors and
policies. Answers *"what happens, in what order, and who is involved?"* Nodes
carrying two `dkg:inContext` edges are highlighted: they straddle a boundary.

**5. Traceability** — the vertical one, and the reason the graph exists.
Idea → Deliverable → Impact → Capability → Command → Event → Term. Answers
*"whatever happened to that idea?"* and, run backwards, *"why are we building
this?"* Anything that cannot be walked from top or bottom shows up in the orphan
report instead.

## Reading the register first

`index.md` opens with the artifact register: what has been ingested, when, and
how many nodes each artifact contributed. Read it before the lenses. A graph
where one artifact contributed eighty per cent of the nodes is not a domain
map — it is one board with garnish, and the lenses will mislead accordingly.

## SPARQL recipes

Run with `python3 scripts/check_graph.py graph.ttl --sparql query.rq`, or paste
into any triple store.

**Everything that touches one concept, across all artifacts** — the single most
used query:

```sparql
PREFIX dkg: <https://w3id.org/dkg/ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
SELECT ?node ?label ?type ?artifact WHERE {
  ?target skos:prefLabel "Meal plan" .
  { ?node ?p ?target } UNION { ?target ?p ?node }
  ?node a ?type ; skos:prefLabel ?label ; dkg:source ?artifact .
}
```

**Ideas that never became anything** — the brainstorm's own report card:

```sparql
SELECT ?label WHERE {
  ?idea a dkg:Idea ; skos:prefLabel ?label .
  FILTER NOT EXISTS { ?x dkg:proposedSameAs ?idea }
  FILTER NOT EXISTS { ?idea dkg:proposedSameAs ?x }
  FILTER NOT EXISTS { ?idea dkg:realises|dkg:supports ?y }
}
```

**Deliverables with no path to a goal:**

```sparql
SELECT ?label WHERE {
  ?d a dkg:Deliverable ; skos:prefLabel ?label .
  FILTER NOT EXISTS { ?d (dkg:realises/dkg:towardsGoal)|dkg:supports ?g . ?g a dkg:Goal }
}
```

**Concepts corroborated by three or more artifacts** — the domain's spine:

```sparql
SELECT ?label (COUNT(DISTINCT ?a) AS ?n) WHERE {
  ?c a dkg:Concept ; skos:prefLabel ?label ; dkg:source ?a .
} GROUP BY ?label HAVING (COUNT(DISTINCT ?a) >= 3) ORDER BY DESC(?n)
```

**Straddlers — nodes in two contexts:**

```sparql
SELECT ?label (COUNT(DISTINCT ?ctx) AS ?n) WHERE {
  ?n0 skos:prefLabel ?label ; dkg:inContext ?ctx .
} GROUP BY ?label HAVING (COUNT(DISTINCT ?ctx) > 1)
```

**Every open disagreement, with both sides:**

```sparql
SELECT ?aLabel ?bLabel ?srcA ?srcB WHERE {
  ?a dkg:contradicts ?b .
  ?a dkg:source ?srcA ; rdfs:label ?aLabel .
  ?b dkg:source ?srcB ; rdfs:label ?bLabel .
}
```

**Actors named in strategy but absent from any flow** — the segment nobody is
building for:

```sparql
SELECT ?label WHERE {
  ?a a dkg:CustomerSegment ; skos:prefLabel ?label .
  FILTER NOT EXISTS { ?x dkg:performedBy ?a }
}
```

## What a dense node means

Nothing, on its own. Degree measures how often a word was written down, which
tracks how *talkable* a concept is, not how important it is. `Cook` will always
win on a cooking board. The interesting nodes are the ones with high degree and
**one** source (a whole model resting on a single artifact), and the ones with
several sources and **low** degree (everyone mentions it, nobody builds on it).
`views.py` lists both under "worth a second look".