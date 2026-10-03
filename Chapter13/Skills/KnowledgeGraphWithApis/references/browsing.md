# Browsing the graph

The graph is for reading. `scripts/views.py` renders six lenses plus a register
and an orphan report into `views/index.md`; this file says what each lens is
*for*, and gives the queries for the questions people actually ask out loud.

Run all of it with `python3 scripts/views.py graph.ttl --out views/`, or one
lens with `--lens language`.

## The six lenses

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

**5. Interface** — per bounded context, its API specifications: each `dkg:Api`
as a box with its style (`sync` / `async`) and protocol, synchronous operations
as `METHOD /path`, channels as hexagons with their messages, `send` / `receive`
arrows, and dotted `specifies` edges out to the commands, events and canvas
messages each contract stands for; a coverage table underneath, and the API
Product Canvases' quality requirements with what each constrains. An Api drawn
on a canvas and specified in a file is one box with two sources. Answers *"what
can be called, what is published, what is consumed from across a border — and
does the contract match the model?"* Empty until a spec is ingested, and says
so.

**6. Traceability** — the vertical one, and the reason the graph exists.
Idea → Deliverable → Impact → Capability → Command → Event → Term, and
Decision → Question, and now Command → Operation / Event → ApiMessage via
`specifies`. Answers *"whatever happened to that idea?"*, *"whatever
happened to that question?"* and, run backwards, *"why are we building this?"*
— all the way down to an endpoint. Anything that cannot be walked from top or
bottom shows up in the orphan report instead.

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

**Open questions no decision has closed** — the agenda for the next ADR row:

```sparql
SELECT ?label ?src WHERE {
  ?q a dkg:Question ; skos:prefLabel ?label ; dkg:source ?src .
  FILTER NOT EXISTS { ?d dkg:answers ?q }
  FILTER NOT EXISTS { ?q dkg:proposedSameAs ?q2 . ?d2 dkg:answers ?q2 }
}
```

**Decisions the team reversed** — both options, both dates:

```sparql
SELECT ?later ?laterOpt ?earlier ?earlierOpt WHERE {
  ?l dkg:supersedes ?e ;
     skos:prefLabel ?later ; dkg:decidedOption ?laterOpt .
  ?e skos:prefLabel ?earlier ; dkg:decidedOption ?earlierOpt .
}
```

**A context's border, as its canvas drew it** — inbound and outbound in one
table:

```sparql
SELECT ?dir ?kind ?msg ?other WHERE {
  ?m a dkg:Message ; skos:prefLabel ?msg ; dkg:messageKind ?kind ;
     dkg:from ?f ; dkg:to ?t .
  { ?t skos:prefLabel "Cooking Assistance" . ?f skos:prefLabel ?other . BIND("in" AS ?dir) }
  UNION
  { ?f skos:prefLabel "Cooking Assistance" . ?t skos:prefLabel ?other . BIND("out" AS ?dir) }
}
```

**A context's interface, sync and async in one table** — the question that
made the API classes worth adding:

```sparql
SELECT ?style ?what ?detail ?specifies WHERE {
  ?api a dkg:Api ; dkg:inContext ?ctx ; dkg:interactionStyle ?style .
  ?ctx skos:prefLabel "Rack management" .
  ?x dkg:exposedBy ?api ; skos:prefLabel ?what .
  OPTIONAL { ?x dkg:httpMethod ?m ; dkg:path ?p . BIND(CONCAT(?m, " ", ?p) AS ?detail) }
  OPTIONAL { ?x dkg:action ?detail }
  OPTIONAL { ?x dkg:specifies ?t . ?t skos:prefLabel ?specifies }
  FILTER NOT EXISTS { ?x a dkg:Schema }
} ORDER BY ?style ?what
```

**Commands with no endpoint, events with no message** — per context, what the
specs leave uncovered:

```sparql
SELECT ?ctxLabel ?kind ?label WHERE {
  { ?n a dkg:Command ; dkg:inContext ?ctx . BIND("command" AS ?kind) }
  UNION
  { ?n a dkg:DomainEvent ; dkg:inContext ?ctx . BIND("event" AS ?kind) }
  ?n skos:prefLabel ?label . ?ctx skos:prefLabel ?ctxLabel .
  ?api a dkg:Api ; dkg:inContext ?ctx .
  FILTER NOT EXISTS { ?c dkg:specifies ?n }
}
```

**Endpoints the workshops never asked for** — operations and schemas that
specify nothing:

```sparql
SELECT ?apiLabel ?label WHERE {
  ?x a ?cls ; skos:prefLabel ?label ; dkg:exposedBy ?api .
  ?api skos:prefLabel ?apiLabel .
  FILTER (?cls IN (dkg:Operation, dkg:ApiMessage, dkg:Schema))
  FILTER NOT EXISTS { ?x dkg:specifies ?t }
  FILTER NOT EXISTS { ?x dkg:mentions ?t2 }
  FILTER NOT EXISTS { ?x dkg:carriesMessage ?m . ?m dkg:specifies ?t3 }
  FILTER NOT EXISTS { ?x dkg:schemaRole ?r . FILTER (?r IN ("header", "error", "payload")) }
}
```

**Borders the specs draw** — every `receive`, with the producing context:

```sparql
SELECT ?consumer ?message ?producer WHERE {
  ?op a dkg:Operation ; dkg:action "receive" ; dkg:carriesMessage ?m ; dkg:exposedBy ?api .
  ?api dkg:inContext ?c . ?c skos:prefLabel ?consumer .
  ?m skos:prefLabel ?message ; dkg:specifies ?ev .
  ?ev dkg:inContext ?p . ?p skos:prefLabel ?producer .
  FILTER (?p != ?c)
}
```

**Style mismatches** — a synchronous call carrying what the model calls an
event, or a query on a channel:

```sparql
SELECT ?contract ?style ?model ?kind WHERE {
  ?x dkg:specifies ?t ; skos:prefLabel ?contract ; dkg:exposedBy ?api .
  ?api dkg:interactionStyle ?style . ?t skos:prefLabel ?model .
  OPTIONAL { ?t dkg:messageKind ?kind }
  FILTER ( (?style = "sync"  && (?kind = "evt" || EXISTS { ?t a dkg:DomainEvent }))
        || (?style = "async" && ?kind = "qry") )
}
```

**What one artifact should change** — the reverse direction, also available as
`views.py graph.ttl --patches "<artifact>"`, which adds spellings,
relationships drawn elsewhere and unclosed questions:

```sparql
SELECT ?decision ?status ?term WHERE {
  ?term dkg:source ?art . ?art rdfs:label "Visual Glossary Enhanced 08" .
  { ?d dkg:answers ?q . ?q dkg:mentions ?term }
  UNION { ?d dkg:mentions ?term }
  ?d a dkg:Decision ; skos:prefLabel ?decision ; dkg:status ?status .
}
```

## What a dense node means

Nothing, on its own. Degree measures how often a word was written down, which
tracks how *talkable* a concept is, not how important it is. `Cook` will always
win on a cooking board. The interesting nodes are the ones with high degree and
**one** source (a whole model resting on a single artifact), and the ones with
several sources and **low** degree (everyone mentions it, nobody builds on it).
`views.py` lists both under "worth a second look".
