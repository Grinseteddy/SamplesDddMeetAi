# Architecture Question Agenda — Larder

**Mode: graph.** Evidence is the attached `larderEnahnced4.ttl` (1,948 lines; 4,650 RDF triples), which ingests 22 artifacts dated 10–12 September 2026, with an addendum dated 13 September. References below use line numbers in that file. **OnArtifact** means the source states the uncertainty or marking; **Implied** means a traceable relationship is interpreted; **Inferred** marks an architectural consequence or hypothesis. A bounded context is a responsibility boundary here, not an automatic one-to-one service prescription.

## 1. What this agenda is built from

| Source group | Type and date | Graph nodes citing source (including reused nodes) | Architectural use |
|---|---|---:|---|
| Brainstorm, impact map, Wardley map, business model canvas, capability map | Five artifacts, 10 Sep | 51, 42, 37, 30, 40 respectively | Goals, dependencies, investment signals and competing classifications. Graph lines 15–440. |
| Grandma, Ask Chef, Ask Community, Competition, Grandma II | Five domain stories, 10 Sep | 28, 28, 24, 34, 28 respectively | Help and sharing flows, actors and contradictions. Lines 441–666. |
| EventStorming board and visual glossary | Two artifacts, 10 Sep | 63 and 66 | Context bubbles, command/event flow, objects and relations. Lines 667–1036. |
| Cook Profile, Consent Management, Grandma Avatar AI, Meal Planning, Meal Preparation, Media, Notification, Recipe Catalog, Sharing | Nine bounded-context canvases, 12 Sep | 12, 10, 16, 25, 23, 17, 13, 14, 20 respectively | Proposed ownership and border contracts. Lines 1037–1743. |
| `SadrsLarder.md` | Small ADR log, seven rows ingested 12 Sep | 18 | Adopted/superseded answers. Lines 1745–1948. |

The counts are distinct subjects with `dkg:source` pointing at each artifact; a subject can count under more than one source. The graph contains 11 bounded-context nodes, 26 message nodes, 44 question nodes and seven decisions. Its nine canvases contain several explicitly proposed or inferred assertions; they are evidence of a proposal, not confirmation of a system already built. No invariants sheet or context-cut analysis was ingested as a source, even where canvas comments refer to them (lines 1180–1195, 1920–1936).

## 2. Already settled — do not re-open as the same question

| ADR | Adopted answer | Boundary of that answer | Evidence |
|---|---|---|---|
| SADR0001 | Chef access is exclusive. | Settles addressing a Chef, but does not settle whether Chef may later receive Thanks; the story and glossary still disagree. | Lines 1760–1777, 958–971. |
| SADR0003, superseding SADR0002 | Use **Menu** as the term for Menu and Meal plan. | Settles the name, not whether the draft read model and settled output are the same object or version. | Lines 1780–1826. |
| SADR0004 | Split asking for help (**Help request**) from giving it (**Help response**). | Settles the vocabulary, not the owning context, avatar copy or response timing. | Lines 1828–1849, 1942–1948. |
| SADR0005–0007 | Add legends to glossary, brainstorm and capability map. | Documentation convention only; the actual meanings of colors/arrows remain to be documented. | Lines 1852–1899. |

## 3. The agenda

### 3a. Context boundaries and ownership

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|---|---|---|---|---|---|
| AQ-001 | Are planning-time Cooking Assistance and stove-time Cooking Help one context, and who owns each Help request/response lifecycle? | A split introduces a handoff and potentially two owners for the same request; a merge leaves one owner with two urgency modes. | One context (one board rendering and proposed merge); two contexts (the other rendering). No adopted ADR answers this. | EventStorming board, four help bubbles and explicit question, lines 692–704; canvases' implied ownership, lines 1918–1937. | OnArtifact for competing bubbles; Implied for ownership. |
| AQ-002 | Who owns Ingredient: Recipe Catalog's recipe definition, Meal Planning's plan substitutions, or separate concepts in each? | Determines authoritative writes, whether a plan keeps references or ingredient snapshots, and who publishes changes. | Both canvases claim the same term; splitting recipe ingredient from planned/substitute ingredient is an interpretation to test, not a recorded decision. | Meal Planning canvas, lines 1300–1309; Recipe Catalog canvas and explicit question, lines 1612–1659. | OnArtifact for conflicting claims; Inferred for possible split. |
| AQ-003 | Is Recipe Catalog an owned context with a recipe creation/edit/retirement lifecycle, or an external/off-board catalogue exposed through its OHS? | Changes ownership of Recipe, Ingredient and Step, and whether the team builds a write model or integrates a supplier. | Canvas itself asks “context or off-board catalogue behind an OHS”; its recipe lifecycle has no command or event. | Recipe Catalog canvas, lines 1587–1650; brainstorm's store/remix ideas, lines 67–70. | OnArtifact for the missing lifecycle and fork. |
| AQ-004 | What aggregate owns cooking progress, and where do Step completed, Meal rescued and Meal prepared finish? | Without a state owner, Meal Preparation has no credible write boundary or event producer for its seven-step execution flow. | A Meal Preparation work object is named on its canvas, but the state/aggregate and ending are undrawn; alternatives have not been explored. | EventStorming command question, lines 704–707; Meal Preparation canvas, lines 1353–1449. | OnArtifact for missing state/ending; Inferred for aggregate boundary. |
| AQ-005 | After SADR0003's Menu rename, are the board's Menu read model and its settled-plan output one versioned object or two sequential objects, and which does Meal Preparation read? | Determines one aggregate/version stream versus a draft-to-settled projection or handoff; affects consumers and ownership of the settled output. | ADR unifies the label; the board still reads Menu to produce a distinct settled object. Identity and read contract remain unexplored. | ADR and explicit collision note, lines 1795–1826; Meal Planning/Preparation queries and open question, lines 1270–1299, 1341–1349, 1370–1399. | OnArtifact for distinct board objects and naming ADR; Inferred for implementation shapes. |
| AQ-006 | Does Consent Management own one consent covering terms and guests in pictures, or separate permissions; which context enforces guest visibility before Media supplies pictures and Sharing publishes them? | Changes consent schema, enforcement boundary and whether Media/Sharing may rely on a current decision or a replicated grant. | Terms, guests or both are explicitly open; current canvas proposes Consent Management as approver, while Media does not consult it and Sharing does. | Consent canvas, lines 1117–1172; Media and Sharing questions/queries, lines 1460–1520, 1671–1739. | OnArtifact for questions; Inferred for proposed context. |
| AQ-007 | Who owns recipient identity and membership for Community, Chef and Cook, and who consumes Cook Profile's Cook record? | Notification, help routing and Thanks need an authoritative addressable recipient; a shared Community object or a recipient projection crosses several contexts. | Cook Profile canvas says nobody reads Cook; Notification says Community and Chef are on no bubble; Community as responder versus audience is unresolved. No ownership alternatives are recorded. | Stories' role question, lines 640–645; Cook Profile and Notification canvases, lines 1063–1113, 1531–1582. | OnArtifact for gaps; Inferred for projection. |
| AQ-008 | Is Grandma Avatar AI its own gateway/context holding a translated Help request, or a responder inside Cooking Assistance; where does the already-proposed anticorruption layer live? | Moves the provider boundary and translation ownership. It also determines whether avatar state is a copy or authoritative work object. | Canvas draws a separate context and ACL; board/stories depict external system/actor within assistance; the purported inside-assistance counterclaim is explicitly a placeholder, not independently ingested. | Board/stories contradiction, lines 1029–1035; avatar canvas, lines 1180–1252; definition, line 1905. | OnArtifact for canvas and board symbols; Inferred for counterclaim. |
| AQ-009 | Does Sharing obtain Help response from Cooking Assistance, or from Meal Preparation's acted-on advice; what event starts Thanks given? | Chooses authoritative source and whether the downstream sharing flow subscribes to a completion event or polls another context. | Sharing canvas queries Meal Preparation and asks why not the help context; it also has no triggering event. Options otherwise unexplored. | Sharing canvas, lines 1671–1739; assistance ownership addendum, lines 1920–1937. | OnArtifact for inconsistent read and missing trigger. |

### 3b. Integration pattern and consistency

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|---|---|---|---|---|---|
| AQ-010 | When a cook asks during live preparation, must an avatar/human Help response arrive before the cooking screen can continue, or may the cook proceed and receive it later? Does planning-time help have the same budget? | Fixes blocking request/response versus event-driven delivery, response-time budget and timeout/escalation at the stove. | Avatar canvas already draws request and response as events; preparation queries Help response; no timing budget or fallback is stated. | Avatar messages, lines 1200–1212; Meal Preparation queries and real-time purpose, lines 1353–1399, 1907; board help bubbles, lines 692–704. | OnArtifact for event/query labels; Inferred for latency fork. |
| AQ-011 | Is Cook Profile → Consent Management an instruction to record consent, a notification of consent given, or a query for current consent; how current must Sharing's consent answer be when publishing? | Determines the first edge's message shape and whether publication requires a synchronous authorization check, cached grant, or event-fed projection. | Both ends mark the first message `?`; Sharing's edge is marked query and conformist, with no staleness window. | Cook Profile and Consent canvases, lines 1070–1083, 1128–1146; Sharing query, lines 1685–1689. | OnArtifact for `?` and query; Inferred for consistency choices. |
| AQ-012 | For recipe/menu/picture reads, which consumers require a fresh answer and which can use snapshots while cooking or sharing? | Determines direct queries versus replicated read models; an outage or changed recipe can otherwise strand an in-progress meal. | Canvases label these borders queries, but no freshness windows are recorded; snapshots are an option to examine, not an artifact decision. | Meal Planning/Preparation queries, lines 1270–1299, 1370–1399; Media queries, lines 1465–1471; Recipe Catalog query, lines 1607–1610. | OnArtifact for borders; Inferred for timing alternatives. |
| AQ-013 | Who receives Help requested/provided notifications, and does a successful Thanks given also produce a notification? | Sets event subscriptions, recipient lookup and whether Notification persists delivery state; its inbound messages exist, but outbound audience and Thanks trigger do not. | Two assistance events are drawn; Community and Chef have no map bubble, and the canvas asks why Thanks given is missing. | Notification canvas, lines 1524–1582; Sharing trigger question, lines 1731–1739. | OnArtifact. |
| AQ-014 | Can a Chef who was exclusively addressed receive Thanks, and is a Grandma Avatar recipient a system identity or human-like Cook? | The unresolved story/glossary conflict affects recipient identity and the routing/Thanks contracts. SADR0001 settles exclusive access, not this downstream relation. | Story thanks Chef; glossary has recipient arrows only to Avatar/Community. Board makes Avatar an external system while stories draw a person. | Story/glossary contradiction, lines 958–971; avatar assertion pair, lines 1029–1035; SADR0001, lines 1760–1777. | OnArtifact for conflicting representations. |

### 3c. Build versus buy

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|---|---|---|---|---|---|
| AQ-015 | What part of Content Creation is actually core: the creator workflow and community behavior, or commodity writing/media components that should be procured? | Defines the bespoke investment boundary and whether media/write infrastructure is supplied by a product. The classifications refer to different levels, so their recorded contradiction is a prompt to decompose, not proof either chart is wrong. | Capability map marks Content Creation core; Wardley map places Write recipe and Upload media in commodity. | Capability/Wardley assertions, lines 1009–1019; component dependencies, lines 237–253. | OnArtifact for markings; Inferred for scope reconciliation. |
| AQ-016 | Is Grandma/Grandpa AI a launch-defining bespoke capability, a later priority-3 deliverable, or a replaceable provider behind the avatar gateway? | Changes where the team invests now and whether the AI-specific model lives in its own context or behind an adapter. “Core” and priority 3 are different dimensions; neither alone decides sequencing. | Capability map marks it core; impact map marks deliverable priority 3; Wardley has custom “Specific AI” built on commodity Gen AI. | Impact map, lines 166–168; Wardley, lines 230–250; assertions, lines 1020–1027. | OnArtifact for markings; Inferred for build/sequencing alternatives. |
| AQ-017 | Should Recipe Catalog and its recipe/ingredient storage be built and operated, or supplied as a product/commodity behind a stable interface? | Settles a central supplier boundary for Meal Planning, Meal Preparation and search. This depends on AQ-002 and AQ-003. | Canvas labels evolution “product / commodity (proposed)” and asks whether the context is off-board; Wardley locates Recipe database at Product, but no procurement decision is recorded. | Wardley, lines 244–254; Recipe Catalog canvas, lines 1587–1650; addendum, lines 1910–1914. | OnArtifact for stage/proposal; Inferred for supplier choice. |

### 3d. Numbers nobody supplied

| ID | Question | Why it moves the diagram | Options on the table | Evidence | Confidence |
|---|---|---|---|---|---|
| AQ-018 | Is the apparent `0..10` picture limit an actual invariant per Help request, Help response and Thanks, and how many versions/derivatives must be retained? | A true limit constrains storage, upload contracts and fan-out; a diagram-derived limit cannot safely be enforced as a business rule. | Three canvas relationships repeat `0..10` with `cardinalityGiven false`; no confirmed limit or retention option is recorded. | Media canvas, lines 1484–1505; Sharing canvas, lines 1700–1720. | Implied for the cardinality; OnArtifact for lack of confirmation. |
| AQ-019 | How many recipients can a Help request/notification target, and how many independent consent grants can cover a Picture? | Decides single-recipient fields versus recipient/grant collections, delivery fan-out and revocation shape. | Consent and notification relationships have `?`; SADR0001 fixes Chef exclusivity but not fan-out to Community/Avatar or notification audiences. | Consent canvas, lines 1093–1103, 1150–1164; Notification canvas, lines 1554–1573; SADR0001, lines 1760–1777. | OnArtifact for unknown `?`; Inferred for storage impact. |

No `<n>`-style placeholder rule number appears in the ingested graph. These two rows are selected from numerous `cardinalityGiven false` relationships because their answers can change storage or fan-out; the remaining inferred counts belong in domain validation, not automatically in this architecture agenda.

## 4. Severity ranking — first five for the decision room

| Rank | ID | Why this precedes the other open items | Decisions unlocked |
|---:|---|---|---|
| 1 | AQ-001 | Help crosses planning, preparation, avatar, notification and sharing; it has competing board boundaries and no closing ADR. | AQ-008–010, AQ-013–014. |
| 2 | AQ-002 | Ingredient is explicitly claimed by two canvases and is central to Recipe Catalog and Meal Planning. | AQ-003, AQ-005, AQ-012, AQ-017. |
| 3 | AQ-004 | Meal Preparation has active events but no agreed state aggregate or ending. | AQ-005, AQ-009–010, AQ-012. |
| 4 | AQ-006 | Consent crosses Cook Profile, Media and Sharing with an unconfirmed scope and enforcement point. | AQ-011, AQ-018–019. |
| 5 | AQ-003 | The recipe supplier's context versus off-board status controls several query contracts and buy/build work. | AQ-012, AQ-017. |

The rank reflects downstream dependencies in this graph, not a claim about commercial priority. The avatar investment conflict in AQ-016 should join the first session if the product intends to ship that capability in the first release.

## 5. Not on this agenda

| Finding in the graph | Why it stays outside this architecture decision log | Evidence |
|---|---|---|
| Blue sticky, glossary icon/color and capability-arrow meanings | SADR0005–0007 adopt a legend convention; the actual visual decoding remains documentation work unless it revises a capability classification. | Lines 97–100, 413–417, 1852–1899. |
| Competition story repeated sentence numbers; covered event-10 sticky spelling | Source transcription/numbering questions without a demonstrated box or border change. | Lines 643–644, 704–707. |
| Impact-map and Wardley fan wiring | Clarify actor/need and deliverable traceability for product strategy; promote a specific branch only when it changes ownership or integration. | Lines 175–178, 257–260. |
| No state stickies or hotspots on the EventStorming photo | A facilitation gap by itself; AQ-004 captures the architectural missing-state consequence. | Lines 704–707. |
| Which blue features, discounts or contests ship first | Scope/prioritization decisions without an evidenced context or integration fork. | Brainstorm, lines 47–95; impact map priorities, lines 157–173. |

This document is not a generic architecture checklist for SLAs, cloud choice, observability or on-call. Those concerns may need separate work, but the graph does not evidence specific answers to them.

## 6. Feeding the answers back

For each AQ row, copy **Question** and **Options on the table** unchanged into a small-ADR row with `Date · ID · Title · Status · Author · Decision · Question · Options`. Record the exact artifact/locator and any measured latency or cardinality alongside the answer. Ingest the ADR and link its `dkg:answers` to this agenda's question so the next graph pass moves the item to “Already settled.” Recheck dependent rows after AQ-001–006: an answer can close several, or turn an inferred alternative into a new explicit border contract.
