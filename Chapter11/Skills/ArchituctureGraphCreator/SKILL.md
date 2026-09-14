---
name: adr-and-principles-ingester
description: >-
  Ingest ADRs — full Nygard/MADR write-ups (Status/Context/Decision/
  Consequences) or a one-row-per-decision log — plus an Architectural
  Principles document (tenets, guardrails, a charter), and cross-check every
  decision against every principle: honors, conflicts, principles no
  decision ever invoked, decisions citing none. Produces a Principles
  Register, Decision Ledger, Compliance Matrix, and a conflicts/gaps list.
  Use when someone has ADRs, a decision log, a principles/tenets document,
  or a charter and wants them ingested, summarized, cross-checked, or
  reconciled — "do our ADRs follow our principles", "check this decision
  against our principles before we approve it", "which principles have we
  never used", "summarize our ADR log", "does ADR-014 contradict ADR-009".
  Also use when drafting a new ADR to check against standing principles
  first. Complements domain-knowledge-graph's small-ADR ingestion (table
  format only) by also handling full-form ADRs and the principles layer.
author: Annegret Junker
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
list of principles is table stakes. The job is the cross-check.** Producing
two clean lists and stopping there misses the reason someone asked for this.

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

## 5. Output — the Cross-Check Report

```markdown
# ADR & Principles Cross-Check — <project>

## 1. What went in
<artifact register: principles doc (N entries), ADR sources (full-form count,
 small-log rows count), dates>

## 2. Principles Register
| ID | Statement | Rationale | Scope | Testable? |
(mark `unfalsifiable` entries clearly; they're excluded from §4 below)

## 3. Decision Ledger
| ID | Title | Status | Date | Decision | Supersedes | Source |

## 4. Compliance Matrix
| Decision | Honors (principle IDs, cited vs incidental) | Conflicts (principle IDs, acknowledged vs unacknowledged) | Not applicable |
(Silent cells are omitted from the matrix entirely — see §4c on why listing
 them would bury the real findings)

## 5. Conflicts, in full
<every Conflicts cell from §4, expanded: decision's operative sentence next
 to the principle's statement, and whether the decision acknowledged the
 override>

## 6. Governance gaps
- Principles no decision has ever honored or conflicted with (dead letters —
  either nobody's tested them yet, or they're not actually load-bearing)
- Decisions that cite no principle and honor none incidentally (decisions
  made outside the stated framework entirely)
- Principles marked `unfalsifiable`, with a suggested testable rewrite where
  one is obvious
- Principle-vs-principle tension (§2)
- Decision-vs-decision contradictions not resolved by supersession (§4c)

## 7. Feeding this forward
<see §6 below>
```

---

## 6. Feeding this forward

**Into a new decision.** If someone is about to write an ADR and wants it
checked first, run the same §4 cross-check against the drafted decision text
before it's finalized — this is the highest-leverage moment to use this
skill, since a conflict caught here costs a rewrite and a conflict caught
after acceptance costs a reversal.

**Into `domain-knowledge-graph`.** The base vocabulary (`assets/dkg.ttl`)
already defines `dkg:ADRLog` and `dkg:Decision` for the small-log format
(`ingest-small-adr.md`), but has no class for a principle — this cross-check
is upstream of the graph, not a replacement for it. To carry a Principles
Register into a graph that already exists, propose (never silently assume)
this extension, following the project's own `ontology.md` conventions for
new classes:

```turtle
dkg:Principle a owl:Class ; rdfs:subClassOf dkg:Node ;
    rdfs:comment "A standing architectural guideline, distinct from dkg:Rule (a business invariant) and dkg:Decision (one settled question). Not every Principle is dkg:testable — mark accordingly." .
dkg:testable a owl:DatatypeProperty ; rdfs:domain dkg:Principle ;
    rdfs:comment "true = a decision's content could be checked against this principle either way. false = unfalsifiable as written." .
dkg:honors a owl:ObjectProperty ; rdfs:domain dkg:Decision ; rdfs:range dkg:Principle ;
    rdfs:comment "This decision's content follows this principle. Cite whether the ADR names the principle explicitly or complies incidentally in a comment on the edge." .
dkg:overrides a owl:ObjectProperty ; rdfs:domain dkg:Decision ; rdfs:range dkg:Principle ;
    rdfs:comment "This decision's content contradicts this principle. An acknowledged exception (the ADR states the override) still gets this edge — the distinction from an unacknowledged conflict is a comment, not a different property, so both are still findable with one query." .
```

Mint IRIs `:Prin_<Id>` and reuse `:D_<ID>` for decisions exactly as
`ingest-small-adr.md` already does, so a graph carrying both artifact types
stays queryable as one thing rather than two disconnected halves.

**Into `architecture-question-finder`.** That skill's agenda explicitly
treats an ADR row as a *closed* question via `dkg:answers` — this skill's
ledger is the natural input to "already settled, do not re-open" in that
skill's §2, and its Conflicts list (§4 above) is exactly the kind of
contradiction that skill's severity ranking treats as top priority.

---

## 7. Pitfalls to watch for

- **Turning every silent cell into a soft finding.** Covered in §4c —
  resist it; it's the single easiest way to make this cross-check useless.
- **Treating a superseded ADR as a contradiction.** It's the log correcting
  itself, not two incompatible answers standing at once — only decisions both
  still active in the same status count as a real contradiction.
- **Forcing a vague principle into a verdict rather than flagging it
  unfalsifiable.** A forced verdict on an untestable principle looks like
  rigor and is actually noise.
- **Assuming a full-form ADR set and a small-log table in the same project
  are describing different decisions** without checking for overlap first —
  see the deduplication note in §3.
- **Re-deriving the small-log transcription rules from scratch** instead of
  reusing `domain-knowledge-graph/references/ingest-small-adr.md`'s Options-
  cell and Status-word conventions — that reference already worked those
  out; this skill's job is the layer on top, not a second version of the
  same transcription.

---

## 8. Worked example

`references/worked-example.md` runs the whole flow on a small principles
document and a mixed ADR set (a few full-form write-ups plus the small-ADR
log already used elsewhere in this toolkit's Community Cooking material),
including one acknowledged exception, one unacknowledged conflict, one
unfalsifiable principle, and one dead-letter principle. Read it once before
running this on a real project's documents for the first time.