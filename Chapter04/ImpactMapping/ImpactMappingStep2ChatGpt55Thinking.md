# Prompt 2026-06-06:

Criticize the Impact Map based on the Brainstorming result and Business Model Canvas.
Use the provided skill.
![](../assets/ImpactMappingWithAi.jpg)
![](../assets/BrainstormingConvergence.jpg)
![](../assets/BusinessModelCanvasEnhanced.jpg)
[SKILL.md](../Skills/ImpactMappingSkill/SKILL.md)
[Canvas cross check](../Skills/ImpactMappingSkill/references/canvas-cross-check.md)
[Collaboration cross check](../Skills/ImpactMappingSkill/references/collaboration-cross-check.md)
[Coherence and tracibility](../Skills/ImpactMappingSkill/references/coherence-and-traceability.md)
[Four level checklist](../Skills/ImpactMappingSkill/references/four-level-checklist.md)

# Answer:

## One-line read

**In order to get “Larder” to 2,500 members in two years, singles / young families / cooks / partners will find recipes, get cooking support, shop ingredients/tools, plan meals, and publish/share content, supported by features like recipe search, AI proposals, planner, content, rating, ordering, timers, and cookbook publishing.**

The story mostly holds as a product vision, but it is still weak as an impact map: the goal is measurable, but many “impacts” are value propositions or feature themes rather than observable actor behaviour, and the map does not clearly identify the shortest path to the 2,500-member goal.

## Biggest risks first

### 1. The goal measures membership, but the map optimizes for many different values

“2,500 members in two years” is a useful measurable goal. The risk is that the branches underneath serve several different strategies at once: community recipe sharing, AI cooking support, grocery/tool ordering, budget planning, cooking education, and cookbook publishing.

That creates a focus problem. Which behaviour actually drives membership growth? Daily recipe use? Weekly content creation? Social sharing? Paid subscription? Partner-driven acquisition?

The Business Model Canvas points to **monthly member fee** and **ads by partners** as revenue streams, and to **daily sharing / weekly creating / community** as customer relationship mechanisms. The impact map should therefore be much more explicit about the behaviour that creates membership and retention, not just useful product capabilities. This is exactly the kind of goal-to-strategy alignment check recommended by the canvas cross-check skill: the map’s goal should advance the canvas’s value proposition and revenue logic, not drift into loosely related features.

**Cheap test:** before building more features, test which behaviour predicts sign-up or retention: saving recipes, sharing recipes, using AI help, planning meals, or inviting others.

**What would resolve it:** rewrite the first-level impacts as measurable behaviours, for example: “young families create a weekly meal plan,” “singles cook from Larder twice per week,” “cooks publish one recipe per month,” “members invite two friends.”

---

### 2. Several “impacts” are not impacts yet — they are product promises

Impact mapping asks: **How should the actor’s behaviour change?** A strong impact is something observable that an actor does differently, not a feature or value phrase.

In the map, these are weak as impacts:

| Current impact                      | Why it worries me                                  | More impact-like version                                  |
| ----------------------------------- | -------------------------------------------------- | --------------------------------------------------------- |
| “Find easily recipes”               | This describes product value, not actor behaviour. | “Users choose a recipe in under 2 minutes.”               |
| “Provide cooking support”           | Sounds like what the product does.                 | “Beginners complete recipes without abandoning them.”     |
| “Use effective limited budget”      | Close to behaviour, but vague.                     | “Young families plan meals within a weekly budget.”       |
| “Shop easily ingredients and tools” | Again, product promise.                            | “Users order missing ingredients directly from a recipe.” |
| “Use of community shared recipes”   | Awkward and not behavioural enough.                | “Members cook recipes shared by other members.”           |
| “Learn cooking”                     | Outcome, not observable behaviour.                 | “Beginners complete one guided recipe per week.”          |

The map currently reads partly like a **value proposition map** rather than an impact map. That matters because if the impact is not observable, you cannot validate whether a deliverable caused it. The skill’s coherence check says every branch should read: “In order to [goal], [actor] will [impact], and we will support that with [deliverable].”

---

### 3. The deliverables look like a backlog, not a set of experiments

The deliverables are numerous: direct ordering, kitchen tool ordering, cooking club, content, rating, meal chooser, AI proposals, timer, grandma/grandpa AI, planner, famous chefs partner, cookbook publishing.

That breadth is risky for a two-year membership goal. The brainstorming board is even broader, containing shopping, helper, creator support, cooking support, recipe search, planner, content rating, authentication, community, special/historical recipes, food pairing, allergy support, fridge-photo recipe generation, budget, and more. The impact map narrowed the field, but not enough. It still feels like “many plausible features” rather than “the smallest bet that validates the highest-leverage behaviour.”

Impact maps are meant to help find the **shortest path to the goal**, not to build every branch.

**Cheap test:** pick one actor and one behaviour for the first milestone. For example: “young families use a weekly meal planner and return next week.” Then test that with a concierge prototype or clickable prototype before building AI, ordering, clubs, and publishing.

**What would resolve it:** mark each deliverable as either **experiment now**, **later option**, or **cut unless evidence appears**.

---

## Level-by-level critique

### WHY / Goal

Strong point: the goal is concrete: **minimum 2,500 members in two years**.

Weakness: “members” is underspecified. Are these registered users, active monthly members, paying members, content contributors, or community participants? The canvas has a **monthly member fee**, so “member” probably needs to mean **paying member** or at least **active member likely to convert**.

Better goal candidate:

> “Reach 2,500 active registered members, with X% monthly retention and Y paying members, within two years.”

Without retention or payment, the team could hit 2,500 signups with weak business value.

### WHO / Actors

The map includes: singles, young families, cooks, kitchenware retailer, chefs, grocery store, photo book service.

Good: it includes both customer segments and partners, which matches the canvas.

Missing or underdeveloped:

| Missing actor                              | Why it matters                                                                                                                                                             |
| ------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Beginners / low-confidence cooks           | Strongly implied by “cooking support,” “grandma AI,” “step-by-step mode,” and “cooking skill learning” in the brainstorming.                                               |
| Content creators / recipe publishers       | The canvas has “weekly for creating,” and the brainstorming has “content creation,” “store recipes,” “remix of recipes,” and “making photos.”                              |
| Community moderators / trust & safety role | Recipe sharing, ratings, comments, and sponsor contests create quality and abuse risks.                                                                                    |
| Paying member vs free visitor              | Revenue depends on monthly member fee, but the map does not distinguish signups from payers.                                                                               |
| Obstructers                                | No actor is shown who could block adoption: privacy-concerned users, poor-quality recipe contributors, partner integration blockers, or users who distrust AI food advice. |

The absence of obstructers is a classic impact-map weakness: the map only shows who helps, not who can stop the goal.

### HOW / Impacts

The impacts cover the right themes from the brainstorming and canvas: search, support, budget, shopping, community recipes, learning, publishing.

But most are too broad. The best ones to preserve and sharpen are probably:

| Keep the theme    | Rewrite as behaviour                                              |
| ----------------- | ----------------------------------------------------------------- |
| Recipe search     | “Users find and save a cookable recipe in one session.”           |
| Cooking support   | “Beginners finish recipes successfully without external help.”    |
| Budget            | “Families plan meals using ingredients they already have.”        |
| Community recipes | “Members cook, rate, and comment on recipes from other members.”  |
| Publishing        | “Cooks publish recipes often enough to keep the catalogue fresh.” |

### WHAT / Deliverables

Some deliverables have good traceability:

| Deliverable       | Plausible impact                                         |
| ----------------- | -------------------------------------------------------- |
| Meal chooser      | Helps users decide what to cook.                         |
| AI proposals      | Helps users adapt recipes to needs, ingredients, budget. |
| Rating / comments | Supports trust in community recipes.                     |
| Planner           | Supports recurring meal planning.                        |
| Direct ordering   | Supports shopping from recipes.                          |

Some are more questionable:

| Deliverable           | Concern                                                                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Grandma / Grandpa AI  | Interesting, but possibly novelty-driven. It needs evidence that this improves completion or retention.                                                |
| Famous chefs partner  | Expensive and partner-dependent; it belongs closer to acquisition/credibility strategy, not necessarily cooking support.                               |
| Cookbook publishing   | Present in brainstorming and canvas-adjacent via photo book service, but likely serves a niche creator segment, not the fastest path to 2,500 members. |
| Kitchen tool ordering | May monetize partners, but could distract from recipe-sharing membership growth.                                                                       |
| Timer                 | Useful, but generic; unlikely to be decisive for membership growth unless tied to guided cooking.                                                      |

## Map vs. brainstorming result

The impact map is clearly derived from the brainstorming, but it selectively compresses it.

### Good synthesis

The map carries over the major clusters: shopping, community/shared recipes, content/rating, planner, recipe search, cooking support, AI help, budget, and publishing.

### Dropped or weakened ideas worth reconsidering

The brainstorming has several ideas that are stronger or more specific than the final map:

| Brainstorming item                                  | What happened in the map                                                | Why it may matter                                                     |
| --------------------------------------------------- | ----------------------------------------------------------------------- | --------------------------------------------------------------------- |
| “Search with things I already have”                 | Only partly visible under recipe search / budget                        | This is a concrete, high-value behaviour for budget-conscious users.  |
| “Fridge-photo recipe generation”                    | Dropped                                                                 | Could be a sharper experiment than generic AI proposals.              |
| “Dietary requirements” / “Allergies”                | Mostly absent except “specialized nutrition requirements” in the canvas | This is a strong differentiator and trust driver.                     |
| “Cooking skill level” / “step-by-step cooking mode” | Flattened into “Learn cooking” / “Provide cooking support”              | These are more testable than the final wording.                       |
| “Login with social account”                         | Dropped                                                                 | Maybe fine, but it affects acquisition and community sharing.         |
| “Comments / followers / rating”                     | Reduced mostly to “Rating” and “Content”                                | The community loop is weaker in the final map than in the brainstorm. |

The brainstorming shows a richer product ecosystem than the impact map, but the map does not clearly explain why some concrete ideas were cut. The collaboration cross-check skill warns that synthesis can accidentally drop useful session ideas or smooth over unresolved alternatives.

## Map vs. Business Model Canvas

The map aligns reasonably well with the canvas, but there are important gaps.

### Strong alignment

| Canvas block                                                                                     | Reflected in impact map                                           |
| ------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------- |
| Customer segments: singles, young families                                                       | Both are actors.                                                  |
| Key partners: kitchenware retailer, grocery stores, photo book service, chefs                    | All appear as actors.                                             |
| Value propositions: easy recipes, cooking support, nutrition requirements, AI proposals, planner | Mostly represented as impacts/deliverables.                       |
| Channels: app                                                                                    | Implied by deliverables.                                          |
| Revenue: monthly member fee, partner ads                                                         | Indirectly supported through membership and partner deliverables. |

### Weak alignment

The canvas says the relationship model is **community**, with **daily sharing** and **weekly creating**. But the impact map gives community only one branch: “Use of community shared recipes,” with deliverables like cooking club, content, and rating. That is underpowered if community is central to retention and revenue.

The canvas also includes **software development, maintenance, cloud service, licenses, and marketing** as cost structure. The impact map’s deliverables imply substantial cost: AI proposals, grandma/grandpa AI, planner, direct ordering, partner integrations, cookbook publishing, famous chef partnership. The map does not show internal actors, resource constraints, or cost-risk assumptions. The canvas cross-check specifically calls this out: deliverables can silently assume resources, activities, partners, or costs the canvas has not accounted for.

## What is strong

The map has a clear, measurable membership target. It also avoids the common mistake of listing only end users: partners such as grocery stores, kitchenware retailers, chefs, and photo book services are visible. The deliverables are traceable to ideas from the brainstorming and to value propositions in the canvas.

## Path forward

I would not throw this map away. I would tighten it.

First, define “member” precisely: active, paying, contributing, or registered. Then rewrite the impacts as observable behaviours. Finally, pick one shortest-path branch, probably either **young families plan budget-friendly meals weekly** or **members cook and share community recipes regularly**, and validate that branch before investing in the broader feature set.
