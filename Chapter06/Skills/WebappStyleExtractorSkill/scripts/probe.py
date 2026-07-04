#!/usr/bin/env python3
"""
probe.py - Precise pixel analysis for a web-app screenshot.

This is the "ruler and eyedropper" for the webapp-style-extractor skill. Claude
looks at the screenshot to decide *what* to measure (this is a button, that's the
body text); this script answers *exactly* what the pixels are, so the extracted
values are grounded in the image instead of guessed.

All subcommands print JSON to stdout.

Subcommands
-----------
  info      Image dimensions. Run first.
  palette   Dominant colors across the image (or a region), with coverage %.
  color-at  Exact color at a point, median-sampled over a small radius so
            anti-aliasing / JPEG noise doesn't throw the read off.
  measure   Pixel size of a box or the distance between two points, optionally
            divided by --scale to give CSS pixels.
  edges     Scan one row or column and report color-change boundaries and the
            segments between them. This is the workhorse for spacing/padding:
            scan a line through a button and you get
            [background | pad | text | pad | background] as measured segments.

Scale factor
------------
Screenshots are often taken on HiDPI/"retina" displays where 2 (or 3) physical
pixels map to 1 CSS pixel. Measure something of known CSS size once, divide, and
pass the ratio as --scale to measure/edges so lengths come back in CSS px.
Colors are unaffected by scale.

Examples
--------
  python probe.py info shot.png
  python probe.py palette shot.png --colors 12
  python probe.py palette shot.png --region 40,40,320,64
  python probe.py color-at shot.png 128 96 --radius 3
  python probe.py measure shot.png --box 40,40,180,44 --scale 2
  python probe.py measure shot.png --from 40,52 --to 220,52 --scale 2
  python probe.py edges shot.png --row 62 --from 40 --to 260 --scale 2
"""
import argparse
import json
import sys

try:
    from PIL import Image
except ImportError:
    sys.exit("Pillow is required: pip install Pillow --break-system-packages")


def _hex(rgb):
    return "#{:02X}{:02X}{:02X}".format(rgb[0], rgb[1], rgb[2])


def _load(path):
    return Image.open(path).convert("RGB")


def _dist(a, b):
    return ((a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 + (a[2] - b[2]) ** 2) ** 0.5


def _parse_ints(s, n, name):
    parts = [p for p in s.replace(" ", "").split(",") if p != ""]
    if len(parts) != n:
        sys.exit(f"--{name} expects {n} comma-separated integers, got: {s!r}")
    try:
        return [int(round(float(p))) for p in parts]
    except ValueError:
        sys.exit(f"--{name} must be numbers, got: {s!r}")


def cmd_info(args):
    img = _load(args.image)
    print(json.dumps({"width": img.width, "height": img.height}, indent=2))


def cmd_palette(args):
    img = _load(args.image)
    if args.region:
        x, y, w, h = _parse_ints(args.region, 4, "region")
        img = img.crop((x, y, x + w, y + h))
    n = max(2, min(args.colors, 64))
    q = img.quantize(colors=n)
    palette = q.getpalette()
    counts = q.getcolors(maxcolors=n * 4) or []
    total = sum(c for c, _ in counts) or 1
    out = []
    for count, idx in counts:
        r, g, b = palette[idx * 3 : idx * 3 + 3]
        out.append(
            {"hex": _hex((r, g, b)), "rgb": [r, g, b],
             "coverage": round(count / total, 4)}
        )
    out.sort(key=lambda d: -d["coverage"])
    print(json.dumps(out, indent=2))


def cmd_color_at(args):
    img = _load(args.image)
    x, y, r = args.x, args.y, max(0, args.radius)
    box = (max(0, x - r), max(0, y - r),
           min(img.width, x + r + 1), min(img.height, y + r + 1))
    crop = img.crop(box)
    px = [crop.getpixel((i, j)) for j in range(crop.height)
          for i in range(crop.width)]
    if not px:
        sys.exit("sample region is empty (point outside image?)")
    med = [sorted(c[i] for c in px)[len(px) // 2] for i in range(3)]
    print(json.dumps({"hex": _hex(med), "rgb": med,
                      "sampled_pixels": len(px)}, indent=2))


def cmd_measure(args):
    scale = args.scale if args.scale and args.scale > 0 else 1.0

    def css(v):
        return round(v / scale, 1)

    if args.box:
        x, y, w, h = _parse_ints(args.box, 4, "box")
        res = {"pixels": {"width": w, "height": h},
               "css": {"width": css(w), "height": css(h)}, "scale": scale}
    elif args.frm and args.to:
        x1, y1 = _parse_ints(args.frm, 2, "from")
        x2, y2 = _parse_ints(args.to, 2, "to")
        dx, dy = abs(x2 - x1), abs(y2 - y1)
        dist = (dx ** 2 + dy ** 2) ** 0.5
        res = {"pixels": {"dx": dx, "dy": dy, "distance": round(dist, 1)},
               "css": {"dx": css(dx), "dy": css(dy), "distance": css(dist)},
               "scale": scale}
    else:
        sys.exit("measure needs either --box x,y,w,h or --from x,y --to x,y")
    print(json.dumps(res, indent=2))


def cmd_edges(args):
    img = _load(args.image)
    scale = args.scale if args.scale and args.scale > 0 else 1.0
    thr = args.threshold

    if args.row is not None:
        y = args.row
        a = args.frm if args.frm is not None else 0
        b = args.to if args.to is not None else img.width
        line = [img.getpixel((x, y)) for x in range(a, b)]
        axis, start = "x", a
    elif args.col is not None:
        x = args.col
        a = args.frm if args.frm is not None else 0
        b = args.to if args.to is not None else img.height
        line = [img.getpixel((x, y)) for y in range(a, b)]
        axis, start = "y", a
    else:
        sys.exit("edges needs either --row Y or --col X")

    if not line:
        sys.exit("scan line is empty (check coordinates)")

    # Grow runs while pixels stay near the running mean of the current segment.
    segments = []
    seg_start = 0
    seg_pixels = [line[0]]
    for i in range(1, len(line)):
        mean = tuple(sum(p[c] for p in seg_pixels) / len(seg_pixels)
                     for c in range(3))
        if _dist(line[i], mean) > thr:
            segments.append((seg_start, i, mean))
            seg_start = i
            seg_pixels = [line[i]]
        else:
            seg_pixels.append(line[i])
    mean = tuple(sum(p[c] for p in seg_pixels) / len(seg_pixels)
                 for c in range(3))
    segments.append((seg_start, len(line), mean))

    seg_out, boundaries = [], []
    for s, e, mean in segments:
        length = e - s
        seg_out.append({
            "from": start + s, "to": start + e, "length_px": length,
            "length_css": round(length / scale, 1),
            "hex": _hex([int(round(c)) for c in mean]),
        })
        if s != 0:
            boundaries.append(start + s)
    print(json.dumps({
        "axis": axis, "scale": scale,
        "boundaries": boundaries, "segments": seg_out,
    }, indent=2))


def main():
    p = argparse.ArgumentParser(description="Precise pixel analysis for a screenshot.")
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("info"); s.add_argument("image"); s.set_defaults(fn=cmd_info)

    s = sub.add_parser("palette")
    s.add_argument("image"); s.add_argument("--colors", type=int, default=12)
    s.add_argument("--region", help="x,y,w,h")
    s.set_defaults(fn=cmd_palette)

    s = sub.add_parser("color-at")
    s.add_argument("image"); s.add_argument("x", type=int); s.add_argument("y", type=int)
    s.add_argument("--radius", type=int, default=2)
    s.set_defaults(fn=cmd_color_at)

    s = sub.add_parser("measure")
    s.add_argument("image"); s.add_argument("--box", help="x,y,w,h")
    s.add_argument("--from", dest="frm", help="x,y"); s.add_argument("--to", help="x,y")
    s.add_argument("--scale", type=float, default=1.0)
    s.set_defaults(fn=cmd_measure)

    s = sub.add_parser("edges")
    s.add_argument("image")
    s.add_argument("--row", type=int); s.add_argument("--col", type=int)
    s.add_argument("--from", dest="frm", type=int); s.add_argument("--to", type=int)
    s.add_argument("--threshold", type=float, default=32.0)
    s.add_argument("--scale", type=float, default=1.0)
    s.set_defaults(fn=cmd_edges)

    args = p.parse_args()
    args.fn(args)


if __name__ == "__main__":
    main()