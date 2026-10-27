# Step 0 — Start

Loaded by `blueprint-service-implementation` at the start of step 0. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure

1. Create or read `implementation-log.md` (see *Resuming*).
2. Ask for the **output location** (a project folder on the user's computer or
   the working directory) and whether an existing codebase should be extended.
3. **Locate EXACT Coding** (see *EXACT Coding* below) and record where it
   comes from and at which commit. Check in the same pass whether the
   review skills `fagan-code-inspector` (step 13) and
   `owasp-security-reviewer` (step 14) are installed; say which are missing
   and that the run will use the step's fallback for them.
4. **Delivery setup** (see *Delivery* below). First show all defaults in
   one short list and offer **Accept all defaults** (recommended) /
   **Change some**. Only for *Change some*, ask the chosen questions one at a
   time. The stack-specific files are written later, in the walking
   skeleton, once step 5 has chosen the stack.
5. **Working mode.** Ask once: **full checkpoints** (every step approved one
   by one — recommended for the first run) or **condensed checkpoints** from
   increment 1 on (see *Step checkpoint*). Log the answer.
6. Close with the step checkpoint, then go to step 1 — the two graphs are
   asked for there, before anything else.

## Delivery

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
   user prefers. For boards too big for one review, offer **one pull
   request per user story** inside the board's increment
   (`feature/<context>-inc<k>-<board>-US-07`), stacked in story order.
3. **Commits.** Default: **Conventional Commits**, one commit per approved
   unit checkpoint (never per TDD phase — EXACT's method boundary), with
   - type: `feat` for new behavior, `fix` for a defect sent back, `test` for
     test-only changes, `refactor`, `build`, `ci`, `docs`, `chore`;
   - scope: the context or the aggregate (`feat(lending): …`,
     `feat(loan): …`);
   - footer: the traces (`Refs: US-07, B1-S03, INV-03, QS-02`) and the
     increment;
   - `!` and a `BREAKING CHANGE:` footer only for a spec change the user
     approved as breaking in step 4;
   - **shape that passes the commit-message hook the first time:** header at
     most 72 characters, subject in lower case without a final period, a
     blank line, body lines at most 100 characters, footer `Refs: …`. Before
     `git commit`, write the message to a file and run the project's
     commit-message check on it (for example `npx commitlint --edit <file>`);
     commit with `git commit -F <file>` only when it passes. Stage
     explicitly by path; never let a refused commit be followed by a broader
     one under the same message.
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
   another, or none)? Two kinds of job:
   - **`verify`** — written in the walking skeleton, runs the one build
     command on every push and pull request: generation, compile, lint and
     type check, the fast and slow test suites, the example-based contract
     check, the spec diffs once they exist, dependency audit and secret
     scan. It stays fast (target: under 10 minutes).
   - **Long jobs** — load, fault injection, contract fuzzing, static
     analysis and the dynamic security scan — each added in the increment
     that first needs it, on the pull request or on a schedule, each with a
     time cap. Their results are read at the increment checkpoint.
6. **Pre-commit hooks.** Which hook tool (pre-commit, Husky with
   lint-staged, Lefthook — or the stack's usual one)? Default hooks: format,
   lint, type check on changed files, secret scan, and a commit-message
   check for the chosen convention (for example commitlint). Hooks never run
   the whole suite — CI does. Hooks are written in the walking skeleton.
7. **Docs-as-Code.** Where the run's documents live in the repository —
   default `docs/<context>/`: the design brief, user stories, the Example
   Mapping of every board, invariants sheet, slice plan, specs, ADRs, quality
   scenarios, `reviews/`, and the ledger. Offer to
   create or update the project memory file (`AGENTS.md`, or `CLAUDE.md` for
   Claude Code) with pointers to these documents and the conventions above.
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

If the user declines a part (no remote, no CI, no hooks), log it and say
what the run cannot check instead (for example "no CI: the Definition of
Done uses a local run of the build command").

## EXACT Coding

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
run was not built with EXACT Coding. Either way, log the two adaptations this
skill makes to EXACT's cycle (see *TDD cycle*): predictions cover behavior,
not counts; each cycle runs the fast suite, the slow suite runs at the unit
checkpoint. Tell the user once; they may switch either back for a unit. Stacks: EXACT ships profiles for
TypeScript + Vitest and Java + JUnit 5 + Maven; for Kotlin, use the Java
profile adapted to the build, and say so.

## Ledger format (`implementation-log.md`)

**Size rule.** The ledger is read on every resume, so it stays short: one
line per unit, decision, revision and approval, and the header lines below.
Never log individual TDD cycles here. Cycle-level notes — predictions and
their outcomes, mismatches and their explanations, trials, process slips —
go to `ledger/journal-inc<k>-step<n>.md` (one file per step and increment,
appended as the cycles run, never loaded on resume). A unit's ledger line
links its journal. If the ledger passes about 300 lines, move finished
increments' detail lines into `ledger/archive-inc<k>.md` and keep one
summary line per finished increment.

```
Context: <name, as the user confirmed it>   Later runs: <other contexts named>   Output: <path>
Domain knowledge graph: <file or none>   Architecture decision graph: <file, "same file" or none>
Domain Stories: <files or "from graph">   Visual Glossary: <file or "from graph">   Generated drafts: <which, if any>
Design-level boards (step 1): B1 — <name>: <"from graph" / file>, <n> slices; B2 — … ; or "not available"
User stories (step 1): <from graph / uploaded (source, tracker) / generated (Inferred) / edited>, US-01 … US-nn, accepted <date>; INVEST findings <n> (split <ids>); open "so that" <ids or none>
Slice plan (step 1): <from graph / uploaded / proposed / pending>, accepted <date>; board order: B2, B1, …; story order per board: B1: US-03, US-01, …; findings both directions <n> (decided: <how>)
Story ↔ slice map: US-01 → B1-S01, B1-S02; US-02 → B1-S03, B2-S01 (owned by B1); …   automations attached: B1-S04 → US-02
Walking skeleton (step 1): <user story + slice IDs>, <named / proposed / pending>, layers not reached: <list>
Increments: 0 — Walking skeleton <done/approved date>; 1 — Board: <name> <status>; …   Current: <k>, current user story <US-nn>
Planned list: <spec element, rule or user story → increment>, …  (empty after the last increment)
Design-level EventStorming (step 3): <reuse steps 1–2, "from graph", file or "not available">
Example Mapping per board (step 3): B1 — <uploaded file / EXACT session file / "from graph">, read with <example-mapping-interpreter / EXACT session>, user stories US-01…US-05, rules <n>, examples <n>, red cards <n> (answered <n>, deferred → increment <k>: <ids>), verdict <ready / not ready / deferred board>, date; B2 — …   Mapping mode: <all boards in step 3 / just-in-time (chosen <date>)>
Readiness gate: per increment — <date>, map current y/n, board × story closed y/n, red cards answered <ids>, rules anchored y/n, user stories taken out <ids → increment>, result <ready / paused>
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
Working mode (step 0): <full checkpoints / condensed from increment 1>; review skills present: fagan-code-inspector <y/n>, owasp-security-reviewer <y/n>
Delivery (step 0): repo <path, default branch>; defaults <accepted all / changed: …>; branches <per increment / per user story inside the increment / own model: …>; commits <Conventional Commits, at unit checkpoint / step checkpoint / ask>; remote <GitHub / GitLab / none>, PRs <per increment / none>; CI <provider / none>; hooks <tool: format, lint, types, secrets, commit-msg / none>; docs <path>; project memory <AGENTS.md / CLAUDE.md / none>; Definition of Done <default + additions>; declined parts and what replaces them
Increment delivery: per increment — branch <name>, commits <n> (types), DoD <met / waived: items>, PR <link>, CI <green / red → defect>, merged <date by user / open>
TDD (steps 6–12): per unit, one line — <test file>, <active>/<total> tests, <added> added on the way, behavioral prediction mismatches <n>, gates green y/n, approved <date>, commit <hash>, journal ledger/journal-inc<k>-step<n>.md
Property tests (step 6): <library + version>, runs <n>, seed <n>; per aggregate — properties <n> for INV <ids>, failed on first run <ids> → counterexamples kept as tests <n>, candidate examples for step 3 <n>
Generated from specs (steps 9–10): <OpenAPI generator + version, mode>, <AsyncAPI generator + version>, output folder, compiled y/n, warnings
Test infrastructure (steps 7–12): database <Testcontainers / local server / user ran it>, broker <real in step 12: which>
Contract runs (step 12): <tool + version>, seed <n>, <cases> cases, findings <n> → <defect: step / spec: step 4 / documented exclusion>
Intent coverage (step 12): gaps <ids or none>, rules without a domain-level test <ids or none>; user stories done <ids>, partial <ids: missing examples>, planned <ids → increment>
Sent back: <finding> → step <n> (revision r<k>)
Review (step 13): per increment — <agent / fresh session / waived>, inspectors <n> (model, focus), defects major <n> minor <n>; accepted <n> → step <n>, rejected <n> (reasons), deferred <n>; re-inspection <y/n, by new agent>; report reviews/increment-<k>-inspection.md
Security review (step 14): per increment — <agent / fresh session / waived>, model <name>; tools <audit, secret scan, static analysis, dynamic scan + versions>; categories with verdict <n>/<n>; findings critical <n> high <n> medium <n> low <n>; fixed <n> (abuse-case tests), accepted by ADR <ids>, deferred <n>; report reviews/increment-<k>-security.md
Reverse check: <after fix r<k> (scoped) / step 15 (full)> — <mechanical: script path / by reading>, checked names <n>, columns <n>, enums <n>, states <n>, story and example IDs <n>; drifted <n> → <document corrected: step / defect: step>; open <ids or none>; unverifiable claims removed or made verifiable <n>

| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
| 2 | — | Design brief | graph | graph.ttl (ctx Lending, B1, B2) | event-storming-detail-interpreter | y | y | 2026-10-23 | 2 |
| 3 | — | Invariants and acceptance scenarios | upload + EXACT session | example-mapping/B1.md, B2.md (B2 not ready: 2 red cards → inc. 2) | example-mapping-interpreter; EXACT example-mapping | y | y | 2026-10-23 | 2 |
| — | 0 — Walking skeleton (US-01, B1-S01) | Readiness gate | reuse 3 | example-mapping/B1.md (US-01) | — | y | y | 2026-10-24 | 0 |
| 6 | 0 — Walking skeleton (US-01, B1-S01) | Domain model | reuse 2,3,4 | src/lending/domain | aggregate-behavior-author | y | y | 2026-10-24 | 0 |
| 9 | 0 — Walking skeleton (US-01, B1-S01) | Inbound adapters | reuse 4,5,8 | src/lending/infrastructure/http | generator: openapi-typescript 7.13.0; inbound-adapter-author | y | y (r2: 405 Allow, from step 12) | 2026-10-25 | 0 |
| — | 1 — Board: Lending | Readiness gate | reuse 3 | example-mapping/B1.md | — | y | y | 2026-10-26 | 0 |
| 6 | 1 — Board: Lending | Domain model | reuse 2,3,4 | src/lending/domain | aggregate-behavior-author | y | y | 2026-10-26 | 1 |
| 10 | 1 — Board: Lending | Outbound messaging | — | — | deferred | — | — | 2026-10-26 | 3 |
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
