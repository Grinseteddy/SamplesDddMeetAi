---
name: core-domain-chart-author
description: >-
  Create (render) a Core Domain Chart as an SVG in the Nick Tune / DDD Crew
  style, synthesizing a Capability Map (current positions) and a Devil's Advocate
  critique (recommended moves) into one picture: each capability plotted on
  business differentiation × model complexity, with an arrow from its
  Capability-Map position (grey dashed circle) to its critic-derived target (bold
  black circle). Use whenever someone wants to build, draw, generate, render, or
  produce a Core Domain Chart, plot (sub)domains or capabilities as
  core/supporting/generic, or visualize where capabilities should move and why —
  e.g. "make a core domain chart from this capability map and critique," "chart
  these subdomains," "show the target positions with arrows," even if they don't
  name the technique. Pairs with the core-domain-chart-critic (which supplies the
  critique). Produces a single .svg matching the bundled gold-standard example.
  Grounded in Millett & Tune, Patterns, Principles, and Practices of DDD.
---

# Core Domain Chart Author

This skill **renders a Core Domain Chart as an SVG** that matches the bundled
gold-standard (`assets/example-core-domain-chart.svg`). It is the *synthesis* step
in the loop: a **Capability Map** says where each capability sits today, a **Devil's
Advocate critique** says where it should move and why, and this skill draws both —
current position, target position, and the arrow between them.

The chart plots each capability on two axes — **Business differentiation**
(horizontal, Low→High) against **Model complexity** (vertical, Low→High) — over
**Generic / Supporting / Core** regions. Movement is the point: a **grey dashed
circle** marks the Capability-Map (origin) position, a **bold black hollow circle**
marks the critic-derived target, and a thin **arrow** runs origin→target.

Don't hand-author the SVG. Use the generator; it encodes the palette, geometry,
markers, legend, and icons so every chart comes out consistent.

## Workflow

### 1. Gather the two inputs

- **Capability Map** — the capabilities (names) and where each sits today: its
  core/supporting/generic standing and, where available, its complexity. This
  fixes the **grey origin** positions.
- **Devil's Advocate critique** — typically the output of the
  `core-domain-chart-critic` (or `capability-map-critic`). Its findings are the
  **recommended moves**: each "this is mis-placed" becomes a **black target** and
  an arrow.

If you have a map but no critique, you can still plot the portfolio — every
capability becomes a single static black circle at its position (no arrows). If you
have a critique but no map, ask for the map (or the positions); the origins are
what give the arrows meaning. Extract everything you can from what the user already
gave you and ask only what you genuinely can't infer.

### 2. Read the references

- `references/critique-to-moves.md` — **read this first.** How to turn the map +
  critique into origin/target positions and arrow directions, with a table mapping
  each Devil's Advocate finding (core inflation, Hidden Core, Suspect Supporting,
  outsourced core, …) to a move.
- `references/visual-style.md` — the house look: palette, regions, the marker
  convention, labels, legend, and how/when to deviate. Read before editing the
  generator or hand-tuning an SVG.

### 3. Derive positions

For each capability decide: its **origin** (grey, from the Capability Map), whether
the critique **moves** it, and its **target** (black). Positions are normalized
**0..1 on each axis** (0 = Low, 1 = High), origin bottom-left. Static capabilities
(endorsed / no critique) get a target only — no `from`. Choose each `label_pos`
(`right`/`left`/`above`/`below`) to avoid collisions; the generator does not
auto-resolve overlaps.

### 4. Write the spec

Author a JSON spec (see `assets/example-spec.json` for a complete worked example):

```json
{
  "axes": { "x": "Business differentiation", "y": "Model complexity" },
  "regions": { "generic_max_x": 0.34, "core_min_x": 0.66, "core_min_y": 0.35 },
  "capabilities": [
    { "label": "Fraud Detection", "x": 0.80, "y": 0.62,
      "from": { "x": 0.55, "y": 0.55 }, "label_pos": "right" },
    { "label": "Email Sending", "x": 0.18, "y": 0.60, "label_pos": "right" }
  ],
  "options": { "swap_marker_colors": false }
}
```

- `x`, `y` — the **target / final** position → **black** circle.
- `from` *(optional)* — the **Capability-Map origin** → **grey dashed** circle, with
  an arrow drawn `from` → `(x, y)`. Omit for a static capability.
- `regions` — boundaries of the Generic band / Core box; the defaults reproduce the
  gold-standard, adjust only if the data calls for it.
- `options.swap_marker_colors` — leave `false` (grey = origin, black = target, as in
  the example). Only flip if the user explicitly wants the target in grey; see the
  note in `references/visual-style.md`.

### 5. Generate

```bash
python scripts/generate_chart.py SPEC.json -o core-domain-chart.svg
```

(Needs only Python 3 — no third-party packages.) Open the SVG (render it to PNG if
you want to eyeball it) and check for label collisions, markers outside the plot,
or arrows crossing region borders awkwardly; adjust the spec and re-run.

### 6. Present

Save the `.svg` to the outputs directory and present it. Add the short rationale
described in `references/critique-to-moves.md`: one line per arrow (capability, from
→ to, and the critique finding that drove it), then a closing line for the static
ones. The picture shows the moves; your text says *why*.

## House style at a glance

A compressed reminder — `references/visual-style.md` has the full detail.

- **Axes:** X = Business differentiation (Low→High); Y = Model complexity (Low→High).
- **Regions:** Generic `#fff8aa` (left band, dashed), Supporting `#d1f09f` (base
  layer), Core `#90baf9` (upper-right box, dashed); page `#f2f2f2`; icons globe /
  magnifier / person-check in `#232f3d`.
- **Markers:** grey dashed `#b0b0b0` = Capability-Map origin; bold black hollow =
  critic-derived target; thin arrow origin→target; single black circle = static.
- **Don't** hand-write the SVG, add your own legend, or compute pixels in the spec —
  the generator handles all three.

## Bundled resources

- `scripts/generate_chart.py` — the SVG generator. Reads a JSON spec, emits the
  chart. Palette/canvas/radius are constants at the top.
- `assets/example-spec.json` — a complete worked spec (the gold-standard input).
- `assets/example-core-domain-chart.svg` — the gold-standard output; mirror it.
- `references/critique-to-moves.md` — map + critique → positions and arrows.
- `references/visual-style.md` — the full visual specification.

## References

Scott Millett with Nick Tune, *Patterns, Principles, and Practices of
Domain-Driven Design*. Indianapolis: Wrox / Wiley, 2015.

Nick Tune, "Core Domain Patterns."
https://medium.com/nick-tune-tech-strategy-blog/core-domain-patterns-941f89446af5

DDD Crew, *Core Domain Charts* (CC BY 4.0).
https://github.com/ddd-crew/core-domain-charts