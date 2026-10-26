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

## Amendment — 2026-10-25 (step 9, user decisions)
- **AsyncAPI message types:** `@asyncapi/modelina` 5.10.1 is **replaced** — it reads AsyncAPI only up to 3.0.0 (`supportedVersions`), and the spec is 3.1.0; it also pulled in legacy 1.x/2.x and RAML parsers with critical advisories (jsonpath-plus RCE). Instead, `tools/codegen/generate-asyncapi.mjs` checks that the spec parses with `@asyncapi/parser` and compiles the named component schemas with **`json-schema-to-typescript` 16.0.0** (option `maxItems: -1`; array limits stay a runtime check) into `src/generated/asyncapi/cook-assistance.ts`; raw schemas are read with **`yaml` 2.9.1**. The script fails on parse errors and on anonymous or numbered type names.
- **`@asyncapi/parser` 3.6.3** instead of 3.6.6: 3.6.4–3.6.6 were younger than two weeks when installed. Revisit after 2026-10-30.
- **Tool package:** the generators live in `tools/codegen/` with their own pinned `package.json` (openapi-typescript needs TypeScript 5: `typescript` 5.9.3 there; the project stays on 6.0.3). The root `npm run generate` calls it; `pretest` and `pretypecheck` run it first. `src/generated/` is git-ignored and excluded from ESLint.
- Audit after the change: root 0 apart from the accepted multer finding (see the implementation log), `tools/codegen` 0.

