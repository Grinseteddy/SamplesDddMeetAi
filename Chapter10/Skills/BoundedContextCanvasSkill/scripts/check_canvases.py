#!/usr/bin/env python3
"""Check a folder of Bounded Context Canvases.

Two independent checks:

  syntax    each file's Mermaid block parses by the rules that actually break
            canvases -- fence present, subgraph/end balanced, labels quoted,
            no reserved node ids, all four columns present, and messages in
            `type - Message` form inside the collaborator nodes.

  symmetry  every outbound message on one canvas is claimed as an inbound
            message on the named collaborator's canvas, with the same name and
            the same type. Asymmetries are findings, not bugs to auto-fix:
            they mean a missing edge on the context map, a misread producer,
            an undeclared off-board collaborator, or a naming slip.

Usage:
    python check_canvases.py bounded-context-canvases/
    python check_canvases.py a.canvas.md b.canvas.md --quiet

Python 3.8+, no third-party packages. Exit code 1 if any ERROR was reported.
"""

import argparse
import glob
import os
import re
import sys
from collections import namedtuple

RESERVED_IDS = {
    "end", "graph", "flowchart", "subgraph", "class", "classdef", "style",
    "click", "call", "href", "linkstyle", "default", "o", "x",
}
MSG_TYPES = {"cmd", "qry", "evt", "?"}
# `type - Message`, one per line inside a collaborator label. The separator may
# be the middle dot or a hyphen.
MSG_LABEL_RE = re.compile(r"^\s*(cmd|qry|evt|\?)\s*[\u00b7\u2022|-]\s*(.+?)\s*$")
REQUIRED_SUBGRAPHS = ("IN", "BCX", "OUT", "META")

ID_RE = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")
FENCE_RE = re.compile(r"```mermaid\s*\n(.*?)^```", re.S | re.M)
NODE_RE = re.compile(r"(?:^|\s)([A-Za-z_][\w]*)\s*[\[\(\{]{1,2}\s*\"(.*?)\"\s*[\]\)\}]{1,2}")
BARE_NODE_RE = re.compile(r"(?:^|\s)([A-Za-z_][\w]*)\s*\[\s*([^\"\]][^\]]*)\]")
SUBGRAPH_RE = re.compile(r"^\s*subgraph\s+([A-Za-z_][\w]*)\s*(?:\[\s*\"(.*?)\"\s*\])?", re.M)
EDGE_RE = re.compile(
    r"(?:^|\s)([A-Za-z_][\w]*)\s*"
    r"(-\.->|-->|---|-\.-|==>|~~~)\s*"
    r"(?:\|\s*\"(.*?)\"\s*\|\s*)?"
    r"([A-Za-z_][\w]*)"
)
CLASS_RE = re.compile(r"^\s*class\s+([\w,\s]+?)\s+(\w+)\s*$", re.M)
TITLE_MD_RE = re.compile(r"^#\s*Bounded Context Canvas\s*[-\u2014:]\s*(.+?)\s*$", re.M)
TITLE_MM_RE = re.compile(r"^%%\s*Bounded Context Canvas\s*[-\u2014:]\s*(.+?)\s*$", re.M)

Finding = namedtuple("Finding", "level path message")
Message = namedtuple("Message", "direction mtype name collaborator offboard path")


def strip_html(text):
    text = re.sub(r"<br\s*/?>", " ", text, flags=re.I)
    text = re.sub(r"<[^>]+>", "", text)
    return " ".join(text.split())


def collaborator_name(label):
    """First line of a collaborator node label, HTML stripped."""
    first = re.split(r"<br\s*/?>", label, flags=re.I)[0]
    return strip_html(first)


def read_canvas(path):
    raw = open(path, encoding="utf-8").read()
    if path.endswith(".mmd"):
        body = raw
    else:
        blocks = FENCE_RE.findall(raw)
        if not blocks:
            return raw, None
        body = blocks[0]
        if len(blocks) > 1:
            body = "\n".join(blocks)
    return raw, body


def canvas_name(raw, body, path):
    for pattern, text in ((TITLE_MD_RE, raw), (TITLE_MM_RE, body or "")):
        m = pattern.search(text)
        if m:
            return m.group(1).strip()
    base = os.path.basename(path)
    return base.split(".canvas.")[0].replace("-", " ").title()


def check_syntax(path, raw, body, findings):
    if body is None:
        findings.append(Finding("ERROR", path, "no ```mermaid block found"))
        return {}, [], set()

    lines = [ln for ln in body.splitlines() if ln.strip() and not ln.strip().startswith("%%")]
    if not lines:
        findings.append(Finding("ERROR", path, "the mermaid block is empty"))
        return {}, [], set()
    if not re.match(r"^\s*(flowchart|graph)\b", lines[0]):
        findings.append(Finding(
            "ERROR", path,
            "the diagram must open with `flowchart LR`; found: %r" % lines[0].strip()[:60]))

    opens = len(re.findall(r"^\s*subgraph\b", body, re.M))
    closes = len(re.findall(r"^\s*end\s*$", body, re.M))
    if opens != closes:
        findings.append(Finding(
            "ERROR", path,
            "%d `subgraph` vs %d `end` -- unbalanced" % (opens, closes)))

    labels = {}
    for nid, label in NODE_RE.findall(body):
        if nid.lower() in ("subgraph", "class", "classdef", "style", "end"):
            continue
        labels[nid] = label
    for nid, title in SUBGRAPH_RE.findall(body):
        if title:
            labels.setdefault(nid, title)

    for nid, label in BARE_NODE_RE.findall(body):
        if nid.lower() in ("classdef", "class", "style", "linkstyle"):
            continue
        findings.append(Finding(
            "ERROR", path,
            'node `%s` has an unquoted label -- write %s["%s"]' % (nid, nid, label.strip())))

    classes = {}
    for ids, cls in CLASS_RE.findall(body):
        for nid in [i.strip() for i in ids.split(",") if i.strip()]:
            classes.setdefault(nid, set()).add(cls)

    edges = EDGE_RE.findall(body)
    seen_ids = set(labels)
    for src, _kind, _label, dst in edges:
        seen_ids.update((src, dst))

    for nid in sorted(seen_ids):
        if nid.lower() in RESERVED_IDS:
            findings.append(Finding(
                "ERROR", path, "`%s` is a reserved word and cannot be a node id" % nid))
        elif not ID_RE.match(nid):
            findings.append(Finding("ERROR", path, "invalid node id `%s`" % nid))

    for nid in sorted(seen_ids - set(labels)):
        findings.append(Finding(
            "WARN", path, "node `%s` is used in an edge but never given a label" % nid))

    for nid, label in labels.items():
        if "#" in label and "&num;" not in label:
            findings.append(Finding(
                "WARN", path,
                "`%s` label contains a bare `#`, which mermaid reads as an entity code" % nid))

    if "bc" not in labels:
        findings.append(Finding(
            "ERROR", path,
            "no `bc` node -- the context head is the anchor for every message edge"))

    for sid in REQUIRED_SUBGRAPHS:
        if not re.search(r"^\s*subgraph\s+%s\b" % sid, body, re.M):
            findings.append(Finding(
                "WARN", path,
                "no `%s` subgraph -- the canvas layout depends on all four columns "
                "being present, even when one is empty" % sid))

    for src, kind, label, dst in edges:
        if label and MSG_LABEL_RE.match(label):
            findings.append(Finding(
                "WARN", path,
                "edge %s -> %s carries the message %r as a label; messages belong "
                "in the collaborator node, one per line" % (src, dst, label)))

    return labels, edges, {n for n, c in classes.items() if "offboard" in c}


def label_lines(label):
    """Split an HTML node label into its rendered lines."""
    return [strip_html(part) for part in re.split(r"<br\s*/?>", label, flags=re.I)]


def extract_messages(path, labels, offboard, findings):
    """Messages live inside `in_*` / `out_*` node labels: the first line names
    the collaborator, and each later line matching `type - Message` is one
    message."""
    messages = []
    for nid, label in sorted(labels.items()):
        if nid.startswith("in_"):
            direction = "in"
        elif nid.startswith("out_"):
            direction = "out"
        else:
            continue

        lines = [ln for ln in label_lines(label) if ln]
        if not lines:
            findings.append(Finding("WARN", path, "`%s` has an empty label" % nid))
            continue

        collaborator = lines[0]
        if MSG_LABEL_RE.match(collaborator):
            findings.append(Finding(
                "ERROR", path,
                "`%s` opens with a message; the first line must name the "
                "collaborator" % nid))
            continue

        found = [MSG_LABEL_RE.match(ln) for ln in lines[1:]]
        found = [m for m in found if m]
        if not found and nid not in offboard:
            findings.append(Finding(
                "INFO", path,
                "`%s` (%s) lists no message -- an empty column is a finding, so "
                "say so in the label" % (nid, collaborator)))
        for m in found:
            messages.append(Message(
                direction, m.group(1), m.group(2), collaborator,
                nid in offboard, path))
    return messages


def check_symmetry(canvases, findings, partial=False):
    """canvases: name -> (path, [Message])."""
    by_name = {name.lower(): name for name in canvases}
    matched = []

    def find(name, direction, mtype, msg, collaborator):
        target = canvases.get(name)
        if not target:
            return None
        for m in target[1]:
            if (m.direction == direction and m.name.lower() == msg.lower()
                    and m.collaborator.lower() == collaborator.lower()
                    and (mtype is None or m.mtype == mtype)):
                return m
        return None

    for name, (path, messages) in sorted(canvases.items()):
        for m in messages:
            peer_key = by_name.get(m.collaborator.lower())
            want = "in" if m.direction == "out" else "out"
            if peer_key is None:
                level = "INFO" if (m.offboard or partial) else "ERROR"
                note = "off-board, declared" if m.offboard else "no canvas for this collaborator"
                findings.append(Finding(
                    level, path,
                    "%sbound `%s %s` %s %s -- %s" % (
                        m.direction, m.mtype, m.name,
                        "to" if m.direction == "out" else "from",
                        m.collaborator, note)))
                continue
            peer = find(peer_key, want, m.mtype, m.name, name)
            if peer is None:
                loose = find(peer_key, want, None, m.name, name)
                if loose is not None and loose.mtype != m.mtype:
                    if m.direction == "out":
                        shape = "`%s` leaves %s as `%s` and arrives at %s as `%s`"
                        pair = (m.name, name, m.mtype, peer_key, loose.mtype)
                    else:
                        shape = "`%s` arrives at %s as `%s` but leaves %s as `%s`"
                        pair = (m.name, name, m.mtype, peer_key, loose.mtype)
                    findings.append(Finding(
                        "ERROR", path,
                        (shape + " -- one side has the coupling wrong") % pair))
                else:
                    findings.append(Finding(
                        "ERROR", path,
                        "%sbound `%s %s` %s %s is not claimed on %s's canvas -- "
                        "missing edge on the map, misread producer, or a naming slip"
                        % (m.direction, m.mtype, m.name,
                           "to" if m.direction == "out" else "from",
                           m.collaborator, peer_key)))
            else:
                matched.append((name, m))
    return matched


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("paths", nargs="+", help="canvas files, or a folder holding them")
    ap.add_argument("--quiet", action="store_true", help="errors only")
    ap.add_argument("--partial", action="store_true",
                    help="checking a subset of the set: a collaborator with no canvas "
                         "here is reported as INFO rather than ERROR")
    args = ap.parse_args()

    files = []
    for p in args.paths:
        if os.path.isdir(p):
            files += sorted(glob.glob(os.path.join(p, "*.canvas.md")))
            files += sorted(glob.glob(os.path.join(p, "*.canvas.mmd")))
        else:
            files.append(p)
    if not files:
        print("no canvas files found (expected *.canvas.md or *.canvas.mmd)")
        return 1

    findings = []
    canvases = {}
    for path in files:
        raw, body = read_canvas(path)
        labels, _edges, offboard = check_syntax(path, raw, body, findings)
        name = canvas_name(raw, body, path)
        if name in canvases:
            findings.append(Finding(
                "ERROR", path,
                "a second canvas is named `%s` (also %s) -- one context, one file"
                % (name, canvases[name][0])))
        canvases[name] = (path, extract_messages(path, labels, offboard, findings))

    matched = check_symmetry(canvases, findings, partial=args.partial)

    order = {"ERROR": 0, "WARN": 1, "INFO": 2}
    findings.sort(key=lambda f: (order[f.level], f.path))
    errors = sum(1 for f in findings if f.level == "ERROR")
    warns = sum(1 for f in findings if f.level == "WARN")

    for f in findings:
        if args.quiet and f.level != "ERROR":
            continue
        print("%-5s %s: %s" % (f.level, os.path.basename(f.path), f.message))

    print("\n%d canvas(es) - %d message pairing(s) matched - %d error(s), %d warning(s)"
          % (len(canvases), len(matched) // 2 or len(matched), errors, warns))
    if errors:
        print("Fix every syntax error. Do NOT edit a canvas to make an asymmetry go "
              "away -- an unmatched or mistyped message is a finding about the map.")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())