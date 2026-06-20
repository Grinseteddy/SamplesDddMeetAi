# Prompt 2026-06.20:

Check the Wardley Map of the Larder application based on its Capability Map.

![](../assets/WardleyMapEvolution.jpg)
![](../assets/CapabilityMapStep3.jpg)

# Answer: 

I'll review this Wardley Map against the Capability Map. Let me first consult the relevant skill for how to do this properly. This is a rich pairing to check, so let me first say how I'm reading the two artifacts, then where the Wardley Map and the Capability Map disagree — because that disagreement is where the useful findings are.

A quick note on what the Capability Map can and can't adjudicate: it gives me a clean **differentiation read** (blue = Core/differentiating, green = Supporting, yellow = Generic/commodity), which is exactly the lens needed to test whether the map builds what it should build and rents what it should rent. What it does *not* give me is a North Star Metric or a revenue model, so "which single component matters *most*" stays partly unvalidated — I'll flag that rather than bluff it.

## One-line read

It *is* a real map — genuine user anchors, a need layer, a value chain, and an evolution axis where position carries meaning. The single biggest risk: **the app's namesake differentiator — "Proposals based on larder" (Core/blue) — lands on the map as Product-stage components sitting on commodity AI, with no Genesis component carrying the secret sauce.** If that's accurate, Larder is building its moat out of things three competitors can rent tomorrow.

## How I read the map

**Anchors:** four user types — Young families, Singles, Home cook, Prospect — feeding a need layer (Better cooking skills, Efficient grocery use, Create content, Rate content, Search recipes, Support cooking, Become a member). Multiple anchors on one map is usually a smell, but here they're related cooking segments sharing needs, so it's defensible. "Prospect" is a different lifecycle stage, but it correctly hangs off "Become a member."

**Evolution positions, as I read them:**
- *Custom:* Ask Grandma/Grandpa, Ask chef, Competitions, Ask community, Advanced Search, Specific AI
- *Product (+rental):* Refrigerator, Nutrition, Meal, Rate content, Recipe database, Rating Database
- *Commodity (+utility):* Write recipe, Upload media, Timer, Free hand control, Member Management, Remote meeting, Chat, Gen AI, Media Storage, IDM, Cloud, Mobile

**Movement:** none drawn. It's a frozen snapshot — more on why that matters below.

## Biggest risks first

**1. The core differentiator has no differentiating component under it.** "Proposals based on larder, etc." is marked Core (blue) — it's literally what the product is named for. On the Wardley Map that capability decomposes into *Meal*, *Refrigerator*, and *Nutrition* in **Product**, resting on *Gen AI* in **Commodity** and a small *Specific AI* in Custom. So the visible work of the core feature is done by product-stage components on rentable AI. *Why it worries me:* a Core capability is allowed to stand on commodity plumbing — but only if *some* Genesis/Custom component carries the actual differentiation. Here the only candidate is "Specific AI," and it's drawn low and peripheral. *How to test it cheaply:* "Could three competitors each rent the same Gen AI and assemble the same Meal/Refrigerator/Nutrition components in a quarter? If yes, what stops them?" *What resolves it:* either pull "Specific AI" (the larder-reasoning model) up and treat it as the thing you genuinely build and own, or accept that the proposal engine is commoditising and find the differentiation elsewhere (your chef/grandparent content, your community).

**2. "Content Creation" is marked Core but built entirely from commodity.** Blue on the capability map, yet every component beneath it — *Write recipe*, *Upload media*, *Media Storage*, *Cloud* — sits in Product/Commodity. A value proposition resting entirely on commodity components has no moat. *The falsifiable question:* is content creation here actually differentiating, or is it a commodity CMS that's been over-coloured? If the differentiation is really the *famous-chef and grandparent content* flowing through it (which the capability map *does* mark Core elsewhere), then the moat lives in those relationships, not in "Content Creation" — and the blue marking is on the wrong box. Recategorize it Supporting/Generic, or name the differentiating component explicitly.

**3. The map omits the entire value-*capture* half of the capability map.** Five capabilities have no component on the Wardley Map at all: *Direct ordering*, *Kitchen tool ordering*, *Ads management*, *Paywall for premium content*, and *Cookbook publishing*. Several of those are the actual revenue streams. *Why it matters:* the map shows how Larder *delivers* value but is silent on how it *captures* it, so you can't use it to reason about whether the monetization components are rentable (most are marked Generic, so they should be) or whether any are quietly strategic. *What resolves it:* add them, even just as commodity components low on the chain, so the map can say out loud "rent these, don't build them."

## Map-integrity notes

Nothing structurally broken. Worth a light flag: it's a static snapshot — **no movement is drawn**, even though the map already contains the perfect illustration of why that's risky. You've separated *Specific AI* (Custom) from *Gen AI* (Commodity), which captures AI commoditising in real time. The unanswered climate question is whether "Proposals based on larder" rides Specific AI today but gets eroded as Gen AI catches up. The pieces to show that movement are right there; draw the arrow and name what resists it.

## What's strong (protect this)

It's a genuine map, not a flow diagram wearing a map's clothes — anchors, a chain of needs, and a meaningful evolution axis. Infrastructure (Cloud, Mobile, IDM, Media Storage) is correctly pushed to Commodity, so there's no rebuild-the-utility waste. The community components (Competitions, Ask community) are kept in Custom, consistent with their Core marking — that cross-check *passes*, and it's a good sign your community story is both differentiated and located correctly. And splitting Specific AI from Gen AI is genuinely sophisticated.

The path forward is small and concrete: locate the one differentiating component under "Proposals based on larder" and decide whether you're building it or renting it; fix the "Content Creation" colour-vs-position mismatch; and add the missing monetization components so the map can speak to value capture, not just delivery.

One thing that would sharpen all of this a lot: if you have a North Star Metric or a revenue model for Larder, share it — it would let me say *which* of these components actually deserves the build investment, which the capability map alone can't settle. Happy to push on any single finding harder if you want to argue a position back.
