---
name: adr-and-principles-ingester
description: >-
  Build a knowledge graph (Turtle/RDF, in the domain-knowledge-graph
  vocabulary) from Architecture Decision Records — full Nygard/MADR
  write-ups or a one-row-per-decision log — and an Architectural Principles
  document (tenets, guardrails, a charter), cross-checking every decision
  against every principle: honors, overrides (acknowledged or not), citations,
  dead-letter principles, tensions, source defects. Seeds a new graph or grows the project's existing one, wiring
  decisions to the open questions it already holds; a checker derives the
  compliance matrix and report from the graph. Use whenever someone has ADRs,
  a decision log, a principles/tenets document or a charter and wants them
  ingested, graphed, cross-checked or reconciled — "put our ADRs and
  principles in the graph", "do our ADRs follow our principles", "check this
  decision against our principles", "which principles have we never used",
  "does ADR-014 contradict ADR-009". Extends domain-knowledge-graph's
  small-ADR ingestion.
---

# ADR & Principles Ingester

Two kinds of document sit next to almost every real system: a pile of ADRs
recording what was decided, and a shorter document of principles recording
what the team meant to be true across *all* decisions. Read separately, both
look fine — every ADR sounds reasonable, every principle sounds sensible.
Read together, gaps show up that neither document reveals on its own: a
principle nobody's decisions have ever actually invoked, a decision that
quietly does the opposite of a principle without anyone noticing, two ADRs
that answer the same question differently without one superseding the other,
or a principle so vaguely worded that no decision could ever be checked
against it either way.

**The job here is not transcription — a coherent list of ADRs and a coherent
list of principles is table stakes. The job is the cross-check, and its
product is a graph:** every principle, every decision and every verdict as
nodes and edges in the same vocabulary the rest of this toolkit's artifacts
already live in, so "which decisions rest on this principle" and "what
happened to that open question" become one query rather than a re-read of
two documents. The written report is rendered from the graph, never beside
it.

---

## 1. What goes in

### Principles document
A numbered or named list of guiding statements — "principles", "tenets",
"guardrails", an "architecture charter". Usually short: a handful to a few
dozen entries, each an imperative or a stated preference ("services
communicate through events, not direct synchronous calls, unless a request
needs an immediate answer a human is waiting on"), sometimes with a rationale
or a "what this makes easy / what this makes hard" note. Confirm this is the
principles document and not a values or culture statement — the skill only
adds value where a principle is specific enough that a decision could be
checked against it (§2 covers what to do when one isn't).

### ADRs — two shapes, both handled

- **Full-form, one file (or section) per decision.** Nygard's original shape
  (Title, Status, Context, Decision, Consequences) or MADR's expanded one
  (adds Decision Drivers, Considered Options, Pros/Cons per option). Read
  whichever headings are actually there; do not force a document into a
  template it doesn't use.
- **Small-ADR log, one table, one row per decision.** `Date · ID · Title ·
  Status · Author · Decision · Question · Options` — no Context or
  Consequences prose, just the question, the options, and which one won.
  **`domain-knowledge-graph`'s `references/ingest-small-adr.md` already
  documents how to transcribe this format and map it into a graph — reuse
  its column semantics and its Options-cell-splitting caution verbatim rather
  than re-deriving them.** This skill's addition on top of that reference is
  entirely §4: the cross-check against principles, which that reference does
  not cover.

Both shapes can be present in the same project (a table for quick calls, full
write-ups for the ones that needed a real argument). Normalize both into one
Decision Ledger (§3) rather than running two separate cross-checks — the room
asking "does this decision follow our principles" doesn't care which format
recorded it.

**If only one of the two document types is provided,** say so plainly and do
what's still possible: ADRs with no principles document still produce a
Decision Ledger and a contradiction/supersession check (§4c); a principles
document with no ADRs still produces a Principles Register with the
testability flag from §2, and is worth doing on its own before any decisions
exist, since a vague principle is cheaper to fix before it's had years to
quietly not apply to anything.

---

## 2. The Principles Register

Transcribe each principle with:

| Field | What it is |
|---|---|
| ID | The document's own numbering; mint one (`P1`, `P2`, …) if the source has none |
| Statement | Verbatim or lightly trimmed |
| Rationale | The "why", if stated — often the only part that says what the principle is actually trying to prevent |
| Scope | Where it applies, if the document says (e.g. "cross-context calls only") — silently assuming a principle is universal when the document scoped it is a common misreading |
| Testability | See below |

**Testability is the field that does the most work.** A principle is
testable if you could point at a specific decision and its content and say,
concretely, whether it complies — not whether the team feels good about it.

- *Testable:* "Services communicate through events, not direct synchronous
  calls, unless a request needs an answer a human is waiting on." A decision
  to add a synchronous call is checkable against this.
- *Not testable as written:* "Be pragmatic about coupling." Nothing a
  decision could contain would fail this, so no decision can be scored
  against it either way. Mark it `unfalsifiable` in the register and exclude
  it from the Compliance Matrix in §5 rather than quietly passing every
  decision against it — a principle that rubber-stamps everything is worse
  than useless, since it looks like governance while doing none.

Flag principle-vs-principle tension here too, before it reaches the
Compliance Matrix: two principles that pull in different directions on the
same kind of decision ("prefer synchronous calls for simplicity" next to
"prefer eventual consistency to avoid coupling deployments") is a governance
gap, not a modelling error, and it means every future decision between them
is really a coin flip dressed as a principle. Report it as its own finding
(§6) rather than letting it surface only indirectly through inconsistent
decisions later.

---

## 3. The Decision Ledger

One row per decision, regardless of which of the two source shapes it came
from. Columns:

`ID · Title · Status · Date · Author · Decision (trimmed to one or two
sentences) · Context/rationale given (if any) · Supersedes · Source format`

- **Status** words are the document's own — `Proposed`, `Accepted`,
  `Superseded`, `Deprecated`, `Rejected` (Nygard/MADR) or whatever the small
  log uses. Record verbatim; don't normalize `Accepted` and `Adopted` into
  one word, since a project mixing both vocabularies is itself worth a line
  in the report.
- **Decision, trimmed.** Full-form ADRs can run to several paragraphs of
  Context and Consequences — the ledger needs the operative sentence, not the
  essay. Keep enough that the cross-check in §4 doesn't need to re-read the
  source, but don't reproduce the whole document; if in doubt, one sentence
  that a reader unfamiliar with the ADR could act on.
- **Context/rationale given, if any.** This is where a decision sometimes
  already names the principle it's following ("per our async-first
  principle, …") — capture that verbatim. It's the strongest possible
  evidence for §4 and should never be re-derived when it's already stated.
- **Supersedes.** A later decision on the same question, explicitly marked
  as replacing an earlier one. This is history, not error — see §4c for the
  distinction that matters.

**Deduplication across the two source shapes.** If a project has both a small
log and full-form files, check whether any row and any file describe the same
decision (same question, overlapping date, same outcome) before ledgering
both as separate rows — a decision recorded twice in two formats is not two
decisions.

---

## 4. The cross-check — the reason this skill exists

For every non-`unfalsifiable` principle and every decision, decide one of
four verdicts. **Do this by reading the decision's actual content against the
principle's actual statement — never by keyword overlap alone**, and never
default to a verdict just because the pair look unrelated at a glance; a
decision about a data store can still honor or conflict with a principle
about coupling if the store choice implies a coupling shape.

### a. Honors
The decision's content follows the principle, whether or not the decision
says so. Strongest form: the decision explicitly cites the principle or its
rationale (capture the citation). Still real, but weaker, when the decision
just happens to comply — note the difference, since a principle only ever
invoked by accident is thinner evidence that it's actually shaping decisions
than one decisions actively reason from.

### b. Conflicts
The decision's content does something the principle's statement rules out.
This is the finding worth surfacing loudest — quote both the decision's
operative sentence and the principle's statement side by side so the
conflict is visible without cross-referencing two documents. Two sub-cases
matter:

- **The decision doesn't mention the principle at all**, and simply
  contradicts it — the more common and more worrying case, since it suggests
  the principle wasn't consulted rather than was consulted and overridden.
- **The decision explicitly overrides the principle** with a stated reason
  ("we're breaking the async-first principle here because…"). This is a
  legitimate exception, not an error — but it's still worth listing, because
  an exception nobody wrote down as an exception is indistinguishable from a
  principle nobody enforces. Report it as an acknowledged exception, distinct
  from an unacknowledged conflict.

### c. Silent — not counted against the decision
The decision neither follows nor contradicts the principle because the
principle simply isn't engaged by what this decision covers (a naming
convention decision has nothing to say about a consistency-window
principle). **This is the default, and it is not a negative finding.** The
single most important discipline in this whole cross-check is resisting the
pull to read "doesn't mention" as "violates" — a compliance matrix that
marks every silent cell as a soft conflict manufactures far more findings
than the documents actually contain, and buries the real ones. Only escalate
a silent cell to Conflicts if the decision's content — not its silence —
actually contradicts the principle.

### d. Not applicable
The principle is out of scope for this kind of decision entirely (a
principle scoped to "cross-context calls only" against a decision about
which database library to use). Distinct from Silent mainly in why nothing
counts: Silent means the topics don't overlap, Not applicable means the
document itself scoped the principle away from this kind of decision.

**On the other axis — decisions against each other, not against
principles:**

- Two decisions answering the same question, one marked `Superseded`: not a
  conflict, it's the log changing its mind. Pair them and show both options
  side by side as history.
- Two decisions answering the same question, **both** still `Accepted` /
  `Adopted` with different outcomes: a real contradiction inside the ADR set
  itself, independent of principles, and it belongs at the top of §6 — an
  architecture that answers the same question two ways depending on which
  ADR you read is a bigger problem than any single principle mismatch.

---

## 5. Output — the graph first, the report from it

**The primary deliverable is a knowledge graph** — a `.ttl` in the same
vocabulary the rest of this toolkit uses, holding every principle, every
decision, and every cross-check verdict as an edge with its evidence. The
markdown report is derived from the graph afterwards, not written beside it,
so the two cannot disagree. Read `references/ingest-adr-and-principles.md`
for the field-by-field mapping before emitting anything; the vocabulary
extension is `assets/dkg-adr.ttl`.

### Step 0 — Seed or grow
Look for an existing graph: a `.ttl` in the project files, the working
directory, or the uploads. Say which mode you are in, in one line.

- **Seed** — none exists. Copy the base `dkg.ttl` (from the
  `domain-knowledge-graph` skill's `assets/`, or wherever the project keeps
  it) and this skill's `assets/dkg-adr.ttl` beside the new graph. Prologue
  per `domain-knowledge-graph/references/ontology.md` §1, with
  `owl:imports` of both vocabularies. Ask for the project's short name if
  it isn't obvious; it sets the base IRI.
- **Grow** — a graph exists. Load it, read its artifact register, and
  **reuse the IRIs it already has** — `dkg:Decision` rows from a small-ADR
  log (`:D_SADR0003`), Bounded Contexts, Actors, Concepts the principles
  mention, and above all the open `dkg:Question` nodes the ADRs may answer.
  Growth is additive; nothing existing is rewritten.
- **Re-ingest** — the register already holds a `dkg:PrinciplesDocument` or
  an `dkg:ADRDocument` with the same ID. Match on `dkg:principleId` /
  `dkg:decisionId`, report status changes and new entries, never delete.

If the project clearly has a graph you weren't given (an earlier session
built one, a `views/index.md` is mentioned, the small-ADR rows already have
`:D_` IRIs somewhere), **ask for it before seeding a second one** — two
graphs for one project is the most expensive mistake available here.

### Step 1 — Emit
Principles first (they are what decisions get checked against), then
decisions, then the cross-check assertions, then any `dkg:inTensionWith`
and `dkg:defect` triples. Every Principle carries `dkg:testable`; every
`dkg:overrides` is reified with `dkg:acknowledged`; every `dkg:honors` is
reified with `dkg:citedBy`. Silent and not-applicable pairs get no triple.

### Step 2 — Check
```
pip install rdflib --break-system-packages     # once
python3 scripts/check_adr_graph.py graph.ttl --matrix --json findings.json
```
Zero errors is the bar. The findings the checker lists — unacknowledged
overrides, dead-letter principles, decisions citing no principle,
untestable principles, active decisions answering one question two ways,
source defects — are the content of the report. If a finding looks wrong,
fix the graph, not the report.

### Step 3 — Report, from the graph
```markdown
# ADR & Principles — <project>

**Mode: seed | grow | re-ingest.** <what went in; which graph it went into>

## 1. What went in                — artifact register, node counts
## 2. Principles Register         — from the Principle nodes; testable flag; defects
## 3. Decision Ledger             — from the Decision nodes, both source formats
## 4. Compliance Matrix           — checker `--matrix` output, verbatim
## 5. Conflicts, in full          — every overrides Assertion, evidence quoted,
                                    acknowledged vs not
## 6. Governance gaps             — dead letters, uncited decisions, tensions,
                                    untestable principles, source defects
## 7. What newly connects         — dkg:answers edges into questions the graph
                                    already held; decisions answering nothing
                                    the graph had raised
```
### Step 4 — Browser
`assets/adr-browser.html` is a single-file viewer, comparable to
`domain-knowledge-graph`'s artifact browser: open it, pick the `.ttl`, and
it renders the overview with the compliance matrix, a page per principle
(statement, rationale, who cites / honors / overrides it, evidence
assertions, tensions), a page per decision (options, consequences, verdicts
with their evidence, questions answered, supersession history), a page per
artifact, the governance-gaps view and the open-questions view. Everything
is derived from the triples — it has no state of its own and its own
Turtle parser, so no network access is needed. Ship a copy with the graph
embedded so it opens ready: paste the `.ttl` into
`<script type="text/turtle" id="sample" data-name="…">` before the main
script. The browser resolves classes from the loaded file only; type
artifacts directly as `dkg:Artifact` (the reference says so) and it needs
no vocabulary file.

Deliver `graph.ttl` (or the grown graph under its own name), `dkg-adr.ttl`
if seeding, `findings.json`, the report, and `adr-browser.html`.

---

## 6. Feeding this forward

**Into a new decision.** Before an ADR is finalized, run the §4 cross-check
against its draft and emit the assertions provisionally (`dkg:status
"Proposed"`). A conflict caught here costs a rewrite; caught after adoption
it costs a reversal.

**Into `architecture-question-finder`.** That skill reads `dkg:Decision` →
`dkg:answers` as "already settled" and puts contradictions at the top of
its agenda. A grown graph from this skill feeds it directly: unacknowledged
overrides and `dkg:inTensionWith` pairs are exactly the blast-radius
questions it looks for.

**Into `domain-knowledge-graph`'s views.** `views.py` renders any graph in
this vocabulary; the traceability lens will show a Question → Decision →
Principle chain once this skill has wired it.

---

## 7. Pitfalls to watch for

- **Turning every silent cell into a soft finding.** Covered in §4c —
  resist it; and in the graph, emit nothing for those pairs.
- **Treating a superseded ADR as a contradiction.** It's the log correcting
  itself. Only decisions both still active on the same question count.
- **Forcing a vague principle into a verdict** rather than `dkg:testable
  false`.
- **Renumbering or correcting a defective source** in the graph. Emit
  verbatim, flag with `dkg:defect`, let the report say what to fix.
- **Seeding a second graph** when the project already has one you weren't
  handed. Ask.
- **Re-deriving the small-log transcription rules** instead of reusing
  `domain-knowledge-graph/references/ingest-small-adr.md`.

---

## 8. Worked example

`references/worked-example.md` runs the flow on a small principles document
and a mixed ADR set from this toolkit's Community Cooking material — the
graph snippets there show one explicit honor, one by-elimination honor, one
acknowledged exception, one unacknowledged override, one untestable
principle and one dead letter. Read it once before a first real run.