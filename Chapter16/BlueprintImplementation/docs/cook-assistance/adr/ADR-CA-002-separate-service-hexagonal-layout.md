# ADR-CA-002 — Cook Assistance as an acknowledged separate service, hexagonal layout

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 2

## Context
ADR-CA-001 makes Cook Assistance a TypeScript deployable, so it cannot live inside the Java Larder
monolith (ADR0001, AP0001). QS-01 asks for module boundaries that a rule test can check.

## Decision
1. Cook Assistance runs as **one separate service** — a deliberate, **acknowledged exception** to
   ADR0001 and AP0001: it is the Larder book sample for Predictive TDD in TypeScript. The rest of Larder
   stays a modular monolith; ADR0001 is not superseded.
2. Inside the service, a **hexagonal layout** mirroring Chapter 13:
   ```
   src/cook-assistance/domain                    aggregates, value objects, events — no framework
   src/cook-assistance/application               use cases, policy handlers, ports
   src/cook-assistance/adapter/in/http           wiring to the generated OpenAPI contract
   src/cook-assistance/adapter/in/messaging      consumer of Grandma Avatar AI messages (ACL)
   src/cook-assistance/adapter/out/persistence   repositories, outbox, migrations
   src/cook-assistance/adapter/out/messaging     outbox relay, publisher
   src/cook-assistance/adapter/out/recipe-catalog client of the Recipe Catalog
   src/generated                                 code generated from the approved specs (never edited)
   src/main.ts, src/app.module.ts                composition root
   ```
   NestJS decorators and modules appear only in adapters and the composition root.

## Consequences
- **Overrides ADR0001 and AP0001 — acknowledged**, for Cook Assistance only, with the reason above.
  Offered for ingest into the decision graph as an acknowledged override.
- The service has its own pipeline and image; it shares the one database instance (ADR0002) and the
  RabbitMQ broker (ADR0003) with Larder.
- The dependency rule is enforced by dependency-cruiser in CI (QS-01).

## Quality scenarios
Serves QS-01 (maintainability); trades off the single-deployable simplicity of ADR0001.

## Amendment — 2026-10-26 (step 13, reverse check after the inspection rework of increment 0)
The layout as the code has it (dependency-cruiser enforces the arrows, `.dependency-cruiser.cjs`):
```
src/cook-assistance/domain                     aggregates, value objects, events — imports the domain only
src/cook-assistance/application                use cases, policy handlers, ports, query ports — domain and application only
src/cook-assistance/adapter/in/http            wiring to the generated OpenAPI contract
src/cook-assistance/adapter/in/messaging       consumer of Grandma Avatar AI messages (ACL)
src/cook-assistance/adapter/messaging          what inbound and outbound messaging share: delivery types, message schemas
src/cook-assistance/adapter/out/persistence    repositories, outbox, migrations, read models
src/cook-assistance/adapter/out/messaging      outbox relay, publisher, broker binding
src/cook-assistance/adapter/out/recipe-catalog client of the Recipe Catalog
src/cook-assistance/adapter/out/system         clock, id generator
src/cook-assistance/composition.ts, service.ts composition root (with src/main.ts and src/rebuild-views.ts)
src/generated                                  code generated from the approved specs (never edited)
```
Inbound and outbound adapters do not import each other (inspection M-1); adapters do not import the composition root.
There is no `app.module.ts`: the HTTP adapter builds its Nest module itself (`http-app.ts`).
