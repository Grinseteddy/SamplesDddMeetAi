# ADR-CA-010 — Security baseline and security tools

Status: accepted (2026-10-24) · Context: Cook Assistance · Step 5, question 10

## Context
Security is quality goal 3. Step 14 reviews every increment against the OWASP Top 10 and the OWASP API
Security Top 10 and needs tools wired into the build from the walking skeleton on.

## Decision
- **Secrets** only from the environment (database, broker, issuer settings); `.env` files never
  committed; no secret in code, config, URLs or logs.
- **Request limits** in the service: JSON body ≤ **1 MB**; rate limit **60 requests/min per cook**
  (`@nestjs/throttler`, keyed by `cookId`, anonymous by IP) → 429 Problem; list `size` ≤ 100 (spec).
- **Security headers** with `helmet` (no `X-Powered-By`, `Content-Security-Policy: default-src 'none'`
  for the JSON API, `Strict-Transport-Security`).
- **Logging**: structured, without personal data (no question texts, no names, cook IDs only where an
  operator needs them); refused accesses logged as security events (QS-19).
- **Tools** (run from the one build command and in CI from increment 0):
  - dependency audit: `npm audit --audit-level=high` and OSV-Scanner;
  - secret scan: gitleaks (also as a pre-commit hook);
  - static analysis: Semgrep (`p/typescript`, `p/owasp-top-ten`) and `eslint-plugin-security`;
  - dynamic scan: OWASP ZAP API scan of the running service against the OpenAPI spec (on the pull request).

## Consequences
Findings of these tools are triaged in step 14 like contract-test findings: confirmed or false positive
with a reason.

## Principles and quality scenarios
Honors AP0008, AP0007. Serves the security goal, QS-19.

## Amendment — 2026-10-26 (step 13, inspection increment 0, defects M-3, m-6, m-8, m-9)
- **Rate limit before authentication and validation:** `@nestjs/throttler` is replaced by the adapter's own
  per-instance limiter (`edge-limits.ts`, fixed one-minute window, 60 requests, `Retry-After`). The order is
  identify (valid token → cook, otherwise the reason) → count (by cook, otherwise by address) → refuse with 401 →
  parse and validate. A Nest guard ran after the Express middleware, so 401 and 400 floods were never counted.
- **Security events:** every 401 and 403 is logged as `access-refused` with status, code, method, path and reason
  (the token error or the missing scope or role) — no token, no cook id, no body (QS-19). Every 500 is logged as
  `internal-error` with method, path and the error's name (AP0007).
- **No subject, no actor:** a validly signed token without `sub` is answered with 401 (ADR-CA-008's `sub` → cookId).
