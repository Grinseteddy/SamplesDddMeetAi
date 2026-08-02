# Visual Glossary — Help context

Updated from the workshop glossary to match `Help(4).yaml`.

## Removed

- **Meal** and the edges `Cook —prepares→ Meal`, `Help Request —belongs→ Meal`, and `Meal —with→ Recipe`. The schema contains no meal aggregate; the help request itself carries the cooking situation.
- **Substitute** and **Ingredient Substitute**. `HelpIngredients.substitutes` is an array of `Ingredient`, so substitutes are represented directly as ingredients with cardinality `1..3`.
- **Preparation Step Explanation**. The answer text is represented by `Help.helpDescription`, lifted to the business term **Help Text**.
- The two mandatory request-target edges to **Community** and **Grandma Avatar**. The request schema has no provider target; provider information appears on `Help.helpProviderType`.
- Identifier stickies and technical constraints. UUIDs, formats, patterns, discriminators, and examples are intentionally abstracted away.

## Changed

- **Meal Preparation Catastrophe**, **Help for Meal Preparation Step**, and **Help with Ingredients** were reshaped into **Catastrophe Help Request**, **Step Help Request**, and **Ingredients Help Request**, each with an `is-a` relationship to **Help Request**.
- The corresponding response concepts were reshaped into **Catastrophe Help**, **Step Help**, and **Ingredients Help**, each with an `is-a` relationship to **Help**.
- **Steps to Mitigate Catastrophe** became singular **Mitigation Step**, with cardinality `1..10`, and now exposes **Step Number**, **Step Text**, and **Ingredient** from `MitigationStep`.
- **Ingredient** moved into the owned Help context because the schema now describes it by value with **Name**, **Quantity**, and **Unit**.
- **Grandma Avatar** and **Community** are represented as the values of one **Help Provider** term, matching `Help.helpProviderType` and preserving the exclusive choice.
- Foreign-key fields became relationships: `cook` became `Cook —posts→ Help Request`; `helpRequestIdentifier` became `Help —for→ Help Request`; `helpingCook` became `Help —given by→ Cook`; and `Thanks.help` became `Thanks —for→ Help`.
- Picture arrays became `contains 0..10 Picture` relationships on **Help Request**, **Help**, and **Thanks**.

## Added

- **Question** from `HelpRequest.question`.
- **Help Text** from `Help.helpDescription`.
- **Thank Text** from `Thanks.thankText`.
- **Help Provider** from the domain-valued enum `Help.helpProviderType`.
- **Mitigation Step**, **Step Number**, and **Step Text** from `HelpCatastrophe.mitigationSteps` and `MitigationStep`.
- **Name**, **Quantity**, and **Unit** from the locally described `Ingredient` value object.
- Referenced-context terms **Recipe** and **Step**, with dashed cross-context relationships.

## Kept despite schema silence

- **Cook** remains an actor rather than a UUID-shaped attribute.
- Inverse cardinalities are retained where the glossary needs the business-facing direction, such as `Cook —posts→ 0..* Help Request` and `Cook —posts→ 0..* Thanks`.
- **Recipe** and **Step** remain visible as referenced concepts even though their structure is not described in this schema.

## What the redraw exposed

- **Step Help is structurally empty.** `HelpStep` adds only the subtype discriminator and no business content beyond `Help`. This raises the question whether it is still a distinct domain concept.
- **Community is demoted to an enum value.** The schema no longer models community membership or community behavior. The old `Community —contains→ Cook` relationship cannot be derived from the schema and should be confirmed separately if it remains a deliberate business fact.
- `Help.helpProviderType` is optional in the schema, and the updated glossary therefore shows `0..1 Help Provider`. This allows help whose provider has not been recorded.
