# Step 4 — Contracts: OpenAPI + AsyncAPI

Loaded by `blueprint-service-implementation` at the start of step 4. This file
holds everything the step needs; blocks marked `<!-- shared:… -->` are
duplicated in other step files on purpose and must stay identical
(`scripts/check_shared_blocks.py`). SKILL.md keeps the ground rules, the
step checkpoint and the full dependency table.

## Procedure and contract

The context needs an **OpenAPI** spec (synchronous operations) and an
**AsyncAPI** spec (events published and consumed). Handle the two specs
separately but in the same way; a spec the context does not need (for
example no events cross its borders) is marked *not needed* only when the
user confirms it.

**4a. Look in the graph first.** Run *API specs in the graph*. If the graph
holds a spec for this context, say which one and treat it as **provided**
(go to 4c). Nothing is taken from the graph without showing it.

**4b. Ask for what is missing.** For each spec the graph does not hold, ask:
"Do you have an OpenAPI spec / AsyncAPI spec for <context>?" Offer:
- **Upload the spec** (`openapi.yaml`, `asyncapi.yaml`, JSON, or a URL the
  user provides) → go to 4c.
- **Upload an API Product Canvas instead** (picture, export or text) → read
  every field as written, list what it says about consumers, value,
  capabilities or use cases, data, events and quality goals, and show
  anything the canvas leaves open. Then go to 4d and derive the specs from the
  canvas together with steps 2–3. Never fill a field the canvas leaves empty;
  ask.
- **Neither** → go to 4d and propose the specs from steps 2–3 alone.

**4c. A spec was provided: check, lint, propose improvements.**
1. Load `openapi-spec-author` or `asyncapi-spec-author` in **validate /
   lint** mode: validate the spec against its schema and run its Spectral
   ruleset. Report every error and warning with its location.
2. Run that skill's **quality review** (its quality-requirements checklist:
   backward compatibility, additive evolution, capability-based segregation,
   links, one bounded context per API), plus the reconciliation table below.
3. Present the **proposed improvements** as a numbered list, each with the
   finding it fixes, the change, and whether it breaks existing consumers.
   Breaking changes are flagged and never applied without explicit consent.
4. The user accepts, rejects or amends each proposal and may add proposals of
   their own. Apply only what was accepted, re-lint, and show the diff.
5. Repeat 3–4 until the user **approves** the spec.

**4d. No spec was provided: propose one.**
1. Load `openapi-spec-author` and/or `asyncapi-spec-author` and derive the
   spec from the step 2 brief, the step 3 invariants, the step 1 borders and,
   when given, the API Product Canvas. If it is unclear which interactions
   are synchronous and which asynchronous, run `context-map-api-proposer`
   first and show its sync/async classification of each border crossing for
   the user to confirm.
2. Lint the draft and run the quality review before showing it — a proposal
   is never shown with known lint errors.
3. Show the proposal: what each operation, channel and message is for, which
   board element it traces to, and anything you had to assume (listed as open
   questions, not hidden in the YAML).
4. The user **approves** it or **proposes improvements**. Apply each proposal,
   say if it conflicts with a quality requirement or with the board, re-lint,
   and show the diff. Repeat until approved.
5. A generated spec is the user's spec only after approval; until then it is
   a draft.

**4e. Gate.** Show the step checkpoint only when **every needed spec is
approved**: the
OpenAPI spec approved (or confirmed *not needed*) **and** the AsyncAPI spec
approved (or confirmed *not needed*). Record each approval, its date, and the
accepted and rejected proposals in the ledger. If a knowledge graph is in
use, offer to ingest the approved specs with `domain-knowledge-graph`.

**Reconciliation table** (part of 4c and 4d):

  | Slice | User stories | Board element | Expected contract | Found | Status |
  |---|---|---|---|---|---|
  | B1-S03 | US-07 | command | OpenAPI operation or consumed AsyncAPI message | … | ok / missing / name drift |
  | B1-S03 | US-07 | published event | AsyncAPI channel + message | … | … |
  | B1-S05 | US-08 | read model | GET operation | … | … |
  | B2-S01 | US-11 | policy reacting to another context | subscribed AsyncAPI channel | … | … |

- A "missing" or "name drift" row becomes an improvement proposal in 4c or a
  required element in 4d.
- The **Slice** column decides which increment builds each operation,
  channel and message; the **User stories** column says which user story
  needs it. A spec element no slice claims is shown as a finding: assign it
  to a slice or remove it from the spec. A user story whose outcome no
  operation, channel or GET exposes (nothing an actor or another context
  could observe) is a finding too: add the contract element or revisit the
  user story in step 1.
- An example of the board's Example Mapping whose outcome (a status code, a
  rejection, a field value) the spec cannot express is a finding for step 4c/4d.
- The specs cover the whole context and are approved once, here. Later
  increments implement them; they do not reopen them unless a defect or gap
  sends the run back to step 4.
- Done when: every needed spec is linted without errors and approved by the
  user, and the table has no "missing" rows the user has not explicitly
  accepted.

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

<!-- shared:query:contradictions-and-open-questions -->
**Contradictions and open questions**: `?x dkg:contradicts ?y`
and `?q a dkg:Question` in the context — show them before asking the user to
accept the step's input.
<!-- /shared:query:contradictions-and-open-questions -->

If a query returns nothing for the chosen context, say so and fall back to
asking for an upload; never conclude the domain has none.

## Revisiting this step

When this step is revisited ("go back to step N"), re-check — not
regenerate — the completed steps below; offer keep / patch / redo for each
inconsistent one. The full table is in SKILL.md.

| Revised step | Re-check |
|---|---|
| 4 Contracts (spec or API Product Canvas changed) | re-approval of the changed spec, then 6 (event payloads); 9 and 10 regenerate on the next build — re-run their wiring tests and fix the wiring test-first; then 11, 12, 15 |

## Ledger

Update `implementation-log.md` (full format in `references/step-00-start.md`).
Header lines this step writes or updates:

```
OpenAPI: <from graph / uploaded / from API Product Canvas / proposed / not needed>, approved <date>
AsyncAPI: <from graph / uploaded / from API Product Canvas / proposed / not needed>, approved <date>
Spec proposals: <accepted IDs> accepted, <rejected IDs> rejected, <user's own proposals>
```

Step rows:

```
| Step | Increment | Name | Input source | Artifact | Route | Accepted | Done | Approved | Open questions |
|------|-----------|------|--------------|----------|-------|----------|------|----------|----------------|
```

Route values: skill name, `direct (skill not available)`, `generator:
<name>`, `deferred`, `skipped`. A revision `r<k>` in *Done* names what changed
and which step sent it back.
