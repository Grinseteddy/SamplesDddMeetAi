# Inspection package — Cook Assistance, increment 0 (walking skeleton)

Root: `Chapter16/BlueprintImplementation/` in the repository `SamplesDddMeetAi`.

## Increment and slices
Increment 0 — walking skeleton. Two slices:
- **B2-S01** Request help preparation step — a Cook asks the community and/or Grandma Avatar about one step of a
  recipe (HTTP `POST /help-requests`, aggregate Help request, event HelpRequested published on `help.requested`).
- **B1-S04** Provide Help from the Grandma Avatar AI message (Preparation step variant) — a message on the Grandma
  Avatar exchange becomes a Help (aggregate Help, event HelpProvided published on `help.provided`).
Both feed read models behind the GET operations getHelpRequestById, listHelpRequests, getHelpById,
listHelpsForHelpRequest.

## In scope (the whole code of increment 0)
- `src/cook-assistance/**` (hand-written; `src/generated/**` is generated from the specs by `tools/codegen` and is
  not hand-edited — inspect the generator setup, not the output)
- `features/preparation-step.feature`, `cucumber.mjs`
- `tools/codegen`, `tools/contract`, `tools/load`, `tools/intent-coverage`
- build and gates: `package.json`, `tsconfig.json`, `eslint.config.js`, `.dependency-cruiser.cjs`, `vitest.config.ts`,
  `vitest.load.config.ts`, `lint-staged.config.js`, `commitlint.config.js`, repository root
  `.github/workflows/cook-assistance.yml` and `.husky/`

## Approved artifacts the code implements
- Design brief `docs/cook-assistance/03-design-brief.md`; boards and slice tables `01-boards-and-slice-tables.md`;
  slice plan `02-slice-plan.md`
- Invariants sheet `docs/cook-assistance/04-invariants-sheet.md` (rules INV-xx, Given/When/Then); Example Mapping
  `docs/cook-assistance/example-mapping-preparation-step.md` (rules R1–R10 and their examples)
- Specs `contracts/openapi/cook-assistance.openapi.yaml`, `contracts/asyncapi/cook-assistance.asyncapi.yaml`
  (2.0.0), upstream `contracts/upstream/recipe-catalog.openapi.yaml`, released baseline `contracts/released/2.0.0`
- ADRs `docs/cook-assistance/adr/ADR-CA-001` … `ADR-CA-019` (including their amendments)
- Principles in scope: `reviews/increment-0-package/principles.md` (AP0001 … AP0008)
- Quality scenarios `docs/cook-assistance/06-quality-scenarios.md` (QS-01 … QS-22, with how each is verified) and
  their step 12 results `reviews/increment-0-package/quality-results.md`
- Intent-coverage report `reports/intent-coverage.md` (regenerate with `npm run intent-coverage` after `npm test` and
  `npm run test:acceptance` if it is missing)

## Planned — not missing
Built later, not defects of increment 0: everything listed in `tools/intent-coverage/planned.json` (operations
cancelHelpRequest, requestChefReview, provideHelp, acceptHelp, rejectHelp; their channels; INV-11, INV-12;
Example Mapping R4, R6, R7 ex.2–3, R9, R10; QS-05, QS-13, QS-17, QS-18, QS-20, QS-21, QS-22) and the three
`it.todo` tests (two repository tests "a save that fails with ConcurrencyConflict leaves neither changed state nor
outbox rows" and the INV-11 refusal in the B1-S04 policy) — increment 1 unless noted. Security tools (npm audit,
OSV-Scanner, Semgrep, ZAP) are wired in the security review (step 14), not here. QS-06 is verified in production only.

## IDs to cite
Slices `B2-S01`, `B1-S04`; rules `INV-07`…`INV-20` as anchored in the sheet; Example Mapping `R1 ex.1` …;
spec elements by operationId or AsyncAPI operation (`requestHelp`, `receiveGrandmaAvatarHelpProvided`, …) and status
(`POST /help-requests 422`); `ADR-CA-0nn`; `AP000n`; `QS-nn`; or "checklist item".

## Checklist
1. **Traceability and invented behavior** — every guard, state transition, command method and event traces to a
   board element, an invariant or a spec element (trace comments `// slice: …`, test traces after `--`); nothing the
   artifacts do not ask for.
2. **Domain rules** — each invariant in scope is enforced where the sheet anchors it (aggregate, application
   service, or policy for cross-aggregate rules), with the rejection the sheet names; states and transitions match the
   brief; events and payloads match the AsyncAPI messages; quantitative/cardinality rules have property tests.
3. **Architecture** — hexagonal layout (ADR-CA-002): no framework, HTTP, database or broker type in domain or
   application; adapters depend inward; one transaction for state and outbox; optimistic locking; idempotent consumer;
   ordered relay with retry and dead letter; error map to Problem Details (ADR-CA-006); authentication and
   authorization placement (ADR-CA-008, ADR-CA-009); read models and paging (ADR-CA-012).
4. **Tests** — every test can fail for the reason its name gives (no assertion that is always true, no test that
   passes without the behavior); fakes honor the same contract as the real adapters; acceptance scenarios match the
   Example Mapping examples.
5. **Language and principles** — names in the ubiquitous language of the brief, the invariants sheet and the specs;
   each principle AP0001–AP0008 honored, or overridden by an ADR that says so.
6. **Quality scenarios** — for each QS verified by test, does the test measure what the scenario's response measure
   says, in the stated environment, and are indicative results labeled as such?
7. **Obvious security defects** — injection, missing authorization, secrets in code or config, personal data in logs
   (a full OWASP review follows in step 14).

## Defect format (one entry per defect; find defects, do not propose fixes or rewrite code)
- **ID** D-<n>
- **Location** file:line (or file and symbol)
- **Violates** the artifact element (`INV-17`, `POST /help-requests 422`, `ADR-CA-004`, `AP0007`, `QS-12`, or
  "checklist item n")
- **Severity** *major* — wrong or missing behavior, broken contract, violated invariant, ADR or principle, a test
  that cannot fail · *minor* — naming, clarity, structure
- **Evidence** what the code does vs. what the artifact says (quote both briefly)
