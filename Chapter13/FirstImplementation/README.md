# Larder – First Implementation

Larder as a **modular monolith** (ADR0001): one Maven module per Bounded Context, one deployable.
Derived from the data-flow context map of Chapter 12 and the reconciled contracts in [`contracts/`](contracts/CHANGES.md).

## Structure

```
contracts/            OpenAPI + AsyncAPI – the single source of truth, code is generated from here
platform/
  platform-security     JWT resource server, CookId from the token, scope checks (AP0008)
  platform-web          REST conventions, e.g. enum parameters in the contract's spelling
  platform-persistence  one schema + one DB user per Bounded Context (ADR0002)
  platform-messaging    RabbitMQ (ADR0003): topology incl. dead-letter queues, contract messages, transactional outbox
  platform-test         architecture rules every Bounded Context must satisfy
bounded-contexts/     one module per Bounded Context (see table)
larder-app/           the one Spring Boot deployable; no domain code
infra/                docker-compose: PostgreSQL (schemas+users), RabbitMQ, Keycloak (dev realm), S3Mock (bucket)
```

Inside each Bounded Context (hexagonal, checked by ArchUnit):

```
org.larder.<bc>.domain                 aggregates, value objects, events – no framework
org.larder.<bc>.application            use cases, ports
org.larder.<bc>.adapter.in.web         controllers implementing interfaces generated from the own OpenAPI
org.larder.<bc>.adapter.in.messaging   queues (and later listeners) from the AsyncAPI
org.larder.<bc>.adapter.out.persistence  own schema
org.larder.<bc>.adapter.out.<upstream>.client  client generated from the UPSTREAM's contract
```

**Rule:** a Bounded Context never depends on another one. The Maven enforcer fails the build on
`org.larder.bc:*` dependencies; integration goes through clients generated from the upstream contract.

## Bounded Contexts

| Module | Provides | Upstream clients (sync) | Async |
|---|---|---|---|
| recipe-catalog | `/recipe-catalog/recipes/...` (OHS) | – | – |
| meal-planning | `/meal-planning/meal-plans` | recipe-catalog | – |
| meal-preparation | `/meal-preparation/preparations/...` | recipe-catalog | – |
| cooking-assistance | `/cooking-assistance/help-requests`, `/helps` (OHS) | – | publishes on `cooking-assistance` (outbox); consumes `grandma-avatar.help.provided` |
| grandma-avatar-ai | – (ACL to the external AI; deterministic recipe box for now) | – | consumes `cooking-assistance.help.requested`; publishes on `grandma-avatar` (outbox) |
| notification | `/notifications/notifications` | – | consumes `cooking-assistance.help.requested` / `.help.provided` (CF) |
| media | `/media/images` | – | – |
| sharing | `/sharing/thanks` | cooking-assistance, media, consent-management | – |
| cook-profile | `/cook-profile/cooks` | consent-management | – |
| consent-management | `/consent-management/consents` | – | – |

## Test the contracts (works from phase 0 on, no Java needed)

Requires Node.js 18+. Tools run via `npx` with pinned versions (`scripts/common.sh`).

```bash
scripts/lint-contracts.sh      # Redocly for all OpenAPIs, AsyncAPI CLI for all AsyncAPIs
scripts/mock-apis.sh start     # one Prism mock per OpenAPI, ports 4010-4018 (status | stop)
scripts/smoke-mocks.sh         # test bench against the mocks, starts/stops them if needed
```

| Mock | Port | | Mock | Port |
|---|---|---|---|---|
| recipe-catalog | 4010 | | consent-management | 4015 |
| meal-planning | 4011 | | media | 4016 |
| meal-preparation | 4012 | | notifications | 4017 |
| cooking-assistance | 4013 | | sharing | 4018 |
| cook-profile | 4014 | | | |

Mocks run with `--errors`: a response that violates its own contract becomes a 500, so inconsistent examples
surface immediately. Calls need `Authorization: Bearer <anything>` and `version: 1.0.0`.

`smoke-mocks.sh` checks that every API answers and rejects calls without a token, that the invariants are enforced
on requests (e.g. Chef only for menu proposals), that the examples satisfy their own invariants, and plays the
rescue flow across the Bounded Contexts: recipe → preparation → help request → help → pictures → consent → thanks.

## Build and run (phase 1)

Requires Java 21, Maven, Docker.

```bash
mvn install                                         # generates code, compiles, runs architecture tests
docker compose -f infra/docker-compose.yml up -d    # PostgreSQL :5432, RabbitMQ :5672/:15672, Keycloak :8180, S3Mock :9090
java -jar larder-app/target/larder-app-0.1.0-SNAPSHOT.jar
scripts/smoke-app.sh                                # schemas, isolation, topology, security + the whole rescue story
```

Local development only:

- Keycloak realm `larder` (imported from `infra/keycloak/larder-realm.json`): public client `larder-dev`,
  test user `cook` / `cook`. Every access token carries all API scopes and the fixed claim
  `cookId = f64e07f0-f9e7-4b7a-8695-e7a8b3ef5074` (the cook of the glossary examples).
- Get a token:
  `curl -s -X POST localhost:8180/realms/larder/protocol/openid-connect/token -d grant_type=password -d client_id=larder-dev -d username=cook -d password=cook`
- Each Bounded Context connects with its own user (password = user name) and a pool of
  `larder.<bc>.database.pool-size` connections (default 3); 10 pools must fit PostgreSQL's 100 connections.

All ten Bounded Contexts are implemented.

The S3 bucket is Adobe S3Mock locally (the MinIO images are not pullable here); Media talks plain S3 with the
AWS SDK, so a real bucket only needs other `larder.media.storage.*` values.

## Messaging (phase 4)

- **Topology from the AsyncAPIs:** a publisher owns its topic exchange, a consumer owns its queue and binding.
  Operational addition: every consumer queue dead-letters into `<queue>.dlq` (exchange `larder.dead-letter`).
- **Transactional outbox:** a context writes an event into the `outbox` table of its own schema in the same
  transaction as the state change; `OutboxRelay` publishes it, waits for the broker's confirm, then deletes it.
  Events are published if and only if the change was committed - at least once.
- **Idempotent consumers:** because delivery is at least once, every consumer dedupes (by message or business id).
- **Failures:** a listener exception is retried 3 times (500 ms, ×2), then the message goes to `<queue>.dlq`.
  Messages that can never succeed (contract violation, unknown request) are logged and acknowledged.
- **Contract messages:** JSON payload, `MessageHeader` (correlationId, messageId, source) as AMQP headers, the
  contract's message name as AMQP type - no Java class names. Consumers read into their own types.
- **Contract tests:** `AsyncApiContract` validates published and consumed messages against the AsyncAPI schemas,
  resolving `$ref`s across contract files.

## Plan

| Phase | Content | Status |
|---|---|---|
| 0 | Reconcile contracts ([CHANGES](contracts/CHANGES.md)), contract test bench (`scripts/`) | done |
| 1 | Skeleton: modules, generation, platform, schemas, topology, boundary checks | done |
| 2 | Upstreams without dependencies: Recipe Catalog, Cook Profile, Consent Management, Media | done |
| 3 | Meal Planning, Meal Preparation (clients against Recipe Catalog; help flows go UI → Cooking Assistance, see CHANGES) | done |
| 4 | Cooking Assistance (REST + publisher/consumer), Grandma Avatar ACL (deterministic recipe box), Notification | done |
| 5 | Sharing incl. consent check for mentioned cooks | done |
| 6 | Micro-UIs per Bounded Context + AppShell orchestrator (ADR0005) | |
| 7 | Contract tests (OpenAPI + AsyncAPI payloads), Testcontainers end-to-end rescue scenario, coverage gate (AP0005) | |


## Phase 0

Try app
```
docker compose -f infra/docker-compose.yml up -
```

Start
```
mvn -pl larder-app spring-boot:run
```

Mock server from OpenAPI contract
```
npx @stoplight/prism-cli mock contracts/openapi/cooking-assistance.openapi.yaml -p 4010
```

```
curl -X POST localhost:4010/help-requests -H 'version: 1.0.0' -H 'Authorization: Bearer x' -H 'Content-Type: application/json' -d '{"title":"Burning Catastrophe","type":"STEPS_TO_MITIGATE_CATASTROPHE","description":"Scones are burned","preferredProvider":["GRANDMA_AVATAR","COMMUNITY"]}'
```

Shows the messages of the AsyncAPI contract
```
npx @asyncapi/cli start studio contracts/asyncapi/cooking-assistance.asyncapi.yaml
```

