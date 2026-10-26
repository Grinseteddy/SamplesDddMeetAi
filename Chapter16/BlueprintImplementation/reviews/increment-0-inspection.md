# Inspection report — increment 0 (walking skeleton: B2-S01, B1-S04)

2026-10-26 · Moderator: the orchestrator (Opus 5.5) · Author: the agent that ran steps 6–12 · Decider: the user.
Route: inspected **without fagan-code-inspector** (not installed); inspectors briefed with the blueprint's checklist
and defect format. Package: `reviews/increment-0-package/` (overview, principles, quality results), the code, specs,
documents and `reports/intent-coverage.md`; excluded: the ledger, git history, author notes.

| Inspector | Model | Focus | Major | Minor |
|---|---|---|---|---|
| A | Sonnet 5.5, fresh context, read-only | domain | 0 | 5 |
| B | Sonnet 5.5, fresh context, read-only | architecture | 3 | 11 |

No duplicates between the two reports. Defect IDs below are the merged ones (A-n / B-n = the inspector's own).

## Defects

| ID | Sev | Location | Violates | Finding |
|---|---|---|---|---|
| M-1 (B-1) | major | adapter/out/messaging/rabbitmq.ts:3, send-check.ts:4 | ADR-CA-002, QS-01 | outbound adapter imports from inbound adapter; no depcruise rule forbids adapter→adapter |
| M-2 (B-2) | major | .dependency-cruiser.cjs `exclude ^src/generated` | QS-01, ADR-CA-011 | generated code excluded from the graph, so a domain/application import of generated types cannot fail the gate |
| M-3 (B-3) | major | adapter/in/http authentication.ts, authorization.ts, http-app.ts:39 | ADR-CA-010, QS-19, AP0007 | refused accesses (401/403) are not logged as security events; no log port in the HTTP adapter |
| m-1 (A-1) | minor | features/preparation-step.feature; intent coverage R7 ex.1 | Example Mapping R7 ex.1 | R7 ex.1 ("open at 14:59, cancelled at 15:00") counted covered by a deadline-calculation test only; INV-07/08 not in the coverage rows |
| m-2 (A-2) | minor | acceptance/steps/preparation-step.steps.ts:114, :133 | R1 ex.2, R3 ex.2–3, R8 ex.1 | four rejection scenarios assert only status 400, not which rule refused |
| m-3 (A-3) | minor | application/provide-help-when-grandma-avatar-answers.ts:101 | ADR-CA-009 | INV-09 refusal is a free string, not the typed `NotAnAddressee` the ADR names |
| m-4 (A-4) | minor | domain/help-request/help-request.ts:9, application/request-preparation-step-help.ts:45 | checklist 5 | domain command data and use case share the name `RequestPreparationStepHelp` |
| m-5 (A-5) | minor | domain/help-request/help-request.spec.ts:212 | checklist 4 | test name promises "no event, no help request"; only `rejection` defined can fail |
| m-6 (B-4) | minor | adapter/in/http/problem.ts:53 | AP0007, ADR-CA-006 | unexpected errors become 500 without any log |
| m-7 (B-5) | minor | adapter/in/http/problem.ts:24-26 | ADR-CA-006 | Problems never carry `instance`; 500 has no `detail` |
| m-8 (B-6) | minor | adapter/in/http/token-verifier.ts:29 | ADR-CA-008 | a signed token without `sub` becomes cookId "" instead of 401; shared rate-limit bucket |
| m-9 (B-7) | minor | http-app.ts:43-46, edge-limits.ts:12-14 | ADR-CA-010 | throttler runs after authentication and validation: 401/400 floods and anonymous callers are not rate-limited |
| m-10 (B-8) | minor | rabbitmq.ts:49-57, outbox-relay.ts:74-100, relay-poller.ts:23-27 | ADR-CA-004, QS-07 | no publish timeout: a broker outage holds the relay transaction and row locks, attempts never advance, shutdown hangs; claimRound has no LIMIT |
| m-11 (B-9) | minor | adapter/in/messaging/grandma-avatar-help-provided.consumer.ts:40-45 | ADR-CA-004, QS-07 | TopicNotYetSupported is acked with no log, record or dead letter (silent loss) |
| m-12 (B-10) | minor | reviews/increment-0-package/quality-results.md QS-07 | QS-07 | reported "met" though the alert measure (≤ 5 min) is unverified |
| m-13 (B-11) | minor | quality/reliability.spec.ts QS-04, QS-14; quality-results | QS-04, QS-14 | order per aggregate untested; QS-14 test unlikely to catch a missing lock; timeout-runner half untested but "met" |
| m-14 (B-12) | minor | tools/load/queries.js, last-page.js; one-year-of-data.ts | QS-09, QS-16 | only COOK role loaded; the helper visibility path over all open requests is unmeasured |
| m-15 (B-13) | minor | read-model/projections.ts:124 | ADR-CA-012 | no rebuild command (rebuildViews only used by a test) |
| m-16 (B-14) | minor | vitest.config.ts, verify | AP0005 | the 95 % automatic-testing principle is not measured |

## Decisions, rework, follow-up
- 2026-10-26: all 19 **accepted** by the user (0 rejected, 0 deferred). Rework in owning-step order: step 12 rules (M-2, M-1 rule) → step 10 (M-1 code, m-10) → step 9 (M-3, m-6–m-9, m-11) → step 8 (m-3) → step 11 (m-15) → step 6 (m-4, m-5) → step 12 (m-1, m-2, m-12–m-14) → step 5 (m-16); then step 12 again, the scoped reverse check, and a re-inspection of the changed units by a new inspector (majors were reworked).

