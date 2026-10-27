# Step 7 — Persistence

Loaded by `blueprint-service-implementation` at the start of step 7. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

- One repository port per aggregate in the domain layer (find by ID, save);
  the adapter implements it with the ADR's technology. The port's errors
  (`ConcurrencyConflict`) live **next to the port**, never in the adapter.
- Schema and versioned migrations per aggregate; version column for
  optimistic locking by compare-and-set; unique constraints only where an
  invariant demands one, named after the rule.
- Outbox, if the ADR chose one, **complete from the first migration**: event
  columns plus the delivery columns step 10 needs (`attempts`,
  `next_attempt_at`, `last_error`, `dead_lettered_at`) and the claim index;
  written in the same transaction as the aggregate.
- A unit of work where a use case writes more than the aggregate (the
  processed-message record of a policy handler); a real adapter for every
  port a later step uses — no port reaches step 12 with only a fake.
- Integration tests against a real database (Testcontainers or a local
  server); an imitation only with the user's consent.
Done when: migrations apply cleanly, the repository contract passes against
the real database, and the leak checks are clean.

<!-- shared:test-first-7-12 -->
## Steps 7–12 — test-first as well
Each of steps 7–12 runs the *TDD cycle* per unit with its step skill: a test
list first (integration tests against a real database in step 7; use-case
tests with contract-checked fakes in step 8; in-process HTTP and consumer
tests against the specs in step 9; mapping tests and relay tests against the
real database with a fake broker in step 10; projection and query tests
against the real database in step 11; real-adapter, acceptance and contract
tests against the running service in step 12), approval, then one behavior
at a time. Each step's contract says what the finished units must show, for
the current increment's slices (see *Increments — scope rules*); the step skills say how.
<!-- /shared:test-first-7-12 -->

## In increment 0 (walking skeleton)

- **Step 7:** the first migration (with the complete outbox, if the ADR chose
  one) and the repository, against the real database.

The whole walking-skeleton definition is in the shared block below.

<!-- shared:tdd-cycle -->
## TDD cycle (steps 6–12) — EXACT Coding

All hand-written code in steps 6–12 is written with **EXACT Coding** (see
*EXACT Coding* under step 0): its Test List phase, then one-behavior
Predictive TDD cycles. A step skill does not change that — step skills
supply the **content** of a unit's test list and the target shape of its
code; EXACT Coding supplies the **method**. Its rules are binding as written,
with exactly two adaptations, both stated below and logged in step 0: **what
a prediction covers** (behavior, not numbers) and **which tests run in a
cycle** (the fast loop; the slow suite at the unit checkpoint).

**Fast and slow tests.** The test command is split in two (decided in step 5,
set up in the walking skeleton):
- **fast** — unit tests without containers or network (domain, application
  with fakes, mappers, in-process HTTP with fakes): seconds;
- **slow** — everything on real infrastructure (PostgreSQL, the broker,
  end-to-end, acceptance, contract runs). Containers start **once per test
  run and are reused** across test files, never once per file or per test.
Load, fault-injection, dynamic-scan and full contract-fuzzing runs are
neither: they are **CI jobs** (pull request or schedule), never part of a
TDD cycle (see *Increment 0 — Walking skeleton*).

**What a prediction says.** Before every check, state **which tests will fail
or pass and why** — the failing assertion or error you expect, and for a Red
that it fails for the behavioral reason. Gates (type check, lint, format,
architecture rules) are predicted only as **pass or fail**. Never predict
test counts, lint totals, line numbers or durations: a mismatch in those is
not evidence about behavior and is not investigated as one. Lint rules that
emit a finding for every function (for example cognitive complexity with
threshold 0 as a "measurement") are off in the loop; complexity is reported
once per unit at the unit checkpoint.

**Trials** (breaking code on purpose to prove a check can fail, then
restoring it) are run only for a test that is **green on activation** and
once for each **new gate**; not for tests that already went Red for the
predicted reason.

**Per unit of work, within the current increment** (one aggregate in step 6; one adapter, use-case group,
wiring unit, publisher or projection in steps 7–11; one acceptance or
contract suite in step 12):

1. **Test list — EXACT `test-list`.** One test file per unit with every
   behavior as an **inactive test** (`it.todo` in Vitest, `@Disabled` in JUnit
   5), grouped by user story, then by slice, and ordered simple → complex
   within each group (see *Slice order inside a step*), expected values in
   each description, and the trace at the end after `--`, starting with the
   slice ID and its user story (`-- B1-S03 US-07 INV-03 ex.2`,
   `-- B1-S03 US-07 POST /loans 409`, `-- ADR-007`). A slice that serves
   several user stories names all of them (`US-07,US-09`). The step's contract below says what the list must cover;
   EXACT's coverage cross-check treats those elements as the independent
   dimensions. Run the fast suite (and the unit's own slow file, if it has
   one): the inactive list must leave it green. Apply the Test-List
   checkpoint; the approved list is the scope.
2. **Cycles — EXACT `predictive-tdd`**, with the project's stack profile: one
   behavior at a time; a behavioral prediction before every check (see *What
   a prediction says*); Red for the predicted reason; minimal Green;
   Refactor under the Four Rules of Simple Design with the
   domain-responsibility review and the boundary trial. **Each cycle runs the
   unit's own test file plus the fast suite**, and the type check. The slow
   suite and all of the profile's gates (lint, format, architecture rules,
   PMD where configured) run at the **unit checkpoint** and before every
   commit — twice in a row only for tests that touch concurrency or timing.
3. **Checkpoints — EXACT `human-in-the-loop.md`.** Ask once, at the first
   unit, which autonomy level to use (default `full-hitl`: stops after Test
   List, Red, Refactor and every prediction mismatch; others: `refactor-only`,
   `red-only`, `every-n-tests N`, `task-end`, `autonomous`). Set it in the
   project copy of that file and log it; the user can change it at any unit
   checkpoint. `full-hitl` is recommended for increment 0; at the readiness
   gate of every later increment, offer `refactor-only` (recommended) or
   `task-end`. The single-phase controls `red`, `green` and `refactor` are
   available when the user wants to drive one phase at a time.
4. **No phase commits and no hard resets** (EXACT's method boundary). Work is
   kept in the working tree during the cycles. Commits follow the policy
   from step 0 (*Delivery*, `references/step-00-start.md`): by default one Conventional Commit per approved
   unit checkpoint, on the increment's branch, with the message shown at the
   checkpoint. The pre-commit hooks run on every commit; a failing hook is
   fixed under green tests, never bypassed (`--no-verify` only with the
   user's explicit consent, logged).
5. **Unit checkpoint.** When every test in the file is active, the fast and
   slow suites are green and all gates pass, show the unit as a whole — code, tests (active/total), prediction
   mismatches and how they were explained, boundary trials retained or
   undone, tests added on the way, open questions, and the proposed commit
   message (step 0 policy), and the slice-refactoring reports of the unit —
   and ask for approval before the next unit. The step checkpoint follows
   after the last unit.

**Slice order inside a step.** Each unit's test list is grouped by user
story in the board's story order (step 1.4c), then by slice in slice-plan
order, and ordered simple → complex inside each group. A slice already built
for an earlier user story is not repeated; only the behaviors the new story
adds to it are listed. A slice is **implemented in a step** when the last of
its tests in that step's units is active and green; that moment is the
slice's **refactoring point**. A user story is **done** when its slices are
implemented in every step of the increment and every example of its Example
Mapping passes as an acceptance scenario in step 12.

**Fallback** (only when EXACT Coding cannot be located): the same inactive
test list; per behavior predict, Red for the predicted reason, least code to
Green with the fast suite, refactor under the Four Rules; show and ask before
the next behavior. No production code without a failing test, never change a
test to make it pass without asking, never report a result that was not run.
<!-- /shared:tdd-cycle -->

<!-- shared:slice-refactoring -->
## Slice refactoring — EXACT refactor agent

After every slice implementation — at each refactoring point in steps 6–12
(and in increment 0 after each step, since the skeleton is one slice) — start
a **fresh** isolated refactoring agent from EXACT's
`.claude/agents/refactor.md`. It adds to the Refactor phase of each
Predictive TDD cycle; it does not replace it. The cycles refactor one
behavior from the inside; the agent reviews the whole slice from outside,
without the implementer's reasoning.

**Start.** Use the `Agent` tool with the agent type `refactor` when the
session lists it; otherwise start a general-purpose agent whose
instructions are the content of `.claude/agents/refactor.md`, unchanged,
followed by the inputs. A new agent for every refactoring point — never one
continued from an earlier slice. Run it in the foreground: the next slice
waits for its result.

**Inputs** (the agent's isolation is the point — it infers domain behavior
from specification, code and tests only):
- the slice's specification: the user stories it serves, its rows of the
  design brief, its invariants with their Given/When/Then scenarios, its spec operations and messages, the
  ADRs and testable principles that touch it, and the glossary terms of its
  ubiquitous language;
- the production and test files the slice touched in this step, and their
  direct collaborators;
- the stack profile's test command and gates.
Never the conversation, the predictions, the ledger's rationale or the
implementer's explanations.

**Limits** the orchestrator adds to the agent's contract:
- It changes hand-written production code of this step's layer only.
  Tests change only mechanically, to follow a rename or move — never an
  assertion, an expected value, or the number of tests.
- Never generated code, applied migrations, specs, documents or ADRs. A
  structural move that needs a schema change, a spec change or a new
  decision is reported as a **proposal**, not made.
- Names stay in the ubiquitous language of the glossary and the brief. A
  rename away from a glossary term is a proposal (it would show up as drift
  in the reverse check), not a change.
- Every trace comment and test trace after `--` survives the move.

**Before the agent starts**, the orchestrator saves the current diff of the
working tree as a patch in the scratchpad, so the agent's changes can be
undone exactly with a reverse patch — never with a hard reset.

**After the agent returns:**
1. Check its report has every part the agent file asks for: rules reviewed,
   retained and undone changes, the domain-boundary outcome, the semantic
   effect, the Fewest Elements check, its predictions, and the final test
   result.
2. Predict (pass or fail, and which tests a failure would hit), then run the
   fast suite, the slice's slow tests and the profile's gates again, and
   check the test diff: only mechanical changes. A red suite, a gate
   failure or a changed assertion undoes the agent's changes with the
   reverse patch, and is logged.
3. Show the result at the autonomy level from EXACT's
   `human-in-the-loop.md`: with `full-hitl` or `refactor-only`, stop and ask
   **keep / undo / keep part**; otherwise it is shown at the unit
   checkpoint. Proposals become open questions with the step they would go
   back to.
4. Retained changes are committed separately, as
   `refactor(<scope>): … Refs: US-07, B1-S03` under the step 0 policy, after the
   slice's own commit.

If the agent finds nothing that improves the Four Rules, it leaves the code
unchanged and says why; that is a valid result and is logged as such.

**Agent not available** (no `Agent` tool, or the agent file cannot be
located): say so once and offer **Run in a fresh session** — write the
agent file, the inputs and the limits to `refactoring/<slice>-<step>/`, the
user runs it in a new session and the result is checked as above — or
**Skip slice refactoring**, logged as "no isolated refactoring" for each
slice. The implementing agent never runs the refactor contract on its own
code and calls it isolated.
<!-- /shared:slice-refactoring -->

<!-- shared:external-generators -->
## External generators

Offer only when the step 5 stack fits, and always review the output against
the step contract. Generation from the approved specs (steps 9 and 10) is
the default, not an option; the rest is optional:
- OpenAPI Generator in interface-only or delegate mode for controller
  interfaces and DTOs (step 9), for most mainstream server stacks.
- AsyncAPI Generator templates for listener interfaces and message classes
  (steps 9 and 10), where a template exists for the stack.
- ZenWave SDK (Java/Spring): OpenAPI controller implementations, AsyncAPI
  producers/consumers (steps 9, 10); its backend scaffolding (step 7) is
  optional and falls under the TDD rule.
- AI Unified Process `aiup-angular-jpa` plugin (Spring Boot + JPA): Flyway
  migrations (step 7), Spring Boot tests (step 12).
- TypeScript without a reliable server generator: `openapi-typescript`
  (types) with `express-openapi-validator` (runtime contract) and a
  compile-time coverage gate (step 9); `@asyncapi/modelina` for message
  models plus `@asyncapi/parser` and `ajv` with `ajv-formats` for runtime
  checks (steps 9, 10).
- Test tools (step 12): Cucumber for acceptance, Schemathesis for the
  OpenAPI contract run, Testcontainers or local servers for database and
  broker.
Generated CRUD-shaped code must not replace command methods: if a generator
produces setters or CRUD endpoints for a command-oriented spec, keep only the
parts the contract allows and say what was discarded.
<!-- /shared:external-generators -->

<!-- shared:increments -->
## Increments — scope rules

Steps 6–14 never run over the whole context at once. They run once per
increment, in the order of the slice plan from step 1:

| Increment | Scope | Announced as |
|---|---|---|
| 0 | the walking-skeleton user story's thinnest slice (or two) | Increment 0 — Walking skeleton (US-nn) |
| 1…n | the slices of one design-level board not built yet, grouped by its user stories in story order | Increment k — Board: <board name> |

**Scope inside an increment.** Each step applies its contract to the
increment's slices only: the commands, aggregates, invariants, events,
policies, read models, operations and channels those slices own (the slice
plan and the step 4 reconciliation table say which). Everything else stays
out of the increment. Inside the increment, work proceeds **user story by
user story** in the board's story order, so the first user story can be
shown working before the last one starts (see *Slice order inside a step*).
With one pull request per user story (step 0), each user story runs steps
6–12 on its own branch; steps 13 and 14 still review the whole increment.

- **Growing units.** A unit that exists from an earlier increment (an
  aggregate, a repository, a use-case group, a wiring unit, a projection) is
  extended, not rewritten: the new behaviors are appended to its test file as
  inactive tests, the additions pass EXACT's Test-List checkpoint, then
  predicted cycles run as usual. An approved test from an earlier increment
  is never edited to make a new behavior fit; a conflict is shown and asked
  about.
- **Regression.** Every unit checkpoint runs the fast and slow suites, so
  earlier increments stay green. An earlier test that turns red is a defect of the current increment,
  unless the user decides the earlier behavior changes — then it goes back to
  step 3 or 4 as a revision.
- **Migrations are additive.** A later increment adds migrations; it never
  edits an applied one.
- **Planned, not missing.** The coverage gates of step 9 (every operation and
  receive operation wired), step 10 (every domain event has a publish
  decision) and step 12 (intent coverage) check against the slices built so
  far. Operations, channels, events, rules, user stories and quality
  scenarios of later boards (or of user stories taken out of this
  increment) go on one
  **planned list**, each with the increment that will build it
  (`planned: increment 3 — Board: Returns`). A gate fails on anything that is
  neither built nor planned. The list shrinks with every increment and must
  be empty after the last one.
- **Order.** Boards follow the slice plan; user stories inside a board follow
  its story order. A board whose policies or read
  models need events of another board comes after that board. A cycle
  between boards is an open question: the user picks the order, and the
  events the first board needs from the later one are supplied as test data
  until then, marked in the ledger.
<!-- /shared:increments -->

<!-- shared:walking-skeleton -->
## Increment 0 — Walking skeleton

The skeleton is the thinnest slice of one user story that runs end to end
through every layer the service will have, on real infrastructure. It proves
the architecture, the build, the generators and the test infrastructure
before the domain grows; it does not aim at features — the user story it
belongs to is finished later, in its board's increment, unless the skeleton
slice already covers it completely. Steps 6–14 run with their full contracts
and EXACT Coding, scoped to the skeleton.

**Keep it thin.** The skeleton proves that each layer is *wired*, not that
it is *complete*. Build only what the skeleton slice cannot run without:

- **In the skeleton:** the happy path of the skeleton command through every
  layer it touches, the rules that command guards, the real adapters it
  needs, one build command, the fast/slow test split, the `verify` CI job,
  the pre-commit hooks, and authentication on the skeleton's operation.
- **Not in the skeleton** (each goes on the planned list with the increment
  that first needs it): other operations, channels and GETs; resilience
  beyond a timeout (circuit breakers, retry policies, dead-letter
  handling beyond the broker's own limit); upstream clients the skeleton
  slice can run with a stub; coverage thresholds; load seeds; the load,
  fault-injection, contract-fuzzing and dynamic-scan harnesses.

When an approved list in steps 6–12 grows beyond this (a user decision to
build more now), say what it adds to increment 0 and log it as a scope
decision; never grow the skeleton silently.

- **Step 6:** the skeleton's aggregate with only the skeleton command, its
  guards and its event(s).
- **Step 7:** the first migration (with the complete outbox, if the ADR chose
  one) and the repository, against the real database.
- **Step 8:** the skeleton's use case or policy handler.
- **Step 9:** the generator pipeline for both specs, run from the build; the
  skeleton's operation or channel wired; the edge unit (404/405/415/400).
  Every other operation and channel is on the planned list.
- **Step 10:** if the skeleton event is published, its mapper and a relay
  that delivers at least once with a stable message ID; retries, backoff and
  dead letters come with the first increment that needs them. Otherwise
  every published channel is planned and step 10 is skipped for this
  increment.
- **Step 11:** if the skeleton slice itself has a read model or GET
  operation, only that projection and query; every other GET is planned.
  Otherwise skipped for this increment.
- **Step 12:** the composition root, the real database and broker adapters
  the skeleton touches, the skeleton user story's feature file with one
  acceptance scenario per example of the rules the skeleton slice guards,
  the contract check of the skeleton's operations and messages against the
  specs (examples and schemas — no fuzzing run), and the whole build from
  one command. It also writes the delivery setup from step 0 for the chosen
  stack: the **`verify` CI job** running that command on every push and pull
  request, the **pre-commit hooks** (format, lint, type check, secret scan,
  commit-message check), and the `docs/<context>/` folder with the steps
  1–5 artifacts. The skeleton is done only when `verify` is green on its pull
  request. Quality scenarios: only those that need **no new tool and no
  long run** are tested now (typically module boundaries as architecture
  rules, a correlation ID through one request). Every other testable
  scenario is planned to the increment whose operations it measures; its
  harness (load tool, fault injection, spec diff, fuzzing) is built there,
  and long runs are CI jobs, never part of `verify`.
- **Step 13:** an independent review of the skeleton's code, tests, build
  and generator setup — **one** inspector (architecture focus), since every
  later increment builds on it; a second only when the user asks.
- **Step 14:** the security review of the skeleton, with the authentication
  and authorization mechanism from the step 5 ADR in place on the skeleton's
  operation — the skeleton proves auth end to end, and every later operation
  reuses it. Only the fast baseline checks are wired into `verify` now
  (dependency audit, secret scan); static analysis and the dynamic scan
  become CI jobs in the first increment the user schedules them for, each
  with a time cap.

Done when the skeleton's acceptance scenarios (its user story's feature file,
limited to the skeleton slice) pass against the running service on real
infrastructure, built from one command, the independent
review has no open major defects, and the security review has no open
high or critical finding.
<!-- /shared:walking-skeleton -->

<!-- shared:principles-guardrail -->
## Principles as guardrails

From here on, every testable principle in scope
is a constraint on steps 6–12. After each of those steps, check its output
against them and report each one as honored, overridden (with the ADR that
allows it) or violated. A violation is fixed or logged, never ignored.
<!-- /shared:principles-guardrail -->

<!-- shared:defect-routing -->
## Defects and missing step skills

- **A defect a later step finds** (steps 12 and 13 above all) goes **back to the
  step that owns the code** — as a new test there, red first — or, for a
  spec, back to step 4. The finding step records it; the owning step's
  ledger row gets a revision. Never fixed silently where it was found.
  After the fix, when step 12 is green again, run the scoped **reverse
  check** (*Reverse check* — in the step 12–15 files) before the increment checkpoint. A defect from the
  step 13 review or a step 14 security finding takes the same path.
- **Step skill not available** (another workspace, an older install) → say
  so in one sentence and offer:
  1. **Implement directly** — follow the step contract below yourself, with
     the *TDD cycle*.
  2. **External generator** — only where the stack fits (see
     *External generators* (in the step 7, 9, 10 and 12 files)); run it, then review the output
     against the contract.
  3. **Defer** — log the step as deferred with its open inputs; later steps
     that depend on it must say what they cannot verify.

Record in the ledger which route was taken, so the user can later replace a
direct implementation with a new skill and re-run the step.
<!-- /shared:defect-routing -->

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
| 7 Persistence | 8, 10, 11, 12 |
| An increment's step 6–12 | the same step's later work in this increment, then steps 7–12 of every later increment already built that extend the same units, then steps 13 and 14 of each affected increment for the changed units |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
TDD (steps 6–12): per unit, one line — <test file>, <active>/<total> tests, <added> added on the way, behavioral prediction mismatches <n>, gates green y/n, approved <date>, commit <hash>, journal ledger/journal-inc<k>-step<n>.md
Slice refactoring: per refactoring point — increment <k>, step <n>, slice <id>: retained <n>, undone <n>, boundary <seam made visible / cohesive: evidence>, proposals <n> → step <n>, prediction mismatches <n>, suite <green / undone by reverse patch>, decision <keep / undo / part>, commit <hash>
Test infrastructure (steps 7–12): database <Testcontainers / local server / user ran it>, broker <real in step 12: which>
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
