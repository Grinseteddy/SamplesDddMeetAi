# Prompt 2026-07-01:
Critique the provided domain story based on the provided Wardley Map.

![](../assets/WardleyMapEvolution.jpg)
![](../assets/DomainStoryGrandma.jpg)

# Answer:

I read both artifacts first. Here's my transcription of the domain story so we're critiquing the same thing — please correct me if I've misread any arrow:

1. Cook **prepares** → Meal
2. Cook **burns** → Meal → *and takes* → Pictures
3. Cook **asks** → Grandma Avatar → *for* → Help → *with* → Pictures
4. Grandma Avatar **provides** → Help → *to rescue* → Meal
5. Cook **rescues** → Meal → *and takes* → Pictures
6. Cook **thanks** → Grandma → *and shares* → Pictures → *with* → Community

I'm playing devil's advocate here — everything below is a hypothesis to push back on, not a verdict.

## One-line read

A home cook prepares a meal, burns it, photographs the disaster, asks an AI "Grandma" avatar for help, rescues the dish, and shares the result with the community. It does hold together as **one** concrete happy-path scenario — no branches, no loops, consistent to-be/digitalized scope. That's genuinely well-formed, and rarer than it sounds.

## The risks worth your attention

**1. "Grandma Avatar" (steps 3–4) vs. "Grandma" (step 6) — same actor or two?** This is the load-bearing ambiguity. Steps 3 and 4 name an *Avatar* (an AI persona — and your Wardley map backs this: "Ask Grandma / Grandpa" sits in the custom zone wired to Specific AI / Gen AI). Step 6 drops "Avatar" and just says "Grandma." A developer reads "same node, casual rename"; a domain expert might read "now she's thanking a *real* grandma in the community." Those are different actors and different features. *Test:* ask the team to point at the step-6 icon and say who it is. *Fix:* use one exact term throughout — if it's the AI, it's "Grandma Avatar" in step 6 too.

**2. "Pictures" is used for at least two different things.** In step 2 they're photos of the *burnt* meal (sent to the avatar for diagnosis); in step 5 they're photos of the *rescued* meal; step 6 shares "Pictures" with the community — but which set? You'd share the triumph, not the disaster (or deliberately both, before/after). The single label hides that decision. *Fix:* distinguish them, e.g. "Burnt-meal photo" vs. "Finished-dish photo," so the sharing step is unambiguous.

**3. Step 6 bundles two activities with possibly two subjects.** The chain reads Cook —*thanks*→ Grandma —*and shares*→ Pictures —*with*→ Community. Because "Grandma" sits between the two verbs, a reader can't tell whether the *Cook* shares or *Grandma* shares. Logically it's the Cook, but the grammar leaves it open. *Fix:* split into two numbered sentences — "6 Cook thanks Grandma" and "7 Cook shares Pictures with Community."

## Block-level notes (only where it bites)

- **Verbs are a strength.** prepares / burns / rescues / shares are real domain language, not CRUD or UI gestures — exactly what the method wants. The one soft spot is "**provides** Help," which is a vague catch-all; "advises" or "suggests a fix" would carry more meaning.
- **Meal is one entity through states** (raw → burnt → rescued), redrawn each sentence. That's fine and actually the clean spine of the story — just be aware it's one Meal, not three.
- **Untold alternative (shadow):** the story only shows the rescue *succeeding*. "The avatar can't save it / cook starts over" is a real second scenario — note it exists; don't fork it into this one.

## Story vs. the Wardley Map

The most useful Wardley question is: *does the story spend its detail where the map says the value is?* Here, mostly **yes**, and that's the headline positive. The story centers on "Ask Grandma / Grandpa" and "Support cooking" — the *left-most, most custom, most differentiating* components — and stays away from the commodity edges (Timer, Cloud, Member Management, IDM). The community-sharing tail lines up with the "Create content" user need. Good emphasis discipline.

Two coverage questions, though — both fine for a *single* scenario, worth flagging if this is meant to be *the* flagship story:

- The map weights **Advanced Search** (custom-built) and the **Nutrition / Refrigerator / "Efficient usage of groceries"** cluster heavily as value, and none of that appears here. If this is the representative story, it showcases the rescue/community path and skips the search/nutrition path the map says also matters. A companion story for that need would balance the picture.
- **"Become a member"** is a named user need with a whole chain behind it (Member Management, IDM, Mobile), untouched here. Expected in one scenario — just naming the gap.

One consistency note: the story treats the Avatar as a single human-like actor, collapsing the map's AI stack (Gen AI + Specific AI + Chat + Media Storage). For a domain story that abstraction is *correct* — I'd only push back if you later need the story to explain *where* photos go or *how* the AI answers.

## What's strong (protect it)

It's genuinely one concrete scenario with a natural told rhythm ("burns it, panics, asks for help, saves it, shows it off"), the verbs are domain language, and the emphasis matches your map's differentiator. The fixes above are all cheap: one consistent name for Grandma, two labels for the photos, and one extra sentence in step 6 — and the story gets sharper without losing what already works.

The one I'd resolve first is the Grandma/Avatar identity — everything else is tidy-up, but that one changes what the feature *is*.