# Prompt 2026-10-04

/devils-advocate
Critique the Visual Glossary of  Meal Planning

![](../../../Chapter12/assets/ApiProductCanvas/MealPlanningMealPlanVisualGlossary.jpg)

# Answer

**My strongest objection: “meal” means two different things in this glossary.** At plan level, it means breakfast/lunch/dinner/supper. Inside a course, it means something linked to a recipe. That ambiguity could carry directly into conversations, APIs, and implementation.

I could read the attached image. These are the challenges I would raise, in priority order:

| Challenge | Why it matters | What would resolve it |
|---|---|---|
| **What exactly is a Meal Plan?** | The diagram describes one occasion with ordered courses. “Meal Plan” could also mean a week of breakfasts, lunches, and dinners. Those are different concepts. | Explicitly define its scope. If it represents one eating occasion, consider **Planned Meal**; reserve **Meal Plan** for a collection if needed. |
| **“Meal” has two meanings.** | “Dinner” classifies an eating occasion; “lasagne” identifies a dish. Using the same term obscures that distinction. | Consider **Meal Type** for breakfast/dinner and **Dish** for what a course contains. Validate those words with domain users. |
| **Does one course really contain exactly one recipe-backed dish?** | A main course might include roast chicken, potatoes, and salad. A course could also be purchased food or leftovers without a recipe. | Walk through those examples. Decide whether a course contains multiple dishes and whether recipe references are optional. Keep the current restriction only if deliberate. |
| **Six servings of what?** | Six diners does not necessarily mean six portions of every dish. Alternative mains, shared sides, and leftovers break that assumption. | Define whether plan-level “servings” means guest count or a target portion count. Establish dish-level quantities or allocation rules before deriving ingredients. |
| **The entity and value-object labels need justification.** | Course is marked as an entity, but its identity is not shown. Recipe is green and illustrated with a UUID, which looks more like a reference than a whole recipe value object. | Ask whether a course remains the same course when reordered or its dish changes. Label references explicitly—such as **Recipe ID** and **Owner ID**—where that is what the diagram means. |
| **The constraints appear more settled than the evidence shown.** | Why must an occasion be present? Why 1–10 courses? Can someone save an empty draft? The purple statement permits shared steps but does not establish the ordering rules. | Distinguish business requirements from provisional limits. Define validity for drafts and ready-to-use plans, plus the meaning and allowed values of “step.” |

**The pink ingredient-calculation question exposes a missing decision.** A recipe reference and a plan-wide serving count are not sufficient by themselves. Calculation needs a recipe’s base yield, ingredient quantities and units, and the intended quantity of each dish. Also decide whether editing a recipe should change an existing plan’s calculated ingredients.

A useful stress test would be:

> Six people are coming for dinner. Two want a vegetarian main. Everyone shares sides. Dessert is bought. The mains are served together, and one recipe changes after planning.

Can the glossary express that scenario without stretching the meanings of *meal*, *course*, *step*, or *servings*?

I would first resolve the plan’s scope and the overloaded “meal” term, then redraw this example. If multi-dish courses, purchased food, or dietary alternatives are intentionally outside scope, document those exclusions—the simpler model may be entirely appropriate.