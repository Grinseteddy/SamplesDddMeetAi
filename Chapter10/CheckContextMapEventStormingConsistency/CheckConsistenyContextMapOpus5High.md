# Prompt 2026-09-08

/context-map-board-consistency
Check EventStorming board against the Context Map.

![](../assets/EventStormingBoardWithBcs.jpg)
![](../assets/ContextMap.jpg)

# Conformance Report — Community Cooking Context Map × EventStorming board

## 0. Scope checked

**Artifacts.** `ContextMap.jpg` (10 nodes, 12 edges) against
`EventStormingBoardWithBcs.jpg` (21 events in 12 context bubbles carrying 7
distinct names). Both read from the images alone. Project documents were used
only to identify a third artifact the map appears to draw on (§6, F1); no
finding rests on them.

**Bubbles drawn: yes.** The board carries its own cut — `Cook Profile`,
`Meal Planning` ×2, `Cooking Assistance` ×2, `Cooking Help` ×2,
`Meal Preparation` ×2, `Media` ×2, `Sharing`. So node-level findings are *not*
provisional here: there is a cut on the wall to check against.

**UNCHECKED — read the transcription in §3 before the findings.**

- The **`CF` boxes are drawn straddling a node border**, not on a labelled half
  of the edge. On five of them (Recipe Catalog, Cook Profile, Meal Planning,
  Notification, Sharing-bottom) the box sits on the border of what the payload
  evidence makes the **upstream** side. If `CF` means Conformist in the DDD Crew
  sense, the conformist is the downstream, so either the boxes are drawn on the
  wrong side or `CF` means something else here (a published interface? a
  translation point?). **F12 is written both ways.** One sentence settles it.
- The board sticky at Meal Planning reads `plan settled`; the map writes
  `Meal plan setteled`. Treated as the same event.
- Board `Step competeted` (sic) is on the wall and absent from the map (F6).

**Three preconditions — please answer these before acting on §6.**

1. **Was this map drawn from this board?** Seven of its ten nodes match the
   swimlanes of the Event Model in this project *exactly*, including two that
   have no bubble on this board at all. This is F1 and it changes what the rest
   of the report is measuring.
2. **Which is newer?** If the map is newer, several §6 findings are the board
   being stale rather than the map being wrong — `Consent Management` in
   particular reads like a decision taken after the session.
3. **Is this a model map or a team/deployment map?** Nodes like `Notification`
   and `Media` sit comfortably on a deployment map and awkwardly on a model map.
   Assumed **model map** throughout.

**No scope exclusion is stated on either artifact**, so a missing context is
treated as a finding rather than an agreed omission.

---

## 1. Verdict

> **Consistent with drift, on a different spine.** The map handles the board's
> hardest problem — recurrence — better than most maps do, and then reads its
> node set off a different artifact.

**16 findings: 1 blocking · 11 significant · 4 minor.**

By direction: **7 map moves · 5 back to the wall · 4 ask.** A near-even split,
which is itself the headline — roughly a third of what looks like map drift is
the board being thin, and the fix for those is an hour at the wall, not an edit
to the diagram.

The one blocking finding is a reversed supplier: **the map makes Meal
Preparation the upstream source of `Help response` into Sharing**, and the
board's stickies say `Help response` is written by the help context. Anyone
building from the map builds that integration backwards.

---

## 2. Board as read

`💼` = pale-yellow business object **produced** · `👁` = green read model
**consumed** · `⌘` blue command · `👤` yellow actor · pink = external system.
Board spelling kept verbatim.

| # | Event | Command ⌘ | Actor 👤 | Reads 👁 | Produces 💼 | Bubble |
|---|---|---|---|---|---|---|
| 1 | Cook registered | Register cook | User | User | **Cook** | Cook Profile |
| 2 | Dinner planned | Plan dinner | Cook | Guests | **Menu** | Meal Planning ① |
| 3 | Recipes searched | Search recipes | Cook | Recipe Catalog | — | Meal Planning ① |
| 4 | Recipe selected | Search recipes *(sic)* | Cook | Recipe | — | Meal Planning ① |
| 5 | Ingredients missing | Search Ingredients | Cook | Recipe | — | Meal Planning ① |
| 6 | Meal planning stalled 🤖 | Prepare meal *(sic)* | Cook | Guests, Menu | — | Meal Planning ① |
| 7 | Help requested | Request help | Cook | Ingredients, Menu | **Help request** | Cooking Assistance ① |
| 8 | Help provided | Provide help | Community Cook, Chef, *Grandma Avatar* (pink) | Help request | **Help response** | Cooking Assistance ② |
| 9 | Ingredients substituted | Substitute ingredients | Cook | Recipe, Help Response | **Ingredients** | Meal Planning ② |
| 10 | plan settled | Plan meal | Cook | Menu, Help Response | **Meal plan** | Meal Planning ② |
| 11 | Meal preparation started | Prepare meal | Cook | Recipe | — | Meal Preparation ① |
| 12 | Step unclear | Prepare meal | Cook | Recipe | — | Meal Preparation ① |
| 13 | Catastrophe happened | Prepare meal | Cook | Catastrophe, Recipe | — | Meal Preparation ① |
| 14 | Pictures taken | Take pictures | Cook | — | **Pictures** | Media ① |
| 15 | Help requested | Request help | Cook | Catastrophe, Recipe, Pictures | **Help request** | Cooking Help ① |
| 16 | Help provided | Request help *(sic)* | Community Cook, *Grandma Avatar* (pink) | Help request | **Help response** | Cooking Help ② |
| 17 | Step competeted *(sic)* | Prepare meal | Cook | Help response | — | Meal Preparation ② |
| 18 | Meal rescued 🤖 | Prepare meal | Cook | Help response | — | Meal Preparation ② |
| 19 | Meal prepared | Prepare meal | Cook | Recipe | — | Meal Preparation ② |
| 20 | Pictures taken | Take pictures | Cook | — | **Pictures** | Media ② |
| 21 | Thanks given | Provide thanks | Cook | Help provider, Pictures | **Thanks** | Sharing |

**Flagged.**

- **Twelve bubble appearances, seven names.** Meal Planning, Meal Preparation,
  Media appear twice; the help capability appears **four** times under **two**
  names.
- **Not one state sticky, and not one hotspot**, anywhere on the board.
- **Nothing produced by an event is red or lilac** — no policies drawn. The
  trouble events (5, 6, 12, 13) and the request events (7, 15) sit side by side
  with nothing between them.
- **No sticky sits in the overlap of two bubbles**, and none sits outside every
  bubble. The cut is unambiguous, which is why §5 can be strict.
- `Cook` appears both as a pale-yellow 💼 in Cook Profile and as a yellow 👤
  actor on 19 of 21 events. Read as: written once, used as an actor everywhere.

---

## 3. Map as read

**Nodes** (10) — none marked external, off-board or out of scope:

`Recipe Catalog` · `Cook Profile` · `Meal Planning` · `Grandma Avatar AI` ·
`Notification` · `Cooking Assistance` · `Media` · `Consent Management` ·
`Meal Preparation` · `Sharing`

Contents drawn inside the nodes:

| Node | Events shown | Objects shown |
|---|---|---|
| Recipe Catalog | — | 💼 Ingredients, 💼 Recipe |
| Cook Profile | Cook registered | 💼 Cook |
| Meal Planning | Dinner planned · Recipes searched · Recipe selected · Meal plan setteled · Ingredients substituted · Ingredients missing · Meal planning stalled 🤖 | 💼 Menu, 💼 Ingredients, 👁 Recipe |
| Grandma Avatar AI | Help requested · Help provided | 💼 Help response, 💼 Help request |
| Notification | — | 💼 Notification |
| Cooking Assistance | Help requested · Help provided | 💼 Help response, 💼 Help request, 👁 Menu, 👁 Ingredietens, 👁 Recipe, 👁 Pictures |
| Media | Pictures taken | 💼 Pictures |
| Consent Management | — | 💼 Consent |
| Meal Preparation | Meal preparation started · Catastrophe happened · Step unclear | 👁 Menu |
| Sharing | Thanks given | 💼 Thanks |

**Edges** (12). `sync` = solid, `async` = dashed, per the map's own legend.

| # | From | To | Mech. | Payload | Label | Staleness |
|---|---|---|---|---|---|---|
| E1 | Recipe Catalog | Meal Planning | sync | Recipe | CF | (not stated) |
| E2 | Cook Profile | Consent Management | sync | Consent | CF | (not stated) |
| E3 | Meal Planning | Cooking Assistance | sync | Help request | CF | (not stated) |
| E4 | Cooking Assistance | Meal Planning | sync | Help response | CF | (not stated) |
| E5 | Cooking Assistance | Notification | **async** | Help request, Help response | CF | (not stated) |
| E6 | Cooking Assistance | Grandma Avatar AI | **async** | Help request | **ACL** | (not stated) |
| E7 | Grandma Avatar AI | Cooking Assistance | **async** | Help response | **ACL** | (not stated) |
| E8 | Media | Cooking Assistance | sync | Pictures | CF | (not stated) |
| E9 | Cooking Assistance | Meal Preparation | sync | Help response | CF | (not stated) |
| E10 | Meal Preparation | Cooking Assistance | sync | Help request | CF | (not stated) |
| E11 | Meal Preparation | **Sharing** | sync | **Help response** | CF | (not stated) |
| E12 | Media | Sharing | sync | Pictures | CF | (not stated) |
| E13 | Consent Management | Sharing | sync | Consent | CF | (not stated) |

(13 rows; E6/E7 are one ACL drawn as two arrows.) **Every edge but the ACL pair
carries the same label, `CF`.** No `U`/`D` markers anywhere. No staleness window
anywhere. No node is marked external — including `Grandma Avatar AI`, which is
the one node the board's pink sticky says *is*.

---

## 4. Term ledger

The engine of the check. One row per noun on the board, plus the two the map
introduces.

| Term | Written by (board) | Read by (board) | Map says owned by | Verdict |
|---|---|---|---|---|
| **User** | *nobody* | Cook Profile (1) | *no node* | off-board identity upstream neither artifact drew — minor |
| **Cook** | Cook Profile (1) | actor on 19 of 21 events, every bubble | Cook Profile | ownership consistent — **but no map edge carries it → F11** |
| **Guests** | *nobody* | Meal Planning (2, 6) | *no node* | unwritten, single reader — back to the wall |
| **Menu** | Meal Planning (2) | Meal Planning (6, 10), Cooking Assistance (7) | Meal Planning | consistent — but the map also draws 👁 Menu inside Meal Preparation, which no board event reads → **F10** |
| **Recipe Catalog** | *nobody* | Meal Planning (3) | Recipe Catalog node | **map correctly adds an off-board upstream** ✔ |
| **Recipe** | *nobody* | Meal Planning (4, 5, 9), Meal Preparation (11, 12, 13, 19), Cooking Help (15) | Recipe Catalog | 3 consuming contexts, **1 edge drawn → F7** |
| **Ingredients** | Meal Planning (9) — *and* Recipe Catalog per the map | Meal Planning (5), Cooking Assistance (7) | **both** Recipe Catalog and Meal Planning | **contested / two models → F8** |
| **Meal plan** | Meal Planning (10) | *nobody* | Meal Planning | **orphan → F10** |
| **Help request** | Cooking Assistance (7), Cooking Help (15) | Cooking Assistance (8), Cooking Help (16) | Cooking Assistance **and** Grandma Avatar AI | collapse ✔ · ownership unstated → **F9** |
| **Help response** | Cooking Assistance (8), Cooking Help (16) | Meal Planning (9, 10), Meal Preparation (17, 18) | Cooking Assistance **and** Grandma Avatar AI | E4/E9 correct ✔ · **E11 reversed → F2** |
| **Help provider** | *nobody* | Sharing (21) | *not on the map at all* | **missing edge + unwritten noun → F3** |
| **Catastrophe** | *nobody* | Meal Preparation (13), Cooking Help (15) | *not on the map* | read by two contexts, written by none → **F14** |
| **Pictures** | Media (14, 20) | Cooking Help (15), Sharing (21) | Media | edges E8/E12 supported ✔ · **one word, two models → F13** |
| **Thanks** | Sharing (21) | *nobody* | Sharing | orphan — the reciprocity loop ends nowhere → §7 |
| **Notification** | *nobody on the board* | *nobody* | Notification node | **no board evidence → F5** |
| **Consent** | *nobody on the board* | *nobody* | Consent Management | **no board evidence → F4** |

Read the verdict column straight down and the report writes itself: two orphans,
four unwritten nouns, one contested term, one term with two models, and two
terms that exist only on the map.

---

## 5. Node reconciliation

Board bubbles collapsed by recurrence first, then set against the map.

| Board bubble (appearances) | Map node | Rung | Note |
|---|---|---|---|
| Cook Profile ×1 | Cook Profile | exact | ✔ |
| Meal Planning ×2 | Meal Planning | **collapse** ✔ | both appearances' events present |
| Cooking Assistance ×2 **+ Cooking Help ×2** | Cooking Assistance | **collapse** ✔ | 4 appearances → 1 node. See §10 — this is the best thing on the map |
| Meal Preparation ×2 | Meal Preparation | **collapse, half-applied** | appearance ② dropped → **F6** |
| Media ×2 | Media | **collapse** ✔ | |
| Sharing ×1 | Sharing | exact | ✔ |
| *Grandma Avatar* (pink, inside two help bubbles) | Grandma Avatar AI | — | **allowed** — a pink sticky is a bounded context. Not a finding (§8) |
| — | Recipe Catalog | off-board upstream | ✔ supported: 👁 Recipe Catalog read at event 3, `Recipe` written by nobody |
| — | **Notification** | **another artifact** | **F1 / F5** |
| — | **Consent Management** | **unmatched** | **F1 / F4** |

**Collapse evidence for the four help appearances** (so nobody has to re-argue
it): identical commands `Request help` / `Provide help`, identical objects
`Help request` 💼 / `Help response` 💼, overlapping responders (Community Cook in
both), the **same** external system (Grandma Avatar in both), same shape. Four
of the five collapse tests. The burden is on whoever wants two.

**The one condition that would make the split real** is not on the board: a
response-time requirement differing by an order of magnitude. A stalled plan can
wait overnight; a step unclear at the stove cannot wait five minutes. That is an
argument about a number, and the number is nowhere. See F16.

---

## 6. Findings

### Blocking

---

**F2 · `DIR` + `OWN` — E11 makes Meal Preparation the supplier of `Help response`
to Sharing.**

*Evidence.* `Help response` 💼 is produced at events **8** and **16**, both
inside the help bubbles. Meal Preparation **reads** it at 17 and 18 and never
writes it. The writer is upstream; the map's E11 runs
`Meal Preparation → Sharing` carrying it.

*Why it matters.* This is the one defect class that reliably makes someone build
an integration backwards. It also mislabels the only handover the board *does*
show between those two contexts, which is `Meal prepared` (event 19) — an event
the map does not carry (**F6**).

*Recommendation — map moves.* Relabel E11's payload to the finished meal
(identity, recipe reference, finished-at), and add the edge that actually
carries `Help provider` (**F3**). What Sharing needs from Meal Preparation is a
cooked meal; what it needs from the help context is who to thank.

*What would settle it in one sentence:* when a cook gives thanks, is the
information about **who helped** reaching Sharing from the kitchen or from the
help thread?

### Significant

---

**F1 · `NODE` — the map's node set traces to the Event Model, not to this board.
This is the report's headline.**

*Evidence.* Seven of the ten nodes — Cook Profile≈Registration, Meal Planning,
Sharing, Cooking Assistance, Meal Preparation, **Notification**,
**Grandma Avatar AI**≈AI Avatar — are the Event Model's swimlanes in this
project, in that set. Two of those seven have **no bubble on this board**. The
board's own `Cooking Help` name appears nowhere on the map. `Media` comes from
the board and not from the Event Model; `Consent Management` from neither.

*Why it matters.* This is not an error, it is a **provenance** question, and it
changes what every downstream comparison is measuring. A map inheriting a grid
from a different artifact is drifting from *that* artifact, not from this one —
and half of §6 would be re-scored if the answer is "we mapped the Event Model".

*Recommendation — ask, and then annotate.* Whichever artifact governs, say so on
the map. If the Event Model's grid wins, F5 stops being a defect and becomes a
statement that the board is behind. If the board governs, `Notification` and
`Grandma Avatar AI` need board evidence.

---

**F6 · `COLL` — Meal Preparation's second appearance did not make it onto the
map.**

*Evidence.* The board draws Meal Preparation twice: ① `Meal preparation started`,
`Step unclear`, `Catastrophe happened` — ② `Step competeted`, `Meal rescued`,
`Meal prepared`. The map's Meal Preparation node carries **exactly appearance ①**.
Meal Planning and Media and the help bubbles were all collapsed correctly, so
this is a half-applied collapse rather than a policy.

*Why it matters.* The three missing events are the whole second half of the
kitchen: the recovery loop, the AI-marked rescue, and **the handover to
Sharing**. F2 is a direct consequence — with `Meal prepared` off the map, there
was no fact left to label E11 with, and `Help response` got used instead.

*Recommendation — map moves.* Fold appearance ② into the node. Then re-read
E11.

---

**F3 · `EDGE` — no edge carries `Help provider` from the help context to
Sharing.**

*Evidence.* Event 21 `Thanks given` reads 👁 `Help provider`. Nothing on the
board writes that term, but the only context that could is the one that records
who answered — Cooking Assistance. The map has no Cooking Assistance → Sharing
edge, and `Help provider` appears nowhere on it.

*Why it matters.* This is the reciprocity loop — the mechanism the whole
community proposition rests on — and on the map it does not exist. Combined with
F2 the map currently routes the thank-you through the kitchen.

*Recommendation — map moves + back to the wall.* Add
`Cooking Assistance → Sharing` carrying who answered. Then take the harder half
to the wall: **`Help provider` is read by an event and written by none.** Either
`Help provided` produces it and the sticky is missing, or it is a projection
over `Help response` that nobody has drawn.

---

**F7 · `EDGE` — `Recipe` reaches three contexts on the board and one on the
map.**

*Evidence.* `Recipe` is consulted at events 4, 5, 9 (Meal Planning), **11, 12,
13, 19 (Meal Preparation)** and **15 (Cooking Help)**. Written by no event
anywhere — which is exactly why the `Recipe Catalog` node is right. The map
draws E1 to Meal Planning only. Note the map's own Cooking Assistance node
already *shows* 👁 Recipe inside it, so the crossing is acknowledged and simply
not drawn.

*Recommendation — map moves.* Add `Recipe Catalog → Meal Preparation` and
`Recipe Catalog → Cooking Assistance`. Three consumers of one unwritten noun is
the strongest argument a board makes for an upstream, and the map already
believes it.

---

**F4 · `NODE` + `EDGE` + `CONTRACT` — `Consent Management` and both its edges
have no board evidence at all.**

*Evidence.* The word `Consent` appears on **no sticky** on this board. E2
(`Cook Profile → Consent Management`) and E13 (`Consent Management → Sharing`)
both carry a payload nothing produces, into and out of a node nothing supports.

*Why it matters — and why this is not "the map is wrong".* Of the three
explanations for an unsupported edge, the likeliest here is **decided after the
session**: the board does route a cook's kitchen photographs to strangers and
publishes pictures that may contain identifiable guests, and somebody has
evidently since decided that needs consent. That is the map being *ahead* of the
board, which is the good kind of drift — but the board cannot verify any of it,
and a reader cannot tell a considered privacy decision from a stray node.

*Recommendation — back to the wall.* Run the consent path as events: who grants
it, against what, and which event reads it before `Pictures` leave the cook's
phone. Also note the map-internal wrinkle: `Consent Management` holds 💼 `Consent`
(it writes it) yet E2 points *into* it carrying `Consent`. One of those two is
backwards, and the board cannot say which.

---

**F5 · `NODE` + `EDGE` — `Notification` has no board evidence.**

*Evidence.* No bubble, no event, no sticky. The 💼 `Notification` object exists
only on the map. E5 is drawn **async**, which is at least the right instinct.

*Why it matters.* Every prior analysis of this board flagged notification as
*implied and unmodelled* — `Help requested` only works if somebody is told. So
the map is almost certainly recording something real. But it is presented at the
same weight as contexts with seven events behind them, and a reader cannot tell.

*Recommendation — back to the wall, or annotate.* Either draw the informing on
the wall (it is one policy and one event), or mark the node as **from the Event
Model** per F1. Not a defect; an unlabelled provenance.

---

**F9 · `OWN` — `Grandma Avatar AI` and `Cooking Assistance` both carry
`Help requested` / `Help provided` and both `Help request` and `Help response`,
and the map does not say which model owns them.**

*Evidence.* On the board the pink `Grandma Avatar` sticky sits **inside** both
help clusters, as one actor on `Provide help` beside Community Cook and Chef,
producing the same `Help response` 💼. On the map the same two events and the
same two objects are drawn inside two nodes.

*Why it matters.* Two writers of one aggregate with no stated owner is a
standing invitation to implement `Help request` twice. The board's own placement
is unambiguous — the avatar is a responder inside the help context — and the ACL
in E6/E7 is exactly the right treatment, so this is a missing sentence rather
than a wrong structure.

*Recommendation — map moves.* State on the map that **Cooking Assistance owns
`Help request` and `Help response`**, and that the avatar supplies response
content through the ACL. Remove the duplicated event stickies from the avatar
node, or mark them as the same events seen from outside.

*Open question, not a finding:* the map should also say what the ACL
**translates** — an ACL with no named model difference is a proxy. The obvious
candidate is machine-generated versus human advice, which the board's own colour
already distinguishes.

---

**F10 · `GAP` — `Meal plan` is written and read by nobody, and the map draws
👁 `Menu` inside Meal Preparation with no edge to carry it.**

*Evidence.* Event 10 produces 💼 `Meal plan`. **No event on the board reads it** —
every kitchen event reads `Recipe`. Meanwhile the map's Meal Preparation node
displays a green `Menu`, which no board event in that bubble consults, and there
is **no edge between Meal Planning and Meal Preparation at all**.

*Why it matters.* The map is being honest about the board's silence by drawing
no edge, and then quietly contradicting itself by placing a read model on the
downstream side. Whichever way that resolves, the biggest phase boundary in the
domain currently has nothing crossing it on either artifact.

*Recommendation — back to the wall.* Three candidate answers, and the team knows
which: **(a)** cooking really does start from the plan and the read model was
never drawn; **(b)** a **Shopping / mise-en-place** context is missing between
them, which is where a settled plan actually gets consumed; or **(c)** the meal
plan is a report and nothing downstream needs it. *What settles it: what happens
between settling a plan and standing at the stove?*

---

**F11 · `EDGE` — `Cook` is written once and used in every context, and no map
edge carries it.**

*Evidence.* 💼 `Cook` is produced at event 1. A yellow `Cook` actor sticky sits
on 19 of the remaining 20 events, in every bubble. Cook Profile's only edge on
the map (E2) carries `Consent`.

*Why it matters.* One writer and readers everywhere is the textbook Open Host
Service, and it is the cheapest edge on the map to be missing — every context
authorises against it.

*Recommendation — map moves.* Draw Cook Profile as an upstream to every node, or
mark it as a cross-cutting band rather than a peer node. Also note `User` 👁 is
consumed at event 1 and never appears again: there is an identity source
upstream of Cook Profile that neither artifact draws.

---

**F12 · `PAT` — every border but the ACL is labelled `CF`.** *(One finding about
the map, not twelve about its edges.)*

*Evidence.* Eleven of twelve edges carry `CF`; no `U`/`D` markers anywhere.

*Three specific problems, whichever way the UNCHECKED question in §0 resolves.*

- **Conformist in both directions.** E3/E4 and E9/E10 are mutual pairs. Two
  contexts each conforming to the other is not Conformist; the board shows
  crossings both ways, which is Partnership or a boundary defect.
- **Conformist is ruled out where the downstream writes its own model.** Media →
  Cooking Assistance and Media → Sharing carry `Pictures`, and the two
  downstreams demonstrably model it differently (**F13**). Conformist means
  adopting the upstream's model wholesale; here neither does.
- **The boxes sit on the upstream border.** If `CF` means Conformist, the label
  belongs on the downstream side. Five of them are drawn on what the payload
  evidence makes the supplier.

*Recommendation — ask, then map moves.* Tell me what `CF` stands for on this
map. If it is Conformist, the labels need re-deciding per edge and the
directions need `U`/`D` markers; if it is something else — a published interface,
a translation point — the legend needs one line, and most of this finding
evaporates.

---

**F13 · `TERM` — `Pictures` carries two models across two borders and the map
says nothing.**

*Evidence.* `Pictures` is produced twice by the same command: at event **14**,
read one event later by `Help requested` (15) alongside `Catastrophe` — a
diagnostic photograph of a disaster, private, short-lived, one reader; and at
event **20**, read by `Thanks given` (21) — a finished-meal shot, flattering,
indefinite, public. E8 and E12 carry the identical payload word.

*Why it matters.* This is exactly the collision a border exists to record, and
it will be implemented as one type with a nullable field. It is also where F4's
consent question actually bites: the two senses have different audiences.

*Recommendation — map moves.* Split the payload names on E8 and E12 —
`Catastrophe Pictures` on the help edge, `Pictures` on the sharing edge. The
board already hints at it: event 14 is taken in the presence of `Catastrophe`
and event 20 reads nothing at all.

---

**F14 · `CONTRACT` — the inbound help edges name `Help request`, and the board
shows the *situation* crossing.**

*Evidence.* E3 and E10 both carry `Help request` inward. But `Help request` 💼 is
**produced inside** the help context (events 7, 15). What actually crosses from
the requesting contexts is what those events consult: `Ingredients` and `Menu`
from planning (7), and `Catastrophe`, `Recipe`, `Pictures` from the kitchen (15).
The map's own Cooking Assistance node lists all four as read models and draws an
edge for only one of them (Pictures).

*Why it matters.* It makes the help interface look narrow when it is the widest
one on the board. This is the interface-width decision: either the request
carries a **snapshot** taken at request time, or responders **reach back** for
live detail — and the second pulls all three contexts back together.

*Recommendation — map moves + back to the wall.* Name the situation on E3/E10 at
business resolution ("what is being made, what went wrong, evidence"). And take
`Catastrophe` to the wall: it is read at 13 and 15 and **written by nothing**,
which usually means it belongs to an aggregate the kitchen has not named yet.

---

**F16 · `STALE` — the human help borders are drawn synchronous.**

*Evidence.* E3, E4, E9, E10 are solid (`synchronous`) per the map's legend. The
board's responders on those edges are `Community Cook` and `Chef` — human
actors, in a different kitchen, who have not been notified yet (the notifying
policy is undrawn, F17). The two edges the map *does* draw async are the ones to
the machine and to Notification.

*Why it matters.* This is the one severity-raising case in the mechanism check:
an edge labelled synchronous across a crossing the domain treats as a wait will
be built as a blocking call. A cook does not stand at the hob holding an open
request handle while a stranger sleeps.

*Recommendation — map moves.* Make the human help edges asynchronous. Then ask
the question the split in §5 depends on: **how long may a request go unanswered
at the stove, versus at the table?** That number is the only thing that would
justify two help contexts, and it is nowhere on either artifact.

### Minor

---

**F8 · `OWN` — `Ingredients` is written on both sides.** The map's Recipe Catalog
holds 💼 `Ingredients`; the board's event 9 `Ingredients substituted` also
produces 💼 `Ingredients` in Meal Planning. Applying the copy guard, these are
probably two things wearing one word — a catalogue's canonical ingredient list,
and a plan's own post-substitution shopping list. *Ask which, then rename one.*
If they really are one term with two writers, this is contested ownership and
escalates to blocking.

---

**F15 · `TERM` — names lost and mis-spelled at the border.** The board's
`Cooking Help` bubble name does not survive the collapse into `Cooking
Assistance` — correct as a model decision, invisible as a note, and a reader
looking for their own bubble will not find it. Also: map `Meal plan setteled` vs
board `plan settled`; map `Ingredietens` vs board `Ingredients`; map `Grandma
Avatar AI` vs board `Grandma Avatar`. *Add "collapsed from Cooking Assistance ×2
+ Cooking Help ×2" under the node name; fix three spellings while passing.*

---

**F17 · `GAP` — four missing policies and an empty hotspot column presented as a
settled picture.** The board shows `Ingredients missing` (5), `Meal planning
stalled` (6), `Step unclear` (12) and `Catastrophe happened` (13) sitting beside
`Request help` (7, 15) with **nothing between them** — no policy, no lilac
sticky, nothing that turns trouble into an offer of help. The map draws confident
edges over all four. Separately: **no red stickies anywhere**, on a board whose
own vocabulary is *stalled*, *unclear* and *catastrophe*, and which draws one
capability twice under two different names. That reads as "disagreement was
never captured", not "nobody disagreed". *Back to the wall, both.*

---

**F18 · `GAP` — `Thanks` is an orphan on both artifacts.** Event 21 produces 💼
`Thanks`; no event reads it, and the map gives Sharing no outgoing edge. The
reciprocity loop — thanks flowing back to whoever helped — terminates in a
business object nobody consumes. *One question at the wall: who sees a thank-you,
and does it change anything about the person who receives it?*

---

## 7. Undrawn edges and missing nodes

The reverse sweep — crossings the board shows that the map does not draw, and
what neither drew.

| Crossing on the board | Evidence | On the map |
|---|---|---|
| Cook Profile → every context | 💼 Cook (1) · Cook actor on 19 events | **absent** (F11) |
| Recipe Catalog → Meal Preparation | 👁 Recipe at 11, 12, 13, 19 | **absent** (F7) |
| Recipe Catalog → Cooking Assistance | 👁 Recipe at 15 | **absent** (F7) |
| Cooking Assistance → Sharing | 👁 Help provider at 21 | **absent** (F3) |
| Meal Preparation → Sharing, on `Meal prepared` | event 19 | drawn, mislabelled (F2, F6) |
| Meal Planning → Meal Preparation | *no board evidence* | correctly absent — but see the stray 👁 Menu (F10) |
| Meal Planning → Cooking Assistance | 👁 Ingredients, Menu at 7 | drawn, payload imprecise (F14) |
| Meal Preparation → Cooking Assistance | 👁 Catastrophe, Recipe, Pictures at 15 | drawn, payload imprecise (F14) |

**Missing nodes neither artifact drew:**

- **An identity source upstream of Cook Profile.** 👁 `User` is consumed at event
  1 and never appears again. Somebody produces a User.
- **Whatever owns `Catastrophe`.** Read by two contexts, written by none — but
  unlike `Recipe`, this does not look off-board. It looks like the aggregate the
  kitchen has not named. Three prior analyses in this project independently
  reached the same conclusion, and it remains the highest-value single change to
  the board: **Meal Preparation writes no business object at all** across seven
  events, while `Step unclear`, `Step competeted`, `Catastrophe happened` and
  `Meal rescued` all presuppose something with a state.
- **A Shopping / mise-en-place context**, if F10 resolves that way.

---

## 8. Allowed divergence — what I deliberately did not report

- **`Grandma Avatar AI` drawn as an ordinary bounded context.** A pink sticky is
  a separate model outside the team's control, which is what a bounded context
  is. Drawing it as a node is correct and how it is drawn — marked or unmarked,
  named after the persona — is never a finding here. The only thing reported is
  the missing ownership sentence (F9).
- **Team ownership, org structure, who negotiates with whom.** A board cannot
  express any of it.
- **Relationship patterns where the board shows nothing either way.** Patterns
  are decisions. F12 is raised only where the board actively contradicts the
  label or where the label is applied uniformly enough to mean nothing.
- **Missing staleness windows and integration mechanisms.** Nobody supplied a
  number. F16 is raised only because the board shows the clock — human
  responders on a synchronous edge.
- **Contract payloads at business resolution.** "Pictures", "the situation" —
  a contract is not a schema, and I did not demand field-level correspondence.
- **`Recipe Catalog` as a node with no bubble.** That is the map doing its job:
  an unwritten noun with readers in three contexts is precisely the off-board
  upstream a map is supposed to add.
- **Layout and aesthetics.** The map does not reproduce the board's timeline —
  its nodes are arranged by dependency, not by phase — so the layout is not the
  finding here.
- **Everything under UNCHECKED in §0.** The `CF` boxes are *unchecked*, not
  absent; F12 is written so it survives either reading.

---

## 9. Patch list

**Edits to the map** — apply without further discussion:

1. **Relabel E11.** `Meal Preparation → Sharing` carries the finished meal
   (identity · recipe ref · finished-at), not `Help response`. *(F2)*
2. **Add `Cooking Assistance → Sharing`** carrying `Help provider`. *(F3)*
3. **Fold Meal Preparation appearance ② into the node**: `Step competeted`,
   `Meal rescued` 🤖, `Meal prepared`. *(F6)*
4. **Add `Recipe Catalog → Meal Preparation` and `Recipe Catalog → Cooking
   Assistance`.** *(F7)*
5. **Add `Cook Profile → *`**, or redraw Cook Profile as a cross-cutting band.
   *(F11)*
6. **Write one sentence of ownership on the ACL:** Cooking Assistance owns
   `Help request` and `Help response`; the avatar supplies content through the
   translation. Drop the duplicated events from the avatar node. *(F9)*
7. **Split the `Pictures` payload:** `Catastrophe Pictures` on E8,
   `Pictures` on E12. *(F13)*
8. **Make E3, E4, E9, E10 asynchronous.** *(F16)*
9. **Name the situation on E3 and E10** instead of `Help request`. *(F14)*
10. **Annotate Cooking Assistance** — "collapsed from Cooking Assistance ×2 +
    Cooking Help ×2" — and fix `setteled`, `Ingredietens`. *(F15)*
11. **Mark `Notification` and `Grandma Avatar AI` with their source artifact**,
    or remove them pending F1. *(F1, F5)*
12. **Add a legend line for `CF`**, and `U`/`D` markers on every edge. *(F12)*

**Back to the wall** — roughly an hour, and it settles five findings:

1. **Name the aggregate in Meal Preparation.** Seven events, zero business
   objects, four events that presuppose a state. Everything else on this list is
   cheaper than this one and worth less.
2. **Draw the consumer of `Meal plan`** — or accept that Shopping is missing, or
   that the plan is a report. *(F10)*
3. **Draw the four missing policies** between the trouble events and
   `Request help`. *(F17)*
4. **Draw where `Help provider` and `Catastrophe` are written.** *(F3, F14)*
5. **Draw the consent path**, if it is real. *(F4)*
6. **Put state stickies on `Help request`, `Meal plan` and the unnamed
   preparation aggregate.** The board has none, anywhere.
7. **Ask the room for the two numbers**: how long a help request may go
   unanswered *at the stove* versus *at the table* (F16, and the only thing that
   would justify splitting the help context); and whether `Ingredients` on the
   catalogue and `Ingredients` on the plan are the same term (F8).

If the map takes more than roughly a third of these edits at once — it currently
takes twelve on twelve edges — redrawing beats patching. That is
`event-storming-context-mapper`, not this skill.

---

## 10. Already consistent — protect these

- **The four help appearances are one node.** The board draws `Cooking
  Assistance` twice and `Cooking Help` twice; the map draws `Cooking Assistance`
  once. This is the single most commonly failed check in this skill, the failure
  hides the context everything else bends around, and this map got it right. It
  is also independently confirmed by two prior analyses in this project.
- **Meal Planning's two appearances and Media's two appearances are likewise one
  node each**, with all events accounted for.
- **`Recipe Catalog` was correctly promoted to an off-board upstream** — an
  unwritten noun with readers in three contexts is exactly what should become a
  node, and it was.
- **E4 `Cooking Assistance → Meal Planning` (Help response)** and **E9
  `Cooking Assistance → Meal Preparation` (Help response)** are correctly
  directed against the producer evidence at events 8/16 → 9/10 and 17/18.
- **E8 and E12** — Media as the writer of `Pictures` supplying both the help
  context (event 15) and Sharing (event 21) — are correctly directed and
  correctly sourced. Media is drawn with **no inbound edge**, which matches the
  board exactly: `Take pictures` reads nothing.
- **The ACL on the Grandma Avatar.** An anticorruption layer around an AI
  persona standing among human responders is the same treatment two independent
  analyses in this project recommended, and it is on the map already.
- **No sticky on the board sits in two bubbles or outside every bubble**, and
  the map's cut agrees with the board's cut everywhere it overlaps it. The
  disagreements in §6 are all about nodes the board never drew, edges nobody
  drew, and one reversed arrow — not about where the boundaries run.

---

*This is a conformance check, not a critique of either artifact. If you want the
map's own quality challenged — whether these are the right boundaries at all —
that is `event-storming-context-mapper` in review mode. And a context with four
appearances on the board that the map draws as one ordinary node is a candidate
core domain worth taking to `core-domain-chart-critic`.*