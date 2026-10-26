# ADR-CA-006 — RFC 9457 Problem Details and the error map

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 6 (spec proposal 15)

## Context
Spec 2.0.0 returns an `Error` object (code, message, details). Rejections are named after invariants
(INV-nn). Clients need a standard, machine-readable error format; no expected case may answer 500.

## Decision
All error responses are **RFC 9457 Problem Details** (`application/problem+json`) with the members
`type`, `title`, `status`, `detail`, `instance`, and the extensions **`code`** (the rule name, e.g.
`StepNotInRecipe`) and **`rule`** (the invariant ID, e.g. `INV-18`, where one applies).
`type` is `https://larder.org/problems/cook-assistance/<code-in-kebab-case>`.

Error map (application/domain error → HTTP):

| Error | Status |
|---|---|
| request does not match the schema (validator) | 400 `InvalidRequest` |
| missing/invalid token | 401 `Unauthenticated` |
| missing scope; `NotTheRequester`, `NotAnAddressee`, `PremiumRequiredForChef` | 403 |
| help request or help not found — or not visible to the caller (question 9) | 404 `NotFound` |
| undeclared method | 405 with `Allow` |
| `HelpRequestNotRequested`, `HelpNotProvided`, `ConcurrencyConflict` | 409 |
| undeclared content type | 415 |
| invariant violations (`QuestionRequired`, `StepNotInRecipe`, `ChefOnlyForMenu`, …) | 422 |
| database unavailable for a command; Recipe Catalog unavailable | 503 `ServiceUnavailable` (with `Retry-After`) |
| anything unexpected | 500 `InternalError` — never for an expected case |

## Consequences
- **Revision of step 4:** spec 2.0.0 (not yet released) replaces the `Error` schema by `Problem` and
  serves error responses as `application/problem+json`; adds `405`, `415` and `503` responses where
  they apply. Re-linted and recorded in the changelog.
- Domain errors carry the rule name and ID; the HTTP adapter maps them, the domain knows no status codes.

## Quality scenarios
Serves QS-01 (no HTTP in the domain), QS-03 (stable contract), QS-05 (503 during DB outage).
