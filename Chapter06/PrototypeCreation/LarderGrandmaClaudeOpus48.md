# Prompt 2026-07-11:

Create a prototype from the provided domain story and the provided screenshot. The domain story is part of the Larder application.

![](../assets/LarderStyleExample.jpg)
![](../assets/DomainStoryGrandma.jpg)

# Answer:

I'll build a clickable prototype by interpreting the domain story for structure and extracting the visual style from the screenshot. Let me read the two sub-skills that this orchestrates.Now the style extractor skill:Now let me set up and run the style extraction. First, let me check the probe script and schema.Let me run the probe toolkit against the screenshot. First, image info and scale.Let me look at the screenshot to identify regions, then measure. First the overall palette:Background is warm cream. Let me sample the key accent colors and establish scale by measuring the share button.Let me fix the star and amber samples, and measure text heights to establish scale. My scan coordinates are landing on avatars/emoji — the card content sits higher than I estimated. Let me recalibrate by finding element positions precisely.Let me settle the scale (nav bar ≈120px physical → 60px CSS at 2×, standard) and capture the remaining colors — I need to locate the gold stars precisely.Found the gold star (#F5A625) and confirmed the warm palette. I have enough sampled data. Scale factor is 2 (nav bar measures 120px physical → 60px CSS, standard). Let me write the validated style spec.Track A (style) is done. Now **Track B — interpret the domain story**. Let me read Image 2 carefully and transcribe it, then check the frontend-design skill for build craft before I assemble.

Here's my transcription of the domain story (one concrete scenario):

1. **Cook** *prepares* → **Meal**
2. **Cook** *burns* → **Meal** — *and takes* → **Pictures**
3. **Cook** *asks* → **Grandma Avatar** — *for* → **Help** — *with* → **Pictures**
4. **Grandma Avatar** *provides* → **Help** — *to rescue* → **Meal**
5. **Cook** *rescues* → **Meal** — *and takes* → **Pictures**
6. **Cook** *thanks* → **Grandma** — *and shares* → **Pictures** — *with* → **Community**

Let me check the frontend-design skill for build craft.The story is legible and clearly the "burnt-meal rescue" journey behind Larder. The identity is fixed by the screenshot (warm cream + coral + friendly rounded sans), so I'll follow it exactly and spend no "invent a new identity" latitude.

**Track C — reconciliation (the spec the build follows):**

| From the story | Becomes in the prototype | Styled using |
|---|---|---|
| Cook (§actors) | Persona role + "Viewing as" switcher (Amara/Ben/Chen) | avatar + nav |
| Grandma Avatar / Grandma | AI "Grandma's Rescue" helper (system persona, mocked) | tint card + chat bubbles |
| Community | Discover feed (the screenshot) | card grid + tints + stars |
| Meal (prepares→burns→rescues) | **Meal state machine**: Prepared → Burnt → Rescued → Shared | status badge + transition buttons |
| Help (asks→provides) | Rescue request thread on the meal | secondary card + primary button |
| Pictures | Photo tiles taken at burnt & rescued stages, shared to Discover | tint tiles |
| Sentence order 1–6 | The click-through journey | — |

Now let me build the clickable prototype.Now let me validate the build (Track E) — I'll syntax-check the JSX with a real transpiler to make sure nothing is broken before delivering.Compiles cleanly (38kb, no errors). One refinement for the state machine: once Grandma has helped, the "Mark as rescued" control should also be visible on the meal screen itself (not only inside the rescue chat), so the transition is reachable from both places.Both compile clean. Let me copy the style spec alongside the prototype and present them.Here's the clickable **Larder** prototype. Structure comes only from the domain story; every color, type, and component look comes only from the screenshot.

**Walk the domain story end-to-end:** switch "Viewing as" → **Amara**, open **My Kitchen**, pick *Roast Veg Traybake* (Prepared). Then: *Oh no — it burned 🔥* → *Ask Grandma for help 👵* (opens Grandma's Rescue chat) → *Mark as rescued ✨* → *Thank Grandma 💛* → *Share with community →*. It then appears live on **Discover**. *Chen*'s *Sunday Focaccia* is seeded mid-rescue if you want to jump straight to the thank/share end.

**How each story sentence became a feature**

| # | Story | In the prototype |
|---|---|---|
| 1 | Cook *prepares* Meal | "Start a cook" creates a Meal in `prepared` |
| 2 | Cook *burns* Meal + takes Pictures | "It burned 🔥" → `burnt`, adds a photo tile |
| 3 | Cook *asks* Grandma Avatar *for* Help *with* Pictures | "Ask Grandma" opens the Rescue chat with the burnt pics attached |
| 4 | Grandma Avatar *provides* Help | Grandma replies with rescue advice |
| 5 | Cook *rescues* Meal + takes Pictures | "Mark as rescued ✨" → `rescued`, adds a photo |
| 6 | Cook *thanks* Grandma + *shares* Pictures *with* Community | "Thank Grandma 💛" and "Share with community →" (publishes to Discover) |

The **Meal state machine** (Prepared → Burnt → Rescued → Shared) is live: badges and available buttons update as you drive it. Seed data covers all four states so no screen is empty.

**Style fidelity (from the sampled spec):** cream `#FBF6F0`, coral primary `#FF6B4B`, gold stars `#F5A625`, teal/amber avatars, the pink/mint/peach card tints, pill buttons, soft white cards, warm near-black text `#2C2924`. Scale factor 2 (nav bar 120px physical → 60px CSS).

**Caveats & things I extrapolated, worth a look**
- **Font is a guess.** The screenshot's rounded friendly sans is best-guessed as Poppins-like; it falls back to `system-ui` here. Verify the real face.
- **Radii and the card shadow** are approximated from visual falloff, not exact corner measurement.
- **Grandma is mocked** — canned, meal-aware advice, not a live model. Deliberate per prototype scope; easy to wire to a real assistant later.
- **Extrapolated beyond the screenshot** (built from the same tokens): My Kitchen, Meal detail, the Rescue chat, status badges, the "Start a cook" modal, and lavender/blue tints. The screenshot only showed the Discover feed.
- **Rating on the recipe view comes from the screenshot's copy** ("rate it so the next cook knows"), not from the domain story — I kept it minimal and flag it so it's not mistaken for a story-derived feature.

**Open questions the story doesn't settle** (I did not silently decide these):
- Is the Grandma Avatar an AI or a human helper? I treated it as an in-app AI persona.
- The story only shows the burn→rescue path. I added a **Prepared → Share directly** happy path (flagged) — confirm whether a meal can be shared without ever burning.
- Does "thanks Grandma" equal rating the help, and should shared posts require a rating? Currently thanks and community rating are separate.
- Account/permissions model and whether photos are real uploads vs. the emoji tiles used here.

# I can't run a react prototype. Please create an HTML protoype.

If my reading of any sentence is off, or you'd like the Grandma helper wired to a live model, tell me and I'll adjust.

You've got it — here's a standalone HTML version. No build step, no React, no dependencies: just open it in any browser. Same structure from the domain story and the same sampled style tokens from the screenshot. Let me port it faithfully.Let me validate the HTML renders correctly by loading it in a headless browser and screenshotting a couple of screens.Puppeteer needs a Chromium download that's blocked by the network sandbox. Let me instead syntax-check the embedded JavaScript directly (parse-only, no execution) so I can be sure there are no errors.One escaping bug in a `title` attribute (an apostrophe inside a single-quoted JS string). Let me fix it.JS is clean. Let me do a real functional test with jsdom (pure JS, no browser download needed) — render it and click through the full domain-story flow, catching any runtime errors.All 17 flow checks pass with zero runtime errors. One test assertion flagged "flavour" — let me confirm that's just my over-specific test string and not a real issue.That "FAIL" was just my test guessing the wrong branch — "Roast Veg Tr**bake**" matches the baking advice ("a traybake is a bake"), which is correct. The chat, advice, and every transition render properly with zero runtime errors.

Here's the standalone HTML prototype — double-click to open in any browser, nothing to install.`Larder.html` is a single self-contained file — same prototype, now plain HTML/CSS/JS with no dependencies and no build step. It's identical in structure and style to the React version; I verified the full flow renders and clicks through cleanly.

**Walk the domain story:** open it, keep "Viewing as" on **Amara**, go to **My Kitchen** → *Roast Veg Traybake* (Prepared) → *Oh no — it burned 🔥* → *Ask Grandma for help 👵* → *Mark as rescued ✨* → *Thank Grandma 💛* → *Share with community →*. It then appears live on **Discover**. Switch to **Chen** for *Sunday Focaccia*, seeded mid-rescue.

Everything from the earlier version carries over: the live Meal state machine (Prepared → Burnt → Rescued → Shared), Grandma's Rescue chat, the persona switcher, star ratings, and the "Start a cook" modal — all styled from the sampled tokens (cream `#FBF6F0`, coral `#FF6B4B`, gold stars, tinted cards). The same caveats hold: the font is a best-guess (falls back to system fonts), Grandma is mocked, and the screens beyond Discover plus the community rating are extrapolated/screenshot-driven, as noted before. The open questions (AI vs. human Grandma, the prepare-then-share-directly happy path, whether "thanks" equals rating) are still yours to decide.

[Prototype](./LarderGrandmaClaudeOpus48.html)
[Style](./LarderGrandMaClaudeOpus48-larder-style-spec.json)