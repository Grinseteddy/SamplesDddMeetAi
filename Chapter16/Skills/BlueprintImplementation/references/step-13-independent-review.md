# Step 13 — Independent review

Loaded by `blueprint-service-implementation` at the start of step 13. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

The agent that wrote the code shares its blind spots, so it never reviews
its own work. Step 13 runs once per increment, after step 12 is green, as a
Fagan inspection with fixed roles:

| Role | Who |
|---|---|
| Moderator | the orchestrator: plans, runs the phases, owns entry and exit criteria; never judges a defect itself |
| Author | the agent that ran steps 6–12; answers questions, does the rework; never inspects |
| Inspector(s) | one or two **separate review agents**, each started fresh with the `Agent` tool and told to load **`fagan-code-inspector` in inspector mode**; a different model than the author's when one is available (`model` parameter); read-only — they never edit files |
| Decider | the user: accepts, rejects or defers each defect |

**Phases:**
1. **Planning — entry criteria.** Step 12 is done (suite green twice,
   intent coverage without gaps), and the scoped reverse check has no open
   drift. Put together the **inspection package**: the increment's diff (the
   whole code in increment 0), the tests, the generator configuration, and
   the approved artifacts the code implements — brief, the increment's user
   stories and the board's Example Mapping, invariants sheet, specs, ADRs,
   principles in scope, slice plan, the quality scenarios with
   their verification method, and step 12's quality results. Leave out this
   conversation, the ledger's rationale, prediction notes and the author's
   explanations: the inspector judges the work, not the story about it.
2. **Overview.** One short written introduction for the inspectors: the
   increment, its slices, the files in scope, and the checklist. Nothing that
   defends a design choice.
3. **Preparation and inspection.** Each inspector works through the
   package against the **checklist** and reports every defect with:
   location (file and line), the artifact element it violates (`INV-03`,
   `POST /loans 409`, `ADR-007`, `PRIN-04`, `QS-03`, `US-07`, `B1-S03`, or
   "checklist item"), severity
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
5. **Rework — by severity.**
   - **Major** defects go back to the step that owns the code as a new
     failing test, red first (a spec defect to step 4, a document claim to
     the reverse check), one owning step at a time.
   - **Minor** defects are **batched**: one rework unit for the whole
     increment, fixed under green tests — a new test only where a minor
     changes behavior, a refactor otherwise — in one `fix` or `refactor`
     commit, without a separate pass through each owning step's checkpoint.
   - **No scope growth through review.** A defect whose fix would add a gate,
     a tool, a threshold, a new ADR or work for a later slice is not rework:
     show it as a **proposal**, and by default plan it for the increment that
     needs it. It enters the current increment only by an explicit user
     decision, logged as such.
   After all rework: step 12's fast and slow suites and gates once, then the
   scoped reverse check once.
6. **Follow-up — exit criteria.** The moderator checks that every accepted
   defect has its test and fix. When any **major** defect was reworked, a
   **new** inspector agent (fresh context, never the one that found it)
   re-inspects the changed units only. Exit when no accepted major defect is
   open.

**Checklist, severity and formats** come from `fagan-code-inspector`:
traceability and invented behavior, domain rules, architecture, tests,
language and principles, quality scenarios, and obvious security defects.
The overview names the blueprint's IDs the inspectors cite (`B1-S03`,
`US-07`, `INV-03`, `ADR-007`, `PRIN-04`, `QS-03`, spec operations) and the planned
list, so planned work is not reported as missing.

**Skill not available** (`fagan-code-inspector` is not part of every
install; check for it at step 0): say so in one sentence; brief the inspectors with the checklist summary above and the
defect format (location, violated element, severity, evidence, no fix), and
log the review as "inspected without fagan-code-inspector".

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

## In increment 0 (walking skeleton)

- **Step 13:** an independent review of the skeleton's code, tests, build
  and generator setup — **one** inspector (architecture focus), since every
  later increment builds on it; a second only when the user asks.

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

<!-- shared:reverse-check -->
## Reverse check — documents against code

Checks every **structural claim** the run's documents make against the code
as it is now. Five kinds of claim, each read from the code by its source of
truth, never from memory:

| Claim | Where the documents state it | Read from the code |
|---|---|---|
| **Names** — aggregates, entities, value objects, commands, events, policies, read models, packages | design brief, invariants sheet, slice plan, Visual Glossary, ADRs (package layout) | the domain and application packages: class, method, event-type and package names, and the trace comments |
| **Columns** — tables, columns, types, nullability, unique constraints named after rules, outbox and delivery columns | persistence ADR, invariants sheet (uniqueness), step 7 contract, glossary attributes | the schema of the **migrated real database** (information schema), not the migration files alone |
| **Enum values** — statuses, kinds, reasons, error codes | brief, glossary, OpenAPI/AsyncAPI schemas, error-map ADR | the code's enums and union types, and the Problem Details types the error map produces |
| **States** — states, transitions, initial and terminal states, illegal commands per state | brief state machines (per board, merged) | the aggregate's status type and its transition guards, confirmed by the state tests from step 6 |
| **User stories and examples** — every user story of the built increments, its rules and examples, the story ↔ slice map | user-story list and slice plan (step 1), each board's Example Mapping (step 3) | the feature files (one per user story, `@US-nn` tags, one scenario per example), the test traces after `--`, and the trace comments in the code |

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
    revision of the artifact's owning step (user stories and slice plan →
    step 1, brief → step 2, invariants and Example Mappings → step 3, spec →
    step 4, ADR → step 5) and is shown before it is saved. An Example
    Mapping is corrected only with the user's consent, because its examples
    were agreed with domain experts.
  - **Defect** — when the document is right. It goes back to the owning step
    of the code as a new failing test, red first, like any step 12 defect.
- Matching is exact after case folding; a near match (`Lent` vs `Loaned`,
  `due_at` vs `due_date`) is name drift, never silently accepted.
- Spec ↔ generated code is already covered by the generator and the contract
  runs; the reverse check compares spec enums and names with the
  **hand-written** domain and mapping code.
- A claim about a slice or user story still on the planned list is skipped
  and marked *planned*, not *drifted*.
- For user stories, drift means: an example on the map with no scenario, a
  scenario with no example on the map, a scenario whose Given/When/Then
  differs from its green card, a `@US-nn` tag no user story has, or a slice
  serving a user story the slice plan does not link to it.

**Report**, as the drift table:

| # | Kind | Claim | Document location | Code location | Finding | Resolution |
|---|---|---|---|---|---|---|
| 1 | state | `Loan: Overdue → Returned` allowed | brief B1 §5 | `Loan.return()` guard | transition refused in code | defect → step 6 |

Close with the counts: claims checked per kind, claims drifted, resolved,
open.

**When it runs:**
- in full at step 15;
- **after every fix sent back from steps 12–14**: once the defect is fixed in
  its owning step and step 12 is green again, run the reverse check scoped
  to the documents that describe the changed units (their names, tables,
  enums, states, and the user stories and examples they serve), before the increment checkpoint. A fix changes
  behavior, and changed behavior is what makes a correct claim stale.
<!-- /shared:reverse-check -->

## Revisiting this step

When this step is revisited ("go back to step N"), re-check — not
regenerate — the completed steps below; offer keep / patch / redo for each
inconsistent one. The full table is in SKILL.md.

| Revised step | Re-check |
|---|---|
| 13 Review (a defect accepted) | the owning step (new failing test there) or step 4, then 12 again, then the scoped reverse check, then the follow-up of 13 (re-inspection by a new agent when the defect was major), then 14 for the changed units |
| Reverse check finds drift | *correct the document*: the artifact's owning step (1, 2, 3, 4 or 5) as a revision, then its usual re-checks; *defect*: the code's owning step (new failing test there), then 12 again |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Review (step 13): per increment — <agent / fresh session / waived>, inspectors <n> (model, focus), defects major <n> minor <n>; accepted <n> → step <n>, rejected <n> (reasons), deferred <n>; re-inspection <y/n, by new agent>; report reviews/increment-<k>-inspection.md
Sent back: <finding> → step <n> (revision r<k>)
Reverse check: <after fix r<k> (scoped) / step 15 (full)> — <mechanical: script path / by reading>, checked names <n>, columns <n>, enums <n>, states <n>, story and example IDs <n>; drifted <n> → <document corrected: step / defect: step>; open <ids or none>; unverifiable claims removed or made verifiable <n>
Quality results (steps 12–13): per increment — QS-<id>: <met / missed → decision / indicative (environment) / not met / not run (reason)>; open verifications <ids, owner>
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
