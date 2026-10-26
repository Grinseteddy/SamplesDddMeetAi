# Cook Assistance — project memory

Implementation blueprint run for the bounded context **Cook Assistance** (book *DDD meets AI*, chapter 16), with
EXACT Coding (Predictive TDD). The ledger `implementation-log.md` says where the run stands; read it before
continuing ("continue at step N", "implement the next board").

## Documents
- Design: `docs/cook-assistance/01-boards-and-slice-tables.md`, `02-slice-plan.md` (slices, board order, increments),
  `03-design-brief.md`, `04-invariants-sheet.md` (INV-xx), the Example Mappings, `06-quality-scenarios.md` (QS-xx)
- Decisions: `docs/cook-assistance/adr/ADR-CA-001` … `ADR-CA-021` (read the amendments at the end of each)
- Contracts: `contracts/openapi`, `contracts/asyncapi` (2.0.0), `contracts/released/2.0.0` (diff baseline),
  `contracts/CHANGELOG.md`
- Reviews: `reviews/increment-<k>-inspection.md`, `reviews/increment-<k>-security.md`
- Planned list: `tools/intent-coverage/planned.json` (what each later increment must build; empty after the last)

## Conventions
- Hexagonal layout enforced by `.dependency-cruiser.cjs` (ADR-CA-002): domain imports only the domain; application only
  domain and application; inbound and outbound adapters never import each other; shared messaging in
  `adapter/messaging`.
- Every test name ends with its trace after `--` (slice, INV, Example Mapping `R<n> ex.<k>`, operation, ADR, QS);
  acceptance scenarios carry `@B2-S01 @R1 @ex.1` tags. The intent-coverage report reads them.
- Test first: inactive test list (`it.todo`), a falsifiable prediction before every check, red for the predicted
  reason, minimal green, refactor under the Four Rules. Prediction mismatches are stated, never hidden.
- An approved test is never changed to fit new code without the user's decision.
- One build command: `npm run verify` (green before every commit). Load, Schemathesis and ZAP run on pull requests.
- Conventional Commits, checked by commitlint (header ≤ 100 characters, subject not capitalized, body lines ≤ 100).
  Hooks never bypassed.
- Only two paths at the repository root belong to this service: `.github/workflows/cook-assistance.yml` and `.husky/`.
- Dependencies: the newest release at least two weeks old, pinned exactly; `npm run security` must stay green
  (accepted advisories only by ADR, e.g. ADR-CA-021).
