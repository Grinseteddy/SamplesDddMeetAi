# Prompt 2026-09-08

/context-map-board-consistency
Check EventStorming board against the Context Map.

![](../assets/EventStormingBoardWithBcs.jpg)
![](../assets/ContextMap.jpg)

# Answer

# Conformance Report — ContextMap.jpg × EventStormingBoardWithBcs.jpg

## 0. Scope checked

**Artifacts.** `ContextMap.jpg` (10 nodes, 14 directed edges, payload stickies on
most edges, sync/async legend). `EventStormingBoardWithBcs.jpg` (21 events, 12
context bubbles drawn, six distinct bubble names).

**The three preconditions — assumed, not confirmed. Correct me and some findings
change meaning.**

- *Was the map drawn from this board?* Assumed yes for eight of its ten nodes.
  **Two nodes almost certainly came from somewhere else** — see F4 and F5. That
  is the report's headline and it changes what the whole comparison is measuring.
- *Which is newer?* Assumed the map. Where the map adds something the board never
  drew, the report says **"the board never drew this"**, not "the map is wrong".
- *Is the board complete, or a slice?* No scope exclusion was stated with either
  artifact, so both are read as claiming to cover the same domain. If the board is
  a slice, F4, F5 and F13 soften considerably.

**Which map is this?** Read as a **model/strategic map**: nodes are bounded
contexts, edges carry business objects, patterns are DDD-crew (OHS, ACL, CF). Not
checked as a deployment or team map.

**Bubbles drawn:** yes — twelve on the board, resolving to six names. Node-level
findings are therefore *not* provisional; the cut is in evidence.

**UNCHECKED.** Three things I could not read with confidence:

1. **Port placement convention.** OHS boxes sit on the *receiving* end of five
   edges (Notification, Sharing ×2, Meal Preparation ×2, Cooking Assistance). If a
   port box means "this context's own exposed interface", the map is consistent; if
   it means "upstream open host", five of them are on the wrong side. F14 asks;
   nothing else in this report depends on it.
2. **Edge M2** (Cook Profile → Consent Management) carries a *Consent* sticky, and
   *Consent* appears nowhere on the board. Direction unverifiable — see F18.
3. **The green *Catastrophe* sticky** on the board straddles the Media / Cooking
   Assistance bubble border. Attributed to both readers; not used to carry any
   finding on its own.

**Board-rendering note.** In this rendering **all four help bubbles are named
`Cooking Assistance`**. The project's `invariants-community-cooking-board.md` read
two of them as `Cooking Help`. Whatever changed, the board now says one name four
times, which is why the recurrence check below comes out clean.

---

## 1. Verdict

**Consistent with drift.** The map does the hardest thing correctly — every
recurring bubble is collapsed to one node — and its object-carrying edges around
help and pictures match the stickies. The drift is concentrated in three places:
two nodes that came from a different artifact, the ownership of *Help response*
between Cooking Assistance and the Grandma Avatar, and the whole Sharing border.

| | Count |
|---|---|
| Blocking | 3 |
| Significant | 10 |
| Minor | 5 |

| Resolution direction | Count |
|---|---|
| Map moves | 7 |
| **Back to the wall** | **6** |
| Ask | 5 |

A third of this report is not a map defect. It is the board being thin in exactly
the places the map had to guess.

---

## 2. Board as read

21 events. `👁` = green read model consumed · `💼` = pale-yellow business object
produced · `⚙` pink = external system. **Board spellings preserved verbatim,
including the slips.**

| # | Event | Command | Actor(s) | Reads 👁 | Produces 💼 | Bubble |
|---|---|---|---|---|---|---|
| 1 | Cook registered | Register cook | User | User | **Cook** | Cook Profile |
| 2 | Dinner planned | Plan dinner | Cook | Guests | **Menu** | Meal Planning ① |
| 3 | Recipes searched | Search recipes | Cook | Recipe Catalog | — | Meal Planning ① |
| 4 | Recipe selected | Search recipes *(sic)* | Cook | Recipe | — | Meal Planning ① |
| 5 | Ingredients missing | Search Ingredients | Cook | Recipe | — | Meal Planning ① |
| 6 | Meal planning stalled 🤖 | Prepare meal *(sic)* | Cook | Guests, Menu | — | Meal Planning ① |
| 7 | Help requested | Request help | Cook | Ingredients, Menu | **Help request** | Cooking Assistance ① |
| 8 | Help provided | Provide help | Community Cook, Chef, *Grandma Avatar* ⚙ | Help request | **Help response** | Cooking Assistance ② |
| 9 | Ingredients substituted | Substitute ingredients | Cook | Recipe, Help Response | **Ingredients** | Meal Planning ② |
| 10 | Meal plan setteled *(sic)* | Plan meal | Cook | Menu, Help Response | **Meal plan** | Meal Planning ② |
| 11 | Meal preparation started | Prepare meal | Cook | Recipe | — | Meal Preparation ① |
| 12 | Step unclear | Prepare meal | Cook | Recipe | — | Meal Preparation ① |
| 13 | Catastrophe happened | Prepare meal | Cook | Catastrophe, Recipe | — | Meal Preparation ① |
| 14 | Pictures taken | Take pictures | Cook | Catastrophe *(straddles)* | **Pictures** | Media ① |
| 15 | Help requested | Request help | Cook | Recipe, Pictures, Catastrophe *(straddles)* | **Help request** | Cooking Assistance ③ |
| 16 | Help provided | Request help *(sic)* | Community Cook, *Grandma Avatar* ⚙ | Help request | **Help response** | Cooking Assistance ④ |
| 17 | Step competeted *(sic)* | Prepare meal | Cook | Help response | — | Meal Preparation ② |
| 18 | Meal rescued 🤖 | Prepare meal | Cook | Help response | — | Meal Preparation ② |
| 19 | Meal prepared | Prepare meal | Cook | Recipe | — | Meal Preparation ② |
| 20 | Pictures taken | Take pictures | Cook | — | **Pictures** | Media ② |
| 21 | Thanks given | Provide thanks | Cook | Help provider, Pictures | **Thanks** | Sharing |

**Flagged.** `Prepare meal` is written on seven events (6, 11, 12, 13, 17, 18, 19)
— read as a placeholder for several commands, not as one interaction. Events 4 and
16 carry commands that read as slips (*Search recipes* for a selection, *Request
help* for a provision). No red hotspot stickies anywhere on the board. Bubble
appearance counts: Cook Profile 1 · Meal Planning 2 · **Cooking Assistance 4** ·
Meal Preparation 2 · Media 2 · Sharing 1.

---

## 3. Map as read

**Nodes (10).** Recipe Catalog · Cook Profile · Meal Planning · Grandma Avatar AI ·
Notification · Cooking Assistance · Media · Consent Management · Meal Preparation ·
Sharing. None is marked external, off-board or out of scope; all are drawn
identically.

**Edges (14 directed).** `(not stated)` where the map says nothing.

| ID | From → To | Payload as drawn | Mech. | Pattern port | U/D labels |
|---|---|---|---|---|---|
| M1 | Recipe Catalog → Meal Planning | Recipe 💼 | sync | OHS *(on Recipe Catalog)* | (not stated) |
| M2 | Cook Profile → Consent Management | Consent 💼 | sync | OHS *(on Cook Profile)* | (not stated) |
| M3 | Meal Planning → Cooking Assistance | Menu 💼, Ingredients 💼, Recipe 👁 | sync | OHS *(on Meal Planning)* | (not stated) |
| M4 | Cooking Assistance → Meal Planning | Help response 💼 | sync | *(same port)* | (not stated) |
| M5 | Meal Preparation → Cooking Assistance | Menu 👁, Recipe 👁 | sync | OHS *(on Meal Preparation)* | (not stated) |
| M6 | Cooking Assistance → Meal Preparation | Help response 💼 | sync | *(same port)* | (not stated) |
| M7 | Cooking Assistance → Notification | Help request 💼, Help response 💼 | **async** | OHS *(on Notification)* | (not stated) |
| M8 | Cooking Assistance → Grandma Avatar AI | Help request 💼 | **async** | **ACL** *(on Grandma Avatar AI)* | (not stated) |
| M9 | Grandma Avatar AI → Cooking Assistance | Help response 💼 | **async** | *(same ACL)* | (not stated) |
| M10 | Media → Cooking Assistance | Pictures 💼 | sync | OHS *(on Cooking Assistance)* | (not stated) |
| M11 | Media → Sharing | Pictures 💼 | sync | OHS *(on Sharing)* | (not stated) |
| M12 | Meal Planning → Meal Preparation | Recipe 💼, Menu 💼 | sync | OHS *(on Meal Preparation)* | (not stated) |
| M13 | Meal Preparation → Sharing | **Help response 💼** | sync | OHS *(on Sharing)* | (not stated) |
| M14 | Consent Management → Sharing | Consent 💼 | sync | **CF** *(on Sharing)* | (not stated) |

**Node contents as drawn.** Meal Planning holds all 7 planning events. Cooking
Assistance holds `Help requested` + `Help provided` once each (correct collapse) and
four green read models. **Meal Preparation holds only 3 of the board's 6 events** —
`Meal preparation started`, `Catastrophe happened`, `Step unclear`. Media holds
`Pictures taken` once. Grandma Avatar AI holds a *second copy* of `Help requested`
and `Help provided`, plus `Help request` 💼 and `Help response` 💼.

---

## 4. Term ledger

| Term (board spelling) | Written by (board) | Read by (board) | Map says owned by | Verdict |
|---|---|---|---|---|
| User | *nobody* | Cook Profile (1) | *no node* | off-board identity, undrawn — minor |
| **Cook** | Cook Profile (1) | actor in all 5 other contexts | Cook Profile | owner ✅, **crossings undrawn → F7** |
| Guests | *nobody* | Meal Planning (2, 6) | *no node* | undrawn — part of Meal plan? **F13** |
| **Menu** | Meal Planning (2) | Meal Planning (6, 10), Cooking Assistance (7) | Meal Planning | owner ✅; map adds two readers the board lacks → **F8** |
| Recipe Catalog | *nobody* | Meal Planning (3) | Recipe Catalog | ✅ off-board upstream correctly drawn |
| **Recipe** | *nobody* | Meal Planning (4,5,9), Meal Preparation (11,12,13,19), Cooking Assistance (15) | Recipe Catalog | ✅ owner right; relayed via M12/M3 rather than direct — ask |
| **Ingredients** | Meal Planning (9) | Meal Planning (5), Cooking Assistance (7) | **Recipe Catalog** | **two writers / one word → F3** |
| **Help request** | Cooking Assistance (7, 15) | Cooking Assistance (8, 16) | Cooking Assistance **and** Grandma Avatar AI | **contested → F1** |
| **Help response** | Cooking Assistance (8, 16) | Meal Planning (9,10), Meal Preparation (17,18) | Cooking Assistance **and** Grandma Avatar AI | **contested → F1**; also on M13 → **F2** |
| **Help provider** | *nobody* | Sharing (21) | *no node, no edge* | **crossing with no edge → F2** |
| **Pictures** | Media (14, 20) | Cooking Assistance (15), Sharing (21) | Media | ✅ both crossings drawn; **one word, two models → F12** |
| Catastrophe | *nobody* | Meal Preparation (13), Cooking Assistance (15), Media (14?) | *no node* | read twice, written never → **F13** |
| **Meal plan** | Meal Planning (10) | ***nobody*** | Meal Planning | **orphan — F10**; not in any contract (map is honest here) |
| **Thanks** | Sharing (21) | *nobody* | Sharing | orphan, not in a contract — no finding |
| *Consent* | **not on the board** | — | Consent Management | **F5** |
| *Notification* | **not on the board** | — | Notification | **F4** |

Read the verdict column straight down: one orphan the map correctly declined to put
in a contract (*Meal plan*), one crossing with no edge (*Help provider*), one term
with two writers (*Ingredients*), two terms with a contested owner (*Help
request/response*), and two terms that exist only on the map.

---

## 5. Node reconciliation

| Board bubble | Appearances | Map node(s) | Rung | Verdict |
|---|---|---|---|---|
| Cook Profile | 1 | Cook Profile | exact | ✅ |
| Meal Planning | 2 | Meal Planning | collapse | ✅ correct |
| **Cooking Assistance** | **4** | **Cooking Assistance** | collapse | ✅ **correct — the hardest call on this board** |
| Meal Preparation | 2 | Meal Preparation | collapse | ✅ node; ⚠️ half its events missing → F6 |
| Media | 2 | Media | collapse | ✅ correct |
| Sharing | 1 | Sharing | exact | ✅ |
| *(Grandma Avatar ⚙, inside CA ② and ④)* | 2 | Grandma Avatar AI | pink → node | ✅ **allowed** (see §8); only ownership is open → F1 |
| *(Recipe Catalog, green read model)* | 1 | Recipe Catalog | off-board upstream | ✅ correctly promoted |
| — | — | **Notification** | *other artifact* | **F4** |
| — | — | **Consent Management** | *unmatched* | **F5** |

**Twelve bubbles → six nodes, with no over-splitting and no wrong merges.** That is
the check most maps fail, and it is worth saying before any finding below: the
map's cut *is* the board's cut.

**Not the timeline in disguise.** The spine runs in board order, but Media, the
Avatar, Notification and Consent all sit off it, and every spine edge names an
object rather than meaning "and then". No whole-map finding here.

---

## 6. Findings

### Blocking

**F1 · `OWN` · Who owns a Help response — Cooking Assistance or the Grandma Avatar?**

*Evidence.* On the board the pink `Grandma Avatar` ⚙ is an **actor on events 8 and
16**, standing beside `Community Cook` and `Chef`, under the same `Provide help`
command, producing the same `Help response` 💼 into the same `Cooking Assistance`
bubble. On the map, `Grandma Avatar AI` is a node holding **its own copies** of
`Help requested`, `Help provided`, `Help request` 💼 and `Help response` 💼, and
M8/M9 move both objects across an ACL.

*Why it matters.* The node itself is fine and is not reported — an external system
is a bounded context, and your `prototype-community-cooking-event-model.md` records
the decision to let the Event Model's grid win on exactly this point. What is
missing is the sentence that decision needs: **which model owns the shared
aggregate.** As drawn, two nodes write `Help request` and `Help response` and
neither is marked as the writer of record. Anyone building from this map builds two
help stores, and `INV-HELP-06` ("many answers, at most one resolving") becomes
unenforceable because no single context sees all the answers.

*What would settle it.* One sentence: *"Does a request answered by the avatar and a
request answered by a Community Cook end up in the same store?"*

*Recommendation — **ask**, and I would answer:* Cooking Assistance owns both
objects; the avatar supplies **content** through the translation, and never
instantiates a `Help request` of its own. Then M8/M9 carry an answer, not an
aggregate, and the duplicated stickies come off the Avatar node.

---

**F2 · `CONTRACT` + `EDGE` · The Sharing border carries the wrong object, and the
right one has no edge**

*Evidence.* M13 draws **Meal Preparation → Sharing carrying `Help response` 💼**.
The board says: `Help response` is written by **Cooking Assistance** (8, 16), never
by Meal Preparation; and `Thanks given` (21) reads **`Help provider` 👁 and
`Pictures` 👁** — not `Help response`. Meanwhile `Help provider` is read by Sharing
and written by nobody, and **no map edge connects Cooking Assistance to Sharing at
all.**

*Why it matters.* Two artifacts assert different facts. Someone following the map
integrates Sharing with the kitchen to fetch advice, when what Sharing actually
needs is *who answered*, from the help context. The reciprocity loop — the one
mechanism on this board that sustains the community — is drawn between the two
contexts that have nothing to do with it.

*Map moves.* Replace M13's payload with the finished meal (see F6 — `Meal prepared`
is the fact Sharing waits on, and it is missing from the node). Add
**Cooking Assistance → Sharing carrying `Help provider`**.

*Back to the wall, in the same hour.* Nothing on the board **writes** `Help
provider`. It is read at 21 and produced nowhere. Draw its writer, or accept that
Sharing is reading a projection the help context has never been asked to publish.

---

**F3 · `OWN` + `TERM` · `Ingredients` has two writers and probably two models**

*Evidence.* The map's `Recipe Catalog` node holds `Ingredients` 💼 alongside
`Recipe` 💼. The board has **Meal Planning writing `Ingredients` 💼 at event 9**
(`Ingredients substituted`), and Cooking Assistance reading `Ingredients` 👁 at
event 7.

*Why it matters.* These are plausibly two different things wearing one word: a
recipe's ingredient list, owned off-board, and *this cook's post-substitution list
for this dinner*, owned by Meal Planning. The map silently gave the word to the
catalogue, so M3's `Ingredients` payload is ambiguous — the responder at event 7
sees either the recipe's list or the cook's, and those differ by exactly the thing
the help request is about.

*What would settle it.* *"When a cook substitutes walnuts for pine nuts, does that
change anything in the catalogue?"* If no, they are two terms.

*Recommendation — **ask**, expecting **map moves**:* split into
`Recipe Ingredients` (Recipe Catalog) and `Ingredient list` (Meal Planning).

### Significant

**F4 · `NODE` · `Notification` traces to a different artifact — this is the
provenance headline**

There is **no `Notification` sticky, bubble or noun anywhere on the board.** There
*is* a `Notification` swimlane in this project's
`prototype-community-cooking-event-model.md`, whose lane order reads
`Registration · Meal Planning · Sharing · Cook Assistance · Meal Preparation ·
Notification · AI Avatar` — which is the map's node list, plus Recipe Catalog and
Consent Management, minus Media.

**The map's nodes match the Event Model better than they match this board.** That is
not an error, and the finding is not "no evidence" — it is *"evidence from a
different artifact, and the map should say which one governs."* Three prior analyses
in this project also reached Notification independently as *implied and unmodelled*,
so the context is almost certainly real. Say where it came from, and put a
`Community/Chef informed` sticky on the wall. **Ask** → then **back to the wall**.

---

**F5 · `NODE` · `Consent Management` has no board evidence at all**

No sticky, no bubble, no `Consent` noun, no event that grants or withdraws consent.
Two edges hang off it (M2, M14) and it is the only node carrying a `CF` pattern. The
nearest thing in this project is `INV-MEDIA-03` in the invariants sheet — an
**inferred** rule about guests consenting before their photo is shared, flagged
there as inferred rather than read.

**Unsupported, not wrong.** A map may legitimately encode a decision made after the
session, and given that this board routes private kitchen photos to strangers and an
AI persona, adding consent is a *good* decision. But as drawn it reads as a
description of a working system. **Ask**: was this decided after the session, or
imported from a compliance requirement? Either answer belongs on the map as a note.

---

**F6 · `NODE` (coverage) · Half of Meal Preparation is missing from the map**

The board's Meal Preparation bubbles hold six events; the map's node holds three.
Absent: **`Step competeted`**, **`Meal rescued`**, **`Meal prepared`**. The node
therefore contains only the *trouble* half of cooking and none of the *resolution*
half — which is precisely why M6 (`Help response` inbound) has no consumer drawn and
why M13 could not find the finished meal to carry (F2). **Map moves:** add the three
events, or state that the map deliberately shows only the events that cross borders
— which would be a reasonable rule, applied nowhere else on this map.

---

**F7 · `EDGE` (reverse sweep) · `Cook` crosses into five contexts and is drawn
crossing into one**

`Cook` 💼 is written by Cook Profile at event 1 and appears as the actor sticky on
events in **Meal Planning, Cooking Assistance, Meal Preparation, Media and
Sharing**. The map's only edge out of Cook Profile is M2, to Consent Management.
Cook Profile carries an OHS, which is the right pattern for one writer with many
readers — the readers just are not drawn. **Map moves:** five thin edges, or one
stated convention ("Cook Profile's OHS serves every context; edges omitted"). One
finding, not five.

---

**F8 · `CONTRACT` · `Menu` is named crossing into Meal Preparation; nothing there
reads it**

M12 carries `Menu` 💼 into Meal Preparation, M5 carries `Menu` 👁 back out, and the
node shows `Menu` 👁 as a read model. On the board, **no Meal Preparation event
reads Menu** — 11, 12, 13, 19 read `Recipe`, 13 also reads `Catastrophe`, 17 and 18
read `Help response`. Either the board never drew a read that happens (likely — a
cook at the stove plausibly looks at the menu), or the payload was copied from the
Meal Planning → Cooking Assistance contract where it *is* supported. **Back to the
wall**, one sticky either way.

---

**F9 · `GAP` · Meal Preparation is given four borders and an OHS while writing no
business object anywhere**

Every one of its six board events produces **nothing pale-yellow**. Yet `Step
unclear`, `Step competeted`, `Catastrophe happened`, `Meal rescued` and `Meal
prepared` all presuppose something with state. The map gives this context an Open
Host Service on M5, M6, M12 and M13 — **an OHS over nothing.** Whatever crosses
those borders has no aggregate to carry it.

This is the finding all three prior analyses in this project reached independently
(*"a stretch with no business object"*, *"ten invariants with nowhere to live"*, *"a
lane whose Meal is clearly stateful and clearly unnamed"*). It now has a map
consequence as well as a model one. **Back to the wall — highest value hour
available on this board.**

---

**F10 · `GAP` · `Meal plan` is written, read by nobody, and quietly absent from the
map**

Event 10 produces `Meal plan` 💼. **No event on the board reads it.** The map does
not name it on any edge — which is the *honest* move, and I want to be clear the map
is not being scored down for it. But look at what M12 carries instead: `Recipe` and
`Menu`. **The map routes the plan's contents across the planning/cooking border and
never routes the plan.** That is the board's orphan showing through, and it means
one of three things: the read model is simply undrawn; a **Shopping /
Mise-en-place** context is missing between the two; or the meal plan is a report and
the border sits in the wrong place. **Back to the wall — do not build M12 until the
team says which.**

---

**F11 · `GAP` · Two of the map's busiest edges rest on policies the board never
drew**

M3 (Meal Planning → Cooking Assistance) and M5 (Meal Preparation → Cooking
Assistance) are the crossings that carry a stuck cook to a responder. On the board,
`Ingredients missing` (5), `Meal planning stalled` (6), `Step unclear` (12) and
`Catastrophe happened` (13) sit next to `Request help` (7, 15) with **nothing
between them** — no lilac policy sticky anywhere. The map's edges present as
mechanism what the board leaves as adjacency.

*What would settle it.* *"Does the cook press a button, or does something notice they
are stuck and offer?"* If the system offers, those four policies are where this
product's behaviour lives and they belong on both artifacts. **Back to the wall.**

---

**F12 · `TERM` · `Pictures` is one word carrying two models, and the map uses the
same label on both borders**

M10 carries `Pictures` from Media into Cooking Assistance: on the board these are
the photos taken at event 14, **reading `Catastrophe`** — diagnostic evidence,
lifetime of the request, audience of one responder. M11 carries `Pictures` from
Media into Sharing: the photos taken at event 20, **reading nothing at all** — a
result, indefinite lifetime, audience the whole community.

Same word, two lifecycles, two audiences, two sets of questions asked of them. This
is exactly the collision a border exists to record, and it will be implemented once
and then argued about. This project's own domain story already writes
**`Catastrophy Pictures`** in one place and `Pictures` in the other — the language
has already forked; only the artifacts have not caught up. **Map moves:** relabel
M10 `Catastrophe Pictures`, or split the Media node's object in two.

---

**F13 · `EXT` + `GAP` · Three nouns are read and never written, and none is on the
map**

`Catastrophe` — read by Meal Preparation (13) and Cooking Assistance (15), possibly
Media (14); written nowhere; owned by no node; named on no contract. `Guests` — read
twice in Meal Planning, written nowhere. `User` — read at event 1, written nowhere.

`Catastrophe` is the one that matters: it is read **across a context border**, so
something must carry it, and the map's M5 does not name it. It is almost certainly
an undrawn part of the aggregate F9 says is missing, not an off-board upstream —
which makes it the same finding twice. **Back to the wall.**

### Minor

**F14 · `PAT` · One finding about the whole map, not one per edge.** Eight of ten
nodes carry an `OHS` port; the only other patterns are one `ACL` and one `CF`. No
edge carries `U`/`D` labels, so direction rests entirely on arrowheads. And the port
boxes sit on the *receiving* end of five edges (Notification, Sharing ×2, Meal
Preparation ×2). If the convention is "each context's own exposed interface", say so
on the legend; if it is DDD-crew's "OHS marks the upstream", five ports need moving.
**Sub-point worth its own line:** the `ACL` on M8/M9 sits on the **Grandma Avatar
AI** boundary. Convention puts the anticorruption layer on the side protecting
itself — and this project's `context-cut-cooking-rescue-story.md` argues for exactly
that: wrap the avatar *from inside the help context*, and label its answers as
machine-generated where the cook sees them. As drawn, the AI is protecting itself
from you.

**F15 · `PAT` · Two mutual crossings, one label each.** M3/M4 and M5/M6 carry
objects in both directions with a single OHS on one end. Both sides write; that is
either a genuine **Partnership** or Customer/Supplier running both ways with
separate contracts. The map does not say which, and the coordination cost differs.
**Ask** — the answer is organisational and the board cannot show it.

**F16 · `TERM` · Names.** Map `Grandma Avatar AI` vs board `Grandma Avatar` — a
synonym, matched, noted once. The map preserves the board's `Meal plan setteled`
(good — do not normalise it silently, but do fix it on the wall) and introduces
`Ingre dietns` where the board says `Ingredients`. The board's `Step competeted` is
absent from the map only because F6 dropped the event.

**F17 · `STALE` · M12 is drawn synchronous.** The planning → preparation border is
the one gap this domain plausibly tolerates in hours or days — a plan settled on
Thursday, cooked on Saturday. The board shows no clock, so this is an open question
rather than a contradiction, but a synchronous mechanism there asserts the kitchen
is online with the planning context at cook time. Worth one sentence on the map.

**F18 · `DIR` (uncertain) · M2 carries `Consent` away from its own writer.** As
drawn, Cook Profile → Consent Management with a `Consent` 💼 payload. If Consent
Management writes `Consent` (M14 says it does), the payload on M2 is travelling
upstream. Most likely M2 actually carries `Cook`, and the `Consent` sticky belongs to
M14. Nothing on the board can settle it — **ask**.

---

## 7. Undrawn edges and missing nodes

The reverse sweep — crossings the board shows and the map does not:

| Crossing on the board | Map edge | Status |
|---|---|---|
| `Cook` : Cook Profile → 5 contexts | none | **F7** |
| `Help provider` : (unwritten) → Sharing (21) | none | **F2** |
| `Catastrophe` : Meal Preparation ↔ Cooking Assistance (13, 15) | not on M5 | **F13** |
| `Recipe` : Recipe Catalog → Meal Preparation, → Cooking Assistance | relayed via M12/M3 | ask — deliberate relay or missing edges? |
| `Pictures` : Media → Cooking Assistance, → Sharing | M10, M11 | ✅ both drawn |

**Off-board upstreams neither artifact drew.** An identity provider behind `User`
(read at event 1, written nowhere) — minor, and arguably inside Cook Profile.
Nothing else: the map already promoted `Recipe Catalog`, which is the off-board
upstream three prior analyses in this project kept flagging as missing. That one is
closed.

**No missing bubbles.** Every board bubble appears on the map.

---

## 8. Allowed divergence — deliberately not reported

- **The `Grandma Avatar AI` node itself.** A pink sticky is a separate model outside
  the team's control, which is what a bounded context is. Drawing it as an ordinary
  node is correct and is never a finding here — nor is how it is drawn (unmarked,
  solid, named after the persona). Your `prototype-community-cooking-event-model.md`
  records this as a decision already taken. Only the *ownership silence* is reported
  (F1).
- **`Recipe Catalog` as a node.** Same reasoning, and the right call.
- **Team ownership, org structure, who can negotiate.** Never on a board.
- **Relationship patterns where the board shows nothing either way.** Patterns are
  decisions. F14 and F15 are about the map's internal consistency and about a
  convention, not about awarding patterns the board could settle.
- **Integration mechanisms and staleness windows nobody supplied.** Every `<n>` on
  this map is an open question, not a finding. F17 is raised once, softly.
- **Contract payloads at business resolution.** "Recipe" covering a whole recipe is
  fine; no field-level correspondence demanded.
- **Deployment, technology and service boundaries.** Different map.
- **Layout and aesthetics.** The spine runs in board order, but the map is not a
  timeline copy (§5), so nothing is reported.
- **The straddling `Catastrophe` sticky and the `Prepare meal` command overload.**
  Marked uncertain in §2 — unchecked, not absent, and no finding rests on them.

---

## 9. Patch list

### Edits to the map

1. **M13:** replace the `Help response` payload with the finished meal, and add
   **Cooking Assistance → Sharing carrying `Help provider`**. *(F2)*
2. **Grandma Avatar AI:** delete the duplicated `Help requested` / `Help provided` /
   `Help request` / `Help response` stickies and add one line — *"Cooking Assistance
   owns both objects; the avatar supplies response content."* *(F1)*
3. **Move the ACL** from the Grandma Avatar AI boundary to the Cooking Assistance
   boundary, and name what it translates. *(F14)*
4. **Meal Preparation node:** add `Step competeted`, `Meal rescued`, `Meal
   prepared`. *(F6)*
5. **`Ingredients`:** move out of Recipe Catalog, or split into `Recipe Ingredients`
   and `Ingredient list`. *(F3)*
6. **Cook Profile:** draw the five OHS consumers, or state the convention once.
   *(F7)*
7. **M10:** relabel the payload `Catastrophe Pictures`. *(F12)*
8. **Notification and Consent Management:** annotate provenance — *"from the Event
   Model"*, *"decided after the session"*. *(F4, F5)*
9. **Drop `Menu` from M12 and M5** unless a reader is drawn. *(F8)*
10. **Add `U`/`D` labels to all 14 edges** and a legend line stating what a port box
    on a boundary means. *(F14)*

### Back to the wall

1. **Name the Meal Preparation aggregate and put a business object on it.** Six
   events, four map borders, nothing written. *(F9)* — the single highest-value hour
   available, and the fourth independent analysis in this project to say so.
2. **Draw who reads `Meal plan`**, or accept that a Shopping / Mise-en-place context
   is missing between planning and cooking. *(F10)*
3. **Draw the four policies** between `Ingredients missing` / `Meal planning
   stalled` / `Step unclear` / `Catastrophe happened` and `Request help`. *(F11)*
4. **Draw the writer of `Help provider`.** *(F2)*
5. **Draw who writes `Catastrophe` and `Guests`.** *(F13)*
6. **Split `Prepare meal`** into the six commands it is standing in for — until then
   no guard on this board has anything to attach to.
7. **Fix the slips:** `Meal plan setteled`, `Step competeted`, event 4's `Search
   recipes`, event 16's `Request help`. And decide `Catastrophe` vs the domain
   story's `Catastrophy` before either enters the ubiquitous language.
8. **Add hotspots.** The board has **no red stickies at all**, while the map carries
   contested ownership on the single most important object in the domain. On a board
   whose own vocabulary is *stalled*, *unclear* and *catastrophe*, an empty hotspot
   column reads as *"disagreement was never captured"*, not as *"nobody disagreed"* —
   and the disagreements are exactly where the remaining decisions are.

---

## 10. Already consistent — what to protect

- **Recurrence is handled correctly on every recurring context.** Cooking Assistance
  4→1, Meal Planning 2→1, Meal Preparation 2→1, Media 2→1. This is the defect that
  breaks most maps, it is the one the project's own earlier pivotal-event cut got
  wrong twice, and this map does not make it. The four help appearances are evidence
  that Cooking Assistance is **central**, and the map reads them that way.
- **`Recipe Catalog` promoted to an off-board upstream with an OHS.** Read by seven
  events across three contexts, written by none — textbook, and the gap three prior
  analyses kept raising is now closed.
- **`Pictures` crossings both drawn, both in the right direction** — Media → Cooking
  Assistance (event 15) and Media → Sharing (event 21). Only the naming is open.
- **`Help response` flows out of Cooking Assistance into both Meal Planning (events
  9, 10) and Meal Preparation (events 17, 18)** — M4 and M6 match the stickies
  exactly, direction included.
- **`Menu` and `Ingredients` flowing Meal Planning → Cooking Assistance** matches
  event 7's read models.
- **All six board bubbles are on the map**, and the map declined to put the orphaned
  `Meal plan` into a contract — a restraint worth naming, since an orphan in a border
  contract is the sharpest defect this check finds.
- **No direction error that would make anyone build an integration backwards**,
  except the unreadable M2 (F18).

---

## What this report did not do

This is a conformance check, not a critique of either artifact. Nothing above judges
whether the map is a *good* map — whether Media deserves to be a context, whether
Sharing should exist at all, whether Cooking Assistance is the core domain. Those
are different passes: `event-storming-context-mapper` in review mode, or a Devil's
Advocate on the map itself. Given that thirteen of the board's twenty-one events
touch Cooking Assistance and every branch on the wall bends toward it,
`core-domain-chart-critic` is the one I would run next.