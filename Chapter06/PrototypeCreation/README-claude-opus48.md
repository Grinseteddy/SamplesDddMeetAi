# Cooking Competition — Prototype

A clickable prototype built from **two separate inputs kept in separate lanes**:

- **Structure & behavior** come only from the **domain story** (`DomainStoryCompetition.jpg`).
- **Visual style** comes only from the **screenshot** (`ScreenshotBlogpostCodecentric.jpg`), sampled pixel-by-pixel into `style-spec.json`.

Open `cooking-competition-prototype.html`. Use the **“View as”** switcher (top right) to see the three roles. State is in memory only.

---

## What maps to what (feature → story sentence)

| In the prototype | Comes from the story |
|---|---|
| **Competition** entity with a 6-state lifecycle | The verbs across the story: *plans → selects recipe → registration → prepares → rate → crowns* |
| Admin **“Plan a competition”** form (starts in *Planned*) | (2) Community Administrator **plans** Competition |
| Admin **“Select recipe”** → *Recipe selected*; recipe card on detail | (3) Community Administrator **selects** Recipe **for** Preparation |
| Cook **“Register for competition”** | (3) Cook **registers for** Competition |
| Admin **“Open rating”** / rater personas | (4) Community Administrator **finds** Other cooks **for** Rating |
| Cook **“Prepare & share meal”** modal (dish + share online) | (4) Cook **prepares** Meal **and shares** online |
| Other cook **“Rate”** stars on each meal | (5) Other cooks **rate** Meal **and share** online |
| Admin **“Crown winner”** (highest average rating) + 🏆 badge | (6) Community Administrator **crowns** Winner **with** Highest rate |
| Roles: Community Administrator / Cook / Other cook | The three actors in the story |
| The “compare your craft” framing on the list | (1) Cook wants to compare **Meal Preparation Skill with other Cooks** — treated as the app’s *motivation*, not a stored entity |

**State machine (wired & live):** `Planned → Recipe selected → Registration open → Cooking → Rating → Completed`. Each transition button updates the badge, the lifecycle strip, and which actions are available. A **Meal entry** moves `Registered → Submitted/shared → Rated`, and one entry is flagged **Winner** on completion.

Seed data spans every state so no screen is empty: a competition in **Rating** (rate + crown it), one in **Registration** (register, then start cooking), a **Completed** one with a crowned winner, and a fresh **Planned** one you can walk through the whole lifecycle.

---

## Style fidelity (screenshot → tokens)

Every color and component style traces to `style-spec.json`, sampled with the extractor’s probe — not eyeballed:

- **Palette:** white background `#FFFFFF`, near-black titles, muted-grey eyebrows/heading `#9D9EA2`, hairline borders `#DCE0E3`, light chips `#F6F7F9`, and the single **codecentric green `#72D2A2`** used sparingly (brand dot, primary buttons, winner accent).
- **Layout language reused verbatim from the screenshot:** large muted hero heading → search bar with magnifier + clear → filter label → light rounded chips → a three-column card grid whose cards are an uppercase **eyebrow + big bold near-black title + grey excerpt + meta line**. The competitions list *is* that blog-listing layout.

### Caveats carried over (things to verify)
- **Fonts are best-guesses.** The title face reads as a geometric/grotesque sans (codecentric uses a custom brand sans); the prototype requests **Poppins** (display) + **Inter** (body) from Google Fonts and falls back to `system-ui`. If the CDN is blocked in your viewer, you’ll see the system fallback — swap in the real brand font.
- **Sizes are marketing-scale in the screenshot.** The captured page uses very large type; the prototype preserves the *visual character* at conventional web sizes rather than the literal pixel sizes. This was a deliberate scaling decision (noted in `style-spec.json → meta.notes`).
- **Body text color:** glyph centers sample near-black; the palette averaged edge anti-aliasing to `#484647`. Near-black is used.

### Extrapolated beyond what the screenshot showed
The screenshot only showed a search box, filter chips, and blog cards — all in one resting state. These were synthesized from the same tokens (palette, radii, spacing, nearest component) and should be verified against the real design system:
- **Active/selected chip** (black fill, white text) — the screenshot’s chips were all inactive.
- **Status badges, buttons (primary/secondary/ghost), star ratings, forms, the rate modal, the lifecycle strip, and the role switcher** — none appeared in the screenshot.
- Hover/focus states — none were visible; derived from the green accent.

---

## Open questions the story doesn’t settle (decide before a real build)

1. **Sequence ambiguity:** the story numbers two sentences “3” and two “4”. I ordered *select recipe → registration → prepare → find raters → rate*. Confirm whether recipe selection precedes registration, and whether raters are recruited before or during cooking.
2. **Who rates?** I assumed “Other cooks” are community members other than a meal’s author (a cook can’t rate their own dish). Should participating cooks also rate each other, or only a separate judge pool?
3. **Winner rule:** assumed **highest average rating**. Could be highest total, median, or admin discretion.
4. **Rating scale:** assumed **1–5 stars** (from the star icon). Confirm scale and whether ratings are one-per-rater-per-meal.
5. **“Share online”:** modelled as sharing into the community feed. Is it public/external instead?
6. **Multiplicity:** one recipe per competition and one meal per cook are assumed. Multiple allowed?
7. **Registration limits:** deadlines/capacity not shown — needed?
8. **“Meal Preparation Skill” (sentence 1)** is treated as motivation only. If it should be a tracked/scored attribute, that’s a new entity.
