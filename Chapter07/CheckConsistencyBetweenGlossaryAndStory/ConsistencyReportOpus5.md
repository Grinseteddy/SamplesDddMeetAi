# Consistency Report — 3 Domain Stories × Visual Glossary "Grandma"

## 0. Scope checked

| Story | File | Scenario | Scope assumed | Lanes |
|---|---|---|---|---|
| **A** | `Community.jpg` | Cook lacks ingredients, the Community helps | to-be (assumed) | none drawn |
| **B** | `Grandma.jpg` | Cook burns the meal, the Grandma Avatar rescues it | to-be (assumed) | none drawn |
| **C** | `GrandmaII.jpg` | Cook is stuck on a preparation step, the Grandma Avatar explains it | to-be (assumed) | none drawn |

Glossary: 15 terms, 26 relationships, **no bounded-context groups drawn**, no pictogram distinctions (all stickies identical yellow).

**Scope assumption.** No story carries an as-is/to-be marker. Because a "Grandma Avatar" is plainly a system feature rather than an observed practice, all three are treated as **to-be**, so vocabulary a story has that the glossary lacks is filed as glossary work, not as a story defect (guard G5). If any of these is actually an as-is story, tell me and F5/F6/F7 flip direction.

**Marked UNCHECKED — nothing.** Two glossary edges were read with only moderate confidence and are flagged inline: the two `Thanks —to→` edges (the long routed lines through the top-left of the board) and `Thanks —for→ 1 Help` (the bottom-most line). Confirm those three before acting on F8.

---

## 1. Verdict

**Diverged — but in one specific and fixable way.**

The three stories agree with each other on actors and on the shape of the scenario. Where they diverge from the glossary, it is almost always the *same* divergence: **the glossary carefully splits "the thing you ask for" from "the thing you get", and none of the three stories makes that split.** Every story writes `Help` for both. Six of the fifteen glossary terms — the entire request/response taxonomy — therefore never appear in any story by name.

**2 Blocking · 6 Significant · 2 Minor (grouped).**
By story: A — 6, B — 7, C — 6 (four shared across all three).
**9 of 15 glossary terms** exercised by name (two of those only as variants). **16 of 26 relationships** exercised, 6 of them only implicitly.

---

## 2. Term ledger

| Glossary term                  | Story A (Community) | Story B (Grandma) | Story C (GrandmaII) | Status |
|--------------------------------|---|---|---|---|
| Cook                           | Cook (1,2,3,5,6) | Cook (1,2,3,5,6) | Cook (1,2,3,5,6) | = |
| Community                      | Community (3,4,6) | Community (6) | Community (6) | = |
| Grandma Avatar                 | — | Grandma Avatar (3,4) / **Grandma** (6) | Grandma Avatar (3,4,6) | ✗ mismatch in B(6) |
| Meal                           | Meal (1,5) | Meal (1,2,4,5) | Meal (1,5) | = |
| Recipe                         | — | — | — | — unused |
| **Help Request**               | — *(written `Help`)* | — *(written `Help`)* | — *(written `Help`)* | ✗ **never named — F1** |
| Help                           | Help (2,3,4) | Help (3,4) | Help (2,3,4) | ✗ **overloaded — F1** |
| Picture                        | Pictures (5,6) | Pictures (2,3,5,6) | Pictures (5,6) | ~ plural |
| Thanks                         | *(verb only, 6)* | *(verb only, 6)* | *(verb only, 6)* | ✗ never a work object — F9 |
| Meal Preparation Catastrophe   | — | — *(B is this scenario; B(2) says "burns Meal")* | — | — unused, **F3** |
| Help for Meal Preparation Step | — | — | "Help for Meal Preparation Step" (2) / "Help **with** Meal Preparation Step" (3,4) | ~ variant, misused in (4) |
| Help with Ingredients          | "Help with Ingredients" (2,3) | — | — | = |
| Ingredient Substitute          | "Ingredients Substitutes" (4) | — | — | ~ plural/spacing |
| Preparation Step Expanation    | — | — | — *(C(4) says "Help with Meal Preparation Step")* | — unused, **F3** |
| Steps to Mitigate Catastrophe  | — | — *(B(4) says "Help to rescue Meal")* | — | — unused, **F3** |

### Story-only nouns — domain terms missing from the glossary

| Noun | Where | Note |
|---|---|---|
| **Ingredients** | A(2), A(3) | The glossary has `Ingredient Substitute` but no `Ingredient`. You can model the replacement without ever modelling the thing being replaced. |
| **Meal Preparation Step** | C(2), C(3), C(4) | Appears only inside the compound names `Help for Meal Preparation Step` and `Preparation Step Expanation`, never as a term of its own with a link to `Meal` or `Recipe`. |
| **Grandma** | B(6) | Almost certainly `Grandma Avatar` — but see F4, it may not be. |

### Out of glossary scope
Nothing. No UI channels, devices or scene props appear in any of the three stories — they are unusually clean in that respect.

---

## 3. Findings

### F1 · `SENSE` · **Blocking** — `Help` means two different things, in all three stories

The glossary draws a sharp line: `Help Request` is what a Cook posts, `Help` is what a Community or Grandma Avatar provides, and `Help —for→ 1 Help Request` joins them. The stories collapse both onto the single word `Help`:

- **request side:** A(2) "needs Help with Ingredients", A(3) "asks Community for Help", B(3) "asks Grandma Avatar for Help with Pictures", C(2) "needs Help for Meal Preparation Step", C(3) "asks Grandma Avatar for Help"
- **response side:** A(4) "Community provides Help", B(4) "Grandma Avatar provides Help", C(4) "Grandma Avatar provides Help"

Two glossary terms plausibly match one story noun, and that ambiguity *is* the finding. It is worse than a synonym: a reader of Story A sees "Help" in sentence 3 and "Help" in sentence 4 and reasonably concludes they are the same object, which is exactly the false agreement that survives until two people build a `Help` table and discover they meant different rows.

Note also that the request side is the one carrying the glossary's *pictures*: B(3) attaches Pictures to what it calls "Help", and the glossary attaches pictures to both `Help Request` and `Help`. So the overload is not merely lexical — it is already ambiguous about where an attachment lands.

*Story moves:* rename the request-side occurrences to `Help Request` in all three stories.
*Glossary moves:* only if the split is not real — but the glossary's whole lower half (three request subtypes, three response content types, each pair joined by `belongs to`) says it is very real.
**Recommendation: the stories move.** This one edit fixes roughly half of everything else in this report.

### F2 · `CARD` · **Blocking** — a Help Request cannot be posted to only one place

The glossary reads `Help Request —at→ 1 Community` **and** `Help Request —at→ 1 Grandma Avatar`. Both mandatory. Taken literally, every Help Request must sit at a Community *and* at a Grandma Avatar simultaneously.

The stories say otherwise, unanimously: A(3) goes to the Community alone, B(3) and C(3) go to the Grandma Avatar alone. No story exercises both. Three independent scenarios all constructing the thing without a mandatory part is strong evidence.

*Glossary moves* — this is the check where the story is the stronger evidence, because it records what people do while the cardinality records what someone recalled at the board. Two candidate fixes, and they are not the same model:

1. `0..1` on both ends, plus the constraint "exactly one of the two" — a request goes *either* to the community *or* to the avatar.
2. Introduce a common supertype (`Helper`, say) that both `Community` and `Grandma Avatar` are, with `Help Request —at→ 1 Helper`.

Option 2 also tidies the duplicated `provides 0..*` edges. **Recommendation: ask which, then fix the glossary, not the stories.**

### F3 · `DRIFT` · Significant — three stories, three ad-hoc names for the same slot

The glossary names the *content* of a Help precisely: `Ingredient Substitute`, `Preparation Step Expanation`, `Steps to Mitigate Catastrophe`. Sentence 4 of each story is exactly the moment that content is delivered — and each story names it differently:

| | Story A(4) | Story B(4)                        | Story C(4) |
|---|---|-----------------------------------|---|
| story says | "Help with **Ingredients Substitutes**" | "Help **to rescue Meal**"         | "Help with **Meal Preparation Step**" |
| glossary says | `Ingredient Substitute` ~ close | `Steps to Mitigate Catastrophe` ✗ | `Preparation Step Expanation` ✗ |

Only Story A lands near the glossary word. Story B invents free prose. Story C reuses the *request*-type name (`Help ... Meal Preparation Step`) for the *response*, which is F1 showing up a second time at a different place.

The same holds for the request subtypes: A(2,3) correctly says `Help with Ingredients`, C(2) correctly says `Help for Meal Preparation Step`, and Story B — which is the `Meal Preparation Catastrophe` scenario in its entirety — never once uses that name. The glossary's word for what Story B is *about* does not appear in Story B.

The insight is not six words. It is that the bottom row of the glossary — the taxonomy that took the most work to draw — has not reached the stories at all. *Story moves,* all three, toward the glossary terms.

### F4 · `TERM` · Significant — `Grandma` vs `Grandma Avatar` in Story B

Story B says `Grandma Avatar` in sentences 3 and 4, then `Grandma` in sentence 6. Story C says `Grandma Avatar` in all three. Inside one story that usually means a late edit — but here the shorter word is doing something suspicious: it appears precisely at the *thanking* step.

So there are two possible worlds, and they are not close:

- **Alias.** Someone shortened the label while drawing. → *Story moves:* B(6) `Grandma` → `Grandma Avatar`.
- **Two parties.** The Cook asks an *avatar* for help but thanks a *person* — a real grandmother behind the persona, or the community member whose recipe the avatar drew on. → then the glossary is missing a term and the `Thanks —to→` edges point at the wrong thing.

The word "Avatar" itself implies something stands behind it. **Recommendation: ask.** One sentence from a domain expert settles it; guessing does not.

### F5 · `UNDEF` · Significant — `Ingredients` and `Meal Preparation Step` are undefined

Both are load-bearing domain nouns, not scenery.

- **`Ingredients`** — Story A(2,3): the Cook needs help *with Ingredients*. The glossary defines `Ingredient Substitute` but has no `Ingredient` and no link from `Meal` or `Recipe` to one. You can record the substitute without being able to record what it substitutes for, or which recipe called for it.
- **`Meal Preparation Step`** — Story C(2,3,4): the whole scenario turns on one step of a preparation. The glossary contains the phrase only inside two compound sticky names; there is no `Meal Preparation Step` term, and therefore no way to say *which* step the Cook is stuck on.

Both stories are to-be, so these are **glossary moves**: add both terms, and relate them (`Recipe —contains→ 1..* Meal Preparation Step`, `Recipe —requires→ 0..* Ingredient` — cardinalities are guesses).

### F6 · `REL-X` · Significant — the Meal has a lifecycle the glossary cannot express

Story B(2) "Cook **burns** Meal"; B(5) "Cook **rescues** Meal". That is one Meal in three states — prepared, burnt, rescued — and the glossary has no status, state, or lifecycle concept anywhere on `Meal`. Per guard G1 this is raised once, not once per adjective.

This is worth more than it looks, because `Meal Preparation Catastrophe` is currently modelled as a *kind of Help Request* — the catastrophe exists only as something you ask about. Nothing in the model says the *meal* went wrong. If the burnt meal is never rescued and no request is ever posted, the model has no record that anything happened.

*Glossary moves:* add a state to `Meal`, and decide whether `Meal Preparation Catastrophe` is a request type, a meal state, or both (in which case it is two terms wearing one name — closely related to F1).

### F7 · `REL-X` · Significant — a Picture of a Meal has nowhere to attach

Every story photographs a meal: A(5), B(2), B(5), C(5) all read "…Meal **and takes** Pictures". The glossary attaches `Picture` to `Help Request`, `Help` and `Thanks` — never to `Meal`. So the picture the Cook takes in A(5) can only enter the model once it is wrapped in a Thanks.

B(2) makes this concrete: the Cook photographs the burnt meal *before* posting anything. At that moment the picture belongs to a meal and nothing else.

*Glossary moves:* add `Picture —of→ 1 Meal` (or `Meal —has→ 0..* Picture`), and then decide whether the pictures inside a Help Request / Thanks are references to those, or separate uploads. **Recommendation: add the edge; it is the missing link that makes the three existing `contains Picture` edges coherent.**

### F8 · `REL-X` · Significant — thanking and sharing are two acts, modelled as one

Sentence 6 of Stories B and C splits the recipients: *"Cook thanks Grandma Avatar and shares Pictures with Community."* The thanks goes to the helper; the pictures go to the crowd. Story A(6) does not split them — it thanks the Community and shares with the Community, because there they are the same party.

The glossary models one `Thanks` that both `—contains→ 0..* Picture` and `—to→ 0..1 Community` / `—to→ 0..1 Grandma Avatar`. Strictly, a single Thanks with both recipients set can express B(6) — but it says the Community *received the thanks*, which is not what the story shows. The stories are describing a **share** that is distinct from the thanks.

*Glossary moves:* either add a distinct `Share`/`Post` concept carrying pictures to a Community, or split the recipient roles on `Thanks` (`—thanks→ Helper`, `—published to→ Community`). **This finding depends on my reading of the two `Thanks —to→` edges — confirm those first.**

### F9 · `UNUSED` + `REL-0` · Minor (grouped)

- **`Recipe`** and `Meal —with→ 1 Recipe` — never touched by any story, yet mandatory. A `1` on a term no scenario exercises is worth a question: must a Meal have a Recipe before it can exist? Story C, which is about being stuck on a *preparation step*, is the story that should have used it. Likely a **missing story** rather than dead vocabulary.
- **`Community —contains→ 1..* Cook`** — never exercised. Community appears in the stories only as a helper and an audience, never as something a Cook belongs to.
- **`Help —contains→ 0..* Picture`** — never exercised. Only Help *Requests* (B3) and Thanks (all 6s) carry pictures in the stories. Does a Grandma Avatar's answer ever include an image? If not, drop the edge.
- **`Thanks` as a work object** — all three stories treat thanking as an activity verb ("6 thanks") and never draw `Thanks` as a thing. Not a defect (guard G6), but the glossary makes it an artifact with pictures and a recipient, so at least one story should show it being created.
- **`Meal Preparation Catastrophe`**, **`Preparation Step Expanation`**, **`Steps to Mitigate Catastrophe`** — never named; covered by F3.

### F10 · Minor (grouped) — variants and spellings

- `Pictures` → `Picture` (plural, all stories, all sentences) — the glossary term is singular and the cardinality carries the plurality.
- A(4) `Ingredients Substitutes` → `Ingredient Substitute` (plural + spacing).
- C(3), C(4) "Help **with** Meal Preparation Step" → `Help for Meal Preparation Step` (preposition), though see F3 — in C(4) the right fix is a different term entirely, not a preposition.
- **Glossary-side spellings**, read charitably and noted once (G7): `Preparation Step **Expanation**` → *Explanation*; `**Catastrophe**` → *Catastrophe* (twice). Not findings; just fix them.

---

## 4. Relationships & cardinalities

| Glossary relationship                                                   | Card. | Exercised by | Verdict |
|-------------------------------------------------------------------------|---|---|---|
| Cook —prepares→ Meal                                                    | 0..* | A(1,5), B(1), C(1,5) | consistent |
| Meal —with→ Recipe                                                      | 1 | — | never exercised — F9 |
| Community —contains→ Cook                                               | 1..* | — | never exercised — F9 |
| Cook —posts→ Help Request                                               | 0..* | A(3), B(3), C(3) | consistent *(named `Help` — F1)* |
| Cook —posts→ Thanks                                                     | 0..* | A(6), B(6), C(6) | consistent *(as a verb — F9)* |
| Help Request —at→ Community                                             | **1** | A(3) | **contradicted — F2** |
| Help Request —at→ Grandma Avatar                                        | **1** | B(3), C(3) | **contradicted — F2** |
| Community —provides→ Help                                               | 0..* | A(4) | consistent |
| Grandma Avatar —provides→ Help                                          | 0..* | B(4), C(4) | consistent |
| Help —for→ Help Request                                                 | 1 | implied A(3→4), B(3→4), C(3→4) | consistent, implicit only |
| Help Request —belongs→ Meal                                             | 1 | implied A(2), B(3), C(2) | consistent, implicit only |
| Help Request —contains→ Picture                                         | 0..* | B(3) | consistent |
| Help —contains→ Picture                                                 | 0..* | — | never exercised — F9 |
| Thanks —contains→ Picture                                               | 0..* | A(6), B(6), C(6) | consistent |
| Help —contains→ Ingredient Substitute                                   | 0..* | A(4) | consistent |
| Help —contains→ Preparation Step Expanation                             | 0..* | implied C(4) | never exercised by name — F3 |
| Help —contains→ Steps to Mitigate Catastrophe                           | 0..* | implied B(4) | never exercised by name — F3 |
| Meal Preparation Catastrophe —is→ Help Request                          | — | implied B(3) | never exercised by name — F3 |
| Help for Meal Preparation Step —is→ Help Request                        | — | C(2) | consistent |
| Help with Ingredients —is→ Help Request                                 | — | A(2,3) | consistent |
| Ingredient Substitute —belongs to→ Help with Ingredients                | 1 | implied A(3→4) | consistent, implicit only |
| Preparation Step Expanation —belongs to→ Help for Meal Preparation Step | 1 | — | never exercised |
| Steps to Mitigate Catastrophe —belongs to→ Meal Preparation Catastrophe | 1 | — | never exercised |
| Thanks —to→ Community *(reading uncertain)*                             | 0..1 | A(6) | consistent |
| Thanks —to→ Grandma Avatar *(reading uncertain)*                        | 0..1 | B(6), C(6) | consistent |
| Thanks —for→ Help *(reading uncertain)*                                 | 1 | implied A(6), B(6), C(6) | consistent, implicit only |

**Implied by the stories, absent from the glossary:**

| Implied relationship | Evidence | Finding |
|---|---|---|
| Picture —of→ Meal | A(5), B(2), B(5), C(5) | F7 |
| Meal → state (prepared / burnt / rescued) | B(2), B(5) | F6 |
| Recipe / Meal → Meal Preparation Step | C(2,3,4) | F5 |
| Recipe / Meal → Ingredient | A(2,3) | F5 |
| Cook —shares Pictures with→ Community *(distinct from Thanks)* | B(6), C(6) | F8 |

**Three edges carry no cardinality at all** — the three `is` edges. Nothing in the stories can contradict them, so they are noted as unverifiable rather than consistent. They also read as *is-a* subtyping while being drawn as plain arrows; if that is the intent, the arrowhead should say so.

---

## 5. Bounded contexts

**Not checkable.** The glossary draws no groups, boxes or lanes, and none of the three stories is divided into lanes either. So `CTX` produces no findings — but also no assurance.

The glossary does suggest a natural clustering worth confirming, since the stories back it up: **Cooking** (`Cook`, `Meal`, `Recipe`, `Meal Preparation Step`, `Ingredient`) · **Help Exchange** (`Help Request` + its three subtypes, `Help` + its three content types, `Grandma Avatar`) · **Community** (`Community`, `Thanks`, `Picture`). Note that `Picture` and `Community` would then be shared across two of the three — usually a sign they deserve a closer look, not a rename.

---

## 6. Patch list

### Stories

- **A(2,3), B(3), C(2,3)**: request-side `Help` → **`Help Request`** *(F1 — the single highest-value edit)*
- **A(4)**: `Help with Ingredients Substitutes` → `Help with Ingredient Substitute` *(F3, F10)*
- **B(3)**: name the request type — `Meal Preparation Catastrophe` *(F3)*
- **B(4)**: `Help to rescue Meal` → `Help with Steps to Mitigate Catastrophe` *(F3)*
- **B(6)**: `Grandma` → `Grandma Avatar` *(F4 — pending the question below)*
- **C(3)**: `Help with Meal Preparation Step` → `Help for Meal Preparation Step` *(F10)*
- **C(4)**: `Help with Meal Preparation Step` → `Help with Preparation Step Explanation` *(F3)*
- **All stories, all sentences**: `Pictures` → `Picture` *(F10)*
- **New story wanted**: one that exercises `Recipe` — the term is mandatory on `Meal` and no scenario touches it *(F9)*

### Glossary

- `Help Request —at→ Community` and `—at→ Grandma Avatar`: `1` → `0..1` with an exactly-one constraint, **or** introduce a `Helper` supertype *(F2 — decide which)*
- Add **`Ingredient`**; relate `Recipe —requires→ 0..* Ingredient` *(guess)* and `Ingredient Substitute —substitutes for→ 1 Ingredient` *(F5)*
- Add **`Meal Preparation Step`**; relate `Recipe —contains→ 1..* Meal Preparation Step` *(guess)* *(F5)*
- Add a **state/lifecycle** on `Meal` (prepared / burnt / rescued) and decide whether `Meal Preparation Catastrophe` is a request type, a meal state, or two terms sharing a name *(F6)*
- Add **`Picture —of→ 1 Meal`** *(F7)*
- Split the thanking act from the sharing act, or split `Thanks`'s recipient roles *(F8 — confirm my reading of those edges first)*
- Decide whether `Help —contains→ Picture` is real; drop it if a Help never carries images *(F9)*
- Put cardinalities on the three `is` edges, or redraw them with a generalisation arrowhead if they are subtyping *(F10)*
- Spelling: `Expanation` → `Explanation`; `Catastrophe` → `Catastrophe` (×2) *(F10)*

### Questions for a domain expert

1. **F4** — In Story B(6), is `Grandma` the same thing as `Grandma Avatar`, or is there a person behind the persona who gets thanked?
2. **F2** — Can a Help Request go to a Community *and* a Grandma Avatar at once, or is it always exactly one of the two?
3. **F6** — When a meal is burnt and no help is ever requested, should the system know the meal went wrong?
4. **F9** — Must a Meal always have a Recipe? Nothing in three stories exercises that mandatory link.

---

## 7. Already aligned

Worth protecting, and evidence that the aligned parts were checked too:

- **The actors are clean.** `Cook`, `Community` and `Grandma Avatar` are the glossary's words, used consistently everywhere they appear (with the single exception of B(6)). No story invents an actor.
- **The scenario shape matches the model exactly.** All three stories run *prepare → problem → post request → receive help → resolve → thank*, which is precisely the path `Cook → Help Request → Help → Thanks` that the glossary lays out. The disagreement is about *names*, not about how the domain works — which is the cheap kind.
- **Story A is the closest to the glossary** and is the model the other two should be brought to: it is the only story that uses a request-subtype name (`Help with Ingredients`) and a content-type name (`Ingredients Substitutes`) in the right slots. Fix its `Help` → `Help Request` and its plurals and it is essentially conformant.
- **Community and Grandma Avatar are not vocabulary drift.** They are two genuinely different helpers, exactly as the glossary's two parallel `provides` edges say — Story A uses one, Stories B and C the other. No `DRIFT` finding there.
- **No UI channels, devices or scene props** anywhere in the three stories — nothing had to be excused under the out-of-scope guards.
- `Cook —prepares→ Meal`, `Cook —posts→ Thanks`, `Thanks —contains→ Picture`, `Community/Grandma Avatar —provides→ Help` and `Help —contains→ Ingredient Substitute` are borne out exactly as drawn.

---

*Method: `domain-story-glossary-consistency` — conformance check across the union (coverage), per story (mismatches), and pairwise (drift, sense). This report does not judge the stories' own quality — CRUD verbs, collapsed actors, hidden branches. If you want that, `domain-story-critic` is the follow-up.*