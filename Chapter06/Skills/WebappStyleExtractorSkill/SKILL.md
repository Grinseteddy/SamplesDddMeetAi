---
name: webapp-style-extractor
description: >-
  Extract visual formatting/styling from a SCREENSHOT of a web application and
  output it as machine-readable JSON — colors & typography, spacing & sizing, and
  component styles (buttons, inputs, cards, badges) — structured so a developer can
  rebuild the UI in code. Use this whenever the user gives a screenshot, image, or
  mockup of a web app / dashboard / website and wants the formatting, styling,
  design tokens, CSS values, color palette, fonts, spacing, or "how it looks"
  pulled out into JSON (or asks to reverse-engineer / clone / reproduce a UI's look
  from a picture). Trigger even if they just say "get the styling out of this
  screenshot" or "what colors and fonts is this using" — as long as the input is an
  image and they want structured formatting data back. Does NOT cover layout/DOM
  structure extraction, nor reading styles from live HTML/CSS (this is for images).
---

# Web-app style extractor

Turn a screenshot of a web UI into a JSON description of its **colors, typography,
spacing/sizing, and component styles**, precise enough to rebuild the interface in
code.

## The one idea that makes this work

A screenshot is pixels, not CSS. If you *eyeball* a hex code or a font size you will
be wrong often enough to make the output untrustworthy — and untrustworthy is
useless when the goal is to rebuild the UI. So split the job by what each side is
good at:

- **You (looking at the image)** decide *what* things are: this rectangle is a
  primary button, that grey text is a muted label, these cards repeat with a gap
  between them. Semantics and judgment.
- **`scripts/probe.py` (reading the pixels)** answers *exactly what the pixels say*:
  the button fill is `#3B82F6`, the padding is 16px, the card corner radius spans 8
  pixels. Measurement and ground truth.

Never report a color or a measurement you could have sampled instead. Sampling is
cheap and it's the entire reason this skill is better than guessing.

## Setup

1. Find the image. Uploaded files live under `/mnt/user-data/uploads/`. If you
   can already see the screenshot in context, still run the script against the file
   on disk for exact values — your visual read of a color is not exact.
2. Pillow is required (`python3 -c "import PIL"`; if missing,
   `pip install Pillow --break-system-packages`).
3. Read `references/schema.md` — it defines the exact JSON you're producing. Keep it
   open; you're filling in that shape.

## The probe toolkit

`python3 scripts/probe.py <command> <image> [...]` — all output is JSON. Run
`probe.py -h` or see the header of the script for full options. The commands:

- `info` — image width/height. Run first.
- `palette [--colors N] [--region x,y,w,h]` — dominant colors with coverage %.
- `color-at X Y [--radius R]` — exact color at a point, median-sampled so
  anti-aliasing doesn't skew it. This is your eyedropper.
- `measure --box x,y,w,h` or `--from x,y --to x,y` `[--scale S]` — pixel size /
  distance, optionally in CSS px.
- `edges --row Y` or `--col X` `[--from A --to B] [--scale S]` — scan a line and
  return the color-change boundaries and the segments between them. This is your
  ruler for padding and element size: scan a row straight through a button and you
  get back `[background | padding | text | padding | background]` with each
  segment's length. Use it constantly for spacing.

## Workflow

Work top-down: establish scale, pull the primitives (color → type → spacing), then
assemble components from those primitives. Roughly:

### 1. Inspect and lock the scale factor

Run `info`. Then decide the **scale factor** (physical pixels per CSS pixel) before
measuring anything, because it divides every size you report. Screenshots from
retina/HiDPI displays are usually 2× (sometimes 3×): a button that's really 16px
tall shows as 32 pixels.

How to decide: find something whose real CSS size you can guess confidently — body
text is almost always 14–16px, a default input ~36–40px tall, a favicon 16px. Use
`edges`/`measure` to get its pixel size, divide by the likely CSS value, and round
to 1, 2, or 3. State your reasoning in `meta.scaleFactorBasis`. If genuinely
ambiguous, assume the image is 1× **and put the assumption in `meta.notes`** so the
developer can rescale — don't silently guess and hide it. Pass this value as
`--scale` to `measure`/`edges` from here on so lengths come back in CSS px.

### 2. Colors

Run `palette` on the whole image for the dominant colors, then assign each a
semantic role by looking at *where* it appears (the color covering most of the
canvas is the background; the small vivid one on the main button is `primary`; the
near-background greys are surfaces and borders). For any specific color you need —
a button fill, a text color, a border — sample it directly with `color-at` on a
representative pixel rather than trusting the palette bucket. Fill in `colors.roles`
and `colors.palette` per the schema. Every hex must come from the script.

### 3. Typography

For each distinct text style you can see (page title, section heading, body,
labels, button text), capture it as a named style:

- **Size**: measure the glyph height or, better, the line height with `edges` on a
  vertical scan through a line of text, then divide by scale. Snap to a common size
  (12, 14, 16, 18, 20, 24, 32…) unless the measurement clearly says otherwise.
- **Family**: a visual judgment — classify serif / sans-serif / monospace, then give
  a best-guess CSS stack (e.g. `Inter, system-ui, sans-serif`). You cannot read the
  exact font from pixels; put low-confidence guesses in `meta.notes`.
- **Weight**: visual judgment (400 regular / 500 medium / 600 semibold / 700 bold).
- **Color**: sample it with `color-at` on the text strokes.

### 4. Spacing and sizing

Use `edges` to measure the gaps and paddings that recur — button padding, gaps
between cards, form field spacing, container margins. Collect the raw pixel numbers
into `spacing.observed`, infer the `baseUnit` (nearly always 4 or 8), and snap the
recurring values into a clean `scale`. Real UIs sit on a grid, so snapping is
usually right — but keep raw values so nothing is lost.

### 5. Components

This is what the rebuild consumes. For each component type visible (button, input,
card, badge, nav item, toggle…), assemble an entry keyed `component.variant` with
its measured/sampled properties: background, text color, border, border-radius,
padding (X/Y), font size/weight, shadow. Sample colors with `color-at`; get padding
by scanning `edges` straight through the component; read border-radius by measuring
how many pixels the corner curve spans. Only add a `states` entry (hover, foc
/
us,
disabled) for a state actually visible in the screenshot — don't invent states.

### 6. Assemble and validate

Build the full JSON object per `references/schema.md`. Then **validate it** — write
it to a file and `python3 -c "import json;json.load(open('out.json'))"`, or pipe it
through `json.loads`. Invalid JSON defeats the "machine-readable" requirement, so
never hand over unvalidated output.

### 7. Deliver

Save the JSON to `/mnt/user-data/outputs/` and present it. Alongside the file, give
a short plain-language summary: the scale factor you assumed, the headline palette,
and the top 2–3 caveats from `meta.notes` (especially anything a developer must
verify, like font families). Keep it brief — the JSON is the deliverable.

## Honesty is a feature, not a disclaimer

The value of this output is that a developer can trust it. So be explicit about the
seams: pixel-sampled colors are reliable; measured sizes are good but subject to the
scale-factor assumption; font families and shadows are educated guesses. Route every
guess and every single-state capture into `meta.notes`. A smaller, honest spec beats
a larger one padded with invented values — if you didn't measure it, leave it out
and say so rather than filling the gap with a plausible default.

## Common pitfalls

- **Reporting an eyeballed color.** Always `color-at`. Screens, JPEG compression, and
  anti-aliasing make eyeballed hex codes unreliable.
- **Forgetting scale.** A 2× screenshot silently doubles every size. Lock scale in
  step 1 and thread `--scale` through every measurement.
- **Sampling on an edge or a gradient.** Sample `color-at` on a flat interior pixel,
  not on a border, text stroke, or icon, or you'll read a blended value.
- **Inventing a full state matrix.** A static screenshot shows one state per element.
  Only record states you can actually see.
- **Over-snapping.** Snapping 15px→16px is fine; snapping 22px→16px is data loss.
  Snap within a pixel or two; otherwise keep the measured value and note it.