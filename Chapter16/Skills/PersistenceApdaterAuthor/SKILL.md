---
name: "persistence-adapter-author"
description: "Build ONE bounded context's persistence with EXACT Coding against a real database: repository ports, adapters, migrations, optimistic locking, transactional outbox. TypeScript by default, Java or Kotlin on request."
author: Annegret Junker
---

# Persistence Adapter Author

Builds the outbound persistence of one bounded context, one aggregate at a
time, with **EXACT Coding** — Predictive TDD v1 by Ferdi Ade and Marco
Emrich (github.com/marcoemrich/EXACT-Coding-Exercises, MIT). EXACT Coding
supplies the method; this skill supplies **what the persistence test lists
must cover** and **what shape** ports, adapters, schema and outbox must have.

Works inside **blueprint-service-implementation** (step 7), which hands over
the inputs and owns the step checkpoint. It also works on its own; then ask
for the inputs one at a time.

## Inputs

| Input | Required | What is used from it |
|---|---|---|
| The **aggregates** from step 6 (code and tests) | yes | state fields, value objects, typed IDs, `version`, `reconstitute`, `pullEvents` |
| **Persistence ADRs** (step 5) | yes | persistence style (state-stored / event store), database, ORM or query library, migration tool, outbox yes/no, ID strategy |
| The **invariants sheet** | yes | rules that must also hold **across** aggregates of one type (uniqueness) — the only rules the database enforces |
| The **domain events** and their **AsyncAPI** messages | if outbox | what the outbox row must carry for step 10 |
| **Principles in scope** | optional | e.g. "no shared database between contexts", "no foreign keys across contexts" |
| An **approved inactive test list** (test file) | optional | if the caller hands one over, use it as is |

Missing a required input: say which, and stop for it. A persistence ADR that
was skipped in step 5: ask it now; if skipped again, say what you will do
instead (default below) and report it as an open risk — never choose
silently.

**Defaults when an ADR stays open:** state-stored, PostgreSQL, one table per
aggregate root, forward-only SQL migrations, outbox **on**, optimistic
locking on a `version` column.

## 0. Locate EXACT Coding

Same as the other blueprint skills. Use the first that works:
1. **Available skills** `exact-coding`, `test-list`, `predictive-tdd` (Skill tool);
2. **the project's** `.claude/skills/exact-coding/SKILL.md`;
3. **vendor** them into the project — ask first; clone
   `https://github.com/marcoemrich/EXACT-Coding-Exercises`, check out
   `88fb3f9` (or the commit the user names), copy `.claude/skills/` and the
   `LICENSE` as `.claude/skills/EXACT-LICENSE`.

Read `exact-coding`, `test-list`, `predictive-tdd`, the stack profile and
`human-in-the-loop.md` before the first test; their rules are binding. Use
the autonomy level the run already set (ask once if standalone). If EXACT
Coding cannot be located, use the fallback in §8 and say so.

## 1. A real database for the tests

Persistence tests run against the **same database engine** as production.
Find the first that works and show which one you use:

1. **Testcontainers** — if a Docker daemon is reachable (`docker info`):
   TypeScript `@testcontainers/postgresql`, Java/Kotlin
   `org.testcontainers:postgresql` with JUnit 5.
2. **A local server** of the same engine — e.g. PostgreSQL started for the
   test run on a free port and a throw-away data directory. PostgreSQL
   refuses to run as root: if the shell is root, start it as the `postgres`
   user, with data and socket directories that user can read and write
   (e.g. under `/var/tmp`, not inside a private home or temp folder).
3. **Ask the user** to run the tests where a database is available, and stop
   claiming results until they have.

An in-memory imitation (`pg-mem`, H2 in compatibility mode, SQLite for a
PostgreSQL target) is used **only** if the user explicitly accepts it; then
every report says the evidence is weaker because dialect, locking and
constraint behavior differ. Never switch silently.

Each test gets a clean schema: run all migrations on an empty database once
per test file, and truncate the aggregate's tables (and the outbox) between
tests — or wrap each test in a rolled-back transaction where the stack
supports it.

## 2. Units and order

One unit per aggregate, in the order step 6 built them. By default the
repository's `save` owns its transaction: it writes the aggregate's state
and appends its pending events to the outbox, then commits — or rolls both
back. That covers one aggregate per transaction without any further port.

Add a separate **unit-of-work** unit first only when an ADR asks for it, or
when a step 8 use case must write something besides the one aggregate in
the same transaction (an idempotency record for a consumed message, say).
Then `save` joins the unit of work's transaction instead of opening its
own.

## 3. Test lists — EXACT `test-list`, with persistence coverage

Each unit gets one integration test file with every behavior as an
**inactive test** (`it.todo`, `@Disabled`), ordered simple → complex,
expected values in the description, the trace after `--`, starting with
slice and user story (`-- B1-S03 US-07 INV-05`). The coverage
below is in addition to EXACT's own cross-check; treat **aggregate fields,
value objects, states, version, uniqueness rules and events** as the
independent dimensions.

**Unit of work** (only if added, see §2)
1. work done inside `transactionally(...)` is committed when it returns;
2. work is rolled back when it throws — nothing of it is visible afterwards;
3. a repository `save` called inside it joins its transaction.

**Repository, state-stored** (per aggregate)
1. the migration creates the aggregate's tables on an empty database;
2. `findById` of an unknown ID returns *absent* (null / `Optional.empty()`),
   not an error;
3. a saved new aggregate is found again with **every** field equal — one
   test per value object and per state the aggregate can be in;
4. a loaded aggregate has no pending events (reconstitution raised none);
5. saving a new aggregate stores version 1; saving a loaded one increments
   the stored version by exactly 1;
6. **optimistic locking**: two loads of the same aggregate, both changed,
   both saved — the second save fails with `ConcurrencyConflict` and the
   stored state is the first save's;
7. `save` appends each pending event to the outbox in the same
   transaction — one row per event with aggregate type, aggregate id, event
   type (= the AsyncAPI message name), the payload under the message's field
   names, `published_at` empty, and the delivery columns at their start
   values — `attempts` 0, `next_attempt_at` due at once, no `last_error`,
   `dead_lettered_at` empty;
8. a failed save (e.g. a conflict) leaves neither state nor outbox rows;
9. per **uniqueness rule** from the invariants sheet: a second aggregate
   violating it is rejected with the **domain** rejection named after the
   rule (the adapter translates the constraint violation; the rule ID
   travels with it);
10. per entity **inside** the aggregate (child rows): added, changed and
    removed children round-trip; no orphan rows remain;
11. finders other than `findById` only when a test of step 8 or 11 already
    asks for one — never speculatively.

**Repository, event-sourced** (if the ADR chose an event store) — replaces
the state-stored list:
1. appending the pending events of a new aggregate stores them as stream
   version 1..n;
2. loading replays the stream into an equal aggregate with no pending events;
3. appending with a stale expected version fails with `ConcurrencyConflict`
   and stores nothing;
4. events are stored under the AsyncAPI message names, with their payloads;
5. snapshots only if an ADR asks for them, with a test that a load from a
   snapshot plus later events equals a full replay.

Apply EXACT's Test-List checkpoint per unit. The approved list is the scope.

## 4. Cycles — EXACT `exact-coding`

Run EXACT Coding's Predictive TDD cycles as written. Persistence
adds failure categories to predict precisely: migration error, connection or
container start failure, constraint violation, serialization error, wrong
row count. A container or connection failure is **not** a behavioral Red —
fix the setup and run again. In EXACT's domain-responsibility review, the
seams to look for are: domain ↔ persistence model mapping, SQL/ORM access,
transaction handling, and constraint translation — each its own named unit.

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

## 5. What the finished persistence shows

- **Port in domain terms.** `CopyRepository` with `findById(CopyId)` and
  `save(Copy)` (plus finders tests asked for), declared next to the domain
  or in the application layer — domain types in, domain types out. No ORM,
  SQL, connection or transaction types in its signature.
- **Adapter in infrastructure**, implementing the port. A separate
  **persistence model** (row type or ORM entity) and a **mapper** in both
  directions; loading always goes through `Aggregate.reconstitute(...)`.
  ORM decorators or annotations never appear on domain classes.
- **Optimistic locking by compare-and-set.** Update with
  `WHERE id = :id AND version = :loadedVersion`, set `version = version + 1`;
  zero affected rows → `ConcurrencyConflict`. The aggregate never increments
  its version itself.
- **Errors of the port live with the port.** `ConcurrencyConflict` is part of
  the repository contract, so it is declared **next to the port** (the
  domain's shared folder, or the application layer if the port lives
  there) — never in the adapter. Every caller of the port catches or maps it
  (step 8 lets it propagate, step 9 maps it to 409, test fakes throw it)
  without importing infrastructure; the adapter imports it, never the
  reverse. The same holds for any other error a port declares.
- **One aggregate per transaction** in `save`. Cross-aggregate consistency is
  a policy (step 8), not a join or a multi-aggregate save.
- **Schema.** One table per aggregate root; child tables for entities inside
  the aggregate with a foreign key to the root; value objects as columns (or
  JSON where the ADR says so). References to **other** aggregates are plain
  ID columns with an index — no foreign key across aggregates, never across
  contexts. snake_case names; a unique constraint only where an invariant
  demands one, named after the rule (`uq_book_isbn__inv_07`).
- **Migrations**: versioned, forward-only, one per change; an applied
  migration is never edited. Each runs in its own transaction where the
  database supports it.
- **Outbox table**, complete from the first migration so step 10 needs no
  schema change: `id` (ascending, e.g. `bigserial` — the relay's order),
  `aggregate_type`, `aggregate_id`, `event_type`, `payload`, `occurred_at`,
  `published_at` (null until step 10 publishes), and the delivery columns
  step 10 owns: `attempts` (default 0), `next_attempt_at` (default
  `'-infinity'`, i.e. due at once), `last_error` (null), `dead_lettered_at`
  (null). One partial index for the relay's claim query:
  `(aggregate_id, id) WHERE published_at IS NULL AND dead_lettered_at IS NULL`.
  Written only inside the transaction that saves the aggregate; the
  repository sets only the event columns and leaves the delivery columns at
  their defaults.
- **Constraint translation**: the adapter turns the database's unique
  violation for a named constraint into the domain rejection for that rule;
  every other database error passes through unchanged.
- **Principles in scope** checked at the end (e.g. no table shared with
  another context).

## 5a. Finish

1. EXACT's completion: all tests active, all profile gates green,
   behavioral mismatches explained; the fast and slow suites green.
2. Leak checks, shown with output:
    - the domain does not import infrastructure:
      TypeScript `grep -rnE "from ['\"](pg|typeorm|@prisma|drizzle-orm|mikro-orm|knex)" src/*/domain`;
      Java/Kotlin `grep -rnE "jakarta\.persistence|javax\.persistence|org\.springframework\.(data|jdbc)|org\.jooq" src/main/*/**/domain`;
    - the port's errors and types are reachable without infrastructure —
      domain, application, the repository fakes and the shared contract
      import nothing from it:
      TypeScript `grep -rnE "from ['\"][./]*.*infrastructure/" src/*/domain src/*/application test/fakes/*-repository.ts test/contracts`;
      Java/Kotlin `grep -rnE "import .*\.infrastructure\." src/main/*/**/{domain,application} src/test/*/**/fakes/*Repository*`;
    - migrations were not edited after they first ran (git diff on the
      migrations folder, if the project is a repository).
3. Report: database used for the tests (Testcontainers / local server / user
   ran them / in-memory **accepted by the user**); EXACT source and autonomy
   level; tests active/total per unit; behavioral prediction mismatches; tables,
   constraints (with rule IDs) and migrations created; outbox on/off; open
   risks (skipped ADRs, defaults used); files created.
4. Return to the caller's unit checkpoint, or ask the user to approve.

## 6. Stack idioms

### TypeScript (default)

- Query layer per the ADR: plain SQL with `pg`, Kysely, Drizzle, TypeORM,
  MikroORM or Prisma. With an ORM, its entities live in
  `src/<context>/infrastructure/persistence/` — never the domain classes.
- Migrations with the tool the ADR names (plain SQL files with a small
  runner, `node-pg-migrate`, Drizzle Kit, TypeORM or Prisma migrations).
- Ports as TypeScript `interface`s returning `Promise<Copy | undefined>`;
  `ConcurrencyConflict extends Error` next to the port, e.g.
  `src/<context>/domain/shared/concurrency-conflict.ts`; the adapter, the
  fakes and the HTTP error map import it from there.
- `save` takes a client from the pool, `BEGIN`, writes state and outbox,
  `COMMIT`, `ROLLBACK` on any error, and always releases the client. A unit
  of work, if added, is `transactionally<T>(work: (tx) => Promise<T>)`.
  NestJS wiring (providers, modules) comes in step 8, not here.
- Tests with Vitest: `beforeAll` creates a fresh test database and runs the
  migrations, `beforeEach` truncates the aggregate's tables and the outbox,
  `afterAll` closes the pool.
- Predicting Vitest output: a file whose tests are **all** `it.todo` is
  reported as *skipped*, not passed (`Test Files 1 skipped`, `Tests n todo`).
  A suite that only fails to import shows `Tests no tests`.
- Run the type check as its own gate (`tsc --noEmit`); Vitest strips types
  without checking them.

### Java + Spring Boot (second option)

- Spring Data JDBC or JPA per the ADR; persistence entities in
  `<base>.<context>.infrastructure.persistence`; Flyway migrations in
  `db/migration` (`V1__create_copy.sql`). The AI Unified Process
  `flyway-migration` skill can draft the SQL when the stack is Spring Boot +
  JPA; its output still enters only through a failing test.
- With JPA, prefer an explicit `version` column checked by the adapter, or
  `@Version` **on the persistence entity**; never on the domain class.
- Translate `DataIntegrityViolationException` for named constraints into the
  domain rejection; `OptimisticLockingFailureException` or zero updated rows
  into `ConcurrencyConflict` — an unchecked exception declared in the
  domain (or application) package next to the port, not in infrastructure.
- Tests: JUnit 5 with Testcontainers PostgreSQL; `@DataJdbcTest` or
  `@DataJpaTest` with the container, not an embedded H2.

### Kotlin + Spring Boot (third option)

- As Java; Exposed or jOOQ instead of Spring Data where the ADR says so.
  Persistence entities as Kotlin classes in the infrastructure package; the
  domain stays free of their annotations. EXACT has no Kotlin profile — use
  the Java profile adapted to Gradle, and say so.

## 7. Never

- Put ORM, SQL or transaction types into the domain or the port signatures.
- Let the database enforce a rule the invariants sheet does not name, or
  skip one it does name.
- Add a foreign key to another aggregate's or another context's table.
- Edit a migration that has already run.
- Save two aggregates in one repository call, or write the outbox outside
  the aggregate's transaction.
- Replace the real database with an imitation without the user's consent.
- Report a test result that was not run.

## 8. Fallback (only if EXACT Coding cannot be located)

The same inactive test lists. Per behavior: predict, Red for the predicted
reason, least code to Green with the unit's tests and the fast suite (slow
suite and gates at the finish), refactor under the Four
Rules with §5 kept; show and ask before the next behavior.