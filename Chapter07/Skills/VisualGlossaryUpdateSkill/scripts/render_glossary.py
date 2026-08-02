#!/usr/bin/env python3
"""
render_glossary.py — draw a Visual Glossary as SVG from a model file, and check
the geometry.

The model is YAML (or JSON) describing terms and edges; see references/rendering.md
for the full format. Minimal example:

    title: Visual Glossary — Ordering
    canvas: {width: 1600, height: 900}
    contexts:
      core:    {color: "#fff79e", legend: "Ordering context — owned here"}
      catalog: {color: "#8ab4f8", legend: "Catalog context — referenced only"}
    terms:
      - {name: Order,    context: core, x: 400, y: 300, w: 200, h: 110}
      - {name: Customer, context: core, x: 100, y: 300, w: 170, h: 100}
    edges:
      - {from: Customer, label: places, card: "0..*", to: Order}

Usage:
    python render_glossary.py model.yaml -o out.svg      # render (also checks)
    python render_glossary.py model.yaml --check         # geometry report only

--check reports label/sticky overlaps, edges routed through unrelated stickies,
overlapping stickies, and text that will not fit its sticky. Iterate on the model
until it is clean: you cannot eyeball an SVG you have not rendered, and a clean
geometry report is the substitute for looking at it.
"""

import argparse
import json
import sys

DEFAULTS = {
    "canvas": {"width": 2040, "height": 1240},
    "edge_color": "#333333",
    "ink": "#1a1a1a",
    "grid": {"margin": 60, "columns": 12, "gap": 20},
}


# ---------------------------------------------------------------- model loading

def load_model(path):
    text = open(path, encoding="utf-8").read()
    if path.endswith(".json"):
        return json.loads(text)
    try:
        import yaml
    except ImportError:
        sys.exit("PyYAML needed for YAML models: pip install pyyaml --break-system-packages")
    return yaml.safe_load(text)


def resolve_geometry(model):
    """Fill in x/y/w/h for terms given as band/col/span on the 12-column grid."""
    cv = {**DEFAULTS["canvas"], **model.get("canvas", {})}
    g = {**DEFAULTS["grid"], **model.get("grid", {})}
    bands = model.get("bands", [])
    colw = (cv["width"] - 2 * g["margin"]) / g["columns"]
    for t in model["terms"]:
        if "x" in t and "y" in t:
            t.setdefault("w", 180)
            t.setdefault("h", 100)
            continue
        if "band" not in t or "col" not in t:
            sys.exit(f"term {t['name']!r} needs either x/y or band/col")
        span = t.get("span", 1)
        t["x"] = g["margin"] + t["col"] * colw
        t["w"] = span * colw - g["gap"]
        t["y"] = bands[t["band"]]
        t.setdefault("h", 100)
    return cv


# ---------------------------------------------------------------- geometry

def rect(t):
    return t["x"], t["y"], t["w"], t["h"]


def overlaps(a, b, pad=0):
    return not (
            a[0] + a[2] <= b[0] + pad or b[0] + b[2] <= a[0] + pad
            or a[1] + a[3] <= b[1] + pad or b[1] + b[3] <= a[1] + pad
    )


def anchor(a, b, force=None):
    ax, ay, aw, ah = rect(a)
    bx, by, bw, bh = rect(b)
    acx, acy, bcx, bcy = ax + aw / 2, ay + ah / 2, bx + bw / 2, by + bh / 2
    dx, dy = bcx - acx, bcy - acy
    if force == "h" or (force is None and abs(dx) > abs(dy) * 1.15):
        if dx > 0:
            return (ax + aw, acy), (bx, bcy), "h"
        return (ax, acy), (bx + bw, bcy), "h"
    if dy > 0:
        return (acx, ay + ah), (bcx, by), "v"
    return (acx, ay), (bcx, by + bh), "v"


def polyline(p0, p1, kind):
    (x0, y0), (x1, y1) = p0, p1
    if kind == "h":
        mx = (x0 + x1) / 2
        return [(x0, y0), (mx, y0), (mx, y1), (x1, y1)]
    my = (y0 + y1) / 2
    return [(x0, y0), (x0, my), (x1, my), (x1, y1)]


def routed(edge, terms):
    a, b = terms[edge["from"]], terms[edge["to"]]
    r = edge.get("route") or {}
    if "via_y" in r:
        y = r["via_y"]
        ax, ay, aw, ah = rect(a)
        bx, by, bw, bh = rect(b)
        ay2 = ay + ah if y > ay else ay
        by2 = by + bh if y > by else by
        return [(ax + aw / 2, ay2), (ax + aw / 2, y), (bx + bw / 2, y), (bx + bw / 2, by2)]
    if "via_x" in r:
        x = r["via_x"]
        ax, ay, aw, ah = rect(a)
        bx, by, bw, bh = rect(b)
        ax2 = ax + aw if x > ax else ax
        bx2 = bx + bw if x > bx else bx
        return [(ax2, ay + ah / 2), (x, ay + ah / 2), (x, by + bh / 2), (bx2, by + bh / 2)]
    p0, p1, kind = anchor(a, b, force=r.get("force"))
    return polyline(p0, p1, kind)


def point_at(pts, t):
    segs = [(pts[i], pts[i + 1],
             abs(pts[i + 1][0] - pts[i][0]) + abs(pts[i + 1][1] - pts[i][1]))
            for i in range(len(pts) - 1)]
    total = sum(s[2] for s in segs) or 1
    d = t * total
    for a, b, ln in segs:
        if d <= ln or ln == 0:
            f = 0 if ln == 0 else d / ln
            return a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f
        d -= ln
    return pts[-1]


def seg_hits_box(a, b, r):
    return not (
            max(a[0], b[0]) <= r[0] + 2 or min(a[0], b[0]) >= r[0] + r[2] - 2
            or max(a[1], b[1]) <= r[1] + 2 or min(a[1], b[1]) >= r[1] + r[3] - 2
    )


def wrap(text, width_px, per_char=9.6):
    per = max(4, int(width_px / per_char))
    words, lines, cur = text.split(), [], ""
    for w in words:
        if cur and len(cur) + 1 + len(w) > per:
            lines.append(cur)
            cur = w
        else:
            cur = f"{cur} {w}".strip()
    if cur:
        lines.append(cur)
    return lines


def label_text(e):
    lab, card = e.get("label", ""), e.get("card", "")
    return f"{lab}  {card}".strip() if card else lab


def place_labels(model, terms):
    """Slide each label along its path until it clears stickies and other labels."""
    boxes = [rect(t) for t in model["terms"]]
    placed, positions = [], {}
    for i, e in enumerate(model["edges"]):
        pts = routed(e, terms)
        text = label_text(e)
        if not text:
            positions[i] = None
            continue
        tw = len(text) * 6.6 + 10
        chosen = None
        for step in range(21):
            for t in (0.5 + step * 0.02, 0.5 - step * 0.02):
                if not 0.06 <= t <= 0.94:
                    continue
                x, y = point_at(pts, t)
                r = (x - tw / 2, y - 11, tw, 22)
                if any(overlaps(r, b) for b in boxes + placed):
                    continue
                chosen = (x, y, r)
                break
            if chosen:
                break
        if not chosen:
            x, y = point_at(pts, 0.5)
            chosen = (x, y, (x - tw / 2, y - 11, tw, 22))
        placed.append(chosen[2])
        positions[i] = (chosen[0], chosen[1], tw, text)
    return positions, placed


# ---------------------------------------------------------------- checking

def check(model, terms, positions, placed):
    issues = []
    boxes = {t["name"]: rect(t) for t in model["terms"]}

    names = list(boxes)
    for i in range(len(names)):
        for j in range(i + 1, len(names)):
            if overlaps(boxes[names[i]], boxes[names[j]]):
                issues.append(f"STICKY-OVERLAP  {names[i]} × {names[j]}")

    for i, pos in positions.items():
        if not pos:
            continue
        x, y, tw, text = pos
        r = (x - tw / 2, y - 11, tw, 22)
        for n, b in boxes.items():
            if overlaps(r, b):
                e = model["edges"][i]
                issues.append(f"LABEL-ON-STICKY  '{text}' ({e['from']}→{e['to']}) sits on {n}")

    for e in model["edges"]:
        pts = routed(e, terms)
        for k in range(len(pts) - 1):
            for n, b in boxes.items():
                if n in (e["from"], e["to"]):
                    continue
                if seg_hits_box(pts[k], pts[k + 1], b):
                    issues.append(f"EDGE-THROUGH-STICKY  {e['from']}→{e['to']} crosses {n}")

    for t in model["terms"]:
        size = t.get("font", 19 if t["h"] >= 96 else 15)
        lines = wrap(t["name"], t.get("wrap_px", t["w"] - 16))
        widest = max(len(l) for l in lines) * size * 0.56
        height = len(lines) * (size + 4) + (16 if t.get("subtitle") else 0)
        if widest > t["w"] - 14:
            issues.append(f"TEXT-TOO-WIDE  {t['name']} (needs ~{widest:.0f}px, has {t['w'] - 14})")
        if height > t["h"] - 10:
            issues.append(f"TEXT-TOO-TALL  {t['name']}")

    return sorted(set(issues))


# ---------------------------------------------------------------- rendering

def render(model, terms, cv, positions):
    ec = model.get("edge_color", DEFAULTS["edge_color"])
    ink = model.get("ink", DEFAULTS["ink"])
    ctx = model.get("contexts", {})
    W, H = cv["width"], cv["height"]

    out = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" '
        f'viewBox="0 0 {W} {H}" font-family="Noto Sans, Segoe UI, Helvetica, sans-serif">',
        f'<rect width="{W}" height="{H}" fill="#ffffff"/>',
        '<defs>'
        f'<marker id="a" markerWidth="9" markerHeight="9" refX="8" refY="3" orient="auto">'
        f'<path d="M0,0 L9,3 L0,6 z" fill="{ec}"/></marker>'
        f'<marker id="t" markerWidth="13" markerHeight="13" refX="12" refY="5" orient="auto">'
        f'<path d="M0,0 L12,5 L0,10 z" fill="#ffffff" stroke="{ec}" stroke-width="1.4"/></marker>'
        '</defs>',
    ]
    if model.get("title"):
        sub = model.get("subtitle", "")
        subtag = (f'<tspan font-size="17" font-weight="400" fill="#666"> ({sub})</tspan>'
                  if sub else "")
        out.append(f'<text x="60" y="46" font-size="26" font-weight="600" fill="{ink}">'
                   f'{model["title"]}{subtag}</text>')

    for i, e in enumerate(model["edges"]):
        pts = routed(e, terms)
        kind = e.get("kind", "assoc")
        dash = ' stroke-dasharray="7 5"' if kind == "cross" else ""
        marker = "t" if kind == "is-a" else "a"
        d = "M " + " L ".join(f"{x:.0f} {y:.0f}" for x, y in pts)
        out.append(f'<path d="{d}" fill="none" stroke="{ec}" stroke-width="1.6"{dash} '
                   f'marker-end="url(#{marker})"/>')
        pos = positions.get(i)
        if pos:
            x, y, tw, text = pos
            out.append(
                f'<rect x="{x - tw/2:.0f}" y="{y - 11:.0f}" width="{tw:.0f}" height="21" '
                f'rx="4" fill="#ffffff" opacity="0.94"/>'
                f'<text x="{x:.0f}" y="{y + 4:.0f}" font-size="13" fill="{ec}" '
                f'text-anchor="middle">{text}</text>')

    for t in model["terms"]:
        x, y, w, h = rect(t)
        fill = ctx.get(t.get("context", ""), {}).get("color", "#fff79e")
        out.append(f'<rect x="{x+3:.0f}" y="{y+3:.0f}" width="{w:.0f}" height="{h:.0f}" '
                   f'fill="#000" opacity="0.07"/>'
                   f'<rect x="{x:.0f}" y="{y:.0f}" width="{w:.0f}" height="{h:.0f}" fill="{fill}"/>')
        size = t.get("font", 19 if h >= 96 else 15)
        lines = wrap(t["name"], t.get("wrap_px", w - 16))
        total = len(lines) * (size + 4) + (16 if t.get("subtitle") else 0)
        ty = y + h / 2 - total / 2 + size
        for ln in lines:
            out.append(f'<text x="{x + w/2:.0f}" y="{ty:.0f}" font-size="{size}" fill="{ink}" '
                       f'text-anchor="middle">{ln}</text>')
            ty += size + 4
        if t.get("subtitle"):
            out.append(f'<text x="{x + w/2:.0f}" y="{ty + 2:.0f}" font-size="11.5" fill="#555" '
                       f'text-anchor="middle">{t["subtitle"]}</text>')

    lg = model.get("legend")
    if lg:
        lx, ly = lg.get("x", 60), lg.get("y", H - 140)
        rows = [(c.get("color"), c.get("legend")) for c in ctx.values() if c.get("legend")]
        out.append(f'<rect x="{lx}" y="{ly}" width="{lg.get("w", 700)}" height="104" rx="6" '
                   f'fill="#fafafa" stroke="#e0e0e0"/>')
        yy = ly + 20
        for color, text in rows[:2]:
            out.append(f'<rect x="{lx+18}" y="{yy}" width="26" height="20" fill="{color}"/>'
                       f'<text x="{lx+54}" y="{yy+16}" font-size="13.5" fill="{ink}">{text}</text>')
            yy += 34
        out.append(f'<path d="M{lx+400} {ly+30} L{lx+448} {ly+30}" stroke="{ec}" '
                   f'stroke-width="1.6" marker-end="url(#t)"/>'
                   f'<text x="{lx+470}" y="{ly+35}" font-size="13.5" fill="{ink}">is a</text>')
        out.append(f'<path d="M{lx+400} {ly+64} L{lx+448} {ly+64}" stroke="{ec}" stroke-width="1.6" '
                   f'stroke-dasharray="7 5" marker-end="url(#a)"/>'
                   f'<text x="{lx+470}" y="{ly+69}" font-size="13.5" fill="{ink}">'
                   f'crosses a context boundary</text>')

    out.append("</svg>")
    return "\n".join(out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("model")
    ap.add_argument("-o", "--out", default=None)
    ap.add_argument("--check", action="store_true", help="geometry report only")
    args = ap.parse_args()

    model = load_model(args.model)
    cv = resolve_geometry(model)
    terms = {t["name"]: t for t in model["terms"]}

    for e in model["edges"]:
        for side in ("from", "to"):
            if e[side] not in terms:
                sys.exit(f"edge references unknown term: {e[side]!r}")

    positions, placed = place_labels(model, terms)
    issues = check(model, terms, positions, placed)

    if issues:
        print(f"{len(issues)} geometry issue(s):")
        for i in issues:
            print("  " + i)
    else:
        print("Geometry clean: no overlaps, no edges through stickies, all text fits.")

    if args.check:
        return
    out = args.out or args.model.rsplit(".", 1)[0] + ".svg"
    open(out, "w", encoding="utf-8").write(render(model, terms, cv, positions))
    print(f"Wrote {out}  ({len(model['terms'])} terms, {len(model['edges'])} edges)")


if __name__ == "__main__":
    main()