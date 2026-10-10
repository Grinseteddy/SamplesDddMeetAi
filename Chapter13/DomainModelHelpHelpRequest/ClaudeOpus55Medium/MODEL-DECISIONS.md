# Model Decision Record — Cooking Assistance: Help & Help Request

## Context

| | |
|---|---|
| **Bounded context** | Cooking Assistance (as named by the team; the earlier analyses in this project argue *Cooking Help* is the same context) |
| **Language** | Java 17 (records, sealed interfaces) — compiled with `javac --release 17 -Xlint:all -Werror` |
| **Root package** | `de.codecentric.cookingassistance.domain` — derived, no codebase given |
| **Sources** | `HelpNew.jpg` (Help glossary), `HelpRequestNew.jpg` (Help Request glossary) |
| **Verification** | `SmokeTest` — 38 checks: one valid path per aggregate, one violation per cardinality and per purple invariant sticky. All pass. |

**Terms in scope:** everything on both glossaries.
**Terms excluded:** none. Cook, Recipe, howTo Step and ingredient belong to other
contexts and appear only as typed IDs (see *External references*).

Both glossaries carry a colour legend (aggregate / entity / value object /
invariant). It was used as evidence, not as an instruction; the one place this
model departs from it is called out below and is open question 1.

```
src/main/java/de/codecentric/cookingassistance/domain/
  helprequest/  HelpRequest, Status, HelpRequestRuleViolation, HelpRequestRepository
  help/         Help, Answer (sealed), Substitutes, Substitute, SubstituteIngredient, Unit,
                PreparationStepExplanation, CatastropheMitigation, MenuProposal, Course, Meal,
                HelpRuleViolation, HelpRepository
  shared/       HelpId, HelpRequestId, AnswerType, HelpProviderType, HelpAccepted,
                DomainRuleViolation, Require
  external/     CookId, RecipeId, HowToStepId, IngredientId
src/test/java/…/SmokeTest.java
```

---

## Classification

| Term (glossary spelling) | Java type | Kind | Evidence | Confidence |
|---|---|---|---|---|
| **Help Request** | `HelpRequest` | **Aggregate root** | Legend colour *aggregate*; own id; *status* lifecycle; referred to from Help | High |
| help request Id | `HelpRequestId` | Value object (ID) | Identifier sticky | High |
| requester | `CookId` | External ID | UUID of a cook, owned by Cook Profile | High |
| title | `String` (non-blank) | Value (primitive field) | Leaf, only rule is non-blank — not wrapped | High |
| type | `AnswerType` | Enum (shared) | Closed set of four; identical to Help's *answerType* | High |
| description | `String` (non-blank) | Value (primitive field) | Leaf | High |
| recipe | `RecipeId` | External ID | UUID; recipes are owned off-context | High |
| howTo Step | `HowToStepId` | External ID | UUID of a recipe step | High |
| ingredients | `List<IngredientId>` | External IDs | UUIDs, 0..* | High |
| preferred Provider | `Set<HelpProviderType>` | Enum set | Closed set Chef / Grandma Avatar / Community, 1..2 | High |
| status | `Status` | Enum | Open / Answered / Closed | High |
| picture | `List<URI>` | Value | URL leaf; `URI` is already a validated value type | High |
| **Help** | `Help` | **Aggregate root** | Legend colour *aggregate*; own id; *isAccepted* changes over time | High |
| help Id | `HelpId` | Value object (ID) | Identifier sticky | High |
| help request (refers to) | `HelpRequestId` | ID of another aggregate | Drawn "refers to", not "contains" → hold the id, never the object | High |
| help requester (refers to) | `CookId` | External ID | Same UUID as the request's requester in the examples → copied from the request at creation | Medium |
| answer title | `String` (non-blank) | Value (primitive field) | Leaf | High |
| help provider type | `HelpProviderType` | Enum (shared) | Same three values as *preferred Provider* | High |
| help provider | `CookId` (0..1) | External ID | UUID of a cook — "A cook can answer their own request" | Medium |
| answer | `Answer` (sealed interface) | **Value object** | Legend says *entity* — see below | **Medium** |
| answerType | derived from the `Answer` subtype | Enum (shared) | Each oneOf branch "contains 1 answerType" fixed to one value | High |
| substitutes | `Substitutes` | **Value object** | Legend says *entity*; no id, reached only by composition | Medium |
| substitute | `Substitute` | Value object | Green; composition, 1..* | High |
| ingredient | `IngredientId` | External ID | UUID | High |
| substitute ingredient | `SubstituteIngredient` | Value object | Name + Value + Unit always travel together | High |
| Name / Value | `String` / `BigDecimal` | Fields of `SubstituteIngredient` | Leaves | High |
| Unit | `Unit` | Enum | Closed set of 13 drawn | High |
| preparation step explanation | `PreparationStepExplanation` | **Value object** | Legend says *entity*; no id | Medium |
| description (of explanation) | `String` | Field | Leaf | High |
| images | `List<URI>` | Value | URL leaf, 0..* | High |
| catastrophe mitigation | `CatastropheMitigation` | **Value object** | Legend says *entity*; no id | Medium |
| explanation | `String` | Field | Leaf | High |
| menu proposal | `MenuProposal` | **Value object** | Legend says *entity*; no id | Medium |
| note / how to serve | `String` / `Optional<String>` | Fields | Leaves (1 / 0..1) | High |
| servings | `int` (≥ 1) | Field | Count | High |
| meal | `Meal` | Enum | breakfast / lunch / dinner / supper | High |
| course | `Course` | **Value object** | Legend says *entity*; no id; two courses with equal dish and recipe are interchangeable | Medium |
| dish | `int` (≥ 1) | Field of `Course` | Example "2"; marked **!** | Low |
| isAccepted | `boolean` on `Help` | State | Changed only by `Help.accept(...)`; marked **!** | Medium |

### Where the model departs from the legend

The team coloured *answer*, *substitutes*, *preparation step explanation*,
*catastrophe mitigation*, *menu proposal* and *course* as **entities**. None of
them carries an identifier, none is referred to from anywhere else, and each is
reached only by `contains` from a single owner. By the skill's rules that is a
value object, and modelling it as an entity would mean inventing an id the
glossary does not draw. Promoting a value object to an entity later is an
additive change; the reverse breaks references. **Open question 1** asks the
team which they meant.

---

## Aggregates

**Help Request** — root `HelpRequest`. Holds its own values and the IDs of
cook, recipe, howTo step and ingredients. Changes after creation only through
`acceptHelp(HelpAccepted)` and `close()`.

**Help** — root `Help`. Holds `HelpRequestId` (never the `HelpRequest`), the
copied `CookId` of the requester, and the `Answer` value. Created by
`Help.provide(…, HelpRequest request, …)` and changed only by
`accept(HelpRequest)`. The request is passed in to be **read** — the glossary's
"same as in request" rules cannot be checked otherwise — and is never stored.

**The one thing not on either glossary:** `HelpAccepted (helpId, helpRequestId)`.
Help Request's rule *"At least one Help must be accepted"* depends on Help's
*isAccepted*, which lives in the other aggregate. `Help.accept()` returns this
fact and `HelpRequest.acceptHelp()` consumes it, so neither aggregate holds the
other and package dependencies stay one-way (`help → helprequest`). It is the
natural candidate for a published domain event.

---

## Invariants — where each is enforced

### Help Request

| Glossary rule | Enforced in | Exception |
|---|---|---|
| help request Id, requester, title, type, description, status — **1** | constructor | `RequiredTermMissing` |
| recipe **0..1**, *Mandatory* for Preparation Step Explanation and Ingredient Substitute | constructor | `RecipeMandatoryForType` |
| howTo Step **0..1**, *Only for type* Preparation Step Explanation | constructor | `HowToStepOnlyForPreparationStepExplanation` |
| ingredients **0..\***, *Only for type* Ingredient Substitute | constructor | `IngredientsOnlyForIngredientSubstitute` |
| preferred Provider **1..2** (named constants) | constructor | `PreferredProviderCountOutOfRange`, `DuplicatePreferredProvider` |
| *Chef help is provided exclusively* | constructor | `ChefHelpMustBeExclusive` |
| *Chef support only for menu proposal* / *Only for type* Menu proposal | constructor | `ChefSupportOnlyForMenuProposal` |
| status Answered ⇒ *At least one Help must be accepted* | `acceptHelp` is the only way into Answered | `AcceptedHelpForOtherRequest` |
| *Answered help requests can be still answered* | `acceptsHelp()` true for Open and Answered | — |
| Closed is terminal | `acceptHelp`, `close` | `HelpRequestClosed` |
| picture **0..\*** | constructor, unmodifiable | — |

### Help

| Glossary rule | Enforced in | Exception |
|---|---|---|
| help Id, help request, help requester, answer title, help provider type, answer, isAccepted — **1** | constructor | `RequiredTermMissing` |
| answer = **oneOf** four branches, each with a fixed answerType | `sealed interface Answer` — a mismatched type cannot be built | (compile time) |
| answerType *Must be the same as request type* | `Help.provide` | `AnswerTypeNotAsRequested` |
| recipe *Same recipe as in request* (substitutes, preparation step explanation; catastrophe mitigation when present) | `Help.provide` | `RecipeNotAsInRequest` |
| howTo Step *Same howTo as in request* | `Help.provide` | `HowToStepNotAsInRequest` |
| ingredient *same ingredients as in request* | `Help.provide` | `IngredientNotInRequest` |
| *A cook can answer their own request — except for Chef requests* | `Help.provide` | `CannotAnswerOwnChefRequest` |
| substitute **1..\*** | `Substitutes` | `RequiredTermMissing` |
| course **1..10** (named constants) | `MenuProposal` | `CourseCountOutOfRange` |
| *different courses can have the same step when served in parallel* | deliberately **no** uniqueness check on `dish` | — |
| images **0..\***, how to serve **0..1**, recipe on catastrophe mitigation **0..1** | constructors | — |
| isAccepted — accept once, only while the request accepts help | `Help.accept` | `HelpAlreadyAccepted`, `HelpRequestClosed`, `HelpBelongsToOtherRequest` |

### Rules added by inference (not drawn — confirm)

| Rule | Where | Why |
|---|---|---|
| Substitute ingredient *Value* > 0 | `SubstituteIngredient` | A quantity of zero is not an amount |
| *servings* ≥ 1 | `MenuProposal` | A menu for nobody |
| *dish* ≥ 1 | `Course` | Read as a serving step |
| Help can't be provided to a Closed request | `Help.provide` | Follows from "Answered … can be still answered", read as "Closed cannot" |

---

## External references

| Term | Owning context | Held as |
|---|---|---|
| requester, help requester, help provider | Cook Profile | `CookId` |
| recipe | Recipe Catalogue (not on any board yet) | `RecipeId` |
| howTo Step | Recipe Catalogue | `HowToStepId` |
| ingredients / ingredient | Recipe Catalogue | `IngredientId` |

---

## Open questions for a domain expert

1. **Entity or value object?** The legend marks *answer*, its four branches and
   *course* as entities, but none has an id. Will anyone ever change one part
   of an answer while it stays "the same" answer, or refer to one course from
   outside the menu? If yes, they need ids and become inner entities.
2. **Self-answering contradicts the earlier invariants sheet.** This glossary
   says *"A cook can answer their own request — except for Chef requests"*;
   `invariants-community-cooking-board.md` (INV-HELP-03) has *no self-help* at
   all. The code follows the glossary. Which is right?
3. **What is a "Chef request"?** Read as *preferred Provider = Chef* (which,
   by the other rules, also means type = Menu proposal). Does it instead mean
   *the Help is provided by a Chef*?
4. **Does "Chef help is provided exclusively" also mean only a Chef may answer
   a Chef request?** Today any provider type may answer it; only the
   request-side exclusivity is enforced.
5. **Must a Help's provider type be one of the request's preferred providers?**
   Not drawn, not enforced.
6. **Closed (marked !).** Who closes a request, and can an *Open* request be
   closed without any accepted Help (withdrawn)? Both are currently allowed.
7. **isAccepted (marked !).** Who may accept — only the requester? Can an
   acceptance be withdrawn? Can several Helps on one request be accepted?
   (Today: anyone holding the aggregate, never withdrawn, several allowed.)
8. **dish (marked !).** Is *dish* the serving step (example "2"), a dish
   count, or a position in the menu? The parallel-serving rule suggests a step.
9. **howTo Step for Preparation Step Explanation.** The request draws it 0..1,
   but the answer must carry "the same howTo" with cardinality 1 — so a request
   without one can never be answered. Should *howTo Step* be mandatory for that
   type? (Same question for at least one *ingredient* on an Ingredient
   Substitute request.)
10. **help provider 0..1.** Is it absent exactly when the Grandma Avatar
    answers? If so, that could be enforced.
11. **help requester** duplicates the request's requester. Kept as drawn
    (copied at creation); is it needed on Help at all?
12. **course 1..10** — is ten a business rule or a UI limit?
13. **Spelling:** the Help glossary says *answerType* and *help provider type*;
    the Help Request glossary says *type* and *preferred Provider* for the same
    value sets. One Java type each (`AnswerType`, `HelpProviderType`) — which
    names should the ubiquitous language keep? Also *"porovided"* on the Chef
    sticky is read as *provided*.
14. **Inferred rules** in the table above (Value > 0, servings ≥ 1, dish ≥ 1) —
    confirm or drop.
