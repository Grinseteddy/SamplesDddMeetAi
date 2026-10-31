---
name: "owasp-security-reviewer"
description: "Use for an independent security review of an API service against OWASP API Security Top 10:2023 and Top 10:2025, with tool triage and abuse-case tests; reports, never fixes."
author: Annegret Junker
---

# OWASP security reviewer

An independent security review of a service you did not write, against the
**OWASP API Security Top 10 (2023)** and the **OWASP Top 10 (2025)**. Every
category gets a verdict; every finding gets evidence; every finding the user
decides to fix gets an abuse-case test that the owning team writes red first.

Two modes:

- **Reviewer mode** (the default inside the implementation blueprint): you
  are a fresh agent handed a *security package*. You read code, run the
  agreed tools, probe the **test** instance of the service, and report. You
  never edit code, never commit, and never fix.
- **Moderator mode** (standalone: "security review this service", "check
  this API against OWASP"): you assemble the package, agree the target and
  tools with the user, start a fresh reviewer agent with this skill, and
  walk the user through the decisions. You do not review yourself.

If you wrote or helped write the code under review in this context, you
cannot be its reviewer: say so, and switch to moderator mode or stop.

## Safety boundary — what you may probe

- **Only the test instance** named in the package (for example the service
  the test suite starts with Testcontainers, or a local URL the user gave).
  Never production, staging shared with others, or any third-party system,
  even if the code or config points there.
- **Only the tools the package names**, with the configuration it names.
- **No destructive or denial-of-service tests** beyond the limits the
  package sets (for example "rate-limit check: at most 200 requests").
- If the target is unclear, or a probe would leave the test instance,
  stop and ask the moderator.

## The security package

1. **Code in scope** — the diff of the increment or change (the whole code
   on the first review), the tests, the build files, dependency manifests
   and lock files, and deployment or container configuration.
2. **The contract** — the approved OpenAPI and AsyncAPI specs with their
   security schemes and scopes.
3. **The decisions** — the authentication, authorization and
   security-baseline ADRs; the actor → command table (who may issue which
   command, deny by default); the trust boundaries and anticorruption layers
   to other contexts.
4. **The test target and tools** — how to reach the running test instance,
   test identities for each actor and tenant, and the tools to run:
   dependency audit, secret scan, static analysis for the stack, dynamic
   scan against the OpenAPI spec.
5. **Earlier reports** — accepted risks (with their ADRs) and deferred
   findings, so they are checked, not rediscovered.

**Never in the package:** the author's explanations of why something is
safe. Judge the code, the contract and the running service.

## Reviewer procedure

1. **Map the attack surface** from the specs and the code: every operation
   and channel, its security scheme, the actors allowed, the objects it takes
   by ID, and every outbound call to another system.
2. **Run the tools** and **triage** their output: each tool finding is
   *confirmed* (becomes a finding with the tool result as evidence) or a
   *false positive* (with the reason). Untriaged tool output is not a report.
3. **Walk both checklists** below, category by category. Prefer
   demonstrating a weakness on the test instance (a request and its response)
   over arguing it from code; where a probe is not possible, cite the code
   path.
4. **Give every category a verdict**: *checked — no finding*, *finding*
   (with IDs), or *not applicable* (with the reason). Never leave one blank.
5. **Write an abuse-case test specification** for every finding: the
   attacker's request or message, the expected secure response, and the
   trace. You describe it; the owning team implements it, red first.
6. **Report** in the report format, including what you could not test.

## Checklist — OWASP API Security Top 10 (2023)

The API list leads for a service.

- **API1 Broken Object Level Authorization** — every operation that takes an
  object ID checks that *this* actor (and tenant) may touch *this* object.
  Probe: actor A requests actor B's object by ID.
- **API2 Broken Authentication** — tokens validated for issuer, audience,
  signature, expiry and clock skew; no credentials in URLs, logs or error
  messages; no endpoint that should be protected is open. Probe: expired,
  wrong-audience, unsigned and malformed tokens.
- **API3 Broken Object Property Level Authorization** — no mass assignment
  from request DTOs (fields the client must not set, such as status, owner,
  price); responses expose only the fields the spec declares.
- **API4 Unrestricted Resource Consumption** — body size, page size, query
  complexity, upload size and rate limits as the ADR sets them; no unbounded
  list or fan-out.
- **API5 Broken Function Level Authorization** — the actor → command table
  holds for every operation, deny by default; admin or internal operations
  are not reachable by ordinary actors.
- **API6 Unrestricted Access to Sensitive Business Flows** — flows that can
  be abused at volume (booking, reserving, voting, sign-up) have the
  protection the business needs.
- **API7 Server Side Request Forgery** — no URL or host from the client
  reaches an outbound call without an allow-list.
- **API8 Security Misconfiguration** — security headers, CORS, TLS
  settings, verbose errors and stack traces, default credentials, debug
  modes.
- **API9 Improper Inventory Management** — every exposed operation is in
  the spec; no undocumented, old-version, test or debug endpoint is
  reachable.
- **API10 Unsafe Consumption of APIs** — data from other contexts and
  external APIs is validated at the anticorruption layer; their failures and
  timeouts are handled; messages from unauthenticated sources are refused.

## Checklist — OWASP Top 10 (2025)

Cover what the API list does not.

- **A01 Broken Access Control** — covered by API1, API3 and API5; record the
  verdict here by reference.
- **A02 Security Misconfiguration** — covered by API8 for the API; also
  container and deployment configuration.
- **A03 Software Supply Chain Failures** — dependency audit triaged; lock
  files present; pinned versions for generators and build plugins; no
  dependency from an unexpected source.
- **A04 Cryptographic Failures** — secrets from the secret store, never from
  code or committed config; sensitive data encrypted in transit and where the
  ADR requires it at rest; no home-made crypto.
- **A05 Injection** — no query, command or template built from input; ORM
  raw queries and dynamic sorting or filtering checked.
- **A06 Insecure Design** — the threats the design implies (for example a
  missing ownership rule on the board) are handled; report design gaps
  against the ADR or the board, not just code.
- **A07 Authentication Failures** — covered by API2; also session and token
  lifetime and revocation where the ADR defines them.
- **A08 Software or Data Integrity Failures** — messages and events checked
  for integrity where the ADR requires it; no unsafe deserialization;
  CI and build steps not open to tampering.
- **A09 Security Logging & Alerting Failures** — security events
  (authentication failures, authorization denials, rejected input) are
  logged with a correlation ID; no personal data or secrets in logs; an
  alert path exists where the ADR names one.
- **A10 Mishandling of Exceptional Conditions** — errors fail closed (deny
  on doubt); exceptions do not leak internals; timeouts and partial failures
  leave no half-authorized state.

## Severity

- **Critical** — exploitable now on the test instance with no special
  access, leading to data of another actor or tenant, privilege escalation,
  or remote code execution.
- **High** — exploitable with a valid account or a likely precondition; or a
  missing control the ADR requires on a protected operation.
- **Medium** — a weakness that needs an unlikely precondition, or limits
  that are missing but not yet exploitable at the current scale.
- **Low** — hardening and hygiene.

## Finding format

```
SEC-03 · high · API1 Broken Object Level Authorization
Location:  GET /loans/{loanId}; src/lending/application/GetLoan.ts:22
Violates:  ADR-012 (resource check in the application service), spec scope loans:read
Evidence:  as member-A (token in package), GET /loans/LN-0042 owned by
           member-B → 200 with the full loan. Expected 404.
Abuse-case test:
  Given loan LN-0042 owned by member-B
  When member-A requests GET /loans/LN-0042
  Then the response is 404 Problem Details and no loan data is returned
  -- B1-S05 OWASP-API1
```

No fix, no patch, no code. Evidence must let someone else reproduce it.

## Report format

```
# Security report — <scope>, reviewer <model>

## Package and target
What was received; the test target; tools and versions; limits applied.

## Category verdicts
API1 … API10, A01 … A10 · checked — no finding / finding SEC-nn / not applicable (reason)

## Findings
SEC-01 … in the finding format, critical first.

## Tool triage
Per tool: findings confirmed (→ SEC-nn) and false positives (reason).

## Earlier risks and deferrals
Each accepted risk and deferred finding: still valid / changed / now resolved.

## Not tested
What could not be tested, and why.

## Counts
critical <n> · high <n> · medium <n> · low <n> · categories with verdict <n>/20
```

## Moderator mode (standalone)

1. **Agree the target and limits** with the user: which running instance
   (test only), test identities per actor and tenant, which tools, and limits
   on request volume. Do not continue without an explicit test target.
2. **Assemble the package** and show its contents.
3. **Start a fresh reviewer agent** with the `Agent` tool, told to load this
   skill in reviewer mode, given the package. Use a different model from the
   author's when one is available.
4. **Decide with the user**, finding by finding:
   - **Fix** — the owning team writes the abuse-case test red first, then
     the fix. A gap in the spec's security scheme goes to the spec; a missing
     decision becomes a new ADR.
   - **Accept the risk** — only with an ADR naming the risk, the reason and
     an owner. A critical finding is accepted only when the user says so
     explicitly.
   - **Defer** — an open question with the date or increment that must
     close it.
5. **Re-test.** After fixes, a reviewer re-tests the changed operations — a
   **new** reviewer if a critical or high finding was fixed.
6. **Save** the report as `reviews/<scope>-security.md`.

Without the `Agent` tool, write the package and a prompt (this skill's
reviewer sections) to `reviews/<scope>-security-package/` for a fresh
session, and continue at step 4 with the pasted report.

## Inside the implementation blueprint

`blueprint-service-implementation` step 14 uses this skill in reviewer
mode. The blueprint is the moderator: it builds the package from steps 4, 5
and 12, starts the reviewer, logs decisions, sends fixes back to the owning
step as abuse-case tests, and runs the re-test. The blueprint's step-14
rules (done criteria, ledger, report path) take precedence where they are
more specific.