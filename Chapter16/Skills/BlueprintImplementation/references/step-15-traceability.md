# Step 15 — Traceability check

Loaded by `blueprint-service-implementation` at the start of step 15. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

1. Build the matrix: board → user story → example → slice → board element →
   invariant → spec element → class/method → test → increment, starting from step 12's intent-coverage report (its traces after
   `--` and scenario tags link tests to rules and operations; the trace
   comments in the code link classes and methods). Mark every row complete /
   partial / missing. Every slice of the plan must appear; a slice with no
   class or no test is *missing*. Every user story must appear as *done*; a
   user story with a failing or missing example is *partial*, one with no
   slice built is *missing*. Add a **user-story summary**: per board, its
   user stories with done / partial / missing, and the Example Mapping each
   came from. Confirm the planned list is empty.
   Add a **quality matrix**: quality scenario → ADRs that serve or trade it
   off → test (step 12) and/or review verdict (step 13) → result (*met*,
   *missed*, *indicative*, *not met*, *open verification*). Every accepted
   scenario must appear; one marked *not verifiable here* stays as an open
   verification with its owner.
2. **Reverse check — documents against code.** The matrix runs forward
   (rule → test) and never notices a document that describes something the
   code does not have. Run the reverse check (*Reverse check* — in the step 12–15 files) over the whole context
   and show its drift table.
3. If a graph exists, offer to ingest the specs and the matrix with
   `domain-knowledge-graph` (grow or revise mode) and report what connected,
   what contradicts, what is orphaned.
4. Compile one closing report: artifacts used (source per step), the
   increments in the order they ran with their user stories and slices, the
   user-story summary, the Example Mapping of each board with its red cards
   (answered, still open), what the walking
   skeleton proved, steps implemented directly vs by skill vs deferred, open questions grouped by
   step, the traceability gaps, the reverse-check result (claims
   checked, drifted, resolved, open), the review summary per increment
   (inspectors and models, defects by severity, rejected and deferred ones,
   increments marked "not independently reviewed"), the security summary
   per increment (findings by OWASP category and severity, fixed, accepted
   by ADR, deferred, increments marked "no security review"), and the
   quality summary (scenarios met, missed, indicative, verified by review,
   open verifications, deferred quality questions), and the delivery summary
   (pull requests per increment with their CI results and merge state,
   Definition of Done items waived). Deliver the code location, the ledger
   and the report.

Done when: the matrix has no *missing* rows the user has not accepted, every
user story is *done* or explicitly accepted as partial or dropped, the
planned list is empty, the reverse check reports no open drift, and every
quality scenario has a result or is an open verification with an owner.

<!-- shared:reverse-check -->
## Reverse check — documents against code

Checks every **structural claim** the run's documents make against the code
as it is now. Five kinds of claim, each read from the code by its source of
truth, never from memory:

| Claim | Where the documents state it | Read from the code |
|---|---|---|
| **Names** — aggregates, entities, value objects, commands, events, policies, read models, packages | design brief, invariants sheet, slice plan, Visual Glossary, ADRs (package layout) | the domain and application packages: class, method, event-type and package names, and the trace comments |
| **Columns** — tables, columns, types, nullability, unique constraints named after rules, outbox and delivery columns | persistence ADR, invariants sheet (uniqueness), step 7 contract, glossary attributes | the schema of the **migrated real database** (information schema), not the migration files alone |
| **Enum values** — statuses, kinds, reasons, error codes | brief, glossary, OpenAPI/AsyncAPI schemas, error-map ADR | the code's enums and union types, and the Problem Details types the error map produces |
| **States** — states, transitions, initial and terminal states, illegal commands per state | brief state machines (per board, merged) | the aggregate's status type and its transition guards, confirmed by the state tests from step 6 |
| **User stories and examples** — every user story of the built increments, its rules and examples, the story ↔ slice map | user-story list and slice plan (step 1), each board's Example Mapping (step 3) | the feature files (one per user story, `@US-nn` tags, one scenario per example), the test traces after `--`, and the trace comments in the code |

**How.** Prefer a mechanical check: a small script in the project (for
example `tools/reverse-check`) that extracts the claims from the documents
and the facts from the code and database, compares them, and runs from the
build. Where a claim cannot be extracted mechanically, check it by reading
and say so. A structural claim that no check can verify is shown as a
finding: make it verifiable, or remove it from the document.

**Rules.**
- **Never change the code to match a document.** The document records an
  intention, the code is the reality. Each drift is resolved one of two ways,
  and the user picks:
  - **Correct the document** — when the code is right (a rename during
    refactoring, a column added for a test-driven reason). The change is a
    revision of the artifact's owning step (user stories and slice plan →
    step 1, brief → step 2, invariants and Example Mappings → step 3, spec →
    step 4, ADR → step 5) and is shown before it is saved. An Example
    Mapping is corrected only with the user's consent, because its examples
    were agreed with domain experts.
  - **Defect** — when the document is right. It goes back to the owning step
    of the code as a new failing test, red first, like any step 12 defect.
- Matching is exact after case folding; a near match (`Lent` vs `Loaned`,
  `due_at` vs `due_date`) is name drift, never silently accepted.
- Spec ↔ generated code is already covered by the generator and the contract
  runs; the reverse check compares spec enums and names with the
  **hand-written** domain and mapping code.
- A claim about a slice or user story still on the planned list is skipped
  and marked *planned*, not *drifted*.
- For user stories, drift means: an example on the map with no scenario, a
  scenario with no example on the map, a scenario whose Given/When/Then
  differs from its green card, a `@US-nn` tag no user story has, or a slice
  serving a user story the slice plan does not link to it.

**Report**, as the drift table:

| # | Kind | Claim | Document location | Code location | Finding | Resolution |
|---|---|---|---|---|---|---|
| 1 | state | `Loan: Overdue → Returned` allowed | brief B1 §5 | `Loan.return()` guard | transition refused in code | defect → step 6 |

Close with the counts: claims checked per kind, claims drifted, resolved,
open.

**When it runs:**
- in full at step 15;
- **after every fix sent back from steps 12–14**: once the defect is fixed in
  its owning step and step 12 is green again, run the reverse check scoped
  to the documents that describe the changed units (their names, tables,
  enums, states, and the user stories and examples they serve), before the increment checkpoint. A fix changes
  behavior, and changed behavior is what makes a correct claim stale.
<!-- /shared:reverse-check -->

## Reading the graphs

<!-- shared:graph-basics -->
Both graphs use the `dkg:` vocabulary (`https://w3id.org/dkg/ns#`); the
decision graph adds `dkg:Principle`, `dkg:honors`, `dkg:overrides` and related
terms. Load both files into one rdflib graph (`pip install rdflib`;
`g.parse(dkg_file); g.parse(adg_file)`) and run SPARQL on the union. Match
labels case-insensitively; keep the graphs' own spellings in everything you
hand on. Never treat `dkg:Inferred` content as fact without saying so; pass
the confidence along.

Prefixes for every query:

```sparql
PREFIX dkg:  <https://w3id.org/dkg/ns#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
```
<!-- /shared:graph-basics -->

<!-- shared:query:inventory -->
**Inventory** — counts per class, overall and per context:

```sparql
SELECT ?ctxLabel ?cls (COUNT(DISTINCT ?n) AS ?count) WHERE {
  ?n a ?cls .
  FILTER(?cls IN (dkg:Aggregate, dkg:AggregateRoot, dkg:Command, dkg:DomainEvent,
                  dkg:PivotalEvent, dkg:Policy, dkg:Rule, dkg:ReadModel,
                  dkg:ExternalSystem, dkg:Hotspot, dkg:Question, dkg:Decision,
                  dkg:Entity, dkg:ValueObject, dkg:Term, dkg:Slice))
  OPTIONAL { ?n dkg:inContext ?ctx . ?ctx skos:prefLabel ?ctxLabel }
} GROUP BY ?ctxLabel ?cls ORDER BY ?ctxLabel ?cls
```
<!-- /shared:query:inventory -->

<!-- shared:query:api-specs-in-the-graph -->
**API specs in the graph** — newer graph versions carry
OpenAPI/AsyncAPI content, older ones do not; the class names vary, so match
on the name:

```sparql
SELECT DISTINCT ?cls (COUNT(DISTINCT ?n) AS ?count) WHERE {
  ?n a ?cls .
  FILTER(REGEX(STR(?cls), "(OpenAPI|AsyncAPI|ApiSpec|Operation|Channel)", "i"))
  FILTER(?cls != dkg:Channel)
} GROUP BY ?cls
```

(`dkg:Channel` is excluded: it is the Business Model Canvas block, not an API
channel.) Then check that a spec node is linked to the chosen context
(`dkg:inContext`, or `dkg:mentions` the context) before calling it provided.
If nothing matches, step 4 asks for the spec files.
<!-- /shared:query:api-specs-in-the-graph -->

If a query returns nothing for the chosen context, say so and fall back to
asking for an upload; never conclude the domain has none.

## Revisiting this step

When this step is revisited ("go back to step N"), re-check — not
regenerate — the completed steps below; offer keep / patch / redo for each
inconsistent one. The full table is in SKILL.md.

| Revised step | Re-check |
|---|---|
| Reverse check finds drift | *correct the document*: the artifact's owning step (1, 2, 3, 4 or 5) as a revision, then its usual re-checks; *defect*: the code's owning step (new failing test there), then 12 again |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
Intent coverage (step 12): gaps <ids or none>, rules without a domain-level test <ids or none>; user stories done <ids>, partial <ids: missing examples>, planned <ids → increment>
Quality results (steps 12–13): per increment — QS-<id>: <met / missed → decision / indicative (environment) / not met / not run (reason)>; open verifications <ids, owner>
Reverse check: <after fix r<k> (scoped) / step 15 (full)> — <mechanical: script path / by reading>, checked names <n>, columns <n>, enums <n>, states <n>, story and example IDs <n>; drifted <n> → <document corrected: step / defect: step>; open <ids or none>; unverifiable claims removed or made verifiable <n>
Review (step 13): per increment — <agent / fresh session / waived>, inspectors <n> (model, focus), defects major <n> minor <n>; accepted <n> → step <n>, rejected <n> (reasons), deferred <n>; re-inspection <y/n, by new agent>; report reviews/increment-<k>-inspection.md
Security review (step 14): per increment — <agent / fresh session / waived>, model <name>; tools <audit, secret scan, static analysis, dynamic scan + versions>; categories with verdict <n>/<n>; findings critical <n> high <n> medium <n> low <n>; fixed <n> (abuse-case tests), accepted by ADR <ids>, deferred <n>; report reviews/increment-<k>-security.md
Increment delivery: per increment — branch <name>, commits <n> (types), DoD <met / waived: items>, PR <link>, CI <green / red → defect>, merged <date by user / open>
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
