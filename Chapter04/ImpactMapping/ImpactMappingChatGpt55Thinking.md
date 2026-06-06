# Prompt 2026-06-06:

Criticize the Impact Map based on the Brainstorming result and Business Model Canvas.
Use the provided skill.
![](../assets/ImpactMapping.jpg)
![](../assets/BrainstormingConvergence.jpg)
![](../assets/BusinessModelCanvasEnhanced.jpg)
[SKILL.md](../Skills/ImpactMappingSkill/SKILL.md)
[Canvas cross check](../Skills/ImpactMappingSkill/references/canvas-cross-check.md)
[Collaboration cross check](../Skills/ImpactMappingSkill/references/collaboration-cross-check.md)
[Coherence and tracibility](../Skills/ImpactMappingSkill/references/coherence-and-traceability.md)
[Four level checklist](../Skills/ImpactMappingSkill/references/four-level-checklist.md)

# Answer:

## One-line read

Right now the map reads roughly as: **“In order to build Larder, singles, young families, cooks, retailers, chefs, grocery stores and photo-book services will create shopping/community/recipe/cooking-support behaviours, and we will support that with features such as ordering, AI proposals, timers, planners and cookbook publishing.”**

That **does not yet hold together as an impact map**, mainly because the **goal is a product name, not a measurable business outcome**, and several “impacts” are actually value areas or feature clusters rather than observable actor behaviour. The skill’s key test is that every branch should read as: “In order to [goal], [actor] will [impact], and we will support that by [deliverable].”

## Biggest risks first

### 1. “Larder” is not a goal; it is the solution

The map’s centre says **Larder**. That tells us what you intend to build, but not **why**. From the Business Model Canvas, plausible goals could be things like: increase paid memberships, increase partner ad revenue, improve recipe discovery for nutrition needs, or reduce meal-planning effort for singles and young families. But the impact map does not choose one.

Why this worries me: without a measurable goal, every branch can look equally valid. “Shopping,” “community,” “recipe search,” “planner,” and “cookbook publishing” all seem useful, but they may serve different business outcomes. The provided checklist explicitly treats an unmeasured goal as a major failure because there is no metric, target, or deadline to decide whether the map worked.

Cheap test: rewrite the centre as one measurable outcome, for example: **“Increase monthly paying members among singles and young families from X to Y by Q4.”**

What would resolve it: a single metric, current baseline, target, and date. Until then, this is more of a solution map than an impact map.

---

### 2. The map appears to converge on almost the whole brainstorm, not the shortest path

The brainstorming result is very broad: shopping, helper, creator support, cooking support, recipe search, planner, community, content creation, content rating, authentication, special recipes, smart kitchen, allergens, food pairing, fridge-photo recipe generation, cookbook publishing, and more.

The final impact map still keeps many of those areas: **Shopping, Community, Easy-to-find recipes, Cooking support, Budget, Cooking skill learning, Cookbook publishing.** That suggests the team may have summarized the brainstorm rather than made a hard strategic cut.

Why this worries me: impact mapping is not meant to justify building everything. It is meant to find the shortest path to the goal and validate one high-leverage behaviour change first.

Cheap test: force-rank the branches against the chosen metric. Ask: **Which one actor-impact pair could move the goal fastest with the smallest experiment?**

What would resolve it: mark one first bet, two backup bets, and explicitly park the rest.

---

### 3. Several “impacts” are not actor behaviour changes

The impact layer contains items like **Shopping**, **Community**, **Easy-to-find recipes**, **Cooking support**, **Budget**, **Cooking skill learning**, and **Cookbook publishing**. These are mostly themes, outcomes, or product areas. They do not yet say what the actor will do differently.

For example:

| Current impact       | Better impact phrasing                                        |
| -------------------- | ------------------------------------------------------------- |
| Shopping             | Singles reorder missing ingredients directly from a recipe    |
| Community            | Young families share weekly meal plans or rate cooked recipes |
| Easy-to-find recipes | Users find a suitable recipe within two minutes using filters |
| Cooking support      | Novice cooks complete a recipe without abandoning midway      |
| Budget               | Households choose recipes based on available budget           |
| Cookbook publishing  | Power users publish curated recipe collections                |

Why this worries me: the checklist says impacts should be observable behaviour changes, not features, vague outcomes, or value labels.

Cheap test: for each impact, complete the sentence: **“Actor will…”** If it does not produce a visible behaviour, rewrite it.

What would resolve it: convert every impact into a verb phrase tied to a specific actor.

## Level-by-level notes

### WHY / Goal

The goal is the weakest part. **“Larder”** is a product concept, not a business objective. The Business Model Canvas includes revenue streams of **monthly member fee** and **ads by partners**, so the impact map should probably choose whether it is primarily optimizing for paid retention, acquisition, partner revenue, or engagement that later monetizes.

A stronger centre might be:

**“Increase monthly paid subscriptions from singles and young families by X% within six months.”**

or

**“Increase weekly active recipe-to-shopping conversions by X% by Q4.”**

The right one depends on whether the business model prioritizes subscription revenue or partner commerce.

### WHO / Actors

The actors in the impact map are mostly faithful to the brainstorm, but not to the canvas strongly enough.

The canvas names **Customer Segments: Singles and Young families**. Those do appear in the map. It also names **Key Partners: kitchenware retailers, grocery stores, photo book services, chefs**. Those appear too. That is good.

But the map adds **Cook** as a central actor, while the canvas does not clearly define “cooks” as a customer segment. That may be fine, but it needs clarification: is “Cook” a content creator, a normal user, a professional chef, or a hobbyist? If it is a different monetizable segment, it should be reflected in the business model too.

Missing possible actors:

| Missing or vague actor           | Why it matters                                                                                                             |
| -------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| Paying subscriber / member       | The canvas has monthly fees, but the map does not distinguish free users from paying members                               |
| Advertiser / partner marketer    | “Ads by partners” is a revenue stream, but no actor is responsible for buying or renewing ads                              |
| Nutrition-sensitive user         | The canvas emphasizes specialized nutrition requirements; the map only weakly captures this through “easy-to-find recipes” |
| Internal content/moderation team | Community, rating, and content creation create trust and quality risks                                                     |
| Data/privacy gatekeeper          | Fridge photos, allergens, AI proposals, and profiles imply sensitive data                                                  |

### HOW / Impacts

The impact layer is too abstract. It reads like a set of product modules rather than behaviour changes.

The strongest candidate impacts are:

**Easy-to-find recipes** — closely tied to the canvas value proposition.

**Cooking support** — also tied to the canvas.

**Shopping** — tied to possible partner revenue and direct ordering.

The weaker or more questionable impacts are:

**Cookbook publishing** — present in the brainstorm and deliverables, but not clearly tied to the Business Model Canvas except through photo book services. It may be a niche monetization path, but it should not compete with the core subscription/ad model unless evidence shows demand.

**Budget** — strong user need, but it is absent from the Business Model Canvas value proposition. Either the canvas missed it, or the impact map is drifting.

**Cooking skill learning** — appears in the brainstorm, but the canvas emphasizes recipe finding, cooking support, and nutrition requirements more than education. This could be a valuable branch, but it needs evidence.

### WHAT / Deliverables

Some deliverables are promising, but many look like scope before validation.

Strongest deliverable candidates:

| Deliverable                       | Why it is promising                                     |
| --------------------------------- | ------------------------------------------------------- |
| Meal chooser                      | Supports easy recipe selection                          |
| AI proposals                      | Ties to recipe discovery, planning, and personalization |
| Planner                           | Fits weekly creation/planning from the canvas           |
| Search with things I already have | Strongly supports recipe search and shopping            |
| Direct ordering                   | Connects user value to partner revenue                  |
| Dietary requirements / allergens  | Directly supports the nutrition value proposition       |

Riskier or overextended deliverables:

| Deliverable          | Concern                                                                    |
| -------------------- | -------------------------------------------------------------------------- |
| Grandma / Grandpa AI | Interesting, but it appears more like novelty than core value              |
| Famous chefs partner | Partnership-heavy and expensive; not clearly required for first validation |
| Cooking club         | Community feature with moderation and cold-start risk                      |
| Cookbook publishing  | Operationally and partner-heavy for a likely secondary use case            |
| Smart kitchen        | Potentially costly and dependent on hardware/integration partners          |

The skill warns against treating deliverables as committed scope rather than prunable options.

## Map vs. Brainstorming result

The impact map is broadly traceable to the brainstorm, but it loses some useful distinctions.

### Dropped or underrepresented ideas worth reconsidering

**Allergens / dietary requirements / fridge-photo recipe generation / food pairing** are visible in the brainstorm but not clearly represented in the impact map’s deliverables, even though the canvas explicitly mentions **recipes for specialized nutrition requirements**. That looks like a dropped strategic thread.

**Authentication / login with social account** appears in the brainstorm but not the impact map. That is probably fine as a technical enabler, but if community, rating, followers, and content creation are important, identity becomes more central than the map admits.

**Content creation and content rating** are in the canvas as key activities and in the brainstorm, but the impact map mostly shifts them into community/content deliverables. It should clarify who creates, who rates, and what behaviour change is expected.

### Unsupported or weakly supported branches

**Budget** appears in the impact map and brainstorm, but not strongly in the Business Model Canvas. It may be a good idea, but it looks less strategically grounded than nutrition, recipe search, cooking support, and planning.

**Cookbook publishing** appears in the brainstorm and map, and photo book service exists in the canvas. Still, the branch feels disproportionately prominent compared with its likely contribution to monthly member fees or partner ads.

### Possible false convergence

The brainstorm contains many competing product directions: smart kitchen, social community, recipe search, AI helper, shopping integration, creator platform, cookbook publishing, nutrition support. The impact map tidies these into a coherent-looking tree, but it does not show which branch won and why. That is a warning sign for premature convergence, one of the cross-check risks in collaborative synthesis.

## Map vs. Business Model Canvas

The impact map reflects the canvas in broad strokes, but the strategy is not sharp enough.

### Good alignment

The map includes the canvas’s two customer segments: **singles** and **young families**.

It includes the key partners: **kitchenware retailer, grocery store, chefs, photo book service**.

It includes deliverables that match value propositions: **easy recipe discovery, cooking support, AI proposals, planner, ordering, rating, content**.

### Gaps and mismatches

The canvas’s value proposition **“recipes for specialized nutrition requirements”** is much more central than it is in the impact map. In the map, nutrition seems folded vaguely into “easy-to-find recipes,” while the brainstorm has clearer deliverable ideas: allergens, dietary requirements, fridge-photo recipe generation, food pairing. This is probably the most important fidelity gap.

The canvas revenue streams are **monthly member fee** and **partner ads**, but the impact map does not show the actor behaviours that cause those revenues. For example:

| Revenue stream     | Missing behaviour change                                                         |
| ------------------ | -------------------------------------------------------------------------------- |
| Monthly member fee | Users subscribe, renew, or upgrade because Larder solves recurring meal planning |
| Ads by partners    | Partners buy ads because users show purchase intent or order ingredients/tools   |

The map includes partner actors, but not enough partner impacts. Grocery stores and retailers are shown as actors, yet the impacts mostly focus on user-side shopping. What do partners need to do differently? Provide catalog data? Sponsor recipes? Fulfil orders? Pay for placements?

The canvas cost side includes **software development, maintenance, cloud service, licenses, marketing**. The map proposes AI, smart kitchen, community, content, ordering, rating, and cookbook publishing, all of which increase cost and operational complexity. The canvas-cross-check warns that deliverables should be checked against key resources, activities, partners, and costs.

## What is strong

The raw material is strong. The map did not ignore the canvas: the main customer segments and key partners survived. The brainstorm-to-map synthesis also preserves several promising clusters: recipe search, shopping, cooking support, planning, community, and publishing.

The best product spine seems to be:

**Singles and young families struggle to decide what to cook, find suitable recipes, and turn those recipes into shopping actions. Larder helps them find suitable recipes, plan meals, and order what they need.**

That is a coherent product. It just needs a sharper impact map.

## Suggested next revision

Make the next version much narrower:

**Goal:** increase paid subscription conversion or weekly active meal-planning usage.

**Primary actors:** singles and young families.

**Primary impacts:**
Users find suitable recipes faster.
Users plan meals weekly.
Users order missing ingredients from recipes.
Users return because recipes match dietary needs and available ingredients.

**First deliverables to test:** meal chooser, dietary/allergen filters, “cook with what I have,” simple weekly planner, and direct grocery ordering.

I would park **grandparent AI, famous chef partnerships, smart kitchen, cookbook publishing, and cooking club** until the core recipe-to-plan-to-shop loop proves it can move the chosen metric.
