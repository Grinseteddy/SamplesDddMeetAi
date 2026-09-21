---
name: openapi-spec-author
description: >-
  Author OpenAPI 3.1.0 API specifications in YAML following a consistent,
  componentized house style (the same style as the bundled Catalog Management
  example). Use to create, write, draft, design, scaffold, or extend an
  OpenAPI/Swagger spec, design a REST API contract, turn a feature or data
  model into endpoints, or produce an api.yaml/openapi.yaml — even without
  saying "OpenAPI 3.1.0". Also covers adding to an existing spec, and
  validating/linting one. Also derives specs from DDD modeling artifacts —
  EventStorming boards, Domain Stories, Visual Glossaries, or their
  interpreter briefs — scoped to one or all bounded contexts/modules.
  Produces YAML file(s) that pass Spectral linting.
---

# OpenAPI 3.1.0 Spec Author

This skill produces OpenAPI **3.1.0** specifications in **YAML** that follow a
consistent, heavily componentized house style and pass linting. The bundled
`assets/example-catalog-management.yaml` is the gold-standard reference for what
"good" looks like — when in doubt about structure or formatting, open it and
mirror it.

The conventions below are **strong defaults**: apply them unless the user's
domain clearly calls for something different (e.g. a different auth scheme, no
versioning header, an internal-only audience). Adapt thoughtfully rather than
forcing every rule, but keep the spec internally consistent.

## Workflow

Follow these steps in order. Don't skip validation.

### 1. Understand the API

Establish, from the conversation or by asking briefly:
- The **resources** (nouns) the API manages and how they relate (parent/child).
- The **operations** needed per resource (create / read / update / delete / list / search).
- **Auth**: who calls it, what scopes/roles exist (default is OAuth2 with
  read/write/admin scopes).
- **Audience**: public, partner, or internal (drives `x-audience`).

If the user already gave a data model, feature description, or existing spec,
extract the resources from that instead of interrogating them. Ask only what you
genuinely can't infer.

**If the input is an EventStorming board** (a photo/screenshot of stickies, a
Miro/Mural export, a plain sticky list, or an Event Storming Brief already
produced by the `event-storming-interpreter` skill) instead of a plain
description, read `references/eventstorming-mapping.md` before doing
anything else — it governs how aggregates, commands, read models, actors,
and state machines translate into resources, operations, query endpoints,
scopes, and status fields. That file also covers what to do with domain
events (usually not exposed directly) and cross-context arrows (documented,
not modeled as synchronous calls).

**If the input is a Domain Story and/or a Visual Glossary** (diagrams,
transcriptions, or the Prototype Brief / Glossary Brief from
`domain-story-interpreter` / `visual-glossary-interpreter`), read
`references/domain-story-glossary-mapping.md` instead — it's a different
notation pair from EventStorming (verbs from the story, nouns and
cardinalities from the glossary) and covers what to do when only one of the
two is supplied, and how to reconcile them when both are.

### 2. Choose the scope: one API or several

This matters most for diagram-shaped input (EventStorming, Domain Story,
Visual Glossary), where a bounded context / module is the natural unit of an
API, but it applies to any request that spans more than one obvious service.
Confirm with the user:
- **All bounded contexts / modules** — one `.yaml` per context; repeat steps
  3–6 below once per context.
- **One named bounded context / module** — a single `.yaml` for just that
  one.

If there's only one context in play (a single-service description, or a
diagram with one context / no drawn grouping), skip the question and
proceed with that single scope.

### 3. Read the conventions

Before writing, read `references/conventions.md` — it is the authoritative,
detailed specification of the house style (naming, componentization rules,
schema design, the standard response set, security patterns). The summary in
this file is a reminder, not a substitute.

### 4. Write the spec

Start from `assets/skeleton.yaml` and build outward. Write the file in this
order so structure stays clean:

1. `openapi: 3.1.0` + `info` (with `x-api-id` and `x-audience`) + `servers`.
2. Root `security` + `tags` (one per resource area, each with a description).
3. `paths` — operations that reference everything via `$ref`, inlining as little
   as possible.
4. `components` — `parameters`, `schemas`, `responses`, `requestBodies`,
   `securitySchemes`.

The single most important convention: **componentize and reuse via `$ref`.**
Parameters, request bodies, responses, and any reused schema live under
`components` and are referenced from the paths — exactly as in the example.

### 5. Validate and lint

Always validate the finished spec. Read `references/validation.md` for the exact
commands. In short: lint with Spectral using the bundled ruleset:

```bash
npm install -g @stoplight/spectral-cli   # if not already installed
spectral lint <spec>.yaml --ruleset assets/house-style.spectral.yaml
```

A property without a `description` is an **error** (`house-properties-have-description`).
Fix every **error** and review **warnings** (resolve them unless there's a
deliberate reason). Re-run until clean. If Spectral can't be installed, fall
back to the Python structural validator described in `references/validation.md`.

### 6. Present

Save the final `.yaml` (or one per bounded context, if scoped to several) to
the outputs directory and present it/them. Briefly note the resources
covered and the result of the lint run per file. When generating several
files, also restate anything flagged in
`references/eventstorming-mapping.md` §6–9 that the user should resolve
(unexposed commands, events not modeled, cross-context integration notes,
hotspots).

## House style at a glance

A compressed reminder — `references/conventions.md` has the full detail.

**Naming**
- Paths: `kebab-case`, plural collection nouns (`/catalog-entries`), sub-resources
  nested (`/catalog-entries/{catalogEntryId}/tags`).
- Path / query / header parameters: `camelCase`.
- `operationId`: `camelCase`, verb + noun (`createCatalogEntry`, `getCatalogEntryById`).
- Component keys & schema names: `PascalCase`, with role suffixes —
  `...Parameter`, `...Response`, `...Request`.
- Schema properties: `camelCase`.

**info block** — title, a one/two-sentence description (purpose + who uses it),
semver `version`, `contact.name`, a stable UUID `x-api-id` (never changes across
versions), and `x-audience` (`external-public` / `external-partner` /
`company-internal` / `component-internal`).

**Operations** — each has `operationId`, `tags` (must be a defined tag),
`summary` (short imperative), `description` (a `|` block explaining behavior and
side effects), `parameters`/`requestBody`/`responses` via `$ref`, and a
per-operation `security` block only when it narrows the default (e.g. write/admin
scopes for mutations).

**Standard responses** (reuse shared components):
- GET → `200, 400, 401, 403, 404, 500`
- POST → `201, 400, 401, 403, 500`
- PUT → `200, 400, 401, 403, 404, 500`
- DELETE → `204` (inline description), `400, 401, 403, 404, 500`

All error responses point at a shared `Error` schema (`code`, `message`,
`details`).

**Schemas** — `type: object` with `required` + `properties`. **Every property
carries a `description`** — a domain-language sentence, not a restatement of the
name — including `$ref` properties (as a sibling of the `$ref`), nested and
inline schemas, write-model variants and `Error`; the Spectral ruleset fails the
lint otherwise. Add constraints
liberally: `minLength`/`maxLength`, `minItems`/`maxItems`, `pattern`, and
`format` (`uuid`, `date-time`, `uri`, `email`, …). Keep **read** models (include
server-generated fields like ids and timestamps) separate from **write** models:
a `...Create` schema (required fields, no server-generated ones) and a
`...Update` schema (all-optional / partial). For nullable fields in 3.1.0 use a
type array, e.g. `type: [string, "null"]`.

**Security** — default is OAuth2 authorization-code flow with PKCE, declared once
in `components.securitySchemes` and referenced at the root, with scopes such as
`read`/`write`/`admin`.

**Versioning** — a required `version` header parameter whose `enum` is locked to
the current version with a matching `default`, referenced by every operation.

## Bundled resources

- `references/conventions.md` — full house-style specification. Read before writing.
- `references/eventstorming-mapping.md` — how to derive resources, operations,
  query endpoints, scopes, and status fields from an EventStorming board or
  brief, and how to scope generation to one or all bounded contexts. Read
  when the input is board-shaped.
- `references/domain-story-glossary-mapping.md` — how to derive operations
  from a Domain Story's use cases and schemas/cardinalities from a Visual
  Glossary's terms, separately or reconciled together. Read when the input
  is a domain story and/or a visual glossary.
- `references/validation.md` — how to install and run Spectral (and fallbacks).
- `assets/example-catalog-management.yaml` — gold-standard example to mirror.
- `assets/skeleton.yaml` — minimal starting template with the right structure.
- `assets/house-style.spectral.yaml` — Spectral ruleset encoding the conventions.