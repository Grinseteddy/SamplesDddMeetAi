# Larder – First Implementation

Larder as a **modular monolith** (ADR0001): one Maven module per Bounded Context, one deployable.
Derived from the data-flow context map of Chapter 12 and the reconciled contracts in [`contracts/`](contracts/CHANGES.md).

## Structure

```
contracts/            OpenAPI + AsyncAPI – the single source of truth, code is generated from here
platform/
  platform-security     JWT resource server, CookId from the token, scope checks (AP0008)
  platform-persistence  one schema + one DB user per Bounded Context (ADR0002)
  platform-messaging    RabbitMQ topology helpers, MessageHeader, JSON (ADR0003)
  platform-test         architecture rules every Bounded Context must satisfy
bounded-contexts/     one module per Bounded Context (see table)
larder-app/           the one Spring Boot deployable; no domain code
infra/                docker-compose: PostgreSQL (schemas+users), RabbitMQ, Keycloak
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
| meal-planning | `/meal-planning/meal-plans` | recipe-catalog, cooking-assistance | – |
| meal-preparation | `/meal-preparation/preparations/...` | cooking-assistance | – |
| cooking-assistance | `/cooking-assistance/help-requests`, `/helps` (OHS) | media | publishes on `cooking-assistance`; consumes `grandma-avatar.help.provided` |
| grandma-avatar-ai | – (ACL to the external AI) | – | consumes `cooking-assistance.help.requested`; publishes on `grandma-avatar` |
| notification | `/notifications/notifications` | – | consumes `cooking-assistance.help.requested` / `.help.provided` (CF) |
| media | `/media/images` | – | – |
| sharing | `/sharing/thanks` | cooking-assistance, media, consent-management | – |
| cook-profile | `/cook-profile/cooks` | consent-management | – |
| consent-management | `/consent-management/consents` | – | – |

## Build and run

```bash
mvn install                                    # generates code, compiles, runs architecture tests
docker compose -f infra/docker-compose.yml up -d
mvn -pl larder-app spring-boot:run
```

All operations answer `501 Not Implemented` until the domain is implemented.

## Plan

| Phase | Content | Status |
|---|---|---|
| 0 | Reconcile contracts ([CHANGES](contracts/CHANGES.md)) | done |
| 1 | Skeleton: modules, generation, platform, schemas, topology, boundary checks | done |
| 2 | Upstreams without dependencies: Recipe Catalog, Cook Profile, Consent Management, Media | |
| 3 | Meal Planning, Meal Preparation (clients against Recipe Catalog / Cooking Assistance; Prism mocks until phase 4) | |
| 4 | Cooking Assistance (REST + publisher/consumer), Grandma Avatar ACL (stub AI first), Notification | |
| 5 | Sharing incl. consent check for mentioned cooks | |
| 6 | Micro-UIs per Bounded Context + AppShell orchestrator (ADR0005) | |
| 7 | Contract tests (OpenAPI + AsyncAPI payloads), Testcontainers end-to-end rescue scenario, coverage gate (AP0005) | |
