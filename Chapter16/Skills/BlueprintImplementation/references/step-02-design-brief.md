# Step 2 — Design brief

Loaded by `blueprint-service-implementation` at the start of step 2. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

- The board must come from people. If the user has only Domain Stories or a
  description, offer to seed events (`domain-story-event-seeder`,
  `event-storming-seeder`) as preparation, but say plainly that a seeded list
  is not a design-level board and confidence will be lower.
- **One brief per board.** Run `event-storming-detail-interpreter` on every
  design-level board from step 1 (reuse its slice tables) and produce one
  Detail Design Brief per board. Then merge them into one context brief keyed
  by slice ID: an aggregate on several boards gets one state machine, and a
  conflict between boards (a transition one board allows and another
  forbids) is an open question.
- With a graph: extract aggregates, commands, events, policies, read models,
  external systems and hotspots per board in the context, then hand them to
  the interpreter as a sticky list.
- **Re-check the slice plan and the skeleton.** The full brief can show what
  the slice tables hid: a command on two aggregates, a policy reading another
  board's event, a guard that needs an external system. Show every effect on
  the slice plan, the board order, the story order and the skeleton, and
  offer to adjust. If both were *pending* from step 1, make the proposals now
  (step 1.4c–1.4d) from the brief.
- **Re-check the user stories against the brief.** Every user story must map
  to brief rows through its slices. A user story the merged brief cannot
  realize (no command, no read model that shows its outcome) is an open
  question before step 3, with the options of the step 1.4c finding table.
- Done when: every command has a target aggregate and a resulting event, or is
  listed as an open question; every slice of the plan maps to brief rows;
  every user story maps to brief rows through its slices or is an open
  question; and the slice plan and walking skeleton are accepted.

## Reading the graphs

<!-- shared:graph-basics -->
Both graphs use the `dkg:` vocabulary (`https://w3id.org/dkg/ns#`); the
decision graph adds `dkg:Principle`, `dkg:honors`, `dkg:overrides` and related
terms. Load both files into one rdflib graph (`pip install rdflib`;
`g.parse(dkg_file); g.parse(adg_file)`) and run SPARQL on the union. Match
labels case-insensitively; keep the graphs' own spellings in everything you
hand on. Never treat `dkg:Inferred` content as fact without saying so; pass
the confidence along.

Prefixes for every query:

```sparql
PREFIX dkg:  <https://w3id.org/dkg/ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
```
<!-- /shared:graph-basics -->

<!-- shared:query:context-slice -->
**Context slice** — replace `CTX` with the chosen label:

```sparql
SELECT ?cls ?label ?confidence ?locator ?source WHERE {
  ?ctx a dkg:BoundedContext ; skos:prefLabel ?cl .
  FILTER(LCASE(STR(?cl)) = LCASE("CTX"))
  ?n dkg:inContext ?ctx ; a ?cls ; skos:prefLabel ?label .
  OPTIONAL { ?n dkg:confidence ?confidence }
  OPTIONAL { ?n dkg:locator ?locator }
  OPTIONAL { ?n dkg:source ?source }
} ORDER BY ?cls ?label
```
<!-- /shared:query:context-slice -->

<!-- shared:query:command-event-chains -->
**Command → event chains**:

```sparql
SELECT ?cmd ?evt ?agg WHERE {
  ?c a dkg:Command ; skos:prefLabel ?cmd ; dkg:triggers ?e .
  ?e skos:prefLabel ?evt .
  OPTIONAL { ?e dkg:emittedBy ?a . ?a skos:prefLabel ?agg }
}
```
<!-- /shared:query:command-event-chains -->

<!-- shared:query:slices-in-the-graph -->
**Slices in the graph** — slices of the context with their
command, aggregate, events and source board; replace `CTX`. Older graphs
carry `dkg:Slice` only for Event Models; then the query returns nothing for
boards and step 1 asks or proposes:

```sparql
SELECT ?slice ?sliceLabel ?board ?cmd ?agg ?evt WHERE {
  ?ctx a dkg:BoundedContext ; skos:prefLabel ?cl .
  FILTER(LCASE(STR(?cl)) = LCASE("CTX"))
  ?s a dkg:Slice . OPTIONAL { ?s skos:prefLabel|rdfs:label ?sliceLabel }
  BIND(?s AS ?slice)
  ?s (dkg:inContext|dkg:mentions) ?ctx .
  OPTIONAL { ?s dkg:source ?b . ?b rdfs:label ?board }
  OPTIONAL { ?s dkg:mentions ?c . ?c a dkg:Command ; skos:prefLabel ?cmd }
  OPTIONAL { ?s dkg:mentions ?a . ?a a dkg:Aggregate ; skos:prefLabel ?agg }
  OPTIONAL { ?s dkg:mentions ?e . ?e a dkg:DomainEvent ; skos:prefLabel ?evt }
} ORDER BY ?board ?slice
```
<!-- /shared:query:slices-in-the-graph -->

<!-- shared:query:policies -->
**Policies**: `?p a dkg:Policy ; dkg:reactsTo ?e ; dkg:triggers ?c`.
<!-- /shared:query:policies -->

<!-- shared:query:contradictions-and-open-questions -->
**Contradictions and open questions**: `?x dkg:contradicts ?y`
and `?q a dkg:Question` in the context — show them before asking the user to
accept the step's input.
<!-- /shared:query:contradictions-and-open-questions -->

If a query returns nothing for the chosen context, say so and fall back to
asking for an upload; never conclude the domain has none.

## Revisiting this step

When this step is revisited ("go back to step N"), re-check — not
regenerate — the completed steps below; offer keep / patch / redo for each
inconsistent one. The full table is in SKILL.md.

| Revised step | Re-check |
|---|---|
| 2 Design brief | 3, 4, 6, 8, 11, 15; the slice plan, the user-story mapping and the skeleton (step 1.4c–1.4d) |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Slice plan (step 1): <from graph / uploaded / proposed / pending>, accepted <date>; board order: B2, B1, …; story order per board: B1: US-03, US-01, …; findings both directions <n> (decided: <how>)
Story ↔ slice map: US-01 → B1-S01, B1-S02; US-02 → B1-S03, B2-S01 (owned by B1); …   automations attached: B1-S04 → US-02
Walking skeleton (step 1): <user story + slice IDs>, <named / proposed / pending>, layers not reached: <list>
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
| 2 | — | Design brief | graph | graph.ttl (ctx Lending, B1, B2) | event-storming-detail-interpreter | y | y | 2026-10-23 | 2 |
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
