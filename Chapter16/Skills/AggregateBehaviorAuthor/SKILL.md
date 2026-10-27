---
name: "aggregate-behavior-author"
description: "Implement ONE aggregate's behavior with EXACT Coding (Predictive TDD) from DDD artifacts — commands, invariant guards, state machine, events, typed IDs. TypeScript by default, Java or Kotlin on request."
author: Annegret Junker
---

# Aggregate Behavior Author

Turns one aggregate from the design artifacts into tested domain code with
**EXACT Coding** — Predictive TDD v1 by Ferdi Ade and Marco Emrich
(github.com/marcoemrich/EXACT-Coding-Exercises, MIT). This skill does not
define its own TDD cycle. It adds what EXACT Coding leaves to the
specification: **which behaviors** an aggregate's test list must cover, and
**what shape** the finished aggregate must have.

Works inside **blueprint-service-implementation** (step 6), which hands over
the inputs and owns the step checkpoint. It also works on its own; then ask
for the inputs one at a time.

## Inputs

| Input | Required | What is used from it |
|---|---|---|
| The aggregate's slice of the **design brief** | yes | name, commands, events, states and allowed transitions, policies that touch it |
| The **invariants sheet** rows anchored to this aggregate | yes | rule IDs (`INV-nn`), Given/When/Then scenarios, the rejection each causes |
| **AsyncAPI** message schemas of the events it publishes | if it publishes | payload field names and types |
| **ADRs** (step 5) | yes | language, ID strategy, time source, error style; principles in scope |
| **Visual Glossary** naming guide | optional | entity vs value object, field names |
| An **approved inactive test list** (test file) | optional | if the caller hands one over, use it as is and skip §2 |

Missing a required input: say which, and stop for it. Never fill it in. An
ADR the code needs that was skipped (language, ID strategy, time source):
ask it now; if skipped again, use the stack default and report it as an
open risk.

## 0. Locate EXACT Coding

EXACT Coding is a set of skills: `exact-coding` (the workflow), `test-list`,
`predictive-tdd` with one profile per stack under `predictive-tdd/stacks/`,
the shared checkpoint file `exact-coding-shared/human-in-the-loop.md`, and
the single-phase controls `red`, `green` and `refactor`. Its files refer to
each other as `.claude/skills/...` relative to the project root. Find them in
this order and use the first that works:

1. **Available skills** — `exact-coding`, `test-list` and `predictive-tdd`
   are listed; load them with the Skill tool.
2. **The project** — `<project>/.claude/skills/exact-coding/SKILL.md` exists;
   read the files from there.
3. **Vendor them into the project** — ask first, then clone the repository at
   the pinned commit and copy its `.claude/skills/` folder (and `LICENSE`, as
   `.claude/skills/EXACT-LICENSE`) into the project:
   `git clone https://github.com/marcoemrich/EXACT-Coding-Exercises` →
   checkout `88fb3f9` (distribution 2026-09-21) or the commit the user names.
   Record the commit in the report.

If none works, say so once and use the **fallback cycle** in §7; record that
the aggregate was not built with EXACT Coding.

Read, in this order, before the first test: `exact-coding`, `test-list`,
`predictive-tdd`, the stack profile for the project
(`typescript-vitest.md`, `java-junit-maven.md`), and
`human-in-the-loop.md`. Their rules are binding as written; this skill
never relaxes them.

**Autonomy level.** EXACT Coding's checkpoint file holds the current level
(`full-hitl` by default: stops after Test List, Red, Refactor, and every
prediction mismatch). Ask the user once which level to use for this
aggregate; if it differs from the file, change the file's setting in the
project copy and say so.

## 1. Check the slice

Before the test list, show a one-screen summary: commands → events, states
and transitions, invariants with their scenario count, events and their
AsyncAPI messages. Flag and ask about:
- a command with no resulting event, or an event no command produces;
- an invariant with no command, or one that needs data from another
  aggregate (that is a policy, not a guard — it leaves this aggregate);
- an event payload field the aggregate cannot know;
- a state that no transition reaches, or a transition no command causes.

This summary is the **specification** EXACT Coding's `test-list` reads.

## 2. Test list — EXACT `test-list`, with DDD coverage

Run EXACT Coding's Test List phase: one test file for the aggregate, every
behavior as an **inactive test** (`it.todo` in Vitest, `@Disabled` in
JUnit 5), ordered simple → complex, expected values in each description, the
suite verified green with the inactive list. In addition, the list must cover
— each as its own inactive test:

1. creation, through the command that brings the aggregate into being, with
   its event;
2. the happy path of each further command, with the event it raises;
3. each guard: one test per Given/When/Then scenario and one per rejection,
   naming the typed rejection and its rule ID;
4. each forbidden transition (command in the wrong state → rejection);
5. each event's payload against its AsyncAPI message;
6. reconstitution from stored state raises no events and runs no guards;
7. the boundaries the scenarios name (the 5th and the 6th, not "many").

Put the trace at the end of each description, after `--`:
`it.todo('borrowing a copy on loan is rejected with CopyNotAvailable -- B1-S03 US-07 INV-03 ex.2')`.
In EXACT's coverage cross-check, treat **commands, invariants, states,
events and payload fields** as the independent dimensions: a command or an
invariant without a discriminating test means the list is not complete.

Apply EXACT's Test-List checkpoint. The approved list is the scope.

## 3. Cycles — EXACT `exact-coding`

Run EXACT Coding's Predictive TDD cycles as written: one behavior at
a time, a falsifiable prediction before every check, behavioral Red for the
predicted reason, minimal Green, Refactor under the Four Rules of Simple
Design with the domain-responsibility review and the boundary trial, with
the fast loop below (fast suite per cycle; slow suite and all gates at the
finish), checkpoints per the autonomy level. No phase commits; no hard resets.

**Fast loop.** This skill runs EXACT's cycle with the two adaptations
`blueprint-service-implementation` makes (the same in every step skill;
standalone, they apply too unless the user asks for EXACT's original):
- **Predictions cover behavior.** Before every check, say which tests fail
  or pass and why — the failing assertion or error you expect, and for a
  Red that it fails for the behavioral reason. Gates (type check, lint,
  format, architecture rules) are predicted only as pass or fail. Never
  predict test counts, lint totals, line numbers or durations; lint rules
  that report every function (cognitive complexity with threshold 0 as a
  "measurement") are off in the loop and reported once at the finish.
- **Fast suite per cycle, slow suite at the finish.** Each cycle runs the
  unit's own test file, the fast suite (tests without containers or
  network) and the type check. The slow suite (real database, broker, end
  to end) and every gate of the stack profile run at the finish and before
  every commit — twice in a row only for tests that touch concurrency or
  timing. Containers start once per test run and are reused.
- **Trials** (break the code, predict the failure, restore) only for a test
  that is green on activation and once per new gate.
- **Traces** start with the slice and the user story it serves:
  `-- B1-S03 US-07 …`; several user stories as `US-07,US-09`.
- **Walking skeleton (increment 0):** list and build only what the skeleton
  slice needs; everything else goes on the blueprint's planned list.

**Domain-responsibility review, applied to aggregates.** When EXACT asks
for the unit's responsibility in specification language and its independent
change axes, use the invariants sheet: each rule ID is a candidate axis. A
guard that grows into its own policy becomes a named domain predicate or
value object, not a branch inside a long command method. Rules that need a
second aggregate's state are not boundary candidates here — they leave the
aggregate (§4).

## 4. What the finished aggregate shows

EXACT drives the code there one behavior at a time; the Refactor reviews keep
it there. Check it at every Refactor checkpoint and at the end.

- **One method per command**, named with the command's verb in the
  ubiquitous language (`borrow`, not `setStatus` or `update`). No public
  setters; state changes only through command methods.
- **Creation** through a static factory named after the creating command
  (`Copy.register(...)`), which raises the creation event.
- **Guards before change.** A violated invariant raises a **typed
  rejection** named after the rule (`CopyNotAvailable`), carrying the rule
  ID (`INV-03`). Rejections are domain types, not HTTP or framework errors.
  This is the observable error contract EXACT's `test-list` asks for.
- **State machine.** A status value with the brief's states; each command
  checks the state it requires; forbidden transitions are rejections.
- **Events.** One type per event, named in the past tense as on the board,
  payload fields as in the AsyncAPI message (or a mapping listed in the
  report). Events are **recorded, not published**: a pending list the
  application service takes with `pullEvents()`.
- **Identity and version.** A typed ID per aggregate; references to other
  aggregates use **their** typed ID too (`MemberId`, not `string`). A
  `version` for optimistic locking that the aggregate never increments.
- **Reconstitution.** A second factory (`Copy.reconstitute(state)`) rebuilds
  from stored state without guards or events.
- **Time and IDs from outside.** A clock passed in, per the ADR; new IDs
  passed in or from an injected generator. No hidden `new Date()`,
  `Instant.now()` or `randomUUID()` in the domain.
- **Value objects** for concepts the glossary marks as such: immutable,
  validated on creation, equal by value.
- **Trace comments** on every command method, guard and event:
  `// cmd: BorrowCopy · INV-03 · evt: CopyBorrowed`.
- **Cross-aggregate rules stay out**: reported as a policy for step 8.
- **No framework** in the domain: no NestJS, Spring, ORM, HTTP or
  serialization types, decorators or annotations. Tests are pure.

## 5. Finish

1. EXACT's completion: every listed behavior executable, the fast and slow
   suites green, all gates of the stack profile green (type check,
   lint/PMD where configured, the complexity report), behavioral
   mismatches explained.
2. Leak check on the domain folder, shown with its output:
    - TypeScript: `grep -rnE "@nestjs|typeorm|@prisma|mongoose|express|Date\.now\(|new Date\(\)|randomUUID" src/*/domain`
    - Java/Kotlin: `grep -rnE "org\.springframework|jakarta\.persistence|javax\.persistence|Instant\.now\(|LocalDateTime\.now\(|UUID\.randomUUID" src/main/*/**/domain`
      Any hit becomes a new test-list item, or is justified by an ADR.
3. Report: EXACT source and commit, autonomy level; tests active/total;
   behavioral prediction mismatches and how they were explained; commands → methods,
   invariants → rejections, events → message mapping; boundary trials
   retained or undone; policies found for step 8; open questions and risks;
   files created.
4. Return to the caller's unit checkpoint or, standalone, ask the user to
   approve the aggregate. Committing is the user's call at this point.

## 6. Stack idioms

EXACT's stack profile governs test syntax, commands and gates. These are the
DDD shapes on top of it.

### TypeScript + Vitest (default) — EXACT profile `typescript-vitest`

- Folder `src/<context>/domain/<aggregate>/`; one file per type; tests in
  `*.spec.ts` next to the code; commands from `package.json`.
- Typed IDs as branded types:
  `export type CopyId = string & { readonly __brand: 'CopyId' };` with a
  validating `CopyId.of(value)`.
- Rejections: `class CopyNotAvailable extends DomainRejection` with
  `readonly rule = 'INV-03'`; `DomainRejection extends Error` in
  `src/<context>/domain/shared/`. If the ADR chose result types, return
  `Result<Ok, DomainRejection>` and assert on the value.
- Events as a discriminated union with `type` equal to the AsyncAPI message
  name; aggregate with `private constructor`, `static register`,
  `static reconstitute`, `private pending`, `pullEvents()`; `readonly`
  wherever possible; no `any`.

### Java + JUnit 5 + Maven — EXACT profile `java-junit-maven`

- Package `<base>.<context>.domain.<aggregate>`; tests mirrored, JUnit 5
  with `assertThrows` for rejections (AssertJ if the project has it).
- Typed IDs and value objects as `record`s validated in the compact
  constructor; rejections `final class … extends DomainRejection`
  (unchecked) with `rule()`; events as a `sealed interface` with `record`s.
- Aggregate `final class`, private constructor, static `register` and
  `reconstitute`, `List<CopyEvent> pullEvents()`; no Spring, JPA or
  Jackson annotations.

### Kotlin + JUnit 5 — no EXACT profile

EXACT ships no Kotlin profile. Use the Java profile's rules (`@Disabled`
inactive tests, Red/Green interpretation, gates) with the project's Gradle
or Maven commands, and say in the report that the profile was adapted.
Typed IDs as `@JvmInline value class`; value objects as `data class` with
`init` checks; rejections as subclasses of a `sealed class DomainRejection`;
events as a `sealed interface` of `data class`es; aggregate with a private
constructor and a `companion object` holding `register` and `reconstitute`.

## 7. Fallback cycle (only if EXACT Coding cannot be located)

Same test list (§2) in the test file as inactive tests. Per item: predict,
then Red for the predicted reason; least code to Green with the unit's tests
and the fast suite (slow suite and gates at the finish); refactor
under the Four Rules with §4 kept; show and ask before the next item. No
production code without a failing test; never change a test to make it pass
without asking; never report a result that was not run.

## Never

- Write production code no failing test asked for.
- Invent a command, rule, state, event or field the inputs do not contain.
- Put a framework, ORM, HTTP or serialization type into the domain.
- Publish events or call a repository from the aggregate.
- Reach into another aggregate's state to check a rule.
- Relax an EXACT Coding rule, or report a test result that was not run.