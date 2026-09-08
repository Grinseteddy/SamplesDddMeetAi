# Prompt 2026-09-08

Check EventStorming board against the Context Map. Use the provided skill.

![](../assets/EventStormingBoardWithBcs.jpg)
![](../assets/ContextMap.jpg)

[Skill](../Skills/ContextBoardEventStormingConsistency/SKILL.md)
[Refrences Checks](../Skills/ContextBoardEventStormingConsistency/references/checks.md)

# Answer

# Conformance Report — Context Map × EventStorming Board

## 0. Scope checked

I compared the supplied **Context Map** with the supplied **EventStorming board with bounded-context bubbles**, using the provided consistency-checking skill and its check catalogue.

I treated the board as evidence of observed producers/consumers and the map as the architectural claim. Per the skill, lack of board evidence means **unsupported**, not automatically **wrong**.  Direction is derived primarily from the writer/reader relationship: the context producing an object is upstream of contexts reading it.

One limitation: I cannot tell from the artifacts alone whether the Context Map was produced after additional design decisions not captured on this board, nor which artifact is newer. Findings involving completely new concepts such as **Consent** and **Notification** should therefore be read as “not supported by this board” rather than “definitely incorrect.”

## 1. Verdict

**Consistent with substantial drift.**

The central context structure is quite good: recurring board bubbles have generally been collapsed correctly, and several important producer/consumer integrations are represented correctly. The main problems are around **ownership and routing of Recipe/Ingredients**, an entirely **missing Catastrophe upstream**, and several map nodes/edges for which this board provides no evidence.

My count:

| Severity                  | Findings |
| ------------------------- | -------: |
| **Blocking**              |        2 |
| **Significant**           |        7 |
| **Minor / open question** |        2 |

The two most important corrections are:

1. **Recipe Catalog must not own Ingredients if the board is authoritative on observation.** The board writes `Ingredients` in Meal Planning.
2. **Recipe must not be routed through Meal Planning/Cooking Assistance as though those contexts produce it.** The board repeatedly reads Recipe but never shows a local writer.

---

## 2. Term ledger

The most useful subset of the ledger is:

| Term              | Written by on board                           | Read by on board                                    | Context Map claim                                                                     | Verdict                                                 |
| ----------------- | --------------------------------------------- | --------------------------------------------------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| **Cook**          | Cook Profile                                  | Mainly appears as actor elsewhere                   | Cook Profile                                                                          | Consistent enough                                       |
| **Menu**          | Meal Planning                                 | Meal Planning; Cooking Assistance                   | Meal Planning                                                                         | **Consistent; MP → CA**                                 |
| **Ingredients**   | **Meal Planning** (`Ingredients substituted`) | Cooking Assistance                                  | **Recipe Catalog** also shown owning Ingredients; MP sends Ingredients                | **OWN conflict**                                        |
| **Recipe**        | **No writer shown**                           | Meal Planning; Meal Preparation; Cooking Assistance | Recipe Catalog owns it; then Recipe is propagated through other contexts              | Upstream plausible, routing unsupported                 |
| **Meal plan**     | Meal Planning                                 | No reader shown                                     | Meal Planning                                                                         | Board orphan, but not harmful unless used in a contract |
| **Help request**  | Cooking Assistance                            | Cooking Assistance help-provision flow              | Cooking Assistance **and Grandma Avatar AI** visually contain it                      | Ownership needs clarification                           |
| **Help response** | Cooking Assistance                            | Meal Planning; Meal Preparation                     | Cooking Assistance, also represented in Grandma Avatar AI; used toward other contexts | CA → MP / CA → Prep supported                           |
| **Pictures**      | Media                                         | Cooking Assistance; Sharing                         | Media                                                                                 | **Consistent**                                          |
| **Catastrophe**   | **No writer shown**                           | Cooking Assistance; Meal Preparation                | No node                                                                               | **Missing off-board upstream**                          |
| **Help provider** | No writer shown                               | Sharing                                             | No clear owner/edge                                                                   | Board gap                                               |
| **Thanks**        | Sharing                                       | No reader shown                                     | Sharing                                                                               | Consistent                                              |
| **Consent**       | **Absent from board**                         | Absent                                              | Cook Profile → Consent Management → Sharing                                           | Unsupported by this board                               |

The checks explicitly require the reverse sweep: any term written in one context and read in another should correspond to an edge, and nouns read by multiple contexts but written nowhere are strong evidence of an off-board upstream.

---

## 3. Node reconciliation

### Correct collapsing of recurring contexts

This is one of the stronger parts of the map.

The board draws several contexts multiple times along the timeline:

* **Meal Planning** — multiple appearances → one map node.
* **Cooking Assistance** — several appearances → one map node.
* **Meal Preparation** — multiple appearances → one map node.
* **Media** — multiple appearances → one map node.

That is exactly the intended treatment: repeated appearances of one context on an EventStorming timeline should collapse to one Context Map node.

### Direct matches

`Cook Profile`, `Meal Planning`, `Cooking Assistance`, `Meal Preparation`, `Media`, and `Sharing` all have clear board bubbles and corresponding map nodes.

### Grandma Avatar AI

**Supported as a node.** The pink `Grandma Avatar` stickies participate in the Cooking Assistance flows. Under the supplied rules, an external system represented by a pink sticky may legitimately be represented as an ordinary bounded context on the map; its existence is not itself a finding.

### Recipe Catalog

There is no `Recipe Catalog` bubble, but there **is** a green `Recipe Catalog` read model, while `Recipe` itself is repeatedly read and has no board writer. So an off-board Recipe supplier is a reasonable architectural inference.

What is not supported is the stronger map claim that this context owns **Ingredients** as well.

### Notification

No corresponding board bubble, event, external participant, object, or obvious policy appears in the supplied EventStorming board.

→ **NODE: unsupported by this board.**

### Consent Management

Likewise, neither a Consent Management bubble nor a `Consent` object/read model appears on the board.

→ **NODE: unsupported by this board.**

---

# 4. Findings

## F1 — `OWN` — Ingredients ownership conflicts

**Severity: Blocking · Resolution: map moves / ask**

The board places the `Ingredients` aggregate with **Meal Planning**, specifically around `Ingredients substituted`. Cooking Assistance later reads Ingredients.

The Context Map, however, places an `Ingredients` owned-state sticky inside **Recipe Catalog** while also showing Ingredients moving from Meal Planning toward Cooking Assistance.

That means the map appears to claim an owner that the board does not show writing the object.

The ownership check defines exactly this shape as a finding: a map assigning a term to a context that never writes it on the board.

**Recommendation:** remove `Ingredients` ownership from Recipe Catalog unless that sticky represents a copy or a distinct Recipe-Catalog concept. If it is a copy, label the translation explicitly.

---

## F2 — `CONTRACT` / `EDGE` — Recipe is being relayed by contexts that never produce it

**Severity: Blocking · Resolution: map moves**

The board repeatedly consumes `Recipe` in:

* Meal Planning
* Meal Preparation
* Cooking Assistance

but nowhere on the board is `Recipe` produced.

The Context Map instead appears to do this:

**Recipe Catalog → Meal Planning → Cooking Assistance → Meal Preparation**

with Recipe included in downstream contracts.

The problem is that Meal Planning and Cooking Assistance are readers of Recipe, not producers of record. Sharing a read model from the same off-board source does **not** establish an edge between the readers. The check explicitly warns that when two contexts merely read the same off-board thing, each should depend on the upstream source rather than on one another for that object.

**Recommendation:** if Recipe Catalog truly owns Recipe, model direct Recipe dependencies from Recipe Catalog to every consuming context:

* Recipe Catalog → Meal Planning
* Recipe Catalog → Cooking Assistance
* Recipe Catalog → Meal Preparation

Do not include `Recipe` in MP→CA or CA→Meal Preparation contracts unless one of those contexts genuinely publishes a transformed Recipe model.

---

## F3 — `EXT` — Catastrophe upstream is missing

**Severity: Significant · Resolution: map moves**

`Catastrophe` is a green read model in at least:

* Meal Preparation (`Catastrophe happened`)
* Cooking Assistance (`Help requested`)

No context on the board writes it.

This is the strongest missing-upstream pattern in the supplied checks: a noun read by two or more contexts and written by none is evidence for an off-board upstream context.

The Context Map has no Catastrophe-related node.

**Recommendation:** add an off-board/external source for `Catastrophe` and connect it independently to Meal Preparation and Cooking Assistance—or return to the board and identify the actual producer.

---

## F4 — `EDGE` / `CONTRACT` — Meal Preparation → Sharing is not supported

**Severity: Significant · Resolution: back to the wall**

The map shows a solid relationship from **Meal Preparation → Sharing**, apparently carrying `Help response`.

But on the board:

* Meal Preparation does not produce `Help response`.
* `Help response` is produced by Cooking Assistance.
* Sharing reads `Help provider` and `Pictures`, not `Help response`.

So neither the source context nor the named payload is supported.

**Recommendation:** establish what Sharing actually needs:

* If it needs the helper/provider identity from Cooking Assistance, draw **Cooking Assistance → Sharing** and add the missing production/read evidence.
* Otherwise remove the Meal Preparation → Sharing edge.

---

## F5 — `NODE` / `EDGE` / `CONTRACT` — Consent Management chain is unsupported

**Severity: Significant · Resolution: ask / back to the wall**

The map introduces:

**Cook Profile → Consent Management → Sharing**

with `Consent` as the crossing object.

The board contains no visible `Consent` sticky and no Consent Management context.

Therefore this whole chain may be a valid later design decision, but the supplied board does not support it.

This is important wording-wise: it is **unsupported**, not contradicted. The supplied rules explicitly distinguish those cases.

**Question that settles it:** “Was consent added as an architectural/domain decision after this EventStorming session?”

If yes, document it as off-board provenance. If no, take it back to the wall.

---

## F6 — `NODE` / `EDGE` — Notification is unsupported

**Severity: Significant · Resolution: ask**

The map contains a `Notification` context with an asynchronous edge from Cooking Assistance, but the board shows no Notification bubble, notification event, external participant, or explicit policy.

It might be a post-session design decision. The board simply cannot verify it.

**Recommendation:** either annotate Notification as derived from another artifact/decision, or add the notification event/policy to the board.

---

## F7 — `OWN` — Grandma Avatar AI duplicates Help ownership

**Severity: Significant, potentially Blocking · Resolution: ask**

Grandma Avatar itself is a perfectly legitimate external node.

The ambiguity is inside the node: the Context Map visually puts `Help request` / `Help response` and corresponding events into **Grandma Avatar AI**, while the board places those aggregates/events inside **Cooking Assistance**.

That can mean either:

1. Grandma Avatar receives/sends **copies/contracts** while Cooking Assistance owns the local lifecycle — fine; or
2. both contexts are being presented as owners — contested ownership.

The checks specifically call out the situation where an external system and local context appear to produce the same object and the map does not state whose model owns it.

**Recommendation:** label these as request/response contracts or translated DTOs, rather than owned domain state, if that is the intent.

---

## F8 — `EDGE` — Help provider has no source

**Severity: Significant · Resolution: back to the wall**

Sharing consults the green read model `Help provider`, but no event visibly produces `Help provider`.

It could be derived from Community Cook / Chef / Grandma Avatar participation, but that derivation is not drawn.

This makes the source of Sharing's provider information unresolved.

**Recommendation:** identify who publishes `Help provider` and add the corresponding producer/crossing.

---

## F9 — `GAP` — Meal plan is produced but appears unconsumed

**Severity: Minor / board gap · Resolution: back to the wall**

`Meal plan` is produced by Meal Planning, but I do not see another event consuming the aggregate/read model.

That is not itself a map defect because the map does not seem to use `Meal plan` as a border contract. It is nevertheless a useful board gap: either it is genuinely terminal state or its downstream consumer is missing.

---

## F10 — `PAT` — ACL on Grandma Avatar is not verifiable from this board

**Severity: Minor · Resolution: ask**

The map explicitly selects an **ACL** for Grandma Avatar AI.

The board establishes that Grandma Avatar is external, but it does not visibly name a model/language mismatch that the ACL translates.

However, the supplied checks say relationship patterns encode organisational/design decisions and should not be rejected merely because the board is silent.

So I would **not call the ACL wrong**.

Just record the unanswered question: *what semantic difference is the ACL translating?*

---

# 5. Undrawn edges / missing upstreams

This reverse sweep is where the largest corrections appear.

| Board evidence                                                       | Expected map relationship                 | Current status                  |
| -------------------------------------------------------------------- | ----------------------------------------- | ------------------------------- |
| Meal Planning writes `Menu`; Cooking Assistance reads it             | **Meal Planning → Cooking Assistance**    | Present                         |
| Meal Planning writes `Ingredients`; Cooking Assistance reads it      | **Meal Planning → Cooking Assistance**    | Present                         |
| Cooking Assistance writes `Help response`; Meal Planning reads it    | **Cooking Assistance → Meal Planning**    | Present                         |
| Cooking Assistance writes `Help response`; Meal Preparation reads it | **Cooking Assistance → Meal Preparation** | Present                         |
| Media writes `Pictures`; Cooking Assistance reads it                 | **Media → Cooking Assistance**            | Present                         |
| Media writes `Pictures`; Sharing reads it                            | **Media → Sharing**                       | Present                         |
| Off-board `Recipe`; MP reads it                                      | Recipe source → Meal Planning             | Present only via Recipe Catalog |
| Off-board `Recipe`; CA reads it                                      | Recipe source → Cooking Assistance        | **Missing**                     |
| Off-board `Recipe`; Meal Preparation reads it                        | Recipe source → Meal Preparation          | **Missing**                     |
| Off-board `Catastrophe`; CA reads it                                 | Catastrophe source → Cooking Assistance   | **Missing**                     |
| Off-board `Catastrophe`; Meal Preparation reads it                   | Catastrophe source → Meal Preparation     | **Missing**                     |
| Unknown producer of `Help provider`; Sharing reads it                | Producer → Sharing                        | **Unresolved**                  |

The skill explicitly says this reverse sweep is mandatory because missing crossings are often more valuable than questionable existing arrows.

---

# 6. Allowed divergence

I deliberately **did not** report the following as defects:

* The repeated board bubbles for Cooking Assistance, Meal Planning, Meal Preparation and Media. The map correctly collapses recurrence.
* Grandma Avatar AI existing as its own node despite being pink/external on the board.
* Synchronous versus asynchronous choices where the board supplies no timing constraint.
* OHS / Conformist / ACL decisions merely because the board doesn't contain organisational negotiation facts.
* Context-map layout and placement.
* The fact that Recipe Catalog has no bubble by itself: there is enough evidence of an off-board Recipe dependency to make such a node plausible; the problem is the ownership/routing details.

These are explicitly classes the supplied checks say to allowlist rather than score as findings.

---

# 7. Patch list

### Edits to the Context Map

1. **Remove `Ingredients` ownership from Recipe Catalog**, or mark it explicitly as a translated/cached copy.
2. Keep `Ingredients` ownership with **Meal Planning** based on this board.
3. Treat **Recipe Catalog/off-board Recipe source** as the direct upstream for every context reading Recipe:

    * → Meal Planning
    * → Cooking Assistance
    * → Meal Preparation
4. Remove `Recipe` from MP→CA and CA→Meal Preparation contracts unless those contexts really republish a transformed Recipe.
5. Add a **Catastrophe** off-board upstream and connect it to:

    * Cooking Assistance
    * Meal Preparation
6. Revisit **Meal Preparation → Sharing**; it is not evidenced by the board.
7. Clarify Grandma Avatar's `Help request` / `Help response` as **contracts/copies versus owned state**.
8. Mark **Notification** and **Consent Management** as off-board/post-session decisions if that is their provenance.
9. If Consent was not a later decision, remove those edges until the board establishes the concept.

### Take back to the EventStorming wall

1. Who produces `Recipe`?
2. Who produces `Catastrophe`?
3. Who produces `Help provider`?
4. Does Sharing actually consume `Help response`, `Help provider`, or both?
5. What consumes the `Meal plan` aggregate?
6. Does Grandma Avatar own a separate Help model, or merely participate in Cooking Assistance?
7. Where does `Consent` enter the domain?
8. What event/policy triggers Notification?

---

## 8. Already consistent

Several important pieces match cleanly and should be preserved:

* **Recurring contexts were collapsed correctly.**
* **Meal Planning → Cooking Assistance** for `Menu` and `Ingredients`.
* **Cooking Assistance → Meal Planning** for `Help response`.
* **Cooking Assistance → Meal Preparation** for `Help response`.
* **Media → Cooking Assistance** for `Pictures`.
* **Media → Sharing** for `Pictures`.
* `Cook Profile`, `Meal Planning`, `Cooking Assistance`, `Meal Preparation`, `Media`, and `Sharing` all correspond cleanly to board contexts.
* Grandma Avatar is correctly recognized as an external participating context rather than being ignored.

The supplied skill calls for findings rather than a binary verdict and asks for concrete map and board patch lists, because the board records observations while the map may legitimately contain later decisions.

**Bottom line:** the map's **core bounded-context decomposition is strong**, but the **information-flow model needs revision**, especially for `Recipe`, `Ingredients`, `Catastrophe`, and the unsupported Consent/Notification additions.
