# Prompt 2026-08-02

Update the provided glossary based on the provided schema. Use the provided skill.

![](../assets/VisualGlossaryGrandma.jpg)
[Schema](../code/Help.yaml)

# Answer

Schema is coherent — no blocking defects, so I can derive straight from it. Building the model now.I checked `Help.yaml` for coherence first — every `$ref` resolves, every `required` name exists, subtypes all add something. No blocking defects, so I derived directly. 26 terms, 35 edges, geometry clean after two iterations.

## Changelog

**Removed**

- **`Help Request —at→ 1 Community` and `—at→ 1 Grandma Avatar`.** `HelpRequest` carries only `cook`, `question` and `pictures` — nothing records who a request is addressed to. Only the answer records a provider. This also kills the old picture's bug: two mandatory `1` edges out of one term read as "every request goes to both".
- **`Grandma Avatar` as a term, and both `provides` edges.** `helpProviderType` is an enum, so it becomes one **Help Provider** term with the values as a subtitle — exclusive by construction, which the two stickies could never say.
- **`Ingredient Substitute`.** `HelpIngredients.substitutes` is an array of `Ingredient` — a substitute *is* an ingredient, drawn as `Ingredients Help —offers→ 1..3 Ingredient`.
- **`Preparation Step Explanation`.** No such type. The explanation is the base `helpDescription`, now `Help —explains in→ 1 Help Text`.
- **`Help —contains→ 0..*` × 3 and their `belongs to` inverses.** In the schema the content hangs off the *answer subtypes*, not off `Help`, and `Help —for→ 1 Help Request` (via `helpRequestIdentifier`) replaces the old edge pointing at a request subtype.
- **`Help Request —belongs→ 1 Meal`.** The subtypes now reference `recipe` directly.

**Changed**

- The three request subtypes renamed to pair 1:1 with the schema types (`Meal Preparation Catastrophe` → **Catastrophe Help Request**, and so on), so a reader can navigate from picture to type.
- `Steps to Mitigate Catastrophe` → **Mitigation Step**, singular, `1..10`, with its own content (`sequence`, `stepDescription`, `ingredient`).
- Pictures tightened from `0..*` to **`0..10`** on request, help and thanks (`maxItems: 10`).
- **Recipe** moved to the referenced colour — the schema knows it only as a uuid, with no shape. **Ingredient** sits in the owned colour instead, because `{name, quantity, unit}` describes it here.
- Identifiers became edges, not stickies: `cook` → `Cook —posts→ 0..*`, `helpingCook` → `Help —given by→ 0..1 Cook`, `help` on `Thanks` → `Thanks —for→ 1 Help`.

**Added**

- **Question**, **Help Text**, **Thank Text** — the text payloads the workshop drew structure around but never named.
- **Help Provider**, **Ingredient** with **Name / Quantity / Unit** (the ten-value enum as a subtitle), **Mitigation Step** with **Step Number / Step Text**, **Recipe Step** (from the `recipeStepNumber` pattern — an index into the recipe context, so a dashed edge).
- The answer side, previously one sticky, now splits three ways to mirror the requests.

**Kept despite absence** (grey, marked *not in Help.yaml*): `Community —contains→ 1..* Cook`, `Cook —prepares→ 0..* Meal`, `Meal —with→ 1 Recipe`. No help payload would ever carry these and nothing contradicts them. If the team has actually decided the meal is gone — that the cooking occasion *is* the request — say so and I'll drop all three.

## What the redraw exposed

**`Step Help` is an empty sticky.** `HelpStep` adds nothing to `Help` but a discriminator value. Either it isn't a distinct concept, or it's missing something the other two answers have. `Catastrophe Help Request` is nearly the same — one optional recipe.

**A `Help` can record neither who helped nor what kind of helper.** Both `helpProviderType` and `helpingCook` are optional, so the picture has two `0..1` edges where the old board had every answer coming from exactly one of two providers. That's a business rule that quietly disappeared.

**The recipe reference is duplicated across all three request subtypes** — required, required, optional — rather than sitting on the base. And the answer side references no recipe at all, so an answer is only tied to a recipe transitively.

**Every mitigation step must name an ingredient** (`required: [sequence, ingredient, stepDescription]`). "Turn the oven down" can't be expressed. Likely a `required` nobody argued about.

**Nothing points at the community any more** — it survives only as one of two provider values, which is why it's grey in the picture rather than gone.The model file is included so the next revision is an edit rather than a rebuild.

[Updated glossary SVG](./UpdatedGlossaryOpus5/glossary-updated.svg)
[Updated glossary PNG](./UpdatedGlossaryOpus5/glossary-updated.png)
[Glossary as yaml](./UpdatedGlossaryOpus5/grandma-glossary.yaml)