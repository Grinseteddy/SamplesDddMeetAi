# Larder — Grandma Rescue Prototype

## Story transcription used
1. Cook prepares Meal.
2. Cook burns Meal and takes Pictures.
3. Cook asks Grandma Avatar for Help with Pictures.
4. Grandma Avatar provides Help to rescue Meal.
5. Cook rescues Meal and takes Pictures.
6. Cook thanks Grandma Avatar and shares Pictures with Community.

## Built feature mapping
- **Prepare/Burn/Add pictures**: steps 1–2.
- **Ask Grandma**: step 3, including question and picture context.
- **Grandma Desk persona**: step 4, with a response action that updates the Cook view.
- **Rescue and after picture**: step 5.
- **Thank and community post**: step 6.
- **Meal state machine**: Preparing → Burned → Help requested → Guidance received → Rescued → Shared.
- **Role switcher**: prototype-only affordance for Cook and Grandma Avatar.

## Visual basis
The interface follows the supplied Larder screenshot: warm off-white canvas, white rounded cards, coral primary actions, muted gray copy, pastel peach/mint/cream media panels, pill controls, and soft shadows.

## Extrapolated components
The source screenshot did not show uploads, chat bubbles, progress steps, or a rescue desk. These were derived from the same palette, spacing, radii, cards, and button treatment.

## Caveats
- The font family is approximated with Nunito/system sans-serif.
- Shadows are approximated from the screenshot.
- The screenshot appears to be a 1× capture.

## Open questions
- Is Grandma Avatar a human community role, an AI persona, or both?
- Are pictures mandatory for asking for help and sharing a rescue?
- Can Grandma decline a request or ask for more information?
- Can a failed rescue be retried, escalated, or marked unsalvageable?
- Does the Community post require moderation or consent from Grandma?
