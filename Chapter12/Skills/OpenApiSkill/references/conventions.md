# House-Style Conventions (full reference)

The authoritative description of the OpenAPI 3.1.0 house style. The bundled
`assets/example-catalog-management.yaml` demonstrates every rule here; treat it
as the canonical example and mirror its structure and formatting.

## Contents
1. Document & info block
2. Servers, security, tags
3. Paths & operations
4. The standard response set
5. Componentization (the core principle)
6. Schema design
7. Parameters
8. Security schemes
9. Naming cheat sheet

---

## 1. Document & info block

- First line is always `openapi: 3.1.0`.
- The `info` block contains:
  - `title` — human-readable API name.
  - `description` — one or two sentences: what the API does **and** who uses it.
    Use a `|` block scalar if it spans lines.
  - `version` — semantic version (`MAJOR.MINOR.PATCH`), e.g. `2.0.0`.
  - `contact.name` — owning person or team.
  - `x-api-id` — a **UUID** that uniquely and permanently identifies the API.
    It is stable: it never changes across versions. Generate a fresh UUID for a
    new API.
  - `x-audience` — intended consumers. Use one of:
    - `external-public` — anyone may use it.
    - `external-partner` — specific external partners.
    - `company-internal` — internal to the organization.
    - `component-internal` — internal to one component/system.

```yaml
openapi: 3.1.0
info:
  title: Catalog Management API
  description: API for managing catalog entries and allows members to search for books...
  version: 2.0.0
  contact:
    name: Annegret Junker
  x-api-id: 2f8c1a3b-9d4e-4f12-bb23-947c7def3184
  x-audience: external-public
```

---

## 2. Servers, security, tags

- `servers` — a list; each `url` includes the API base path
  (`https://apis.online-library.org/catalog-management`).
- Root `security` — the default security requirement applied to all operations
  unless an operation overrides it. References a scheme from
  `components.securitySchemes` with its scopes.
- `tags` — a list of `{ name, description }`, **one per resource area / domain
  grouping**. Every tag used on an operation must be declared here, and every
  declared tag should have a description.

```yaml
servers:
  - url: 'https://apis.online-library.org/catalog-management'

security:
  - oAuth2:
      - catalog:read
      - catalog:write
      - catalog:admin

tags:
  - name: Catalog Entries
    description: Catalog entry management operations
  - name: Books
    description: Book information operations
```

---

## 3. Paths & operations

- Path templates use **kebab-case**, **plural** collection nouns:
  `/catalog-entries`. Sub-resources nest under an item:
  `/catalog-entries/{catalogEntryId}/tags`.
- Path parameters are **camelCase** inside braces: `{catalogEntryId}`.
- Each operation includes, in this order:
  - `operationId` — **camelCase**, verb + noun (`createCatalogEntry`,
    `getCatalogEntryById`, `updateCatalogEntryTags`). Unique across the document.
  - `tags` — references a tag declared in the top-level `tags` list.
  - `summary` — short imperative phrase ("Create a new catalog entry").
  - `description` — a `|` block scalar explaining behavior, preconditions, and
    side effects (especially anything irreversible or surprising).
  - `security` — include **only when it narrows the default**, e.g. mutations
    require `write`, destructive ops require `admin`. Read operations usually
    inherit the root default and omit `security`.
  - `parameters` — a list of `$ref`s to `components.parameters`.
  - `requestBody` — a `$ref` to `components.requestBodies` (for POST/PUT/PATCH).
  - `responses` — `$ref`s to `components.responses` (see §4).

```yaml
paths:
  /catalog-entries:
    post:
      operationId: createCatalogEntry
      tags:
        - Catalog Entries
      summary: Create a new catalog entry
      description: |
        Create a new catalog entry for a book.
        The book information will be retrieved from an external service based on the ISBN.
      security:
        - oAuth2:
            - catalog:write
      parameters:
        - $ref: '#/components/parameters/VersionParameter'
      requestBody:
        $ref: '#/components/requestBodies/BookCreateRequest'
      responses:
        '201':
          $ref: '#/components/responses/CatalogEntryResponse'
        '400':
          $ref: '#/components/responses/BadRequestResponse'
        '401':
          $ref: '#/components/responses/ForbiddenResponse'
        '403':
          $ref: '#/components/responses/ForbiddenResponse'
        '500':
          $ref: '#/components/responses/ServiceNotAvailableResponse'
```

---

## 4. The standard response set

Every operation declares the same baseline of responses for its method. Reuse
shared error responses from `components.responses`.

| Method | Success | Errors |
|--------|---------|--------|
| GET    | `200`   | `400, 401, 403, 404, 500` |
| POST   | `201`   | `400, 401, 403, 500` |
| PUT    | `200`   | `400, 401, 403, 404, 500` |
| PATCH  | `200`   | `400, 401, 403, 404, 500` |
| DELETE | `204` (inline `description`, no body) | `400, 401, 403, 404, 500` |

- `400` → `BadRequestResponse` (invalid request)
- `401` → unauthorized; `403` → forbidden. Both may reuse a shared response
  backed by the `Error` schema (the example maps both to `ForbiddenResponse`).
- `404` → `NotFoundResponse`
- `500` → `ServiceNotAvailableResponse`

All error responses use a single shared `Error` schema. Add more codes
(e.g. `409 Conflict`, `422`) when the domain needs them, defined the same way.

---

## 5. Componentization (the core principle)

**Inline as little as possible.** Parameters, request bodies, responses, and any
reused schema are defined under `components` and referenced from the paths with
`$ref`. This keeps paths readable and the contract DRY. The example inlines
almost nothing — only a `204` description and a couple of trivial link-response
shapes.

`components` contains these sections, each keyed in **PascalCase** with a role
suffix:

- `parameters` — suffix `Parameter` (`CatalogEntryIdParameter`, `VersionParameter`).
- `schemas` — domain models, no suffix (`Book`, `CatalogEntry`), plus
  write-model variants (`BookCreate`, `CatalogEntryUpdate`) and `Error`.
- `responses` — suffix `Response` (`CatalogEntryResponse`, `BadRequestResponse`).
- `requestBodies` — suffix `Request` (`BookCreateRequest`, `TagsUpdateRequest`).
- `securitySchemes` — the auth scheme(s).

A named response wraps a description + media type + schema reference:

```yaml
components:
  responses:
    CatalogEntryResponse:
      description: Single catalog entry response
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/CatalogEntry'
    BadRequestResponse:
      description: Invalid request
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/Error'
  requestBodies:
    BookCreateRequest:
      description: Book creation request for catalog entry
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/BookCreate'
```

---

## 6. Schema design

- Every object schema declares `type: object`, a `required` list, and
  `properties`. Properties are **camelCase**.
- Apply constraints generously — they are part of the contract:
  - Strings: `minLength`, `maxLength`, and `pattern` for formatted values
    (e.g. the ISBN regex in the example).
  - Arrays: `minItems`, `maxItems`, and `items` (often a `$ref`).
  - `format`: `uuid`, `date-time`, `uri`, `email`, `int64`, etc.
- **Separate read models from write models:**
  - Read model (e.g. `Book`, `CatalogEntry`) includes server-generated, read-only
    fields: identifiers (`uuid`), `createdAt`/`updatedAt` (`date-time`).
  - `...Create` (e.g. `BookCreate`) — the fields a client supplies on creation;
    `required` lists the mandatory ones; **omit** server-generated fields.
  - `...Update` (e.g. `CatalogEntryUpdate`) — partial update: properties are
    optional (no `required` list), and only mutable fields appear.
- Compose with `$ref` (an `Author` array inside `Book`, a `Publisher` object,
  etc.) rather than repeating structures.
- **Nullable** (3.1.0): use a JSON-Schema type array — `type: [string, "null"]` —
  not the OpenAPI 3.0 `nullable: true` keyword.
- Provide a shared `Error` schema:

```yaml
    Error:
      type: object
      required:
        - code
        - message
      properties:
        code:
          type: string
        message:
          type: string
        details:
          type: object
```

---

## 7. Parameters

Define every parameter once under `components.parameters` and `$ref` it.

- **Identifier path parameters** — `in: path`, `required: true`, typed with the
  right `format` (usually `uuid`):

```yaml
    CatalogEntryIdParameter:
      name: catalogEntryId
      in: path
      required: true
      schema:
        type: string
        format: uuid
```

- **Version header** — a required `version` header whose `enum` is locked to the
  current API version, with a matching `default`. Referenced by every operation:

```yaml
    VersionParameter:
      name: version
      in: header
      required: true
      schema:
        type: string
        default: "2.0.0"
        enum: ["2.0.0"]
```

---

## 8. Security schemes

Default to OAuth2 with the authorization-code flow and PKCE, defined once and
referenced at the root (and narrowed per-operation). Declare every scope used
anywhere in the document.

```yaml
  securitySchemes:
    oAuth2:
      type: oauth2
      description: OAuth 2.0 Authorization Code Flow with PKCE
      flows:
        authorizationCode:
          authorizationUrl: https://online-library.com/auth
          tokenUrl: https://online-library.com/tokens
          refreshUrl: https://online-library.com/refresh
          scopes:
            catalog:read: Grants read access to resources
            catalog:write: Grants write access to modify resources
            catalog:admin: Grants administrative access
```

If the domain needs a different scheme (API key, bearer JWT, mutual TLS), adapt
— but still define it once in `securitySchemes` and reference it.

---

## 9. Naming cheat sheet

| Element | Convention | Example |
|---------|-----------|---------|
| Path | kebab-case, plural | `/catalog-entries` |
| Path parameter | camelCase | `{catalogEntryId}` |
| Query / header parameter | camelCase | `version` |
| operationId | camelCase verb+noun | `getCatalogEntryById` |
| Schema / component key | PascalCase | `CatalogEntry` |
| Parameter component | PascalCase + `Parameter` | `VersionParameter` |
| Response component | PascalCase + `Response` | `NotFoundResponse` |
| Request body component | PascalCase + `Request` | `BookCreateRequest` |
| Schema property | camelCase | `catalogEntryId` |
| Scope | `resource:action` | `catalog:write` |
