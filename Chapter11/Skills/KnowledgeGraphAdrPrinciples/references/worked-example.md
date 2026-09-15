# Worked example — Community Cooking principles and ADRs

Same project as the rest of this toolkit's Community Cooking material, so the
decisions below can be read against the board, the invariants sheet and the
context cuts already in that project. Inputs: one short principles document,
two full-form ADRs, and three rows borrowed from the small-ADR log already
documented in `domain-knowledge-graph/references/ingest-small-adr.md`
(SADR0001, SADR0002/0003, the Menu/Meal-plan naming pair).

---

## 1. What went in

- **`principles.md`** — 6 entries, no IDs in the source; minted `P1`–`P6`.
- **`ADR-011-meal-prep-reads-meal-plan.md`**, **`ADR-012-cook-assistance-sync-call.md`**
  — full-form, Nygard headings (Status / Context / Decision / Consequences).
- **`SadrsLarder.md`** — small-ADR log, 3 rows used here (SADR0001, SADR0002,
  SADR0003).
- **`ADR-013-avatar-badge.md`** — full-form, short.

No `graph.ttl` exists yet for this cross-check; it runs standalone. §7 shows
what would be proposed if one did.

---

## 2. Principles Register

| ID | Statement | Rationale | Scope | Testable? |
|---|---|---|---|---|
| P1 | Cross-context integration is asynchronous by default; a synchronous call is justified only when a human is actively waiting on the answer. | Keeps contexts deployable independently; sync calls couple uptime. | all cross-context calls | **yes** |
| P2 | Any AI-generated content shown to a cook must be labelled as machine-generated. | Trust — a cook should know when advice came from Grandma Avatar rather than a person. | user-facing content only | **yes** |
| P3 | No bounded context reads another context's aggregate directly; borders are crossed only through published read models or events. | Prevents a schema change in one context silently breaking another. | all cross-context reads | **yes** |
| P4 | Be pragmatic about consistency. | — (none given) | — | **no — unfalsifiable.** No decision's content could fail this as written; excluded from §4. A testable rewrite, if the team wants one: name the actual staleness windows they're willing to accept per border, the way `pivotal-event-cut-cooking-board.md` already did for *Meal plan settled* ("hours to days"). |
| P5 | Prefer buying commodity capabilities and building only the core domain. | Focus engineering effort where it's differentiating. | build-vs-buy calls | **yes** |
| P6 | Every cross-context read must return within 200ms. | User-perceived latency budget. | all cross-context reads | **yes** — and see §6, it stands in real tension with P1. |

---

## 3. Decision Ledger

| ID | Title | Status | Date | Decision | Supersedes | Source |
|---|---|---|---|---|---|---|
| SADR0001 | Chef needs to be accessed exclusively | Adopted | (log date) | Chef needs to be accessed exclusively | — | small log |
| SADR0002 | Meal Plan (naming) | Superseded | (log date) | Meal plan is used for Menu and Meal plan | — | small log |
| SADR0003 | Menu (naming) | Adopted | (log date) | Menu is used for Menu and Meal plan | SADR0002 | small log |
| ADR-011 | Meal Preparation reads the Meal Plan via a shared database view | Accepted | 2026-08-02 | Meal Preparation queries Meal Planning's `meal_plan` table through a read-only DB view, to avoid re-fetching the plan at prep start | — | full-form |
| ADR-012 | Cook Assistance calls Recipe Catalogue synchronously | Accepted | 2026-08-10 | When a Help request is opened mid-cook, Cook Assistance makes a synchronous call to Recipe Catalogue for the current recipe, rather than waiting on the async recipe-snapshot event | — | full-form |
| ADR-013 | Avatar responses carry a machine-generated badge | Accepted | 2026-08-14 | Every Help response whose responder is Grandma Avatar is rendered in the Help feed with a "machine-generated" badge, per our labelling principle | — | full-form |

**Deduplication check:** none of these five describe the same underlying
decision — no overlap found between the small-log rows and the full-form
ADRs.

---

## 4. Compliance Matrix

Silent cells omitted, per §4c of the skill.

| Decision | Honors | Conflicts | Not applicable |
|---|---|---|---|
| SADR0001 | — | — | P1, P2, P3, P5, P6 (an access-routing rule; none of these principles are engaged by *who* may call Chef) |
| SADR0003 | — | — | all (naming convention only) |
| ADR-011 | — | **P3** (unacknowledged), **P1** (unacknowledged) | P2, P5 |
| ADR-012 | **P6** (incidental — the sync call satisfies the latency budget, though the ADR doesn't cite P6) | **P1** (acknowledged — see §5) | P2, P3, P5 |
| ADR-013 | **P2** (cited explicitly — "per our labelling principle") | — | P1, P3, P5, P6 |

---

## 5. Conflicts, in full

**ADR-011 vs P3 — unacknowledged.**
> Principle: "No bounded context reads another context's aggregate directly;
> borders are crossed only through published read models or events."
> Decision: "Meal Preparation queries Meal Planning's `meal_plan` table
> through a read-only DB view."
A view is still a direct read of the owning context's storage, not a
published read model — the ADR's Consequences section doesn't mention P3 at
all, so this reads as a decision made without checking the principle rather
than a deliberate exception. This is also exactly the missing-consumer gap
`pivotal-event-cut-cooking-board.md` already flagged from a different angle
(*"Meal plan settled" mints Meal plan, and nothing on the board reads it*) —
ADR-011 is the team's actual answer to that open question, and it answers it
in a way that creates a new coupling nobody signed off on as a principle
trade-off.

**ADR-011 vs P1 — unacknowledged.**
A DB view is a standing synchronous coupling by construction — Meal
Preparation cannot read it if Meal Planning's database is unreachable, and no
human is "actively waiting" in the sense P1 carves out (the read happens once
at prep start, not mid-conversation). Same evidence, same unacknowledged
status.

**ADR-012 vs P1 — acknowledged.**
> Principle: "…a synchronous call is justified only when a human is actively
> waiting on the answer."
> Decision's own Context section: "the cook is mid-conversation with a
> stranger about a burning pan and cannot wait for an async recipe-snapshot
> event to catch up."
This is the carve-out P1 itself names, invoked correctly and in writing. Not
a governance problem — recorded here so it's visible as a *deliberate*
exception rather than indistinguishable from ADR-011's silent one.

---

## 6. Governance gaps

- **P5 (build vs buy) is a dead letter in this set** — no decision here
  honors or conflicts with it. Either no build-vs-buy call has been made yet,
  or one was made without checking the principle. Worth asking directly;
  `core-domain-chart-critic`'s independent finding that **Cook Assistance
  is the likely core domain** in this project makes this principle load-
  bearing the moment anyone proposes buying a generic help-desk product for
  it — check that decision against P5 explicitly when it happens.
- **ADR-011 is made without reference to any principle, and conflicts with
  two.** The more consequential finding than either conflict alone: this
  suggests the principles document isn't being consulted at decision time,
  not just that this one decision got it wrong.
- **P4 is unfalsifiable as written** and excluded from the matrix above — see
  the suggested rewrite in §2.
- **P1 and P6 stand in real tension**, not just in ADR-012's case: "async by
  default" and "every cross-context read returns within 200ms" pull opposite
  directions whenever the honest answer to a read is "the source hasn't
  published yet." ADR-012 resolved this once, for one border, by picking
  synchronous — but that was a decision about *this* border, not a resolution
  of the principle conflict itself. Left standing, the next team hitting the
  same tension has no basis to know whether ADR-012 set a precedent or was a
  one-off exception. **This is the single highest-value thing to put in
  front of the room from this whole cross-check** — pick a resolution (e.g.
  "P6 wins only for calls a human is waiting on; P1 wins for background
  reads") and write it down as its own decision, so ADR-012 stops being the
  only evidence of how the tie gets broken.

---

## 7. The graph these findings come from

Seed mode (no graph existed). Abridged — the checker's own output is what
produced §4–§6 above.

```turtle
:Art_Principles_CC a dkg:PrinciplesDocument , dkg:Artifact ; rdfs:label "principles.md" ;
    rdfs:comment "6 entries, no IDs in source; P1–P6 minted" ; dkg:ingestedAt "2026-09-13"^^xsd:date .

:Prin_P1_AsyncFirst a dkg:Principle ; skos:prefLabel "Async-first cross-context integration" ;
    dkg:principleId "P1" ; dkg:statement "Cross-context integration is asynchronous by default; …" ;
    dkg:scope "all cross-context calls" ; dkg:testable true ;
    dkg:source :Art_Principles_CC ; dkg:locator "P1" ; dkg:confidence dkg:OnArtifact .

:Prin_P4_Pragmatic a dkg:Principle ; skos:prefLabel "Be pragmatic about consistency" ;
    dkg:principleId "P4" ; dkg:testable false ;
    rdfs:comment "no decision content could fail this as written; excluded from the matrix" ;
    dkg:source :Art_Principles_CC ; dkg:locator "P4" ; dkg:confidence dkg:OnArtifact .

:Prin_P1_AsyncFirst dkg:inTensionWith :Prin_P6_Latency200ms .
:Q_AsyncVsLatency a dkg:Question ; skos:prefLabel "When P1 (async by default) and P6 (every cross-context read < 200ms) collide, which wins, and for which calls?" ;
    dkg:source :Art_Principles_CC ; dkg:locator "P1, P6" ; dkg:confidence dkg:Implied .

:Art_ADR_ADR012 a dkg:ADRDocument , dkg:Artifact ; rdfs:label "ADR-012-cook-assistance-sync-call.md" ; dkg:ingestedAt "2026-09-13"^^xsd:date .
:D_ADR012 a dkg:Decision ; skos:prefLabel "Cook Assistance calls Recipe Catalogue synchronously" ;
    dkg:decisionId "ADR-012" ; dkg:status "Accepted" ; dkg:decidedAt "2026-08-10"^^xsd:date ;
    dkg:decidedOption "…synchronous call to Recipe Catalogue for the current recipe…" ;
    dkg:sourceFormat "full-form" ; dkg:answers :Q_HowDoesAssistanceGetTheRecipe ;
    dkg:overrides :Prin_P1_AsyncFirst ; dkg:honors :Prin_P6_Latency200ms ;
    dkg:source :Art_ADR_ADR012 ; dkg:locator "ADR-012" ; dkg:confidence dkg:OnArtifact .
:As_ADR012_overrides_P1 a dkg:Assertion ; rdf:subject :D_ADR012 ; rdf:predicate dkg:overrides ; rdf:object :Prin_P1_AsyncFirst ;
    dkg:acknowledged true ; rdfs:comment "Context: 'the cook is mid-conversation … cannot wait for an async recipe-snapshot event'" ;
    dkg:source :Art_ADR_ADR012 ; dkg:confidence dkg:OnArtifact .
:As_ADR012_honors_P6 a dkg:Assertion ; rdf:subject :D_ADR012 ; rdf:predicate dkg:honors ; rdf:object :Prin_P6_Latency200ms ;
    dkg:citedBy "incidental" ; dkg:source :Art_ADR_ADR012 ; dkg:confidence dkg:Implied .

:D_ADR011 dkg:overrides :Prin_P1_AsyncFirst, :Prin_P3_NoDirectAggregateReads .
:As_ADR011_overrides_P3 a dkg:Assertion ; rdf:subject :D_ADR011 ; rdf:predicate dkg:overrides ; rdf:object :Prin_P3_NoDirectAggregateReads ;
    dkg:acknowledged false ; rdfs:comment "a DB view is a direct read of the owning context's storage; P3 is never mentioned" ;
    dkg:source :Art_ADR_ADR011 ; dkg:confidence dkg:Implied .
```

`check_adr_graph.py` on this graph lists: unacknowledged overrides — ADR011
→ P1, ADR011 → P3; acknowledged exceptions — ADR012 → P1; principle tensions
— P1 ↔ P6; dead letters — P5; untestable — P4; decisions citing no principle
— ADR011. That list *is* §6.

**Into `architecture-question-finder`.** `:Q_AsyncVsLatency` is an open
question no Decision answers — it lands on that skill's agenda directly, and
ADR-012 is only a partial answer to it (one border, not the rule).