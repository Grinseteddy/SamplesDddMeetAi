# Cook Assistance

The Cook Assistance bounded context of Larder: a cook asks the community and/or Grandma Avatar for help with one step
of a recipe, and Grandma Avatar's answer arrives as a message. Built with the implementation blueprint
(*DDD meets AI*, chapter 16) and EXACT Coding (Predictive TDD). TypeScript, NestJS, PostgreSQL 16, RabbitMQ 3.13.

Increment 0 (walking skeleton) serves slices **B2-S01** (`POST /help-requests`, Preparation step) and **B1-S04**
(the Grandma Avatar AI's answer on `grandma-avatar.help.provided`), and the read models behind
`GET /help-requests`, `/help-requests/{id}`, `/help-requests/{id}/helps` and `/helps/{id}`. Everything else in the
specs is planned per increment (`tools/intent-coverage/planned.json`).

## Requirements
- Node.js ≥ 22.12 and npm
- Docker (Testcontainers starts PostgreSQL and RabbitMQ for the tests; k6, oasdiff, gitleaks, Semgrep, OSV-Scanner and
  ZAP run as pinned images)
- Python 3.12 only for the OpenAPI contract run (Schemathesis)

## Build and test
```sh
npm ci && npm ci --prefix tools/codegen   # dependencies; `npm ci` also installs the repository's git hooks
npm run verify                            # the one build command (ADR-CA-018), ~2–4 min
```
`verify` runs: code generation from the specs → `tsc` → ESLint → Prettier → dependency rules → Vitest with coverage
(unit, integration on real PostgreSQL and RabbitMQ, AsyncAPI contract, spec diffs; lines and branches ≥ 95 %) →
acceptance (Cucumber) → intent coverage → security baseline (npm audit, OSV-Scanner, gitleaks, Semgrep).

Separate runs (pull-request jobs in CI):
```sh
npm run test:load               # k6 load scenarios QS-08, QS-09, QS-10, QS-16 (~7 min)
npm run test:contract:openapi   # Schemathesis against the running service (needs tools/contract/.venv, ~11 min)
npm run test:dast               # OWASP ZAP API scan (ZAP_MAX_MINUTES, default 20)
```
For the contract run: `python3 -m venv tools/contract/.venv && tools/contract/.venv/bin/pip install -r tools/contract/requirements.lock`.

## Run the service
```sh
DATABASE_URL=postgres://… AMQP_URL=amqp://… OIDC_ISSUER=https://… OIDC_AUDIENCE=cook-assistance \
PUBLIC_BASE_URL=https://larder.org/cooking-assistance PORT=8080 RECIPE_CATALOG_URL=https://… \
node --import tsx src/main.ts
```
Migrations run at start; logs are one JSON line per event on stdout. Until a client-credentials token provider for
Recipe Catalog exists, requests that need a step lookup answer 503 (ADR-CA-015, amended).
`npm run rebuild-views` (with `DATABASE_URL`) rebuilds the read models from the outbox.

## Where things are
- Code: `src/cook-assistance` — `domain`, `application`, `adapter/{in,out,messaging}`, composition root
  (`composition.ts`, `service.ts`), `src/main.ts`; generated code in `src/generated` (never edited)
- Contracts: `contracts/` (OpenAPI and AsyncAPI 2.0.0, the released baseline, the changelog, the upstream spec)
- Design documents and ADRs: `docs/cook-assistance/`
- Reviews: `reviews/` (independent inspection and security review per increment)
- The run's ledger: `implementation-log.md`
