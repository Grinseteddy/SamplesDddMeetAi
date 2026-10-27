# Step 3 — Invariants and acceptance scenarios

Loaded by `blueprint-service-implementation` at the start of step 3. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

Most invariants already exist before this step asks for anything. Collect
them first, then ask for the design-level EventStorming of the context, then
for the Example Mapping **of every board**, then merge. The questions are
independent: an available Example Mapping never replaces the EventStorming
question, and the other way round.

All boards are mapped here, before step 4, because the specs are approved
once for the whole context. Each board's map is confirmed again at its
increment's readiness gate. If the user prefers just-in-time mapping (one
board's map right before its increment), offer it as an explicit choice,
warn that a new rule then may reopen the approved specs (back to step 4),
and log the choice; at least the skeleton user story's map is still needed
now.

**3a. Collect what is already there** (no question to the user):
- **Graph:** `dkg:Rule` and `dkg:Policy` nodes in the context, and the
  glossary's relationships with their cardinalities (query *Glossary
  cardinalities*).
- **Visual Glossary** (from step 1 or the graph): every multiplicity is a
  candidate invariant — `1..5` on "Member borrows Copy" means "a member holds
  at most five copies". Take them from the Glossary Brief of
  `visual-glossary-interpreter`. Keep drawn and derived cardinalities apart.
Show the collected list with the **source of each rule** (graph / glossary).

**3b. Ask for the design-level EventStorming of this context — always.**
Ask, naming the context: **"Is there a design-level EventStorming of
<context>?"** Ask even when an Example Mapping is available and even when 3a
found many rules: only a design-level board shows which command each rule
guards, on which aggregate, and in which state — the Example Mapping does
not.
- **Design level** means the board carries commands (blue), aggregates or
  systems (large yellow), policies (lilac) and read models (green) around the
  events, for this bounded context. A big-picture board with events and
  hotspots only does not qualify; say so if that is what arrives, and offer
  to use it as preparation only.
- Offer, in this order, only the options that apply:
  - **Reuse the boards from steps 1–2** — when step 2 was built from
    design-level boards of exactly this context. Ask to confirm they are
    still current and complete; a board added now also gets slices and user
    stories (step 1.4c), its own Example Mapping (3c) and its own
    increment.
  - **From the graph** — when the query *Design-level board for the context*
    finds commands and aggregates for this context sourced from an
    EventStorming board.
  - **Upload** — photo, Miro/Mural export, or a typed sticky list.
  - **Not available.**
- Run `event-storming-invariant-finder` on each board (or
  `event-model-invariant-finder` if the user offers an Event Model of the
  context instead, and say that it stands in for the board) for guards,
  status models, ordering, cardinality, authorization and lifecycle rules.
- *Not available* → continue, but mark every rule from 3a and 3c that has no
  command or aggregate as **"not anchored — no design-level board"**; step 6
  will ask for its command and aggregate before writing code. Log the missing
  board as an open question.
- Add the board's rules to the list from 3a, with source *board*, and mark
  duplicates and contradictions between sources (for example the glossary
  says `0..5`, a board hotspot says "max three"). Contradictions are open
  questions; never pick one silently.

**3c. Example Mapping — required for every board.** Each design-level
board needs its own Example Mapping, because it is the only source of
**concrete examples**, of the questions nobody could answer, and of the
user stories' acceptance criteria. Ask after 3b, board by board in the board
order: **"Is there an Example Mapping for board <B1 — name>?"** The map's
yellow cards are the board's user stories from the slice plan (step 1.4c);
show them before asking, so the user knows what the map must cover. One
session per user story is fine (Example Mapping is defined per story) — the
board's Example Mapping is then the set of those per-story maps. With no
design-level board at all (step 1), the whole context counts as one board
(`B0 — <context>`) and needs one Example Mapping over all its user stories.

Offer, only the options that apply:
- **From the graph** — when the query *Example Mapping in the graph* finds
  one linked to this board or its user stories. Show it before using it.
- **Upload** (photo, Miro/Mural screenshot, typed list, or the Markdown file
  of an earlier EXACT session) → read it with `example-mapping-interpreter`:
  user stories, rules, examples, questions, readiness verdict.
- **Run an Example Mapping session now** → load EXACT Coding's
  `example-mapping` and follow it as written: a 1:1 interview in which the
  user is the domain expert, one yellow card at a time. Seed each card with
  its user story, the acceptance criteria that came with it, and the rules
  from 3a and 3b anchored to its slices, but **ask** for every rule and
  example — EXACT's facilitator rules forbid inventing them. Questions the
  user cannot answer become red cards; behavior outside the context becomes
  a New Story card (log it for a later run). Save the result where EXACT puts
  it (`src/<feature>-example-mapping.md`, or `docs/<context>/example-mapping/
  <board>.md` when step 0 set up Docs-as-Code) and show its health
  indicators.
- **Defer this board** — there is no "not available": a board without an
  Example Mapping cannot be built. It stays in the slice plan marked **not
  ready**; its increment cannot pass the readiness gate, and its operations,
  channels, rules and user stories stay on the planned list. Ask for the map
  again at that board's readiness gate. The walking skeleton's board cannot
  be deferred: at least the skeleton user story's map is needed before
  increment 0.

**Check each board's map against the slice plan:**
- a user story of the board with no yellow card → map it now or take it out
  of the board (back to step 1.4c);
- a yellow card that is no user story of the board → a new user story (add
  it to the plan, with slices) or a card of another board;
- a New Story card or a split the session proposes (many blue cards) → back
  to step 1.4c, for the user to accept; the slice plan is updated before
  step 4.

**Readiness verdict per board.** A board's map is **ready** when every
user story of the board has a yellow card, every yellow card has at least
one rule, every rule at least one example, and every red card is answered
or explicitly **deferred** to a named increment (it must be answered at that
increment's readiness gate). Otherwise the board is **not ready**: show what
is missing and offer to continue the session now or defer the board.

**3d. Merge into one invariants sheet.**
- Match each blue rule card to a collected rule (same meaning, not just same
  words); new rules from the map are added with source *example mapping*.
- Each green example becomes a Given/When/Then scenario under its rule, with
  its user story (`US-07 ex.2`).
- Each red question becomes an open question attached to its rule or user
  story, with the increment it is deferred to.
- Rules from 3a and 3b that no board's map confirms are shown per board:
  for each, the user **adds an example now**, **drops the rule**, or
  **keeps it for a later board** whose map is still to come. No rule of a
  ready board is carried without an example.
- Examples whose rule is missing are listed and settled in the session.
- Every rule gets a stable ID (`INV-01` …), its sources, its user stories,
  one aggregate, one command and the slice and board they belong to (all
  taken from the board), the Given/When/Then scenarios, the rejection it
  causes, and its **kind**: *quantitative*, *cardinality* or *other*.
  Quantitative and cardinality rules get a property test in step 6. The
  sheet is grouped **board → user story → rule → example**, so each
  increment takes its rules from its own group. A rule the board does not
  anchor keeps the "not anchored" mark.
- Rules spanning two aggregates or contexts become policies with a staleness
  window and a compensating action — they are implemented in steps 8–9, never
  inside one aggregate.
- Done when: the design-level EventStorming question was asked and answered;
  every board has an Example Mapping with a readiness verdict, or is
  explicitly deferred as **not ready** (never the skeleton's board); every
  user story of a ready board is covered by its map; every command from the
  board has its guards listed (possibly none); every rule of a ready board
  has at least one example; and every rule is anchored to a command and an
  aggregate or is marked "not anchored".

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

<!-- shared:query:glossary-cardinalities -->
**Glossary cardinalities** — candidate invariants; replace `CTX`:

```sparql
SELECT ?from ?verb ?to ?card ?given WHERE {
  ?r a dkg:Relationship ; dkg:from ?f ; dkg:to ?t ; dkg:cardinality ?card .
  ?f skos:prefLabel ?from . ?t skos:prefLabel ?to .
  OPTIONAL { ?r dkg:verb ?verb }
  OPTIONAL { ?r dkg:cardinalityGiven ?given }
  FILTER EXISTS { { ?f dkg:inContext ?ctx } UNION { ?r dkg:inContext ?ctx }
                  ?ctx skos:prefLabel ?cl . FILTER(LCASE(STR(?cl)) = LCASE("CTX")) }
} ORDER BY ?from ?to
```
<!-- /shared:query:glossary-cardinalities -->

<!-- shared:query:design-level-board-for-the-context -->
**Design-level board for the context** — commands, aggregates,
policies and read models of the context, per EventStorming board; replace
`CTX`. A board with commands **and** aggregates counts as design level:

```sparql
SELECT ?board ?boardLabel
       (COUNT(DISTINCT ?cmd) AS ?commands) (COUNT(DISTINCT ?agg) AS ?aggregates)
       (COUNT(DISTINCT ?pol) AS ?policies) (COUNT(DISTINCT ?rm) AS ?readModels)
WHERE {
  ?board a dkg:EventStormingBoard . OPTIONAL { ?board rdfs:label ?boardLabel }
  ?ctx a dkg:BoundedContext ; skos:prefLabel ?cl .
  FILTER(LCASE(STR(?cl)) = LCASE("CTX"))
  OPTIONAL { ?cmd a dkg:Command ; dkg:inContext ?ctx ; dkg:source ?board }
  OPTIONAL { ?agg a dkg:Aggregate ; dkg:inContext ?ctx ; dkg:source ?board }
  OPTIONAL { ?pol a dkg:Policy ; dkg:inContext ?ctx ; dkg:source ?board }
  OPTIONAL { ?rm a dkg:ReadModel ; dkg:inContext ?ctx ; dkg:source ?board }
} GROUP BY ?board ?boardLabel
```
<!-- /shared:query:design-level-board-for-the-context -->

<!-- shared:query:example-mapping-in-the-graph -->
**Example Mapping in the graph** — the vocabulary has no dedicated
class yet, so look for an artifact or node whose class or label names it:

```sparql
SELECT DISTINCT ?n ?cls ?label WHERE {
  ?n a ?cls . OPTIONAL { ?n rdfs:label|skos:prefLabel ?label }
  FILTER(CONTAINS(LCASE(STR(?cls)), "examplemapping")
         || CONTAINS(LCASE(STR(?label)), "example mapping"))
}
```

If it returns nothing, the graph holds no Example Mapping: ask for one. When
it finds one, check it is linked to the board or to the board's user stories
(`dkg:mentions`, `dkg:source`) before using it for that board.
<!-- /shared:query:example-mapping-in-the-graph -->

<!-- shared:query:user-stories-in-the-graph -->
**User stories in the graph** — the vocabulary has no fixed class
for backlog stories yet (`user-story-author` reads the graph but does not
write to it), so match on the class name and leave out Domain Storytelling
classes; replace `CTX`:

```sparql
SELECT DISTINCT ?story ?cls ?label ?text ?actor ?ctxLabel WHERE {
  ?story a ?cls .
  FILTER(REGEX(STR(?cls), "(UserStory|BacklogItem|Story$)", "i"))
  FILTER(!REGEX(STR(?cls), "DomainStory|Sentence|Activity", "i"))
  OPTIONAL { ?story skos:prefLabel|rdfs:label ?label }
  OPTIONAL { ?story dkg:statement|dkg:text|rdfs:comment ?text }
  OPTIONAL { ?story dkg:actor|dkg:mentions ?a . ?a a dkg:Actor ; skos:prefLabel ?actor }
  OPTIONAL { ?story (dkg:inContext|dkg:mentions) ?ctx .
             ?ctx a dkg:BoundedContext ; skos:prefLabel ?ctxLabel }
  FILTER(!BOUND(?ctxLabel) || LCASE(STR(?ctxLabel)) = LCASE("CTX"))
} ORDER BY ?label
```

User stories with no context link are shown separately and the user decides
whether they belong to this context. If the query returns nothing, step 1
asks for an upload or offers **Generate**.
<!-- /shared:query:user-stories-in-the-graph -->

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
| 3 Invariants (incl. a new or changed design-level board) | 6, 8, 12, 15; a changed board also re-checks 2 |
| 3 A board's Example Mapping changed (rule, example or red-card answer — also at a readiness gate) | the board's readiness verdict, 3d, then 4 if an outcome needs a contract change, then 6, 8, 12 for that board's increment (already built: new failing tests there first), then 15; a New Story card or split goes to step 1.4c first |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Design-level EventStorming (step 3): <reuse steps 1–2, "from graph", file or "not available">
Example Mapping per board (step 3): B1 — <uploaded file / EXACT session file / "from graph">, read with <example-mapping-interpreter / EXACT session>, user stories US-01…US-05, rules <n>, examples <n>, red cards <n> (answered <n>, deferred → increment <k>: <ids>), verdict <ready / not ready / deferred board>, date; B2 — …   Mapping mode: <all boards in step 3 / just-in-time (chosen <date>)>
Planned list: <spec element, rule or user story → increment>, …  (empty after the last increment)
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
| 3 | — | Invariants and acceptance scenarios | upload + EXACT session | example-mapping/B1.md, B2.md (B2 not ready: 2 red cards → inc. 2) | example-mapping-interpreter; EXACT example-mapping | y | y | 2026-10-23 | 2 |
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
