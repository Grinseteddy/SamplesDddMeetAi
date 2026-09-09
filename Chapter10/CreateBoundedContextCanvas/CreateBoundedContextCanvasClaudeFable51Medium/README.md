# Bounded Context Canvases — Community Cooking

Ten canvases, one per bubble on the context map, derived from **ContextMap.jpg**
(node names, borders, what crosses) and **VisualGlossaryEnhanced.jpg** (the
agreed vocabulary, partitioned by owner), with business decisions from the
invariants sheet and purpose lines from the pivotal-event and story cuts already
in this project. This index is not a canvas: it holds the roster, the message
ledger, the glossary partition and the reconciliation report, which belong to
the set rather than to any one file.

Every field carries a provenance marker — `(given)` stated by a source,
`(derived)` read off one, `(proposed)` mine, `(unknown)` nothing to derive from.
A box left grey is a to-do, not an oversight.

## 1. Map as read

Ten bubbles. Orange = domain event, pale yellow 💼 = business object, green 👁 =
read model. Border boxes: **OHS** ×8, **ACL** ×1 (on Grandma Avatar AI), **CF**
×1 (on Sharing). Solid arrows are synchronous, dashed asynchronous (legend on
the map). Loose stickies beside an arrow are read as what crosses.

| Bubble | Events | Business objects | Read models |
|---|---|---|---|
| Recipe Catalog | — | Ingredients · Recipe | — |
| Cook Profile | Cook registered | Cook | — |
| Meal Planning | Dinner planned · Recipes searched · Recipe selected · Meal plan setteled *(sic)* · Ingredients substituted · Ingredients missing · Meal planning stalled 🤖 | Menu · Ingredients | Recipe |
| Grandma Avatar AI | Help requested · Help provided | Help response · Help request | — |
| Notification | — | Notification | — |
| Cooking Assistance | Help requested · Help provided | Help response · Help request | Menu · Ingredietens *(sic)* · Recipe · Pictures |
| Media | Pictures taken | Pictures | — |
| Consent Management | — | Consent | — |
| Meal Preparation | Meal preparation started · Catastrophe happened · Step unclear | **none** | Recipe · Menu |
| Sharing | Thanks given | Thanks | — |

**Reading notes, recorded before deriving anything:**

- **The pattern boxes are not where the patterns say they should be.** An OHS
  sits on the *supplier's* edge for Recipe Catalog, Cook Profile and Meal
  Planning (→ Cooking Assistance); on the *receiver's* edge for Notification,
  Cooking Assistance (← Media), Meal Preparation (← Meal Planning) and Sharing
  (× 2); and one box serves two opposite arrows on each of the Meal Planning ↔
  Cooking Assistance and Meal Preparation ↔ Cooking Assistance pairs. The ACL is
  on the AI's edge, where the prior analyses had it on the help context's. Only
  the CF is unambiguous. Read as **labels placed at the arrowhead**, not as a
  statement about who hosts; every canvas records this as an assumption.
- **Every synchronous arrow ends at a context holding a green read model of the
  object on the arrow** (or, for *Help response* and *Consent*, at a context
  whose board events read it). So sync crossings are read as **queries issued by
  the arrowhead side**; dashed crossings as **events**. One rule, applied to all
  nineteen messages — overturn it once and every canvas moves together.
- **Two bubbles are the same shape.** Grandma Avatar AI duplicates Cooking
  Assistance's two events and two objects behind an ACL. Read as a translated
  copy, and flagged as contested ownership.
- **Three bubbles have no events** (Recipe Catalog, Notification, Consent
  Management) and one has **no business object** (Meal Preparation). Both are
  carried as findings, not filled in.
- Spellings kept verbatim: *setteled*, *Ingredietens*, *Notifi cation*.

## 2. Roster

| Context | File | Events | Terms owned (glossary) | Notes |
|---|---|---|---|---|
| Cooking Assistance | `cooking-assistance.canvas.md` | 2 | Help Request (+4 kinds) · Help (+4 contents) · Menu proposal | the board's *Cooking Assistance* and *Cooking Help*, correctly drawn once |
| Meal Planning | `meal-planning.canvas.md` | 7 | Dinner · Menu · Course · Substitute | |
| Meal Preparation | `meal-preparation.canvas.md` | 3 | **none** | the only canvas with no solid term |
| Recipe Catalog | `recipe-catalog.canvas.md` | 0 | Recipe · Ingredient · Step | supplier; no lifecycle |
| Cook Profile | `cook-profile.canvas.md` | 1 | Cook | *Cook* crosses no border |
| Grandma Avatar AI | `grandma-avatar-ai.canvas.md` | 2 | Grandma Avatar | prior analyses put it inside Cooking Assistance |
| Media | `media.canvas.md` | 1 | Picture | one word, two senses |
| Sharing | `sharing.canvas.md` | 1 | Thanks | no inbound message |
| Consent Management | `consent-management.canvas.md` | 0 | — (*Consent* is not a glossary term) | new since every prior artifact |
| Notification | `notification.canvas.md` | 0 | — (*Notification* is not a glossary term) | events in, nobody to tell |

No off-board collaborator is drawn on the map, so none gets a canvas. The
**Community** and **Chef** that the glossary names as help providers, and the
guests that three invariants read, are on no bubble and no arrow.

## 3. Message ledger

One row per crossing. *Emitter* is who sends the message — for a `qry` that is
the context asking, which is the **arrowhead** side of the map; for an `evt` it
is the arrow's tail. Each row is exactly two canvas entries: outbound on the
emitter, inbound on the handler.

| # | Emitter (outbound) | Handler (inbound) | Message | Type | Mechanism | Evidence |
|---|---|---|---|---|---|---|
| 1 | Meal Planning | Recipe Catalog | Recipe | `qry` | sync, Recipe Catalog OHS | yellow Recipe sticky on the arrow; green Recipe read model inside Meal Planning |
| 2 | Cook Profile | Consent Management | Consent | `?` | sync, Cook Profile OHS | yellow Consent sticky beside the arrow -- the only thing Cook Profile emits, and it is not Cook |
| 3 | Cooking Assistance | Meal Planning | Menu | `qry` | sync, Meal Planning OHS | yellow Menu sticky on the downward arrow; green Menu read model inside Cooking Assistance |
| 4 | Cooking Assistance | Meal Planning | Ingredients | `qry` | sync, Meal Planning OHS | yellow Ingredients sticky on the downward arrow; green read model spelled Ingredietens inside Cooking Assistance |
| 5 | Cooking Assistance | Meal Planning | Recipe | `qry` | sync, Meal Planning OHS | green Recipe sticky on the downward arrow (a read model, not a business object -- Meal Planning passes Recipe Catalog's Recipe through) |
| 6 | Meal Planning | Cooking Assistance | Help response | `qry` | sync, upward arrow into Meal Planning's OHS | yellow Help response sticky on the upward arrow |
| 7 | Meal Preparation | Meal Planning | Recipe | `qry` | sync, into Meal Preparation's left OHS | yellow Recipe sticky on the long arrow from Meal Planning; green Recipe read model inside Meal Preparation |
| 8 | Meal Preparation | Meal Planning | Menu | `qry` | sync, into Meal Preparation's left OHS | yellow Menu sticky on the same arrow; green Menu read model inside Meal Preparation |
| 9 | Meal Preparation | Cooking Assistance | Help response | `qry` | sync, downward arrow into Meal Preparation's top OHS | yellow Help response sticky on the downward arrow |
| 10 | Cooking Assistance | Meal Preparation | Menu | `qry` | sync, upward arrow from Meal Preparation's top OHS | green Menu sticky on the upward arrow |
| 11 | Cooking Assistance | Meal Preparation | Recipe | `qry` | sync, upward arrow from Meal Preparation's top OHS | green Recipe sticky on the upward arrow |
| 12 | Cooking Assistance | Media | Pictures | `qry` | sync, into Cooking Assistance's right OHS | yellow Pictures sticky on the arrow from Media; green Pictures read model inside Cooking Assistance |
| 13 | Cooking Assistance | Notification | Help request | `evt` | async (dashed), into Notification's OHS | yellow Help request sticky beside the dashed arrow; corresponds to the Help requested event |
| 14 | Cooking Assistance | Notification | Help response | `evt` | async (dashed), into Notification's OHS | yellow Help response sticky beside the dashed arrow; corresponds to the Help provided event |
| 15 | Cooking Assistance | Grandma Avatar AI | Help request | `evt` | async (dashed), into Grandma Avatar AI's ACL | yellow Help request sticky beside the rising dashed arrow |
| 16 | Grandma Avatar AI | Cooking Assistance | Help response | `evt` | async (dashed), out of Grandma Avatar AI's ACL | yellow Help response sticky beside the descending dashed arrow |
| 17 | Sharing | Media | Pictures | `qry` | sync, into Sharing's top OHS | yellow Pictures sticky on the arrow from Media |
| 18 | Sharing | Meal Preparation | Help response | `qry` | sync, into Sharing's left OHS | yellow Help response sticky on the arrow from Meal Preparation -- an object Meal Preparation never writes |
| 19 | Sharing | Consent Management | Consent | `qry` | sync, into Sharing's CF box | yellow Consent sticky on the long arrow from Consent Management; CF = Sharing conforms |

**Nineteen messages: 14 queries, 4 events, 1 `?`.** That is the ledger's first
finding, and it shapes every canvas: this is a **pull architecture**. Only
Cooking Assistance publishes anything, and only to Notification and the avatar.
No pivotal event of any other context crosses a border — *Meal plan setteled*,
*Meal preparation started*, *Pictures taken*, *Thanks given* and *Cook
registered* are all internal. Every downstream context has to already know when
to ask.

**Typing calls worth showing the working for:**

- ***Recipe* into Cooking Assistance is drawn green** (a read model) where
  *Menu* and *Ingredients* beside it are yellow (business objects). The map is
  saying Meal Planning does not own what it serves — Recipe Catalog does.
- ***Help response* from Meal Preparation to Sharing** is a query for an object
  Meal Preparation never writes and does not even hold as a read model. Typed
  `qry` by the rule; flagged on both canvases as a probable misdrawn edge.
- ***Consent* from Cook Profile** is the one `?`. A sync arrow from an OHS
  carrying an object Consent Management owns and Cook Profile does not — a push
  of a consent given at registration, or a mislabelled *Cook*. Both canvases
  ask.
- The dashed stickies name **objects** (*Help request*, *Help response*); they
  are read as the events *Help requested* / *Help provided* carrying those
  objects, and named as the map names them so the pairs match.

## 4. Glossary as read, and partitioned

The Visual Glossary carries **26 terms** in four colours: yellow (most), blue ⊞
(Dinner, Menu, Course), green ⊞ (Help provider, Chef, Menu proposal, Help Meal
plan) and one grey (Meal). Read as bounded-context colours in the glossary's own
notation, with ⊞ marking a term detailed elsewhere; the glossary labels none of
them, so the mapping to bubbles below is `(derived)` from the map's term ledger
— the context that *writes* a term owns it.

| Glossary term | Owner (writer, per the map) | Appears on canvases |
|---|---|---|
| Cook | Cook Profile | Cook Profile (owned); Consent Management (borrowed) |
| Dinner · Menu · Course | Meal Planning — blue | Meal Planning (owned) |
| Meal | **nobody** — grey in the glossary; map has *Meal plan* and *Meal preparation* | Meal Planning, Meal Preparation, Cooking Assistance (all dashed) |
| Recipe · Ingredient · Step | Recipe Catalog (Step by elimination) | Recipe Catalog (owned); Meal Planning, Meal Preparation (borrowed); *Ingredient* **contested** with Meal Planning |
| Substitute | Meal Planning (*Ingredients substituted*) | Meal Planning (owned) |
| Help Request · Meal Preparation Catastrophe · Help for Meal Preparation Step · Help with Ingredients · Help Meal plan | Cooking Assistance — **contested** by Grandma Avatar AI | Cooking Assistance (owned); Grandma Avatar AI, Media, Notification (borrowed) |
| Help · Ingredient Substitute · Preparation Step Explanation · Steps to Mitigate Catastrophe · Menu proposal | Cooking Assistance — **contested** by Grandma Avatar AI | Cooking Assistance (owned); Meal Preparation, Sharing, Media, Notification (borrowed) |
| Picture | Media | Media (owned); Cooking Assistance, Sharing, Consent Management (borrowed) |
| Thanks | Sharing | Sharing (owned); Media (borrowed) |
| Grandma Avatar | Grandma Avatar AI | Grandma Avatar AI (owned); Sharing (borrowed) |
| Help provider · Community · Chef | **nobody** | dashed wherever they appear |

**What the partition finds:**

- ***Meal* is the glossary's own hotspot.** It is the only grey term, sits at
  the centre of the picture with five edges, and the map has no context called
  anything like it — *Meal plan* on one bubble, *Meal preparation* on another,
  and the story cut found the word carries a *described* sense in help and a
  *physical* sense in the kitchen. The glossary refusing to colour it is the
  right instinct; the canvases draw it dashed on all three contexts that touch
  it and ask.
- ***Community*, *Chef* and *Help provider* are defined and owned by nobody.**
  The glossary's three responders; the map's one. The whole community
  proposition — and Notification's reason to exist — sits on terms with no
  context.
- ***Help Request* / *Help* is the largest cluster in the glossary** (ten
  terms, all yellow) and every one of them belongs to Cooking Assistance —
  which is the glossary agreeing with the pivotal-event re-run that assistance
  is the subject, not the setting. Not one of the eight specialisations
  (four request kinds, four answer contents) appears on the map.
- **The glossary and the map disagree on five names:** *Help Request* /
  *Help request*; *Help* / *Help response*; *Picture* / *Pictures*;
  *Ingredient* / *Ingredients* / *Ingredietens*; and the glossary has no
  *Meal plan*, no *Consent*, no *Notification*, no *Catastrophe*, no *Guests*,
  no *User*. Patch list for the wall, both walls.
- **One glossary edge is broken.** A dangling `◀ belongs to` arrowhead points
  left from *Ingredient Substitute* toward *Help with Ingredients* with no line
  between them. Read, by symmetry with the other three content → request-kind
  edges, as `Ingredient Substitute belongs to 1 Help with Ingredients`; recorded
  as an assumption on the Cooking Assistance canvas.
- **A Chef cannot be thanked.** `Thanks to 0..1 Community`, `to 0..1 Grandma
  Avatar`, and no edge to Chef — though Chef `provides 0..* Help`. Omission or
  rule; nobody has said.

**Cardinalities that are business decisions**, carried onto the canvases marked
`(given)`: `Menu has 1..* Course` and `Course contains 1..* Meal` (a plan cannot
settle empty — `INV-PLAN-03` in another notation); `Help for 1 Help Request`
(`INV-HELP-05`); `Thanks for 1 Help` (`INV-SHARE-03`); `Meal with 1 Recipe`
(a meal is one recipe — nobody has agreed to that); `0..10 Picture` on request,
answer and thanks; `Help contains 1..3 / 1 / 1..10 / 1..3` (an answer with *no*
substitute is refused by this model).

## 5. Reconciliation report

`check_canvases.py`: 10 canvases, **19 message pairings matched, 0 errors, 0
warnings**. Every canvas also parses in the real Mermaid engine.

Symmetry is clean because the ledger was built once and both sides were
generated from it. What the set exposes is everything symmetry cannot see:

| Finding | Where | Kind |
|---|---|---|
| **Nothing tells Cooking Assistance a cook is stuck.** No *Ingredients missing*, *Meal planning stalled*, *Step unclear* or *Catastrophe happened* crosses a border; the help context's only inbound messages are pulls for its own *Help response*. | Cooking Assistance, Meal Planning, Meal Preparation | **missing edges** — the four policies every prior analysis flagged |
| ***Cook* crosses no border.** Cook Profile emits only a `?` *Consent*; nobody queries it for who a cook is. | Cook Profile | **missing edges** (several) |
| ***Help response* reaches Sharing from Meal Preparation**, which never writes it, instead of from Cooking Assistance, which owns it and the *Help provider* that `INV-SHARE-01` needs. | Sharing, Meal Preparation | **misread producer** (probably) |
| **Sharing has no inbound message** and the Meal Preparation bubble has no ending event (*Step completed*, *Meal rescued*, *Meal prepared* are on the board, not the map). *Thanks given* has no trigger. | Sharing, Meal Preparation | **missing edge / missing events** |
| ***Help request* and *Help response* are written by two bubbles** — Cooking Assistance and Grandma Avatar AI, identical events and objects, an ACL between them. | Cooking Assistance, Grandma Avatar AI | **contested ownership** |
| ***Ingredients* is written by two bubbles** — Recipe Catalog and Meal Planning — and the glossary hangs *Ingredient* under Recipe. | Recipe Catalog, Meal Planning | **contested ownership** |
| ***Recipe* takes three paths**: Recipe Catalog → Meal Planning → Cooking Assistance, and → Meal Preparation → Cooking Assistance again. Two read paths for one object into one context. | Cooking Assistance | **duplicate edge** |
| **Media never consults Consent Management**, though it holds the kitchen photos that go to strangers; only Sharing conforms to consent. | Media, Consent Management | **missing edge** (`INV-MEDIA-03` has no home) |
| **Five contexts emit nothing** (Recipe Catalog, Notification, Consent Management, Media, Meal Preparation) and **three own no rule** (Recipe Catalog beyond two cardinalities, Notification, Consent Management). Three of those have no events. | — | **component or context?** — asked on each canvas |
| **Pattern boxes at the arrowhead.** Eight OHS boxes, five on the receiving side; ACL on the AI's side. | whole map | **notation** — fix the wall |
| **One `?` message**: *Consent* from Cook Profile. | Cook Profile, Consent Management | **untyped** |

**Fields `(unknown)` across the set:** *Verification metrics* on all ten
(nothing supplies any; a `(proposed)` candidate is given on each). *Strategic
classification* on all ten — no Core Domain Chart exists; three canvases carry
a prior analysis's hypothesis marked `(proposed)` (Cooking Assistance core,
Meal Preparation supporting, Recipe Catalog generic), and the rest are grey.
*Ubiquitous language* is `(given)` on seven canvases and `(derived)` on three
(Meal Preparation, Consent Management, Notification), because the glossary
defines nothing they write.

## 6. Where the map departs from the prior cuts

Worth having in the room, because the canvases follow the map and the map
overrules three findings the project had converged on:

| Prior finding (two or three analyses agreed) | The map | Canvases do |
|---|---|---|
| The Grandma Avatar is a responder *inside* the help context, wrapped by an ACL the help context owns | a separate context, with the ACL on **its** edge | follow the map; carry the disagreement on both canvases |
| *Sharing* / *Thanks given* belongs inside the help context | a separate context | follow the map; note nothing is inbound |
| *Pictures* is one word for two concepts and should be split | one bubble, one word | draw *Catastrophe Pictures* dashed as undefined |
| Recipe Catalogue is off-board, conformist | an on-map bubble with an OHS and no events | canvas it; ask whether it is a context |
| Cooking Assistance and Cooking Help are one context drawn twice | **one bubble** | agree — the one place the map fixed the board |
| Meal Preparation has no aggregate | still none | the only canvas with no solid term |

## 7. The three questions that would fill the most boxes

1. **What is the grey *Meal*, and what is the cook holding while cooking?** One
   answer names the unclaimed term on three canvases and the missing aggregate
   under ten business decisions.
2. **Does a help request carry a snapshot, or do responders read live — and how
   does the help context learn a cook is stuck?** One answer settles six
   outbound queries, four missing policies and the empty inbound column of the
   context the product is built around.
3. **Which context does the community live in?** *Community*, *Chef* and *Help
   provider* are owned by nobody; Notification has nobody to tell; *Cook*
   crosses no border; a Chef cannot be thanked. One answer fills gaps on six
   canvases.

## Rendering note — why the config block differs from the template

The first version of this set was unreadable when a viewer fit it to page
width: 14px type in a ~2400×1900 px diagram, and — rendered in a real browser —
the template's pinned font never applied (Mermaid 11 quotes the whole
comma-separated list as one family name, which resolves to nothing, so the
diagram inherits whatever the page uses; inside a `<pre>` that is monospace and
every label clips at the right edge).

Every canvas here was therefore rendered in Chromium and looked at, and the
frontmatter carries what actually worked, in the same block the template
prescribes:

| Key | Template | Here | Why |
|---|---|---|---|
| `fontFamily` | a comma list | `Arial` | a **single** family name is the only form Mermaid applies; Arial resolves on Windows, macOS and (via Liberation Sans) most Linux |
| `fontSize` | 14px | 22px | type must survive fit-to-width scaling of a ~2400 px diagram |
| `rankSpacing` / `nodeSpacing` | 55 / 45 | 30 / 30 | removes the empty vertical space that made the diagram tall |
| `wrappingWidth` | 280 | 380 | fewer wrapped lines per panel at the larger size |
| `padding` | 16 | 12 | headroom is still there; the measured font now matches the rendered one |

The PNGs in `renders/` are those Chromium renders, one per canvas, for checking
legibility without a Mermaid viewer. If a canvas still clips in a particular
tool, that tool is overriding the font; change `fontFamily` to that tool's
font — one name — rather than shortening labels.

## Files

```
bounded-context-canvases/
├── README.md                          # this file — roster, ledger, partition, reconciliation
├── cooking-assistance.canvas.md
├── meal-planning.canvas.md
├── meal-preparation.canvas.md
├── recipe-catalog.canvas.md
├── cook-profile.canvas.md
├── grandma-avatar-ai.canvas.md
├── media.canvas.md
├── sharing.canvas.md
├── consent-management.canvas.md
├── notification.canvas.md
└── renders/                           # Chromium renders of each canvas, for legibility checks
```

Re-check after any edit: `python check_canvases.py bounded-context-canvases/`.
