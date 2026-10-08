# Prompt 2026-10-04

/devils-advocate
Critique Recipe Catalog Visual Glossary

![](../../../Chapter12/assets/ApiProductCanvas/RecipeCatalogRecipeVisualGlossary.jpg)

# Answer

My strongest objection: **the glossary makes several product rules look settled through cardinalities, while leaving their meaning unresolved.** If implemented literally, it could reject ordinary recipes and accept ambiguous quantities.

These are the challenges I would raise, based on the image:

| Challenge | Evidence and risk | What would resolve it |
|---|---|---|
| **What does “ingredient” identify?** | The ingredient entity combines `ingredientId`, `Name`, `Value`, and `Unit`. Is its ID for flour as a catalog item, or for this recipe’s particular use of flour? A recipe can use flour twice—for dough and dusting—with different quantities. | Define **Recipe Ingredient** as a recipe line; distinguish its identity from any shared ingredient reference. Show a repeated-ingredient example. |
| **Does every quantity fit a number and one unit?** | Exactly one `Value` and `Unit` cannot naturally express “salt to taste,” “2–3 apples,” or “one 400 g can.” Cup and pint also need a measurement convention if conversion is supported. | Decide which quantity forms are supported. Treat amount and unit together as a quantity concept, with explicit rules for approximate, ranged, and unspecified amounts. |
| **Why exactly one meal and one diet?** | A recipe can serve breakfast and lunch. Vegan and vegetarian overlap unless these are deliberately exclusive classifications. “Normal” could mean unrestricted, unclassified, or something else. | Clarify whether these are tags, exclusive categories, or derived properties. Rename “normal” to its intended meaning and distinguish unknown classification from unrestricted diet. |
| **Are these requirements for drafts or published recipes?** | `1..*` ingredients and steps, plus required preparation time, meal, diet, and servings, prevent an incomplete recipe from existing under the shown rules. | State that only complete recipes are modeled, or distinguish draft requirements from publication requirements. |
| **Where are the aggregate’s consistency rules?** | The legend includes invariants, but none are shown. Cardinalities alone do not explain valid servings, step ordering, or quantity constraints. | Add concrete rules: for example, positive servings and unique step sequence numbers within a recipe. Decide whether sequence gaps are allowed and how reordering behaves. |
| **What does preparation time measure?** | `02:00` could mean two minutes or two hours. It is also unclear whether it includes cooking, resting, or overnight waiting. | Define duration notation and whether the field means active preparation or elapsed time. Add separate durations only if the product needs them. |

**The modeling notation also deserves a challenge.** Green boxes represent identifiers, text fields, and choices such as “vegan” and “Cup.” The `is` arrows could mean enum membership or specialization. That ambiguity matters if this glossary will drive API or implementation work. State the intended relationship explicitly; showing a field as a value object should reflect domain meaning and validation.

The strongest case for keeping this model simple is that it serves a small catalog with deliberately constrained recipe entry. That is defensible—but those constraints should be explicit product choices. The image alone does not establish them.

**My cheapest confidence test:** model three recipes without workarounds:

- A draft with only a name.
- A vegan dish suitable for breakfast and lunch, containing “salt to taste.”
- A baking recipe using flour in two places, a packaged ingredient, and overnight resting.

If those are outside scope, document that. If they are inside scope, I would resolve ingredient identity, quantity semantics, and classification cardinalities before using this glossary as an implementation contract.