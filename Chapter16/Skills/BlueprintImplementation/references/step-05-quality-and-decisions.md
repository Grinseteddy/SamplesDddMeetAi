# Step 5 — Quality requirements and architecture decisions

Loaded by `blueprint-service-implementation` at the start of step 5. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

Quality requirements come first, because they decide between the options of
every later question. Step 5 has two parts: **quality scenarios** (5a–5b),
then the **required decisions** (5c–5f), each of which cites the scenarios
it serves.

**5a. Ask for an Architecture Knowledge Graph.** Ask: **"Do you have an
Architecture Knowledge Graph for this system — your quality goals, quality
scenarios and constraints as a `.ttl`?"** First run the query *Quality
requirements in the graph* on the graphs from step 1; if the decision graph
(or the domain knowledge graph) already holds quality nodes, say what it
holds and ask whether that is the Architecture Knowledge Graph or a separate
one exists. Offer: **Upload** / **Same file as the decision graph** / **Not
available**.
- On upload, load it into the run's union graph (never merged into the
  user's files), record it in the ledger, and run the *Quality inventory*:
  quality goals by priority; scenarios per quality characteristic (ISO/IEC
  25010); scenarios scoped to this context and system-wide ones; constraints;
  open quality questions.
- Check every scenario in scope: is it complete in the **six-part form**
  (source, stimulus, environment, artifact, response, response measure)? Is
  the response measure measurable (a number, a percentile, a time)? Does it
  have a priority? Show the findings. An incomplete or unmeasurable scenario,
  and every open quality question, goes into 5b as a question. Never complete
  a scenario by assumption.

**5b. Critical quality questions.** Ask them when there is **no
Architecture Knowledge Graph**, and, with a graph, for every critical area
it leaves open or unanswered — no scenario for this context, no measurable
response measure, or an open question. An area the graph answers is shown,
not asked again. Ask one question at a time, naming it ("Quality question 3
of 8 — Availability and recovery"); offer typical answers as options,
**Other**, and **Skip for now**.

The critical areas, in this order:

Loaded at step 5 by `blueprint-service-implementation`.

1. **Quality goals** — the three most important quality characteristics for
   this context, ranked (ISO/IEC 25010). They order every later question and
   break ties between ADR options.
2. **Load and latency** — expected and peak load (requests per second per
   operation group, messages per second per channel) and the latency budget
   for commands and for queries, as a percentile (p95, p99).
3. **Availability and recovery** — the availability target; what the
   service does while the database or the broker is down (refuse, queue,
   degrade); recovery time and tolerated data loss (RTO, RPO); whether a
   published event may ever be lost.
4. **Consistency and staleness** — the maximum lag of read models and of
   events to other contexts (it feeds the query-side decision below).
5. **Scalability and contention** — number of instances, growth of data
   volume, aggregates that many actors change at the same time.
6. **Data protection** — the personal data the context holds; retention and
   deletion, including erasure in events, read models and backups (GDPR);
   audit trail.
7. **Operability** — logs, metrics, traces, health and readiness checks,
   correlation IDs across HTTP and messages, who is alerted.
8. **Evolvability and compatibility** — how consumers of the APIs and events
   are protected from change (versioning, additive change, deprecation
   period), and maintainability expectations (module boundaries, build time).

Each answer becomes a **quality scenario** `QS-01…`, written in the six-part
form, with its quality characteristic, a priority (importance H/M/L ×
difficulty H/M/L, as in an ATAM utility tree), and **how it can be verified
here**:
- **test (step 12)** — measurable on the running service with the test
  infrastructure: load, latency, fault injection and recovery, staleness,
  compatibility diffs, observability signals, architecture rules;
- **review (step 13)** — judged by reading: data-protection design, logging
  content, structure where no rule tool fits;
- **both**;
- **not verifiable here** — needs production or a staging environment (real
  availability, real capacity); logged as an *open verification* with what
  would verify it and who owns it.

An answer without a number ("fast", "highly available") is asked once more
for a measure, with a suggested value marked as a suggestion. If there is
still none, the scenario is logged *not measurable* and verified by review
only. A skipped question is logged as a deferred quality question, with the
decisions and tests it leaves without a driver. Show all scenarios together
and ask to accept or revise them. Offer to write them as Turtle
(`quality-scenarios.ttl`, in the working directory, never merged into the
user's files) for ingest into the Architecture Knowledge Graph.

The accepted scenarios drive 5c–5f: every question cites the scenarios it
affects, every recommendation says which scenario it serves, and every ADR
names the scenarios it serves or trades off.

**Required decisions.** These must be decided before code is written, or
explicitly skipped by the user (see 5e–5f):
- language, framework, build tool; package layout (hexagonal / layered);
- persistence style (state-stored table per aggregate vs event store),
  database, ORM or query library, migration tool;
- broker and client library; transactional outbox yes/no; consumer
  idempotency; delivery guarantees;
- concurrency (optimistic locking with a version column);
- error format (RFC 9457 Problem Details);
- ID strategy; time source; how domain errors map to HTTP status codes;
- **authentication**: who calls the service (people through a frontend or
  gateway, other services, message consumers) and how each proves its
  identity — OAuth 2.0 / OpenID Connect bearer tokens validated by the
  service (issuer, audience, signing keys, clock skew), tokens validated at a
  gateway and trusted downstream (and how that trust is secured), mTLS
  between services, API keys only with a stated reason; how messages are
  authenticated (broker credentials, per-channel access rules, signed
  payloads where needed); how the identity reaches the application layer (a
  principal type of the application, never a framework type in the domain).
  The security schemes in the OpenAPI and AsyncAPI specs must match this
  decision; a mismatch goes back to step 4;
- **authorization**: the model (scopes, roles, attributes, or an external
  policy engine) and where each kind of check lives — scope or role checks
  at the inbound adapter; resource checks ("does this actor own this
  loan?") in the application service before the domain call; in the domain
  only when a rule names the actor, and then it is an invariant with an
  `INV` ID. Which board actor may issue which command, taken from the
  board's actors and the brief, with **deny by default**; when to answer
  401, 403, or 404 to hide that a resource exists; tenant isolation if the
  service is multi-tenant;
- **security baseline**: where secrets come from (never from code or
  committed config), request limits (body size, rate limits — in the
  service or at a gateway), security headers, security-event logging
  without personal data, and the tools step 14 uses (dependency audit,
  secret scan, static analysis, a dynamic scanner for the running service);
- code generators for the inbound and messaging layer: which OpenAPI
  generator and mode (interface-only or delegate), which AsyncAPI generator
  or template, their pinned versions, and where generated sources live;
- delivery details the step skills need: retry and backoff, maximum
  deliveries before a dead letter (an unbounded redelivery is an open risk),
  order per aggregate, which events are published;
- idempotency of policy handlers: the processed-message record written in
  the same transaction as the aggregate (a unit of work in step 7);
- query side: consistency (in the command's transaction or eventual), the
  staleness accepted, **read-your-writes** for the actor who just changed
  something, history retention for rebuilds, paging style;
- test infrastructure: the **fast/slow split** (one command for fast unit
  tests without containers, one for tests on real infrastructure; containers
  started once per run and reused); how the database and the **broker** run in tests
  (Testcontainers, local servers), and the contract-test tools for step 12;
- property-based testing for step 6: the library (for example fast-check
  with Vitest, jqwik with JUnit 5, Kotest property testing for Kotlin), the
  run count, and how the seed is fixed;
- build and pipeline: the one command that generates, builds and runs every
  test; the stack's formatter, linter and type checker for the hooks; and how
  the CI provider chosen in step 0 runs the command (which jobs on push,
  which on the pull request, which on a schedule) — the walking skeleton
  proves this first.

**5c. Pre-fill.** With an architecture decision graph from step 1, show for
each checklist item the in-scope decision that settles it (ID, status,
decided option) or "open". Only decisions with an adopting status (the
graph's own status word, e.g. *Accepted*, *Adopted*) count as settled;
superseded ones are shown with their successor. Without a decision graph,
ask for an ADR log and pre-fill from it; otherwise every item is open.

**5d. Formulate the open questions.** Collect everything still open into one
numbered question list and show it before asking anything:
- every checklist item marked "open";
- the questions `architecture-question-finder` raises from the artifacts
  (run it on the graph, or on the brief, invariants and specs);
- the governance issues from step 1: unacknowledged overrides and principles
  in tension that touch this context;
- the decisions the quality scenarios force (for example a latency budget
  that rules out a synchronous call to another context, or "no event may be
  lost" that requires the outbox).
Write each as **one concrete question** with: why it matters for this
context (cite the board element, invariant, spec element, principle or
quality scenario that raises it), the options on the table (from the graph's
considered options where they exist), the consequence of each — including
its effect on the quality scenarios it touches — and a recommendation marked
as such, naming the scenario it serves. With the quality goals ranked in 5b,
a tie between options goes to the higher-ranked goal. Order the list so that questions later steps depend on most come first
(stack and persistence before broker, broker before outbox, authentication
before authorization, both before the generators, and so on). The
authentication and authorization questions may be skipped only with an
explicit warning: without them, step 8 cannot check access and step 14
reports every protected operation as a finding.

**Now or later.** Mark each question **now** or **later**:
- **now** — the walking skeleton cannot be built without it: stack and
  build, package layout, persistence and migrations, ID strategy and time
  source, error format, authentication and the authorization model,
  generators, test infrastructure with the fast/slow split, and — only if
  the skeleton slice publishes or consumes — broker, outbox and consumer
  idempotency;
- **later** — everything the skeleton does not touch (retry and dead-letter
  details, query-side consistency and paging when the skeleton has no GET,
  caching, rate limits, property-testing library when the skeleton has no
  quantitative rule, load and scan tooling, CI jobs beyond `verify`). A
  *later* question names the step and increment that first needs it; it is
  asked there through 5f, with what that increment has shown.
Show the list with the marks and let the user move questions between them.
Only *now* questions are asked in 5e. Quality scenarios (5b) are still all
collected here — they are short and drive the marks.

**5e. Ask question by question.** Ask exactly one question at a time, in the
list's order, naming its number ("Question 3 of 9 — …"). Offer (option picker
when available): the options, **Other** (the user's own answer), and **Skip
for now**.
- **Answered** → note the decision and move on. When all *now* questions
  are answered, draft their ADRs (context, decision, consequences, the
  principles each honors or overrides — an override is stated, never
  implied — and the quality scenarios it serves or trades off) and show
  them **together** for one review: accept all, or name the ones to revise.
- **Faster path** — before asking one by one, offer the *now* questions as
  one list with the recommended option of each: **Accept all
  recommendations** / **Change some** (then only those are asked).
- **Skip for now** → log the question as *deferred*, with the later steps it
  blocks, and move to the next question without arguing. Never re-ask a
  skipped question within the same pass.
- The user may stop the round at any point ("enough for now"); the remaining
  questions count as skipped.
- After the last question, show the summary: decided (with ADR IDs),
  *later* (with the step and increment that will ask), skipped (with what
  each blocks), and the skipped ones that block step 6 or 7. Offer **one** more pass over the skipped questions; accept "no".

**5f. Skipped and *later* questions.** A skipped or *later* question that a
later step needs is asked **at that step**, in the same form, before the step's work
starts — with **Skip** still allowed. If it is skipped again, that step uses
no default silently: it says what it will do instead (for example "implement
without an outbox; events are published after commit and can be lost") and
logs that as an open risk.

New ADRs written here are offered for ingest into the decision graph with
`adr-and-principles-ingester`, so it checks them against the principles.

Done when: the Architecture Knowledge Graph question was answered (file,
"same file" or "not available"); every critical quality area is covered by
an accepted scenario, or logged as deferred or *not measurable*; every
scenario says how it is verified; every decision question was asked once
and is either decided (with an accepted ADR naming its scenarios) or skipped
(logged with what it blocks); and no new decision overrides a principle or
trades off a scenario without saying so.

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

<!-- shared:query:quality-requirements-in-the-graph -->
**Quality requirements in the graph** — there is no fixed class
yet for quality goals and scenarios, so match on the class name, as for API
specs:

```sparql
SELECT DISTINCT ?cls (COUNT(DISTINCT ?n) AS ?count) WHERE {
  ?n a ?cls .
  FILTER(REGEX(STR(?cls), "(Quality|Scenario|QualityGoal|QualityRequirement|Constraint)", "i"))
} GROUP BY ?cls
```
<!-- /shared:query:quality-requirements-in-the-graph -->

<!-- shared:query:quality-inventory -->
**Quality inventory** — the scenarios with their parts; replace
`CTX`. Properties vary by graph, so read every literal of each scenario and
map it to the six parts by its predicate name; a part with no literal is
*missing*:

```sparql
SELECT ?n ?label ?p ?value ?scope WHERE {
  ?n a ?cls . FILTER(REGEX(STR(?cls), "(Quality|Scenario)", "i"))
  OPTIONAL { ?n skos:prefLabel|rdfs:label ?label }
  ?n ?p ?value . FILTER(isLiteral(?value))
  OPTIONAL { ?n (dkg:inContext|dkg:mentions|dkg:scope) ?scope }
  FILTER(!BOUND(?scope) || EXISTS { ?scope skos:prefLabel ?sl .
           FILTER(LCASE(STR(?sl)) = LCASE("CTX")) } || isLiteral(?scope))
} ORDER BY ?n ?p
```

A scenario with no scope applies system-wide; a scoped one whose scope
names another context is out of scope for this run. Never treat a quality
node as a measurable scenario unless its response measure is a number, a
percentile or a time.
<!-- /shared:query:quality-inventory -->

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

If a query returns nothing for the chosen context, say so and fall back to
asking for an upload; never conclude the domain has none.

## Revisiting this step

When this step is revisited ("go back to step N"), re-check — not
regenerate — the completed steps below; offer keep / patch / redo for each
inconsistent one. The full table is in SKILL.md.

| Revised step | Re-check |
|---|---|
| 5 Quality scenario added, changed or reprioritized | the ADRs that cite it (5d–5e), then its step 12 test and step 13 verdict in every increment already built, then 15 |
| 5 Decisions | 6–12 for the decisions that changed; an authentication, authorization or security-baseline decision also re-checks 4 (security schemes) and 14 for every increment already built |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Architecture Knowledge Graph (step 5): <file, "same file as decision graph" or "not available">   Quality goals: <ranked list>
Quality scenarios (step 5): QS-01 … QS-nn — <from graph / answered / deferred / not measurable>; verify by <test / review / both / not here>; quality questions asked <n>, skipped <n>
Step 5 questions: <n> asked, <n> decided (ADR IDs), <n> skipped — Q<id>: blocks steps <list>; re-asked at step <n>: decided / skipped again (risk logged)
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
