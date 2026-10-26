# Security review package — Cook Assistance, increment 0 (walking skeleton)

Root: `Chapter16/BlueprintImplementation/` in the repository `SamplesDddMeetAi`.

## Scope
The whole code of increment 0 (walking skeleton): slices **B2-S01** Request help preparation step (HTTP
`POST /help-requests`) and **B1-S04** Provide Help from the Grandma Avatar AI message (AMQP), plus the four served GET
operations getHelpRequestById, listHelpRequests, getHelpById, listHelpsForHelpRequest. Hand-written code in
`src/cook-assistance/**`, `src/main.ts`, `src/rebuild-views.ts`; generated code `src/generated/**` from `tools/codegen`;
build and delivery: `package.json`, both lockfiles, `tools/**`, `.github/workflows/cook-assistance.yml` and `.husky/`
at the repository root.

## Approved artifacts
- OpenAPI `contracts/openapi/cook-assistance.openapi.yaml` (security scheme, scopes, error responses), AsyncAPI
  `contracts/asyncapi/cook-assistance.asyncapi.yaml`, upstream `contracts/upstream/recipe-catalog.openapi.yaml`
- ADRs `docs/cook-assistance/adr/` — above all ADR-CA-008 (authentication), ADR-CA-009 (authorization, the
  operation → scope → role table, resource checks, visibility "hidden = 404"), ADR-CA-010 (security baseline and
  tools, with its amendment), ADR-CA-004 (messaging), ADR-CA-015 (Recipe Catalog), ADR-CA-006 (error map)
- Board actors and commands: COOK requests help (B2-S01); the Grandma Avatar AI answers by message (B1-S04);
  COMMUNITY_COOK and CHEF answer by `provideHelp` (planned, increment 1); the command → actor table is ADR-CA-009 §1
- Quality scenarios `docs/cook-assistance/06-quality-scenarios.md` (QS-17, QS-18, QS-19 data protection and
  accountability)
- Principles `reviews/increment-0-package/principles.md` (AP0008 fine-grained access rights)
- The step 13 inspection `reviews/increment-0-inspection.md` (security-relevant defects already fixed: M-3, m-8, m-9,
  N-1)

## Planned, not missing
Operations cancelHelpRequest, requestChefReview, provideHelp, acceptHelp, rejectHelp and their channels; premium and
Chef checks beyond the schema (INV-02, INV-03); retention and erasure (QS-17, QS-18, increment 4); the
client-credentials token for Recipe Catalog (ADR-CA-015 amendment: lookups answer 503 until it exists). Accepted
risks or deferred findings from earlier increments: none (this is the first security review).

## Test target — safety boundary
- Only the local **test** instance, never anything else: start it with
  `CONTRACT_PORT=8098 node --import tsx src/cook-assistance/contract/serve-for-contract.ts` (from the project root;
  PostgreSQL, RabbitMQ and a Recipe Catalog stub in Testcontainers; prints `ready <url>`), stop it with SIGTERM.
  Base path `http://127.0.0.1:8098/cooking-assistance`, header `version: 2.0.0`.
- Identities: `reports/security/identities.json` (written at start): cook-a (requester of the Sunday scones example),
  cook-b (another cook), a community helper, a Chef, a cook without scopes, an expired token, a token without `sub`.
  The Grandma Avatar AI publishes on the exchange `grandma-avatar`, routing key `grandma-avatar.help.provided`; the
  broker URL is in the service's Testcontainers setup (`src/cook-assistance/adapter/out/messaging/test-support`).
- Limits: stay below 60 requests per minute and identity (the API's rate limit); no load, no denial-of-service
  attempts, no scans of anything but this instance.

## Tool results (pinned, run before the review; triage each: confirmed or false positive with a reason)
- `reports/security/npm-audit.json` — `npm audit --audit-level=high` of the service; `npm-audit-codegen.json` of the
  generators
- `reports/security/gitleaks.json` — gitleaks v8.30.1 over every commit that touched the service folder (81 commits)
- `reports/security/semgrep.json` — Semgrep 1.180.0, `p/typescript` and `p/owasp-top-ten`
- ESLint with `eslint-plugin-security` 4.2.0 — run `npx eslint src/` and read the `security/*` warnings
- `reports/security/zap-report.json` / `.html` — OWASP ZAP 2.17.0 (image `zaproxy/zap-stable:20261008`) API scan
  against the OpenAPI spec with a COOK token (`tools/security/run-zap.sh`)
- `reports/security/osv-scanner.json` — OSV-Scanner v2.6.0 (`ghcr.io/google/osv-scanner:v2.6.0`) over the three
  lockfiles: service `package-lock.json`, `tools/codegen/package-lock.json`, `tools/contract/requirements.lock`

## Report format
For every one of the 20 categories (OWASP API Security Top 10:2023 API1–API10, then OWASP Top 10:2025 A01–A10):
*checked — no finding*, *finding*, or *not applicable*, each with its reason. Each finding: OWASP category,
severity (critical / high / medium / low), location (file:line or request), evidence (a request and response, a code
path, a tool result), the ADR or spec element it violates, and an abuse-case test specification traced
`-- B2-S01 OWASP-API1` (Given/When/Then, the expected safe response). Tool findings triaged as confirmed or false
positive with a reason.
