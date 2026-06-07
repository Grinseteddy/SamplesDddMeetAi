# Prompt 2026-06-07:

Criticize the Impact Map based on the Brainstorming result and Business Model Canvas.
Use the provided skill.

![](../assets/ImpactMappingWithAiStep3.jpg)
![](../assets/BrainstormingConvergence.jpg)
![](../assets/BusinessModelCanvasEnhanced.jpg)
[SKILL.md](../Skills/ImpactMappingSkill/SKILL.md)
[Canvas cross check](../Skills/ImpactMappingSkill/references/canvas-cross-check.md)
[Collaboration cross check](../Skills/ImpactMappingSkill/references/collaboration-cross-check.md)
[Coherence and tracibility](../Skills/ImpactMappingSkill/references/coherence-and-traceability.md)
[Four level checklist](../Skills/ImpactMappingSkill/references/four-level-checklist.md)

# Answer:

## One-line read

**In order to get “Larder” to 2,500 paying members in two years, singles / young families / creator-partners / partners will change behaviours such as following diets, sharing/using recipes, learning cooking, publishing content, converting to paid, renewing, and accepting ads — supported by a large bundle of app features and partnerships.**

It mostly holds as a *feature discovery map*, but it is weaker as an *impact map*: the goal is measurable, yet the actor–impact–deliverable chains are too broad, many “impacts” are actually product capabilities or user states, and the map looks like it tries to keep almost every brainstormed idea alive instead of finding the shortest path to the goal. That is exactly the kind of risk the provided critic skill asks us to surface: untested assumptions, feature-factory drift, and weak traceability from deliverables to outcomes.

## Biggest risks first

### 1. The goal is measurable, but not strategically sharp enough

“**Recipe sharing platform with min. 2,500 paying members in two years**” is better than a vague goal because it has a target and deadline. The problem is that it bundles at least three different bets:

**platform growth**, **paid conversion**, and **recipe-sharing/community activity**.

Those may not be moved by the same actors. For example, “singles follow dietary plans” might increase usage, while “convertible members pay for ads” or “prospects convert to paid plan” drives revenue. The map does not say which metric is the bottleneck: acquisition, activation, conversion, renewal, or content supply.

**Cheap test:** define the funnel explicitly: visitors → registered members → active recipe users → paying members → renewed paying members. Then choose one branch that is expected to move one funnel step.

**What would resolve it:** rewrite the goal as something like:
“Reach 2,500 paying members by month 24, with at least X% monthly retention and Y% of paying members using shared recipes weekly.”
That would stop “paying members” from becoming a vanity number disconnected from actual platform health.

### 2. The map is trying to build the whole product, not find the shortest path

The deliverables include nutrition management, ordering, kitchen tools, cooking club, content, rating, meal chooser, AI proposals, timer, grandma/grandpa AI, planner, famous chefs, cookbook publishing, member management, paywall, and ads management. That is a large product portfolio, not a pruned set of experiments.

The coherence checklist says the map should help find the **shortest path to the goal**, not staff every branch at once; every link is an assumption that should be tested cheaply before full build.

**Cheap test:** force-rank the branches by “expected paid-member impact / delivery effort / evidence strength.” Pick one actor, one impact, and one deliverable for a first experiment.

**What would resolve it:** mark most deliverables as “later / unvalidated,” and identify a first path such as:
“Prospects → see premium content → paywall for premium content → paid conversion.”
or
“Young families → use effective limited budget → meal chooser / planner → paid conversion.”
Right now the map does not make that strategic choice.

### 3. Several impacts are not behaviour changes

Some impact nodes are strong behaviour candidates: “convert to paid plan,” “paid member renews,” “publish their own cookbook,” “follow cooks.” But several are closer to features, states, or value propositions:

* “Find easily recipes” sounds like a product quality/value proposition, not an actor behaviour. A stronger impact would be: “members search and save recipes weekly.”
* “Use effective limited budget” is vague. What behaviour changes? “Plan weekly meals under a budget” would be testable.
* “Cookbooks make curious” is not observable behaviour. Do users click previews, follow cooks, buy cookbooks, or share them?
* “supports unexperienced members” sounds like what the product does, not what an actor does differently.
* “Shop easily ingredients and tools with discount” mixes behaviour and solution. The behaviour may be “members order ingredients through Larder instead of leaving the app.”

The four-level checklist’s core test is whether an impact is something the **actor does differently**, not something the team builds or a vague outcome.

## Level-by-level notes

### WHY / Goal

Strong: measurable target and deadline.
Weak: “paying members” is not connected tightly enough to revenue quality, retention, or community supply. The BMC says revenue comes from **monthly member fee** and **ads by partners**, but the impact map gives much more attention to member growth than to ad inventory quality, advertiser value, or paid retention.

### WHO / Actors

The map includes useful actors from the canvas and brainstorm: singles, young families, cook-as-author, kitchenware retailer, chefs, grocery store, photo book service, prospects, convertibles.

But it misses or underplays several actor types:

* **Existing paying members** are only implied through “paid member renews.” They should be a first-class actor if retention matters.
* **Non-paying active members** are too vaguely split into “prospects” and “convertible.” What makes someone convertible?
* **Advertisers / sponsors** appear only indirectly through grocery stores, kitchenware retailers, and ads management. Since ads are a revenue stream in the BMC, advertisers need explicit behaviour impacts.
* **Internal actors** are absent: content moderators, support, partnership managers, AI/content operations, platform maintainers. The BMC includes software development, maintenance, cloud service, licenses, and marketing costs; the impact map mostly ignores the actors who make those feasible.
* **Obstructers** are missing: users who distrust AI recipes, partners who resist integration, cooks who do not want their content monetized, users with allergies who need safety, or moderators/legal stakeholders for nutrition claims.

### HOW / Impacts

The impact layer is the weakest part. It mixes:

* real behaviours: “follow cooks,” “convert to paid plan,” “paid member renews,” “pay for ads”
* desired outcomes: “cookbooks make curious”
* product promises: “supports inexperienced members”
* feature-like capabilities: “find easily recipes,” “use community shared recipes”

A sharper version would make each impact observable:

* “Members with dietary needs save and cook at least one matching recipe per week.”
* “Young families create a weekly meal plan before grocery shopping.”
* “Cooks publish one recipe per week and respond to ratings/comments.”
* “Partners buy sponsored placements because they can attribute clicks/orders.”
* “Paying members renew because premium planning/search features are used monthly.”

### WHAT / Deliverables

The deliverables are recognizable and mostly traceable to brainstorm clusters, but they are too solution-heavy and too committed-looking.

Some deliverables are also large products in their own right:

* “Grandma / Grandpa AI”
* “AI proposals”
* “Planner”
* “Cookbook publishing”
* “Ads management”
* “Nutrition requirement management”

Each of these could become a product stream. The map needs smaller experiment candidates: landing-page test, concierge meal-planning service, fake-door premium paywall, manual partner coupon trial, one dietary requirement prototype, one creator publishing pilot.

## Map vs. brainstorming result

The map is faithful in one sense: it pulled many brainstorming clusters into the final structure — shopping, recipe search, cooking support, community, content creation, planner, rating, authentication/member management, and partner ideas all appear.

But the synthesis also flattened some important distinctions:

* The brainstorm had a richer **Recipe Search** cluster: search with existing ingredients, cooking time, leftovers, budget, allergies, dietary requirements, food pairing, fridge-photo recipe generation. The impact map compresses much of this into “Find easily recipes,” “Meal Chooser,” “AI proposals,” and “Nutrition requirement management.” That loses the behavioural specificity that could help prioritize.
* The brainstorm had a **Community** cluster with cooking club, sponsor contest, sharing on Instagram, and grill master. The impact map keeps “Cooking club” and “Content,” but the social growth mechanics are less explicit.
* The brainstorm had **Cooking Support** with skill level, step-by-step mode, ask grandma, famous chefs, grandpa shows how to do it, and smart kitchen. The map includes some outputs, but bundles them into broad impacts like “Learn cooking” and “supports inexperienced members.” This is a sign of premature convergence: many different support concepts are treated as one branch.
* “Authentication / login with social account” appears in the brainstorm but is not clearly represented in the impact map except perhaps through member management. If onboarding/conversion matters, login friction may be more important than several flashy AI features.
* “Voice control” appears in the brainstorm but seems dropped. That may be fine, but it should be an explicit cut, not an accidental disappearance.

The collaboration cross-check warns that synthesis can drop ideas, smooth over tension, or overrepresent the loudest / most obvious branch; the risk here is less obvious HiPPO dominance and more **everything became a feature cluster**, while the behavioural rationale got weaker.

## Map vs. Business Model Canvas

The canvas and impact map align on the surface:

* Customer segments: singles and young families appear as actors.
* Key partners: kitchenware retailers, grocery stores, photo book service, chefs appear.
* Value propositions: easy recipe finding, cooking support, specialized nutrition, AI proposals, planner appear.
* Revenue streams: monthly member fee and ads by partners appear as member management/paywall/ads management branches.

The deeper mismatch is feasibility and economics. The canvas cost side lists software development, maintenance, cloud service, licenses, and marketing. The impact map proposes AI, planner, nutrition management, community, publishing, ordering, ads, timer, and partner integrations — but does not show the internal actors or cost constraints required to deliver and operate them. The canvas cross-check specifically flags deliverables that silently assume resources, activities, partners, or costs not represented in the map.

The biggest canvas-related question is:

**Is Larder primarily a paid consumer subscription business, an ad/partner marketplace, or a creator/community platform?**

The map tries to be all three. The BMC has only two revenue streams, monthly fees and partner ads, but the impact map also invests heavily in creator publishing, cookbook curiosity, and chef partnerships. That may be strategic, but the map should make the causal logic explicit:

* creator content → more valuable recipe discovery → higher conversion/retention?
* partner ads → discounts/order revenue → lower churn?
* AI cooking support → premium feature willingness to pay?

Right now those links are implied, not tested.

## What is strong

The map has a real measurable target, not just “build an app.” It also preserves a broad set of useful ideas from the brainstorm and reflects the BMC better than many first-pass maps do. The actors are more specific than generic “users,” and the deliverables are concrete enough to become experiments.

The next improvement is not to add more. It is to cut harder: choose the one or two actor behaviours most likely to create paying, retained members, rewrite those impacts as observable behaviours, and turn the matching deliverables into cheap tests rather than a full product roadmap.
