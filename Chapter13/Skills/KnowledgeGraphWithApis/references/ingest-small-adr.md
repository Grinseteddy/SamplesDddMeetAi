# Ingest — Small ADRs (decision log)

A lightweight architecture-decision log: one table, one row per decision,
typically `Date · ID · Title · Status · Author · Decision · Question · Options`.
Unlike a full Nygard-style ADR there is no context or consequences prose — the
whole record is the question, the options that were on the table, and which one
won. The file usually accumulates across a project rather than arriving from one
session.

The log is the graph's **spine for settled doubt**. Every other artifact raises
questions; this is the one that closes them. Its value is entirely in the
`dkg:answers` edges — a Decision that resolves no Question in the graph is a
decision the graph cannot show the point of, and the checker will list it as an
orphan on purpose.

**One `dkg:ADRLog` artifact per file, one `dkg:Decision` per row.** On a
re-ingest of a grown log, rows already in the graph (matched on ID) are not
re-minted; new rows are added and any row whose `Status` changed gets a fresh
`dkg:status` with the change in the report.

## Transcribe

The table, row by row, every column verbatim. Three columns need care:

- **Options** is free text and inconsistently shaped. Some rows enumerate
  alternatives with commas (`Chef is accessed exclusively, Chef is accessed
  besides Grandma Avatar and Community`); others write one clause with an "or"
  (`Meal plan or Menu as term`) and may contain commas that are *not*
  separators (`… in Meal planning, Meal preparation, Cooking Assistance`).
  **Do not split an Options cell mechanically.** Emit one `dkg:consideredOption`
  per alternative only where the row unambiguously enumerates them; otherwise
  keep the whole cell as one `dkg:consideredOption` and say in the transcription
  that it was not split.
- **Question** is often a near-verbatim copy of an open question raised by an
  earlier artifact — a glossary's `⊞` legend question, a canvas's open question,
  a cut's "what would settle it" list. Note the resemblance in the transcription;
  it decides the `dkg:answers` edge below.
- **Status** words are the log's own. `Adopted`, `Superseded`, `Proposed`,
  `Rejected`, `Deprecated` — record whatever is written. A `Superseded` row
  almost always has a later row with the same Question; note the pair.

## Map

| Column | Class / property |
|---|---|
| The file | `dkg:ADRLog` (subclass of `dkg:Artifact`) |
| One row | `dkg:Decision`, IRI `:D_<ID>` (`:D_SADR0003`); `dkg:locator` = the ID |
| Title | `skos:prefLabel` |
| Date | `dkg:decidedAt`, `xsd:date` |
| Status | `dkg:status`, verbatim |
| Author | `dkg:decidedBy`, verbatim |
| Decision | `dkg:decidedOption`, verbatim |
| Options | `dkg:consideredOption`, one per enumerated alternative (see Transcribe) |
| Question | a `dkg:Question` node sourced to the log, `dkg:confidence dkg:OnArtifact`; the Decision `dkg:answers` it |
| A later row with the same Question as a `Superseded` one | `dkg:supersedes` from the later Decision to the earlier |
| Nouns the decision settles (`Menu`, `Help request`, `Chef`) | `dkg:mentions` → the existing Concept / Actor, by exact label |

`dkg:Decision` is **not** a `dkg:Rule`. Some rows *are* business rules in
disguise (SADR0001 — Chef is accessed exclusively — is a routing rule); others
are naming choices (SADR0003 — *Menu*, not *Meal plan*) or documentation
conventions (SADR0005–0007 — every glossary, brainstorm and capability map gets a
legend). Typing them all as Rule would put "add a legend" next to "you cannot
answer your own question". Where a decided option is an enforceable business
rule and a matching `dkg:Rule` exists, write `dkg:proposedSameAs` between them —
cross-class, so propose, never auto-merge.

## The `dkg:answers` edge

Every Decision gets its own `dkg:Question` node, minted from the row's Question
column and sourced to the log. That is the *minimum*, and it is what keeps the
row from being an orphan.

Then look for the question it is actually closing. If an earlier artifact in the
graph raised a `dkg:Question` that this row's Question restates — same nouns,
same doubt — do **not** merge the two Questions (`merging.md`: Questions never
merge; two artifacts asking the same thing is a signal). Instead write
`dkg:proposedSameAs` from the log's Question to the earlier one, with the
resemblance as the evidence, and add a second `dkg:answers` from the Decision
straight to the earlier Question. The second edge is what makes *"whatever
happened to that question?"* walkable in the traceability lens; the
`proposedSameAs` is what keeps it honest.

A row whose Question resembles nothing already in the graph is a decision made
about something no ingested artifact raised. That is worth a line in the report
— either an artifact is missing from the graph, or the team decided something
nobody had written down as a doubt.

## Renames the log makes

A naming decision — *Menu is used for Menu and Meal plan* — is the team
choosing a `skos:prefLabel`. Do not rewrite IRIs (`ontology.md` §2). Change the
prefLabel on the existing node, move the losing name to `skos:altLabel`, add the
log as a `dkg:source`, and state the rename in the report. If the decision is
later `Superseded` and reversed, do the same again; the two Decisions, joined by
`dkg:supersedes`, are the record of the flip.

`Help response / Help request` for a term that was one word *Help* is not a
rename but a **split** — the log confirming the one-word-two-concepts finding a
story or glossary ingest already flagged. Add the log as a source on both nodes
and on any `dkg:contradicts` or `proposedSameAs` that previously held the two
apart; the decision is what settles it.

## Merge hazards

- **Re-ingest of a grown log.** Match on ID, never on Title — titles get
  edited. A row present in the graph with a different Status is a status change,
  reported; a row missing from the new file is a deletion, reported and **not**
  removed from the graph.
- **Questions across artifacts** — propose, never merge, as above. The
  temptation is strong because the wording is often identical; resist it, and
  let the `proposedSameAs` say so.
- **Decisions against Rules** — cross-class, always propose.
- **Decisions against each other.** Two rows with the same Question and
  different `dkg:decidedOption` are `dkg:supersedes` if one is marked
  `Superseded`, and `dkg:contradicts` if both are `Adopted`. The second case
  is a finding about the log.
- **Author** stays a literal. Do not mint a `dkg:Actor` for the person who
  wrote a decision; actors in this graph are domain roles, not team members.

## Snippet

```turtle
:Art_ADRLog_SadrsLarder a dkg:ADRLog ;
    rdfs:label "SadrsLarder.md — small ADR log" ;
    rdfs:comment "7 rows, SADR0001–SADR0007, one author." ;
    dkg:ingestedAt "2026-09-12"^^xsd:date .

:Q_MealPlanCorresponds_SADR0003 a dkg:Question ;
    skos:prefLabel "Dinner has 1 Menu, Menu has 1..* Course, Course contains 1..* Meal, Meal with 1 Recipe — which of these does the board's 'Meal plan' correspond to?" ;
    dkg:proposedSameAs :Q_MealPlanReadByNobody ;   # raised by the pivotal-event cut
    rdfs:comment "restates the cut's orphaned-artefact question in glossary terms" ;
    dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0003, Question" ;
    dkg:confidence dkg:OnArtifact .

:D_SADR0002 a dkg:Decision ;
    skos:prefLabel "Meal Plan" ;
    dkg:decidedAt "2026-09-08"^^xsd:date ; dkg:status "Superseded" ; dkg:decidedBy "Junker" ;
    dkg:decidedOption "Meal plan is used for Menu and Meal plan" ;
    dkg:consideredOption "Meal plan or Menu as term" ;   # one cell, not split
    dkg:answers :Q_MealPlanCorresponds_SADR0002 ;
    dkg:mentions :Con_MealPlan, :Con_Menu ;
    dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0002" ;
    dkg:confidence dkg:OnArtifact .

:D_SADR0003 a dkg:Decision ;
    skos:prefLabel "Menu" ;
    dkg:decidedAt "2026-09-09"^^xsd:date ; dkg:status "Adopted" ; dkg:decidedBy "Junker" ;
    dkg:decidedOption "Menu is used for Menu and Meal plan" ;
    dkg:consideredOption "Meal plan or Menu as term in Meal planning, Meal preparation, Cooking Assistance" ;
    dkg:answers :Q_MealPlanCorresponds_SADR0003, :Q_MealPlanReadByNobody ;
    dkg:supersedes :D_SADR0002 ;
    dkg:mentions :Con_MealPlan, :Con_Menu ;
    dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0003" ;
    dkg:confidence dkg:OnArtifact .

# the rename the decision makes — prefLabel changes, IRI does not
:Con_MealPlan skos:prefLabel "Menu" ; skos:altLabel "Meal plan" ;
    dkg:source :Art_ADRLog_SadrsLarder .

:D_SADR0001 a dkg:Decision ;
    skos:prefLabel "Chef needs to be accessed exclusively" ;
    dkg:decidedAt "2026-09-07"^^xsd:date ; dkg:status "Adopted" ; dkg:decidedBy "Junker" ;
    dkg:decidedOption "Chef needs to be accessed exclusively" ;
    dkg:consideredOption "Chef is accessed exclusively",
                         "Chef is accessed besides Grandma Avatar and Community" ;
    dkg:answers :Q_HelpRequestAddressedToWhom_SADR0001 ;
    dkg:mentions :Act_Chef, :Act_GrandmaAvatar, :Act_Community ;
    dkg:proposedSameAs :R_INV_HELP_Routing ;   # if such a Rule exists — cross-class, propose
    dkg:source :Art_ADRLog_SadrsLarder ; dkg:locator "SADR0001" ;
    dkg:confidence dkg:OnArtifact .
```

## Report additions

- **Decisions that answer a Question already in the graph**, and — the more
  telling list — **decisions that answer nothing the graph had raised.**
- **Every `dkg:supersedes` pair, both decided options side by side.** A
  reversal is the log's most valuable content.
- **Renames and splits the log settles**, with the nodes whose prefLabel
  changed.
- **Questions still open in the graph that no Decision touches**, once the log
  is in — the agenda for the next log entry.
- **Options cells that were not split**, so the room can split them if it
  wants.
