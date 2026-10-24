# Cook Assistance — quality goals and quality scenarios (step 5a–5b)

2026-10-24. No Architecture Knowledge Graph available; every scenario comes from the user's answers to
the eight critical quality questions. Priority = importance × difficulty (H/M/L, ATAM utility tree).
Verification: **test** (step 12) · **review** (step 13) · **both** · **not here** (open verification).

## Quality goals (ranked, ISO/IEC 25010)

1. **Maintainability** — modularity, modifiability, testability
2. **Reliability** — fault tolerance, recoverability, availability
3. **Security** — confidentiality, integrity, accountability (AP0008 fine-grained access)

## Scenarios

Six parts: **Src** source · **Stim** stimulus · **Env** environment · **Art** artifact · **Resp** response · **Meas** response measure.

| ID | Characteristic | Scenario | Prio | Verify |
|---|---|---|---|---|
| QS-01 | Maintainability · modularity | Src developer · Stim changes or adds an adapter or a use case · Env development · Art domain and application modules · Resp the domain imports no framework, HTTP, database or broker type; adapters depend inward only · Meas 0 architecture-rule violations in CI | H/M | test |
| QS-02 | Maintainability · testability | Src developer · Stim pushes a commit · Env CI · Art the one build command (generate, compile, lint, types, all tests incl. containers) · Resp the build finishes · Meas ≤ 10 min (load tests excluded, run on the pull request) | M/M | test |
| QS-03 | Compatibility · interoperability | Src developer · Stim changes an approved spec within major 2 · Env CI · Art OpenAPI and AsyncAPI 2.x · Resp the spec diff against the last released 2.x fails the build on any breaking change; a breaking change needs a new major with ≥ 6 months deprecation · Meas 0 undetected breaking changes | H/M | test |
| QS-04 | Reliability · fault tolerance | Src RabbitMQ · Stim broker unavailable for 10 min · Env normal load · Art command handling, outbox, relay · Resp commands keep succeeding; their events are stored and published after recovery, in order per aggregate · Meas 100 % of commands accepted during the outage; all stored events published ≤ 2 min after the broker is back; no duplicate beyond the message ID | H/H | test |
| QS-05 | Reliability · fault tolerance | Src PostgreSQL (or the chosen DB) · Stim database unavailable for 5 min · Env normal load · Art queries and commands · Resp queries are served from a cache (marked as possibly stale); commands answer 503 Problem; the service recovers without restart · Meas 100 % of GETs on cached resources answered 200; 0 partial writes; normal operation ≤ 1 min after the DB is back | H/H | test |
| QS-06 | Reliability · availability | Src operations · Stim any failure · Env production · Art the running service · Resp service available; restart and recovery · Meas availability 99.5 % per month; RTO 30 min; RPO 0 (no committed data lost) | M/H | **not here** — needs production; owner: platform team |
| QS-07 | Reliability · recoverability | Src relay or consumer · Stim a message fails repeatedly (poison message, consumer down) · Env normal operation · Art outbox relay, Grandma Avatar consumer · Resp bounded retries with backoff, then dead letter; the team is alerted; loss of an event is accepted only as a dead letter, never silently · Meas 0 silently lost events; dead letter alert ≤ 5 min | M/M | test |
| QS-08 | Performance · time behavior | Src cooks · Stim 10 requests/s on command operations (POST) · Env peak load · Art Help API incl. the Recipe Catalog lookup · Resp commands answered · Meas p95 ≤ 300 ms | M/L | test (indicative) |
| QS-09 | Performance · time behavior | Src cooks and helpers · Stim 10 requests/s on queries (GET), one year of data (QS-16) · Env peak load · Art read models · Resp queries answered · Meas p95 ≤ 200 ms | M/L | test (indicative) |
| QS-10 | Performance · capacity | Src Grandma Avatar AI · Stim 5 messages/s · Env peak load · Art consumer · Resp messages consumed · Meas queue depth does not grow over 10 min | L/L | test |
| QS-11 | Functional suitability · consistency | Src cook · Stim a command succeeds · Env normal load · Art read models (GET /help-requests, /helps) · Resp the change becomes visible to everyone · Meas ≤ 5 s (eventual; no read-your-writes guarantee) | M/M | test |
| QS-12 | Performance · time behavior | Src cook · Stim a command succeeds · Env normal load, broker up · Art outbox relay · Resp the event is published · Meas ≤ 2 s p95 from commit to publication | H/M | test |
| QS-13 | Functional suitability · correctness | Src clock · Stim an answer deadline passes (Catastrophe 10 min … Menu 48 h) · Env normal operation · Art timeout runner · Resp the request is cancelled and *Help request cancelled* published · Meas ≤ 1 min after the deadline | H/M | test |
| QS-14 | Maintainability · scalability readiness | Src operations · Stim a second instance is started · Env production later (1 instance now) · Art the service · Resp no in-memory locks or state; exactly one timeout runner and safe concurrent relays · Meas with 2 instances: no duplicate cancellation, no duplicate publication beyond the message ID | M/M | both |
| QS-15 | Reliability · integrity | Src two actors · Stim change the same aggregate concurrently (two helpers; cook cancels while the timeout fires) · Env normal load · Art aggregates, repository · Resp optimistic locking: one wins, the other gets 409 (API) or a retry (policy handlers) · Meas 0 lost updates | H/M | test |
| QS-16 | Performance · capacity | Src cooks · Stim ~10 000 help requests per month · Env one year of data (~120 000 requests and their helps) · Art database, read models, paged GETs · Resp queries stay within QS-09; lists are paged | L/L | test |
| QS-17 | Security · confidentiality (data protection) | Src retention job · Stim a request ended (closed or cancelled) 12 months ago · Env normal operation · Art requests, helps, read models, outbox · Resp the request and its helps are deleted everywhere · Meas deleted ≤ 1 day after the 12 months | M/M | test |
| QS-18 | Security · confidentiality (GDPR erasure) | Src Cook Profile / data-protection officer · Stim erasure request for a cook · Env normal operation · Art requests, helps, read models, outbox payloads · Resp the cook's requests are deleted; helps the cook gave are kept but anonymized · Meas ≤ 30 days; 0 occurrences of the cook ID afterwards | H/M | both |
| QS-19 | Security · accountability | Src any actor · Stim a state change or a refused access · Env normal operation · Art published events, security-event log · Resp every state change is an event (the audit trail); refused accesses are logged without personal data · Meas 100 % of state changes as events; 0 personal data in security logs | M/L | review |
| QS-20 | Maintainability · analysability (AP0007) | Src operations · Stim runs normally or fails · Env production-like · Art logs, metrics, traces, health · Resp structured JSON logs; metrics for requests, latencies, outbox backlog, timeouts fired, dead letters; OpenTelemetry traces; health and readiness endpoints · Meas every named metric exists; readiness reflects DB and broker | M/L | test |
| QS-21 | Maintainability · analysability | Src cook · Stim a request with a correlation ID header · Env normal operation · Art HTTP, logs, outbox, published message · Resp the same correlation ID appears in every log line and in the message's `correlationId` | M/L | test |
| QS-22 | Reliability · recoverability | Src monitoring · Stim a dead letter, an outbox backlog older than 5 min, or a failed readiness check · Env production · Art alerting · Resp the team is alerted · Meas alert ≤ 5 min | M/M | both — metrics in step 12; routing **not here** (owner: team / platform) |

## Notes for the decisions (5c–5f)

- QS-04 (keep accepting while the broker is down) needs events stored durably → transactional outbox; QS-07 bounds the retries.
- QS-05 (reads from a cache while the database is down) needs a cache outside the database.
- QS-11 (≤ 5 s eventual) allows asynchronous projections; QS-12 and QS-13 need a relay and a timeout runner that poll often enough.
- QS-14 needs single-runner coordination (e.g. a database lease) for the timeout runner and safe concurrent relays.
- Security goal (rank 3) has no scenario of its own yet beyond QS-17–QS-19; authentication and authorization are decided in 5e.
