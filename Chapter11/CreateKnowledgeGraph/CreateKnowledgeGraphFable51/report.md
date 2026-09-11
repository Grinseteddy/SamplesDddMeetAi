# Larder — knowledge graph, seed ingest of eight artifacts (2026-09-10)

**Mode: seed.** No graph existed. Base IRI `https://w3id.org/dkg/graph/larder#`,
taken from the Impact Map's own goal sticky — change it in the prologue if the
project has another name. Files: `graph.ttl` (source of truth), `dkg.ttl`
(vocabulary), `views/index.md` (register + five lenses + orphan report).

Ingested in the order given: brainstorm → Impact Map → Wardley Map → Business
Model Canvas → Capability Map → five Domain Stories (one artifact each) →
EventStorming board → Visual Glossary. `ContextMap.jpg` was uploaded but not in
the list and was **not** ingested; the six analysis documents in the project
were not ingested either (they would enter as `dkg:Inferred` only — say the
word).

---

## 1. How corroborated the graph is

**358 of 384 nodes rest on a single artifact.** 11 nodes have two sources, 3
have three, 7 have four, 3 have five, 2 have six. The multi-source spine is:
`Cook` (6), `Pictures` (6), `Meal` (5), `Help` (5), `Community` (5),
`Grandma Avatar`, `Chef`, `Recipe`, `Ingredients`, `Thanks`, `Help Request`,
`Menu`, `Dinner`, `Help provider`, plus the verbs `prepares`, `takes`, `asks`,
`provides`, `thanks`, `shares`. Everything else is one artifact's opinion, and
that includes every strategy node: **no goal, impact, deliverable, value
proposition or capability is corroborated by a second artifact** — they are
linked only by 150-odd `proposedSameAs` edges that the room has not confirmed.

Confidence: 376 on-artifact, 4 implied, 4 inferred (the Wardley stages that sit
on a band line).

## 2. What went in

384 nodes, ~3,700 triples. Sentence 44 · Idea 39 · Relationship 35 · Actor 28 ·
Term 25 · Component 24 · Capability 21 · DomainEvent 18 · Question 18 ·
Activity 17 · WorkObject 17 · Deliverable 16 · Impact 14 · Command 11 ·
Cluster 11 · ReadModel 11 · BusinessObject 8 · UserNeed 7 · BoundedContext 7 ·
BMC blocks 26 · Goal 1.

Per artifact: brainstorm 51 · impact map 42 · Wardley 37 · BMC 30 · capability
map 40 · stories 28/28/24/34/28 · board 63 · glossary 66. **No single artifact
dominates**; the board is 16 % of the graph.

## 3. Merge ledger

Auto-merges (exact label, same spine class, no contradicting property):

| Label | Merged into | Sources now | If wrong |
|---|---|---|---|
| Cook (story actor ×5, board actor) | `:Act_Cook` | 6 | requester and registered user are one person — almost certainly right |
| Chef (story, board) | `:Act_Chef` | 3 | — |
| Grandma Avatar (story ×2, board) | `:Act_GrandmaAvatar` | 3 | kept the board's `ExternalSystem` type on the node — see contradiction 5 |
| Community (story ×4) | `:Act_Community` | 4 | responder and audience may be two things — Q raised |
| Meal, Pictures, Help, Dinner, Recipe, Ingredients (across stories) | one node each | 3–6 | Pictures/Help carry two senses inside one node — Q raised |
| Recipe, Ingredients (story → board read model / business object) | `:Con_Recipe`, `:Con_Ingredients` | +1 | term vs projection |
| Dinner, Menu, Cook (BO), Help provider, Help Request, Help, Thanks, Meal, Recipe (glossary → existing Concepts) | existing nodes, `dkg:Term` added | +1 | glossary *Help* is the response; story *Help* is both — Q raised |

Proposed, never merged — 150 edges, all in the file with a reason each. The
ones that matter most:

| Label seen | Candidate(s) | Why not auto | If wrong |
|---|---|---|---|
| Singles / Single / Singles (BMC) | three nodes | plural, and segment vs actor | the impact map and the canvas are talking about different people |
| Young families / Young family / Parents in Law | three nodes | plural; a guest is not a segment | — |
| Prospect / Propects | two nodes | misspelling | trivial |
| Chefs / Chef / Chefs as senior partner / Famous chefs partner / Famous chefs as partner / Ask chef | six nodes across five artifacts | one referent in six vocabularies, three spine classes | the partner and the responder are different chefs |
| Grandma Avatar / Grandma / Grandma / Grandpa / Grandma / Grandpa AI / Ask Grandma / Grandpa / Ask grandma / Grandpa shows how to do it | seven nodes | the stories say only *Grandma*; the strategy layer says *Grandma / Grandpa*; only the impact map says *AI* | the avatar is a persona the stories dropped Grandpa from, or Grandpa is a second persona |
| Community / Community Cook / Com-munity (BMC) / Community Engagement / Ask community | five nodes | audience, responder, relationship, capability, component | — |
| Cooking Support (cluster, key activity, capability L1, VP, Wardley need, board bubble Cooking Assistance) | six nodes | one phrase across six artifacts, six classes | nothing — it is the product's own name for itself |
| Cooking Assistance / Cooking Help (board bubbles) | two contexts | same commands, objects, responders; one photo already uses one name for all four | **two contexts with one aggregate each** |
| Pictures (board 14) / Pictures (board 20) / Catastrophe Pictures / Pictures / Picture | five nodes | one label, two lifetimes, two audiences | evidence photos become public posts |
| Help / Help Request / Help response | three nodes | the board split what the stories and glossary keep as one word | — |
| Planner / Planner (VP) / Planner (capability) / Dinner-party planner / Meal plan / Meal Planning | six nodes | cross-class throughout | — |
| Content Creation (cluster, key activity, capability) | three nodes | exact label, but the capability map marks it *core* and the canvas makes no claim | see contradiction 3 |
| Member Management / Member management ×2 | three nodes | both Wardley and capability map are `Capability`, but Wardley carries an evolution stage | safe to merge if the room agrees |
| Timer (idea, deliverable, component) | three | cross-class | the one idea that survived all the way to Commodity |

Brainstorm ideas with a proposed descendant: Cooking Club, Timer, Meal Chooser,
Cookbook publishing, Rating, Dietary requirements, Order ingredients directly,
Kitchen tools, Famous chefs as partner, Ask grandma, Grandpa shows how to do it,
Dinner-party planner, Budget, Cooking skill learning, Followers, Larder tracker,
Search with things I already have, Fridge-photo recipe generation, Voice
control, Sponsor contest, Making Photos, Sharing on Instagram, Step-by-step
cooking mode, Cooking skill level — 24 of 42.

## 4. What newly connects

- **Brainstorm → strategy → build, walkable for the first time.** *Ask grandma*
  (brainstorm, Cooking Support) → *Grandma / Grandpa AI* (impact map, realises
  *Learn cooking* and *supports unexperienced members*, priority 3) → *Grandma /
  Grandpa* (capability map, core) → *Ask Grandma / Grandpa* (Wardley, Custom)
  → *Grandma Avatar* (four stories, board pink sticky) → *Grandma Avatar*
  (glossary, is-a Help provider, provides 0..* Help, Help Request at 1). This is
  the longest trace in the graph and the only one that reaches every layer.
- **Timer** is the second: brainstorm → deliverable (priority 3) → Wardley
  Commodity component depending on Mobile. Nowhere in stories, board or
  glossary — it is a feature with a heritage and no behaviour.
- **The help loop has a vocabulary trail.** Story *asks* → board `Request help`
  → *Help requested* → glossary *Help Request* (0..* per Cook, at 1 Avatar and
  1 Community, contains 0..10 Picture, belongs to 1 Meal). Story *provides* →
  board `Provide help` → *Help provided* → glossary *Help* (for 1 Help Request,
  contains 1..3 Ingredient Substitute / 1 Preparation Step Explanation / 1..10
  Steps to Mitigate Catastrophe / 1..3 Menu proposal). The glossary's four
  sub-types of Help Request map one-to-one onto the four trouble situations —
  *Help with Ingredients* ↔ story AskCommunity s.2 / board *Ingredients
  missing*; *Help Meal plan* ↔ story AskChef s.2 / *Meal planning stalled*;
  *Help for Meal Preparation Step* ↔ story II s.2 / *Step unclear*; *Meal
  Preparation Catastrophe* ↔ story Grandma s.2 / *Catastrophe happened*.
- **User → Cook.** The board's `Register cook` reads *User*, produces *Cook*,
  and *User* never recurs. Recorded as `dkg:renamedTo` — the one language shift
  the board draws.
- **Membership money.** Goal metric *2,500 paying members* → impacts *convert
  to paid plan* / *paid member renews* → deliverable *Member management*
  (priority 1) → BMC *Monthly member fee* → Wardley *Become a member* →
  *Member Management* (Commodity) → capability map *Member management*
  (generic) → board *Cook registered*. Every layer agrees this is bought, not
  built.

## 5. What contradicts

Five pairs, reified, both sides kept.

1. **Thanks to Chef.** Story AskChef s.6: *Cook thanks Chef*. Glossary: *Thanks
   —to 0..1→ Grandma Avatar*, *Thanks —to 0..1→ Community*; **no arrow to
   Chef**, though *Chef is-a Help provider*.
2. **Help Request at Chef.** Story AskChef s.3: *Cook asks Chef for Help*.
   Glossary: *Help Request —at 1→ Grandma Avatar* and *—at 1→ Community*, both
   exactly one, none to Chef.
3. **Content Creation.** Capability map: *core*. Wardley: the components under
   *Create content* (*Write recipe*, *Upload media*) sit in Commodity.
4. **Grandma / Grandpa.** Capability map: *core*. Impact map: *Grandma /
   Grandpa AI* carries priority **3**, the lowest on the map. Meanwhile four of
   five stories and 13 of 21 board events are about it.
5. **What the avatar is.** Board: pink gear = external system. Stories: a
   person pictogram, asked and thanked like a human.

Not reified, but worth a line: the two board photos disagree on whether the
bubbles round events 15–16 are *Cooking Help* or *Cooking Assistance* (recorded
as a `dkg:spelling` on the context); *Rating* is a key activity on the BMC, a
Wardley need, a capability-map L1 chevron, an impact-map deliverable and a
story work object — and **absent from the board and the glossary**.

## 6. What is orphaned

- **BMC blocks nothing else touches:** *App* (the only channel), *Weekly for
  creating*, all five cost items. The canvas draws no inter-block lines, so
  none were emitted.
- **Impact-map deliverable *Content*** — priority 1, realises nothing readable.
- **Story Competition** is an island: *Community Administrator*, *Competition*,
  *Winner*, *Highest rate*, *online*, *Other cooks* appear in no other artifact
  except via *Competitions* on the Wardley map and *Sponsor contest* on the
  brainstorm. There is no board event, glossary term, capability or deliverable
  for a competition.
- **Glossary-only terms:** *Course*, *Step*, *Substitute*, *Preparation Step
  Explanation*, *Steps to Mitigate Catastrophe*, *Menu proposal*. *Course* in
  particular exists nowhere else — the *Dinner → Menu → Course → Meal* chain
  is the glossary's alone.
- **Board-only nouns read but never written:** *Guests*, *Recipe Catalog*,
  *Catastrophe*, *Help provider* (produced by nothing; read at 21).
- **Brainstorm ideas with no descendant anywhere (18):** Necessary stuff,
  Historical recipes, Grill master, Login with social account, Cooking Videos,
  Remix of recipes, Store recipes, Comments, Cooking Calendar, Food pairing,
  Smart kitchen, Search with cooking time, Left over optimizations, Allergenes,
  Kitchen tools' sibling *Larder tracker* (only weakly to *Refrigerator*), and
  the whole *Helper* cluster bar Timer.
- **Wardley components nothing downstream mentions:** Refrigerator, Nutrition,
  Meal, Free hand control, Remote meeting, Chat, IDM, Specific AI, Gen AI.
  Neither the stories nor the board contain a chat, a meeting or a fridge.
- **Actors on the impact map that no story or board sticky ever plays:**
  Kitchen ware retailer, Grocery store, Photo book service, Propects,
  Convertable, Cook as author.

## 7. Open questions (18 `dkg:Question` nodes in the file)

Merges awaiting a decision — the ones that change an architecture:
Cooking Assistance ≟ Cooking Help · Grandma Avatar ≟ Grandma / Grandpa (AI) ·
Pictures (evidence) ≟ Pictures (result) ≟ Picture · Help ≟ Help Request ≟
Help response · Community (audience) ≟ Community (responder) ≟ Community Cook.

Raised by the artifacts themselves:
- Brainstorm: what the blue stickies mean.
- Impact map: the actor→impact fan is not readable on the photo; which impacts
  Single, Young family, Chefs, Grocery store and Photo book service hang off,
  and what *Content*, *Rating*, *Planner*, *Famous chefs partner* realise.
- Wardley: which user owns which need (only Prospect → Become a member is a
  single line).
- Capability map: the ▲ and ▶ marks.
- Stories: duplicate numbering in Competition; *Grandma* vs *Grandma Avatar*
  in story II; whether *Help* is one thing or two; whether *Pictures* is.
- Board: what the sparkle and robot icons mean; whether *Prepare meal* is one
  command for seven events; why there are no state stickies and no hotspots;
  whether event 10 reads *plan setteled* or *Meal plan setteled*.
- Glossary: the colour groups and ⊞ marks; whether every Help Request is
  really *at 1* Avatar *and* *at 1* Community and never at a Chef; where the
  board's *Meal plan* sits in Dinner → Menu → Course → Meal.

---

**What this graph cannot do.** It shows what eight artifacts said, with every
spelling and its source. It has no view on whether the Wardley positions are
right, whether *Cooking Help* should be split, or whether priority 3 for the
avatar is a mistake. Those are questions for `wardley-map-critic`,
`pivotal-event-boundary-finder` / `event-storming-context-mapper` and
`impact-mapping-critic`, with this file as their input.