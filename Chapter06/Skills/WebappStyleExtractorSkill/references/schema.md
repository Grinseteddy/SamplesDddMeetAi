# Output schema

The skill produces a single JSON object. It is designed to be **paste-ready for a
UI rebuild**: primitives (color/type/spacing) come first as reusable tokens, then
`components` reference those primitives so a developer can turn each entry into a
CSS rule or component style with minimal translation.

Every value is a plain JSON value (string/number/array/object) — no comments, no
trailing commas. Validate with `json.loads` before delivering.

## Top-level shape

```
{
  "meta":        { ... },   // provenance + how to trust the numbers
  "colors":      { ... },   // semantic roles + raw palette
  "typography":  { ... },   // text styles as a scale
  "spacing":     { ... },   // spacing scale + base unit
  "radii":       { ... },   // corner radii seen
  "effects":     { ... },   // shadows / borders seen (best-effort)
  "components":  { ... }     // per-component style, referencing the above
}
```

Omit a section only if the user explicitly scoped it out. If a section applies but
nothing was found, include it as an empty object and note why in `meta.notes`.

## meta

```json
"meta": {
  "source": "screenshot.png",
  "imageSize": { "width": 2560, "height": 1440 },
  "scaleFactor": 2,
  "scaleFactorBasis": "measured a 32px control that reads as 16px UI",
  "units": "css-px",
  "notes": [
    "Font families are visual guesses; verify against the real stack.",
    "Only hover state was captured for the primary button; other states unknown."
  ]
}
```

- **scaleFactor** — physical pixels per CSS pixel. `1` for a standard-DPI capture,
  `2`/`3` for HiDPI/retina. All `css`/size numbers elsewhere are already divided by
  this. State how you decided (`scaleFactorBasis`) since it scales every size.
- **notes** — the honesty channel. Anything measured loosely, guessed, or captured
  in only one state goes here so the developer knows what to double-check.

## colors

Semantic roles are what a rebuild actually consumes; `palette` is the raw evidence.

```json
"colors": {
  "roles": {
    "background":    "#0B0B0F",
    "surface":       "#16161C",
    "primary":       "#3B82F6",
    "primaryText":   "#FFFFFF",
    "text":          "#E5E7EB",
    "textMuted":     "#9CA3AF",
    "border":        "#2A2A33",
    "success":       "#22C55E",
    "danger":        "#EF4444"
  },
  "palette": [
    { "hex": "#0B0B0F", "coverage": 0.61, "role": "background" },
    { "hex": "#3B82F6", "coverage": 0.04, "role": "primary" }
  ]
}
```

Only include roles you actually saw. All hex values should come from `color-at` or
`palette` (pixel-sampled), not from memory — that is the whole point of the script.

## typography

Model text as a small set of named styles, ordered largest to smallest. `fontSize`
and `lineHeight` are CSS px (already scale-adjusted). `fontFamily` is a best-guess
CSS stack, most-likely face first.

```json
"typography": {
  "styles": {
    "h1":     { "fontFamily": "Inter, system-ui, sans-serif", "fontSize": 32, "fontWeight": 700, "lineHeight": 40, "letterSpacing": -0.5, "color": "#E5E7EB" },
    "body":   { "fontFamily": "Inter, system-ui, sans-serif", "fontSize": 14, "fontWeight": 400, "lineHeight": 20, "color": "#E5E7EB" },
    "label":  { "fontFamily": "Inter, system-ui, sans-serif", "fontSize": 12, "fontWeight": 500, "lineHeight": 16, "color": "#9CA3AF" }
  }
}
```

`fontSize` is estimated from measured glyph/line height, so it is approximate —
prefer snapping to a plausible value (14, 16, 18, 24…) and note it if unsure.
`fontFamily` and `fontWeight` are visual judgments; keep them in `meta.notes` when
low-confidence.

## spacing

Report the recurring gaps/paddings you measured, plus the base unit they seem to
snap to (4 and 8 are by far the most common in web UIs).

```json
"spacing": {
  "baseUnit": 4,
  "scale": [4, 8, 12, 16, 24, 32, 48],
  "observed": [
    { "context": "button paddingX", "px": 16 },
    { "context": "card gap", "px": 24 }
  ]
}
```

Snap measured values to the nearest scale step when they're within a pixel or two —
real UIs are built on a grid and screenshot measurement has slack. Keep the raw
number in `observed` so nothing is lost.

## radii and effects

```json
"radii":   { "sm": 4, "md": 8, "pill": 9999 },
"effects": {
  "shadows": [ { "name": "card", "value": "0 1px 3px rgba(0,0,0,0.4)" } ],
  "borders": [ { "name": "hairline", "value": "1px solid #2A2A33" } ]
}
```

Shadows can't be read exactly from a flat screenshot — approximate offset/blur/color
from the visible falloff and flag it in `meta.notes`.

## components

The payoff section. Key each entry as `component.variant` (e.g. `button.primary`,
`input.default`, `card`). Reference the tokens above by their resolved value so the
entry is self-contained and pasteable. Include a `states` object only for states
actually visible in the screenshot.

```json
"components": {
  "button.primary": {
    "background": "#3B82F6",
    "color": "#FFFFFF",
    "fontSize": 14,
    "fontWeight": 600,
    "paddingX": 16,
    "paddingY": 10,
    "borderRadius": 8,
    "border": "none",
    "shadow": "0 1px 2px rgba(0,0,0,0.3)",
    "states": {
      "hover": { "background": "#2563EB" }
    }
  },
  "input.default": {
    "background": "#16161C",
    "color": "#E5E7EB",
    "placeholderColor": "#6B7280",
    "border": "1px solid #2A2A33",
    "borderRadius": 6,
    "paddingX": 12,
    "paddingY": 8,
    "fontSize": 14
  },
  "card": {
    "background": "#16161C",
    "border": "1px solid #2A2A33",
    "borderRadius": 12,
    "padding": 20,
    "shadow": "0 1px 3px rgba(0,0,0,0.4)"
  }
}
```

Use `paddingX`/`paddingY` when horizontal and vertical padding differ (common), or a
single `padding` when uniform. Every numeric value is CSS px. Only assert what you
measured or sampled; leave a property out rather than inventing a plausible-looking
value, and record the gap in `meta.notes` if it matters for the rebuild.