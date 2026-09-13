# Architecture Question Agenda — Larder (Community Cooking)

**Mode: graph.** Built by mining `larderEnahnced4.ttl` — a domain knowledge
graph merging 21 artifacts (brainstorm, BMC, Capability Map, Wardley Map,
Impact Map, EventStorming board, Visual Glossary, 5 Domain Stories, 9 Bounded
Context Canvases, and a small-ADR log) — with `mine_signals.py`, then reading
each finding for architectural consequence. 122 raw signals came back across
14 categories; this agenda keeps the ~30 that pass the blast-radius test
(would a different answer move a box on the diagram?) and files the rest in
§5.

## 1. What this agenda is built from

| Artifact | Type | Nodes sourced |
|---|---|---|
| BrainstormingConvergence.jpg | Brainstorm | 51 |
| Business Model Canvas | BusinessModelCanvas | 30 |
| CapabilityMapStep3.jpg | CapabilityMap | 40 |
| WardleyMapEvolution.jpg | WardleyMap | 37 |
| ImpactMappingWithAiStep3.jpg | ImpactMap | 42 |
| EventStorming board (+ BC overlay) | EventStormingBoard | 64 |
| VisualGlossaryEnhanced.jpg | VisualGlossary | 66 |
| Domain Story — Ask Chef | DomainStory | 28 |
| Domain Story — Ask Community | DomainStory | 24 |
| Domain Story — Competition | DomainStory | 34 |
| Domain Story — Grandma (I) | DomainStory | 28 |
| Domain Story — Grandma (II) | DomainStory | 28 |
| BCC — Cook Profile | BoundedContextCanvas | 12 |
| BCC — Consent Management | BoundedContextCanvas | 11 |
| BCC — Grandma Avatar AI | BoundedContextCanvas | 17 |
| BCC — Meal Planning | BoundedContextCanvas | 25 |
| BCC — Meal Preparation | BoundedContextCanvas | 23 |
| BCC — Media | BoundedContextCanvas | 19 |
| BCC — Notification | BoundedContextCanvas | 15 |
| BCC — Recipe Catalog | BoundedContextCanvas | 14 |
| BCC — Sharing | BoundedContextCanvas | 22 |
| small-ADR log (SADR0001–0007) | ADRLog | 18 |

Signal counts from the miner: 7 straddlers · 2 orphaned business objects ·
0 contexts with no owned concept · 1 possible duplicate context · 2 unresolved
message kinds · 6 contradictions · 1 unwrapped external system ·
40 evolution/classification markings with no decision · 0 classification
conflicts (formal) · 23 unconfirmed cardinalities · 0 placeholder numbers ·
30 open questions · 10 single-source high-fan-in concepts · 0 borders without
stated timing.

## 2. Already settled — do not re-open

| Decided | Status | What it settles | Caveat |
|---|---|---|---|
| **SADR0004** — "Help" splits into **Help request** / **Help response** | Adopted, 2026‑09‑11 | Confirms the one-word-two-concepts finding independently reached by the Domain Story, EventStorming, and three Bounded Context Canvases. `Con_HelpRequest_ES` / `Con_HelpResponse_ES` are the two terms going forward. | None — clean close. |
| **SADR0003** (supersedes SADR0002) — **Menu** is the term used for both Menu and Meal plan | Adopted, 2026‑09‑09 | Settles the naming dispute between the board's *Menu* and *Meal plan* read models. | The graph itself flags that this is a **naming** decision only: the ES board still models `Con_Menu_ES` (produced at event 2/6/7) and `Con_MealPlan_ES` (produced at event 10 by reading Menu) as two objects eight events apart. Giving them the same label does not say they are the same aggregate over time — carried forward as **AQ‑005** below, not re-litigated as a naming question. |
| **SADR0001** — "Chef needs to be accessed exclusively" | Adopted, 2026‑09‑07 | Answers the Visual Glossary's own question, "is every Help request addressed to both Grandma Avatar and Community, and never to a Chef?" | The decision's own `decidedOption` and `consideredOption` text are near-identical ("Chef needs to be accessed exclusively" vs "Chef is accessed exclusively"), and no matching `Rule` node exists — the graph notes this is really a routing rule that was never formalized as one. It does **not** carry a `dkg:contradicts`-closing edge to the two raw assertions it bears on (`As_RequestAtChef_Story` vs `As_RequestAtAvatarAndCommunity_VG`; `As_ThanksToChef_Story` vs `As_ThanksToOnlyAvatarOrCommunity_VG`). Treated as **directionally settled** — Chef is a separate channel, not a third member of the avatar/community pool — but the integration consequence of "exclusive" access is still open. Carried forward as **AQ‑009**. |
| SADR0005 / SADR0006 / SADR0007 | Adopted | Documentation conventions (legends on the Visual Glossary, Brainstorm, and Capability Map) | Not architectural — no `dkg:mentions` beyond the artifacts themselves. Excluded from this agenda entirely. |

## 3. The agenda

### 3a. Context boundaries & ownership

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-001 | Where does the **Grandma Avatar** actually live, and what is it? | Three unresolved, compounding disagreements sit on one node: (a) its **type** — Actor in the Domain Stories vs ExternalSystem on the EventStorming board; (b) its **context** — asserted `inContext` both `Grandma Avatar AI` and `Cooking Assistance`, and `Grandma Avatar AI` shares 50% of its members with `Notification` (possible duplicate); (c) it has **no anticorruption layer anywhere in the graph** despite the canvas's own purpose text promising one ("behind an anticorruption layer"). Each answer produces a different service boundary — a first-class bounded context, a responder folded into Cooking Assistance, or a thin wrapper shared with Notification. | Not yet explored as one decision — the graph currently holds all three positions simultaneously | `CONTRA-1`, `CONTRA-5`, `DUPCTX-1`, `EXTSYS-1` · BCC — Grandma Avatar AI · EventStorming board · Domain Stories Grandma I/II | OnArtifact (type + placement), Inferred (ACL gap) |
| AQ-002 | Are **Cooking Assistance** and **Cooking Help** one bounded context or two? | Same commands (Request help / Provide help), same objects (Help request / Help response), overlapping responders including the avatar — but drawn as four separate bubbles across two names on the board. Merging vs keeping separate decides whether this is one service or two, and it is upstream of AQ‑001 (the avatar's home only matters once this context exists as one thing). | `Ctx_CookingHelp proposedSameAs Ctx_CookingAssistance` — not yet promoted to a Decision | EventStorming board, events 7/8/15/16 · `OPENQ-9` | OnArtifact |
| AQ-003 | Is **Consent Management** a real bounded context, and what crosses its border — the consent decision, or the Cook themselves? | `Consent` is asserted `inContext` three separate bounded contexts (Cook Profile, Consent Management, Sharing) with no message-kind resolved on either side of its busiest crossing. Depending on the answer, Consent Management either owns a small, queryable fact ("may this be shown?") or the border needs to carry the whole Cook. | Not yet explored — `Q_WhatCrossesToConsentManagement` and `Q_WhatIsConsentOf` are both open on the canvas itself | `STRAD-10`, `MSGKIND-1`, `MSGKIND-17` · BCC — Consent Management, Cook Profile, Sharing · `OPENQ-17`, `OPENQ-18`, `OPENQ-22`, `OPENQ-28` | Inferred (straddle) / Inferred (message kind) |
| AQ-004 | Who owns **Recipe** and **Ingredient** — is Recipe Catalog a real context with its own model, or a conformist wrapper around an off-board catalogue nobody on this graph creates, edits, or retires? | `Recipe` straddles three contexts (Meal Planning, Meal Preparation, Recipe Catalog) and is referenced 7 times but sourced to exactly one artifact; `Ingredient` straddles two (Meal Planning, Recipe Catalog) with both canvases claiming to write it. No event or command anywhere in the graph creates a Recipe. The answer decides a supplier relationship (conformist/OHS to an external catalogue) versus a context this team actually builds and maintains. | Recipe Catalog's own canvas carries the comment "Evolution — product / commodity (proposed)", i.e. the canvas author already leans toward treating it as bought, not built | `STRAD-41`, `STRAD-23`, `SINGLESRC-10` · `OPENQ-4`, `OPENQ-5`, `OPENQ-25` | OnArtifact |
| AQ-005 | Are `Con_Menu_ES` (produced at "Dinner planned") and `Con_MealPlan_ES` (produced at "Meal plan settled," eight events later, itself reading Menu) the **same aggregate over its lifetime**, or two sequential objects that happen to now share a label? | SADR0003 gave both the label "Menu," which is a naming call, not a lifecycle call. If they are one aggregate, the eight-event gap between them needs an in-place state transition; if they are two, the border between Meal Planning's drafting phase and its settled phase needs its own contract. This is the direct architectural follow-on the ADR log itself flags as unresolved. | `Con_MealPlan_ES proposedSameAs Con_Menu_ES` — flagged as "a label collision the ADR authors should be shown before it is built on," not an auto-merge | `STRAD-32` · ES board events 2, 6, 7, 10 · SADR0002/0003 commentary | Implied |
| AQ-006 | Is **Pictures** one concept or two (evidence taken mid-catastrophe vs a trophy shot of the finished meal), and does that split follow the Media/Sharing context line or cut across it? | Pictures straddles Media and Sharing, and the two production events (14 — catastrophe evidence, 20 — finished-meal trophy) are each produced and then **never referenced again by anything in the graph** — the orphan finding on both. If the two senses are really different, Media and Sharing each need their own picture model rather than sharing one aggregate that nothing consistently reads. | Not yet explored as a formal split — flagged independently by the pivotal-event cut, the invariants sheet, and the domain-story cut already in this project | `STRAD-39`, `ORPHAN-7`, `ORPHAN-8` · `OPENQ-12`, `OPENQ-13` | OnArtifact |

### 3b. Integration pattern & consistency

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-007 | What is the **call shape** for Help request / Help response crossing into Notification — command, query, or event, and is the caller expected to block? | `Help request` is asserted `inContext` three contexts (Cooking Assistance, Grandma Avatar AI, Notification) and `Help response` five, but no canvas resolves whether Notification's side is a subscribed event feed or a queried read model. Sync vs async here decides whether Notification is a downstream consumer or sits in the critical path of getting a cook an answer. | Not yet explored | `STRAD-20`, `STRAD-22` · BCC — Notification, "Inbound communication, row 1" | Implied |
| AQ-008 | What message kind is **Consent** — a command that changes something, or a query the border asks before acting? | Recorded literally as `"?"` on **both** sides of the crossing: Consent Management's inbound row and Cook Profile's outbound row. A synchronous permission check and an asynchronously-propagated consent-changed event are different architectures for every context that touches Pictures or Sharing. | Not yet explored | `MSGKIND-1`, `MSGKIND-17` · BCC — Consent Management, Cook Profile | Inferred |
| AQ-009 | What does "Chef needs to be accessed **exclusively**" (SADR0001) mean for integration — a separate channel/service with its own routing, or just a UI-level distinction inside the same Help request model? | The Decision resolves the *naming* dispute (Chef is not folded into the avatar/community pool) but not the *shape* of the exclusivity — same aggregate with a responder-type filter, or a genuinely separate request type with its own lifecycle. The two remaining raw contradictions (Help request "to Chef" vs "to Avatar+Community"; Thanks "to Chef" vs "to Avatar/Community only") sit exactly on this seam. | The ADR's own considered options ("Chef is accessed exclusively" / "Chef is accessed besides Grandma Avatar and Community") are nearly identical text — worth re-reading with the room before building either interpretation | `CONTRA-8`, `CONTRA-10` · SADR0001 | OnArtifact |
| AQ-010 | Can **one Help request** be answered by more than one responder type in the same instance (Community *and* Chef *and* the avatar), or does answering it once close it? | `Help request → Grandma Avatar` is modelled with a derived cardinality of exactly `1`, while the Visual Glossary separately asserts the request goes `to Community`. If more than one responder can legitimately answer the same request, the aggregate needs a collection of responses and a resolution-marking step that does not exist anywhere in the graph yet. | Not yet explored | `CARD-25` · `STRAD-20` · Visual Glossary | Inferred |
| AQ-011 | How many **Pictures** can attach to a single Help request or Help response, and where do they live physically? | Derived (not drawn) as `0..10` on both `Help request → Picture` and `Help response → Picture`. A hard cap this low is a real product constraint that also sizes the Media context's storage design — 10 photos per request is a very different service than an unbounded gallery. | Not yet explored | `CARD-19`, `CARD-28` | Inferred |

### 3c. Build vs. buy

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-012 | How much should be invested in **Grandma/Grandpa AI**, and is it even the same priority across the strategy artifacts? | Marked **core** on the Capability Map but **priority 3 — the lowest of all deliverables** on the Impact Map; on the Wardley Map it sits Custom-Built and depends on `Specific AI`, itself Custom-Built and depending on commodity `Gen AI`. A "core, invest here" capability built on top of a commodity foundation but funded last is an internally inconsistent brief — the build effort for AQ‑001 (the avatar's context) cannot be sized until this is resolved. | Not yet explored | `CONTRA-6` · Capability Map "Cooking Support 5" · Impact Map "Deliverables 10" · Wardley rows 1 & 4 | OnArtifact / Inferred |
| AQ-013 | Is **content creation / writing a recipe** a differentiator to invest in, or a commodity capability to buy off the shelf? | Marked **core** on the Capability Map ("Sharing 1") but the matching Wardley component, `Write recipe`, sits on the **Commodity** line. Same capability, opposite strategic signal — decides whether the recipe-authoring flow gets custom UX investment or a bought editor. | Not yet explored | `CONTRA-3` · Capability Map "Sharing 1" · Wardley Map | OnArtifact |
| AQ-014 | Does **Recipe Catalog** get built as a first-class context or bought/wrapped as a conformist supplier? | The canvas itself carries a two-stage, unresolved marking — "product / commodity (proposed)" — never emitted as a clean evolution stage because it names two. This is the direct build-vs-buy twin of AQ‑004: the ownership question decides *whether* it's a context, this one decides *how much* to invest in it if so. | Recipe Catalog canvas comment names both candidate stages without choosing | BCC — Recipe Catalog, `rdfs:comment` on `Ctx_RecipeCatalog` | Inferred |
| AQ-015 | Which of the 30+ other core/differentiating capability markings still need an investment call before they can be sized? | Community Engagement, Famous chefs partner, Proposals based on larder, Meal Chooser, Cookbook publishing, and ~30 further Wardley components each carry an evolution or core/supporting/generic mark with no matching Decision. Individually most are low blast-radius (a component can be re-marked without moving a context boundary), but as a block they represent the bulk of the strategy work that has not yet become an architecture commitment. Not itemized here to avoid crowding the agenda — see the full list in the mined signals (`BUILDBUY-1..40` minus the four already broken out above). | — | `evolution_without_decision` (36 remaining after AQ‑012/013/014) | Mixed |

### 3d. Numbers nobody supplied

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|----|----------|---------------------------|-----------------------|----------|------------|
| AQ-016 | Can **Thanks** legitimately go to both a human responder *and* the avatar for the same help exchange, or is it one or the other? | Derived cardinalities give `Thanks → Community` and `Thanks → Grandma Avatar` each `0..1` independently, with nothing on the graph stating whether both can be non-empty on the same Thanks. This decides whether Thanks is a single-recipient object or needs a small collection, and it is entangled with AQ‑009's Chef-exclusivity question. | Not yet explored | `CARD-55`, `CARD-57`, `CARD-53` | Inferred |
| AQ-017 | What is the real cardinality of **Recipe → Step** and **Step → Ingredient**? | `Recipe contains Step` is derived as `1..*` and `Step → Ingredient` as an unresolved `?`. Whether a step can reference zero, one, or several ingredients decides whether Ingredient is a value object on the step or needs its own reference collection — a real schema decision for whichever context ends up owning Recipe under AQ‑004. | Not yet explored | `CARD-45`, `CARD-46` | Inferred |

## 4. Severity ranking

The five to put in front of the room first, ranked by how much else in this
agenda sits downstream of the answer:

1. **AQ-001 — Where does the Grandma Avatar live, and what is it?** Touches
   two of the six contradictions, the one unwrapped external system, and the
   one possible duplicate context. Nothing about the avatar's build cost
   (AQ‑012) or its integration pattern can be sized until this is settled.
2. **AQ-002 — Cooking Assistance vs Cooking Help.** Structurally upstream of
   AQ‑001: the avatar's home only matters once this context exists as a
   single thing rather than four bubbles under two names.
3. **AQ-004 — Recipe / Ingredient ownership.** The highest-fan-in straddle in
   the graph (Recipe touches every cooking-adjacent context) and the
   precondition for AQ‑014's build-vs-buy call.
4. **AQ-003 — Consent Management's real border.** Three-way straddle plus two
   unresolved message kinds on the same concept — the densest single cluster
   of unresolved signal in the graph outside the avatar.
5. **AQ-012 — Grandma/Grandpa AI investment level.** The strategic twin of
   AQ‑001: an internally contradictory brief (core, lowest-priority,
   custom-built-on-commodity) that determines how much engineering the
   answer to AQ‑001 is worth funding.

## 5. Not on this agenda

Real findings that failed the blast-radius test — visible here so they don't
compete for the room's time above:

- **Documentation/legend conventions** (SADR0005/0006/0007) — settled, and
  never touched a domain noun in the first place.
- **`OPENQ-3`** (Competition story sentence numbering — parallel or a slip?),
  **`OPENQ-6`** (sparkle/robot icon meaning), **`OPENQ-14`** (event‑10 sticky
  partly covered by the Menu read model), **`OPENQ-15`** ("Prepare meal" as
  one command wearing six events), **`OPENQ-21`** ("the bubble has no
  ending") — real reading/transcription doubts about the source boards, but
  answering them one way or another relabels a sticky rather than moving a
  context boundary. Worth a minute at the top of the next workshop, not an
  architecture session.
- **`OPENQ-23`** (does the avatar watch, or is it told?) — a product/UX
  behaviour question. It would only become architectural if the answer is
  "watches," which would make the avatar a first-class listener rather than
  a responder — flag to revisit if that turns out to be the direction.
- The bulk of **`BUILDBUY-1..40`** not broken out in §3c (Ads management,
  Chat, Cloud, IDM, Media Storage, Member Management, Mobile, Timer, and
  similar commodity-line Wardley components) — real gaps in the ADR log, but
  each is a low-blast-radius "yes, buy it" call rather than a diagram-moving
  decision. Listed in the mined signals file for whoever picks up the
  build-vs-buy backlog next.
- **`SINGLESRC-1/2/4/6/9`** (Catastrophe, Competition, "online," Consent
  Management, Notification each sourced to one artifact) — a review-coverage
  flag, not an architecture question by itself; folded into AQ‑003 and AQ‑004
  where the single-sourced concept is also part of a straddle, otherwise
  noted here as "worth a second look" without its own agenda row.

This agenda is not a generic architecture checklist — no SLAs, hosting,
observability stack, or on‑call rotation appear here, because none of those
come from the modelling artifacts. If the team wants that checklist too, it
should be a clearly separate document so it never gets mistaken for something
the team's own diagrams already argued for.

## 6. Feeding the answers back

Each row's **Question** and **Options on the table** columns are written to
drop straight into a small-ADR log's `Question` and `Options` columns.
Re-ingesting the grown log with `domain-knowledge-graph` will write the
`dkg:answers` edge that closes the loop — the next run of this skill will
find that question in §2 instead of §3. AQ‑005 and AQ‑009 in particular are
half-answered already (SADR0002/0003 and SADR0001 respectively); the fastest
wins on this agenda are finishing those two rather than opening new ones.