# ADR & Principles — Larder

**Mode: seed.** The project's existing knowledge graph (`larder.ttl` /
`larderEnahnced4.ttl` from earlier sessions) was not uploaded, so
`larder-adr.ttl` is a fresh graph. It uses the project's base IRI
(`https://w3id.org/dkg/graph/larder#`) and the same `:D_SADR000n`,
`:Ctx_…`, `:Act_…`, `:Con_…` slugs the existing graph uses, so it merges by
concatenation plus a dedup pass on the SADR rows and the twelve
Bounded Context nodes. **Upload the current project graph and this becomes
a grow** — the `dkg:proposedSameAs` links flagged in §7 get wired then.

Ingested: `LarderArchitecturalPrinciples.md`, `ADR0001`–`ADR0005`,
`SadrsLarder.md`. Ingest date 2026-09-15.

Files: `larder-adr.ttl` (graph) · `dkg.ttl`, `dkg-adr.ttl` (vocabularies) ·
`findings.json` (checker output) · `adr-browser.html` (viewer, graph
embedded) · `views/index.md` (register).

Both checkers pass with zero errors: 1,084 triples, 7 artifacts,
9 principles, 12 decisions, 14 questions, 9 reified assertions.

---

## 1. What went in

| Artifact | Type | Nodes | Note |
|---|---|---|---|
| `LarderArchitecturalPrinciples.md` | `dkg:PrinciplesDocument` | 9 Principles | 9 entries under **8 IDs** — §6 |
| `ADR0001` … `ADR0005` | `dkg:ADRDocument` × 5 | 5 Decisions, 7 Questions | all Adopted 2026-09-10, one author |
| `SadrsLarder.md` | `dkg:ADRLog` | 7 Decisions, 7 Questions | 2026-09-07 → 09-11, one author |
| *(mentioned)* | — | 12 Bounded Contexts, 3 Actors, 6 Concepts | 10 contexts from ADR0001's module list, **2 only in ADR0005** |

**Deduplication:** no small-log row describes the same decision as any
full-form ADR. The log settles naming and documentation conventions; the ADRs
settle deployment, storage, integration and UI composition.

**Status vocabulary:** both sources say `Adopted` (plus one `Superseded` in
the log). Consistent — no `Accepted`/`Adopted` mix.

---

## 2. Principles Register

| ID | Title | Statement (trimmed) | Scope | Testable |
|---|---|---|---|---|
| AP0001 | Monolith before Microservices | Always start with a deployment monolith; modules modular per DDD | deployment shape | yes |
| AP0002 | Async before Sync | Between Bounded Contexts, asynchronous communication is preferred | between Bounded Contexts | yes |
| AP0003 | Using DDD | Functional architecture designed by DDD principles | functional architecture | yes — close call; checkable only as "modules follow the Bounded Contexts" |
| AP0004 | Using Residuality | Technical architecture designed by Residuality Theory | technical architecture | yes, as a process rule (were stressors recorded?) |
| **AP0005** (1st) | Automatic Testing | 95% of the system tested automatically | — | yes |
| **AP0005** (2nd) | Micro-UIs | Bounded Contexts publish their own UI as micro-UIs; teams as independent as possible | UIs of Bounded Contexts | yes |
| AP0006 | Sync for UIs | UI communication is done synchronously | UI communication | yes |
| AP0007 | Monitoring | System-critical states identified reliably and fast | — | yes |
| AP0008 | Fine-grained access rights | Domain artifacts under fine-grained access control | domain artifacts | yes |

No principle is unfalsifiable as written; all nine are in the matrix.

**Apparent tension, dissolved by scope:** AP0002 (async) and AP0006 (sync)
read as opposites until the scope words are applied — *between Bounded
Contexts* vs *UI communication*. No `dkg:inTensionWith` was emitted. ADR0003
and ADR0004 each land cleanly in one scope, which confirms the split works
in practice. The one place it could bite is ADR0005's cross-context view
aggregation — see the open question in §7.

---

## 3. Decision Ledger

| ID | Title | Status | Date | Decision (as written) | Supersedes | Source |
|---|---|---|---|---|---|---|
| ADR0001 | Using Monolith | Adopted | 2026-09-10 | "We will implement Larder as Monolith." — read as **modular monolith**, see §6 | — | full-form |
| ADR0002 | Using One Database Instance | Adopted | 2026-09-10 | One database instance; read as schema-per-context with a DB user per schema | — | full-form |
| ADR0003 | Using Asynchronous Communication | Adopted | 2026-09-10 | Asynchronous communication with RabbitMQ (Cooking Assistance ↔ Notification, ↔ Grandma Avatar) | — | full-form |
| ADR0004 | Synchronous Communication per UI | Adopted | 2026-09-10 | Synchronous communication for UIs via REST | — | full-form |
| ADR0005 | Orchestrator UI | Adopted | 2026-09-10 | UI orchestrator / AppShell ingesting the contexts' micro-UIs | — | full-form |
| SADR0001 | Chef needs to be accessed exclusively | Adopted | 2026-09-07 | Chef needs to be accessed exclusively | — | small log |
| SADR0002 | Meal Plan | Superseded | 2026-09-08 | Meal plan is used for Menu and Meal plan | — | small log |
| SADR0003 | Menu | Adopted | 2026-09-09 | Menu is used for Menu and Meal plan | SADR0002 | small log |
| SADR0004 | Help | Adopted | 2026-09-11 | Help response / Help request | — | small log |
| SADR0005 | Visual Glossary with Legend | Adopted | 2026-09-11 | Each Visual Glossary needs a legend | — | small log |
| SADR0006 | Brainstorming with Legend | Adopted | 2026-09-11 | Each Brainstorming result needs a legend | — | small log |
| SADR0007 | Capability Map with Legend | Adopted | 2026-09-11 | The Capability Map needs a legend | — | small log |

Options cells **not split** (kept as one `dkg:consideredOption`): SADR0002,
SADR0003, SADR0004. SADR0005–0007 list `Global legend | local legend` but
the decided option names neither — recorded as written.

---

## 4. Compliance Matrix

Checker `--matrix` output, verbatim. Silent and not-applicable cells omitted.

| Decision | Honors | Overrides | Cites |
|---|---|---|---|
| D_ADR0001 | Prin_AP0001_MonolithBeforeMicroservices (explicit), Prin_AP0003_UsingDDD (incidental) | — | Prin_AP0001_MonolithBeforeMicroservices |
| D_ADR0002 | Prin_AP0001_MonolithBeforeMicroservices (explicit), Prin_AP0003_UsingDDD (incidental) | — | Prin_AP0001_MonolithBeforeMicroservices |
| D_ADR0003 | Prin_AP0003_UsingDDD (incidental), Prin_AP0002_AsyncBetweenContexts (explicit) | — | Prin_AP0002_AsyncBetweenContexts |
| D_ADR0004 | Prin_AP0006_SyncForUIs (explicit), Prin_AP0008_FineGrainedAccessRights (by-elimination) | — | Prin_AP0006_SyncForUIs, Prin_AP0008_FineGrainedAccessRights |
| D_ADR0005 | — | Prin_AP0005b_MicroUIs (UNACKNOWLEDGED) | — |
| D_SADR0001 … D_SADR0007 | — | — | — |

The seven small-log rows are naming and documentation decisions; none
engages any of the nine principles. That is silence, not a finding.

---

## 5. Conflicts, in full

**ADR0005 vs AP0005 Micro-UIs — unacknowledged.**

> Principle rationale: *"The teams developing the Bounded Contexts must be
> as independent as possible. The UIs are part of the Bounded Contexts
> projects."*
> Decision consequences: *"Team independence — ‼️ All teams depend on
> AppShell team."*

The statement's letter survives — contexts still publish micro-UIs and the
orchestrator ingests them — but the principle's stated reason is exactly the
thing the decision gives up, and the ADR records that loss in its own
consequences table without naming the principle or arguing why the trade is
worth it. It is also the only ADR of the five that cites no principle at all,
for or against any option; ADR0001–0004 cite one for every option including
the losers. Read together, ADR0005 looks like the one decision made without
the principles document open.

The Context section does contain a real argument (guided cooking workflows
need strictly sequential state; catastrophe-and-rescue spans four contexts
in one dialog) that would make this a defensible *acknowledged* exception.
The fix is one sentence: "This overrides AP0005's team-independence rationale
because …". Then `dkg:acknowledged` flips to true on re-ingest.

No acknowledged exceptions in this set. No two active decisions answer the
same question differently.

---

## 6. Governance gaps

**Dead letters — three of nine principles touched by no decision.**
AP0004 Residuality, AP0005 Automatic Testing, AP0007 Monitoring. All three
are *process* or *quality* principles, and the five ADRs so far are all
*structure* decisions, so the silence is partly a matter of what has been
decided yet. But AP0004 is the sharpest of the three: it commits every
technical-architecture decision to a stressor analysis, and none of
ADR0001–0005 records one. Either the analyses exist in the "meeting protocol"
every ADR points to and should be linked, or AP0004 is not yet being applied.

**Never cited in writing — two.** AP0003 (DDD) is honored by ADR0001, 0002
and 0003 but only incidentally; nobody writes "per AP0003". AP0005 Micro-UIs
is engaged only by the override above. A principle that shapes decisions
without ever being named is one small step from a dead letter.

**Every "Advice" section says "See meeting protocol".** Five ADRs, five
unresolved pointers. The graph cannot follow them; if the protocols hold the
Residuality stressors or the reasoning behind ADR0005, they are the missing
artifact.

**Source defects — eleven, on three documents.**
- *Principles:* the ID `AP0005` is used twice (Automatic Testing, Micro-UIs);
  AP0007 Monitoring's Implications line is a copy of AP0005 Micro-UIs'.
  Kept verbatim; disambiguated only in the IRIs `Prin_AP0005a_…` / `Prin_AP0005b_…`.
- *ADR0001:* the bolded Decision says **Monolith**, while the Options
  section marks plain Monolith as contradicting AP0001, lists the ten
  modules under Modular Monolith, and ADR0002 refers to "the modular
  monolith". Cross-check done on the modular-monolith reading. This is
  the defect to fix first — as written, the ADR's headline decision
  contradicts AP0001.
- *ADR0002:* decision line ambiguous between options 1 and 3; consequences
  table headers copied from ADR0001; one truncated sentence.
- *ADR0005:* the "Choreography" alternative describes the chosen hybrid,
  not an alternative; typo "UI or Larder".

**Modules that appear only in ADR0005.** *Paywall for Premium Content* and
*Ads Management* are named as sources of gating rules and event subscribers,
but are absent from ADR0001's ten-module list and from every DDD artifact in
this project. Raised as `:Q_ModulesOnlyInADR0005`.

---

## 7. What newly connects

**Decisions answering questions the graph raised (this seed).**
Every decision answers at least one question; all questions for the
full-form ADRs were minted from their Context sections. Two remain open with
no decision touching them:

1. `:Q_OrchestratorAggregationReadsWhat` — ADR0005's orchestrator
   "synthesizes live data" from Recipe Catalog, Meal Preparation and Cooking
   Assistance. If that is live querying of each context, AP0002 is engaged
   and no ADR has argued it; if it reads published read models, it is AP0006
   territory and fine. One line in ADR0005 settles it.
2. `:Q_ModulesOnlyInADR0005` — see §6.

**Supersession:** SADR0003 supersedes SADR0002 (Meal plan → Menu). Recorded
on `:Con_MealPlan` as the prefLabel rename with *Meal plan* kept as altLabel.
SADR0004 is a split, not a rename: *Help* → *Help request* / *Help response*.

**To wire on merge with the project graph** (the `proposedSameAs` edges this
seed cannot draw because the targets are not here):

| This graph's question | Restates |
|---|---|
| `:Q_AssistanceBordersCommunication_ADR0003` | the sync-vs-async border questions from the architecture-question agenda |
| `:Q_HelpRequestAddressedToWhom_SADR0001` | the glossary-brief question on who a Help request is addressed to |
| `:Q_MealPlanCorresponds_SADR0002/3` | the pivotal-event cut's "*Meal plan* is read by nobody" |
| `:Q_HelpSameTerm_SADR0004` | the one-word-two-concepts *Help* finding from the context cut |
| `:Q_OrchestratorAggregationReadsWhat` | possibly the "does a help request carry a snapshot, or do responders look things up?" question from the pivotal-event cut §8 |

**Feeding forward.** `architecture-question-finder` reads the
`dkg:answers` edges here as settled: deployment shape, storage layout, the
two async borders, UI transport and UI composition. It will put the
ADR0005/AP0005 override and the two open questions above at the top of its
agenda.
