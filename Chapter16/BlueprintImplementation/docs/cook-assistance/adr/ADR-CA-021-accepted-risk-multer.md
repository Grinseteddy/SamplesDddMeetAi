# ADR-CA-021 — Accepted risk: multer GHSA-q7qp-rjg7-q26m through @nestjs/platform-express

Status: accepted (2026-10-26) · Context: Cook Assistance · Step 14 (security review increment 0, finding S-4)

## Context
`@nestjs/platform-express` 11.2.7 depends on multer 2.4.0, which has GHSA-q7qp-rjg7-q26m (denial of service through
a pending asynchronous `fileFilter` after the request is torn down; high, CVSS 7.5). `npm audit` and OSV-Scanner
report it; their suggested fix is a downgrade of `@nestjs/platform-express` (11.1.1), not a fix.

## Decision
The risk is **accepted** for Cook Assistance:
- **Why it is not reachable:** multer only runs for multipart requests through Nest's file interceptors. The service
  has none — no multipart operation in the approved OpenAPI spec (request bodies are `application/json` only, 415
  otherwise), no `FileInterceptor`, no multer import; the gate test in `edge.spec.ts` ("no multipart handling or
  FileInterceptor in src") fails the build if that changes.
- **Owner:** the Cook Assistance service team (the book author for this sample).
- **Re-check:** at every increment's security review, and as soon as a multer release fixes the advisory (then
  update `@nestjs/platform-express` and drop the acceptance).
- **Tools:** the security baseline (`tools/security/run-baseline.sh`) accepts exactly this advisory — `npm audit`
  through an allow list of one GHSA, OSV-Scanner through `tools/security/osv-scanner.toml` with this reason and a
  re-check date — and fails on any other finding.

## Consequences
The build stays red for every new advisory. A multipart operation (Catastrophe pictures arrive as Media URLs, INV-24,
so none is planned) would end the acceptance.

## Principles and quality scenarios
Trades off the security goal for this one advisory, by the gate above; honors AP0005 (the gate is automated).
