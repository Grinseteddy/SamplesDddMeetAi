---
name: "read-model-author"
description: "Build one bounded context's query side test-first with EXACT Coding: projections from domain events into read models and the spec's GET operations with filters and paging."
author: Annegret Junker
---

# Read Model Author

Builds the query side of one bounded context: read models fed by the
context's events, and the GET operations that serve them. Written with
**EXACT Coding** — Predictive TDD v1 by Ferdi Ade and Marco Emrich
(github.com/marcoemrich/EXACT-Coding-Exercises, MIT). EXACT supplies the
method; this skill supplies **what the projection and query test lists must
cover** and **what shape** they must have.

A read model **answers a question an actor asks to decide something** (the
green sticky); it is shaped by that question, not by the aggregate. The query
side reads; it never loads an aggregate and never changes one.

Works inside **blueprint-service-implementation** (step 11), which hands
over the inputs and owns the step checkpoint. It also works on its own; then
ask for the inputs one at a time.

## Inputs

| Input | Required | What is used from it |
|---|---|---|
| The **read models** of the design brief (step 2): green stickies, or Event Model state views with their wireframes | yes | the question each answers, its fields, the events that feed it |
| The **GET operations** of the approved OpenAPI spec (step 4) | yes | paths, filters, paging parameters, response schemas |
| The **domain events** (step 6) and the **outbox** (step 7) | yes | the event union every projection decision is checked against; the event history projections read |
| Messages **received from other contexts** (step 9) | if a read model combines contexts | the consumer that feeds the projection |
| **ADRs** (step 5) | yes | consistency (same transaction or eventual), staleness accepted, read store, history retention for rebuilds, failure handling, paging style |
| An **approved inactive test list** | optional | use it as is |

Missing a required input: say which, and stop for it. A GET operation no
read model serves, or a read model no operation exposes, goes **back to
step 4** (or step 2). A skipped ADR this layer needs: ask it now; if skipped
again, use the defaults below and report them as open risks.

**Defaults when an ADR stays open:** eventual consistency from the
context's own outbox, polled; read models in the same database as separate
tables owned by the projection; progress recorded **per outbox row** per
projection; one runner per projection; a failing row stops the run and is
retried on the next (projections are deterministic — a failure is a bug or
an outage, not data to skip); outbox rows are kept while any projection may
need a rebuild; keyset paging with opaque cursors.

## 0. Locate EXACT Coding

Use the first that works:
1. **Available skills** `exact-coding`, `test-list`, `predictive-tdd` (Skill tool);
2. **the project's** `.claude/skills/exact-coding/SKILL.md`;
3. **vendor** them into the project — ask first; clone
   `https://github.com/marcoemrich/EXACT-Coding-Exercises`, check out
   `88fb3f9` (or the commit the user names), copy `.claude/skills/` and the
   `LICENSE` as `.claude/skills/EXACT-LICENSE`.

Read `exact-coding`, `test-list`, `predictive-tdd`, the stack profile and
`human-in-the-loop.md` before the first test; their rules are binding. Use
the autonomy level the run already set. If EXACT Coding cannot be located,
use the fallback in §9 and say so.

## 1. Check the slice

Show, before any test: each read model → the question it answers and who
asks it → its fields → the events that change each field → the GET
operations that expose it, with their filters and paging; the consistency
choice and what the actor sees right after a command. Flag and ask about:
- a field no event can fill, or an event field the question does not need
  (it stays out of the read model);
- a filter or sort order in the spec the read model's table cannot serve
  with an index;
- a read model that needs data from another context — it is fed by that
  context's messages (step 9 consumer), never by a join across databases;
- **read-your-writes**: an actor who must see their own change at once
  (the wiring test will show the gap with eventual consistency) — choose in
  the ADR: project in the command's transaction, return the new state from
  the command, or let the client wait or poll;
- an outbox whose rows are deleted after publishing — rebuilds then need an
  archive or event store (ADR).

## 2. Units

One unit per **projection** (against the real database from step 7) and
one per **query operation group** (in-process HTTP against the real
framework and the real query adapter on the same database — the SQL is the
logic here). The projector, shared by all projections, is built with the
first projection's tests.

## 3. Test lists — EXACT `test-list`, with query-side coverage

Inactive tests, ordered simple → complex, expected values in the
description, the trace after `--` (`-- B1-S05 US-08 RM copy availability, evt
CopyBorrowed`, `-- B1-S05 US-08 op listCopies`). Treat **events, read-model fields, filters, paging and
failure modes** as EXACT's independent dimensions.

**Per projection**
1. per event that changes the read model: the expected rows after projecting
   it (the first event creates the row; later ones update it);
2. a second run applies nothing again; each outbox row is processed once;
3. **no gaps**: a row that commits after a later row was already projected
   is still projected (hold a transaction open while a later row commits);
4. an event type the projection has no decision for fails the run, nothing
   of that row applied — never a silent skip;
5. a failing row: nothing of it applied, the run stops, the next run applies
   it once it succeeds; the rows after it wait;
6. two runs at once project every row once;
7. **rebuild**: damage the read model, rebuild from history, get the
   original back (a rebuild that does nothing must fail this test).

**Per query operation**
1. per operation: the spec's status and body from the read model, fields
   mapped to the response schema (absent fields left out, not `null`, unless
   the spec says nullable);
2. single resource not found → 404 Problem;
3. per filter in the spec: only matching rows, in the spec's order;
4. paging: a page of `limit` items with a `next` cursor; following it gives
   the rest and no `next` on the last page;
5. an invalid filter value (enum, range) → 400 Problem from the spec;
6. a cursor the API did not issue → 400 Problem, not 500.

**Gates**: the step 9 coverage gate fails until every new GET operation is
wired (expect it to fail the moment the spec gains them); every domain event
has a decision in every projection (compile time where the language allows);
one wiring test: command over HTTP → projector run → the query shows the
change.

Apply EXACT's Test-List checkpoint per unit. A behavior found on the way is
added as a new inactive test and activated in turn. The approved list is
the scope.

## 4. Cycles — EXACT `exact-coding`

Run EXACT's Predictive TDD cycles as written — one behavior per activation,
Green and Refactor as separate steps. Scaffold types and functions first so
the Red is behavioral. Predict the **route** too: a query test against an
operation with no controller yet fails as an unmapped error (500), not as an
empty result.

Expect guard items (second run, no gaps, failing row waits, invalid enum)
to be green on activation; keep them. **Boundary trials:** replace per-row
progress with a highest-seen-ID checkpoint and predict that the "no gaps"
item fails (it does — the late row is lost forever); remove the runner lock
and predict duplicate-key errors.

In EXACT's domain-responsibility review, the signals at this layer are a
**business rule in a projection** (a status computed from several fields, a
threshold) — it belongs in the domain and travels as an event field — and a
query that **loads aggregates** or joins the write tables — it reads the read model instead.

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

## 5. What the finished query side shows

- **Read models** as plain tables (or documents) owned by their projection,
  shaped by the question, indexed for the spec's filters and order. Nothing
  but the projection writes them.
- **Projections** with one decision per domain event — handle or `ignore` —
  in one table checked against the event union; an undecided type fails
  loudly (`UnhandledEvent`). Each handler is a small idempotent write keyed
  by the aggregate ID.
- **Projector**, shared by all projections: reads unprocessed outbox rows in
  ID order; per row, in one transaction, applies it and records it as
  processed for this projection; stops on the first failure; one runner per
  projection (a lock keyed by the projection's name, a concurrent run
  returns at once); `rebuild()` waits for the lock, resets the read model and
  its progress in one transaction and replays. Progress is **per row**, so
  a transaction that commits late is never skipped; order per aggregate
  holds because an aggregate's saves are serialized by optimistic locking
  (step 7), so its outbox rows commit in ID order.
- **Same-transaction alternative** (ADR): the repository applies the
  projection handlers in the save transaction — no lag, no projector, but
  write and read model fail together; rebuild still replays history.
- **Read models across contexts**: fed by the step 9 consumer of the other
  context's messages, idempotent by message ID, through the same handlers.
- **Query port** in the application layer: plain read types of domain IDs
  and values, `get` and `list(query)` returning a page; no aggregate, no
  repository, no SQL in its signature. The adapter implements it with the
  read store's query language.
- **Controllers** typed against the generated operation contract, calling
  the query port directly (no use case needed for a read), mapping read
  types to the generated response schemas. Query parameters arrive
  validated, coerced and defaulted by the spec — no default repeated in
  code.
- **Keyset paging**: the adapter fetches `limit + 1` rows after the cursor
  key to know whether a next page exists; the HTTP mapper turns the last key
  into an **opaque** cursor and rejects any cursor it did not issue
  (`InvalidCursor` → 400 in the step 9 error map).
- **Trace comments**: `// RM: Copy availability · evt: CopyBorrowed`,
  `// op: listCopies · RM: Copy availability`.

## 6. Finish

1. EXACT's completion: all tests active, all profile gates green,
   behavioral mismatches explained; the fast and slow suites green. Run projection tests more than once.
2. Checks, shown with output:
    - the step 9 coverage gate is green with the new operations;
    - leaks: TypeScript
      `grep -rnE "generated/|@nestjs|express|\bpg\b|from ['\"][./]*.*infrastructure/" src/*/domain src/*/application`;
      Java/Kotlin
      `grep -rnE "\.generated\.|\.infrastructure\.|org\.springframework\.(web|jdbc|data)" src/main/*/**/{domain,application}`;
    - no query loads an aggregate: no repository port in the query
      controllers or the query adapter;
    - list every `if` in projections, projector, query adapter and mappers and
      say what it decides.
3. **Wiring test**: a command over HTTP, one projector run, then the query
   shows the change — and, with eventual consistency, the same query before
   the run does not (so the lag is visible, not assumed away).
4. Report: EXACT source and autonomy level; read models → questions →
   events → operations; consistency and staleness with the ADR; retention
   and rebuild strategy; tests active/total per unit; behavioral prediction mismatches;
   boundary trials; open risks (progress table growth, rebuild downtime —
   readers see a partial view during a rebuild unless it builds into a
   shadow table and swaps); files created.
5. Return to the caller's unit checkpoint, or ask the user to approve.

## 7. Stack idioms

### TypeScript + NestJS (default)

- Folders: `src/<context>/infrastructure/read-model/` for projector,
  projections and query adapter; `src/<context>/application/queries/` for
  the query port and read types; controllers next to step 9's in
  `infrastructure/http/`.
- Projection decisions:
  `const HANDLERS = { CopyRegistered: …, CopyBorrowed: … } satisfies Record<CopyEvent['type'], Handler | 'ignore'>;`
  `Projection = { name; apply(db, record); reset(db) }`.
- Projector with `pg`: one pooled client per run; one-runner lock
  `pg_try_advisory_lock(hashtext(name))` (session level — unlock before
  `release()`, always in `finally`); rebuild with the blocking
  `pg_advisory_lock`. Unprocessed rows by
  `NOT EXISTS (SELECT 1 FROM projection_processed p WHERE p.projection = $1 AND p.outbox_id = o.id)`;
  progress table `projection_processed(projection, outbox_id)` with that
  primary key, created by a step-7-style migration. `BEGIN` … `COMMIT` per
  row, `ROLLBACK` and rethrow on error — never release a client inside an
  open transaction.
- Query adapter: `WHERE ($1::text IS NULL OR status = $1) AND ($2::text IS NULL OR copy_id > $2) ORDER BY copy_id LIMIT $3`
  with `limit + 1`. Cursors with `Buffer.from(id).toString('base64url')`;
  decode, re-encode and compare to reject foreign cursors.
- `express-openapi-validator` coerces query parameters and applies the
  spec's defaults (checked on Express 5); type the controller's `@Query()`
  from `operations[…]['parameters']['query']` with those fields required.
- Injection tokens in their own `tokens.ts`: a controller importing its
  token from the module that imports the controller is a cycle — the token
  is `undefined` when the decorator runs, and the test worker crashed
  (SIGABRT) instead of failing.
- Command-side tests get a query-port stand-in that throws on any call, so
  a command test that queries is a visible bug.
- Poller for the projector as a NestJS provider with `OnModuleInit` and
  `OnApplicationShutdown`, as in step 10.

### Java + Spring Boot (second option)

- Read models as tables written with `JdbcTemplate` (not JPA entities of the
  write side); projection decisions as an exhaustive `switch` over the
  `sealed` event interface; projector with `TransactionTemplate` per row and
  `pg_try_advisory_lock`, scheduled with `@Scheduled(fixedDelay=…)`.
- Query port as an interface in the application package returning
  `record`s; controllers implement the generated `…Api` interfaces (step 9),
  so a new GET operation fails compilation until wired. Keyset paging with
  `Base64.getUrlEncoder()` cursors.
- Tests with Testcontainers PostgreSQL and `@SpringBootTest` + MockMvc for
  the query operations.

### Kotlin + Spring Boot (third option)

- As Java; decisions as an exhaustive `when`; read types as `data class`es.
  EXACT has no Kotlin profile — use the Java profile adapted to Gradle, and
  say so.

## 8. Never

- Load or change an aggregate on the query side, or query the write tables.
- Let anything but its projection write a read model.
- Skip an event silently, or advance progress past a row that failed.
- Track progress by a highest-seen ID when rows can commit out of ID order.
- Join across contexts' databases; feed cross-context read models from
  messages.
- Repeat a spec default in code, or expose the cursor's inner format.
- Report a test result that was not run, or claim read-your-writes the
  wiring test did not show.

## 9. Fallback (only if EXACT Coding cannot be located)

The same test lists. Per behavior: predict, Red for the predicted reason,
least code to Green with the unit's tests and the fast suite (slow
suite and gates at the finish), refactor under the Four Rules with
§5 kept; show and ask before the next behavior.