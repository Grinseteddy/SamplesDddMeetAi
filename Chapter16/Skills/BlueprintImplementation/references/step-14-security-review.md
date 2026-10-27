# Step 14 — Security review (OWASP)

Loaded by `blueprint-service-implementation` at the start of step 14. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

Runs once per increment, after step 13, on the increment's code and the
running service. Like step 13, it is done by a **separate agent** in a fresh
context (`Agent` tool), never the author, told to load
**`owasp-security-reviewer` in reviewer mode**, with read access to the code
and the running **test** instance and no write access to the code. The
orchestrator is the moderator. It gets the
increment's diff (the whole code in increment 0), the approved specs with
their security schemes, the step 5 authentication, authorization and
security-baseline ADRs, the board's actors, the increment's user stories
(their "As a …" actors are the allowed actors of step 8) and the command →
actor table,
the dependency manifests, the test target with one test identity per
actor and tenant, the tools and request limits, and the accepted risks and
deferred findings of earlier increments.

**Checklists, severity and formats** come from `owasp-security-reviewer`:
the OWASP API Security Top 10:2023 first, then the OWASP Top 10:2025 for
what the API list does not cover. For every one of the 20 categories, the
report gives *checked — no finding*, *finding*, or *not applicable*, with
the reason; none is left blank. Each finding carries an abuse-case test
specification traced to its slice and user story
(`-- B1-S03 US-07 OWASP-API1`).

**Tools**, as the step 5 ADR chose them: the dependency audit and secret
scan run in `verify` from increment 0; static analysis and the dynamic scan
of the running service against the OpenAPI spec run as **CI jobs** with a
time cap, set up in the first increment the user schedules them for. The
security agent reads their latest results; it does not start long scans
itself unless the user asks.
Tool output is triaged like Schemathesis findings: each finding is
confirmed or marked false positive with a reason.

**Findings.** Each with: OWASP category, severity (critical / high / medium /
low), location, evidence (a request and response, a code path, a tool
result), and the ADR or spec element it violates. The user decides each:
- **Fix** — critical, high and medium findings go back to the owning step as
  an **abuse-case test**, red first (`-- B1-S03 US-07 OWASP-API1`); low
  findings are batched into one fix unit, like minor review defects. Then
  step 12's suites and gates once, then the scoped reverse check once. An
  auth or scheme gap in the spec goes to step 4; a missing decision to step
  5. A fix that would add a tool or a new CI job is a proposal, as in step 13.
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

**Skill not available:** say so in one sentence; brief the agent with both
OWASP lists by category, the safety boundary (test instance only, agreed
tools and limits) and the finding format, and log the review as "reviewed
without owasp-security-reviewer".

Done when: all 20 categories of both lists have a verdict, no critical or high
finding is open unless accepted by ADR, every fixed finding has an active
abuse-case test, the baseline tools run green from the build, and the report
is saved.

## In increment 0 (walking skeleton)

- **Step 14:** the security review of the skeleton, with the authentication
  and authorization mechanism from the step 5 ADR in place on the skeleton's
  operation — the skeleton proves auth end to end, and every later operation
  reuses it. Only the fast baseline checks are wired into `verify` now
  (dependency audit, secret scan); static analysis and the dynamic scan
  become CI jobs in the first increment the user schedules them for, each
  with a time cap.

## After step 14 of increment 0 — skeleton review

Before the increment checkpoint: show which layers and
step 5 decisions the skeleton proved, which layers it did not reach (for
example no consumer, because the skeleton has no policy) and which increment
will reach them, and every decision the skeleton contradicted or made
doubtful. Offer **go back to step 5** for each; the dependency table says
what to re-check.

## After step 14 — increment checkpoint

After step 14 of every increment, before the step checkpoint that leads on:

1. **Name the increment that ends** and summarize it: user stories done
   (IDs, examples passing / total), user stories taken out and where they
   went on the planned list, slices built (IDs), units added or extended, tests active/total, slice refactorings (retained,
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
   increment 2`); description with the user stories done, the slices built,
   the Definition of Done
   checklist, the review, security and quality summaries, links to the
   reports in `reviews/`, the open questions, and the planned list. After
   confirmation, push and open it, wait for the CI result, and show it. A red
   pipeline is a defect of this increment. The user merges.
4. **Show the remaining boards** in order, with their user stories, slice
   counts and Example Mapping status (ready / not ready, red cards open).
5. **Ask for approval:** **"Do you approve increment k — <name>, so we can go
   ahead with increment k+1 — <name>?"** Offer: **Approve and continue** /
   **Reorder the remaining boards or user stories** / **Revise this increment** / **Go back to
   an earlier step** / **Pause here**. After the last increment, the next
   step is step 15.
6. **Record** the approval, the pull request link, the CI result and the
   date in the ledger.

## Definition of Done (agreed in step 0)

<!-- shared:definition-of-done -->
8. **Definition of Done** for an increment — show the default and let the
   user add to it:
   - every test active and green, and the CI pipeline green on the pull
     request;
   - every user story of the increment *done*: each example of its Example
     Mapping passes as an acceptance scenario;
   - all gates and hooks pass; generated code untouched;
   - step 13 review and step 14 security review closed, reports saved in
     `reviews/`;
   - the documents updated to match the code: the scoped reverse check
     (step 15) clean, new or changed ADRs and quality scenarios saved, the
     specs' changelog updated, README run instructions current;
   - commits follow the convention; the pull request is open, with its
     description filled in;
   - the ledger updated.
<!-- /shared:definition-of-done -->

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
| 14 Security review (a finding fixed) | the owning step (abuse-case test there, red first), step 4 for a spec gap or step 5 for a missing decision, then 12 again, then the scoped reverse check, then the re-test of 14 |
| Reverse check finds drift | *correct the document*: the artifact's owning step (1, 2, 3, 4 or 5) as a revision, then its usual re-checks; *defect*: the code's owning step (new failing test there), then 12 again |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Security review (step 14): per increment — <agent / fresh session / waived>, model <name>; tools <audit, secret scan, static analysis, dynamic scan + versions>; categories with verdict <n>/<n>; findings critical <n> high <n> medium <n> low <n>; fixed <n> (abuse-case tests), accepted by ADR <ids>, deferred <n>; report reviews/increment-<k>-security.md
Sent back: <finding> → step <n> (revision r<k>)
Reverse check: <after fix r<k> (scoped) / step 15 (full)> — <mechanical: script path / by reading>, checked names <n>, columns <n>, enums <n>, states <n>, story and example IDs <n>; drifted <n> → <document corrected: step / defect: step>; open <ids or none>; unverifiable claims removed or made verifiable <n>
Increment delivery: per increment — branch <name>, commits <n> (types), DoD <met / waived: items>, PR <link>, CI <green / red → defect>, merged <date by user / open>
Increments: 0 — Walking skeleton <done/approved date>; 1 — Board: <name> <status>; …   Current: <k>, current user story <US-nn>
Planned list: <spec element, rule or user story → increment>, …  (empty after the last increment)
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
