#!/usr/bin/env python3
"""
generate_chart.py — render a Core Domain Chart as SVG from a JSON spec.

The visual style matches the bundled gold-standard
(assets/example-core-domain-chart.svg): a differentiation x complexity plot with
Generic / Supporting / Core regions, and capability markers that show movement
from a Capability-Map position (grey dashed circle) to a critic-derived target
(bold black hollow circle) via an arrow.

Usage:
    python generate_chart.py SPEC.json -o OUTPUT.svg

Spec format (all positions are 0..1 on each axis; 0 = Low, 1 = High):

{
  "axes": { "x": "Business differentiation", "y": "Model complexity" },
  "regions": { "generic_max_x": 0.34, "core_min_x": 0.66, "core_min_y": 0.35 },
  "capabilities": [
    { "label": "Fraud Detection", "x": 0.80, "y": 0.62,
      "from": { "x": 0.55, "y": 0.55 }, "label_pos": "right" },
    { "label": "Email Sending", "x": 0.18, "y": 0.60 }
  ],
  "options": { "swap_marker_colors": false }
}

Per capability:
  x, y         -> the FINAL / target position -> bold black hollow circle.
  from {x,y}   -> OPTIONAL Capability-Map (origin) position -> grey dashed
                  circle, with an arrow drawn from `from` to (x, y).
  label_pos    -> "right" | "left" | "above" | "below"  (default "right").

options.swap_marker_colors: if true, the grey dashed circle marks the TARGET
and the black circle marks the origin (flips the convention).
"""
import argparse
import json
import math
import html

# ---- House palette (extracted from the gold-standard) -----------------------
C_PAGE      = "#f2f2f2"
C_GENERIC   = "#fff8aa"   # yellow
C_SUPPORT   = "#d1f09f"   # green
C_CORE      = "#90baf9"   # blue
C_INK       = "#1a1a1a"   # axes, arrows, text, target stroke
C_GREY      = "#b0b0b0"   # capability-map (origin) circle fill
C_ICON      = "#232f3d"   # region icons
FONT        = "'Noto Sans','Open Sans','Helvetica Neue',Arial,sans-serif"

# ---- Canvas geometry --------------------------------------------------------
W, H        = 1404, 1088
M_LEFT      = 120     # left margin (y-axis label + ticks)
M_RIGHT     = 470     # right margin (legend lives here)
M_TOP       = 28
M_BOTTOM    = 150     # bottom margin (x-axis label)
PLOT_L      = M_LEFT
PLOT_R      = W - M_RIGHT
PLOT_T      = M_TOP
PLOT_B      = H - M_BOTTOM
PLOT_W      = PLOT_R - PLOT_L
PLOT_H      = PLOT_B - PLOT_T

R           = 22      # marker radius
TARGET_SW   = 4       # target circle stroke width
ORIGIN_SW   = 3       # origin (dashed) circle stroke width


def px(nx, ny):
    """Map normalized (0..1, 0..1) with origin bottom-left to pixel coords."""
    x = PLOT_L + nx * PLOT_W
    y = PLOT_B - ny * PLOT_H
    return x, y


def esc(s):
    return html.escape(str(s), quote=True)


def region_icon(kind, cx, cy, s=30):
    """Tiny inline icon centred at (cx, cy). s = nominal size."""
    k = s / 2.0
    col = C_ICON
    if kind == "generic":   # globe
        return (
            f'<g fill="none" stroke="{col}" stroke-width="2.2">'
            f'<circle cx="{cx}" cy="{cy}" r="{k}"/>'
            f'<ellipse cx="{cx}" cy="{cy}" rx="{k*0.45:.1f}" ry="{k}"/>'
            f'<line x1="{cx-k}" y1="{cy}" x2="{cx+k}" y2="{cy}"/>'
            f'<line x1="{cx-k*0.85:.1f}" y1="{cy-k*0.5:.1f}" x2="{cx+k*0.85:.1f}" y2="{cy-k*0.5:.1f}"/>'
            f'<line x1="{cx-k*0.85:.1f}" y1="{cy+k*0.5:.1f}" x2="{cx+k*0.85:.1f}" y2="{cy+k*0.5:.1f}"/>'
            f'</g>'
        )
    if kind == "supporting":  # magnifying glass
        r = k * 0.7
        gx, gy = cx - k*0.2, cy - k*0.2
        return (
            f'<g fill="none" stroke="{col}" stroke-width="2.4" stroke-linecap="round">'
            f'<circle cx="{gx:.1f}" cy="{gy:.1f}" r="{r:.1f}"/>'
            f'<line x1="{gx+r*0.7:.1f}" y1="{gy+r*0.7:.1f}" x2="{cx+k:.1f}" y2="{cy+k:.1f}"/>'
            f'</g>'
        )
    if kind == "core":  # person with check
        hx, hy = cx, cy - k*0.45
        return (
            f'<g fill="none" stroke="{col}" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">'
            f'<circle cx="{hx}" cy="{hy:.1f}" r="{k*0.32:.1f}"/>'
            f'<path d="M {cx-k*0.7:.1f} {cy+k:.1f} q {k*0.7:.1f} -{k*0.8:.1f} {k*1.4:.1f} 0"/>'
            f'<polyline points="{cx+k*0.25:.1f},{cy+k*0.05:.1f} {cx+k*0.55:.1f},{cy+k*0.35:.1f} {cx+k*1.05:.1f},{cy-k*0.35:.1f}"/>'
            f'</g>'
        )
    return ""


def build(spec):
    axes = spec.get("axes", {})
    xlab = axes.get("x", "Business differentiation")
    ylab = axes.get("y", "Model complexity")
    reg = spec.get("regions", {})
    g_max = reg.get("generic_max_x", 0.34)
    c_minx = reg.get("core_min_x", 0.66)
    c_miny = reg.get("core_min_y", 0.35)
    opts = spec.get("options", {})
    swap = bool(opts.get("swap_marker_colors", False))

    out = []
    out.append(
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" '
        f'viewBox="0 0 {W} {H}" font-family="{FONT}">'
    )
    out.append(f'<rect x="0" y="0" width="{W}" height="{H}" fill="{C_PAGE}"/>')

    # arrowhead markers
    out.append(
        '<defs>'
        f'<marker id="axis" markerWidth="14" markerHeight="14" refX="9" refY="5" '
        f'orient="auto"><path d="M0,0 L10,5 L0,10 z" fill="{C_INK}"/></marker>'
        f'<marker id="move" markerWidth="12" markerHeight="12" refX="8" refY="4.5" '
        f'orient="auto"><path d="M0,0 L9,4.5 L0,9 z" fill="{C_INK}"/></marker>'
        '</defs>'
    )

    # ---- region bands (Supporting is the base layer) ----
    gx = PLOT_L + g_max * PLOT_W
    cx0 = PLOT_L + c_minx * PLOT_W
    cy0 = PLOT_B - (1.0 - 0.0) * PLOT_H  # = PLOT_T
    core_bottom = PLOT_B - c_miny * PLOT_H
    out.append(f'<rect x="{PLOT_L}" y="{PLOT_T}" width="{PLOT_W}" height="{PLOT_H}" fill="{C_SUPPORT}"/>')
    # generic band (left, full height, dashed border)
    out.append(
        f'<rect x="{PLOT_L}" y="{PLOT_T}" width="{gx-PLOT_L:.1f}" height="{PLOT_H}" '
        f'fill="{C_GENERIC}" stroke="{C_INK}" stroke-width="2" stroke-dasharray="9 7"/>'
    )
    # core box (upper-right, dashed border)
    out.append(
        f'<rect x="{cx0:.1f}" y="{PLOT_T}" width="{PLOT_R-cx0:.1f}" height="{core_bottom-PLOT_T:.1f}" '
        f'fill="{C_CORE}" stroke="{C_INK}" stroke-width="2" stroke-dasharray="9 7"/>'
    )

    # ---- region labels + icons ----
    def region_label(text, cx_centre, kind):
        out.append(
            f'<text x="{cx_centre:.1f}" y="{PLOT_T+48}" text-anchor="middle" '
            f'font-size="34" fill="{C_INK}">{esc(text)}</text>'
        )
        out.append(region_icon(kind, cx_centre, PLOT_T+92))
    region_label("Generic", (PLOT_L+gx)/2, "generic")
    region_label("Supporting", (gx+cx0)/2, "supporting")
    region_label("Core", (cx0+PLOT_R)/2, "core")

    # ---- axes ----
    out.append(
        f'<line x1="{PLOT_L}" y1="{PLOT_B}" x2="{PLOT_R+30}" y2="{PLOT_B}" '
        f'stroke="{C_INK}" stroke-width="3" marker-end="url(#axis)"/>'
    )
    out.append(
        f'<line x1="{PLOT_L}" y1="{PLOT_B}" x2="{PLOT_L}" y2="{PLOT_T-12}" '
        f'stroke="{C_INK}" stroke-width="3" marker-end="url(#axis)"/>'
    )
    # axis end labels
    out.append(f'<text x="{PLOT_L}" y="{PLOT_B+38}" text-anchor="middle" font-size="24" fill="{C_INK}">Low</text>')
    out.append(f'<text x="{PLOT_R}" y="{PLOT_B+38}" text-anchor="middle" font-size="24" fill="{C_INK}">High</text>')
    out.append(f'<text x="{PLOT_L-30}" y="{PLOT_B}" text-anchor="middle" font-size="24" fill="{C_INK}">Low</text>')
    out.append(f'<text x="{PLOT_L-30}" y="{PLOT_T+6}" text-anchor="middle" font-size="24" fill="{C_INK}">High</text>')
    # axis titles
    out.append(
        f'<text x="{(PLOT_L+PLOT_R)/2:.1f}" y="{PLOT_B+92}" text-anchor="middle" '
        f'font-size="30" font-weight="700" fill="{C_INK}">{esc(xlab)}</text>'
    )
    yt_x, yt_y = PLOT_L-72, (PLOT_T+PLOT_B)/2
    out.append(
        f'<text x="{yt_x}" y="{yt_y:.1f}" text-anchor="middle" font-size="30" font-weight="700" '
        f'fill="{C_INK}" transform="rotate(-90 {yt_x} {yt_y:.1f})">{esc(ylab)}</text>'
    )

    origin_fill = C_GREY
    target_fill = "none"
    if swap:
        origin_fill, target_fill = "none", C_GREY  # rarely used; keeps flag honest

    def circle(cx, cy, kind):
        if kind == "origin":
            return (
                f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{R}" fill="{C_GREY}" '
                f'stroke="{C_INK}" stroke-width="{ORIGIN_SW}" stroke-dasharray="6 5"/>'
            )
        return (
            f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{R}" fill="none" '
            f'stroke="{C_INK}" stroke-width="{TARGET_SW}"/>'
        )

    def label(cx, cy, text, pos):
        pos = pos or "right"
        fs = 24
        if pos == "right":
            return f'<text x="{cx+R+10:.1f}" y="{cy+fs*0.35:.1f}" font-size="{fs}" fill="{C_INK}">{esc(text)}</text>'
        if pos == "left":
            return f'<text x="{cx-R-10:.1f}" y="{cy+fs*0.35:.1f}" text-anchor="end" font-size="{fs}" fill="{C_INK}">{esc(text)}</text>'
        if pos == "above":
            return f'<text x="{cx:.1f}" y="{cy-R-12:.1f}" text-anchor="middle" font-size="{fs}" fill="{C_INK}">{esc(text)}</text>'
        return f'<text x="{cx:.1f}" y="{cy+R+28:.1f}" text-anchor="middle" font-size="{fs}" fill="{C_INK}">{esc(text)}</text>'

    # ---- capabilities ----
    arrows, origins, targets, labels = [], [], [], []
    for cap in spec.get("capabilities", []):
        tx, ty = px(cap["x"], cap["y"])
        frm = cap.get("from")
        if frm:
            fx, fy = px(frm["x"], frm["y"])
            # shorten the segment so it touches circle edges, not centres
            dx, dy = tx - fx, ty - fy
            d = math.hypot(dx, dy) or 1.0
            ux, uy = dx / d, dy / d
            sx, sy = fx + ux * R, fy + uy * R
            ex, ey = tx - ux * (R + 6), ty - uy * (R + 6)
            arrows.append(
                f'<line x1="{sx:.1f}" y1="{sy:.1f}" x2="{ex:.1f}" y2="{ey:.1f}" '
                f'stroke="{C_INK}" stroke-width="2" marker-end="url(#move)"/>'
            )
            origins.append(circle(fx, fy, "origin"))
        targets.append(circle(tx, ty, "target"))
        labels.append(label(tx, ty, cap.get("label", ""), cap.get("label_pos")))
    # draw arrows first, then origins, then targets, then labels (z-order)
    out += arrows + origins + targets + labels

    # ---- legend ----
    lx = PLOT_R + 70
    ly = PLOT_T + 90
    out.append(circle(lx, ly, "origin"))
    out.append(
        f'<text x="{lx+R+18}" y="{ly-4}" font-size="23" fill="{C_INK}">Capability Map</text>'
        f'<text x="{lx+R+18}" y="{ly+24}" font-size="23" fill="{C_INK}">position</text>'
    )
    ly2 = ly + 100
    out.append(circle(lx, ly2, "target"))
    out.append(
        f'<text x="{lx+R+18}" y="{ly2-4}" font-size="23" fill="{C_INK}">New position</text>'
        f'<text x="{lx+R+18}" y="{ly2+24}" font-size="23" fill="{C_INK}">based on critic</text>'
    )

    out.append('</svg>')
    return "\n".join(out)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("spec")
    ap.add_argument("-o", "--output", default="core-domain-chart.svg")
    args = ap.parse_args()
    with open(args.spec) as f:
        spec = json.load(f)
    svg = build(spec)
    with open(args.output, "w") as f:
        f.write(svg)
    print(f"Wrote {args.output} ({len(svg)} bytes)")


if __name__ == "__main__":
    main()