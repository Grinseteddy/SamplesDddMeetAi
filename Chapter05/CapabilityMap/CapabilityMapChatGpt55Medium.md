# Prompt 2026-06-14:

Validate the provided Capability Map based on the NSM: "Larder is a new recipe-sharing platform.It covers cooking skills and the support for meal preparation.Skills and meal preparation are supported by chefs and experienced home cooks.Furthermore, it is a community platform, where members can share and rate recipes.From 100 friends and family in the beginning, the platform grows to more than 2,500 members in two years.The community is supported by different kinds of competitions." and the provided Impact Mapping. Use the provided skill.

![](./assets/CapabilityMap.jpg)
![](./assets/ImpactMap.jpg)
[SKILL.md](../Skills/CapabilityMapSkill/SKILL.md)
[Capability Essentials](../Skills/CapabilityMapSkill/CapabilityEssentials.md)
[Upstream Cross-Check](../Skills/CapabilityMapSkill/UpstreamCrossCheck.md)
[Classification Core/Supporting/Generic](../Skills/CapabilityMapSkill/ClassificationCoreSupportingGeneric.md)

# Answer:

## One-line read

The capability map is directionally plausible, but **the core/supporting/generic marking does not fully hold**. For Larder to reach **2,500 paying members in two years**, the business must be able to **onboard and retain members, help them cook/prepare meals, enable recipe/community contribution and rating, and convert free/community value into paid membership**. The current map over-emphasizes feature boxes and under-represents **community growth, competition management, subscription conversion, and retention**.

The capability-map critique below follows the supplied capability-map skill: a capability should be a stable “ability to do X,” not a process, system, project, KPI, or implementation detail; core/supporting/generic is a competitive-contribution lens, not just “important vs. unimportant.”

## Interpreted capability map

| Area            |                 Capability shown | Current marking | My validation                                                                                                                                                                                                                                                                              |
| --------------- | -------------------------------: | --------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Onboarding      |             Famous chefs partner |      Supporting | Plausible, but better named **Chef Partnership Management**. It supports credibility and acquisition, but is not enough on its own to be core.                                                                                                                                             |
| Onboarding      |                Member management |         Generic | **Under-marked.** For a community/subscription platform, this is not just admin. It should include **Member Lifecycle Management**, acquisition, activation, retention, and subscription status. At least supporting; parts may be core.                                                   |
| Onboarding      |                   Ads management |         Generic | Plausible generic/supporting. Not central to the NSM unless ads are a major acquisition or revenue lever.                                                                                                                                                                                  |
| Cooking         |                     Meal Chooser |            Core | Defensible as core: it directly supports meal preparation and recurring usage.                                                                                                                                                                                                             |
| Cooking         |                     AI proposals |            Core | Defensible only if Larder’s recipe/meal recommendation quality is meant to differentiate. Otherwise it is a feature of **Meal Recommendation Management**.                                                                                                                                 |
| Cooking         |                          Planner |      Supporting | I would challenge this. Planning recurring meals may be closer to core than shown because it drives repeated use and paid conversion.                                                                                                                                                      |
| Cooking Support | Nutrition requirement management |            Core | Possibly core if diet plans/nutrition are central to the value proposition. The NSM/impact map shows “follows dietary plans,” so this is defensible.                                                                                                                                       |
| Cooking Support |                  Direct ordering |         Generic | Plausible generic/supporting. It helps convenience but can likely be bought/integrated.                                                                                                                                                                                                    |
| Cooking Support |            Kitchen tool ordering |         Generic | Correct as generic unless Larder is strategically competing in kitchen-commerce.                                                                                                                                                                                                           |
| Cooking Support |                            Timer |         Generic | Correct. Useful feature, not differentiating.                                                                                                                                                                                                                                              |
| Cooking Support |             Grandma / Grandpa AI |      Supporting | Better as **Cooking Assistance / Coaching**. Could be core if the “experienced home cooks” support promise is delivered uniquely through this.                                                                                                                                             |
| Sharing         |                 Content Creation |            Core | Defensible, but should be named **Recipe Content Creation Management** or **Recipe Publishing Enablement**.                                                                                                                                                                                |
| Sharing         |              Cookbook publishing |      Supporting | Plausible supporting. It aligns with “members can share recipes,” but cookbook production is secondary unless it is a paid differentiator.                                                                                                                                                 |
| Sharing         |      Paywall for premium content |         Generic | **Probably under-marked.** For “2,500 paying members,” premium-content access and monetization are strategically important. The capability is more like **Subscription Monetization / Premium Access Management** and should be supporting, maybe core if conversion is the growth engine. |
| Rating          |                           Rating |            Core | Defensible: community rating is explicitly in the NSM text and helps trust/discovery. Better named **Recipe Rating & Trust Management**.                                                                                                                                                   |

## Biggest risks

### 1. The map misses the explicit “competition” capability

The NSM says the community is supported by “different kinds of competitions,” but the capability map has no clear **Competition Management**, **Challenge Management**, or **Community Engagement Campaign Management** capability.

Why this worries me: competitions are not a decorative detail; they are a stated mechanism for growing and energizing the community. In impact-map terms, that is a deliverable/impact with no clear capability behind it, which is a classic execution risk. The supplied cross-check guidance says every important deliverable or behaviour-change should have a capability behind it.

Cheap test: ask, “Which capability owns designing, running, moderating, rewarding, and measuring recipe competitions?”

Resolution: add **Community Competition Management** under Sharing or Onboarding/Engagement. I would mark it **core** or at least **supporting**, because it directly supports member growth and engagement.

### 2. “Member management” is too generic for a paid community platform

The current map treats **Member management** as generic. That is risky.

For a recipe-sharing community growing from friends and family to 2,500 paying members, member acquisition, onboarding, activation, moderation, retention, subscription status, and renewal are not simple back-office admin. They are part of the growth engine.

Cheap test: ask, “Could Larder outsource member lifecycle and conversion to a standard membership tool and still win, with no customer-visible difference?” If the answer is no, it is not generic.

Resolution: split it into clearer capabilities:

| Proposed capability                   |                   Marking |
| ------------------------------------- | ------------------------: |
| Member Lifecycle Management           |        Core or Supporting |
| Subscription / Paid Member Management |                Supporting |
| Community Moderation & Trust          | Supporting, possibly Core |
| Generic Account Administration        |                   Generic |

### 3. Core inflation is mild, but the core set is still feature-heavy

Five boxes are marked core: Meal Chooser, AI proposals, Nutrition requirement management, Content Creation, Rating. That is not absurdly inflated, but it leans toward **features** rather than durable business abilities.

The classification guidance warns that “necessary” is not the same as “differentiating,” and that core should be the small set where the business must be better, not merely good enough.

I would reframe the core set as fewer, stronger capabilities:

| Stronger core capability                  | Absorbs current boxes                                         |
| ----------------------------------------- | ------------------------------------------------------------- |
| Personalized Meal & Recipe Recommendation | Meal Chooser, AI proposals, Planner                           |
| Dietary & Cooking Support                 | Nutrition requirement management, Grandma/Grandpa AI          |
| Community Recipe Contribution & Trust     | Content Creation, Rating                                      |
| Community Growth & Engagement             | Competitions, member activation, chef/home-cook participation |

That tells a more strategic story than a list of product features.

## Capability hygiene notes

Several boxes are closer to **features or implementation choices** than capabilities:

| Current label               | Issue                      | Better capability wording        |
| --------------------------- | -------------------------- | -------------------------------- |
| AI proposals                | Feature/technology wording | Meal Recommendation Management   |
| Timer                       | Feature                    | Cooking Session Support          |
| Grandma / Grandpa AI        | Branded solution           | Expert Cooking Assistance        |
| Paywall for premium content | Mechanism                  | Premium Access Management        |
| Famous chefs partner        | Partner/activity wording   | Chef Partnership Management      |
| Direct ordering             | Feature/channel            | Ingredient Ordering Enablement   |
| Rating                      | Activity/feature           | Recipe Rating & Trust Management |

The map also mixes different levels of abstraction. “Timer” is much smaller than “Member management”; “Content Creation” is broad, while “Kitchen tool ordering” is narrow. A capability map should keep sibling boxes at roughly comparable granularity.

## Map vs. NSM and impact map

The capability map covers several impact-map deliverables well:

| Impact-map / NSM element | Capability coverage                                                 |
| ------------------------ | ------------------------------------------------------------------- |
| Meal preparation support | Meal Chooser, Planner, Nutrition requirement management, Timer      |
| Cooking skills support   | Grandma/Grandpa AI, AI proposals, chef partner                      |
| Recipe sharing           | Content Creation, Cookbook publishing                               |
| Recipe rating            | Rating                                                              |
| Paid members             | Member management, Paywall for premium content — but underdeveloped |
| Community growth         | Weak coverage                                                       |
| Competitions             | Missing                                                             |

The biggest strategic mismatch is this: the goal is about **paid member growth**, but the map’s strongest core markings sit around cooking features. That can work only if those features clearly drive acquisition, engagement, and conversion. Otherwise, the map is optimizing product utility while under-mapping the business model that reaches 2,500 paying members.

## What is strong

The overall flow from **Onboarding → Cooking → Cooking Support → Sharing → Rating** is sensible for Larder. The map also correctly separates some commodity capabilities, such as Timer and Kitchen tool ordering, from more differentiating ones. The strongest current core candidates are **Personalized Meal/Recipe Recommendation**, **Dietary Support**, and **Community Recipe Contribution & Rating**.

## Recommended changes

Add or promote these capabilities:

| Capability                               |         Suggested marking |
| ---------------------------------------- | ------------------------: |
| Community Growth & Engagement            |                      Core |
| Competition Management                   |        Core or Supporting |
| Member Lifecycle Management              |        Core or Supporting |
| Subscription Conversion & Renewal        | Supporting, possibly Core |
| Community Moderation & Trust             | Supporting, possibly Core |
| Recipe Discovery & Search                |                      Core |
| Expert/Home-Cook Contribution Management |        Supporting or Core |

Demote or reframe these:

| Current item                | Suggested action                                                   |
| --------------------------- | ------------------------------------------------------------------ |
| AI proposals                | Keep only if it differentiates; otherwise fold into Recommendation |
| Timer                       | Keep generic                                                       |
| Paywall for premium content | Rename and raise to Supporting                                     |
| Member management           | Split; do not leave all of it Generic                              |
| Famous chefs partner        | Rename as partnership capability                                   |

My verdict: **the capability map is a useful first draft, but not yet a reliable investment map.** It needs stronger line of sight to the paid-member NSM, especially around community growth, competitions, conversion, and retention.

# Next Prompt:

Please check the changes in the capability map.

![](./assets/CapabilityMapStep2.jpg)

# Answer:

## Quick verdict

This revision is **meaningfully better**. The biggest improvements are:

1. **Community Engagement was added and marked Core** — this fixes the earlier gap around competitions and community growth.
2. **Famous chefs partner is now Core** — defensible if chefs are a major acquisition/trust lever, but I would still rename it.
3. **Grandma / Grandpa AI is now Core** — defensible if it is the main way Larder delivers “support by experienced home cooks.”
4. **Nutrition requirement management and Rating moved to Supporting** — mostly sensible, though Rating is debatable.

The map now tells a clearer story: Larder wins through **community engagement, meal choice/recommendation, AI-assisted cooking support, trusted content creation, and expert/chef credibility**, while commerce and admin capabilities stay generic.

## Updated capability validation

| Area            | Capability                       | Current marking | Validation                                                                                                                          |
| --------------- | -------------------------------- | --------------: | ----------------------------------------------------------------------------------------------------------------------------------- |
| Onboarding      | Famous chefs partner             |            Core | **Possibly valid**, but only if chef participation is a key growth/trust differentiator. Rename to **Chef Partnership Management**. |
| Onboarding      | Member management                |         Generic | Still too low. For a paid community, part of this should be **supporting or core**.                                                 |
| Onboarding      | Ads management                   |         Generic | Fine.                                                                                                                               |
| Cooking         | Meal Chooser                     |            Core | Strong core candidate. Directly supports meal preparation and repeated use.                                                         |
| Cooking         | AI proposals                     |            Core | Defensible if recommendations are genuinely differentiated. Otherwise fold into Meal Recommendation.                                |
| Cooking         | Planner                          |      Supporting | Sensible, though it could become core if recurring meal planning drives retention.                                                  |
| Cooking         | Community Engagement             |            Core | **Good addition.** This now covers the competition/community-growth gap from the NSM.                                               |
| Cooking Support | Nutrition requirement management |      Supporting | Reasonable. Important, but not necessarily differentiating unless dietary-plan support is a strategic promise.                      |
| Cooking Support | Direct ordering                  |         Generic | Fine.                                                                                                                               |
| Cooking Support | Kitchen tool ordering            |         Generic | Fine.                                                                                                                               |
| Cooking Support | Cooking support                  |         Generic | This is suspiciously broad and probably mis-marked. Cooking support is central to the NSM.                                          |
| Cooking Support | Grandma / Grandpa AI             |            Core | Better than before if this is the distinctive “experienced home cook” support mechanism.                                            |
| Sharing         | Content Creation                 |            Core | Strong core candidate. Community recipe contribution is central.                                                                    |
| Sharing         | Cookbook publishing              |      Supporting | Fine.                                                                                                                               |
| Sharing         | Paywall for premium content      |         Generic | Still questionable. Monetization for paid members should probably be **supporting**, not generic.                                   |
| Rating          | Rating                           |      Supporting | Acceptable, but debatable. If trusted community ratings drive discovery and retention, it may deserve core.                         |

## Biggest remaining issues

### 1. “Member management” is still under-marked

Leaving **Member management** as generic is the biggest remaining weakness. A simple account-management tool is generic, yes. But Larder’s NSM is about reaching **2,500 paying members**, so the capability behind acquisition, activation, retention, renewal, and paid conversion is strategically important.

I would split it:

| Proposed capability                |  Suggested marking |
| ---------------------------------- | -----------------: |
| Account Administration             |            Generic |
| Member Lifecycle Management        |         Supporting |
| Paid Member Conversion & Retention | Core or Supporting |
| Community Moderation & Trust       |         Supporting |

This follows the classification rule that necessity alone does not make something core, but a capability that directly drives the strategy or NSM should not be dismissed as commodity.

### 2. “Cooking support” as Generic conflicts with the NSM

The NSM says Larder covers **cooking skills and support for meal preparation**, supported by chefs and experienced home cooks. A box literally called **Cooking support** being marked generic is therefore hard to defend.

Either remove it because it duplicates other boxes, or rename/decompose it:

| Better capability           | Suggested marking |
| --------------------------- | ----------------: |
| Cooking Guidance Management |        Supporting |
| Expert/Home-Cook Assistance |              Core |
| Cooking Session Utilities   |           Generic |

Right now, “Cooking support” is too broad and sits beside narrower items like “Direct ordering” and “Kitchen tool ordering,” which creates a granularity problem. Capabilities should be stable abilities at comparable levels, not a mix of broad domains and small features.

### 3. Core set is now stronger, but slightly inflated

Current core items:

* Famous chefs partner
* Meal Chooser
* AI proposals
* Community Engagement
* Grandma / Grandpa AI
* Content Creation

That is a plausible core set, but I would compress it into fewer durable capabilities:

| Cleaner core capability                   | Absorbs                                               |
| ----------------------------------------- | ----------------------------------------------------- |
| Community Growth & Engagement             | Community Engagement, competitions, member activation |
| Personalized Meal & Recipe Recommendation | Meal Chooser, AI proposals                            |
| Expert Cooking Support                    | Grandma / Grandpa AI, chef/home-cook support          |
| Community Recipe Contribution             | Content Creation                                      |
| Chef & Expert Network Management          | Famous chefs partner                                  |

That version is more capability-like and less feature-like. The skill guidance warns that maps should avoid systems/features/projects masquerading as capabilities and should use stable noun phrases.

## Rating: supporting or core?

Moving **Rating** from Core to Supporting is defensible, but I would test it.

Ask: does Larder win because its rating/trust layer makes community recipes more reliable than competitors? If yes, **Recipe Rating & Trust Management** is core. If rating is just a standard five-star/comment feature, supporting is correct.

My recommendation: mark it **Supporting** for now, but rename it to **Recipe Rating & Trust Management** and revisit once the product strategy is clearer.

## What improved most

The revised map now has a much better line of sight to the provided NSM and impact map:

| Strategic element             | Now covered?                         |
| ----------------------------- | ------------------------------------ |
| Meal preparation support      | Yes                                  |
| Cooking skills support        | Better                               |
| Chefs and experienced cooks   | Better                               |
| Community sharing             | Yes                                  |
| Recipe rating                 | Yes                                  |
| Competitions/community growth | Much better via Community Engagement |
| 2,500 paying members          | Still weak                           |

## Final recommendation

I would accept this as a **stronger second draft**, with three changes before using it for investment decisions:

1. Split **Member management** into generic admin vs. strategic member lifecycle/conversion.
2. Replace broad **Cooking support** with clearer support capabilities and do not leave all of it generic.
3. Rename feature-like boxes into stable capabilities: **AI proposals → Meal Recommendation Management**, **Grandma / Grandpa AI → Expert Cooking Assistance**, **Famous chefs partner → Chef Partnership Management**, **Rating → Recipe Rating & Trust Management**.

The map is now much closer, but the paid-member growth engine still needs sharper capability coverage.

# Next Prompt:

Check again. We left Grandma/Grandpa and Proposals as core. We use AI for it, but quite specific. What would your Grandma do, when your meal is burned... However, we don't need AI in the capability.  How we do it, does not matter, it is a core capability.

![](./assets/CapabilityMapStep3.jpg)

# Answer:

Yes — I agree with your reasoning.

You are right to remove **AI** from the capability name. The capability is not “AI proposals”; the capability is the business ability to provide **context-specific cooking rescue / meal proposals / practical guidance**. AI is only one possible implementation. That fits the capability rule: the box should describe *what the business can do*, not *how it does it*.

## Updated verdict

This version is stronger again. The changes make the map more capability-oriented:

| Change                                             | Validation                                                                                                                               |
| -------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| **AI proposals → Proposals based on larder, etc.** | Good direction. It removes the technology from the capability. I would still rename it slightly.                                         |
| **Grandma / Grandpa AI → Grandma / Grandpa**       | Correct. The essence is experienced, human-like cooking support, not AI.                                                                 |
| **Cooking support → Cooking aids**                 | Better, but still likely generic if it means timers, conversion tables, utensils, checklists, etc.                                       |
| **Meal Chooser moved to Supporting**               | Defensible, but I would challenge it depending on strategy.                                                                              |
| **Grandma / Grandpa kept Core**                    | Defensible. This may be one of Larder’s distinctive capabilities.                                                                        |
| **Proposals kept Core**                            | Defensible if these proposals are context-aware and based on the user’s larder, preferences, dietary constraints, and cooking situation. |

## The important distinction

Your point is exactly right:

> “What would your Grandma do when your meal is burned?”

That is not a generic recipe feature. It implies a capability like:

**Contextual Cooking Guidance**

or

**Cooking Rescue & Adaptation**

or

**Experienced Cook Assistance**

That can absolutely be **core** if Larder’s promise is not just “find recipes,” but “help people succeed while cooking.”

## Suggested improved names

I would avoid “Grandma / Grandpa” as the formal capability name, because it sounds like a product concept or persona. It is great as a product metaphor, but the capability should be more stable.

| Current label                   | Better capability name                  |                   Marking |
| ------------------------------- | --------------------------------------- | ------------------------: |
| Proposals based on larder, etc. | **Contextual Meal Proposal Management** |                      Core |
| Grandma / Grandpa               | **Experienced Cooking Guidance**        |                      Core |
| Meal Chooser                    | **Meal Selection Support**              |        Supporting or Core |
| Cooking aids                    | **Cooking Utility Support**             |                   Generic |
| Famous chefs partner            | **Chef Partnership Management**         |        Core or Supporting |
| Content Creation                | **Recipe Contribution Management**      |                      Core |
| Rating                          | **Recipe Rating & Trust Management**    | Supporting, possibly Core |

## Core marking: now more defensible

The current core set appears to be:

* Famous chefs partner
* Proposals based on larder, etc.
* Community Engagement
* Grandma / Grandpa
* Content Creation

That is a much more coherent core set than before. Read as a sentence:

> Larder differentiates by combining expert credibility, contextual meal proposals, community engagement, experienced cooking guidance, and user-generated recipe content.

That story fits the NSM much better than a map centered on generic cooking tools.

## Remaining challenges

### 1. “Meal Chooser” may be under-marked

If **Proposals based on larder** is core, then **Meal Chooser** may not need to be a separate capability. It could be part of the same broader capability:

**Personalized Meal Decision Support**

This could include choosing, proposing, adapting, and rescuing meals.

### 2. “Member management” is still too generic

I still would not leave all of **Member management** generic. Account administration is generic, but community membership growth, activation, paid conversion, and retention are not generic for a platform whose goal is 2,500 paying members.

Split it into:

| Capability                         |            Marking |
| ---------------------------------- | -----------------: |
| Account Administration             |            Generic |
| Member Lifecycle Management        |         Supporting |
| Paid Member Conversion & Retention | Supporting or Core |
| Community Moderation               |         Supporting |

### 3. “Paywall for premium content” should probably not be generic

A paywall mechanism is generic. But **Premium Content Monetization** or **Paid Access Management** is more strategic. Since the NSM mentions paying members, I would mark this as **Supporting**, not Generic.

## Final assessment

This version is now much closer to a real capability map. Your reasoning on **Grandma/Grandpa** and **Proposals** is sound: remove AI from the label, keep the underlying capability core.

My strongest recommendation is to rename the two core boxes as:

* **Contextual Meal Proposal Management**
* **Experienced Cooking Guidance**

Those names preserve your strategic intent while keeping the map implementation-agnostic.
