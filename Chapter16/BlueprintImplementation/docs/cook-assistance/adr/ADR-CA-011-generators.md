# ADR-CA-011 — Generated contract code: types plus runtime validation

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 11

## Context
The inbound and messaging layers are generated from the approved specs (OpenAPI 2.0.0, AsyncAPI 2.0.0),
never written by hand. TypeScript has no reliable NestJS server generator for OpenAPI 3.1.

## Decision
- **OpenAPI → `src/generated/openapi/`**: `openapi-typescript` **7.13.0** generates the path, operation
  and schema types. At runtime `express-openapi-validator` **5.6.2** validates requests and responses
  against the spec, formats included (`uuid`, `date-time`, `date`, `uri`). A compile-time
  **coverage gate** maps every `operationId` of the spec to a handler (or to the planned list): a new
  operation fails type checking until it is wired or planned.
- **AsyncAPI → `src/generated/asyncapi/`**: `@asyncapi/modelina` **5.10.1** generates the message
  payload models; `@asyncapi/parser` **3.6.6** with `ajv` **8.20.0** and `ajv-formats` **3.0.1** validates
  every sent and received message against the spec. A coverage check fails the build when a send or
  receive operation has no mapper or consumer (or planned-list entry).
- Versions are **pinned** (exact, in `package.json` and the lockfile); `npm run generate` runs first in
  the one build command; generated sources are **git-ignored** and never edited — a generator problem
  is fixed in the spec (step 4) or a generator option.

## Consequences
- Wiring code (mappers, controllers implementing the generated types) is hand-written test-first
  (steps 9, 10); generated types stay out of the domain package (dependency-cruiser rule).
- Step 12's contract runs (Schemathesis, AsyncAPI examples) check what the types cannot.

## Quality scenarios
Serves QS-01, QS-03.
