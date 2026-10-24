# ADR-CA-016 — Test infrastructure and quality-test tools

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 16

## Context
Steps 7–12 test against real infrastructure; step 12 adds acceptance, contract and quality-scenario
tests (QS-01 … QS-21). Docker 26 is available locally and on GitHub Actions runners.

## Decision
| Purpose | Tool |
|---|---|
| Unit and integration tests | Vitest (EXACT TypeScript profile) |
| Database in tests | Testcontainers `@testcontainers/postgresql` — PostgreSQL 16 |
| Broker in tests | Testcontainers `@testcontainers/rabbitmq` — RabbitMQ 3.13 (fake broker behind the publisher port in step 10; real broker from step 12) |
| Recipe Catalog in tests | contract-checked stub: Prism mock from Recipe Catalog's OpenAPI (container), plus an in-memory fake that passes the same port contract |
| Acceptance tests | Cucumber-JS, one feature file per board, scenarios tagged with slice and rule |
| OpenAPI contract run | Schemathesis against the running service |
| AsyncAPI contract run | messages validated against the spec (parser + ajv) from the spec's examples and captured traffic |
| Load (QS-08, QS-09, QS-10, QS-16) | k6, on the pull request only |
| Fault injection (QS-04, QS-05, QS-07) | Testcontainers stop/pause/restart of PostgreSQL and RabbitMQ |
| Compatibility (QS-03) | oasdiff (OpenAPI) and `@asyncapi/diff` (AsyncAPI) against the last released 2.x |
| Module rules (QS-01) | dependency-cruiser |

All tool versions are pinned in `package.json` / CI. Results in the test environment are reported as
*indicative* where it differs from production (e.g. load and latency).

## Quality scenarios
Serves QS-01 … QS-21 verification; QS-06 stays an open verification.
