# Security review — increment 0 (walking skeleton: B2-S01, B1-S04)

2026-10-26 · Moderator: the orchestrator (Opus 5.5) · Reviewer: a separate agent (Sonnet 5.5, fresh context) with
`owasp-security-reviewer` in reviewer mode, no write access to the code · Decider: the user.
Package: `reviews/increment-0-security-package/overview.md`. Test target: the local contract instance on
127.0.0.1:8098 only, below 60 requests per minute and identity, no load.

## Tools (pinned, ADR-CA-010)
| Tool | Result | Triage |
|---|---|---|
| npm audit (service / generators) | 2 high entries = one advisory (multer via @nestjs/platform-express) / 0 | confirmed, not reachable → S-4 |
| OSV-Scanner v2.6.0 (3 lockfiles) | multer GHSA-q7qp-rjg7-q26m; pip 9.0.3 (18 advisories) in tools/contract | multer confirmed (S-4); pip false positive — no pip line in the lock, venv pip 26.2.2 |
| gitleaks v8.30.1 (81 commits of the service) | 0 | — |
| Semgrep 1.180.0 (p/typescript, p/owasp-top-ten) | 0 | — |
| eslint-plugin-security 4.2.0 | 7 warnings | false positives (typed keys, constant paths); hardening note on visibility.ts:14 (roles index a plain object) |
| ZAP 2.17.0 API scan (COOK token, 1 thread, 1.1 s delay, active scan capped at 20 min; 13 URLs) | FAIL 0, WARN 2, PASS 119 | 100000 server error ×6 (planned operations cancellation/acceptance/rejection, paths outside the API) → confirmed, S-1; 90022 application error disclosure ×3 → false positive as a disclosure (generic Problem body, no internals), same cause as S-1; informational 10049 non-storable content fits S-2. Coverage: the run reached the cap (20:36); the report does not show whether every URL and rule finished — the 119 passes may be incomplete |

## Verdicts (20/20)
| Category | Verdict |
|---|---|
| API1 BOLA | checked — no finding (cook-b, Chef 404 on cook-a's request and help; visibility in SQL) |
| API2 Broken authentication | finding S-3 (alg:none, HS256, tampered, expired, no sub, wrong schemes → 401) |
| API3 Object property level | checked — no finding (extra properties ignored; forged helpRequester ignored) |
| API4 Resource consumption | checked — no finding (413, 400 on size/page; 429 by code only) |
| API5 Function level | finding S-1 (deny by default holds; unserved operations answer 500) |
| API6 Sensitive business flows | checked — no finding |
| API7 SSRF | checked — no finding (only Recipe Catalog, base URL from env, ids uuid-validated) |
| API8 Misconfiguration | finding S-2 |
| API9 Inventory | finding S-1 (no undocumented endpoints; outside paths 500) |
| API10 Unsafe consumption | checked — no finding (AMQP: source and schema enforced, dead-lettered) |
| A01 Broken access control | checked — no finding |
| A02 Security misconfiguration | finding S-2 |
| A03 Software supply chain | finding S-4 |
| A04 Cryptographic failures | checked — no finding |
| A05 Injection | checked — no finding |
| A06 Insecure design | checked — no finding (broker source trust per ADR-CA-008; broker permissions outside this repo) |
| A07 Authentication failures | checked — no finding |
| A08 Integrity failures | checked — no finding |
| A09 Logging and alerting | checked — no finding (by code; the test instance logs nothing) |
| A10 Exceptional conditions | finding S-1 |

## Findings (critical 0 · high 0 · medium 0 · low 4)
| ID | Sev | Category | Finding | Abuse-case test |
|---|---|---|---|---|
| S-1 | low | A10, API9, API5 | paths outside the server path and spec operations not served yet answer 500 InternalError (and raise internal-error events) instead of 404/501 — problem.ts fallback, Nest NotFoundException unmapped | GET /x, /cooking-assistance/nonexistent, POST …/cancellation → 404 (501 for a planned spec operation) Problem, no internal-error event -- B2-S01 OWASP-A10 |
| S-2 | low | API8, A02 | no `Cache-Control: no-store` on authenticated personal data; the listener binds all interfaces (service.ts) | GET /help-requests/{id} carries Cache-Control: no-store -- B2-S01 OWASP-API8; the process binds the configured host |
| S-3 | low | API2 | the 401 detail echoes JOSE error codes (ERR_JWT_EXPIRED, …), so an unauthenticated caller learns why its token failed (ADR-CA-008: "learns nothing … but 401") | expired and tampered tokens → 401 with the same generic detail; the reason only in the log -- B2-S01 OWASP-API2 |
| S-4 | low | A03 | multer 2.4.0 (GHSA-q7qp-rjg7-q26m, DoS) via @nestjs/platform-express 11.2.7; no multipart in src or spec (gate edge.spec) — unreachable | the existing gate: no multipart operation, no FileInterceptor -- B2-S01 OWASP-A03 |

Not tested (reviewer): the 429 limit live, wrong-audience/issuer tokens (no foreign key), real Keycloak/JWKS, broker
permissions, real log output, TLS and deployment, planned operations.

## Decisions, fixes, re-test
- 2026-10-26 (user): **S-1, S-2, S-3 and the roles-lookup hardening deferred** to increment 1 — Board: Meal plan,
  which must close them (abuse-case tests as specified above, red first in steps 8–10). **S-4 accepted** by
  ADR-CA-021 (owner: the service team; re-check every increment's security review and when multer is fixed).
- Baseline wired into the build: `npm run security` (npm audit with the one accepted GHSA, OSV-Scanner with
  `tools/security/osv-scanner.toml` — the accepted GHSA and the triaged pip false positive, gitleaks over the history,
  Semgrep) as the last step of `npm run verify`; ZAP as the pull-request job `dast` (`npm run test:dast`). Trial: the
  audit check with an empty allow list fails on GHSA-q7qp-rjg7-q26m.
- No fixes in this round, so no re-test of changed operations.
