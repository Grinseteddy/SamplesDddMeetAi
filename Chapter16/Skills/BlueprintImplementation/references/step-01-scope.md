# Step 1 — Scope: graphs, context, boards and user stories

Loaded by `blueprint-service-implementation` at the start of step 1. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

Ask the four questions below in this order, one at a time. Question 4 has
four parts and sets the scope on both axes: boards and user stories.

1. **Domain knowledge graph.** "Do you have a domain knowledge graph (`.ttl`)
   for this domain?" Offer: **Upload** / **Build one now** (load
   `domain-knowledge-graph` and ingest the artifacts the user has) / **Not
   available**. On upload, run the *inventory* query and show a short table of
   what the graph can supply per step: counts of contexts, aggregates,
   commands, events, policies, read models, rules, open questions,
   contradictions, and whether API specs are present.

   **No graph → minimum inputs.** If the answer is *Not available*, ask for
   the two artifacts the run cannot start without, one at a time:
   - **At least one Domain Story** (picture, egon.io export, or numbered
     actor → activity → work object sentences). Ask "any more Domain
     Stories?"
     after each one; several are welcome.
   - **A Visual Glossary** (picture, transcription, or a Glossary Brief).

   For each, offer **Upload** / **Generate a draft** / **Stop here**:
   - *Upload* → interpret it: `domain-story-interpreter` for Domain Stories,
     `visual-glossary-interpreter` for the glossary.
   - *Generate a draft* → Domain Stories with `domain-story-seeder`; a
     glossary derived from the Domain Stories' work objects, with relationships and
     cardinalities asked as questions. Say plainly that a generated Domain Story or
     glossary is a strawman to correct with domain experts, mark it
     `Inferred` in the ledger, and let the user edit it before accepting.
   - *Stop here* → explain that without a graph, a Domain Story and a glossary,
     there is nothing to anchor the bounded context, the ubiquitous language
     or the domain model to; end the run and log what is missing.

   Once both are accepted, run `domain-story-glossary-consistency` on them
   and show the findings (terms the Domain Stories use but the glossary lacks,
   unused glossary terms, contradicted cardinalities). Offer to fix them now
   or log them as open questions. Then offer to **build a knowledge graph
   from them** with `domain-knowledge-graph` (recommended: later steps can
   then use **Graph** as a source and step 15 can ingest the result). If the
   user declines, carry the Domain Stories and the glossary forward as files.
2. **Architecture decision graph.** "Do you have an architecture decision
   graph — your ADRs and architectural principles as a `.ttl`?" First check
   whether the domain knowledge graph already holds `dkg:Decision` or
   `dkg:Principle` nodes; if it does, say so and ask whether that is the
   decision graph or a separate one exists. Offer: **Upload** / **Same file
   as the knowledge graph** / **Build one now** (load
   `adr-and-principles-ingester` with the user's ADRs and principles
   document) / **Not available**. On upload, run the *decision inventory*
   and show: decisions by status, principles (testable vs not), unacknowledged
   overrides, principles in tension, open questions no decision answers.
3. **Bounded context to implement.** Always ask, in these words or close to
   them: **"Which bounded context should be implemented in this run?"** Ask
   even when the graph holds only one context, and even when the user named
   one earlier — then ask to confirm it. Never pick a context on the user's
   behalf.
   - **With a graph:** run the *Context readiness* query and offer the
     contexts as options (option picker, up to four; more go in a short list
     with "type the name"). Show for each: number of aggregates, commands,
     events, rules, and in-scope decisions, plus open questions. Mark contexts
     with no aggregates or commands as "not ready for implementation" — they
     can still be chosen, but say steps 2–3 will need uploads.
   - **Without a graph:** work from the Domain Stories and the glossary. If
     they already carry labeled lanes or groups, or the glossary is
     colored by context, offer those as the options. Otherwise run
     `domain-story-context-finder` on the Domain Stories, show the proposed contexts
     with their evidence and contested sentences, and offer them as the
     options. The user may also upload a context map or a board with
     boundaries instead (then use `event-storming-context-mapper` or
     `pivotal-event-boundary-finder` if the boundaries are not settled).
     Then ask the question on that result.
   - **One context per run.** If the user names several, ask which one goes
     first and log the others in the ledger as later runs.
   - Repeat the chosen name back exactly as the graph or map spells it, and
     use that spelling in every later step.
4. **Design-level boards, user stories, slice plan and walking skeleton.**
   Scope is set on two axes — boards and user stories — and both are
   required. Ask the four parts below one at a time, after the context is
   chosen. Each is **asked first**; only when the user has none is a
   **proposal** made, shown and accepted like any other artifact (accept,
   revise, or replace).

   **1.4a. Boards.** "Which design-level EventStorming boards exist for
      <context>?" A context often has several, one per process. Offer:
      **From the graph** (query *Design-level board for the context*, one row
      per board) / **Upload** (photo, Miro/Mural export, typed sticky list;
      ask "any more boards?" after each) / **Not available**. For each board,
      run `event-storming-detail-interpreter` steps 1–3 only (legend and
      flavour, transcription, slice table); the full brief follows in step 2.
      A big-picture board does not qualify: say so and keep it as
      preparation. A process-level board (no aggregates) is accepted for
      slicing; its slices carry "aggregate to be confirmed in step 2". Give
      every board a short ID and name (`B1 — Lending`).
   **1.4b. User stories.** "Which user stories belong to <context>?" Offer:
      **From the graph** (query *User stories in the graph*) / **Upload**
      (backlog export, a list, story cards, or the issue tracker through a
      connector when one is available) / **Generate** (`user-story-author`
      on the knowledge graph for this context; without a graph, derive
      candidate user stories from the boards' actors and commands and from
      the Domain Stories) / **Not available**.
      - Keep each user story's wording exactly as given; give it an ID
        (`US-01` …) unless it has one, and keep an existing tracker key next
        to it.
      - A **generated** user story is a strawman: mark it `Inferred` in the
        ledger and let the user edit it before accepting. Never invent the
        "so that": take it from the graph (impact, goal, capability) or ask;
        otherwise write `so that ⟨open⟩` and log the open question.
      - Check every user story against **INVEST** and show the findings:
        too big for one increment (many commands, several boards, many
        rules expected) → offer a split along commands or rules; not
        testable (no observable outcome) → ask for the outcome; dependent →
        record the dependency for the story order.
      - Acceptance criteria are **not** written here. They come from the
        board's Example Mapping in step 3 (blue rules, green examples) — one
        source of truth. Criteria that come with an uploaded user story are
        kept as seed rules for that map.
      - *Not available* → say that the run needs user stories to define
        value and "done", and offer **Generate** once more. If the user
        still declines, generate them anyway as `Inferred` strawmen from
        the boards and log that scoping by user story rests on them.
   **1.4c. Slice plan — board × user story.** "Do you already have a slice
      plan for <context> — which slices belong to which board, which user
      stories each slice serves, and in which order boards and user stories
      should be built?" Offer: **From the graph** (query *Slices in the
      graph* and *User story ↔ slice links*) / **Upload** (a list, a sliced
      Event Model, a story map, a backlog with linked items) / **Propose
      one**.
      - *Propose one* → one slice per row of each board's slice table, with
        IDs `B1-S01, B1-S02, …` in board order, and per slice: trigger,
        command, aggregate or system, events, read models consulted and
        updated, the slices it depends on (a policy or read model that
        needs another slice's event), and **the user stories it serves**
        (matched by actor, command and outcome). A slice that appears on two
        boards (same command and aggregate) is merged, and the merge is
        shown. Never add a slice the boards do not show; a command without
        an aggregate becomes a slice with an open question.
      - **Check both directions** and show every finding for the user to
        decide:

        | Finding | Meaning | Options |
        |---|---|---|
        | user story realized by no slice | a board misses a command, or the user story is outside the context | add to a board (then step 2 confirms it) / move to another context's run / drop |
        | slice serving no user story | a technical step, a policy automation, or a missing user story | attach to the user story whose flow it completes / write a new user story / remove the slice |
        | user story spanning several boards | the flow crosses processes | split the user story per board / name the board that owns it (its slices on other boards are built in that board's increment) |
        | user story with very many slices | too big for one review | split it (INVEST) |

        A slice triggered by a policy (an automation) usually has no
        actor-facing user story; attach it to the user story whose flow it
        completes rather than inventing an "as a system …" story — unless
        the user prefers one.
      - **Board order:** dependencies first, then the core process or the
        board with the most user stories; the user may change it.
      - **Story order inside each board:** dependencies first, then the
        user story with the most value or risk as the user ranks them. The
        first user story of each board should be one whose slices need no
        later story's slices.
      - An uploaded or graph plan is checked against the boards' slice
        tables and the user-story list in the same way.
   **1.4d. Walking skeleton.** "Is a walking skeleton defined — the one user
      story and its thinnest slice that we build first, end to end through
      every layer?" Offer: **Name the user story and slice** / **Propose
      one**.
      - *Propose one* → rank the slices and show the best one with up to two
        alternatives, each with its user story. Prefer a slice that is
        triggered by an actor through an API operation; serves a user story
        of the first board in the board order; changes one aggregate with at
        least one real guard (not plain CRUD); emits an event the context
        publishes and feeds a read model behind a GET, when the context has
        those; depends on no other slice and no external system; and sits
        early on the timeline. For each candidate, say which layers it
        exercises (HTTP in, domain, database, outbox and broker out, broker
        in, read model) and which it leaves out. If no single slice reaches a
        layer that matters (typically consuming a message), offer one more
        slice for it — two at most.
      - A named slice is checked the same way: say which layers it leaves
        out, then accept it.
      - The skeleton's user story needs its examples in step 3 before
        increment 0 starts (see *Readiness gate*).
   - **No design-level board at all** → the slice plan and the skeleton
     cannot be grounded yet. Collect the user stories anyway (1.4b), log the
     plan and the skeleton as *pending*, say that the proposal will be made
     at the end of step 2 from the design brief (an Event Model's
     state-change slices also count), and continue. If the brief still has
     no commands and aggregates, the whole context becomes one increment,
     and the skeleton is the brief's first command for the first user
     story, marked inferred.

Then:
- Load both graphs into one in-memory graph for every later query; record
  each file in the ledger. If the two disagree on a label, keep both and log
  the difference — never merge them into the user's files.
- Record the neighbors and, for each border, direction, pattern and what
  crosses — these decide which AsyncAPI channels are published vs subscribed
  and where an anticorruption layer is needed (step 9).
- **Decisions and principles in scope:** list the decisions and principles
  that mention the chosen context or have no scope limit (query *Decisions in
  scope*). Show unacknowledged overrides and principles in tension as open
  questions now — they can block step 5.
- Without a knowledge graph, keep the accepted Domain Stories and glossary as
  the run's reference: step 2 can seed events from the Domain Stories
  (`domain-story-event-seeder`) as preparation for a board, step 6 takes the
  glossary for structure, and step 15 checks the code's names against the
  glossary. Without a decision graph, step 5 asks for an ADR log or builds
  the decisions from scratch.
- Done when: both graph questions are answered (file or "not available");
  if there is no knowledge graph, at least one Domain Story and a Visual
  Glossary are accepted and checked against each other; the user has
  explicitly chosen (or confirmed) the one bounded context to implement; its
  borders are listed; the in-scope decisions and principles are listed; the
  design-level boards are listed (or "not available"); the user stories are
  accepted (uploaded, from the graph, or generated and edited); and the slice
  plan — every slice with one board and at least one user story, every user
  story with at least one slice, the board order and the story order — and
  the walking skeleton (user story + slice) are accepted, or logged as
  *pending* until step 2.

<!-- shared:principles-guardrail -->
## Principles as guardrails

From here on, every testable principle in scope
is a constraint on steps 6–12. After each of those steps, check its output
against them and report each one as honored, overridden (with the ADR that
allows it) or violated. A violation is fixed or logged, never ignored.
<!-- /shared:principles-guardrail -->

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

<!-- shared:query:inventory -->
**Inventory** — counts per class, overall and per context:

```sparql
SELECT ?ctxLabel ?cls (COUNT(DISTINCT ?n) AS ?count) WHERE {
  ?n a ?cls .
  FILTER(?cls IN (dkg:Aggregate, dkg:AggregateRoot, dkg:Command, dkg:DomainEvent,
                  dkg:PivotalEvent, dkg:Policy, dkg:Rule, dkg:ReadModel,
                  dkg:ExternalSystem, dkg:Hotspot, dkg:Question, dkg:Decision,
                  dkg:Entity, dkg:ValueObject, dkg:Term, dkg:Slice))
  OPTIONAL { ?n dkg:inContext ?ctx . ?ctx skos:prefLabel ?ctxLabel }
} GROUP BY ?ctxLabel ?cls ORDER BY ?ctxLabel ?cls
```
<!-- /shared:query:inventory -->

<!-- shared:query:api-specs-in-the-graph -->
**API specs in the graph** — newer graph versions carry
OpenAPI/AsyncAPI content, older ones do not; the class names vary, so match
on the name:

```sparql
SELECT DISTINCT ?cls (COUNT(DISTINCT ?n) AS ?count) WHERE {
  ?n a ?cls .
  FILTER(REGEX(STR(?cls), "(OpenAPI|AsyncAPI|ApiSpec|Operation|Channel)", "i"))
  FILTER(?cls != dkg:Channel)
} GROUP BY ?cls
```

(`dkg:Channel` is excluded: it is the Business Model Canvas block, not an API
channel.) Then check that a spec node is linked to the chosen context
(`dkg:inContext`, or `dkg:mentions` the context) before calling it provided.
If nothing matches, step 4 asks for the spec files.
<!-- /shared:query:api-specs-in-the-graph -->

<!-- shared:query:contexts -->
**Contexts**:

```sparql
SELECT ?ctx ?label WHERE { ?ctx a dkg:BoundedContext ; skos:prefLabel ?label }
```
<!-- /shared:query:contexts -->

<!-- shared:query:context-readiness -->
**Context readiness** — one row per context, for the question
"which bounded context should be implemented?":

```sparql
SELECT ?label (COUNT(DISTINCT ?agg) AS ?aggregates) (COUNT(DISTINCT ?cmd) AS ?commands)
       (COUNT(DISTINCT ?evt) AS ?events) (COUNT(DISTINCT ?rule) AS ?rules)
       (COUNT(DISTINCT ?dec) AS ?decisions) (COUNT(DISTINCT ?q) AS ?questions)
WHERE {
  ?ctx a dkg:BoundedContext ; skos:prefLabel ?label .
  OPTIONAL { ?agg a dkg:Aggregate ; dkg:inContext ?ctx }
  OPTIONAL { ?cmd a dkg:Command ; dkg:inContext ?ctx }
  OPTIONAL { ?evt a dkg:DomainEvent ; dkg:inContext ?ctx }
  OPTIONAL { ?rule a dkg:Rule ; dkg:inContext ?ctx }
  OPTIONAL { ?dec a dkg:Decision ; dkg:mentions ?m .
             FILTER(?m = ?ctx || EXISTS { ?m dkg:inContext ?ctx }) }
  OPTIONAL { ?q a dkg:Question ; dkg:inContext ?ctx }
} GROUP BY ?label ORDER BY DESC(?commands) ?label
```
<!-- /shared:query:context-readiness -->

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

<!-- shared:query:user-story-slice-links -->
**User story ↔ slice links** — which commands, read models or
slices each user story mentions; used to propose the story column of the
slice plan:

```sparql
SELECT ?story ?storyLabel ?kind ?elemLabel WHERE {
  ?story a ?cls . FILTER(REGEX(STR(?cls), "(UserStory|BacklogItem|Story$)", "i"))
  FILTER(!REGEX(STR(?cls), "DomainStory|Sentence|Activity", "i"))
  OPTIONAL { ?story skos:prefLabel|rdfs:label ?storyLabel }
  ?story (dkg:mentions|dkg:realizedBy|dkg:covers) ?e .
  ?e a ?kind ; skos:prefLabel ?elemLabel .
  FILTER(?kind IN (dkg:Command, dkg:ReadModel, dkg:Slice, dkg:DomainEvent, dkg:Policy))
} ORDER BY ?storyLabel ?kind
```

A user story with no link is matched to slices by actor, command and
outcome in step 1 (4c) and the match is shown for the user to confirm.
<!-- /shared:query:user-story-slice-links -->

<!-- shared:query:decision-inventory -->
**Decision inventory**:

```sparql
SELECT ?kind ?status (COUNT(DISTINCT ?n) AS ?count) WHERE {
  { ?n a dkg:Decision . BIND("decision" AS ?kind) OPTIONAL { ?n dkg:status ?status } }
  UNION
  { ?n a dkg:Principle . BIND("principle" AS ?kind)
    OPTIONAL { ?n dkg:testable ?t } BIND(IF(BOUND(?t) && ?t, "testable", "not testable") AS ?status) }
} GROUP BY ?kind ?status ORDER BY ?kind ?status
```
<!-- /shared:query:decision-inventory -->

<!-- shared:query:unacknowledged-overrides-and-principles-in-tension -->
**Unacknowledged overrides and principles in tension**:

```sparql
SELECT ?issue ?a ?b WHERE {
  { ?d dkg:overrides ?p .
    FILTER NOT EXISTS { ?x a dkg:Assertion ; rdf:subject ?d ; rdf:predicate dkg:overrides ;
                          rdf:object ?p ; dkg:acknowledged true }
    ?d skos:prefLabel ?a . ?p skos:prefLabel ?b . BIND("override not acknowledged" AS ?issue) }
  UNION
  { ?p1 dkg:inTensionWith ?p2 . ?p1 skos:prefLabel ?a . ?p2 skos:prefLabel ?b .
    BIND("principles in tension" AS ?issue) }
}
```

The ingester reifies each override as a `dkg:Assertion` carrying
`dkg:acknowledged true|false`; an override with no assertion counts as
unacknowledged.
<!-- /shared:query:unacknowledged-overrides-and-principles-in-tension -->

<!-- shared:query:decisions-in-scope -->
**Decisions in scope** — replace `CTX`:

```sparql
SELECT ?id ?label ?status ?opt ?successor WHERE {
  ?d a dkg:Decision ; skos:prefLabel ?label .
  OPTIONAL { ?d dkg:decisionId ?id }
  OPTIONAL { ?d dkg:status ?status }
  OPTIONAL { ?d dkg:decidedOption ?opt }
  OPTIONAL { ?s dkg:supersedes ?d . ?s skos:prefLabel ?successor }
  OPTIONAL { ?d dkg:mentions ?m . ?m skos:prefLabel ?ml }
  FILTER(!BOUND(?ml) || LCASE(STR(?ml)) = LCASE("CTX")
         || EXISTS { ?d dkg:mentions/dkg:inContext/skos:prefLabel ?cl .
                     FILTER(LCASE(STR(?cl)) = LCASE("CTX")) })
} ORDER BY ?id
```
<!-- /shared:query:decisions-in-scope -->

<!-- shared:query:principles-in-scope -->
**Principles in scope**:

```sparql
SELECT ?id ?label ?statement ?scope ?testable WHERE {
  ?p a dkg:Principle ; skos:prefLabel ?label .
  OPTIONAL { ?p dkg:principleId ?id }
  OPTIONAL { ?p dkg:statement ?statement }
  OPTIONAL { ?p dkg:scope ?scope }
  OPTIONAL { ?p dkg:testable ?testable }
} ORDER BY ?id
```

Read each principle's `dkg:scope` phrase and decide with the user whether it
applies to this context; a scoped principle is never silently treated as
universal, and an unscoped one applies everywhere.

If a query returns nothing for the chosen context, say so and fall back to
asking for an upload; never conclude the domain has none.
<!-- /shared:query:principles-in-scope -->

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
| 1 Scope | everything |
| 1 Domain knowledge graph replaced or grown | 2, 3, 4, then 15 |
| 1 Architecture decision graph replaced or grown | 5, then the guardrail check of 6–12 |
| 1 Domain Story or Visual Glossary changed (no graph) | the consistency check and context cut in 1, then 2, 6, 15 |
| 1 Board added or changed | its slice table and the slice plan in 1, then 2, 3 (incl. that board's Example Mapping), 4; a new board gets its own Example Mapping and its own increment; a changed board re-checks the increments already built from it |
| 1 User story added, split, merged or changed | the slice plan in 1 (both directions), then the board's Example Mapping in 3c (its yellow cards), 3d, the reconciliation table in 4; an increment already built that holds the user story re-runs step 12 for its feature file, then 15 |
| 1 Slice plan, board order or story order changed | the planned list and the order of the open increments and user stories; increments already built are re-checked only for slices or user stories that moved |
| 1 Walking skeleton changed | before increment 0: nothing else; after it: increment 0 is re-run for the new slice, and the old skeleton slice moves to its board's increment |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Context: <name, as the user confirmed it>   Later runs: <other contexts named>   Output: <path>
Domain knowledge graph: <file or none>   Architecture decision graph: <file, "same file" or none>
Domain Stories: <files or "from graph">   Visual Glossary: <file or "from graph">   Generated drafts: <which, if any>
Design-level boards (step 1): B1 — <name>: <"from graph" / file>, <n> slices; B2 — … ; or "not available"
User stories (step 1): <from graph / uploaded (source, tracker) / generated (Inferred) / edited>, US-01 … US-nn, accepted <date>; INVEST findings <n> (split <ids>); open "so that" <ids or none>
Slice plan (step 1): <from graph / uploaded / proposed / pending>, accepted <date>; board order: B2, B1, …; story order per board: B1: US-03, US-01, …; findings both directions <n> (decided: <how>)
Story ↔ slice map: US-01 → B1-S01, B1-S02; US-02 → B1-S03, B2-S01 (owned by B1); …   automations attached: B1-S04 → US-02
Walking skeleton (step 1): <user story + slice IDs>, <named / proposed / pending>, layers not reached: <list>
Principles in scope: <IDs>   Open governance issues: <count>
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
