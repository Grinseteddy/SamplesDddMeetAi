---
name: "service-test-author"
description: "Test one bounded context as a running service: Gherkin acceptance from Example Mapping, OpenAPI/AsyncAPI contract tests, real broker adapters, intent-coverage report; EXACT Coding."
---

# Service Test Author

Tests what only the **running** service can show. Unit and integration tests
already exist from steps 6–11; this step adds acceptance tests from the step
3 scenarios, contract tests of the running service against both approved
specs, and contract tests of the real infrastructure adapters earlier steps
deferred (the broker client above all). Then it runs everything and reports
**coverage of intent**, not of lines.

Hand-written tests follow **EXACT Coding** — Predictive TDD v1 by Ferdi Ade
and Marco Emrich (github.com/marcoemrich/EXACT-Coding-Exercises, MIT): test
list first, one behavior per activation, a falsifiable prediction before
every run. Tool-generated tests (Schemathesis, spec-driven AsyncAPI checks)
are not written test-first, but every such check — and every new gate — is
**seen failing once** before its green counts.

A failing test is reported, never silently changed to pass. A defect found
here goes **back to the step that owns the code** (or the spec back to step
4) as a new test there, red first.

Works inside **blueprint-service-implementation** (step 12), which hands
over the inputs and owns the step checkpoint. It also works on its own; then
ask for the inputs one at a time.

## Inputs

| Input | Required | What is used from it |
|---|---|---|
| The **invariants sheet** and **Example Mapping** (step 3) | yes | rules, examples, policies — the acceptance scenarios and the coverage inventory |
| **OpenAPI** and **AsyncAPI** specs (step 4), approved | yes | operations, channels, message examples |
| The **code and tests** of steps 6–11 | yes | what exists, the trace convention (`--` in test titles) |
| **ADRs** (step 5) | yes | broker and its delivery semantics, deployment shape (one process, ports), test infrastructure |
| A **design brief** read model / journey list (step 2) | optional | journeys spanning several scenarios |
| An **approved inactive test list** | optional | use it as is |

Missing a required input: say which, and stop for it.

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

## 1. Real infrastructure for the tests

The service runs against the **same kinds** of infrastructure as production:
its database and its **broker**. Find the first that works and show which:
1. Testcontainers (Docker reachable) for database and broker;
2. local servers — the database as in step 7; the broker as a binary from
   its official release (NATS: one static binary from GitHub releases;
   Redpanda for Kafka; RabbitMQ needs Erlang) on a free port with a
   throw-away data directory;
3. ask the user to run the suite where the infrastructure exists.

A fake broker is **not** acceptable here — step 10 already used one; this
step exists to replace it. Never switch silently.

Each scenario and contract run starts from empty state: a fresh test
database (drop it **with force**, so sessions a stopped service left behind
cannot block it) and fresh streams, topics or queues the service does not
own. The service creates what it owns.

## 2. Check the slice

Show, before any test: the inventory — rules, examples, policies, HTTP
operations, send and receive operations, domain events — and what already
traces each (from the existing tests); the adapters the earlier steps
deferred to here (broker publisher, consumer binding, real stores behind
ports that only have fakes); the composition root, if one exists. Flag and
ask about:
- a port with **only a fake** (an idempotency store, a clock source) — the
  real adapter is missing; it is built test-first in its owning step,
  driven by the acceptance test that needs it;
- a receive operation with **no message example** in the AsyncAPI spec — the
  spec-driven contract test needs one (→ step 4);
- path or query parameters with no example — the OpenAPI contract tool will
  mostly hit 404s (warning "missing test data"); add examples (→ step 4);
- writes that must be atomic but are not (an idempotency record saved apart
  from the aggregate) — an open risk for step 7's unit of work.

## 3. Test lists — EXACT `test-list`

**A. Real adapters** (deferred from steps 9 and 10), against the real broker:
1. the publisher puts the message on the channel's address with its headers
   and payload;
2. `publish` resolves only after the broker confirmed; with nothing to store
   it (no stream, topic or exchange) it rejects;
3. the same message ID twice is stored once, where the broker deduplicates
   (NATS `Nats-Msg-Id`, Kafka idempotent producer) — else say consumers must;
4. the consumer binding hands a received message, raw, to the consumer and
   acks it; it is not redelivered;
5. an `InvalidMessage` is terminated (not redelivered) and copied with its
   reason to the dead-letter address;
6. any other error is redelivered until the consumer succeeds (and, per the
   ADR, dead-lettered after N deliveries — an unbounded retry is an open
   risk).

**B. Acceptance** — **one feature file per user story**
(`US-07-<slug>.feature`), its `Feature:` line the user story as written; one
`Rule:` per blue card and one Gherkin scenario per green example of the
board's Example Mapping, taken one to one — never written separately —
tagged with board, user story, slice and rule (`@B1 @US-07 @B1-S03 @INV-03
@ex.1`, policies as `@P-01`); undefined steps are the inactive state. An
example the running service cannot show is a finding for the user (missing
slice → step 1, spec gap → step 4, example to revise → step 3), never a
dropped scenario. Steps in the domain language, driven through a **driver**
that talks to the running service only through its contracts — HTTP
requests and messages on channel addresses — never through its internals or
tables (only the test's own setup touches the database, to reset it).
- each example's Given/When/Then end to end, including messages the context
  receives (Given: "Acquisition announced …" publishes on the upstream
  channel) and messages it publishes (Then: "Lending announced …" reads the
  channel);
- every message the driver reads is validated against the AsyncAPI spec
  before the scenario looks at it;
- asynchronous outcomes polled with a deadline (`eventually`), never slept
  for a fixed time.

**C. Contract, OpenAPI** — two parts:
- in the build (`verify`): an example-based check of the operations built
  so far against the spec (each documented example request answered as
  the spec says), fast enough for every push;
- as a **CI job** (pull request or schedule) with a time cap and a rate
  limit below the API's own: Schemathesis against the running service, all
  checks, a fixed seed, a JUnit report, limited by name to the operations
  built so far. Its latest result is read at the increment checkpoint.
  In the walking skeleton (increment 0) the job is not set up yet unless
  the user schedules it.
  Not test-first; triage every failure (§4).

**D. Contract, AsyncAPI**, generated from the spec, per **receive**
operation: the operation has at least one example; every example is
accepted (acked, nothing pending, nothing dead-lettered); the first example
with its first required payload field removed is dead-lettered. Per **send**
operation: covered by an acceptance scenario whose driver validates the
message (the coverage report checks it).

**E. Quality scenarios** (from step 5, verified by *test* or *both*, for
the operations built so far): short checks — architecture rules, spec
diffs, one request or one message — run in `verify`; load, fault
injection and anything longer than about a minute run as **CI jobs** with
time caps, never in a TDD cycle or in `verify`. A scenario's harness is
built in the first increment that measures it; in the walking skeleton only
scenarios that need no new tool. Each result names its environment and is
marked *indicative* where that differs from the scenario's.

**F. Fast and slow suites and intent coverage** — run both suites; build
the report (§5).

Apply EXACT's Test-List checkpoint per unit. A behavior found on the way is
added to the owning step's list as a new inactive test and activated there.

## 4. Cycles, triage and trials

- **Inside out, then outside in.** Build and prove the real adapters (A)
  first; then let the acceptance scenarios (B) drive the composition root —
  the first run fails because no server is listening, and each missing
  wire shows as the next failing step.
- **Every tool-generated check, every test green on activation and every
  new gate is seen failing once** (hand-written tests that went Red for the
  predicted reason need no extra trial). Boundary trials: remove the relay
  poller → the "Lending announced" step fails; add an untested rule to the
  inventory → the coverage gate fails. A helper that cannot fail makes a
  check vacuous: a dead-letter counter that always read 0 passed the
  "accepted" test until the "dead-lettered" test exposed it.
- **Triage of contract-tool findings**, each one written down:
    - *defect* (a 500 for a malformed body, a 405 without `Allow`) → a new
      failing test in the owning step, then the fix;
    - *spec gap or ambiguity* (an empty cursor the spec allowed) → step 4;
    - *a rule the spec cannot express* (a cursor must be one the API issued)
      → the narrowest exclusion in the tool's config, with a comment naming
      the rule and the service test that covers it instead. Never a blanket
      switch-off.

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

## 5. Intent coverage

A small script reads the test runners' JSON reports and the inventory, and
lists, per **user story, rule, example, policy, HTTP operation, send
operation, receive operation and domain event**: the passing active tests that trace it and
their **levels** (domain, application, adapter, contract, acceptance).
Traces are the words after `--` in a test title and a scenario's tags.
Inactive and failing tests do not count. It reports:
- per **user story**: *done* (every example of its Example Mapping passes),
  *partial* (which examples are missing and why) or *planned* (with the
  increment that builds it);
- **gaps** — an item with no passing test (exit code 1);
- rules and examples with **no domain-level test** — a rule proven only end
  to end is a step 6 gap; tests written now for existing code are
  *characterization tests*, labelled as such, not TDD evidence;
- failing and inactive tests, by title.

## 6. Finish

1. EXACT's completion for the hand-written tests; the example-based
   OpenAPI check and the AsyncAPI contract green; the latest Schemathesis
   CI run green or every failure triaged; the fast and slow suites green —
   tests that touch a broker or several processes run twice.
2. Report: infrastructure used (and what was not real); EXACT source and
   autonomy level; tests active/total per unit; acceptance scenarios
   passed/total, per user story done/partial/planned; contract runs (tool, version, seed, cases, failures and
   their triage); defects sent back, per owning step; spec changes for step
   4; adapters found missing and built; the intent-coverage table, gaps and
   domain-level gaps; open risks (atomicity, unbounded redelivery, missing
   test data); files created.
3. Hand over to step 13 (traceability): the coverage table and the trace
   convention are its input.
4. Return to the caller's checkpoint, or ask the user to approve.

## 7. Stack idioms

### TypeScript (default)

- **Acceptance** with `@cucumber/cucumber` (pin it), steps in TypeScript run
  through `tsx` (`NODE_OPTIONS="--import tsx" cucumber-js`), `strict: true`,
  a JSON report for the coverage script. A `World` holds the pool, broker
  connection, running service and driver; `Before` resets state and starts
  the service on port 0; `After` stops it.
- **Composition root** `startService(config) → { baseUrl, stop }` in
  `src/main.ts`: pool, repositories and stores, broker connection and the
  streams the service owns, one consumer binding per receive operation
  (subject read from the spec), relay and projector pollers, the HTTP app;
  `stop` closes in reverse order and awaits running passes. The process
  entry point and the tests both call it. Scripts with top-level `await`
  need the `.mts` extension under `tsx` in a CommonJS package.
- **NATS JetStream** client `nats` (pin it): publish with headers and
  `msgID` (broker-side dedupe within the stream's duplicate window); durable
  consumers with explicit ack, `ack_wait`, and the binding's outcome mapping
  `ack` / `term` + dead-letter publish / `nak(delay)`. Strip `Nats-*`
  transport headers before handing headers to the consumer. Per-subject
  message counts need `streams.info(name, { subjects_filter })`.
- **Schemathesis** (Python, pinned, `pip install`): `st run <openapi> --url <base> --seed <n> --max-examples <n> --report junit`.
  All checks are on by default; **do not pass `--checks all`** — it overrides
  the per-operation settings in `schemathesis.toml` (`[[operations]]`
  `include-operation-id`, `checks.<name>.enabled = false`).
- Findings from the trial, for step 9's error map: Nest wraps an unparsable
  JSON body in `BadRequestException` — map framework `HttpException`s by
  their status, not to 500; a 405 from `express-openapi-validator` carries
  the allowed methods in `error.headers.Allow` — send it, uppercased.
- `ajv` ignores `format` keywords (only warning) unless `ajv-formats` is
  added — add it wherever specs are checked at runtime (steps 9, 10).
- Start background services with a pidfile and stop them by that PID or by
  process name; `pkill -f <pattern>` can match the shell running it.

### Java + Spring Boot (second option)

- Acceptance with Cucumber JVM (`cucumber-junit-platform-engine`), steps
  through a driver using `RestClient` / `WebTestClient` and the broker
  client; `@SpringBootTest(webEnvironment = RANDOM_PORT)` as the running
  service; Testcontainers for PostgreSQL and the broker (Kafka, RabbitMQ,
  NATS).
- Schemathesis as above (language-independent), or Spring Cloud Contract /
  Specmatic if an ADR chose them; Microcks for AsyncAPI contract runs where
  Docker is available, else the spec-driven test of §3 D with the AsyncAPI
  Java parser or a JSON Schema validator.
- Coverage script reads the JUnit XML and Cucumber JSON reports.

### Kotlin + Spring Boot (third option)

- As Java; Cucumber JVM steps in Kotlin. EXACT has no Kotlin profile — use
  the Java profile adapted to Gradle, and say so.

## 8. Never

- Use a fake broker or an imitation database in this step without the
  user's consent.
- Let the acceptance driver read the service's tables or call its classes.
- Change a failing test to pass, or a spec to fit the code, without sending
  it back to its owning step.
- Switch a contract check off for an operation without naming the rule and
  the test that covers it instead.
- Count inactive or failing tests as coverage, or present characterization
  tests as TDD.
- Sleep for a fixed time instead of polling with a deadline.
- Report a result that was not run.

## 9. Fallback (only if EXACT Coding cannot be located)

The same test lists. Per behavior: predict, Red for the predicted reason,
least code to Green with the unit's tests and the fast suite (slow
suite and gates at the finish), refactor under the Four Rules;
show and ask before the next behavior.