# Prompt 2026-06-06:

Criticize the Impact Map based on the Brainstorming result and Business Model Canvas.
Use the providded skill.
![](../assets/ImpactMapping.jpg)
![](../assets/BrainstormingConvergence.jpg)
![](../assets/BusinessModelCanvas.jpg)
[SKILL.md](../Skills/ImpactMappingSkill/SKILL.md)
[Canvas cross check](../Skills/ImpactMappingSkill/references/canvas-cross-check.md)
[Collaboration cross check](../Skills/ImpactMappingSkill/references/collaboration-cross-check.md)
[Coherence and tracibility](../Skills/ImpactMappingSkill/references/coherence-and-traceability.md)
[Four level checklist](../Skills/ImpactMappingSkill/references/four-level-checklist.md)

# Answer:

I used the uploaded **Impact Mapping Critic** skill and its cross-check guidance against the brainstorming board and Business Model Canvas.

## One-line read

Right now the map reads roughly as:

> “In order to make **Larder** successful, **singles, young families, cooks, chefs, grocery stores, kitchenware retailers and photo-book services** will do things like **shop, join community, find recipes, get cooking support, manage budgets and publish cookbooks**, and we will support that with **ordering, meal chooser, AI proposals, timer, planner, famous-chef partner, cookbook publishing**, etc.”

That chain is recognizable, but it does **not fully hold together yet** because the **goal is not a measurable business outcome**, many “impacts” are actually **solution areas**, and several deliverables feel like they were pulled from brainstorming without enough proof that they move the business model.

## Biggest risks first

### 1. The goal is not a goal yet

The center of the impact map is simply **“Larder.”** That is the product name, not the reason for doing the product.

A stronger goal would say what business outcome Larder must create, for example:

> “Acquire 5,000 paying monthly members among singles and young families within 6 months.”

or:

> “Increase weekly active cooking sessions to X and convert Y% of users to paid membership by Q4.”

Without a metric, target, and deadline, the map cannot decide whether **Meal Chooser**, **AI proposals**, **Planner**, **Timer**, or **Grandma/Grandpa AI** are good bets or just interesting features.

**Cheap test:** pick one primary metric: paid subscriptions, weekly recipe usage, completed cooking sessions, partner ad revenue, or grocery-order conversion.

**What would resolve it:** rewrite the WHY level as a measurable outcome before judging any branch.

---

### 2. Most “impacts” are not actor behaviour changes

The impact level contains items like:

* Shopping
* Community
* Easy-to-find recipes
* Cooking support
* Budget
* Cooking skill learning
* Cookbook publishing

These are closer to **themes, value propositions, or product areas** than observable behaviours.

For example:

* “Easy-to-find recipes” is not what an actor does.
* “Cooking support” is not what an actor does.
* “Budget” is not what an actor does.

Better impact wording would be:

* Singles **choose a meal within 2 minutes instead of abandoning search**.
* Young families **reuse leftovers twice per week instead of buying extra ingredients**.
* Beginner cooks **complete a recipe without outside help**.
* Grocery stores **pay for promoted ingredient placement**.
* Cooks **publish reusable recipe content weekly**.

**Cheap test:** for every impact, ask: “Can I watch a real person do this differently?” If not, it is probably not an impact yet.

**What would resolve it:** rewrite the HOW level as verbs attached to actors.

---

### 3. The map is trying to cover the whole brainstorm instead of finding the shortest path

The brainstorming result is huge: shopping, larder tracking, cooking support, recipe search, planner, content creation, rating, community, authentication, smart kitchen, voice control, allergens, dietary requirements, fridge-photo recipe generation, grandma support, famous chefs, cookbook publishing, and more.

The impact map carries many of these forward, but it does not show **priority**. It looks like a synthesis into many feature streams, not a decision about the smallest route to business impact.

This is dangerous because Larder could become a feature factory: grocery ordering, community, AI cooking helper, recipe search, partner ads, cookbook publishing, planner, ratings, and photo-book services all at once.

**Cheap test:** force the team to choose one first branch:

> Which single actor behaviour, if changed in the next 4 weeks, would most prove Larder’s business model?

**What would resolve it:** mark one branch as the first validation path and explicitly park the others.

---

## Level-by-level critique

### WHY — Goal

**Current:** “Larder”

This is the weakest level. It names the product, not the outcome. The Business Model Canvas suggests possible goals around **monthly membership fees** and **partner ads**, but the impact map does not choose one.

A goal such as “make recipe search easy” would still be too weak because it mirrors a value proposition. The business goal should probably connect to either:

* paid monthly membership,
* recurring active use,
* grocery / kitchenware partner revenue,
* or successful entry into singles and young-family segments.

### WHO — Actors

The actor list is stronger than the goal. It includes customer segments from the canvas: **Single**, **Young family**, and **Cook**. It also includes partners: **Kitchenware retailer**, **Chefs**, **Grocery store**, and **Photo book service**.

But a few important actors from the brainstorming and canvas logic are missing or underdeveloped:

* **Grandma / Grandpa / family expert** appears as a deliverable concept, but could be an actor or persona if the value is trusted human-like cooking guidance.
* **People with allergies or dietary requirements** are prominent in the brainstorm and canvas value proposition, but not explicit actors.
* **Content creators / recipe publishers** are present indirectly as “Cook” and “Chefs,” but the map does not distinguish amateur cooks, professional chefs, and community contributors.
* **Internal AI/content moderation/operations** are absent, even though AI search support, media, recipes, and content rating are key resources or activities in the canvas.

Also missing are obstructing actors: people who could block success, such as users who distrust AI recipes, grocery partners who do not integrate, cooks who will not contribute content, or families who find the subscription not worth paying for.

### HOW — Impacts

This is the second weakest level after the goal.

Several impacts should be rewritten from nouns into behaviours:

| Current impact         | Better behavioural version                                            |
| ---------------------- | --------------------------------------------------------------------- |
| Shopping               | Users order missing ingredients from a recipe without leaving the app |
| Community              | Cooks share recipes/comments weekly                                   |
| Easy-to-find recipes   | Users find a suitable recipe in under X minutes                       |
| Cooking support        | Beginners complete recipes without getting stuck                      |
| Budget                 | Families choose meals within a weekly budget                          |
| Cooking skill learning | Beginners repeat guided lessons and improve confidence                |
| Cookbook publishing    | Cooks convert saved/shared recipes into paid or printed collections   |

The map also lacks negative impacts: behaviours you want to prevent. For example:

* users blindly follow bad AI recipe suggestions,
* users abandon because results ignore allergies,
* partner ads make recommendations feel untrustworthy,
* community content becomes low-quality without moderation.

### WHAT — Deliverables

The deliverables are rich, but many feel premature.

Some are plausible MVP candidates:

* **Meal Chooser**
* **Search with cooking time**
* **Search with things I already have**
* **Leftover optimization**
* **Allergens / dietary requirements**
* **Timer**
* **Planner**

Some are strategically riskier and need stronger justification before building:

* **Grandma / Grandpa AI**
* **Famous chefs partner**
* **Cookbook publishing**
* **Kitchen tool ordering**
* **Photo book service**
* **Cooking club**

These might be good later, but they are expensive distractions if the first business goal is subscription adoption or weekly cooking usage.

## Map vs. brainstorming result

The impact map captures the big clusters from the brainstorm: **Shopping**, **Community**, **Recipe Search**, **Cooking Support**, **Planner**, **Budget**, **Cooking skill learning**, and **Cookbook publishing**.

But it drops or flattens some valuable detail:

* **Larder tracker** is prominent in the brainstorm but disappears from the impact map. If “Larder” means knowing what is already in the pantry, this is a serious omission.
* **Fridge-photo recipe generation** appears in the brainstorm but is absent from the map. That could be a distinctive differentiator.
* **Allergens** and **dietary requirements** appear as deliverables, but the canvas has “recipes for specialized nutrition requirements” as a core value proposition. This may deserve a stronger actor/impact branch, not just leaves under recipe search.
* **Authentication / login with social account** disappears. That is fine if intentionally cut, but if community, followers, comments, ratings, and sharing matter, identity is a hidden dependency.
* **Content creation** and **content rating** are central brainstorm clusters, yet the impact map only partially reflects them through “Community,” “Cook,” and “Rating.” The behaviour of contributors is under-specified.
* **Smart kitchen / voice control / helper** appears in the brainstorm but is not meaningfully carried into the map except indirectly through timer and cooking support.

My concern is not that every brainstorm note must survive. It should not. The concern is that the map does not explain **why these ideas were chosen and others were cut**.

## Map vs. Business Model Canvas

The map aligns reasonably with the canvas in several places:

* Customer segments: **Singles** and **Young families** appear as actors.
* Partners: **Chefs**, **grocery stores**, **kitchenware retailers**, and **photo-book service** appear.
* Value propositions: **easy recipe discovery**, **cooking support**, and **specialized nutrition** are visible.
* Revenue hints: partner deliverables such as grocery/kitchenware ordering could support ads or affiliate-style revenue.

But there are mismatches:

### Specialized nutrition is underweighted

The canvas gives “Recipes for specialized nutrition requirements” a large place in the value proposition. In the impact map, this becomes mostly **Allergens** and **Dietary requirements** as deliverables under Recipe Search.

That weakens the strategic focus. If specialized nutrition is central to why people pay, then the actor might be “people with allergies / dietary restrictions,” and the impact might be “they trust Larder enough to choose safe meals repeatedly.”

### Revenue streams are not traced clearly

The canvas has **monthly member fee** and **ads by partners**. The impact map does not show which actor behaviour produces those revenues.

Examples of missing impact links:

* Singles / young families **subscribe after repeated successful meals**.
* Grocery stores **pay for promoted ingredient placement**.
* Kitchenware retailers **pay for contextual tool recommendations**.
* Cooks / chefs **publish content that attracts paying users**.

Without these links, the map optimizes product usefulness but not necessarily the business model.

### Cost and resource assumptions are hidden

The canvas lists **software development, maintenance, cloud service, licenses**, plus resources like **AI search support, media, recipes, cooks**.

The impact map contains deliverables that could be expensive: AI proposals, Grandma/Grandpa AI, famous-chef partner, fridge-photo generation, smart recipe search, community, content rating, cookbook publishing. The map does not reflect the operational cost, moderation cost, licensing cost, or AI reliability risk.

That is a hidden bet: the right-hand value side is exciting, but the left-hand cost/resource side is not visible in the map.

## What is strong

The map does a good job of preserving the breadth of the concept. It clearly sees Larder as more than a recipe database: it combines recipes, pantry awareness, cooking help, planning, community, shopping, and partner ecosystems.

The Business Model Canvas and the impact map also agree on the broad strategic shape: an app for singles and young families, supported by cooks/media/AI, monetized through subscriptions and partners.

## Path forward

I would fix the map in this order:

1. Replace **“Larder”** with one measurable business goal.
2. Choose one primary customer segment first: probably **singles** or **young families**, not both.
3. Rewrite impacts as observable behaviours.
4. Pick one shortest validation branch, for example:
   **Young families → choose meals within budget using existing ingredients → meal chooser + “things I already have” + leftover optimization.**
5. Move everything else into a parking lot until the first branch proves it can move the goal.
