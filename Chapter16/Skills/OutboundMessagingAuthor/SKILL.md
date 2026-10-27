---
name: "outbound-messaging-author"
description: "Publish one bounded context's domain events per its approved AsyncAPI spec: generated message models, test-first mapping and an outbox relay (ordered, retried, dead-lettered) with EXACT Coding."
author: Annegret Junker
---

# Outbound Messaging Author

Builds the outbound side of one bounded context: from the outbox rows step 7
writes to the messages the AsyncAPI spec promises. Message models are
**generated** from the approved spec and never edited; everything else is
written with **EXACT Coding** — Predictive TDD v1 by Ferdi Ade and Marco
Emrich (github.com/marcoemrich/EXACT-Coding-Exercises, MIT). EXACT supplies
the method; this skill supplies **what the mapping and relay test lists must
cover** and **what shape** they must have.

The relay **delivers**; it decides nothing about the business. Which events
leave the context is a design decision recorded per event type, not a
default.

Works inside **blueprint-service-implementation** (step 10), which hands
over the inputs and owns the step checkpoint. It also works on its own; then
ask for the inputs one at a time.

## Inputs

| Input | Required | What is used from it |
|---|---|---|
| **AsyncAPI** spec, approved in step 4 | yes | **send** operations, channel addresses, message headers and payloads |
| The **domain events** from step 6 | yes | the event union every publish decision is checked against |
| The **outbox** from step 7 (table, columns, how events are stored) | yes | what the relay reads; whether the store is state-stored with an outbox or event-sourced |
| **ADRs** (step 5) | yes | which events are published, broker and client, delivery guarantee, order, retry and backoff, max attempts, dead-lettering, message identity |
| The step 9 **AsyncAPI contract** helper, if it exists | optional | reuse the parser and schema checks instead of writing them again |
| An **approved inactive test list** | optional | use it as is |

Missing a required input: say which, and stop for it. A spec that changes
here goes **back to step 4**. A skipped ADR this layer needs: ask it now; if
skipped again, use the defaults below and report them as open risks.

**Defaults when an ADR stays open:** at-least-once delivery; message ID
derived from the outbox row (`<context>-outbox-<id>`), stable across
retries, so consumers deduplicate on it; message key = aggregate ID; order
per aggregate; exponential backoff from 1 s, dead letter after 5 attempts;
a message that breaks the spec is dead-lettered at once (retrying cannot fix
it); a dead-lettered row **unblocks** its aggregate's later rows and is
reported; time from an injected clock.

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

Show, before any test: each domain event → its publish decision (send
operation and channel, or **internal**) → payload fields → message fields;
each send operation → the event that feeds it; the outbox columns. Flag and
ask about:
- a domain event with no decision, or a send operation no event feeds;
- a payload field the event does not carry (the mapping cannot invent it),
  or an event field that would leak internal state onto the wire;
- a message with no identity header, or no key for ordering (→ step 4);
- the outbox lacks a delivery column (`attempts`, `next_attempt_at`,
  `last_error`, `dead_lettered_at`) or the claim index — step 7 creates them
  with the outbox; in a project built before that, go back to step 7 and
  add them there as a new migration, test-first, never by editing an applied
  one;
- an inline (anonymous) payload schema — generators invent names; name it
  as a component schema (→ step 4).

## 2. Generate — no TDD, no hand edits

Message models (and producer interfaces, where the generator offers them)
for every **send** operation come from the same pinned generator as step 9,
run from the build into the generated folder, never edited. Build once and
show what was generated. What the generator does not produce (headers,
channel addresses, operation lists) is checked at runtime against the spec
(§5), not written by hand as if generated.

## 3. Test lists — EXACT `test-list`, with outbound coverage

Two units: the **event → message mapping** (pure) and the **relay**
(against the real database from step 7, with a fake broker behind the
publisher port). Inactive tests, ordered simple → complex, expected values
in the description, the trace after `--`, starting with slice and user story
(`-- B1-S03 US-07 op publishCopyBorrowed`). Treat **event types, send
operations, row outcomes (published / internal / retried / dead-lettered)
and concurrency** as EXACT's independent dimensions.

**Mapping**
1. per published event: the outbox record becomes a message on the spec's
   channel, with key, headers (message ID from the row) and payload —
   expected values written out;
2. the message validates against the spec's send schema, and one with a
   missing required field does not (the check really checks); where the
   schema uses `format` (`occurredAt` as `date-time`), one with a broken
   value does not either — proof that formats are checked at all;
3. per internal event: no message;
4. an event type with no decision fails with `UnmappedEvent` — never a
   silent "internal";
5. coverage: every send operation in the spec has a mapper.

**Relay**
1. one pending row → published once and marked published;
2. a published row is not sent again;
3. an internal row → marked done, nothing sent;
4. a broker failure → row pending, attempts + 1, the error, next attempt
   at now + backoff; not retried before that time;
5. after the next attempt time → published, **same message ID**;
6. the N-th failure → dead-lettered, not retried;
7. a message that breaks the spec → dead-lettered at once, nothing sent;
8. an `UnmappedEvent` row → dead-lettered, the batch goes on;
9. **order per aggregate**: a pending (failed, waiting) row blocks the later
   rows of its aggregate; other aggregates go on;
10. **drain**: an aggregate's next row goes out in the same run once its
    earlier row is done;
11. **several relays at once** publish every row exactly once — with a fake
    broker that delays, so the runs really overlap.

Apply EXACT's Test-List checkpoint per unit. A behavior found on the way is
added as a new inactive test and activated in turn. The approved list is
the scope.

## 4. Cycles — EXACT `exact-coding`

Run EXACT's Predictive TDD cycles as written — one behavior per activation,
Green and Refactor as separate steps. Scaffold a new function or type first
so the Red is behavioral. Expect guard items (no resend, same message ID on
retry) to be green on activation; keep them.

**Boundary trial for the concurrency item**: remove the row lock and
predict duplicates; then restore it. Know what the test proves: with
`FOR UPDATE` alone the database re-checks the claim after the lock wait, so
the test passes with or without `SKIP LOCKED` — it proves **locking**;
skipping only shows as throughput (relays do not wait on each other).

In EXACT's domain-responsibility review, the signals at this layer are a
**business rule in a mapper** (a field computed, an event filtered by its
values) — it belongs in the domain (step 6) or the spec (step 4) — and a
relay that grows broker-specific branches — they belong in the publisher adapter.

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

## 5. What the finished outbound layer shows

- **One publish decision per domain event** in one table, checked against
  the domain's event union at compile time where the language allows: a new
  event type fails the build until someone decides "message" or "internal".
- **Mappers** per send operation: stored event → generated payload model,
  field by field (a domain rename does not silently change the wire), key =
  aggregate ID, headers with the stable message ID and the occurrence time.
- **Send-side validation** against the spec's schema before publishing.
- **Publisher port** with one method, resolving only when the broker
  confirmed the message (Kafka `acks=all`, AMQP publisher confirms). The
  broker client lives in an adapter behind it.
- **Relay** that, per run, repeats claim rounds until nothing is claimed
  (capped): each round claims, in one transaction, the oldest pending row of
  each aggregate that is due, with `FOR UPDATE SKIP LOCKED`; publishes;
  marks published, retries with backoff, or dead-letters; commits.
  At-least-once: a crash after publishing and before commit republishes
  with the same message ID.
- **Scheduling** outside the relay: a poller (framework lifecycle hook or
  scheduler) calls the relay at an interval and waits for the current run on
  shutdown. Alternatives the ADR may choose instead of polling: database
  notifications (`LISTEN/NOTIFY`) to wake the poller, or change data capture
  (Debezium) reading the outbox — then the relay tests become CDC
  configuration tests.
- **Event-sourced stores**: the relay reads the event stream from a
  checkpoint instead of an outbox table; the same list applies with
  "checkpoint advanced" in place of "marked published".
- **Trace comments**: `// op: publishCopyBorrowed · channel lending.copy-borrowed.v1 · evt: CopyBorrowed`.

## 6. Finish

1. EXACT's completion: all tests active, all profile gates green,
   behavioral mismatches explained; the fast and slow suites green. Run the relay tests more than once — concurrency
   tests that pass once prove little.
2. Checks, shown with output:
    - regenerate from the spec and build;
    - the publish-decision gate: add a made-up event to the domain union,
      show the build fail, restore;
    - leaks: TypeScript
      `grep -rnE "generated/|@asyncapi|ajv|kafkajs|amqplib|\bpg\b" src/*/domain src/*/application`;
      Java/Kotlin
      `grep -rnE "\.generated\.|org\.springframework\.(kafka|amqp|cloud\.stream|jdbc)" src/main/*/**/{domain,application}`;
    - list every `if` in mappers and relay and say what it decides.
3. **Wiring test**: one command through HTTP (step 9), use case (8) and
   repository (7), then one relay run → exactly the promised message on the
   promised channel, and no pending rows left.
4. Report: EXACT source and autonomy level; generator and version; publish
   decisions per event; mappings; delivery guarantee, order, retry, dead
   letter with their ADRs; what was tested with a fake broker and is left
   for step 12 (the real broker client: confirms, keys and partitions,
   serialization); behavioral prediction mismatches; open risks; files created.
5. Return to the caller's unit checkpoint, or ask the user to approve.

## 7. Stack idioms

### TypeScript (default; NestJS for scheduling)

- Message models with `@asyncapi/modelina` (as in step 9); it generates
  payloads only. Headers, channel addresses and the send-operation list come
  from the spec at runtime: `@asyncapi/parser` →
  `operations().filterBySend()`, one `ajv` check per channel address
  (`strict: false`) **with `ajv-formats`** — without it `ajv` only warns
  "unknown format ignored" and a broken `date-time` is published. Share one
  `messageCheck(operation)` with step 9's receive side.
- Publish decisions:
  `export const EVENT_MESSAGES = { CopyRegistered: 'internal', CopyBorrowed: copyBorrowed } satisfies Record<CopyEvent['type'], EventMapper | 'internal'>;`
  with `EventMapper = { operation: string; map(r): OutgoingMessage }`, so the
  coverage test can list the operations served.
- Relay with `pg`: one pooled client per claim round, `BEGIN` … `COMMIT`,
  `ROLLBACK` and rethrow on unexpected errors, `release()` in `finally`. The
  claim query selects `id::text` (bigint IDs arrive as strings anyway; be
  explicit) and filters `next_attempt_at <= $now` with the injected clock,
  never `now()` in SQL. Step 7's claim index on `(aggregate_id, id)` for
  pending rows serves the "earlier row pending" check.
- Fake broker in `test/fakes/` with `failNext(n)` and an optional delay, so
  concurrent runs overlap. Run relay tests with `--no-file-parallelism` on a
  shared test database.
- Broker adapters behind the port: KafkaJS `producer.send` with
  `acks: -1`, idempotent producer, `key` = aggregate ID, headers from the
  message; `amqplib` confirm channel and `waitForConfirms`. Tested in step 12
  against a real broker (Testcontainers).
- Poller as a NestJS provider with `OnModuleInit` (start interval) and
  `OnApplicationShutdown` (stop, await the running pass).

### Java + Spring Boot (second option)

- Message classes from the step 9 generator (ZenWave SDK or an AsyncAPI
  Generator Java template), producer side.
- Publish decisions as an exhaustive `switch` over the `sealed` event
  interface (the compiler enforces completeness), returning the message or
  `Internal`.
- Relay with `JdbcTemplate` and `TransactionTemplate`, the same claim query
  with `FOR UPDATE SKIP LOCKED`; scheduled with `@Scheduled(fixedDelay=…)`
  (never `fixedRate`, so passes do not overlap in one instance). Publisher
  adapter with `KafkaTemplate.send(...).get()` (or Spring Cloud Stream
  `StreamBridge` with confirms). Tests with Testcontainers PostgreSQL and a
  fake publisher bean; Spring Modulith's event publication registry is an
  alternative to a hand-written outbox only if an ADR chose it.

### Kotlin + Spring Boot (third option)

- As Java; publish decisions as an exhaustive `when` over the `sealed`
  event hierarchy. EXACT has no Kotlin profile — use the Java profile adapted
  to Gradle, and say so.

## 8. Never

- Publish from the aggregate or the use case; only the relay publishes,
  from the outbox.
- Treat an undecided event type as internal, or publish one nobody decided
  to publish.
- Retry a message that breaks the spec, or drop a row silently.
- Change a message ID on retry, or let later rows of an aggregate overtake a
  pending one (unless an ADR gave up ordering).
- Edit generated message models, or change the spec without step 4.
- Claim a test result that was not run, or a broker behavior that was only
  tested against a fake.

## 9. Fallback (only if EXACT Coding cannot be located)

The same test lists. Per behavior: predict, Red for the predicted reason,
least code to Green with the unit's tests and the fast suite (slow
suite and gates at the finish), refactor under the Four Rules with
§5 kept; show and ask before the next behavior.