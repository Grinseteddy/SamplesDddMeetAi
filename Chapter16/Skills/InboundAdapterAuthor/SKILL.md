---
name: "inbound-adapter-author"
description: "Generate one bounded context's controllers and message consumers from approved OpenAPI/AsyncAPI specs and wire them to the use cases test-first with EXACT Coding; TypeScript/NestJS, Java or Kotlin."
---

# Inbound Adapter Author

Builds the inbound layer of one bounded context. The approved specs are the
source: **generate what a generator can produce reliably, never hand-write
or edit it**, and write the wiring around it with **EXACT Coding** —
Predictive TDD v1 by Ferdi Ade and Marco Emrich
(github.com/marcoemrich/EXACT-Coding-Exercises, MIT). EXACT supplies the
method; this skill supplies the generation rules, **what the wiring test
lists must cover**, and **what shape** the adapters must have.

Adapters **translate**; they decide nothing. Every `if` in an adapter maps an
error or checks the contract.

Works inside **blueprint-service-implementation** (step 9), which hands over
the inputs and owns the step checkpoint. It also works on its own; then ask
for the inputs one at a time.

## Inputs

| Input | Required | What is used from it |
|---|---|---|
| **OpenAPI** spec, approved in step 4 | if the context has an HTTP API | operations, schemas, status codes, security |
| **AsyncAPI** spec, approved in step 4 | if the context receives messages | **receive** operations, message headers and payloads |
| The **use cases and policy handlers** from step 8 | yes | command objects, application errors, the rejections that pass through |
| **ADRs** (step 5) | yes | stack and generator choice, error map (error → status and problem type), retry, idempotency, dead-lettering, authentication |
| The **context map** (step 1) | if messages come from other contexts | which borders need an anticorruption mapping |
| An **approved inactive test list** | optional | use it as is |

Missing a required input: say which, and stop for it. A spec that changes
here goes **back to step 4**: show the change, get it approved, regenerate.
A skipped ADR this layer needs (generator, error map, dead-lettering): ask it
now; if skipped again, use the defaults below and report them as open risks.

**Defaults when an ADR stays open:** Problem Details (RFC 9457) as
`application/problem+json` with `type`, `title`, `status`, `detail` and, for
domain rejections, a `rule` extension carrying the rule ID; not found → 404,
domain rejection → 409, concurrency conflict → 409, unmapped error → 500
without internal detail; an invalid message is dead-lettered and not retried;
other consumer errors are retried by the transport.

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

Show, before generating: each OpenAPI operation → use case → the errors it
can raise → status and problem type from the error map; each AsyncAPI
**receive** operation → policy handler → its message ID header → whether it
crosses a context border (anticorruption mapping needed). Flag and ask about:
- an operation or receive operation no use case serves, or a use case no
  operation reaches;
- an error a use case can raise that the error map does not name, or a
  status the map uses that the spec does not declare for that operation;
- a spec with no `default` (or 5xx) Problem response — errors the adapter
  produces must be declared too (→ step 4);
- a received message with no identity field for idempotency (→ step 4);
- an inline (anonymous) payload or request schema: generators then invent
  names like `AnonymousSchema_3` — name it as a component schema (→ step 4).

## 2. Generate — no TDD, no hand edits

Code a pinned, reliable generator produces from the approved specs is not
written test-first and is never edited (blueprint: *Exception: code generated
from the approved specs*). It is checked by compiling, by the coverage gates
in §5 and by the step 12 contract tests.

- **Pin** each generator and its version in the build; run it **from the
  build** on every build (`generate` before `test` and `build`), into its
  own folder (`src/generated/`, `target/generated-sources/`). Never copy
  generated sources into the hand-written tree.
- A generator whose own dependencies clash with the project (a different
  compiler version, say) gets its own tool package (`tools/codegen/`) with
  its own pinned dependencies.
- Build once and show: generator and version, what was generated
  (operations, DTOs, message models), that it compiles, every warning.
- A construct the generator cannot express, or a name it invents, is fixed
  **in the spec** (→ step 4) or by a generator option — never in the output.
- **What to generate, per stack:** §7. Where a stack has no reliable
  generator for a part (controllers, consumer interfaces), generate the
  **types** and enforce the rest with a **runtime contract** (the spec
  validated at the edge) and a **coverage gate** (§5) — and say so in the
  report; never fill the gap with hand-written "generated" code.

## 3. Test lists — EXACT `test-list`, with adapter coverage

One unit per **operation group** (tag) and one per **receive operation**,
plus one **edge unit** for what the whole API shares. Inactive tests,
ordered simple → complex, expected values in the description, the trace
after `--` (`-- B1-S03 US-07 op borrowCopy`, `-- B2-S01 US-11 op
onCopyAcquired`, `-- B1-S03 US-07 ADR-014`).
Treat **operations, status codes, error types and message outcomes** as
EXACT's independent dimensions.

**Per HTTP operation** (in-process HTTP against the real framework, the use
cases from step 8 and their contract-checked fakes — no database)
1. happy path: the request reaches the right use case with the mapped
   command; the response has the spec's status, headers and body;
2. invalid request, from the spec: a missing required field, an undeclared
   field (if `additionalProperties: false`), an invalid enum value — each
   → 400 Problem, use case not called, nothing saved;
3. not found → 404 Problem;
4. each domain rejection the use case passes through → the error map's
   status and problem type, **with the rule ID** — one test per rejection;
5. concurrency conflict → the ADR's status (409, or 412 with `If-Match`);
6. per security requirement in the spec: no credentials → 401, an actor the
   use case refuses → 403;
7. an unexpected error → 500 Problem **without** the internal message.

**Edge unit** (once per API): a path the spec does not declare → 404, a
method it does not declare → 405 **with `Allow`** listing the methods the
spec declares for the path (RFC 9110), a content type it does not declare →
415, a body that is not valid JSON → 400 — each as a Problem, not as the
framework's default page and not as 500. A catch-all error handler turns
these into 500 unless they are mapped; test them. (The step 12 contract run
with Schemathesis found the missing `Allow` and the 500 for a malformed body
when these items were not on the list.)

**Per receive operation** (call the consumer with a raw message, as the
transport would hand it over)
1. a valid message → the policy handler receives the **mapped** input in
   its own language (anticorruption mapping where the context map marks a
   border: their field names and IDs → our typed IDs);
2. an invalid payload and an invalid header (each, from the spec) →
   `InvalidMessage`, nothing changed; where the schema uses `format`
   (`date-time`, `email`, `uuid`), one field breaking it — this proves the
   validator checks formats at all;
3. the same message twice → one effect (passes through the step 8 handler);
4. outcome → transport action, if the stack has a test harness for the
   binding: success → ack; `InvalidMessage` → dead letter; anything else →
   retry per the ADR. Without a harness, list it for step 12.

**Gates, as tests or compile checks** (§5): every OpenAPI operation is
served; every receive operation has a consumer; one wiring test through the
real adapters.

Apply EXACT's Test-List checkpoint per unit. A behavior found on the way
(an unmapped validator error, say) is added to the list as a new inactive
test and activated in turn. The approved list is the scope.

## 4. Cycles — EXACT `exact-coding`

Run EXACT's Predictive TDD cycles as written — **one behavior per
activation**. When the next item needs a type that does not exist yet,
scaffold it first so the Red is behavioral (status, body), not an import
error.

Expect **guard tests** to be green on activation — the validator already
rejects an undeclared field; the step 8 handler is already idempotent.
Keep them; they fail the day someone loosens the spec or the handler.

Before relying on a runtime validator for a check, **run a boundary trial**:
break the thing once (answer an undeclared status, drop a required response
field) and predict what the validator does. Validators often check bodies
but not bodyless responses; whatever they do not check, the tests assert
explicitly.

In EXACT's domain-responsibility review, the signal at this layer is a
**decision in an adapter** (an `if` on a field value, a default the spec
does not declare, a calculation). Move it into the domain (step 6) or the
spec (step 4); the adapter keeps only mapping.

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

## 5. What the finished inbound layer shows

- **Generated contract, hand-written wiring.** Controllers implement (or are
  typed against) the generated operation contract; consumers take generated
  message models. Mapping lives in small **mapper** functions next to them:
  DTO → command of domain types, result → response DTO, their message →
  our input.
- **The spec is enforced at the edge**: requests and received messages are
  validated against the spec at runtime before any mapping; responses are
  validated against the spec in tests where the stack allows.
- **One error map** per API, in one place, from the ADR: each application
  error and domain rejection → status, problem type, title, and `rule` for
  rejections; spec-validation errors (400, 404, 405, 415) mapped too, the
  405 with its `Allow` header; the **framework's own HTTP errors** (an
  unparsable body, say) keep their 4xx status; only what is left → 500
  without detail.
- **Coverage gates**: a new operation in the spec fails the build until it is
  wired — at compile time where the generated types allow it (a map keyed by
  operation ID that must be complete), otherwise a test that reads the spec
  and compares it with the registered controllers or consumers.
- **No generated type below the adapter**: domain and application import
  nothing from the generated folder, the web framework, the broker client or
  the validator.
- **Trace comments**: `// op: borrowCopy (lending.openapi.yaml) · cmd: BorrowCopy`,
  `// op: onCopyAcquired (channel acquisition.copy-acquired.v1) · policy: P-01`.

## 6. Finish

1. EXACT's completion: all tests active, all profile gates green,
   behavioral mismatches explained; the fast and slow suites green.
2. Checks, shown with output:
    - regenerate from the specs and build — the generated folder is
      reproducible and untouched;
    - coverage gates green; trial one by adding a made-up operation to a copy
      of the spec and showing the gate fail, then restore;
    - leaks: TypeScript
      `grep -rnE "generated/|@nestjs|express|@asyncapi|ajv|kafkajs|amqplib" src/*/domain src/*/application`;
      Java/Kotlin
      `grep -rnE "\.generated\.|org\.springframework\.(web|http|messaging|cloud\.stream)|jakarta\.validation" src/main/*/**/{domain,application}`;
    - list every `if` in the adapters and say what it decides.
3. **Wiring test**: one per API and one per consumer group, through the real
   persistence adapter (step 7's database setup): request in → row and
   outbox entry out.
4. Report: EXACT source and autonomy level; generators and versions, what
   they generated, warnings; spec changes sent back to step 4; tests
   active/total per unit; behavioral prediction mismatches; operations → use cases,
   receive operations → handlers; the error map; what the runtime validator
   does **not** check; anticorruption mappings; open risks; files created.
5. Return to the caller's unit checkpoint, or ask the user to approve.

## 7. Stack idioms

### TypeScript + NestJS (default)

- **OpenAPI → types**: `openapi-typescript` (pin the version) generates
  `paths`, `operations` and `components` into `src/generated/`. It needs
  TypeScript 5 as a peer; with another compiler version in the project,
  install it in `tools/codegen/` with its own `package.json`. If the project
  can use OpenAPI Generator (Java runtime and Maven Central reachable), check
  `openapi-generator-cli list` for a NestJS **server** generator and pin it;
  otherwise use types plus the runtime contract below.
- **Runtime contract**: `express-openapi-validator` with
  `validateRequests: true, validateResponses: true`. Create the app with
  `bodyParser: false` and mount `express.json({ type: ['application/json', 'application/*+json'] })`
  **before** the validator — Nest registers its own parser only in
  `init()`, after middleware added with `app.use`, and the validator then
  sees no body. Response validation covers bodies sent with `res.json`; a
  bodyless response (204) is not checked — assert status codes in tests.
- **Controllers** typed against the generated contract:
  `type Body = operations['borrowCopy']['requestBody']['content']['application/json']`,
  `@Param('copyId') copyId: operations['borrowCopy']['parameters']['path']['copyId']`.
  **Coverage gate** at compile time:
  `export const SERVED_OPERATIONS = { borrowCopy: LoansController } satisfies Record<keyof operations, unknown>;`
  — a new or removed operation fails `tsc`.
- **Error map** as one global `@Catch()` filter implementing
  `ExceptionFilter` (not `BaseExceptionFilter`, whose fallback is not a
  Problem): `toProblem(e)` with one line per error; the validator's
  `error.BadRequest`, `NotFound`, `MethodNotAllowed`, `UnsupportedMediaType`
  classes mapped explicitly; the `Problem` type from `components['schemas']`.
    - The validator's `MethodNotAllowed` carries the spec's methods in
      `error.headers.Allow` (lowercase) — set `Allow` on the response,
      uppercased (`POST`).
    - Nest wraps an unparsable JSON body in its own `BadRequestException` —
      map `HttpException`s by `getStatus()` (400 → `/problems/invalid-request`,
      other 4xx → their status) before the 500 fallback; do not match on
      body-parser's error type, it never reaches the filter.
- **Dependency injection** with explicit `@Inject(Token)` on every
  constructor parameter: Vitest's transform does not emit decorator
  metadata. Set `experimentalDecorators` in `tsconfig.json`; wire use cases
  with `useFactory` providers in a dynamic `LendingModule.with(ports)` so
  tests pass fakes and the wiring test passes the real adapter.
- **AsyncAPI → message models**: `@asyncapi/modelina` (pin it;
  `TypeScriptFileGenerator`, `modelType: 'interface'`) from a small script
  in `tools/codegen/`. It generates payload models only — no headers, no
  operations. So: validate headers and payload at runtime against the
  spec's schemas, read with `@asyncapi/parser` and compiled with `ajv`
  (`strict: false` — the parser adds `x-parser-*` keywords) **plus
  `ajv-formats`** (`addFormats(ajv)`; without it `ajv` only warns
  "unknown format ignored" and `date-time`, `email`, `uuid` go unchecked;
  in a CommonJS build import it as `mod.default ?? mod`); the coverage
  gate is a test comparing `operations().filterByReceive()` with a
  `CONSUMERS` registry.
- **Consumers** as `handle({ headers, payload })` over raw input; the
  transport binding (KafkaJS, `amqplib`, Nest microservices) only subscribes
  the channel address and calls `handle`, mapping the outcome to ack, dead
  letter or retry.
- Tests with `supertest` against `app.getHttpServer()`; close every app in
  `afterEach`. Run `vitest run --no-file-parallelism` while wiring tests
  share one test database.

### Java + Spring Boot (second option)

- **OpenAPI**: `openapi-generator-maven-plugin`, generator `spring`, with
  `interfaceOnly=true`, `useSpringBoot3=true`, `useTags=true`,
  `skipDefaultInterface=true`, `useBeanValidation=true`,
  `openApiNullable=false`; controllers implement the generated `…Api`
  interfaces, so a new operation fails compilation (the coverage gate).
- **AsyncAPI**: ZenWave SDK (`zenwave-sdk-maven-plugin`, Spring Cloud Stream
  generator, consumer side) for consumer interfaces and DTOs, or an AsyncAPI
  Generator Java Spring template if the ADR chose it. Coverage gate: a test
  asserting a bean exists for every generated consumer interface.
- **Error map**: one `@RestControllerAdvice` extending
  `ResponseEntityExceptionHandler`, returning Spring's `ProblemDetail`;
  `spring.mvc.problemdetails.enabled=true`. It already answers
  `HttpMessageNotReadableException` with 400 and
  `HttpRequestMethodNotSupportedException` with 405 and `Allow` — keep
  those, test them in the edge unit.
- Tests with `@WebMvcTest` plus fakes as `@TestConfiguration` beans; response
  validation against the spec with `swagger-request-validator-mockmvc`.

### Kotlin + Spring Boot (third option)

- As Java, with OpenAPI Generator `kotlin-spring` (`interfaceOnly=true`,
  `useSpringBoot3=true`, `useTags=true`); check that the chosen AsyncAPI
  generator emits Kotlin or Java usable from Kotlin, and say which. EXACT
  has no Kotlin profile — use the Java profile adapted to Gradle, and say
  so.

## 8. Never

- Hand-write or edit code a generator produces from the specs; fix the spec
  or a generator option instead.
- Let a generated DTO, message model, framework or validator type reach the
  domain or application layer.
- Change a spec without sending it back to step 4 for approval.
- Put a business decision or an undeclared default into an adapter.
- Answer an error with the framework's default page or leak internal
  messages in a 500.
- Trust a runtime validator for a check you have not seen it make.
- Report a test result that was not run.

## 9. Fallback (only if EXACT Coding cannot be located)

The same generation and test lists. Per behavior: predict, Red for the
predicted reason, least code to Green with the unit's tests and the fast suite (slow
suite and gates at the finish), refactor under
the Four Rules with §5 kept; show and ask before the next behavior.