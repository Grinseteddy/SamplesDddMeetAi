# Conformance Report — `Help.yaml` × Grandma Visual Glossary

## 0. Scope checked

| | |
|---|---|
| Schema | `Help.yaml` — an OpenAPI `components.schemas` fragment, 13 types, no `paths` |
| Glossary | `VisualGlossaryGrandma.jpg` — 15 terms, one colour, no lanes or groups |
| Parser run | `inspect_schema.py --inventory` → 1 mechanical finding (`ORPHAN`, minor) |
| **Shipped or draft?** | **Assumed draft.** Not confirmed. If data is already stored behind this, F1 and F3 get more expensive and the resolution direction on F8 flips. |
| **Which is newer?** | **Assumed the schema.** It carries decisions the picture never faced (a Help subtype family, unit conventions, hard bounds). On that prior, most findings below resolve as *the glossary moves*. |

**UNCHECKED:** the picture uses a single sticky colour throughout, so I have **no legend for bounded contexts**. I read `Meal`, `Recipe`, and the implied `Preparation Step` as candidates for a neighbouring Recipe context, but that is my inference, not the glossary's claim. Several findings below (F8, F13) change severity depending on your answer.

### Transcription used

Terms: Cook · Community · Grandma Avatar · Meal · Recipe · Help Request · Help · Thanks · Picture · Meal Preparation Catastrophe · Help for Meal Preparation Step · Help with Ingredients · Ingredient Substitute · Preparation Step Explanation · Steps to Mitigate Catastrophe

Edges (fan-outs split, per the interpreter's fan-out rule):

```
Cook —prepares→ 0..* Meal            Meal —with→ 1 Recipe
Cook —posts→ 0..* Help Request       Cook —posts→ 0..* Thanks
Cook —to→ 0..1 Community             Cook —to→ 0..1 Grandma Avatar
Community —contains→ 1..* Cook
Help Request —at→ 1 Community        Help Request —at→ 1 Grandma Avatar
Community —provides→ 0..* Help       Grandma Avatar —provides→ 0..* Help
Help —for→ 1 Help Request            Help Request —belongs→ 1 Meal
Thanks —for→ 1 Help
Help Request —contains→ 0..* Picture
Help —contains→ 0..* Picture         Thanks —contains→ 0..* Picture
Meal Preparation Catastrophe —is→ Help Request
Help for Meal Preparation Step —is→ Help Request
Help with Ingredients —is→ Help Request
Help —contains→ 0..* Ingredient Substitute
Help —contains→ 0..* Preparation Step Explanation
Help —contains→ 0..* Steps to Mitigate Catastrophe
Ingredient Substitute —belongs to→ 1 Help with Ingredients
Preparation Step Explanation —belongs to→ 1 Help for Meal Preparation Step
Steps to Mitigate Catastrophe —belongs to→ 1 Meal Preparation Catastrophe
```

---

## 1. Verdict

**Aligned with drift, tilting toward diverged on the answer side.**

The request half of the model matches well: three glossary `is-a` edges, three `allOf` subtypes, a discriminator, correct `1` links from `Help` and `Thanks`. The answer half has moved on without the picture — the schema grew a whole second subtype family the glossary models as composition — and two structural decisions (how a preparation step is referenced, where a request records who it was addressed to) were made in code and never drawn.

| Severity | Count |
|---|---|
| Blocking | 3 |
| Significant | 8 |
| Minor | 5 |
| Allowlisted (not counted) | 6 classes |

Of the 16 findings, **11 resolve as the glossary moves.**

---

## 2. Mapping ledger

| Glossary term | Schema construct | Kind | Status |
|---|---|---|---|
| Cook | `cook`, `helpingCook`, `thankingCook` (uuid) | actor → identifier, 3 roles | ✅ (see F14) |
| Community | `helpProviderType: COMMUNITY` | term → enum value | ⚠️ F16 |
| Grandma Avatar | `helpProviderType: GRANDMA_AVATAR` | term → enum value | ⚠️ F16 |
| Meal | — | unmatched | ⚠️ **F8** |
| Recipe | `recipe` (uuid, on subtypes only) | reference by identity | ⚠️ F8 |
| Help Request | `HelpRequest` | exact | ✅ |
| Help | `Help` | exact | ✅ |
| Thanks | `Thanks` | exact | ✅ |
| Picture | `Picture` (uri-reference) | leaf term → scalar | ✅ (F15) |
| Meal Preparation Catastrophe | `HelpRequestCatastrophe` | synonym | ⚠️ F6 |
| Help for Meal Preparation Step | `HelpRequestStep` | synonym | ⚠️ F6 |
| Help with Ingredients | `HelpRequestIngredients` | synonym | ⚠️ F6 |
| Ingredient Substitute | `HelpIngredients.substitutes: [Ingredient] 1..3` | role-of `Ingredient` | ⚠️ F7, F12 |
| Preparation Step Explanation | `HelpStep` — no own field; text lands in `helpDescription` | structurally subsumed | ⚠️ **F10** |
| Steps to Mitigate Catastrophe | `HelpCatastrophe.mitigationSteps: [MitigationStep] 1..10` | plural collection | ⚠️ F4, F13 |

### Below the divider — schema-only constructs

**Domain concepts missing from the glossary** (these need terms):

| Construct | What it is |
|---|---|
| `Ingredient` (`name`, `quantity`, `unit`) | The base noun. The glossary only has its *role* (`Ingredient Substitute`), never the thing itself. |
| `HelpIngredients` / `HelpStep` / `HelpCatastrophe` | Three `Help` subtypes with a discriminator. The glossary draws no `is-a` under `Help` at all. |
| `MitigationStep` (`sequence`, `ingredient`, `stepDescription`) | A structured step. The glossary has one undivided sticky. |
| `recipeStepNumber` | A reference to a preparation step. The glossary has no `Preparation Step` term. |
| `helpProviderType` | The avatar-vs-community distinction, as data. |
| `question`, `helpDescription`, `thankText`, `stepDescription` | The prose. Four required text fields, no terms. |

**Allowlisted technical constructs:** all `*Identifier` / uuid fields · `format`, `minLength`, `maxLength`, `pattern`, `examples` · discriminator plumbing · casing conventions.

---

## 3. Findings

### Blocking

**F1 · `REF` · `recipeStepNumber` is a positional reference into an editable collection**
Evidence: `HelpRequestStep.recipeStepNumber`, `type: string, pattern: ^\d+\.$`, required. Glossary: `Help for Meal Preparation Step —is→ Help Request`, no step term drawn.
The stability question: *does `"2."` still mean the same thing after the recipe is edited?* If a cook inserts a step, every stored request pointing at `"2."` silently re-aims at a different instruction — and these requests are stored and read back by whoever answers them. Grandma answers about the wrong step, and nothing in the data shows it happened.
→ **Schema moves** (a step identity, or the step text by value so the request is self-describing), **and the glossary moves** — it needs a `Preparation Step` term with `Recipe —contains→ 1..* Preparation Step` so the reference has something to point at.

**F2 · `CARD` · The request never records who it was addressed to**
Evidence: glossary `Help Request —at→ 1 Grandma Avatar` and `—at→ 1 Community` (two `1` edges from one term — almost certainly an XOR the picture cannot express). Schema: nothing on `HelpRequest`. The only counterpart is `Help.helpProviderType`, which sits on the **answer** and is **optional**.
The glossary says routing is a mandatory property of the request. The schema says it is an optional property of the response — so an unanswered request carries no record of where it was sent, and there is no way to ask "what is queued for the community?"
→ **Both move.** Add the addressee to `HelpRequest` as a required `oneOf`/enum; draw the exclusivity explicitly in the picture (`Help Request —addressed to→ 1 Help Provider`, with `Grandma Avatar` and `Community` as the two `is-a` children).

**F3 · `CARD` · Every mitigation step must name an ingredient**
Evidence: `MitigationStep.required: [sequence, ingredient, stepDescription]`. Glossary: silent.
"Turn the oven down." "Cut off the burned parts." "Wait ten minutes." None of these has an ingredient, and the schema rejects all of them. The file's own worked example survives only because butter happens to appear in it.
→ **Schema moves:** drop `ingredient` from `required`. If it really is mandatory, that is a business rule and belongs on the picture.

### Significant

**F4 · `CARD` · `0..*` against `1..10`**
`Help —contains→ 0..* Steps to Mitigate Catastrophe` vs `mitigationSteps: minItems: 1, maxItems: 10`, required. Two disagreements: the schema forbids the empty case the glossary permits, and it caps at ten, which nobody drew. Ask what happens at eleven — if the answer is "nothing, we picked a number", say so in a description.
→ **Glossary moves** on the minimum (`1..*` is almost certainly what was meant); **ask** on the maximum.

**F5 · `SUB` · Asymmetric subtype family — the schema subtypes both sides, the glossary only one**
The glossary draws three `is-a` edges into `Help Request` and then models the three answer variants as `Help —contains→ 0..*`. The schema has a full mirror family: `HelpIngredients`, `HelpStep`, `HelpCatastrophe`, each `allOf` on `Help`, with a `helpType` discriminator.
This is the finding that is invisible when you look at either side alone. Composition and specialization are different claims: composition says one Help could carry substitutes *and* mitigation steps; the schema says it is exactly one kind.
→ **Glossary moves.** Redraw the three lower-right terms as `is-a` children of `Help`, and hang their payloads off those children.

**F6 · `TERM` · "Help" means two things in the glossary**
`Help with Ingredients` and `Help for Meal Preparation Step` are drawn as children of **Help Request**, not of Help. Read the picture cold and you will attach them to the wrong parent — I nearly did. The schema is unambiguous (`HelpRequestIngredients` vs `HelpIngredients`).
→ **Glossary moves.** Rename the request-side terms so the word "Help" is reserved for answers: *Ingredient Question*, *Preparation Step Question*, *Catastrophe Call* — or mirror the schema exactly (*Help Request — Ingredients*). Note the third sibling, `Meal Preparation Catastrophe`, doesn't say "help" at all, so the family is already inconsistent internally.

**F7 · `SENSE` · `Ingredient` in three roles, with a description that names one**
`Ingredient` is used as: the ingredient the cook has run out of (`HelpRequestIngredients.ingredient`), the replacements offered (`HelpIngredients.substitutes`), and something to add during a rescue (`MitigationStep.ingredient`). Its description still reads "An ingredient of a recipe" — true only of the first.
The glossary has the opposite gap: it names the role (`Ingredient Substitute`) and never the base noun. Neither artifact says these are one shape in three roles.
→ **Glossary moves** (add `Ingredient` with `name`/`quantity`/`unit`, and draw `Ingredient Substitute` as a role of it, not a separate thing). **Schema moves** on the description — generalize it and let the *field* names carry the role.

**F8 · `MISS` · `Meal` has no counterpart; the schema anchors on `Recipe` instead**
Glossary: `Cook —prepares→ 0..* Meal`, `Meal —with→ 1 Recipe`, `Help Request —belongs→ 1 Meal`. Schema: no meal anywhere; requests carry `recipe` (uuid) — and only on the subtypes, so the base `HelpRequest` has no link to what is being cooked at all.
This is a real modelling decision: the schema treats the *cooking occasion* as the request itself and skips the meal. Defensible — but it means two requests about the same dinner are unlinked, and the picture still claims otherwise.
→ **Glossary moves**, most likely: mark `Meal` and `Recipe` as a neighbouring context, referenced not modelled, and redraw the anchor as `Help Request —about→ 0..1 Recipe`. If `Meal` genuinely needs to exist (to group a session's requests), that is a schema change and worth deciding now.

**F9 · `DUP` · `helpType` restates the request's `helpRequestType`**
`Help —for→ 1 HelpRequest` (via required `helpRequestIdentifier`), and both types carry the same three-value enum. The value is derivable, and nothing prevents a `STEP` help answering a `CATASTROPHE` request.
The duplication is structurally *necessary* — OpenAPI discriminators need a local property — so this isn't a deletion. But it should be documented as derived, and validated server-side.
→ **Schema moves:** description on `helpType` saying it must equal the referenced request's type.

**F10 · `MISS` · `Preparation Step Explanation` flattened into `helpDescription`**
The glossary gives it its own term and a `0..*` cardinality; the schema gives `HelpStep` no fields of its own at all, so the explanation lands in the base's single `helpDescription` string. Compare the sibling: `HelpCatastrophe` got a structured `MitigationStep[]`. One answer type is structured, the equivalent one isn't.
Prose is fine when only humans read it — but ask what will consume this. If the app ever wants to render step-by-step guidance the way it renders mitigation steps, this is the field that blocks it.
→ **Ask.** Either the schema gains structure, or the glossary drops the term and says the explanation is prose.

**F11 · `EXTRA` · The prose has no terms**
`question` (required, 5–2048), `helpDescription` (required), `thankText` (required), `stepDescription` (required). Four mandatory text fields and not one sticky. This is the standard workshop blind spot — the room draws structure and forgets the payload is mostly words.
→ **Glossary moves.** Cheap, and it stops this recurring next revision.

### Minor

**F12 · `CARD` · `substitutes: maxItems: 3`** — a business rule ("we show at most three alternatives"?) that appears only in the schema. Probably a screen limit that hardened into a contract. Ask what happens at four.

**F13 · `MISS`/`EXTRA` · Structure inside `MitigationStep`** — `sequence`, `ingredient`, `stepDescription` are an ordered, structured thing behind one undivided glossary sticky. Add the attributes, or add a `Mitigation Step` term with `Steps to Mitigate Catastrophe —contains→ 1..* Mitigation Step`.

**F14 · Mixed identity conventions** — `helpRequestIdentifier` / `helpIdentifier` / `thanksIdentifier` alongside bare `cook`, `recipe`, `help`, `helpingCook`, `thankingCook`. Also `HelpRequest.helpRequestIdentifier` is **not** in `required` while `Help.helpIdentifier` is — so a `Help` can name a request identifier the request payload never carries. Glossary-independent; pick one suffix rule.

**F15 · `REF` · `Picture` is a relative uri-reference** resolved against a media base URL "published in `servers` / configuration" — which isn't in this file. Self-consistent, but the resolution rule lives outside the contract. Glossary treats `Picture` as a term, which is fine.

**F16 · `helpProviderType` optional, `helpingCook` optional** — the glossary says `Community —provides→ Help` and `Grandma Avatar —provides→ Help`. Community help comes from a *cook*; avatar help doesn't. So `helpingCook` should be required exactly when `helpProviderType` is `COMMUNITY`, and `helpProviderType` should be required outright. Currently both are optional and a help can come from nobody.

### `DEFECT` (schema-internal, glossary-independent)

**`ORPHAN`** (minor) — `HelpRequestStep`, `HelpRequestCatastrophe`, `HelpRequestIngredients`, `HelpIngredients`, `HelpStep`, `HelpCatastrophe`, `Thanks` are referenced by nothing in this file. Expected for a `components`-only fragment whose entry points live in `paths` elsewhere — worth a confirming glance, no action.

Nothing else: no broken `$ref`, no `required:` parsed as null, no example failing its own pattern, no `x-extensible-enum` typo, patterns correctly escape the dot, discriminator mappings all resolve. **The file is mechanically clean** — unusually so.

---

## 4. Relationships & cardinalities

| Glossary relationship | Card. | Schema realization | Verdict |
|---|---|---|---|
| Help —for→ Help Request | 1 | `Help.helpRequestIdentifier`, required | ✅ consistent |
| Thanks —for→ Help | 1 | `Thanks.help`, required | ✅ consistent |
| Cook —posts→ Help Request | 0..* | `HelpRequest.cook`, required (inverse) | ✅ (inverse not expressible) |
| Cook —posts→ Thanks | 0..* | `Thanks.thankingCook`, required (inverse) | ✅ |
| Help Request —contains→ Picture | 0..* | `pictures`, `0..10` | ✅ (bound added — F12 class) |
| Help —contains→ Picture | 0..* | `pictures`, `0..10` | ✅ |
| Thanks —contains→ Picture | 0..* | `pictures`, `0..10` | ✅ |
| 3 × `is-a` → Help Request | — | `allOf` + discriminator | ✅ consistent |
| Ingredient Substitute —belongs to→ Help with Ingredients | 1 | `HelpIngredients.substitutes` (containment) | ✅ realized as ownership |
| Steps to Mitigate Catastrophe —belongs to→ Meal Preparation Catastrophe | 1 | `HelpCatastrophe.mitigationSteps` | ⚠️ owner is the *Help*, not the *request* |
| Preparation Step Explanation —belongs to→ Help for Meal Preparation Step | 1 | — | ❌ absent (F10) |
| Help —contains→ Steps to Mitigate Catastrophe | 0..* | `1..10`, required | ❌ contradicted (F4) |
| Help —contains→ Ingredient Substitute | 0..* | `1..3`, required | ❌ contradicted (F12) |
| Help —contains→ Preparation Step Explanation | 0..* | — | ❌ absent (F10) |
| Help Request —at→ Community / Grandma Avatar | 1 / 1 | — (only `Help.helpProviderType`, optional) | ❌ contradicted (F2) |
| Cook —to→ Community / Grandma Avatar | 0..1 | — | ❌ absent (F2) |
| Community —provides→ Help | 0..* | `helpProviderType: COMMUNITY` | ⚠️ flattened to enum (F16) |
| Grandma Avatar —provides→ Help | 0..* | `helpProviderType: GRANDMA_AVATAR` | ⚠️ flattened to enum (F16) |
| Help Request —belongs→ Meal | 1 | — (`recipe` on subtypes instead) | ❌ absent (F8) |
| Cook —prepares→ Meal | 0..* | — | ➖ allowlisted |
| Meal —with→ Recipe | 1 | — | ➖ allowlisted (F8 context question) |
| Community —contains→ Cook | 1..* | — | ➖ allowlisted |

---

## 5. Allowed abstraction — what I deliberately did not report

1. **Identifiers and formats.** Every `*Identifier`, `format: uuid`, `minLength`, `maxLength`, `pattern`, `examples`. Identity is an implementation concern; a glossary that names identifier terms is the exception.
2. **`Cook` not being a type.** It appears as three uuid fields and never as an object. That is correct: an actor that only *acts* belongs in the picture and not in the payload.
3. **`Community —contains→ 1..* Cook`.** A relationship between two things this API doesn't own. Membership lives elsewhere.
4. **Inverse multiplicities.** `Cook —prepares→ 0..* Meal`, `Cook —posts→ 0..* Help Request` — a payload schema cannot express "how many per cook", so the schema's single `cook` field is the faithful realization, not a contradiction.
5. **Casing and separators.** `Help Request` ↔ `HelpRequest` ↔ `helpRequestIdentifier`. Not domain language.
6. **The unit enum.** `CUPS`, `TABLESPOONS`, `TEASPOONS`, `FLUID_OUNCES` are ambiguous across regions — a US cup is 240 ml, a metric cup 250 — but the schema's description **already pins the convention and says where to prefer grams**. That is exactly the right treatment; keeping the informal unit is correct, because it is the vocabulary cooks use. Worth protecting if a Recipe context ever defines its own unit list: those two enums must be compared, and will drift silently if nobody owns them.

Also not reported: the subtypes' `required:` blocks sitting outside `allOf`. The inventory flattens this in a way that *looks* like `cook` and `question` became optional in the subtypes. They didn't — `allOf` composition preserves the base's `required`. No finding.

---

## 6. Patch list

### Edits to the glossary — *where the picture needs to be adapted*

1. **Add three `is-a` children under `Help`** — mirroring the request family — and hang `Ingredient Substitute`, `Preparation Step Explanation`, `Steps to Mitigate Catastrophe` off them instead of off `Help` directly. *(F5)*
2. **Rename the request-side terms** so "Help" means only an answer: `Help with Ingredients` → *Ingredient Question*, `Help for Meal Preparation Step` → *Preparation Step Question*. *(F6)*
3. **Add `Ingredient`** with `name`, `quantity`, `unit`; redraw `Ingredient Substitute` as a **role of** `Ingredient`, not a sibling concept. *(F7)*
4. **Add `Preparation Step`**, with `Recipe —contains→ 1..* Preparation Step`, so the step reference has a target. *(F1)*
5. **Add a `Help Provider` term** with `Grandma Avatar` and `Community` as `is-a` children, and redraw the addressee as one exclusive edge `Help Request —addressed to→ 1 Help Provider` — the current two `1` edges cannot express the XOR. *(F2, F16)*
6. **Add the prose terms**: `Question` on Help Request, `Help Description` on Help, `Thank Text` on Thanks, `Step Description` on the mitigation step. *(F11)*
7. **Add `Mitigation Step`** as its own term under `Steps to Mitigate Catastrophe`, with `sequence` and `stepDescription`. *(F13)*
8. **Fix `Help —contains→ 0..* Steps to Mitigate Catastrophe`** to `1..*`. *(F4)*
9. **Decide `Meal`**: either mark `Meal` and `Recipe` as a neighbouring context (referenced, not modelled) and redraw the anchor as `Help Request —about→ 0..1 Recipe`, or keep `Meal` and make it a schema change. *(F8)*
10. **Introduce a colour or lane for the Recipe context** — `Recipe`, `Meal`, `Preparation Step`, `Ingredient` — so the next check can tell a missing type from a correctly-referenced one without asking.
11. **Record the bounds** the schema found first: at most 3 substitutes, at most 10 mitigation steps, at most 10 pictures. *(F12)*

### Edits to the schema

1. `HelpRequestStep`: replace `recipeStepNumber` (positional) with a step identity, or carry the step text by value. *(F1)*
2. `HelpRequest`: add a required addressee (`GRANDMA_AVATAR` | `COMMUNITY`). *(F2)*
3. `MitigationStep`: drop `ingredient` from `required`. *(F3)*
4. `Ingredient`: generalize the description — it is used for a shortage, a substitute, and a rescue addition. *(F7)*
5. `Help`: make `helpProviderType` required; document that `helpingCook` is required when it is `COMMUNITY`. *(F16)*
6. `Help`: document `helpType` as derived from the referenced request's `helpRequestType`, and validate the match. *(F9)*
7. `HelpRequest`: make `helpRequestIdentifier` required, or state why the request may travel without one. *(F14)*
8. Settle the identity suffix convention across `cook` / `recipe` / `help` / `*Identifier`. *(F14)*
9. `HelpRequestCatastrophe`: `recipe` is optional here while its two siblings require it. Deliberate — a catastrophe may not follow a recipe — but say so in the description, or the asymmetry reads as an oversight. *(F8)*

---

## 7. Already aligned — protect this

- **The request subtype family.** Three glossary `is-a` edges, three `allOf` subtypes, a working `discriminator` with all three mappings resolving. Textbook.
- **The two `1` links that matter.** `Help —for→ 1 Help Request` and `Thanks —for→ 1 Help` are both required uuid fields, both correct, and neither side copies data from the other — no `DUP` in the chain. `HelpIngredients` is the sibling that got it right: it adds only `substitutes` and derives everything else through `helpRequestIdentifier`. Use it as the template.
- **Picture handling.** `0..*` in the glossary, `0..10` on all three carriers, one shared type, consistent everywhere.
- **The unit convention.** The `unit` description pins US measures to millilitres and says where to prefer grams. Most schemas don't, and the ones that don't produce the bug where a US cup meets a metric one.
- **Mechanical hygiene.** No broken refs, no null `required`, no example that fails its own pattern, dots escaped in both patterns. The file was linted or written carefully; keep whatever produced that.