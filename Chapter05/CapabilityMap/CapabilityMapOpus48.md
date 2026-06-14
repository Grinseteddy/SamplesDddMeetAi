# Prompt 2026-06-14:

Validate the provided Capability Map based on the NSM: "Larder is a new recipe-sharing platform.It covers cooking skills and the support for meal preparation.Skills and meal preparation are supported by chefs and experienced home cooks.Furthermore, it is a community platform, where members can share and rate recipes.From 100 friends and family in the beginning, the platform grows to more than 2,500 members in two years.The community is supported by different kinds of competitions." and the provided Impact Mapping. Use the provided skill.

![](../assets/CapabilityMap.jpg)
![](../assets/ImpactMappingWithAiStep3.jpg)

# Answer:

Thanks — I've read both diagrams and the NSM narrative, and I'll work as a Devil's-advocate ally of this map: the goal is a map its authors trust *more*, so treat everything below as hypotheses to push back on.

First, let me confirm what I'm reading, since the whole critique hinges on it. In Image 2 the colours are the core/supporting/generic marking per the legend: **blue = Core, green = Supporting, yellow = Generic**. The chevrons (Onboarding → Cooking → Cooking Support → Sharing → Rating) are a value-stream/journey, not a capability tier. If I've got the legend backwards, stop me and the markings flip.

## One-line read

*"Larder must be able to recommend meals (Meal Chooser, AI proposals), manage nutrition, create content and rate it — supported by chef partnerships and planning, on a commodity base of ordering, billing, ads and member admin."* Read that back against the NSM and something's off: the sentence never mentions the thing the NSM says actually wins — **the backing of chefs and experienced home cooks, and the community that grows through competitions.** The map's core is pointed at features; the strategy's core is people and community.

## The interpreted map (as marked vs. my verdict)

| Capability (as drawn) | Marked | My read |
|---|---|---|
| Meal Chooser | Core | Plausible core *if* it's more than a filter — test it |
| AI proposals | Core | **Likely over-marked** — "AI" isn't differentiation |
| Nutrition requirement management | Core | **Likely supporting** — necessary, not the moat |
| Content Creation | Core | **Genuinely core** — keep it |
| Rating | Core | Named in NSM but a rating widget is commodity → supporting |
| Famous chefs partner | Supporting | **Should be core** — this *is* the NSM's differentiator |
| Grandma / Grandpa AI | Supporting | Part of the "expert backing" moat — core candidate |
| Planner | Supporting | Fine as supporting |
| Cookbook publishing | Supporting | Fine |
| Member mgmt, Ads mgmt, Direct/Kitchen-tool ordering, Timer, Paywall | Generic | Reasonable as commodity rails |

Counted up: 5 of 15 ≈ a third marked core — right at the line where the marking starts to lose meaning.

## Biggest risks first

**1. The NSM's stated differentiator is marked *supporting*.** The narrative is explicit: skills and meal prep are *"supported by chefs and experienced home cooks."* That expert/creator backing is the secret sauce — a recipe site without it is a thousand other recipe sites. Yet "Famous chefs partner" is green (supporting) and the experienced-home-cook value is scattered across Content Creation (core) and Grandma/Grandpa AI (supporting). You're treating your moat as context. *How to test cheaply:* if two clones launched tomorrow with identical features, which one wins — the one with the chef/creator network or the one with the better Meal Chooser? If it's the network, that capability (call it **Creator & Expert Network** or **Recipe Sourcing from Chefs/Home Cooks**) is your single most core thing and should be drawn and funded as such. *What resolves it:* name that capability explicitly and mark it core.

**2. "Competitions" is in the NSM with no capability behind it.** The narrative ends on *"the community is supported by different kinds of competitions"* — and there is no contest / challenge / gamification / engagement capability anywhere on the map. Given the metric is *member growth* (100 → 2,500), the named growth engine is missing. This is ambition with nothing to execute it. The impact map's "Cooking club" deliverable has the same problem — no community/social capability backs it either. *What resolves it:* add a **Community Engagement** capability (competitions, clubs, follow-graph) — and given the platform's whole identity is "community," it's a core candidate, not an afterthought.

**3. Core inflation pointed the wrong way.** Several blues are necessity or novelty, not differentiation: **AI proposals** (wiring up an LLM is increasingly commodity — being "AI" doesn't make it un-copyable), **Nutrition requirement management** (real, but it keeps the lights on for the dietary-plan segment rather than winning the market), and **Rating** (the NSM names rating, but a 5-star widget is the definition of commodity — what's core is the *community trust signal* it produces, which is a community capability, not the widget). Meanwhile the genuine moat (Finding 1) and the community engine (Finding 2) are under-marked or absent. So it's not just that there's too much core — the core set and the high-leverage set don't overlap. *Pocket question for each blue:* "What can Larder do with this that a competitor buying the same tools couldn't replicate in a year?" The honest answers will re-sort the colours fast.

## Capability-hygiene notes (briefer)

Several boxes are **feature/product names rather than capabilities** — Meal Chooser, AI proposals, Grandma/Grandpa AI, Timer, Planner, Paywall. They'd fail the swap test (rename the feature and the box name dies). That's not fatal, but marking *features* core/generic is shakier than marking capabilities, because the marking should survive a redesign. "Timer" sitting as a sibling to "Content Creation" is also an L3-feature-beside-L1-capability granularity mismatch.

## What's strong (protect this)

Content Creation as core is right and ties straight to the NSM's "members share recipes." Commoditizing billing, ordering, timer, ads and member admin as generic is the correct instinct — no gold-plating commodity. And organizing by the user journey makes the map readable for non-architects.

## Path forward

Two concrete moves would resolve most of the above: (1) promote the chef/experienced-cook network to a named **core** capability, and (2) add a **Community Engagement / Competitions** capability the NSM clearly demands. Then re-run the "could a clone copy this in a year?" test on the five blues — I'd bet AI proposals, Nutrition, and Rating drop to supporting, leaving a small, honest core of *content + community + expert network*, which is exactly the business the NSM describes.

Where would you push back first — is the Meal Chooser/AI engine genuinely proprietary in a way I'm underrating?

# Next Prompt:

Please check the changes in the capability map.

![](./assets/CapabilityMapStep2.jpg)

# Answer:

Good revision — you clearly took the critique seriously, and most of it landed. Same legend reading as before (blue/person-check = Core, green/magnifier = Supporting, yellow/globe = Generic; the icons just reinforce the colour, no conflict there). Here's what changed and where it leaves us.

## What you fixed (the three load-bearing findings)

- **Famous chefs partner: Supporting → Core.** This was my #1 — the NSM's explicit differentiator ("supported by chefs and experienced home cooks") is now marked as the moat it is.
- **Community Engagement: added, marked Core.** This was my #2 — the NSM's "supported by different kinds of competitions" now has a capability behind it instead of being an ambition with nothing to execute it.
- **Nutrition requirement management and Rating: Core → Supporting.** This was my #3 — necessary, but not where you out-compete anyone. Both correctly demoted.

You also dropped Timer (good — it was an L3 feature masquerading as a peer capability). I'll concede the map now *points* at the right things: the core set finally describes the community/expert business the NSM actually describes, rather than a feature list. That's the important win.

## The one risk that got sharper, not softer

Core inflation. You demoted two but promoted three (Famous chefs partner, Community Engagement, Grandma/Grandpa AI), so **core went from 5/15 to 6/16 — ~38%, now comfortably past the "a third and the marking stops guiding investment" line.** If you had to protect three from a budget cut, which three? My bet on the NSM is the *people/community* trio: **Famous chefs partner, Community Engagement, Content Creation** — those three *are* the strategy.

The soft spot is the other three cores, which are all **AI features**: Meal Chooser, AI proposals, Grandma/Grandpa AI. Here's the falsifiable challenge — *AI built on the same LLM everyone can call is parity, not advantage.* What would make these genuinely core is if they're fed by data nobody else has: your chef network and your community's recipes/ratings. In other words, **they differentiate only by inheriting it from the three people/community capabilities — they don't generate advantage on their own.** So the honest structure might be: the network + community + content are core, and the AI layer is *supporting* (a strong delivery mechanism on top of a proprietary data flywheel) unless you can name the data moat explicitly. *Cheap test:* "Could a competitor wire up the same AI in a quarter?" If yes for Meal Chooser/AI proposals, they're supporting; Grandma/Grandpa AI survives only if it's trained on your experienced-cook network specifically.

## One hygiene flag

The new **"Cooking support"** generic box (yellow, in the Cooking Support lane) collides by name with the *stage header* above it and is vague as a capability — it reads like a catch-all. What ability is it naming that Direct ordering / Kitchen tool ordering / Grandma-Grandpa AI don't already cover? Either rename it to the specific capability or fold it in. Minor, but a box named the same as its lane invites confusion later.

(Also light: Community Engagement is parked under the "Cooking" stage, but it's cross-cutting — competitions and clubs span the whole platform, not the cooking step. The value-stream framing makes it look like a cooking feature. Not worth redrawing, just worth knowing the lane undersells it.)

## Path forward

One decision resolves the remaining risk: **is the AI a moat or a feature?** If the differentiation is the proprietary chef + community data, mark the three people/community capabilities core, drop the AI trio to supporting, and you land at a tight, honest core of three — exactly the business the NSM describes, and a marking that finally tells you where to spend. If you think the AI itself is defensible, tell me what the data moat is — I'm ready to be talked out of it.

# Next Prompt:

Check again. We left Grandma/Grandpa and Proposals as core. We use AI for it, but quite specific. What would your Grandma do, when your meal is burned... However, we don't need AI in the capability.  How we do it, does not matter, it is a core capability.

![](./assets/CapabilityMapStep3.jpg)

# Answer:

You're right, and it's worth saying so plainly: I made a modeling error in the last turn. I used "it's just AI, anyone can build it" as a reason to doubt the marking — but that's reasoning about the *implementation*, and a capability is implementation-agnostic by definition. "We use AI" is the HOW; it has no bearing on whether the *ability* is core. So judging core-ness by the delivery mechanism was exactly the trap the method warns against, and I walked into it. Dropping "AI" from the box names is the correct fix, not a cosmetic one — the capability should survive you swapping AI for a human chef on call or a decision tree, and now the names do.

So let me re-judge them on the right basis — the ability itself, not how it's delivered:

**Grandma / Grandpa — I concede fully, this is core.** "What would your grandmother do when the sauce splits or the meal's burned" is *the* embodiment of the NSM's promise: "skills and meal preparation are supported by chefs and experienced home cooks." That experiential, situational rescue-and-mentorship ability is the differentiator written into the strategy. However you implement it, a competitor without your experienced-cook knowledge base can't reproduce it. Core. No argument from me.

**Proposals based on larder — core, with one thing to keep honest.** I agree it's defensible. But notice *where* its advantage lives: proposing meals from what's in your pantry is a capability several apps have at parity; what makes Larder's version hard to copy is the corpus it proposes *from* — your community recipes plus the chef/creator content. So it's core, but its moat is *borrowed* from Content Creation and the chef network. That's not a demotion — it's a funding signal: if the corpus thins out, this capability quietly decays to parity. Fund the corpus and this stays core; starve it and the proposals become a commodity pantry-matcher. Same logic applies to Meal Chooser.

Two smaller things:

**"Cooking support" → "Cooking aids."** Good — that clears the name collision with the lane.

**One thing to confirm:** Meal Chooser still carries the core (person-check) icon, but its fill now reads closer to green than the other blue cores — so colour and icon may have drifted apart on that one box. If you demoted it to supporting, the icon didn't follow; if it's still core, the colour didn't. Worth syncing the two so the map doesn't say two things at once. Which did you intend?

Where this leaves us: the core set is now genuinely the business the NSM describes — chef network, community engagement, content, and grandparent-style guidance, with proposals/chooser riding on the corpus. That's a coherent, defensible core. The only residual caution is volume — six of sixteen is still a lot to call "core," and the two that lean on borrowed differentiation (Chooser, Proposals) are the ones I'd watch first if a budget cut forced you to rank. But I'm no longer arguing they're mis-marked. You won the methodological point cleanly.

