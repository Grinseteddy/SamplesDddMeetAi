---
name: "blueprint-service-implementation"
description: "Guided pipeline that implements ONE bounded context as a tested service (API controllers, application services, domain model, repository, database) from a domain knowledge graph and an architecture decision graph, design-level EventStorming boards, user stories, a ready Example Mapping per board, invariants, OpenAPI/AsyncAPI specs and ADRs. Scopes by boards AND user stories, builds a thin walking skeleton end to end first, then one board per increment, user story by user story, with EXACT Coding and a fast test loop; each increment closes with an independent Fagan review and an OWASP security review by separate agents. Asks at every step which input to use. Use for \"implement this bounded context\", \"run the implementation blueprint\", \"continue the implementation at step N\", \"build the walking skeleton\", \"implement the next board\", \"implement the next user story\"."
---

# Blueprint Service Implementation — the 15-step pipeline

Drive the implementation of **one bounded context** through fifteen ordered
steps, from design artifacts to a tested service. At every step the user
decides where the input comes from; the skill never runs ahead. The work is
spec-first and hexagonal: inbound adapters (REST controllers, message
consumers) → application services → domain model → outbound adapters
(repository, database, event publisher).

The work is **sliced vertically**. Steps 1–5 settle the whole context once.
Steps 6–14 then run in **increments** (see *Increments*): steps 6–12 build
and test, step 13 has the result inspected by a separate agent that did not
write it, and step 14 reviews its security against the OWASP Top 10 lists.

- **Increment 0 — walking skeleton:** the thinnest slice (rarely two) of one
  user story, taken through every layer, from the API to the real database
  and broker, before anything else is built.
- **Increment 1…n — one design-level EventStorming board each:** steps 6–14
  for the remaining slices of that board, story by story.

**Scope is set on two axes**, and both are required:

- **Design-level EventStorming boards** say *how* the domain behaves:
  commands, aggregates, events, policies, read models. They decide the
  increments.
- **User stories** say *what value* is delivered and *for whom*. They decide
  what is "done" inside an increment, and they are the yellow cards of the
  Example Mappings.

A **slice** is one *command → aggregate/system → event(s)* unit of a detail
board, with its trigger and read models, as `event-storming-detail-interpreter`
defines it. Every slice belongs to **one board** and serves **at least one
user story**; every user story is realized by **at least one slice**. The
**slice plan** is this board × user-story matrix. Step 15 runs once, after the
last increment.

**Every board requires an Example Mapping** — one map per board, with one
yellow story card for each user story the board's slices realize. A board
whose Example Mapping is not *ready* (step 3c, `references/step-03-invariants.md`) cannot start its
increment.

**Terms.** *Domain Story* always means a Domain Storytelling diagram
(actor → activity → work object). *User story* always means a backlog story
("As a … I want … so that …"). Never write "story" alone where the two could
be confused.

The skill is an **orchestrator**. It does little work itself: each step hands
off to a specialist skill and checks the result against the step's *contract* in its step
file. Steps 6–12 each have their own **step skill** (see *Step skills*); the
orchestrator hands over the inputs, owns the checkpoints and the ledger, and
sends defects back to the step that owns them.

## Ground rules

- **One step at a time.** Announce the step number, its name, and whether it is
  mandatory or optional. Then ask for the input. Never run two steps without a
  decision in between.
- **Always ask for the input source, in the same shape.** Use the interactive
  option picker when the client has one. Offer only the options that apply to
  the step:
  1. **Graph** — extract the step's input from the domain knowledge graph or
     the architecture decision graph (queries in each step file). Only offered
     once step 1 has received a graph that holds content for this step.
  2. **Upload** — the user provides the artifact (photo, export, file, text).
  3. **Generate** — derive the artifact with the step's generate skill from
     earlier steps' results; show it and wait for approval.
  4. **Reuse** — take the output of an earlier step of this run (when one
     exists).
  5. **Skip** — only for optional steps.
- **State exactly what is expected.** Before asking, name the artifact the step
  needs, its accepted formats, and which earlier result could stand in for it
  (one or two lines, from the step table).
- **Accept before moving on.** After a step produces its output, ask "accept,
  revise, or replace?" Only accepted output is carried forward. The user's
  edited version is the artifact of record, never the draft.
- **Approve every step before the next one.** Close every step, 0 to 15,
  with the *step checkpoint* below. Never start the next step without the
  user's explicit approval of the current one — not even when the step had
  nothing to ask, was skipped, or was deferred.
- **Never invent inputs.** If a step needs something an earlier step skipped,
  say so and offer: provide it now, generate it now, or skip this step too.
  Never fill a gap with assumed content; open questions go into the ledger.
- **Never invent domain behavior.** Every guard, state transition, command
  method and event in the code must trace to a board element, an invariant or
  a spec element. Anything else is an open question, not code.
- **Never invent value.** Every slice traces to a user story, and every user
  story's "so that" comes from the user, the graph or an uploaded backlog —
  never from a guess. A missing benefit is an open question on the user
  story.
- **Keep a ledger — short.** Maintain `implementation-log.md` in the working
  directory (format in `references/step-00-start.md`). Update it when a
  decision is made, when output is accepted, when a unit or step completes.
  It holds **one summary line per unit, decision, revision and approval** —
  never per TDD cycle. Cycle-level notes (predictions, mismatches, trials,
  slips) go to a **journal**, `ledger/journal-inc<k>-step<n>.md`, one file
  per step and increment; it is never read on resume.
- **One reference file per step.** At the start of step N, load
  `references/step-NN-….md` (see *Step files*) and follow it. Each file is
  self-contained: its procedure and contract, the graph queries, ledger
  lines and re-checks it needs, and — where the step needs them — the TDD
  cycle, slice refactoring, increment rules, walking skeleton and reverse
  check. Those shared blocks are duplicated on purpose; never edit one copy
  alone (`scripts/check_shared_blocks.py --sync-from <file>` copies a block
  to all others). Do not load files of steps that are not running.
- **Resuming.** If the user says "continue at step N", or an
  `implementation-log.md` already exists, read it (not the journals — open a
  journal only when the user asks about a specific cycle), restate where things stand
  in three lines (including the current increment, its board and its open
  user stories), and continue from the first step without a recorded
  approval — starting with its checkpoint if its work is already done.
  "Implement the next board" resumes at the readiness gate of the next open
  increment; "implement the next user story" resumes at step 6 of the current
  increment with that story's slices next.
- **Edit safely.** Read a file before changing it, and check that an edit
  landed (the formatter may have rewritten the file) before running a
  check; a run on unchanged code is wasted and does not count. Use the
  project's formatter with the project's configuration only — never a tool
  version or settings the project does not pin.
- **Revisiting.** "Go back to step N" or "the AsyncAPI spec changed" means:
  re-run step N with the new input, then re-check (not regenerate) every
  completed downstream step using the dependency table. Offer keep / patch /
  redo for each inconsistent one.

## Step checkpoint (end of every step)

When a step's "done when" condition is met, or the step was skipped or
deferred:

1. **Name the step that ends**, with number and name, and summarize it in a
   few lines: what came in (and from where), what came out, what was
   decided, and the open questions it added.
2. **Name the next step**, with number and name, and say in one line what it
   will ask for. After step 14 of an increment, the next step is step 6 of
   the next increment, or step 15 after the last one; the *increment
   checkpoint* (see *Increments*) comes first.
3. **Ask for approval**, in these words or close to them:
   **"Do you approve step N — <name>, so we can go ahead with step N+1 —
   <name>?"** Offer (option picker when available):
   - **Approve and continue** — go to step N+1.
   - **Revise this step** — say what to change; redo the affected part, then
     show the checkpoint again.
   - **Go back to an earlier step** — follow *Revisiting*.
   - **Pause here** — save the ledger; a later "continue" resumes at step
     N+1 after asking for this approval again.
4. **Record** the approval and its date in the ledger before starting step
   N+1. A step without a recorded approval counts as incomplete when
   resuming.

**Condensed checkpoints** (chosen in step 0, from increment 1 on): steps
6–12 of an increment are approved together at the end of step 12, with one
summary per step; unit checkpoints and EXACT's autonomy level still apply.
Steps 0–5, 13, 14, 15, the readiness gate and the increment checkpoint are
always approved one by one. The user can switch back to full checkpoints at
any checkpoint.

Example: *"Step 3 — Invariants and acceptance scenarios is done: 14 rules
from the glossary, two design-level boards and their two Example Mappings
(7 user stories); every rule has an example, 2 red cards deferred to
increment 2, 3 open questions. Next is step 4 — Contracts:
OpenAPI + AsyncAPI, where I'll look for your specs. Do you approve step 3 —
Invariants and acceptance scenarios, so we can go ahead with step 4 —
Contracts: OpenAPI + AsyncAPI?"*

Step names to use, exactly:

| # | Name |
|---|------|
| 0 | Start |
| 1 | Scope: graphs, context, boards and user stories |
| 2 | Design brief |
| 3 | Invariants and acceptance scenarios |
| 4 | Contracts: OpenAPI + AsyncAPI |
| 5 | Quality requirements and architecture decisions |
| 6 | Domain model |
| 7 | Persistence |
| 8 | Application services |
| 9 | Inbound adapters |
| 10 | Outbound messaging |
| 11 | Query side |
| 12 | Tests |
| 13 | Independent review |
| 14 | Security review (OWASP) |
| 15 | Traceability check |

At the start of each step, announce it the same way: **"Step N — <name>"**,
whether it is mandatory or optional, and what it expects. In steps 6–14,
add the increment: **"Step 7 — Persistence · Increment 0 — Walking
skeleton (US-01)"** or **"Step 7 — Persistence · Increment 2 — Board:
Returns"**; while a step works through the increment's user stories, name
the current one in progress updates ("now US-07 — Return a damaged copy").
The checkpoint question names the increment the same way.

## Step files

| Step | Load at its start | Also holds (shared, duplicated) |
|---|---|---|
| 0 — Start | `references/step-00-start.md` | Delivery, EXACT Coding, full ledger format |
| 1 — Scope: graphs, context, boards and user stories | `references/step-01-scope.md` | graph queries for scoping and decisions, principles guardrail |
| 2 — Design brief | `references/step-02-design-brief.md` | graph queries |
| 3 — Invariants and acceptance scenarios | `references/step-03-invariants.md` | graph queries |
| 4 — Contracts: OpenAPI + AsyncAPI | `references/step-04-contracts.md` | graph queries |
| 5 — Quality requirements and architecture decisions | `references/step-05-quality-and-decisions.md` | step 5 checklists, quality and decision queries |
| 6 — Domain model | `references/step-06-domain-model.md` | readiness gate, start of an increment, TDD cycle, slice refactoring, increment rules, walking skeleton, guardrail, defect routing |
| 7 — Persistence | `references/step-07-persistence.md` | TDD cycle, slice refactoring, external generators, increment rules, walking skeleton, guardrail, defect routing |
| 8 — Application services | `references/step-08-application-services.md` | TDD cycle, slice refactoring, increment rules, walking skeleton, guardrail, defect routing |
| 9 — Inbound adapters | `references/step-09-inbound-adapters.md` | TDD cycle, slice refactoring, generated-code exception, external generators, increment rules, walking skeleton, guardrail, defect routing |
| 10 — Outbound messaging | `references/step-10-outbound-messaging.md` | TDD cycle, slice refactoring, generated-code exception, external generators, increment rules, walking skeleton, guardrail, defect routing |
| 11 — Query side | `references/step-11-query-side.md` | TDD cycle, slice refactoring, increment rules, walking skeleton, guardrail, defect routing |
| 12 — Tests | `references/step-12-tests.md` | TDD cycle, slice refactoring, generated-code exception, external generators, increment rules, walking skeleton, guardrail, defect routing, reverse check |
| 13 — Independent review | `references/step-13-independent-review.md` | increment rules, walking skeleton, defect routing, reverse check |
| 14 — Security review (OWASP) | `references/step-14-security-review.md` | skeleton review, increment checkpoint, Definition of Done, increment rules, walking skeleton, defect routing, reverse check |
| 15 — Traceability check | `references/step-15-traceability.md` | reverse check, graph queries for ingest |

Every step file also carries its ledger lines and its rows of the dependency
table below.

## The steps

| # | Step | Kind | Expected input (any one source) | Skill(s) | Output |
|---|------|------|--------------------------------|----------|--------|
| 1 | Scope: graphs, context, boards and user stories | mandatory | **first** a domain knowledge graph and an architecture decision graph (`.ttl`, may be one file); **if no knowledge graph:** at least one Domain Story and a Visual Glossary; **then** the context from the graph, the Domain Stories, a context map, or an EventStorming board with boundaries; **then** the context's design-level EventStorming boards, its **user stories**, a slice plan (board × user story) and a walking skeleton (each from the graph, an upload, or a proposal) | `domain-knowledge-graph` (to build a graph now), `adr-and-principles-ingester` (to build the decision graph now); without a graph: `domain-story-interpreter`, `visual-glossary-interpreter`, `domain-story-glossary-consistency`, `domain-story-context-finder`; `pivotal-event-boundary-finder` or `event-storming-context-mapper` if boundaries are not settled; `event-storming-detail-interpreter` (slice tables of the boards); `user-story-author` (user stories from the graph) | graph inventory, the context name, its neighbors and border contracts, the decisions and principles in scope, the board list, the user-story list (`US-nn`), the slice plan (slice IDs per board, user stories per slice, board order, story order per board) and the walking skeleton (a user story and its thinnest slice) |
| 2 | Design brief | mandatory | every design-level EventStorming board of the context from step 1 (photo, Miro export, sticky list, or graph board nodes); Event Model as alternative; the step 1 user stories | `event-storming-detail-interpreter` (one Detail Design Brief per board); `event-storming-interpreter` only for a big-picture board used as preparation; for an Event Model, read slices directly | per board: aggregates + state machines, slice table, policies, read models, external systems, hotspots, completeness findings; merged into one context brief keyed by slice ID, with the user stories each slice serves |
| 3 | Invariants and acceptance scenarios | mandatory | **first collected** from what exists: graph rules and glossary cardinalities, the Visual Glossary; **then always** a design-level EventStorming of the chosen context (reuse from step 2, graph, or upload); **then, for every board, its Example Mapping** — required, one yellow card per user story of the board | collect: `visual-glossary-interpreter`; board: `event-storming-invariant-finder` (`event-model-invariant-finder` for an Event Model stand-in); Example Mapping: read an uploaded map with `example-mapping-interpreter`, or run a session with EXACT `example-mapping` | one Example Mapping per board with a readiness verdict; invariants sheet grouped board → user story → rule, with IDs, sources, slice per rule, Given/When/Then per example, cross-context rules marked as policies, open questions from red cards |
| 4 | Contracts: OpenAPI + AsyncAPI | mandatory | specs from the graph; if missing, `openapi.yaml` / `asyncapi.yaml` uploads, **or** an API Product Canvas; if none, a proposal derived from steps 1–3 | `openapi-spec-author`, `asyncapi-spec-author` (validate, lint, quality review, author); `context-map-api-proposer` (sync vs async); `domain-knowledge-graph` (contract coverage); `schema-glossary-consistency` if a glossary exists | both needed specs linted and **approved by the user** + reconciliation table (slice and user story per spec element) |
| 5 | Quality requirements and architecture decisions | mandatory | **first** an Architecture Knowledge Graph (`.ttl`: quality goals, quality scenarios, constraints; may be the decision graph from step 1); **if none, or where it leaves questions open:** the user's answers to the critical quality questions; **then** the architecture decision graph from step 1, an ADR log, and the user's answers, question by question | `architecture-question-finder` (formulate the questions), `adr-and-principles-ingester` (check against principles) | quality scenarios `QS-nn` (six-part form, priority, how each is verified); accepted ADRs for answered questions, each naming the scenarios it serves; skipped questions logged with what they block |
| 6–14 | *run once per increment* | — | the increment's slices only (walking skeleton, then one board at a time, story by story), after the board's **readiness gate**; see *Increments* | as below | as below, for the increment's slices |
| 6–12 ↻ | Slice refactoring (continuous) | mandatory | at each slice's refactoring point in steps 6–12 (the slice's last test green in that step): the slice's specification, the files it touched and their collaborators, the test command and gates — never the author's reasoning | EXACT refactor agent (`.claude/agents/refactor.md`), a fresh isolated agent per slice and step, started with the `Agent` tool | retained and undone changes, domain-boundary outcome, proposals, suite green, a separate `refactor(<scope>)` commit; see *Slice refactoring* in the step 6–12 files |
| 6 | Domain model | mandatory | step 2 brief + step 3 invariants + step 4 schemas; optional Visual Glossary (picture, brief, or graph terms); per aggregate: the approved test list | method: EXACT `exact-coding` (`test-list`, `predictive-tdd`, stack profile); content: `aggregate-behavior-author`; naming: `glossary-domain-model-author`; **EXACT refactor agent** after each slice | per aggregate: approved inactive test list, domain code grown one predicted cycle at a time, all gates green |
| 7 | Persistence | mandatory | step 6 model + step 5 persistence ADR + invariants (uniqueness) | `persistence-adapter-author` + EXACT; a real database (Testcontainers or a local server); **EXACT refactor agent** after each slice | repository ports with their errors, adapters, migrations, outbox with delivery columns, unit of work where a use case needs one |
| 8 | Application services | mandatory | steps 3, 5, 6, 7 | `application-service-author` + EXACT; fakes that pass the shared repository contract; **EXACT refactor agent** after each slice | one use case per command, one handler per policy, idempotent |
| 9 | Inbound adapters | mandatory | approved step 4 specs + step 5 generator ADR + step 8 use cases | **generated**: pinned OpenAPI and AsyncAPI generators; **wiring**: `inbound-adapter-author` + EXACT; **EXACT refactor agent** after each slice | generated contract; tested wiring, error map (Problem Details), runtime spec validation, coverage gates |
| 10 | Outbound messaging | optional (mandatory if the AsyncAPI spec publishes) | step 4 AsyncAPI + step 6 events + step 7 outbox | `outbound-messaging-author` + EXACT; generated message models; fake broker behind the publisher port; **EXACT refactor agent** after each slice | one publish decision per event, mappers, ordered relay with retry and dead letter |
| 11 | Query side | optional (mandatory if step 2 has read models or the spec has GET operations) | step 2 read models + step 4 GET operations + events | `read-model-author` + EXACT; **EXACT refactor agent** after each slice | projections (no gaps, one runner, rebuildable), query port, GET operations with paging |
| 12 | Tests | mandatory | the board's Example Mapping (step 3), step 4 specs, steps 6–11 code, step 5 test-infrastructure ADR | `service-test-author` + EXACT; Cucumber, Schemathesis, a real broker; **EXACT refactor agent** after each slice | real broker adapters, composition root, one feature file per user story, contract runs, whole suite, intent-coverage report including user stories done |
| 13 | Independent review | mandatory | the increment's code and tests (diff against the previous increment; the whole code in increment 0), plus the approved artifacts they implement: brief, user stories, the board's Example Mapping, invariants sheet, specs, ADRs, principles, slice plan — **not** the conversation or the author's reasoning | `fagan-code-inspector` (inspector mode) if installed, loaded by one or two separate review agents in a fresh context (`Agent` tool), a different model where one is available; the orchestrator is the moderator | inspection report: defects with severity, location and the artifact element they violate; rework sent back to the owning steps; follow-up result |
| 14 | Security review (OWASP) | mandatory | the increment's code, the running service from step 12, the approved specs (security schemes, scopes), the step 5 auth and security ADRs, dependency manifests | `owasp-security-reviewer` (reviewer mode), loaded by a separate security agent in a fresh context (`Agent` tool), never the author; OWASP API Security Top 10:2023 and OWASP Top 10:2025; dependency audit, secret scan, static analysis and dynamic scan for the stack | security report: findings by OWASP category and severity, with evidence; abuse-case tests for every accepted finding, sent back to the owning steps; accepted risks with an ADR |
| 15 | Traceability check | mandatory | everything above, starting from step 12's intent-coverage report; graph if present | `domain-knowledge-graph` (ingest specs and code map); `arazzo-from-event-model` for journeys if an Event Model exists | traceability matrix (board → user story → example → slice → board element → invariant → spec element → class/method → test → increment), reverse-check drift table, closing report |

### Step skills and agents (steps 6–14)

| Step | Step skill or agent | It supplies | EXACT supplies |
|---|---|---|---|
| 6 | `aggregate-behavior-author` | the aggregate's test-list coverage and target shape | the method |
| 7 | `persistence-adapter-author` | repository and outbox test lists, schema rules, the real database | the method |
| 8 | `application-service-author` | use-case and policy test lists, contract-checked fakes | the method |
| 9 | `inbound-adapter-author` | generation rules, wiring and edge test lists, error map, coverage gates | the method |
| 10 | `outbound-messaging-author` | publish decisions, mapping and relay test lists | the method |
| 11 | `read-model-author` | projection and query test lists, projector, paging | the method |
| 12 | `service-test-author` | real-adapter, acceptance and contract test lists, triage, intent coverage | the method |
| 13 | `fagan-code-inspector` (prerequisite — not part of every install; check at step 0 and use the step 13 fallback if missing) | the inspector's procedure, checklist, severity rules, defect and report format | — (reviews, never writes code) |
| 14 | `owasp-security-reviewer` | the safety boundary, both OWASP checklists, tool triage, finding format with abuse-case test specs | — (reviews, never writes code) |
| 6–12 ↻ | EXACT refactor agent (`refactor.md`) | an outside review of each finished slice against the Four Rules of Simple Design and the domain-boundary trial, after the TDD cycles' own Refactor phase | the agent definition |

Before a step, load its skill and hand it the step's inputs (the approved
artifacts, the ADRs, the autonomy level already set). For steps 13 and 14,
the orchestrator does not load the skill itself: it is the moderator, and
the separate agent it starts loads the skill. Its output must
satisfy the step contract in its step file; check that it does at the step checkpoint.
Each step skill also works standalone; inside the blueprint the orchestrator
owns the checkpoints and the ledger.

Defects a later step finds, and what to do when a step skill is missing, are
in each step file of steps 6–14 (*Defects and missing step skills*).

## Increments (steps 6–14)

Steps 6–14 never run over the whole context at once. They run once per
increment, in the order of the slice plan from step 1:

| Increment | Scope | Announced as |
|---|---|---|
| 0 | the walking-skeleton user story's thinnest slice (or two) | Increment 0 — Walking skeleton (US-nn) |
| 1…n | the slices of one design-level board not built yet, grouped by its user stories in story order | Increment k — Board: <board name> |

Before step 6 of every increment, the **readiness gate** runs (in
`references/step-06-domain-model.md`): the board's Example Mapping is ready
and current, board × user story is closed, deferred red cards are answered,
and rules are anchored. Inside an increment, work goes user story by user
story. After step 14, the **increment checkpoint** runs (in
`references/step-14-security-review.md`). The scope rules — growing units,
regression, additive migrations, the planned list, board order — are in
every step file of steps 6–14.

## Dependencies (what to re-check when a step is revisited)

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
| 2 Design brief | 3, 4, 6, 8, 11, 15; the slice plan, the user-story mapping and the skeleton (step 1.4c–1.4d) |
| 3 Invariants (incl. a new or changed design-level board) | 6, 8, 12, 15; a changed board also re-checks 2 |
| 3 A board's Example Mapping changed (rule, example or red-card answer — also at a readiness gate) | the board's readiness verdict, 3d, then 4 if an outcome needs a contract change, then 6, 8, 12 for that board's increment (already built: new failing tests there first), then 15; a New Story card or split goes to step 1.4c first |
| 4 Contracts (spec or API Product Canvas changed) | re-approval of the changed spec, then 6 (event payloads); 9 and 10 regenerate on the next build — re-run their wiring tests and fix the wiring test-first; then 11, 12, 15 |
| 5 Quality scenario added, changed or reprioritized | the ADRs that cite it (5d–5e), then its step 12 test and step 13 verdict in every increment already built, then 15 |
| 5 Decisions | 6–12 for the decisions that changed; an authentication, authorization or security-baseline decision also re-checks 4 (security schemes) and 14 for every increment already built |
| 6 Domain model | 7, 8, 12, 15 |
| 7 Persistence | 8, 10, 11, 12 |
| 8 Application services | 9, 12 |
| 9 Inbound adapters | 12, 15 |
| 10 Outbound messaging | 12, 15 |
| 11 Query side | 9, 12, 15 |
| 12 Tests (a defect or spec gap found) | the owning step (new failing test there) or step 4, then 12 again, then the scoped reverse check, then 13 and 14 for the changed units, then 15 |
| 13 Review (a defect accepted) | the owning step (new failing test there) or step 4, then 12 again, then the scoped reverse check, then the follow-up of 13 (re-inspection by a new agent when the defect was major), then 14 for the changed units |
| 14 Security review (a finding fixed) | the owning step (abuse-case test there, red first), step 4 for a spec gap or step 5 for a missing decision, then 12 again, then the scoped reverse check, then the re-test of 14 |
| Reverse check finds drift | *correct the document*: the artifact's owning step (1, 2, 3, 4 or 5) as a revision, then its usual re-checks; *defect*: the code's owning step (new failing test there), then 12 again |
| An increment's step 6–12 | the same step's later work in this increment, then steps 7–12 of every later increment already built that extend the same units, then steps 13 and 14 of each affected increment for the changed units |

Revisiting a step in a finished increment re-opens only that increment's
scope; a later increment that already extended the same unit is re-checked,
not redone.

## Tone

Plain language to the user; no skill names or file paths in the conversation
unless asked. Say "I'll extract the invariants from your board" rather than
naming the skill. Keep each step's exchange short: what is expected, the
choice, the result, the decision. Use American spelling.
