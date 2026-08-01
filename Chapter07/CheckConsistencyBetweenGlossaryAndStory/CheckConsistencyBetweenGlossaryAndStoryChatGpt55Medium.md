# Prompt 2026-08-01

Check the consistency of the three provided Domain Stories with the provided Visual Glossary.

![](../assets/Grandma.jpg)
![](../assets/GrandmaII.jpg)
![](../assets/VisualGlossaryGrandma.jpg)

[Skill](../Skills/DomainStoryVsVisualGlossaryConsistencySkill/SKILL.md)
[Worked example](../Skills/DomainStoryVsVisualGlossaryConsistencySkill/references/worked-example.md)
[Checks](../Skills/DomainStoryVsVisualGlossaryConsistencySkill/references/checks.md)

# Response

# Consistency Report — 3 Domain Stories × Visual Glossary

## 0. Scope checked

I treated the three stories as **as-is, coarse-grained scenarios**:

* **Story A — Community:** ingredient-related assistance
* **Story B — Grandma:** recovery from a burned meal
* **Story C — Grandma II:** assistance with a meal-preparation step

All visible story elements were legible. The glossary contains actor terms, so `Cook`, `Community`, and `Grandma Avatar` were included in the vocabulary check. Activities such as *asks*, *provides*, and *thanks* were not required to equal glossary relationship labels; they were used only to infer structural connections. This follows the supplied matching and false-positive rules.

## 1. Verdict

**Aligned in its main interaction pattern, but diverged in several important nouns and in the level of abstraction used for help requests and help contents.**

* **Blocking:** 0
* **Significant:** 5
* **Minor:** 6
* No clear cardinality contradiction was found.
* No cross-story sense conflict was found.
* The strongest alignment is in Story C, which closely follows the glossary’s meal-preparation-help structure.

The central recurring structure is consistent:

> `Cook → Help Request → Help → Cook continues/rescues Meal → Thanks/Picture sharing`

However, the stories frequently use concrete nouns such as `Ingredients`, `Meal Preparation Step`, and `Grandma`, while the glossary defines more specific concepts such as `Help with Ingredients`, `Ingredient Substitute`, `Help for Meal Preparation Step`, and `Grandma Avatar`.

---

## 2. Term ledger

`=` exact or structurally exact
`~` spelling/number variant
`✗` vocabulary mismatch
`—` absent

| Glossary term                  | Story A: Community          | Story B: Grandma             | Story C: Grandma II              | Status                       |
| ------------------------------ | --------------------------- | ---------------------------- | -------------------------------- | ---------------------------- |
| Cook                           | Cook                        | Cook                         | Cook                             | =                            |
| Meal                           | Meal                        | Meal                         | Meal                             | =                            |
| Recipe                         | —                           | —                            | —                                | — unused                     |
| Community                      | Community                   | Community                    | Community                        | =                            |
| Grandma Avatar                 | —                           | Grandma Avatar / **Grandma** | Grandma Avatar                   | ✗ in B(6)                    |
| Help Request                   | implied by “asks”           | implied by “asks”            | implied by “asks”                | concept present, term absent |
| Help                           | Help                        | Help                         | Help                             | =                            |
| Picture                        | **Pictures**                | **Pictures**                 | **Pictures**                     | ~ plural                     |
| Thanks                         | activity “thanks”           | activity “thanks”            | activity “thanks”                | implied, not named as object |
| Help with Ingredients          | “Help with Ingredients”     | —                            | —                                | = as composite phrase        |
| Ingredient Substitute          | **Ingredients Substitutes** | —                            | —                                | ~ wording/number             |
| Help for Meal Preparation Step | —                           | —                            | “Help for Meal Preparation Step” | = as composite phrase        |
| Preparation Step Explanation   | —                           | —                            | —                                | — unused                     |
| Meal Preparation Catastrophy   | —                           | implied by burned Meal       | —                                | — not explicitly named       |
| Steps to Mitigate Catastrophy  | —                           | possible generic rescue Help | —                                | — not explicitly named       |

### Story-only domain nouns

| Noun                            | Evidence         | Assessment                                                                                                |
| ------------------------------- | ---------------- | --------------------------------------------------------------------------------------------------------- |
| Ingredients                     | A(2), A(3)       | Not independently defined; the glossary only defines `Help with Ingredients` and `Ingredient Substitute`  |
| Meal Preparation Step           | C(2)–C(4)        | Not independently defined; the glossary defines a help-request type and an explanation associated with it |
| Grandma                         | B(6)             | Likely an informal alias for `Grandma Avatar`                                                             |
| Meal as a burned/rescued object | B(2), B(4), B(5) | A state of `Meal`, not an undefined term                                                                  |

`Burned Meal` and `rescued Meal` were treated as states of `Meal`, rather than new glossary terms, in accordance with the state guard.

---

## 3. Findings

### F1 · `TERM` / `DRIFT` · Significant

**Story B changes `Grandma Avatar` to `Grandma`.**

**Evidence:** B(3) and B(4) use `Grandma Avatar`; B(6) says the Cook thanks `Grandma`. Story C consistently uses `Grandma Avatar`.

This is both an internal story inconsistency and cross-story vocabulary drift. The glossary defines only `Grandma Avatar`.

* **Story moves:** B(6), `Grandma` → `Grandma Avatar`.
* **Glossary moves:** introduce `Grandma` as a separate term only if it denotes a human grandmother rather than the digital avatar.
* **Recommendation:** story moves.

---

### F2 · `UNDEF` · Significant

**Story A treats `Ingredients` as an independent work object, but the glossary does not define it.**

**Evidence:** A(2) and A(3) connect Help to `Ingredients`. The glossary contains:

* `Help with Ingredients`
* `Ingredient Substitute`

but no standalone `Ingredient` or `Ingredients`.

Two domain interpretations are possible:

1. Ingredients are real domain entities, in which case the glossary is incomplete.
2. The system only models the request category and suggested substitutes, in which case the story should avoid presenting `Ingredients` as a separately managed object.

* **Story moves:** represent the complete request as `Help with Ingredients`.
* **Glossary moves:** add `Ingredient` and relate it to `Recipe`, `Meal`, and/or `Ingredient Substitute`.
* **Recommendation:** ask the domain experts; adding `Ingredient` is likely if recipes and substitution logic are implemented.

---

### F3 · `UNDEF` · Significant

**Story C treats `Meal Preparation Step` as an independent work object, but the glossary does not define that exact concept.**

**Evidence:** C(2)–C(4). The glossary instead defines:

* `Help for Meal Preparation Step`
* `Preparation Step Explanation`

This leaves the actual preparation step itself unnamed. The glossary can describe an explanation, but not clearly what the explanation explains.

* **Story moves:** use the composite term `Help for Meal Preparation Step` where the request is meant.
* **Glossary moves:** add `Meal Preparation Step`, then relate `Preparation Step Explanation` to it.
* **Recommendation:** glossary moves. The standalone step appears conceptually necessary.

---

### F4 · `TERM` · Significant

**Story A’s `Ingredients Substitutes` does not faithfully use the glossary term `Ingredient Substitute`.**

**Evidence:** A(4).

This is probably a wording/number variation rather than a separate concept, but the current phrase can be read as “substitutes made of ingredients,” rather than “substitutes for an ingredient.”

* **Story moves:** `Ingredients Substitutes` → `Ingredient Substitutes` or, preferably, the glossary’s canonical singular `Ingredient Substitute`.
* **Glossary moves:** none unless collections are deliberately named in the plural.
* **Recommendation:** story moves.

---

### F5 · `REL-X` · Significant

**The stories repeatedly connect pictures directly to a recipient, while the glossary primarily places pictures inside `Help Request`, `Help`, or `Thanks`.**

**Evidence:**

* A(6): Cook shares Pictures with Community.
* B(6): Cook shares Pictures with Community.
* C(6): Cook shares Pictures with Community.

The glossary says that `Help Request`, `Help`, and `Thanks` may contain `Picture`, but it does not clearly show a direct `Community receives/views Picture` relationship.

This may simply be narrative shorthand: the pictures are contained in a `Thanks` object addressed to the Community. If direct picture sharing is a real business capability, the glossary lacks that connection.

* **Story moves:** make the container explicit: “Cook posts Thanks containing Pictures to Community.”
* **Glossary moves:** add something such as `Community —receives/views→ 0..* Picture`.
* **Recommendation:** ask whether pictures can exist or be shared independently of a request, help response, or thanks message.

---

### F6 · `UNUSED` · Minor

**`Recipe` is not touched by any of the three stories.**

The glossary requires each `Meal` to be “with 1 Recipe,” but every story prepares or rescues a Meal without mentioning its Recipe.

This is not automatically a lower-bound contradiction: coarse stories may omit mandatory structure already established outside the scenario. It does, however, leave an important glossary relationship untested.

* **Recommendation:** add a meal-selection or recipe-selection story, or mention the Recipe when the Meal is first prepared.

---

### F7 · `UNUSED` / `REL-0` · Minor

**`Preparation Step Explanation` is not explicitly used.**

Story C obtains generic `Help with Meal Preparation Step`, but does not name the content that the glossary says Help contains.

* **Possible patch:** C(4): “Grandma Avatar provides Help containing a Preparation Step Explanation.”

---

### F8 · `UNUSED` / `REL-0` · Minor

**`Meal Preparation Catastrophy` and `Steps to Mitigate Catastrophy` are not explicitly named in Story B.**

Story B is clearly the scenario intended to exercise those concepts:

* B(2): Cook burns Meal.
* B(4): Grandma Avatar provides Help to rescue Meal.

But it remains at a generic level. Consequently, the glossary’s catastrophe-specific request and content types are only inferred, not exercised directly.

* **Possible patch:** identify the request as a `Meal Preparation Catastrophy` and the Help content as `Steps to Mitigate Catastrophy`.

The spelling `Catastrophy` is retained here because it is the glossary’s current term; the standard spelling is **Catastrophe**. This is best handled as one glossary-wide typo correction rather than as multiple findings.

---

### F9 · `UNUSED` · Minor

**`Thanks` is represented only as an activity, never as a work object.**

All three stories say the Cook “thanks” someone, while the glossary models `Thanks` as a noun that can:

* be posted by a Cook,
* be directed to Community or Grandma Avatar,
* contain Pictures,
* refer to Help.

Because verbs are not checked for lexical equality with glossary terms, this is not a term error. It is a modelling-level mismatch between the dynamic stories and the static glossary.

* **Possible patch:** draw a `Thanks` work object in sentence 6 of each story.

---

### F10 · `REL-0` · Minor

Several glossary relationships are not explicitly exercised:

* `Meal —with→ 1 Recipe`
* `Community —contains→ 1..* Cook`
* `Thanks —for→ 1 Help`
* `Ingredient Substitute —belongs to→ 1 Help with Ingredients`
* `Preparation Step Explanation —belongs to→ 1 Help for Meal Preparation Step`
* `Steps to Mitigate Catastrophy —belongs to→ 1 Meal Preparation Catastrophy`

Some are strongly implied, but no story walks them explicitly.

---

## 4. Relationships and cardinalities

| Glossary relationship                         | Cardinality | Story evidence             | Verdict                                   |
| --------------------------------------------- | ----------: | -------------------------- | ----------------------------------------- |
| Cook —prepares→ Meal                          |        0..* | A1, A5; B1; C1, C5         | Consistent                                |
| Meal —with→ Recipe                            |           1 | —                          | Never exercised; not proven contradictory |
| Cook —posts→ Help Request                     |        0..* | A3, B3, C3 conceptually    | Consistent but different activity wording |
| Help Request —belongs to→ Meal                |           1 | A2–A3, B3–B4, C2–C4        | Implied; clearest in B                    |
| Community —provides→ Help                     |        0..* | A4                         | Consistent                                |
| Grandma Avatar —provides→ Help                |        0..* | B4, C4                     | Consistent                                |
| Help —for→ Help Request                       |           1 | A3–A4, B3–B4, C3–C4        | Consistent                                |
| Help Request —contains→ Picture               |        0..* | B2–B3                      | Consistent                                |
| Help —contains→ Picture                       |        0..* | Not clearly shown          | Never explicitly exercised                |
| Help —contains→ Ingredient Substitute         |        0..* | A4                         | Consistent                                |
| Help —contains→ Preparation Step Explanation  |        0..* | C4 only generically        | Not explicit                              |
| Help —contains→ Steps to Mitigate Catastrophy |        0..* | B4 only generically        | Not explicit                              |
| Cook —posts→ Thanks                           |        0..* | A6, B6, C6 as an activity  | Implied                                   |
| Thanks —contains→ Picture                     |        0..* | A6, B6, C6                 | Implied                                   |
| Thanks —for→ Help                             |           1 | Sequence implies it        | Not explicitly stated                     |
| Thanks —to→ Community                         |        0..1 | A6; possibly B6 and C6     | Consistent                                |
| Thanks —to→ Grandma Avatar                    |        0..1 | B6, after renaming Grandma | Consistent                                |

### Cardinality assessment

No story clearly violates an upper bound.

The mandatory `Meal —with→ 1 Recipe` and `Thanks —for→ 1 Help` relationships are omitted rather than contradicted. At the assumed coarse granularity, omission is insufficient evidence for a `CARD` finding.

---

## 5. Context/lane alignment

The glossary does not show explicit named bounded-context boxes, and the stories do not contain formal lanes. A strict `CTX` comparison is therefore **not available**.

The interaction channels nevertheless align informally:

| Story interaction point   | Glossary term                          |
| ------------------------- | -------------------------------------- |
| Community                 | Community                              |
| Grandma assistance        | Grandma Avatar                         |
| Ingredient-help scenario  | Help with Ingredients                  |
| Preparation-step scenario | Help for Meal Preparation Step         |
| Burned-meal scenario      | Meal Preparation Catastrophy, inferred |

The Community and Grandma Avatar are modelled as two distinct help providers, and the stories preserve that distinction.

---

## 6. Patch list

### Story A — Community

* A(2) and A(3): treat `Help with Ingredients` as the canonical request term.
* A(4): `Ingredients Substitutes` → `Ingredient Substitute`.
* A(4): optionally state that Help **contains** the Ingredient Substitute.
* A(6): add a `Thanks` work object containing the Pictures and addressed to Community.

### Story B — Grandma

* B(3): identify the request as a `Meal Preparation Catastrophy`, if that is the intended glossary category.
* B(4): identify the supplied content as `Steps to Mitigate Catastrophy`.
* B(6): `Grandma` → `Grandma Avatar`.
* B(6): add a `Thanks` work object and clarify whether its Pictures are shared with Community independently or as part of Thanks.

### Story C — Grandma II

* C(2)–C(4): preserve the canonical composite term `Help for Meal Preparation Step`.
* C(4): name `Preparation Step Explanation` as the content of Help.
* C(6): add a `Thanks` work object containing Pictures.

### Visual Glossary

* Decide whether to add standalone `Ingredient`.
* Add standalone `Meal Preparation Step`.
* Relate `Preparation Step Explanation` to `Meal Preparation Step`.
* Clarify whether Community directly receives/views Pictures.
* Correct `Catastrophy` → `Catastrophe` consistently, including:

    * `Meal Preparation Catastrophe`
    * `Steps to Mitigate Catastrophe`
* Consider showing which relationships are alternatives, particularly whether one Thanks is sent to either Community or Grandma Avatar.

---

## 7. Already aligned

All three stories consistently use `Cook`, `Meal`, and `Help`. They also agree that Help is obtained from one of the glossary’s recognised providers: `Community` or `Grandma Avatar`.

The stories exercise all three intended help scenarios:

* ingredient assistance,
* recovery from a meal-preparation failure,
* explanation of a meal-preparation step.

The provider relationships are consistent with the glossary, picture multiplicities are compatible with `0..*`, and the stories do not contradict any explicit upper cardinality. Story C most closely follows the glossary’s canonical vocabulary and can serve as the pattern for revising the other two.

The report structure and severity treatment follow the supplied consistency skill and its worked example.  
