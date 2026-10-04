# Prompt 2026-10-04

/devils-advocate
Critique the Visual Glossary of  Meal Planning

![](../../../Chapter12/assets/ApiProductCanvas/MealPlanningMealPlanVisualGlossary.jpg)

# Answer

I'll load the Devil's Advocate skill and then work through your Meal Planning glossary.

The board is a finished artifact, so I'll go straight to pressure-testing it. Here are the objections in order of how much I think they matter. Push back on any of them.

## 1. "meal" means two different things

This is my strongest objection. **meal** appears twice with two meanings:

- **Meal Plan → meal**: a *mealtime*. Its values are breakfast, lunch, dinner, supper.
- **course → meal → recipe**: the *dish* served in a course, the thing that points to a recipe.

A soup is not a "lunch." If the team says "change the meal," does that mean move it to dinner or swap the soup? A homonym inside one aggregate will leak into code, APIs and conversations.

**What would settle it:** rename one of them, for example *meal type* or *mealtime* for the first and *dish* for the second. Or show me that domain experts really use "meal" for both and can always tell from context.

## 2. Is this a plan, or a single menu?

A Meal Plan contains exactly **1** meal and has **no date or time**. "Plan" usually suggests several meals over days. This one describes one dinner for the parents-in-law, and you can't tell *when* it is.

There are two honest ways to read it:

- It really is one occasion. Then a name like *Menu* or *Occasion Menu* fits better, and a date is probably missing.
- It's meant to grow into several meals. Then the cardinality 1 is wrong today.

Also, "parents in law visiting" sounds like it might last a weekend. Would that be one plan with several meals, or several plans?

**What would settle it:** say which reading you mean and add the time anchor.

## 3. Course is an entity with no visible identity, and "step" is overloaded

course is the only entity, but nothing shows what identifies it. The obvious candidate, **step** (example: 2), can't be the identity, because your own invariant says two courses can share a step. So step is a *serving position*, not an identity.

The word "step" causes a second problem. In cooking, a step is a recipe instruction ("sear the onions"). Next to "recipe," readers will take it that way.

**What would settle it:** rename it to *serving order*, *position* or *sequence*, and give course its own local identity.

## 4. The purple sticky isn't really an invariant

"Different courses *can* have the same step when served in parallel" grants a permission. An invariant states what must always hold. The real rules seem to be hidden or missing:

- **1..10 courses** is a business rule hiding in a cardinality. Where does 10 come from? Is it a real limit, or a guess?
- Must serving positions start at 1? Are gaps allowed (1, 2, 4)?
- Does "served in parallel" need to be its own concept? Right now it's only implied by a duplicate number.

## 5. "how to serve" points at courses by position, in free text

"hold course 2 warm while serving soup" refers to a course by its number, inside a text string. If someone reorders the courses, the note quietly becomes wrong. It also names "soup," which exists nowhere in the glossary, since dishes are known only by recipe ID.

**What would settle it:** decide whether serving instructions belong on the course, or whether plan-level coordination notes are acceptable knowing they will go stale.

## 6. The pink hotspot can't be answered from this picture

"Can we calculate the ingredients based on servings?" The glossary only references recipe by ID, so it says nothing about a recipe's ingredients or its *base yield*. Scaling 6 servings needs to know what the recipe was written for, and that knowledge lives in another context. There are further questions too:

- Do all courses use the plan-level servings, or can a course differ (kids, a dessert only some people eat)?
- Many things don't scale linearly, such as baking times and spices.

**What would settle it:** decide which context owns the calculation. My bet is Recipe, or a separate shopping-list context, with Meal Plan only supplying the servings count.

## Smaller points, lower priority

- **owner** is shown as a value object holding a UUID. It is really a reference to a user in another context, and labelling it that way would make the boundary visible.
- **dinner vs supper** mean the same thing in many regions. Is the distinction real for your users, and is the list closed (no brunch, no snacks)?
- **course → 1 dish**: can a main course really have no side dish?
- **The pink sticky color** isn't in the legend. Add "hotspot/question" so readers know it isn't an invariant variant.

If you fix only one thing, make it #1. The homonym is the one that will spread into everything built from this glossary. #2 is a close second because it decides what the aggregate actually is. The rest are refinements you can make once those two are settled.
