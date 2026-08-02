# Rendering — model format, layout, and the check loop

`scripts/render_glossary.py` turns a model file into an SVG and reports the
geometry problems you cannot see. Read this before Step 5.

## The model file

YAML (or JSON). Everything except `terms` and `edges` has a default.

```yaml
title: Visual Glossary — Help context
subtitle: revised to match Help.yaml      # rendered in grey after the title
canvas: {width: 2040, height: 1290}

contexts:
  help:   {color: "#fff79e", legend: "Help context — owned and described here"}
  recipe: {color: "#8ab4f8", legend: "Recipe context — referenced only"}

legend: {x: 60, y: 1150, w: 700}          # omit to drop the legend box

terms:
  - name: Help Request
    context: help
    x: 470, y: 260, w: 210, h: 118
  - name: Help Provider
    context: help
    x: 1640, y: 60, w: 210, h: 90
    subtitle: Grandma Avatar | Community   # enum values, small grey text
  - name: Step Help Request
    context: help
    x: 300, y: 500, w: 190, h: 104
    wrap_px: 130                           # force an earlier line break

edges:
  - {from: Cook, label: posts, card: "0..*", to: Help Request}
  - {from: Step Help Request, label: is, to: Help Request, kind: is-a}
  - {from: Step Help Request, label: refers to, card: "1", to: Step, kind: cross}
  - {from: Help, label: given by, card: "0..1", to: Cook, route: {via_y: 445}}
```

**Term fields.** `name`, `context`, then either `x`/`y`/`w`/`h` or the grid form
`band`/`col`/`span` (see below). Optional: `subtitle`, `font`, `wrap_px`.

**Edge fields.** `from`, `to`, `label`, `card`, `kind`, `route`.
`kind` is `assoc` (default, solid line, filled arrowhead), `is-a` (hollow
triangle), or `cross` (dashed — crosses a bounded-context boundary).

**Grid form.** Give `bands: [60, 260, 500, 700, 890]` at the top level, then
place terms with `band: 1, col: 3, span: 2`. Columns default to 12 across the
canvas inside a 60px margin. Convenient for a first pass; switch to explicit
coordinates as soon as you're tuning, because that is what the layout below
assumes.

## The standard five-band layout

Bands run top to bottom; this arrangement keeps most edges short and vertical,
which is what stops the picture turning into a hairball.

| Band | y | What goes here |
|---|---|---|
| 1 | ~60 | **Attribute and value terms** — the text payloads, the value enums, the shared leaf types (`Question`, `Picture`, `Help Text`, `Help Provider`) |
| 2 | ~260 | **The two or three core terms** and the actors, spread wide — the request, the response, the person |
| 3 | ~500 | **Specializations**, all at one height: the subtypes of one base on the left, the subtypes of the other on the right |
| 4 | ~690 | **Composed content** — the types that hang off a specialization |
| 5 | ~890 | **Value objects and other-context terms**, with their attributes in a short sixth band below |

Two arrangement rules that matter more than they look:

- **Mirror the pairs.** If the schema subtypes a request three ways and a response
  three ways, put requests left and responses right at the same height. The
  symmetry — or its absence — becomes visible, and asymmetric subtype families
  are a real finding.
- **Keep other-context terms in one region**, not scattered. A reader should be
  able to see the boundary without reading the legend.

Leave a clear horizontal corridor between bands 2 and 3 (~60px). Long edges that
must cross the width get routed through it with `route: {via_y: …}`.

## Routing

Edges are drawn as three-segment orthogonal paths, with the sides chosen from the
relative position of the two boxes. Two or three edges in a typical diagram need
help:

- **`route: {via_y: N}`** — leave both boxes vertically and run across at `y=N`.
  Use it for a long edge that would otherwise cut through a term sitting between
  the two (the corridor above is what `N` should be).
- **`route: {via_x: N}`** — the same, vertically.
- **`route: {force: v}`** or **`{force: h}`** — keep the default routing but pick
  the other pair of sides. Usually enough for a diagonal edge whose midpoint
  vertical clips a neighbour.

Labels are placed automatically: the renderer slides each one along its own path,
outward from the middle, until it clears every sticky and every label already
placed. You do not position labels by hand.

## The check loop

```bash
python scripts/render_glossary.py model.yaml --check
```

Reports four things, all of which are invisible to you until someone opens the
file:

- `STICKY-OVERLAP` — two terms on top of each other.
- `LABEL-ON-STICKY` — a label the auto-placer could not find room for. Means the
  two boxes are too close for the text; open the gap or shorten the label.
- `EDGE-THROUGH-STICKY` — a line crossing an unrelated term. Add a `route`.
- `TEXT-TOO-WIDE` / `TEXT-TOO-TALL` — the name won't fit. Widen the sticky, or
  set `wrap_px` to break earlier.

Iterate until it prints `Geometry clean`. Then render. If `cairosvg` is
available, also produce a PNG — it renders anywhere, and an SVG that opens as XML
in someone's viewer is a wasted deliverable.

```bash
python -c "import cairosvg; cairosvg.svg2png(url='glossary.svg', write_to='glossary.png', output_width=2040)"
```

## Styling

The defaults match a workshop board: `#fff79e` stickies with a soft drop shadow,
`#333` lines, 19px term names (15px on small stickies), 13px edge labels on a
white pad so they stay readable where they cross a line. Keep them unless the
person's existing board uses something else — if you have their original export,
sample the fill colours from it and pass them in `contexts`.

Term names are sentence case as the business writes them, never `camelCase` or
`PascalCase`. If a term ends up matching a schema type name exactly, that's fine;
if it ends up looking like an identifier, the derivation went wrong, not the
styling.