# Prompt 2026-06-06:

Criticize the Impact Map based on the Brainstorming result and Business Model Canvas. 
![](../assets/ImpactMapping.jpg)
![](../assets/BrainstormingConvergence.jpg)
![](../assets/BusinessModelCanvas.jpg)
[SKILL.md](../Skills/ImpactMappingSkill/SKILL.md)

[Canvas cross check](../Skills/ImpactMappingSkill/references/canvas-cross-check.md)

[Collaboration cross check](../Skills/ImpactMappingSkill/references/collaboration-cross-check.md)

[Coherence and tracibility](../Skills/ImpactMappingSkill/references/coherence-and-traceability.md)

[Four level checklist](../Skills/ImpactMappingSkill/references/four-level-checklist.md)

# Answer:

I have both upstream sources here, so let me read the two cross-check reference files to do this properly.Read the impact-mapping-critic role notes and both upstream sources. Here's my critique of the impact map — attacking the map, not anyone's work, and every point is a hypothesis you should feel free to knock down.

## One-line read

Read from the inside out, the map currently says: *"In order to **Larder**, a **Single** will **Shopping**, and we will support that with **Direct ordering**."* That sentence doesn't hold together — and tracing why is the whole critique. The skeleton is a real impact map (clean four columns, actors that genuinely match the canvas), but the WHY and HOW levels aren't yet doing their job.

## Biggest risks first

**1. The goal isn't a goal — it's the product's name.** "Larder" is the app. It names a thing you're building, not a change in the world, and it has no metric, target, or deadline. Every branch beneath it therefore points at nothing measurable: you can't tell whether "Shopping → Direct ordering" succeeded, because there's no outcome it's supposed to move. This is the load-bearing flaw; fix it and most of the others get easier.
- *Test it cheaply:* ask "why are we building Larder?" until you hit a business outcome. Your own canvas answers it — the revenue streams are *monthly member fee* and *ads by partners*. A real goal looks like "X paying members within 12 months" or "Y% of weekly-active users on a paid plan."
- *What resolves it:* a goal phrased as an outcome with a number and a date.

**2. The impacts are feature buckets, not behaviour changes.** "Shopping," "Community," "Budget," "Cooking support" are areas of the product, not things an actor starts/stops/does-differently. Because of that, the map can't answer the only question impact mapping exists to answer: *did a person's behaviour change?* Compare to a real impact — "Singles cook at home more nights per week," "Cooks publish a recipe every week." The tell is that your **canvas already names the behaviours that matter** ("Daily for sharing, Weekly for creating") and *none of them appear as impacts here.*
- *What resolves it:* rewrite each impact as "[actor] does [more/less/differently]," and check that the canvas's daily-sharing and weekly-creating behaviours each show up.

**3. The map was likely built outside-in.** Twelve fairly concrete deliverables, seven vague impacts, a non-goal at the centre — that's the signature of starting from a feature list (your brainstorm leaves) and reverse-justifying upward. The risk isn't that the features are bad; it's that nothing tells you which to *not* build. Deliverables in this technique are bets you prune, and right now almost the entire brainstorm survived intact.

## Level-by-level

**Actors** — strongest level. Both canvas segments (Singles, Young families) and all the canvas partners (kitchenware retailer, grocery store, photo book service, cooks, chefs) made it across. Two soft spots: "Single," "Young family," "Cook" and "Chefs" overlap (is a "Single" not also a "Cook"?), and there's no actor who could *obstruct* the goal — e.g. a regulator, given that allergens/dietary claims carry food-safety liability.

**Impacts** — see risk #2. Also note "Cookbook publishing" is promoted to a full impact here, though in the brainstorm it was a single minor sticky near "Timer," not a cluster. Worth asking whether it earned that weight or just got swept up.

**Deliverables** — held as a fixed list, not options. For each, the useful question is "what's the *smallest* thing that would cause the impact?" — most of these are presented as must-builds.

## Map vs. its sources

**Against the Business Model Canvas (the sharper of the two):**
- **A whole value proposition vanished.** The canvas promises "Recipes for specialized nutrition requirements." The brainstorm backed it (Allergenes, Dietary requirements). Neither survives anywhere on the map — no impact, no deliverable. You're about to build a product that drops one of its three stated value props.
- **Neither revenue stream has a line of sight.** Nothing on the map drives the *monthly member fee* (no impact about conversion, retention, or willingness to pay) and only loosely touches *ads by partners*. The map optimises feature breadth, not money.
- **The cost/infrastructure side is invisible.** "AI proposals," "Grandma/Grandpa AI," and the canvas resource "AI search support" imply real cloud/AI cost (your whole Cost Structure block), but no branch acknowledges that this is the expensive bet. Classic: the map only shows the fun half of the business.
- **A supply gap.** "Easy-to-find recipes" assumes a recipe corpus (canvas resource: "Recipes"), but the behaviour that *produces* recipes — Content Creation — was dropped as an impact and shrunk to one deliverable, "Content." Where do the recipes come from?

**Against the brainstorm:**
- **Dropped clusters worth a second look:** Authentication, the rich Content Creation cluster (Cooking Videos, Making Photos, Remix, Store recipes), Content Rating's detail, Special/Historical recipes, and the Helper cluster (Voice control survived nowhere; only Timer did).
- **Possible premature convergence on the obvious.** The brainstorm's blue stickies look like a flagged-as-distinctive set — Smart kitchen, Voice control, Food pairing, Fridge-photo recipe generation, Allergenes, Larder tracker, Dinner-party planner. *Almost all of them were dropped*, while generic buckets (Shopping, Community) were kept. If blue meant "our differentiators," the map kept the commodity and cut the edge. Worth confirming what blue signified before trusting that call.
- **The product's own namesake feature is missing.** It's called *Larder*, yet the larder/pantry-tracking idea (brainstorm: "Larder tracker," "Search with things I already have," "Fridge-photo recipe generation") didn't reach the deliverables. The thing the app is named after isn't in the plan.

## What's strong

The actor level is genuinely well-grounded — it reflects the canvas faithfully on both the demand and supply sides, which is the hard part many maps fumble. Two of three value props (easy-to-find recipes, cooking support) carry cleanly from canvas → impact → deliverable. And it *is* a proper impact-map shape, which means the fixes are reframing, not rebuilding.

## Path forward

The single highest-leverage move: replace "Larder" with a measured outcome tied to one revenue stream, then re-ask of every impact "is this a behaviour, and does it move that number?" That one change will tell you which of the twelve deliverables to keep and which to park — and will surface whether the specialized-nutrition value prop deserves a branch of its own.

Push back on any of this — especially the premature-convergence read, which rests on my guess about what the blue stickies meant. If blue meant something else, I'll happily drop that point.