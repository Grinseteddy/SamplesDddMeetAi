# Visual Style — the Core Domain Chart House Look

The generator (`scripts/generate_chart.py`) already encodes this style; read this
when you need to understand or deliberately deviate from it, hand-edit the output
SVG, or explain a choice. The gold-standard is
`assets/example-core-domain-chart.svg` (rendered from `assets/example-spec.json`).
When in doubt, open it and mirror it.

## The frame

- **Canvas** 1404 × 1088, page background `#f2f2f2`.
- **X-axis — Business differentiation**, Low (left) → High (right), drawn as a
  black line with an arrowhead; bold title centred below; "Low"/"High" at the ends.
- **Y-axis — Model complexity**, Low (bottom) → High (top), black line with an
  arrowhead; bold title rotated -90° on the left; "Low"/"High" at the ends.
- Positions in the spec are **normalized 0..1 on each axis** with the origin at
  bottom-left (x=0 Low differentiation, y=0 Low complexity). The generator maps
  them to pixels; never hand-compute pixels in the spec.

## The three regions

Drawn as coloured bands; **Supporting is the base layer**, with Generic and Core
laid over it:

| Region     | Colour    | Hex       | Shape                                   | Border |
|------------|-----------|-----------|-----------------------------------------|--------|
| Generic    | yellow    | `#fff8aa` | left vertical band, full height         | dashed black |
| Supporting | green     | `#d1f09f` | the whole plot (base layer)             | none |
| Core       | blue      | `#90baf9` | upper-right box (high diff + high cplx) | dashed black |

Each region has a centred title (Generic / Supporting / Core) with a small icon
beneath it: **globe** (Generic), **magnifying glass** (Supporting), **person-with-
check** (Core), drawn in `#232f3d`. Region boundaries are controlled by the spec's
`regions` block: `generic_max_x` (right edge of the yellow band), `core_min_x`
(left edge of the blue box), `core_min_y` (bottom edge of the blue box, in
complexity terms). Defaults `0.34 / 0.66 / 0.35` reproduce the gold-standard.

## The markers — the core convention

This is the heart of the chart and the thing most worth getting right:

- **Capability-Map position (origin)** → **grey filled, dashed black outline**
  circle (`fill #b0b0b0`, dashed stroke). This is where the capability sits on the
  *Capability Map* today — the starting point.
- **New position based on the critic (target)** → **bold black hollow** circle
  (`fill none`, solid `#1a1a1a` stroke, ~4px). This is where the Devil's Advocate
  critique argues it *should* sit.
- **The move** → a thin black **arrow from the grey origin to the black target**.
  The generator trims the arrow to the circle edges so it doesn't pierce them.
- A capability the critique does **not** move (or that has no critique attached) is
  drawn as a **single black circle** at its position — no grey circle, no arrow.
  (In the gold-standard, "Capability 2" and the upper "Capability 3" are static.)

> **A note on the convention and the one flag.** The legend reads *grey = Capability
> Map position (origin)*, *black = new position (target)*, arrow origin→target. If
> you instead want the **target** drawn in grey, set `options.swap_marker_colors:
> true` in the spec — but the default matches the gold-standard, and the legend
> text is wired to the default. Confirm the direction with the user if their brief
> and the example disagree, rather than guessing.

## Labels

Each marker has a text label placed via `label_pos`: `right` (default), `left`,
`above`, or `below`. Pick the side that avoids collisions with other markers,
arrows, and region borders — the generator does **not** auto-resolve overlaps, so
this is a judgement call when you write the spec. Keep labels to the capability's
real name; avoid duplicates that make the chart ambiguous.

## Legend

Top-right, outside the plot: the grey dashed circle labelled "Capability Map
position" and the black circle labelled "New position based on critic". The
generator draws it automatically; don't hand-add one.

## Deviating safely

The palette, canvas size, and marker radius are constants at the top of
`generate_chart.py`. If a user wants a different size or palette, change them
there (or post-process the SVG) rather than scattering overrides through the spec —
keep one chart internally consistent. Don't relabel an axis without also rethinking
the regions: the *architecture-migration* variant of the chart repurposes the
y-axis, and the region semantics change with it.