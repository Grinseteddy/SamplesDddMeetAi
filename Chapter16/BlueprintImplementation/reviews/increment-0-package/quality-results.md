# Quality-scenario results, increment 0 (step 12)

Environment: laptop, Docker Desktop, Testcontainers PostgreSQL 16 and RabbitMQ 3.13, k6 2.3.0 in Docker; CI on
GitHub Actions ubuntu-24.04. "Indicative" = the environment or duration differs from the scenario's.

| QS | Result | Measured | Test or gate |
|---|---|---|---|
| QS-01 | met | 0 dependency-rule violations | `npm run depcruise` (`.dependency-cruiser.cjs`) |
| QS-02 | met | verify 2 min 12 s – 2 min 43 s on CI | `.github/workflows/cook-assistance.yml` job verify |
| QS-03 | met | 0 breaking changes vs. released 2.0.0 | `src/cook-assistance/contract/spec-compatibility.spec.ts` |
| QS-04 | met, indicative | broker paused 20 s (not 10 min); 5/5 commands 201; all published ≤ 30 s after resume | `src/cook-assistance/quality/reliability.spec.ts` |
| QS-07 | met | poison message dead-lettered; relay backoff then dead letter | `contract/asyncapi.contract.spec.ts`, relay and binding tests |
| QS-08 | met, indicative | POST p95 22 ms at 10/s for 60 s | `quality/performance.spec.ts`, `tools/load/commands.js` |
| QS-09 | met, indicative | GETs p95 18 ms; last page p95 21 ms, on 120 000 requests | `quality/performance.spec.ts` |
| QS-10 | met, indicative | queue depth max 0 at 5 msg/s for 120 s (not 10 min) | `quality/performance.spec.ts` |
| QS-11 | met | read-your-writes, lag 0 | http wiring test "read-your-writes, lag 0" |
| QS-12 | met, indicative | commit → publication p95 511 ms over 20 commands | `quality/reliability.spec.ts` |
| QS-14 | met, indicative | 2 instances, 11 events, 11 distinct message ids | `quality/reliability.spec.ts` |
| QS-15 | met | second save → ConcurrencyConflict / 409 | repository and HTTP tests |
| QS-16 | met, indicative | one year of data seeded (120 000 requests + helps), lists paged | `quality/performance.spec.ts` |
| QS-19 | — | verified by review (this inspection) | — |
