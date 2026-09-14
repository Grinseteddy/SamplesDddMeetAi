# Ingest — Principles document and full-form ADRs

Companion to `domain-knowledge-graph/references/ingest-small-adr.md`, which
covers the one-table decision log. This file covers the two artifact shapes
that reference does not: a principles document, and one-file-per-decision
ADRs. Vocabulary is `assets/dkg-adr.ttl`, loaded alongside the base `dkg.ttl`.

## Principles document → `dkg:PrinciplesDocument`

One artifact for the file, one `dkg:Principle` per entry.

| In the document | Emit |
|---|---|
| The file | `:Art_Principles_<Project> a dkg:PrinciplesDocument , dkg:Artifact` — type it as `dkg:Artifact` directly as well, so `domain-knowledge-graph`'s `check_graph.py` (which loads only the base vocabulary) recognises it; `rdfs:comment` gives entry count and any defects |
| One entry | `:Prin_<Id>_<ShortLabel> a dkg:Principle` — the document's own ID in the IRI and again verbatim in `dkg:principleId` |
| Title | `skos:prefLabel` |
| Statement | `dkg:statement`, verbatim |
| Rational / Rationale | `dkg:rationale`, verbatim |
| Implications / Consequences | `dkg:implication`, verbatim |
| Any scope words in the statement ("between Bounded Contexts", "for UIs") | `dkg:scope` — quote the phrase; do not paraphrase a scope into existence |
| Status | `dkg:status`, verbatim (the base property, reused) |
| Last amended | `dkg:amendedAt`, `xsd:date` |
| Your testability call | `dkg:testable true/false` — **required**; the checker errors without it |
| Nouns the principle governs that already exist in the graph (`Bounded Context`, `Grandma Avatar`, `UI`) | `dkg:mentions` → existing node, exact label |

Every Principle: `dkg:source`, `dkg:locator "AP0002"`, `dkg:confidence
dkg:OnArtifact`. Testability is a judgement → the `dkg:testable` triple is the
one place `dkg:Inferred` reasoning enters a Principle node; say so in a comment
if the call was close.

**Two IDs for one entry, or one ID for two entries** (both happen): keep the
document's value in `dkg:principleId` untouched, disambiguate only in the IRI
(`:Prin_AP0005a_…`, `:Prin_AP0005b_…`), and put a `dkg:defect` on the artifact
with the verbatim evidence. The report will surface it; the graph must not hide
it by renumbering.

**Copy-pasted fields** (an Implications line identical to another entry's and
unrelated to this one) are emitted verbatim *and* flagged with `dkg:defect` on
the Principle. Never "fix" the text — the node records what the document says.

**Principle-vs-principle tension.** Two principles that pull opposite ways on
the same kind of decision, as written: `:Prin_A dkg:inTensionWith :Prin_B`,
once, plus a `dkg:Question` sourced to the principles document that a future
Decision can `dkg:answers`. Scope usually dissolves the apparent tension
("async between contexts" vs "sync for UI" are different scopes) — when it
does, emit nothing and say why in the report; the graph records tensions, not
near-misses.

## Full-form ADR → `dkg:ADRDocument` + `dkg:Decision`

One artifact per file, one `dkg:Decision` per file. IRI `:D_<ID>` exactly as
`ingest-small-adr.md` does for log rows, so both shapes sit in one namespace.

| In the ADR | Emit |
|---|---|
| The file | `:Art_ADR_<ID> a dkg:ADRDocument , dkg:Artifact` (direct `dkg:Artifact` type as above) |
| ID line / filename | `dkg:decisionId`, verbatim; `dkg:locator` on the Decision |
| Title | `skos:prefLabel` |
| Status (+ date, author, if on the same line) | `dkg:status`, `dkg:decidedAt`, `dkg:decidedBy` — base properties |
| Decision (the bolded operative sentence) | `dkg:decidedOption`, verbatim |
| Context / Decision Drivers | `dkg:rationale`, trimmed to what carries the argument |
| Options considered — each heading | one `dkg:consideredOption` per option, the heading text |
| Consequences of the **chosen** option only | `dkg:consequence`, one per row/line |
| Every "(see AP000n)" / "contradicts AP000n" — for any option, winning or losing | `dkg:cites` → the Principle. Cites is about the *document*, not the outcome |
| Domain nouns in the text that exist in the graph | `dkg:mentions` |
| `dkg:sourceFormat "full-form"` | always |

`dkg:answers`: full-form ADRs seldom write a Question column, so mint one from
the Context ("which deployment shape for Larder?") sourced to the ADR, then look
for the Question an earlier artifact already raised that this one restates —
`proposedSameAs` between the two Questions, and a second `dkg:answers` straight
to the earlier one, exactly as `ingest-small-adr.md` prescribes. A decision that
answers nothing the graph had raised is a finding, not an omission to paper over.

**The Decision line contradicts the document's own Options section** (the
bolded sentence names option X, everything else argues option Y): emit
`dkg:decidedOption` verbatim as written, add `dkg:defect` on the Decision quoting
both, and do the cross-check on the reading the rest of the document supports —
saying which reading you took in the Assertion comments.

## The cross-check edges

Direct triple always; reify when the *how* matters — which for this skill is
every time.

```turtle
:D_ADR0003 dkg:honors :Prin_AP0002_AsyncBetweenContexts .
:As_ADR0003_honors_AP0002 a dkg:Assertion ;
    rdf:subject :D_ADR0003 ; rdf:predicate dkg:honors ; rdf:object :Prin_AP0002_AsyncBetweenContexts ;
    dkg:citedBy "explicit" ;               # explicit | incidental | by-elimination
    rdfs:comment "Options considered: 'It is supported by principles (see AP0002)'" ;
    dkg:source :Art_ADR_ADR0003 ; dkg:confidence dkg:OnArtifact .

:D_ADR0005 dkg:overrides :Prin_AP0005b_MicroUIs .
:As_ADR0005_overrides_AP0005b a dkg:Assertion ;
    rdf:subject :D_ADR0005 ; rdf:predicate dkg:overrides ; rdf:object :Prin_AP0005b_MicroUIs ;
    dkg:acknowledged false ;               # REQUIRED — the checker errors without it
    rdfs:comment "consequences table: 'All teams depend on AppShell team'; the principle is never named" ;
    dkg:source :Art_ADR_ADR0005 ; dkg:confidence dkg:Implied .
```

Confidence on cross-check assertions: `OnArtifact` when the ADR itself names
the principle; `Implied` when the decision's content plainly follows or
contradicts it without naming it; `Inferred` when it took domain knowledge to
see the connection. Silent and not-applicable pairs get **no triple** — a graph
full of "doesn't conflict" edges is unreadable and the absence is queryable.

## Re-ingest

Match Principles on `dkg:principleId` and Decisions on `dkg:decisionId`, never
on title. A changed Status is a status change, reported. A principle missing
from the new file is reported and not removed. New cross-check assertions are
added; an assertion whose `acknowledged` flipped (the ADR was amended to name
the override) gets `dkg:supersedes` from the new Assertion to the old — that
is the record of the team fixing it.