---
name: "blueprint-service-implementation"
description: "Guided, step-by-step pipeline that turns ONE bounded context's design-level EventStorming result, invariants, OpenAPI and AsyncAPI specs and ADRs into a service with API controllers, application services, domain model, repository and database. Starts by asking for a domain knowledge graph and an architecture decision graph (.ttl), then for the context's design-level boards, their slices and a walking skeleton (proposing them when missing); builds the walking skeleton end to end first, then implements one design-level board at a time, each increment closed by an independent review and an OWASP security review by separate agents. Asks at every step which input artifact to use. Use for \"implement this bounded context\", \"generate the service from our specs and board\", \"run the implementation blueprint\", \"continue the implementation at step N\", \"build the walking skeleton\", \"implement the next board\"."
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

- **Increment 0 — walking skeleton:** one slice (rarely two) taken through
  every layer, from the API to the real database and broker, before anything
  else is built.
- **Increment 1…n — one design-level EventStorming board each:** steps 6–14
  for the remaining slices of that board.

A **slice** is one *command → aggregate/system → event(s)* unit of a detail
board, with its trigger and read models, as `event-storming-detail-interpreter`
defines it. Step 15 runs once, after the last increment.

The skill is an **orchestrator**. It does little work itself: each step hands
off to a specialist skill and checks the result against the step's *contract*
below. Steps 6–12 each have their own **step skill** (see *Step skills*); the
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
     the architecture decision graph (see *Reading the graphs*). Only offered
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
- **Keep a ledger.** Maintain `implementation-log.md` in the working directory
  (format below). Update it when a decision is made, when output is accepted,
  and when a step completes.
- **Resuming.** If the user says "continue at step N", or an
  `implementation-log.md` already exists, read it, restate where things stand
  in three lines (including the current increment and its board), and
  continue from the first step without a recorded approval — starting with
  its checkpoint if its work is already done. "Implement the next board"
  resumes at step 6 of the next open increment.
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

Example: *"Step 3 — Invariants and acceptance scenarios is done: 14 rules
from the glossary, the design-level board and the Example Mapping; 11 with
examples, 2 not anchored, 3 open questions. Next is step 4 — Contracts:
OpenAPI + AsyncAPI, where I'll look for your specs. Do you approve step 3 —
Invariants and acceptance scenarios, so we can go ahead with step 4 —
Contracts: OpenAPI + AsyncAPI?"*

Step names to use, exactly:

| # | Name |
|---|------|
| 0 | Start |
| 1 | Scope: graphs and the bounded context |
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
skeleton"** or **"Step 7 — Persistence · Increment 2 — Board: Returns"**.
The checkpoint question names the increment the same way.

## Step 0 — Start

1. Create or read `implementation-log.md` (see *Resuming*).
2. Ask for the **output location** (a project folder on the user's computer or
   the working directory) and whether an existing codebase should be extended.
3. **Locate EXACT Coding** (see *EXACT Coding* below) and record where it
   comes from and at which commit.
4. **Delivery setup** (see *Delivery* below). Ask the questions one at a
   time, each with the default marked as recommended; the stack-specific
   files are written later, in the walking skeleton, once step 5 has chosen
   the stack.
5. Close with the step checkpoint, then go to step 1 — the two graphs are
   asked for there, before anything else.

### Delivery

Settled in step 0, before any code, so every increment is delivered the
same way. Record every answer in the ledger.

1. **Repository.** Is the output location a git repository? If not, offer
   to initialize one (with a `.gitignore` for the stack, added once step 5
   has chosen it). Name the default branch the work starts from.
2. **Branches.** Default: **one branch per increment**, cut from the
   default branch — `feature/<context>-inc0-walking-skeleton`,
   `feature/<context>-inc<k>-<board>`. The user may name their own model
   (trunk-based with short-lived branches, one branch per board, an
   existing naming rule). The steps 1–5 artifacts go on a
   `docs/<context>-design` branch, or on the first increment's branch if the
   user prefers.
3. **Commits.** Default: **Conventional Commits**, one commit per approved
   unit checkpoint (never per TDD phase — EXACT's method boundary), with
   - type: `feat` for new behavior, `fix` for a defect sent back, `test` for
     test-only changes, `refactor`, `build`, `ci`, `docs`, `chore`;
   - scope: the context or the aggregate (`feat(lending): …`,
     `feat(loan): …`);
   - footer: the traces (`Refs: B1-S03, INV-03, QS-02`) and the increment;
   - `!` and a `BREAKING CHANGE:` footer only for a spec change the user
     approved as breaking in step 4.
   The user picks when commits happen: **at each approved unit checkpoint**
   (recommended), **at each step checkpoint**, or **ask each time**. Every
   commit message is shown with the unit checkpoint; nothing is committed
   that the user has not approved.
4. **Remote and pull requests.** Is there a remote (GitHub, GitLab, other),
   and may the run push and open pull requests through `gh` or `glab`?
   Default: **one pull request per increment**, opened at the increment
   checkpoint against the default branch, merged by the user. Pushing and
   opening the pull request are shown and confirmed each time; the run never
   merges.
5. **Continuous integration.** Which CI provider (GitHub Actions, GitLab CI,
   another, or none)? The pipeline is written in the walking skeleton and
   runs the one build command on every push and pull request: generation,
   compile, lint and type check, all tests on real infrastructure, contract
   runs, the security tools from step 5, and the spec diffs. Long quality
   runs (load tests) may run on a schedule or on the pull request only; say
   which.
6. **Pre-commit hooks.** Which hook tool (pre-commit, Husky with
   lint-staged, Lefthook — or the stack's usual one)? Default hooks: format,
   lint, type check on changed files, secret scan, and a commit-message
   check for the chosen convention (for example commitlint). Hooks never run
   the whole suite — CI does. Hooks are written in the walking skeleton.
7. **Docs-as-Code.** Where the run's documents live in the repository —
   default `docs/<context>/`: the design brief, invariants sheet, slice plan,
   specs, ADRs, quality scenarios, `reviews/`, and the ledger. Offer to
   create or update the project memory file (`AGENTS.md`, or `CLAUDE.md` for
   Claude Code) with pointers to these documents and the conventions above.
8. **Definition of Done** for an increment — show the default and let the
   user add to it:
   - every test active and green, and the CI pipeline green on the pull
     request;
   - all gates and hooks pass; generated code untouched;
   - step 13 review and step 14 security review closed, reports saved in
     `reviews/`;
   - the documents updated to match the code: the scoped reverse check
     (step 15) clean, new or changed ADRs and quality scenarios saved, the
     specs' changelog updated, README run instructions current;
   - commits follow the convention; the pull request is open, with its
     description filled in;
   - the ledger updated.

If the user declines a part (no remote, no CI, no hooks), log it and say
what the run cannot check instead (for example "no CI: the Definition of
Done uses a local run of the build command").

### EXACT Coding

Steps 3 and 6–12 use **EXACT Coding** — Predictive TDD v1 by Ferdi Ade and
Marco Emrich (github.com/marcoemrich/EXACT-Coding-Exercises, MIT): the skills
`exact-coding`, `test-list`, `predictive-tdd` (with one profile per stack
under `predictive-tdd/stacks/`), `red`, `green`, `refactor`,
`example-mapping`, the checkpoint file
`exact-coding-shared/human-in-the-loop.md`, and the isolated **refactor
agent** `.claude/agents/refactor.md` (see *Slice refactoring*). Their files
refer to each other as `.claude/skills/...` and `.claude/agents/...` relative
to the project root. Locate them in this
order and use the first that works:

1. **Available skills** — `exact-coding`, `test-list` and `predictive-tdd` are
   listed; load them with the Skill tool.
2. **The project** — `<project>/.claude/skills/exact-coding/SKILL.md` exists.
3. **Vendor them into the project** — ask first; then clone the repository,
   check out commit `88fb3f9` (distribution 2026-09-21) or the commit the user
   names, and copy its `.claude/skills/` folder and its
   `.claude/agents/refactor.md` into the project, with its `LICENSE` as
   `.claude/skills/EXACT-LICENSE`. The agent always comes from the same
   commit as the skills.

The refactor agent is located the same way: available as the agent type
`refactor` in this session, or present as `<project>/.claude/agents/refactor.md`,
or vendored as above.

If none works, say so once, use the fallback in *TDD cycle*, and log that the
run was not built with EXACT Coding. Stacks: EXACT ships profiles for
TypeScript + Vitest and Java + JUnit 5 + Maven; for Kotlin, use the Java
profile adapted to the build, and say so.

## The steps

| # | Step | Kind | Expected input (any one source) | Skill(s) | Output |
|---|------|------|--------------------------------|----------|--------|
| 1 | Scope: graphs and the bounded context | mandatory | **first** a domain knowledge graph and an architecture decision graph (`.ttl`, may be one file); **if no knowledge graph:** at least one Domain Story and a Visual Glossary; **then** the context from the graph, the stories, a context map, or an EventStorming board with boundaries; **then** the context's design-level EventStorming boards, a slice plan and a walking skeleton (each from the graph, an upload, or a proposal) | `domain-knowledge-graph` (to build a graph now), `adr-and-principles-ingester` (to build the decision graph now); without a graph: `domain-story-interpreter`, `visual-glossary-interpreter`, `domain-story-glossary-consistency`, `domain-story-context-finder`; `pivotal-event-boundary-finder` or `event-storming-context-mapper` if boundaries are not settled; `event-storming-detail-interpreter` (slice tables of the boards) | graph inventory, the context name, its neighbors and border contracts, the decisions and principles in scope, the board list, the slice plan (slice IDs per board, board order) and the walking skeleton |
| 2 | Design brief | mandatory | every design-level EventStorming board of the context from step 1 (photo, Miro export, sticky list, or graph board nodes); Event Model as alternative | `event-storming-detail-interpreter` (one Detail Design Brief per board); `event-storming-interpreter` only for a big-picture board used as preparation; for an Event Model, read slices directly | per board: aggregates + state machines, slice table, policies, read models, external systems, hotspots, completeness findings; merged into one context brief keyed by slice ID |
| 3 | Invariants and acceptance scenarios | mandatory | **first collected** from what exists: graph rules and glossary cardinalities, the Visual Glossary; **then always** a design-level EventStorming of the chosen context (reuse from step 2, graph, or upload); **then always** an Example Mapping, unless the graph already holds one | collect: `visual-glossary-interpreter`; board: `event-storming-invariant-finder` (`event-model-invariant-finder` for an Event Model stand-in); Example Mapping: read an uploaded map with `example-mapping-interpreter`, or run a session with EXACT `example-mapping` | invariants sheet with IDs, source, board and slice per rule, Given/When/Then per rule (examples from the map), cross-context rules marked as policies, open questions from red cards |
| 4 | Contracts: OpenAPI + AsyncAPI | mandatory | specs from the graph; if missing, `openapi.yaml` / `asyncapi.yaml` uploads, **or** an API Product Canvas; if none, a proposal derived from steps 1–3 | `openapi-spec-author`, `asyncapi-spec-author` (validate, lint, quality review, author); `context-map-api-proposer` (sync vs async); `domain-knowledge-graph` (contract coverage); `schema-glossary-consistency` if a glossary exists | both needed specs linted and **approved by the user** + reconciliation table |
| 5 | Quality requirements and architecture decisions | mandatory | **first** an Architecture Knowledge Graph (`.ttl`: quality goals, quality scenarios, constraints; may be the decision graph from step 1); **if none, or where it leaves questions open:** the user's answers to the critical quality questions; **then** the architecture decision graph from step 1, an ADR log, and the user's answers, question by question | `architecture-question-finder` (formulate the questions), `adr-and-principles-ingester` (check against principles) | quality scenarios `QS-nn` (six-part form, priority, how each is verified); accepted ADRs for answered questions, each naming the scenarios it serves; skipped questions logged with what they block |
| 6–14 | *run once per increment* | — | the increment's slices only (walking skeleton, then one board at a time); see *Increments* | as below | as below, for the increment's slices |
| 6 | Domain model | mandatory | step 2 brief + step 3 invariants + step 4 schemas; optional Visual Glossary (picture, brief, or graph terms); per aggregate: the approved test list | method: EXACT `exact-coding` (`test-list`, `predictive-tdd`, stack profile); content: `aggregate-behavior-author`; naming: `glossary-domain-model-author` | per aggregate: approved inactive test list, domain code grown one predicted cycle at a time, all gates green |
| 7 | Persistence | mandatory | step 6 model + step 5 persistence ADR + invariants (uniqueness) | `persistence-adapter-author` + EXACT; a real database (Testcontainers or a local server) | repository ports with their errors, adapters, migrations, outbox with delivery columns, unit of work where a use case needs one |
| 8 | Application services | mandatory | steps 3, 5, 6, 7 | `application-service-author` + EXACT; fakes that pass the shared repository contract | one use case per command, one handler per policy, idempotent |
| 9 | Inbound adapters | mandatory | approved step 4 specs + step 5 generator ADR + step 8 use cases | **generated**: pinned OpenAPI and AsyncAPI generators; **wiring**: `inbound-adapter-author` + EXACT | generated contract; tested wiring, error map (Problem Details), runtime spec validation, coverage gates |
| 10 | Outbound messaging | optional (mandatory if the AsyncAPI spec publishes) | step 4 AsyncAPI + step 6 events + step 7 outbox | `outbound-messaging-author` + EXACT; generated message models; fake broker behind the publisher port | one publish decision per event, mappers, ordered relay with retry and dead letter |
| 11 | Query side | optional (mandatory if step 2 has read models or the spec has GET operations) | step 2 read models + step 4 GET operations + events | `read-model-author` + EXACT | projections (no gaps, one runner, rebuildable), query port, GET operations with paging |
| 12 | Tests | mandatory | step 3 Example Mapping, step 4 specs, steps 6–11 code, step 5 test-infrastructure ADR | `service-test-author` + EXACT; Cucumber, Schemathesis, a real broker | real broker adapters, composition root, acceptance and contract runs, whole suite, intent-coverage report |
| 13 | Independent review | mandatory | the increment's code and tests (diff against the previous increment; the whole code in increment 0), plus the approved artifacts they implement: brief, invariants sheet, specs, ADRs, principles, slice plan — **not** the conversation or the author's reasoning | a separate review agent in a fresh context (`Agent` tool), a different model where one is available; Fagan inspection run by the orchestrator as moderator | inspection report: defects with severity, location and the artifact element they violate; rework sent back to the owning steps; follow-up result |
| 14 | Security review (OWASP) | mandatory | the increment's code, the running service from step 12, the approved specs (security schemes, scopes), the step 5 auth and security ADRs, dependency manifests | a separate security agent in a fresh context (`Agent` tool), never the author; OWASP Top 10 and OWASP API Security Top 10 as the checklist; dependency audit, secret scan and static analysis tools for the stack | security report: findings by OWASP category and severity, with evidence; abuse-case tests for every accepted finding, sent back to the owning steps; accepted risks with an ADR |
| 15 | Traceability check | mandatory | everything above, starting from step 12's intent-coverage report; graph if present | `domain-knowledge-graph` (ingest specs and code map); `arazzo-from-event-model` for journeys if an Event Model exists | traceability matrix, reverse-check drift table (names, columns, enum values, states), closing report |

### Step skills (steps 6–12)

| Step | Step skill | It supplies | EXACT supplies |
|---|---|---|---|
| 6 | `aggregate-behavior-author` | the aggregate's test-list coverage and target shape | the method |
| 7 | `persistence-adapter-author` | repository and outbox test lists, schema rules, the real database | the method |
| 8 | `application-service-author` | use-case and policy test lists, contract-checked fakes | the method |
| 9 | `inbound-adapter-author` | generation rules, wiring and edge test lists, error map, coverage gates | the method |
| 10 | `outbound-messaging-author` | publish decisions, mapping and relay test lists | the method |
| 11 | `read-model-author` | projection and query test lists, projector, paging | the method |
| 12 | `service-test-author` | real-adapter, acceptance and contract test lists, triage, intent coverage | the method |

Before a step, load its skill and hand it the step's inputs (the approved
artifacts, the ADRs, the autonomy level already set). Its output must
satisfy the step contract below; check that it does at the step checkpoint.
Each step skill also works standalone; inside the blueprint the orchestrator
owns the checkpoints and the ledger.

- **A defect a later step finds** (steps 12 and 13 above all) goes **back to the
  step that owns the code** — as a new test there, red first — or, for a
  spec, back to step 4. The finding step records it; the owning step's
  ledger row gets a revision. Never fixed silently where it was found.
  After the fix, when step 12 is green again, run the scoped **reverse
  check** (see step 15) before the increment checkpoint. A defect from the
  step 13 review or a step 14 security finding takes the same path.
- **Step skill not available** (another workspace, an older install) → say
  so in one sentence and offer:
  1. **Implement directly** — follow the step contract below yourself, with
     the *TDD cycle*.
  2. **External generator** — only where the stack fits (see *External
     generators*); run it, then review the output against the contract.
  3. **Defer** — log the step as deferred with its open inputs; later steps
     that depend on it must say what they cannot verify.

Record in the ledger which route was taken, so the user can later replace a
direct implementation with a new skill and re-run the step.

### TDD cycle (steps 6–12) — EXACT Coding

All hand-written code in steps 6–12 is written with **EXACT Coding** (see
*EXACT Coding* under step 0): its Test List phase, then one-behavior
Predictive TDD cycles. A step skill does not change that — step skills
supply the **content** of a unit's test list and the target shape of its
code; EXACT Coding supplies the **method**. Its rules are binding as written,
and nothing in this skill relaxes them.

**Per unit of work, within the current increment** (one aggregate in step 6; one adapter, use-case group,
wiring unit, publisher or projection in steps 7–11; one acceptance or
contract suite in step 12):

1. **Test list — EXACT `test-list`.** One test file per unit with every
   behavior as an **inactive test** (`it.todo` in Vitest, `@Disabled` in JUnit
   5), grouped by slice and ordered simple → complex within each group (see
   *Slice order inside a step*), expected values in each description, and the
   trace at the end after `--`, starting with the slice ID
   (`-- B1-S03 INV-03 ex.2`, `-- B1-S03 POST /loans 409`,
   `-- ADR-007`). The step's contract below says what the list must cover;
   EXACT's coverage cross-check treats those elements as the independent
   dimensions. Run the suite: the inactive list must leave it green. Apply
   the Test-List checkpoint; the approved list is the scope.
2. **Cycles — EXACT `predictive-tdd`**, with the project's stack profile: one
   behavior at a time; a falsifiable prediction before every check; behavioral
   Red for the predicted reason; minimal Green; Refactor under the Four Rules
   of Simple Design with the domain-responsibility review and the boundary
   trial; close with the full suite and the profile's gates (type check, lint,
   PMD where configured).
3. **Checkpoints — EXACT `human-in-the-loop.md`.** Ask once, at the first
   unit, which autonomy level to use (default `full-hitl`: stops after Test
   List, Red, Refactor and every prediction mismatch; others: `refactor-only`,
   `red-only`, `every-n-tests N`, `task-end`, `autonomous`). Set it in the
   project copy of that file and log it; the user can change it at any unit
   checkpoint. The single-phase controls `red`, `green` and `refactor` are
   available when the user wants to drive one phase at a time.
4. **No phase commits and no hard resets** (EXACT's method boundary). Work is
   kept in the working tree during the cycles. Commits follow the policy
   from step 0 (*Delivery*): by default one Conventional Commit per approved
   unit checkpoint, on the increment's branch, with the message shown at the
   checkpoint. The pre-commit hooks run on every commit; a failing hook is
   fixed under green tests, never bypassed (`--no-verify` only with the
   user's explicit consent, logged).
5. **Unit checkpoint.** When every test in the file is active and all gates
   pass, show the unit as a whole — code, tests (active/total), prediction
   mismatches and how they were explained, boundary trials retained or
   undone, tests added on the way, open questions, and the proposed commit
   message (step 0 policy), and the slice-refactoring reports of the unit —
   and ask for approval before the next unit. The step checkpoint follows
   after the last unit.

**Slice order inside a step.** Each unit's test list is grouped by slice, in
slice-plan order, and ordered simple → complex inside each group. A slice is
**implemented in a step** when the last of its tests in that step's units is
active and green; that moment is the slice's **refactoring point**.

### Slice refactoring — EXACT refactor agent

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
- the slice's specification: its rows of the design brief, its invariants
  with their Given/When/Then scenarios, its spec operations and messages, the
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
2. Predict, then run the whole suite and the profile's gates again, and
   check the test diff: only mechanical changes. A red suite, a gate
   failure or a changed assertion undoes the agent's changes with the
   reverse patch, and is logged.
3. Show the result at the autonomy level from EXACT's
   `human-in-the-loop.md`: with `full-hitl` or `refactor-only`, stop and ask
   **keep / undo / keep part**; otherwise it is shown at the unit
   checkpoint. Proposals become open questions with the step they would go
   back to.
4. Retained changes are committed separately, as
   `refactor(<scope>): … Refs: B1-S03` under the step 0 policy, after the
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

**Exception: code generated from the approved specs.** Controller
interfaces, consumer/listener interfaces, DTOs and message classes that a
pinned, reliable generator produces from the step 4 specs (step 9, and the
message classes in step 10) are **not** written test-first and **not**
edited by hand. They are trusted because the specs were linted and approved
and the generator is deterministic; they are checked by compiling and by the
step 12 contract tests. The hand-written code around them — mapping and
wiring — goes through EXACT Coding like everything else. Any other generator
output (scaffolding, CRUD layers, entities) gets no exception: it is kept only
for behaviors whose test was activated first and failed.

**Fallback** (only when EXACT Coding cannot be located): the same inactive
test list; per behavior predict, Red for the predicted reason, least code to
Green with the whole suite, refactor under the Four Rules; show and ask before
the next behavior. No production code without a failing test, never change a
test to make it pass without asking, never report a result that was not run.

## Increments (steps 6–14)

Steps 6–14 never run over the whole context at once. They run once per
increment, in the order of the slice plan from step 1:

| Increment | Scope | Announced as |
|---|---|---|
| 0 | the walking-skeleton slice (or two) | Increment 0 — Walking skeleton |
| 1…n | the slices of one design-level board not built yet | Increment k — Board: <board name> |

**Start of an increment.** Before step 6, create the increment's branch
as step 0 decided (`feature/<context>-inc<k>-<board>`), cut from the
default branch with the previous increment merged. If the previous pull
request is not merged yet, ask: wait, or stack the new branch on the
previous one (and say that a change in review then has to be carried
forward).

**Scope inside an increment.** Each step applies its contract to the
increment's slices only: the commands, aggregates, invariants, events,
policies, read models, operations and channels those slices own (the slice
plan and the step 4 reconciliation table say which). Everything else stays
out of the increment.

- **Growing units.** A unit that exists from an earlier increment (an
  aggregate, a repository, a use-case group, a wiring unit, a projection) is
  extended, not rewritten: the new behaviors are appended to its test file as
  inactive tests, the additions pass EXACT's Test-List checkpoint, then
  predicted cycles run as usual. An approved test from an earlier increment
  is never edited to make a new behavior fit; a conflict is shown and asked
  about.
- **Regression.** Every gate runs the whole suite, so earlier increments stay
  green. An earlier test that turns red is a defect of the current increment,
  unless the user decides the earlier behavior changes — then it goes back to
  step 3 or 4 as a revision.
- **Migrations are additive.** A later increment adds migrations; it never
  edits an applied one.
- **Planned, not missing.** The coverage gates of step 9 (every operation and
  receive operation wired), step 10 (every domain event has a publish
  decision) and step 12 (intent coverage) check against the slices built so
  far. Operations, channels, events, rules and quality scenarios of later
  boards go on one
  **planned list**, each with the increment that will build it
  (`planned: increment 3 — Board: Returns`). A gate fails on anything that is
  neither built nor planned. The list shrinks with every increment and must
  be empty after the last one.
- **Order.** Boards follow the slice plan. A board whose policies or read
  models need events of another board comes after that board. A cycle
  between boards is an open question: the user picks the order, and the
  events the first board needs from the later one are supplied as test data
  until then, marked in the ledger.

### Increment 0 — Walking skeleton

The skeleton is the thinnest slice that runs end to end through every layer
the service will have, on real infrastructure. It proves the architecture,
the build, the generators and the test infrastructure before the domain
grows; it does not aim at features. Steps 6–14 run with their full contracts
and EXACT Coding, scoped to the skeleton:

- **Step 6:** the skeleton's aggregate with only the skeleton command, its
  guards and its event(s).
- **Step 7:** the first migration (with the complete outbox, if the ADR chose
  one) and the repository, against the real database.
- **Step 8:** the skeleton's use case or policy handler.
- **Step 9:** the generator pipeline for both specs, run from the build; the
  skeleton's operation or channel wired; the edge unit (404/405/415/400).
  Every other operation and channel is on the planned list.
- **Step 10:** if the skeleton event is published, its mapper and the relay;
  otherwise every published channel is planned and step 10 is skipped for
  this increment.
- **Step 11:** if the skeleton slice has a read model or GET operation, its
  projection and query; otherwise skipped for this increment.
- **Step 12:** the composition root, the real database and broker adapters
  the skeleton touches, one acceptance scenario per example of the skeleton's
  rules, the contract run limited to the skeleton's operations and messages,
  and the whole build from one command. It also writes the delivery setup
  from step 0 for the chosen stack: the **CI pipeline** running that command
  on every push and pull request, the **pre-commit hooks** (format, lint,
  type check, secret scan, commit-message check), and the `docs/<context>/`
  folder with the steps 1–5 artifacts. The skeleton is done only when the
  pipeline is green on its pull request. It also sets up the **quality-test harness** — the load
  tool, fault injection on the test containers, the spec diff, the
  architecture-rule tests — and runs every testable quality scenario the
  skeleton's slice can show (typically latency of one command, recovery after
  a broker outage, correlation IDs, module boundaries).
- **Step 13:** an independent review of the whole skeleton — its code,
  tests, build and generator setup — since every later increment builds on
  it.
- **Step 14:** the security review of the skeleton, with the authentication
  and authorization mechanism from the step 5 ADR in place on the skeleton's
  operation — the skeleton proves auth end to end, and every later operation
  reuses it. The baseline checks (dependency audit, secret scan, static
  analysis, security headers) are wired into the build here.

Done when the skeleton's acceptance scenarios pass against the running
service on real infrastructure, built from one command, the independent
review has no open major defects, and the security review has no open
high or critical finding.

**Skeleton review**, before the increment checkpoint: show which layers and
step 5 decisions the skeleton proved, which layers it did not reach (for
example no consumer, because the skeleton has no policy) and which increment
will reach them, and every decision the skeleton contradicted or made
doubtful. Offer **go back to step 5** for each; the dependency table says
what to re-check.

### Increment checkpoint

After step 14 of every increment, before the step checkpoint that leads on:

1. **Name the increment that ends** and summarize it: slices built (IDs),
   units added or extended, tests active/total, slice refactorings (retained,
   undone, proposals), what is left on the planned
   list, defects sent back, reverse-check results after fixes (checked,
   drifted, resolved), the review result (defects found by severity,
   reworked, rejected with reason, deferred), the security result (findings
   by OWASP category and severity, fixed, accepted as risk with ADR), open
   questions.
2. **Definition of Done.** Check every item of the Definition of Done from
   step 0 and show each as met or not met, with the evidence. Bring the
   documents in `docs/<context>/` up to date first: the reports, new or
   changed ADRs and quality scenarios, the specs' changelog, the README run
   instructions, and the project memory file — committed as `docs` commits.
   An item not met blocks the approval unless the user waives it (logged).
3. **Pull request.** Show the push and the pull request for confirmation:
   title as a Conventional Commit summary (`feat(lending): board Returns —
   increment 2`); description with the slices built, the Definition of Done
   checklist, the review, security and quality summaries, links to the
   reports in `reviews/`, the open questions, and the planned list. After
   confirmation, push and open it, wait for the CI result, and show it. A red
   pipeline is a defect of this increment. The user merges.
4. **Show the remaining boards** in order, with their slice counts.
5. **Ask for approval:** **"Do you approve increment k — <name>, so we can go
   ahead with increment k+1 — <name>?"** Offer: **Approve and continue** /
   **Reorder the remaining boards** / **Revise this increment** / **Go back to
   an earlier step** / **Pause here**. After the last increment, the next
   step is step 15.
6. **Record** the approval, the pull request link, the CI result and the
   date in the ledger.

## Step notes

### Step 1 — Scope
Ask the four questions below in this order, one at a time.

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
     actor → activity → work object sentences). Ask "any more stories?"
     after each one; several are welcome.
   - **A Visual Glossary** (picture, transcription, or a Glossary Brief).

   For each, offer **Upload** / **Generate a draft** / **Stop here**:
   - *Upload* → interpret it: `domain-story-interpreter` for stories,
     `visual-glossary-interpreter` for the glossary.
   - *Generate a draft* → stories with `domain-story-seeder`; a glossary
     derived from the stories' work objects, with relationships and
     cardinalities asked as questions. Say plainly that a generated story or
     glossary is a strawman to correct with domain experts, mark it
     `Inferred` in the ledger, and let the user edit it before accepting.
   - *Stop here* → explain that without a graph, a story and a glossary,
     there is nothing to anchor the bounded context, the ubiquitous language
     or the domain model to; end the run and log what is missing.

   Once both are accepted, run `domain-story-glossary-consistency` on them
   and show the findings (terms the stories use but the glossary lacks,
   unused glossary terms, contradicted cardinalities). Offer to fix them now
   or log them as open questions. Then offer to **build a knowledge graph
   from them** with `domain-knowledge-graph` (recommended: later steps can
   then use **Graph** as a source and step 15 can ingest the result). If the
   user declines, carry the stories and the glossary forward as files.
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
   - **Without a graph:** work from the stories and the glossary. If the
     stories already carry labeled lanes or groups, or the glossary is
     colored by context, offer those as the options. Otherwise run
     `domain-story-context-finder` on the stories, show the proposed contexts
     with their evidence and contested sentences, and offer them as the
     options. The user may also upload a context map or a board with
     boundaries instead (then use `event-storming-context-mapper` or
     `pivotal-event-boundary-finder` if the boundaries are not settled).
     Then ask the question on that result.
   - **One context per run.** If the user names several, ask which one goes
     first and log the others in the ledger as later runs.
   - Repeat the chosen name back exactly as the graph or map spells it, and
     use that spelling in every later step.
4. **Design-level boards, slice plan and walking skeleton.** Ask the three
   parts below one at a time, after the context is chosen. Each is **asked
   first**; only when the user has none is a **proposal** made, shown and
   accepted like any other artifact (accept, revise, or replace).

   a. **Boards.** "Which design-level EventStorming boards exist for
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
   b. **Slice plan.** "Do you already have a slice plan for <context> —
      which slices belong to which board, and in which order the boards
      should be built?" Offer: **From the graph** (query *Slices in the
      graph*) / **Upload** (a list, a sliced Event Model, a backlog) /
      **Propose one**.
      - *Propose one* → one slice per row of each board's slice table, with
        IDs `B1-S01, B1-S02, …` in board order, and per slice: trigger,
        command, aggregate or system, events, read models consulted and
        updated, and the slices it depends on (a policy or read model that
        needs another slice's event). Then a board order: dependencies
        first, then the core process or the board with the most commands;
        the user may change it. A slice that appears on two boards (same
        command and aggregate) is merged, and the merge is shown. Never add
        a slice the boards do not show; a command without an aggregate
        becomes a slice with an open question.
      - An uploaded or graph plan is checked against the boards' slice
        tables. Plan slices the boards do not show, and board slices the plan
        lacks, are shown as findings; the user decides each one.
   c. **Walking skeleton.** "Is a walking skeleton defined — the one slice
      we build first, end to end through every layer?" Offer: **Name the
      slice** / **Propose one**.
      - *Propose one* → rank the slices and show the best one with up to two
        alternatives. Prefer a slice that is triggered by an actor through an
        API operation; changes one aggregate with at least one real guard
        (not plain CRUD); emits an event the context publishes and feeds a
        read model behind a GET, when the context has those; depends on no
        other slice and no external system; and sits early on the timeline.
        For each candidate, say which layers it exercises (HTTP in, domain,
        database, outbox and broker out, broker in, read model) and which it
        leaves out. If no single slice reaches a layer that matters
        (typically consuming a message), offer one more slice for it — two
        at most.
      - A named slice is checked the same way: say which layers it leaves
        out, then accept it.
   - **No design-level board at all** → the slice plan and the skeleton
     cannot be grounded yet. Log both as *pending*, say that the proposal
     will be made at the end of step 2 from the design brief (an Event
     Model's state-change slices also count), and continue. If the brief
     still has no commands and aggregates, the whole context becomes one
     increment, and the skeleton is the brief's first command, marked
     inferred.

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
- Without a knowledge graph, keep the accepted stories and glossary as the
  run's reference: step 2 can seed events from the stories
  (`domain-story-event-seeder`) as preparation for a board, step 6 takes the
  glossary for structure, and step 15 checks the code's names against the
  glossary. Without a decision graph, step 5 asks for an ADR log or builds
  the decisions from scratch.
- Done when: both graph questions are answered (file or "not available");
  if there is no knowledge graph, at least one Domain Story and a Visual
  Glossary are accepted and checked against each other; the user has
  explicitly chosen (or confirmed) the one bounded context to implement; its
  borders are listed; the in-scope decisions and principles are listed; the
  design-level boards are listed (or "not available"); and the slice plan and
  the walking skeleton are accepted, or logged as *pending* until step 2.

**Principles as guardrails.** From here on, every testable principle in scope
is a constraint on steps 6–12. After each of those steps, check its output
against them and report each one as honored, overridden (with the ADR that
allows it) or violated. A violation is fixed or logged, never ignored.

### Step 2 — Design brief
- The board must come from people. If the user has only domain stories or a
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
  the slice plan, the board order and the skeleton, and offer to adjust.
  If both were *pending* from step 1, make the proposals now (step 1, 4b–4c)
  from the brief.
- Done when: every command has a target aggregate and a resulting event, or is
  listed as an open question; every slice of the plan maps to brief rows; and
  the slice plan and walking skeleton are accepted.

### Step 3 — Invariants
Most invariants already exist before this step asks for anything. Collect
them first, then ask for the design-level EventStorming of the context, then
for the Example Mapping, then merge. The two questions are independent: an
available Example Mapping never replaces the EventStorming question, and the
other way round.

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
    still current and complete; a board added now also gets slices (step 1,
    4b) and its own increment.
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

**3c. Ask for the Example Mapping — always.** Even when 3a found many rules,
ask for the Example Mapping of the stories in this context, because it is the
only source of **concrete examples** and of the questions nobody could
answer. Skip the question only when the graph already holds an Example
Mapping for this context (query *Example Mapping in the graph*); then say so
and use it. Ask it after 3b, whatever 3b's answer was. Offer:
- **Upload** (photo, Miro/Mural screenshot, typed list, or the Markdown file
  of an earlier EXACT session) → read it with `example-mapping-interpreter`:
  story, rules, examples, questions, readiness verdict.
- **Run an Example Mapping session now** → load EXACT Coding's
  `example-mapping` and follow it as written: a 1:1 interview in which the
  user is the domain expert. Seed it with the story and the rules from 3a and
  3b, but **ask** for every rule and example — EXACT's facilitator rules
  forbid inventing them. Questions the user cannot answer become red cards;
  behavior outside the context becomes a New Story card (log it for a later
  run). Save the result where EXACT puts it (`src/<feature>-example-mapping.md`
  or the path the user names) and show its health indicators.
- **Not available** → continue with the 3a and 3b rules, but mark every rule
  without an example as **"no example — acceptance test will be derived, not
  agreed"**, and log the missing Example Mapping as an open question.

**3d. Merge into one invariants sheet.**
- Match each blue rule card to a collected rule (same meaning, not just same
  words); new rules from the map are added with source *example mapping*.
- Each green example becomes a Given/When/Then scenario under its rule.
- Each red question becomes an open question attached to its rule.
- Rules with no example, and examples whose rule is missing, are listed.
- Every rule gets a stable ID (`INV-01` …), its sources, one aggregate, one
  command and the slice and board they belong to (all taken from the board),
  the Given/When/Then scenarios, the rejection it causes, and its **kind**:
  *quantitative*, *cardinality* or *other*. Quantitative and cardinality rules
  get a property test in step 6. The sheet is
  grouped by board, so each increment takes its rules from its own group. A rule the board does not anchor keeps the "not
  anchored" mark.
- Rules spanning two aggregates or contexts become policies with a staleness
  window and a compensating action — they are implemented in steps 8–9, never
  inside one aggregate.
- Done when: the design-level EventStorming question and the Example Mapping
  question were both asked and answered; every command from the board has
  its guards listed (possibly none); every rule has at least one scenario or
  is marked "no example"; and every rule is anchored to a command and an
  aggregate or is marked "not anchored".

### Step 4 — Contracts and reconciliation
The context needs an **OpenAPI** spec (synchronous operations) and an
**AsyncAPI** spec (events published and consumed). Handle the two specs
separately but in the same way; a spec the context does not need (for
example no events cross its borders) is marked *not needed* only when the
user confirms it.

**4a. Look in the graph first.** Run *API specs in the graph*. If the graph
holds a spec for this context, say which one and treat it as **provided**
(go to 4c). Nothing is taken from the graph without showing it.

**4b. Ask for what is missing.** For each spec the graph does not hold, ask:
"Do you have an OpenAPI spec / AsyncAPI spec for <context>?" Offer:
- **Upload the spec** (`openapi.yaml`, `asyncapi.yaml`, JSON, or a URL the
  user provides) → go to 4c.
- **Upload an API Product Canvas instead** (picture, export or text) → read
  every field as written, list what it says about consumers, value,
  capabilities or use cases, data, events and quality goals, and show
  anything the canvas leaves open. Then go to 4d and derive the specs from the
  canvas together with steps 2–3. Never fill a field the canvas leaves empty;
  ask.
- **Neither** → go to 4d and propose the specs from steps 2–3 alone.

**4c. A spec was provided: check, lint, propose improvements.**
1. Load `openapi-spec-author` or `asyncapi-spec-author` in **validate /
   lint** mode: validate the spec against its schema and run its Spectral
   ruleset. Report every error and warning with its location.
2. Run that skill's **quality review** (its quality-requirements checklist:
   backward compatibility, additive evolution, capability-based segregation,
   links, one bounded context per API), plus the reconciliation table below.
3. Present the **proposed improvements** as a numbered list, each with the
   finding it fixes, the change, and whether it breaks existing consumers.
   Breaking changes are flagged and never applied without explicit consent.
4. The user accepts, rejects or amends each proposal and may add proposals of
   their own. Apply only what was accepted, re-lint, and show the diff.
5. Repeat 3–4 until the user **approves** the spec.

**4d. No spec was provided: propose one.**
1. Load `openapi-spec-author` and/or `asyncapi-spec-author` and derive the
   spec from the step 2 brief, the step 3 invariants, the step 1 borders and,
   when given, the API Product Canvas. If it is unclear which interactions
   are synchronous and which asynchronous, run `context-map-api-proposer`
   first and show its sync/async classification of each border crossing for
   the user to confirm.
2. Lint the draft and run the quality review before showing it — a proposal
   is never shown with known lint errors.
3. Show the proposal: what each operation, channel and message is for, which
   board element it traces to, and anything you had to assume (listed as open
   questions, not hidden in the YAML).
4. The user **approves** it or **proposes improvements**. Apply each proposal,
   say if it conflicts with a quality requirement or with the board, re-lint,
   and show the diff. Repeat until approved.
5. A generated spec is the user's spec only after approval; until then it is
   a draft.

**4e. Gate.** Show the step checkpoint only when **every needed spec is
approved**: the
OpenAPI spec approved (or confirmed *not needed*) **and** the AsyncAPI spec
approved (or confirmed *not needed*). Record each approval, its date, and the
accepted and rejected proposals in the ledger. If a knowledge graph is in
use, offer to ingest the approved specs with `domain-knowledge-graph`.

**Reconciliation table** (part of 4c and 4d):

  | Slice | Board element | Expected contract | Found | Status |
  |---|---|---|---|---|
  | B1-S03 | command | OpenAPI operation or consumed AsyncAPI message | … | ok / missing / name drift |
  | B1-S03 | published event | AsyncAPI channel + message | … | … |
  | B1-S05 | read model | GET operation | … | … |
  | B2-S01 | policy reacting to another context | subscribed AsyncAPI channel | … | … |

- A "missing" or "name drift" row becomes an improvement proposal in 4c or a
  required element in 4d.
- The **Slice** column decides which increment builds each operation,
  channel and message. A spec element no slice claims is shown as a finding:
  assign it to a slice or remove it from the spec.
- The specs cover the whole context and are approved once, here. Later
  increments implement them; they do not reopen them unless a defect or gap
  sends the run back to step 4.
- Done when: every needed spec is linted without errors and approved by the
  user, and the table has no "missing" rows the user has not explicitly
  accepted.

### Step 5 — Quality requirements and architecture decisions
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
**Other**, and **Skip for now**. The critical areas, in this order:

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
- test infrastructure: how the database and the **broker** run in tests
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

**5e. Ask question by question.** Ask exactly one question at a time, in the
list's order, naming its number ("Question 3 of 9 — …"). Offer (option picker
when available): the options, **Other** (the user's own answer), and **Skip
for now**.
- **Answered** → draft the ADR (context, decision, consequences, the
  principles it honors or overrides — an override is stated, never
  implied — and the quality scenarios it serves or trades off), show it, and ask to accept or revise it before the next question.
- **Skip for now** → log the question as *deferred*, with the later steps it
  blocks, and move to the next question without arguing. Never re-ask a
  skipped question within the same pass.
- The user may stop the round at any point ("enough for now"); the remaining
  questions count as skipped.
- After the last question, show the summary: decided (with ADR IDs),
  skipped (with what each blocks), and the skipped ones that block step 6
  or 7. Offer **one** more pass over the skipped questions; accept "no".

**5f. Skipped questions later.** A skipped question that a later step needs
is asked again **at that step**, in the same form, before the step's work
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

### Step 6 — Domain model (contract)
Inputs: brief, invariants, specs, ADRs, optional glossary — limited to the
current increment's slices. Built **one aggregate at a time** with the *TDD
cycle*. In a later increment, an aggregate that exists already is extended
(see *Increments*).

1. **Order the aggregates** touched by the increment's slices. Propose an
   order (the aggregate with the most of the increment's commands first, or
   the one the user names) and let the user change it.
   Before starting, re-ask any skipped step 5 question that the domain model
   needs (language, ID strategy, time source) — see 5f.
2. **Naming guide, not code.** If a Visual Glossary is available, run
   `glossary-domain-model-author` for its entity vs value object decisions
   and field names, and use them as the naming guide. Types are created only
   when a test needs them; nothing is generated ahead of the tests.
   Without a glossary, take names from the board and the AsyncAPI/OpenAPI
   schemas.
3. **Per aggregate:**
   a. **Test list** for this aggregate (TDD cycle 1, EXACT `test-list`).
      It must cover, for the increment's slices, every command on the
      aggregate, every invariant anchored to it (one item per
      Given/When/Then scenario, plus one per rejection), every allowed and
      every forbidden state transition, and every event it raises with its
      payload. Rules marked "not anchored" in step 3 are asked about now: to
      which command and aggregate do they belong? Unanswered ones stay off
      the list and stay open questions.
      It must also hold **one property test per quantitative or
      cardinality invariant** anchored to the aggregate (see *Property
      tests* below), placed after that rule's example tests.
   a2. **Property tests.** Example tests check a rule as written; they miss
      a calculation that only counts when it should weigh, as long as every
      example input happens to be equal.
      - **Which rules.** *Quantitative*: the rule constrains a number derived
        from inputs (a sum, limit, fee, balance, duration, deadline, rate).
        *Cardinality*: the rule bounds how many of something may exist or
        be held (glossary multiplicities like `1..5`, "at most three open
        loans", "exactly one primary address"). Step 3 marks the kind; a rule
        it did not mark is classified now and the user confirms.
      - **One item per such rule**, stated over all valid inputs, with the
        trace ending in `property`:
        `it.todo("open loans never exceed 5 for any sequence of Borrow and Return; the 6th Borrow is rejected with MaxLoansReached -- B1-S03 INV-03 property")`.
      - **State the relation the rule rests on**, not the examples:
        quantitative rules over generated values across the whole valid
        range, including boundaries and inputs that differ from each other;
        cardinality rules over generated **command sequences** (stateful,
        checked after every step against the bound, or against a small model
        of the aggregate).
      - **Ask the property question** before approving the list: which of
        these properties would still fail if a later change altered the
        units, the granularity or the cardinality of an input? A property
        that would not is rewritten until it would.
      - **Method.** The item goes through Predictive TDD like any other: the
        prediction says whether it will fail and why. If it passes on
        activation without new code, follow EXACT's rule for a test that
        passes at once. If it fails, that is a gap the examples missed: keep
        the shrunk counterexample as a plain example test, make it green, and
        log the case as a candidate example for the Example Mapping — an open
        question for step 3 when it changes the rule.
      - **Reproducible.** The library, run count and fixed seed come from the
        step 5 ADR; the seed is logged. Properties stay pure unit tests: no
        database, no HTTP, and time only through the ADR's clock.
   b. **Predictive TDD cycles**, one behavior at a time (TDD cycle 2–4):
      `aggregate-behavior-author` supplies the content and the target shape,
      EXACT Coding the method.
   c. **Unit checkpoint** for the aggregate (TDD cycle 5) before the next
      aggregate starts.
4. **What the finished aggregate must show** (the tests drive it there; the
   refactor steps keep it there):
   - aggregate boundaries from the invariants: objects one rule keeps
     consistent share an aggregate; others are referenced by ID;
   - one intention-revealing method per command, named in the ubiquitous
     language — no public setters;
   - each invariant as a guard before the state change, throwing or returning
     a typed rejection named after the rule;
   - status value + allowed transitions from the brief's state machine;
   - one event type per published event; the aggregate records pending events;
     payloads match the AsyncAPI message schema or a mapping is listed;
   - typed IDs, a version field, and a reconstitution path that does not
     re-run guards;
   - no framework, ORM or HTTP types in the domain package (unless an ADR says
     otherwise);
   - every method, guard and event carries a trace comment
     (`// slice: B1-S03, command: …, INV-03, event: …`);
   - tests are pure unit tests: no database, no HTTP, no clock other than the
     one the ADR names.
Done when: every test in every aggregate's test file is active and approved, the
whole suite is green, every command and invariant of the increment's
slices is covered by an active test or logged as an open question, and every
quantitative or cardinality invariant among them has an active, passing
property test.

### Steps 7–12 — test-first as well
Each of steps 7–12 runs the *TDD cycle* per unit with its step skill: a test
list first (integration tests against a real database in step 7; use-case
tests with contract-checked fakes in step 8; in-process HTTP and consumer
tests against the specs in step 9; mapping tests and relay tests against the
real database with a fake broker in step 10; projection and query tests
against the real database in step 11; real-adapter, acceptance and contract
tests against the running service in step 12), approval, then one behavior
at a time. The contracts below say what the finished units must show, for
the current increment's slices (see *Increments*); the step skills say how.

### Step 7 — Persistence (contract)
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

### Step 8 — Application services (contract)
- One use case per command: load the aggregate, call the domain method, save
  (state and outbox in one transaction, step 7), return. Not found and not
  authorized are application errors; domain rejections and conflicts pass
  through unchanged, with their rule ID.
- Authorization as the step 5 ADR places it: each use case receives the
  caller's principal and runs its resource check **before** the domain call,
  deny by default. The test list holds, per command, one allowed actor and
  one refused actor from the board, with the trace `-- B1-S03 ADR-012`.
  Policy handlers act as the service's own identity, as the ADR says.
- Policies (from steps 2–3) become handlers triggered by incoming events,
  idempotent by message ID per the ADR, compensating as the invariants sheet
  says.
- No business rule lives here; only orchestration and transaction boundaries.
- Tested with in-memory fakes that pass the **same** repository contract as
  the real adapter.
Done when: every command and policy has a use case or handler, every use
case checks access as the ADR places it, and the fakes pass the shared
contract.

### Step 9 — Inbound adapters (contract)
The approved specs are the source of the inbound layer. **Generate it; do not
write it by hand.** Only the wiring to the application services is written,
test-first.

**9a. Generate.** Use the generators the step 5 ADR chose (re-ask the
question now if it was skipped — 5f):
- **OpenAPI → controllers.** Server code in *interface-only* or *delegate*
  mode for the ADR's stack (for example OpenAPI Generator's server
  generators): one interface per operation group, request/response DTOs,
  validation from the schema. No business logic and no persistence calls in
  generated code.
- **AsyncAPI → consumers.** Listener/handler interfaces and message classes
  for every **subscribed** channel (for example an AsyncAPI Generator
  template for the stack, or ZenWave SDK for Java/Spring).
- Pin the generator **and its version**; run it from the build (plugin or
  script) so the code is regenerated on every build from the spec files —
  never copy generated sources into the hand-written tree, never edit them.
  Put them in their own package or the build's generated-sources folder.
- Build once and show: generator and version, what was generated (operations,
  channels, DTO and message counts), and that it compiles. A generator
  warning or a spec construct it cannot express is shown, not hidden; the fix
  goes into the spec (back to step 4) or into a generator option, not into
  the generated code.

**9b. Wire, test-first** (TDD cycle, one unit per operation group and per
subscribed channel). The test list covers, for every operation and channel:
- DTO → command (or query) mapping, including every field the spec makes
  required and every enum value;
- the call to the right use case from step 8;
- authentication per the spec's security scheme and the step 5 ADR: a
  protected operation without credentials or with an invalid token → 401, a
  missing scope or role → 403 (or 404 where the ADR hides existence), each as
  a Problem; the principal is passed on as the application's type; for
  consumers, messages from an unauthenticated source are refused;
- result → response DTO, with the status code and headers the spec defines;
- every domain rejection → Problem Details with the status code from the
  ADR, and the error responses the spec declares;
- for consumers: message → policy use case, idempotency per the ADR (the same
  message twice has one effect), and the anticorruption mapping where step 1
  marked one;
- that no generated DTO or message class leaks into the domain package;
- once per API (edge unit): undeclared path → 404, undeclared method → 405
  with `Allow`, undeclared content type → 415, unparsable body → 400 — each
  as a Problem, never 500;
- coverage gates: a new operation or receive operation in the spec fails
  the build or a test until it is wired — or until it is on the planned list
  with the increment that will wire it (see *Increments*).
The wiring classes implement (or are typed against) the generated contract;
mapping lives in small mapper classes next to them. Runtime spec validation
must check formats too (`date-time` and the like). The real broker binding
is proven in step 12.

The generator always runs on the whole approved specs, so later increments
find their DTOs and interfaces already generated; only the wiring grows per
increment.

Done when: the generated code compiles from the approved specs with the
pinned generator, every OpenAPI operation and every subscribed channel of
the increment's slices is wired (the rest is on the planned list), every
wiring test is active and approved, and all gates pass.

### Step 10 — Outbound messaging (contract)
- Message classes (and producer interfaces, where the generator offers them)
  for every **published** channel come from the same pinned AsyncAPI
  generator as step 9 — generated, not hand-written.
- Hand-written and test-first: **one publish decision per domain event**
  (message or internal, checked against the event union — an undecided
  event fails the build; an event of a later increment counts as decided
  only through its planned-list entry), the mapping from each published event to its
  generated message class, send-side validation against the spec, and the
  outbox relay: at-least-once with a stable message ID, in order per
  aggregate, retry with backoff, dead letter, safe with several relays.
- The relay publishes through a publisher port; here it is tested against
  the real database and a fake broker — the real broker client is proven in
  step 12.
Done when: every send operation of the increment's slices has a mapper and
every domain event built so far has a publish decision.

### Step 11 — Query side (contract)
- One read model per green sticky (or Event Model state view); projections
  updated from events (same transaction or eventually, per the ADR) with one
  decision per event type, progress per outbox row (no gaps), one runner per
  projection, failing loudly, rebuildable from history.
- GET operations served from a query port — never by loading aggregates —
  with the spec's filters and keyset paging behind opaque cursors.
- With eventual consistency, the wiring test shows the lag (the query before
  the projector run does not see the change); read-your-writes is decided in
  the ADR, not assumed.
Done when: every read model and GET operation of the increment's slices is
served and the step 9 coverage gate is green.

### Step 12 — Tests (contract)
Unit and integration tests already exist from steps 6–11. This step adds what
only the running service can show, again as a test list first, against
**real** infrastructure (database and broker — no fake broker here):
- The real adapters earlier steps deferred — broker publisher and consumer
  binding — proven against the real broker; the composition root that wires
  the service, driven by the acceptance tests.
- Acceptance tests: one Gherkin scenario per Example Mapping example of the
  increment's rules, tagged with its trace and its slice (`@B1-S03`), end to
  end through the API and the broker; published messages checked against the
  AsyncAPI spec. One feature file per board.
- Contract tests of the running service against both approved specs: the
  OpenAPI with a property-based tool (Schemathesis), the AsyncAPI from its
  message examples — limited to the operations and messages built so far;
  planned ones are excluded by name, never by a broad filter. Every finding is triaged: a defect goes back to its
  owning step, a spec gap to step 4, a rule the spec cannot express gets the
  narrowest documented exclusion.
- **Quality scenario tests.** One test per quality scenario from step 5
  marked *test* or *both*, traced `-- QS-03`, on the real infrastructure of
  this step and limited to the increment's operations and channels (a
  scenario about operations of a later board is on the planned list).
  Typical forms:
  - *performance*: a load run (k6, Gatling or the stack's tool) against the
    running service at the scenario's load, asserting its percentile;
  - *reliability*: fault injection with the test containers — stop the
    database or the broker, then restart it — asserting the scenario's
    response during the outage, the recovery time, and that no published
    event is lost (outbox rows all delivered, no duplicates beyond the
    idempotency key);
  - *consistency*: lag measured from the command to the read model and to
    the published message;
  - *compatibility*: a diff of each spec against its last released version
    (for example oasdiff for OpenAPI, the AsyncAPI diff tool), failing on a
    breaking change the scenario forbids;
  - *operability*: a correlation ID from the request appears in the
    published message and in the logs; health and readiness endpoints
    answer; the metrics and traces the scenario names exist;
  - *maintainability*: architecture rules as tests (ArchUnit, Konsist,
    dependency-cruiser) for the module boundaries the scenario states;
  - *data protection*, where testable: an erasure request removes the
    person from the state, the read models and the outbox payloads the
    scenario names.
  The test environment is not production. Each result names the
  environment it ran on and is marked **indicative** where that differs from
  the scenario's environment (say how). A missed measure is a finding for the
  user: a defect back to its owning step, a design question back to step 5,
  or a revised scenario in step 5 with the user's consent — never a quietly
  loosened measure. Report per scenario: *met*, *missed*, *indicative* or
  *not run* (with the reason).
- Run the whole suite and report results. A failing test is reported, never
  silently changed to pass.
- Intent coverage, not lines: every rule, example, policy, operation,
  channel, domain event and testable quality scenario of the slices built so
  far has at least one **passing, active** test; rules without a
  domain-level test are listed too. Everything else must be on the planned
  list. Gaps fail the step.
Done when: all hand-written tests are active and green twice, the contract
runs are green or every finding is triaged, every testable quality scenario
in the increment's scope has a result and none is *missed* without a
decision, the intent-coverage report has no gaps, every fix made in this
step's round has passed the scoped reverse check (step 15) with no open
drift, and — after the last increment — the planned list is empty.

### Step 13 — Independent review (Fagan inspection)
The agent that wrote the code shares its blind spots, so it never reviews
its own work. Step 13 runs once per increment, after step 12 is green, as a
Fagan inspection with fixed roles:

| Role | Who |
|---|---|
| Moderator | the orchestrator: plans, runs the phases, owns entry and exit criteria; never judges a defect itself |
| Author | the agent that ran steps 6–12; answers questions, does the rework; never inspects |
| Inspector(s) | one or two **separate review agents**, each started fresh with the `Agent` tool; a different model than the author's when one is available (`model` parameter); read-only — they never edit files |
| Decider | the user: accepts, rejects or defers each defect |

**Phases:**
1. **Planning — entry criteria.** Step 12 is done (suite green twice,
   intent coverage without gaps), and the scoped reverse check has no open
   drift. Put together the **inspection package**: the increment's diff (the
   whole code in increment 0), the tests, the generator configuration, and
   the approved artifacts the code implements — brief, invariants sheet,
   specs, ADRs, principles in scope, slice plan, the quality scenarios with
   their verification method, and step 12's quality results. Leave out this
   conversation, the ledger's rationale, prediction notes and the author's
   explanations: the inspector judges the work, not the story about it.
2. **Overview.** One short written introduction for the inspectors: the
   increment, its slices, the files in scope, and the checklist. Nothing that
   defends a design choice.
3. **Preparation and inspection.** Each inspector works through the
   package against the **checklist** and reports every defect with:
   location (file and line), the artifact element it violates (`INV-03`,
   `POST /loans 409`, `ADR-007`, `PRIN-04`, `QS-03`, or "checklist item"), severity
   (*major*: wrong or missing behavior, broken contract, violated invariant,
   ADR or principle, a test that cannot fail; *minor*: naming, clarity,
   structure), and the evidence. Inspectors find defects; they do not
   propose fixes or rewrite code. With two inspectors, give them different
   focuses — **domain** (rules, states, events, examples, properties) and
   **architecture** (layers, transactions, outbox, idempotency, error map,
   principles, quality scenarios) — and run them in parallel.
4. **Logging.** The moderator merges the reports, removes duplicates, and
   shows the user one defect list. The user decides each defect: **accept**
   (it becomes rework), **reject** (logged with the reason — a false
   positive is never silently dropped), or **defer** (an open question with
   the step it blocks).
5. **Rework.** Every accepted defect goes back to the step that owns the
   code as a new failing test, red first (a spec defect to step 4, a
   document claim to the reverse check). Minor naming defects are fixed by a
   refactor under green tests. Step 12 then runs again, then the scoped
   reverse check.
6. **Follow-up — exit criteria.** The moderator checks that every accepted
   defect has its test and fix. When any **major** defect was reworked, a
   **new** inspector agent (fresh context, never the one that found it)
   re-inspects the changed units only. Exit when no accepted major defect is
   open.

**Checklist** (the inspectors get it in the overview):
- every guard, transition, command method and event traces to a board
  element, invariant or spec element; nothing is invented;
- every invariant of the increment is enforced in its aggregate before the
  state change, with a typed rejection named after the rule; no business
  rule in application services, adapters or mappers;
- states and transitions match the brief; illegal commands are refused;
- the dependency rule holds: no framework, ORM, HTTP or generated types in
  the domain package; generated code is untouched;
- transaction boundaries: state and outbox in one transaction; the
  processed-message record with the aggregate; optimistic locking used;
- error map: every domain rejection maps to the Problem Details and status
  the ADR and the spec declare; no 500 for an expected case;
- idempotency of consumers and policy handlers as the ADR says;
- tests: each can fail for the reason it names (no assertion-free tests, no
  mocks that hide the behavior, no test edited to pass), property tests
  state the relation rather than restating examples, traces after `--` are
  correct;
- names follow the ubiquitous language of the glossary and the brief;
- every testable principle in scope is honored, or overridden by a named
  ADR;
- **quality scenarios marked *review* or *both*:** a verdict per scenario —
  *met*, *not met* or *cannot judge* — with the evidence (for example the
  erasure path for personal data, what the logs contain, how a module
  boundary is kept);
- code that works against a quality scenario even where step 12 measured
  nothing yet: an unbounded query or list against a latency or volume
  budget, a missing timeout or retry limit on a call to another context, a
  blocking call inside a transaction, personal data in a log line or an
  event the scenario forbids — major when it puts a high-priority scenario
  at risk;
- step 12's quality results: an *indicative* result is labeled as such,
  and no missed measure was quietly loosened;
- obvious security defects (unvalidated input reaching a query, secrets in
  code, missing authorization on an operation the spec protects) are
  reported as major. The full security review follows in step 14.

**No separate agent available.** If the `Agent` tool is missing, say so and
offer: **Review in a fresh session** — write the inspection package and a
review prompt (the checklist and the defect format) to
`reviews/increment-<k>-package/`, the user runs it in a new session or
with another model, and pastes the report back for phases 4–6; or **Waive**
— logged as "not independently reviewed", with the increment marked so in
the closing report. The author never reviews its own increment and calls it
independent.

**Docs-as-Code.** Save each inspection report as
`reviews/increment-<k>-inspection.md` in the project: package contents,
inspectors (model, focus), defects with decisions, rework, follow-up result,
and the counts.

Done when: the exit criteria are met, every defect is accepted and reworked,
rejected with a reason, or deferred as an open question, and the report is
saved.

### Step 14 — Security review (OWASP)
Runs once per increment, after step 13, on the increment's code and the
running service. Like step 13, it is done by a **separate agent** in a fresh
context (`Agent` tool), never the author, with read access to the code and
the running test environment and no write access to the code. It gets the
increment's diff (the whole code in increment 0), the approved specs with
their security schemes, the step 5 authentication, authorization and
security-baseline ADRs, the board's actors and the command → actor table,
and the dependency manifests.

**Checklists.** The **OWASP Top 10** (current edition) and the **OWASP API
Security Top 10**, since the service is an API. For every category, the
report says *checked — no finding*, *finding*, or *not applicable*, with the
reason; a category is never left blank. The API list leads for this kind of
service:
- broken object-level authorization — every operation that takes an ID
  checks that this actor may touch this object (step 8's resource checks);
- broken authentication — token validation (issuer, audience, signature,
  expiry), no credentials in URLs or logs;
- broken object-property-level authorization — no mass assignment from
  request DTOs; responses expose only the fields the spec declares;
- unrestricted resource consumption — body size, paging limits, rate limits
  as the ADR set them;
- broken function-level authorization — every command's actor table holds,
  deny by default;
- unrestricted access to sensitive business flows, server-side request
  forgery, security misconfiguration, improper inventory (undocumented
  operations, debug endpoints), unsafe consumption of other contexts' APIs
  and messages (the anticorruption layers from step 1);
- from the general list: injection (queries built from input),
  cryptographic failures, vulnerable components, logging and monitoring
  failures (security events logged, no personal data or secrets in logs).

**Tools**, as the step 5 ADR chose them; set up in increment 0 and run from
the build afterwards: dependency audit, secret scan, static analysis for the
stack, and a dynamic scan of the running service against the OpenAPI spec.
Tool output is triaged like Schemathesis findings: each finding is
confirmed or marked false positive with a reason.

**Findings.** Each with: OWASP category, severity (critical / high / medium /
low), location, evidence (a request and response, a code path, a tool
result), and the ADR or spec element it violates. The user decides each:
- **Fix** — goes back to the owning step as an **abuse-case test**, red
  first (`-- B1-S03 OWASP-API1`), then step 12 runs again, then the scoped
  reverse check. An auth or scheme gap in the spec goes to step 4; a missing
  decision to step 5.
- **Accept the risk** — only with an ADR that names the risk, the reason and
  the owner; critical findings cannot be accepted this way without the user
  saying so explicitly.
- **Defer** — an open question, with the increment that must close it.

After the fixes, the security agent (a new one if a high or critical finding
was fixed) re-tests the changed operations. Save the report as
`reviews/increment-<k>-security.md`.

**No separate agent available.** Offer **Review in a fresh session** (write
the package and the checklist to `reviews/increment-<k>-security-package/`)
or **Waive**, logged as "no security review" and shown in the closing
report.

Done when: every category of both lists has a verdict, no critical or high
finding is open unless accepted by ADR, every fixed finding has an active
abuse-case test, the baseline tools run green from the build, and the report
is saved.

### Step 15 — Traceability and closing
1. Build the matrix: board → slice → board element → invariant → spec
   element → class/method → test → increment, starting from step 12's intent-coverage report (its traces after
   `--` and scenario tags link tests to rules and operations; the trace
   comments in the code link classes and methods). Mark every row complete /
   partial / missing. Every slice of the plan must appear; a slice with no
   class or no test is *missing*. Confirm the planned list is empty.
   Add a **quality matrix**: quality scenario → ADRs that serve or trade it
   off → test (step 12) and/or review verdict (step 13) → result (*met*,
   *missed*, *indicative*, *not met*, *open verification*). Every accepted
   scenario must appear; one marked *not verifiable here* stays as an open
   verification with its owner.
2. **Reverse check — documents against code.** The matrix runs forward
   (rule → test) and never notices a document that describes something the
   code does not have. Run the *reverse check* (below) over the whole context
   and show its drift table.
3. If a graph exists, offer to ingest the specs and the matrix with
   `domain-knowledge-graph` (grow or revise mode) and report what connected,
   what contradicts, what is orphaned.
4. Compile one closing report: artifacts used (source per step), the
   increments in the order they ran with their slices, what the walking
   skeleton proved, steps implemented directly vs by skill vs deferred, open questions grouped by
   step, the traceability gaps, the reverse-check result (claims
   checked, drifted, resolved, open), the review summary per increment
   (inspectors and models, defects by severity, rejected and deferred ones,
   increments marked "not independently reviewed"), the security summary
   per increment (findings by OWASP category and severity, fixed, accepted
   by ADR, deferred, increments marked "no security review"), and the
   quality summary (scenarios met, missed, indicative, verified by review,
   open verifications, deferred quality questions), and the delivery summary
   (pull requests per increment with their CI results and merge state,
   Definition of Done items waived). Deliver the code location, the ledger
   and the report.

Done when: the matrix has no *missing* rows the user has not accepted, the
planned list is empty, the reverse check reports no open drift, and every
quality scenario has a result or is an open verification with an owner.

#### Reverse check

Checks every **structural claim** the run's documents make against the code
as it is now. Four kinds of claim, each read from the code by its source of
truth, never from memory:

| Claim | Where the documents state it | Read from the code |
|---|---|---|
| **Names** — aggregates, entities, value objects, commands, events, policies, read models, packages | design brief, invariants sheet, slice plan, Visual Glossary, ADRs (package layout) | the domain and application packages: class, method, event-type and package names, and the trace comments |
| **Columns** — tables, columns, types, nullability, unique constraints named after rules, outbox and delivery columns | persistence ADR, invariants sheet (uniqueness), step 7 contract, glossary attributes | the schema of the **migrated real database** (information schema), not the migration files alone |
| **Enum values** — statuses, kinds, reasons, error codes | brief, glossary, OpenAPI/AsyncAPI schemas, error-map ADR | the code's enums and union types, and the Problem Details types the error map produces |
| **States** — states, transitions, initial and terminal states, illegal commands per state | brief state machines (per board, merged) | the aggregate's status type and its transition guards, confirmed by the state tests from step 6 |

**How.** Prefer a mechanical check: a small script in the project (for
example `tools/reverse-check`) that extracts the claims from the documents
and the facts from the code and database, compares them, and runs from the
build. Where a claim cannot be extracted mechanically, check it by reading
and say so. A structural claim that no check can verify is shown as a
finding: make it verifiable, or remove it from the document.

**Rules.**
- **Never change the code to match a document.** The document records an
  intention, the code is the reality. Each drift is resolved one of two ways,
  and the user picks:
  - **Correct the document** — when the code is right (a rename during
    refactoring, a column added for a test-driven reason). The change is a
    revision of the artifact's owning step (brief → step 2, invariants →
    step 3, spec → step 4, ADR → step 5) and is shown before it is saved.
  - **Defect** — when the document is right. It goes back to the owning step
    of the code as a new failing test, red first, like any step 12 defect.
- Matching is exact after case folding; a near match (`Lent` vs `Loaned`,
  `due_at` vs `due_date`) is name drift, never silently accepted.
- Spec ↔ generated code is already covered by the generator and the contract
  runs; the reverse check compares spec enums and names with the
  **hand-written** domain and mapping code.
- A claim about a slice still on the planned list is skipped and marked
  *planned*, not *drifted*.

**Report**, as the drift table:

| # | Kind | Claim | Document location | Code location | Finding | Resolution |
|---|---|---|---|---|---|---|
| 1 | state | `Loan: Overdue → Returned` allowed | brief B1 §5 | `Loan.return()` guard | transition refused in code | defect → step 6 |

Close with the counts: claims checked per kind, claims drifted, resolved,
open.

**When it runs:**
- in full at step 15;
- **after every step 12 fix**: once a defect sent back from step 12 is fixed
  in its owning step and step 12 is green again, run the reverse check scoped
  to the documents that describe the changed units (their names, tables,
  enums and states), before the increment checkpoint. A fix changes
  behavior, and changed behavior is what makes a correct claim stale.

## Reading the graphs

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

**Inventory** (step 1) — counts per class, overall and per context:

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

**API specs in the graph** (steps 1, 4) — newer graph versions carry
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

**Contexts** (step 1):

```sparql
SELECT ?ctx ?label WHERE { ?ctx a dkg:BoundedContext ; skos:prefLabel ?label }
```

**Context readiness** (step 1) — one row per context, for the question
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

**Context slice** (steps 2, 3, 6) — replace `CTX` with the chosen label:

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

**Command → event chains** (step 2):

```sparql
SELECT ?cmd ?evt ?agg WHERE {
  ?c a dkg:Command ; skos:prefLabel ?cmd ; dkg:triggers ?e .
  ?e skos:prefLabel ?evt .
  OPTIONAL { ?e dkg:emittedBy ?a . ?a skos:prefLabel ?agg }
}
```

**Glossary cardinalities** (step 3) — candidate invariants; replace `CTX`:

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

**Design-level board for the context** (step 3) — commands, aggregates,
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

**Slices in the graph** (steps 1, 2) — slices of the context with their
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

**Example Mapping in the graph** (step 3) — the vocabulary has no dedicated
class yet, so look for an artifact or node whose class or label names it:

```sparql
SELECT DISTINCT ?n ?cls ?label WHERE {
  ?n a ?cls . OPTIONAL { ?n rdfs:label|skos:prefLabel ?label }
  FILTER(CONTAINS(LCASE(STR(?cls)), "examplemapping")
         || CONTAINS(LCASE(STR(?label)), "example mapping"))
}
```

If it returns nothing, the graph holds no Example Mapping: ask for one.

**Policies** (steps 3, 8): `?p a dkg:Policy ; dkg:reactsTo ?e ; dkg:triggers ?c`.
**States** (step 6): `?a a dkg:Aggregate ; dkg:hasState ?s`.
**Contradictions and open questions** (every step): `?x dkg:contradicts ?y`
and `?q a dkg:Question` in the context — show them before asking the user to
accept the step's input.

### Architecture decision graph

**Decision inventory** (step 1):

```sparql
SELECT ?kind ?status (COUNT(DISTINCT ?n) AS ?count) WHERE {
  { ?n a dkg:Decision . BIND("decision" AS ?kind) OPTIONAL { ?n dkg:status ?status } }
  UNION
  { ?n a dkg:Principle . BIND("principle" AS ?kind)
    OPTIONAL { ?n dkg:testable ?t } BIND(IF(BOUND(?t) && ?t, "testable", "not testable") AS ?status) }
} GROUP BY ?kind ?status ORDER BY ?kind ?status
```

**Unacknowledged overrides and principles in tension** (step 1, step 5):

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

**Decisions in scope** (steps 1, 5) — replace `CTX`:

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

**Quality requirements in the graph** (step 5) — there is no fixed class
yet for quality goals and scenarios, so match on the class name, as for API
specs:

```sparql
SELECT DISTINCT ?cls (COUNT(DISTINCT ?n) AS ?count) WHERE {
  ?n a ?cls .
  FILTER(REGEX(STR(?cls), "(Quality|Scenario|QualityGoal|QualityRequirement|Constraint)", "i"))
} GROUP BY ?cls
```

**Quality inventory** (step 5) — the scenarios with their parts; replace
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

**Principles in scope** (steps 1, 6–12):

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

## Dependencies (what to re-check when a step is revisited)

| Revised step | Re-check |
|---|---|
| 1 Scope | everything |
| 1 Domain knowledge graph replaced or grown | 2, 3, 4, then 15 |
| 1 Architecture decision graph replaced or grown | 5, then the guardrail check of 6–12 |
| 1 Domain Story or Visual Glossary changed (no graph) | the consistency check and context cut in 1, then 2, 6, 15 |
| 1 Board added or changed | its slice table and the slice plan in 1, then 2, 3, 4; a new board gets its own increment; a changed board re-checks the increments already built from it |
| 1 Slice plan or board order changed | the planned list and the order of the open increments; increments already built are re-checked only for slices that moved |
| 1 Walking skeleton changed | before increment 0: nothing else; after it: increment 0 is re-run for the new slice, and the old skeleton slice moves to its board's increment |
| 2 Design brief | 3, 4, 6, 8, 11, 15; the slice plan and the skeleton (step 1, 4b–4c) |
| 3 Invariants (incl. a new or changed design-level board or Example Mapping) | 6, 8, 12, 15; a changed board also re-checks 2 |
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
| Reverse check finds drift | *correct the document*: the artifact's owning step (2, 3, 4 or 5) as a revision, then its usual re-checks; *defect*: the code's owning step (new failing test there), then 12 again |
| An increment's step 6–12 | the same step's later work in this increment, then steps 7–12 of every later increment already built that extend the same units, then steps 13 and 14 of each affected increment for the changed units |

Revisiting a step in a finished increment re-opens only that increment's
scope; a later increment that already extended the same unit is re-checked,
not redone.

## Ledger format (`implementation-log.md`)

```
Context: <name, as the user confirmed it>   Later runs: <other contexts named>   Output: <path>
Domain knowledge graph: <file or none>   Architecture decision graph: <file, "same file" or none>
Domain Stories: <files or "from graph">   Visual Glossary: <file or "from graph">   Generated drafts: <which, if any>
Design-level boards (step 1): B1 — <name>: <"from graph" / file>, <n> slices; B2 — … ; or "not available"
Slice plan (step 1): <from graph / uploaded / proposed / pending>, accepted <date>; board order: B2, B1, …
Walking skeleton (step 1): <slice IDs>, <named / proposed / pending>, layers not reached: <list>
Increments: 0 — Walking skeleton <done/approved date>; 1 — Board: <name> <status>; …   Current: <k>
Planned list: <spec element → increment>, …  (empty after the last increment)
Design-level EventStorming (step 3): <reuse steps 1–2, "from graph", file or "not available">
Example Mapping: <uploaded file, EXACT session file, "from graph" or "not available">   Read with: <example-mapping-interpreter / EXACT example-mapping session>
OpenAPI: <from graph / uploaded / from API Product Canvas / proposed / not needed>, approved <date>
AsyncAPI: <from graph / uploaded / from API Product Canvas / proposed / not needed>, approved <date>
Spec proposals: <accepted IDs> accepted, <rejected IDs> rejected, <user's own proposals>
Principles in scope: <IDs>   Open governance issues: <count>
Architecture Knowledge Graph (step 5): <file, "same file as decision graph" or "not available">   Quality goals: <ranked list>
Quality scenarios (step 5): QS-01 … QS-nn — <from graph / answered / deferred / not measurable>; verify by <test / review / both / not here>; quality questions asked <n>, skipped <n>
Quality results (steps 12–13): per increment — QS-<id>: <met / missed → decision / indicative (environment) / not met / not run (reason)>; open verifications <ids, owner>
Step 5 questions: <n> asked, <n> decided (ADR IDs), <n> skipped — Q<id>: blocks steps <list>; re-asked at step <n>: decided / skipped again (risk logged)
EXACT Coding: <installed skills / project .claude/skills / vendored at commit <hash> / fallback>   Autonomy level: <full-hitl / …>
Refactor agent: <agent type `refactor` / project .claude/agents/refactor.md / vendored at <hash> / fresh session / skipped>
Slice refactoring: per refactoring point — increment <k>, step <n>, slice <id>: retained <n>, undone <n>, boundary <seam made visible / cohesive: evidence>, proposals <n> → step <n>, prediction mismatches <n>, suite <green / undone by reverse patch>, decision <keep / undo / part>, commit <hash>
Delivery (step 0): repo <path, default branch>; branches <per increment / own model: …>; commits <Conventional Commits, at unit checkpoint / step checkpoint / ask>; remote <GitHub / GitLab / none>, PRs <per increment / none>; CI <provider / none>; hooks <tool: format, lint, types, secrets, commit-msg / none>; docs <path>; project memory <AGENTS.md / CLAUDE.md / none>; Definition of Done <default + additions>; declined parts and what replaces them
Increment delivery: per increment — branch <name>, commits <n> (types), DoD <met / waived: items>, PR <link>, CI <green / red → defect>, merged <date by user / open>
TDD (steps 6–12): per unit — <test file>, <active>/<total> tests, <added> added on the way, prediction mismatches <n> (explained y/n), gates green y/n, approved <date>
Property tests (step 6): <library + version>, runs <n>, seed <n>; per aggregate — properties <n> for INV <ids>, failed on first run <ids> → counterexamples kept as tests <n>, candidate examples for step 3 <n>
Generated from specs (steps 9–10): <OpenAPI generator + version, mode>, <AsyncAPI generator + version>, output folder, compiled y/n, warnings
Test infrastructure (steps 7–12): database <Testcontainers / local server / user ran it>, broker <real in step 12: which>
Contract runs (step 12): <tool + version>, seed <n>, <cases> cases, findings <n> → <defect: step / spec: step 4 / documented exclusion>
Intent coverage (step 12): gaps <ids or none>, rules without a domain-level test <ids or none>
Sent back: <finding> → step <n> (revision r<k>)
Review (step 13): per increment — <agent / fresh session / waived>, inspectors <n> (model, focus), defects major <n> minor <n>; accepted <n> → step <n>, rejected <n> (reasons), deferred <n>; re-inspection <y/n, by new agent>; report reviews/increment-<k>-inspection.md
Security review (step 14): per increment — <agent / fresh session / waived>, model <name>; tools <audit, secret scan, static analysis, dynamic scan + versions>; categories with verdict <n>/<n>; findings critical <n> high <n> medium <n> low <n>; fixed <n> (abuse-case tests), accepted by ADR <ids>, deferred <n>; report reviews/increment-<k>-security.md
Reverse check: <after fix r<k> (scoped) / step 15 (full)> — <mechanical: script path / by reading>, checked names <n>, columns <n>, enums <n>, states <n>; drifted <n> → <document corrected: step / defect: step>; open <ids or none>; unverifiable claims removed or made verifiable <n>

| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
| 2 | — | Design brief | graph | graph.ttl (ctx Lending, B1, B2) | event-storming-detail-interpreter | y | y | 2026-10-23 | 2 |
| 6 | 0 — Walking skeleton (B1-S01) | Domain model | reuse 2,3,4 | src/lending/domain | aggregate-behavior-author | y | y | 2026-10-24 | 0 |
| 9 | 0 — Walking skeleton (B1-S01) | Inbound adapters | reuse 4,5,8 | src/lending/infrastructure/http | generator: openapi-typescript 7.13.0; inbound-adapter-author | y | y (r2: 405 Allow, from step 12) | 2026-10-25 | 0 |
| 6 | 1 — Board: Lending | Domain model | reuse 2,3,4 | src/lending/domain | aggregate-behavior-author | y | y | 2026-10-26 | 1 |
| 10 | 1 — Board: Lending | Outbound messaging | — | — | deferred | — | — | 2026-10-26 | 3 |
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.

## Tone

Plain language to the user; no skill names or file paths in the conversation
unless asked. Say "I'll extract the invariants from your board" rather than
naming the skill. Keep each step's exchange short: what is expected, the
choice, the result, the decision. Use American spelling.