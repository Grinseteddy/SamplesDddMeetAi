# Cooking Competition Prototype

This package contains a single-file clickable prototype built from the supplied Domain Story and styled from the supplied codecentric blog screenshot.

Open `index.html` in a browser. The prototype keeps all state in memory; refresh resets it.

## Domain story → prototype mapping

| From the Prototype Brief | Becomes in the prototype | Styled using |
|---|---|---|
| Modules | Chip-style top-level sections: Overview, Competitions, Plan, Recipe, Register, Prepare, Rate, Winner | Screenshot filter chip component and white page surface |
| Screens | Individual in-page views rendered from the chip navigation | Screenshot text-card layout, spacing scale, input styling |
| State machines | Status badges, live story state timeline, and enabled/disabled transition buttons | Badge extrapolated from screenshot chips; buttons derived from chips and visible accent |
| Use-case order | The click-through journey from comparison to crowned winner | Navigation order follows the numbered story |
| Domain model | In-memory seed data for competitions, recipes, meals, ratings, and winner | Form fields and cards derived from screenshot search input and blog cards |
| Actors & roles | Prototype persona selector for Cook, Community Administrator, and Other cooks | Selector derived from screenshot chip/input styling |
| Open questions | Listed below; not silently resolved as production rules | — |

## Built feature mapping

- Step 1: Cook wants to compare meal preparation skill with other cooks → Overview and Competitions screens show the comparison journey.
- Step 2: Community Administrator plans Competition → Plan screen creates a planned competition.
- Step 3: Administrator selects Recipe for Preparation → Recipe screen selects a recipe and opens registration.
- Step 3: Cook registers for Competition → Register screen adds the cook to participants.
- Step 4: Administrator finds Other cooks for Rating → Rate screen assigns peer raters.
- Step 4: Cook prepares Meal and shares online → Prepare screen stores a shared meal link.
- Step 5: Other cooks rate Meal and share online → Rate screen records/updates a rating.
- Step 6: Administrator crowns Winner with Highest rate → Winner screen computes the highest average rate and crowns a winner.

## Style caveats

- Font family is inferred as a clean geometric/system sans-serif because the screenshot does not include font metadata.
- The screenshot shows search, chips, and text cards, but no primary action buttons; primary buttons are extrapolated from the visible turquoise floating accent and the chip shape.
- The screenshot uses a very flat layout with minimal shadows; the prototype uses subtle borders rather than invented card shadows.
- Status colors are token-derived extrapolations because the screenshot does not include status badges.

## Extrapolated beyond the screenshot/story

- A role switcher is included as a prototype affordance instead of authentication.
- Forms for planning, recipe selection, meal sharing, and rating are practical UI extrapolations from the story activities.
- Multiple seed competitions are included so the list view is meaningful and state badges can be reviewed.
- The winner calculation uses highest average rating; tie-breaking is not implemented because the story only says “highest rate.”

## Open questions

- What is the official rating scale and visibility model?
- Can cooks rate competitions in which they participate?
- How are other cooks selected or invited for rating?
- Which online platform/channel is used for sharing meals and ratings?
- What tie-break rule applies when two meals have the same highest rate?
- Does registration open automatically after recipe selection, or only by administrator action?
- Does anyone verify the prepared meal against the recipe before rating?
