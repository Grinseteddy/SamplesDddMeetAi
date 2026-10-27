#!/usr/bin/env python3
"""Keep the duplicated blocks of the per-step reference files identical.

The step files in ../references/ repeat some material on purpose (TDD cycle,
slice refactoring, increment rules, graph queries, reverse check, ...). Each
copy is wrapped in

    <!-- shared:NAME -->
    ...
    <!-- /shared:NAME -->

Usage:
    python3 scripts/check_shared_blocks.py
        Report every block whose copies differ (exit code 1 if any).
    python3 scripts/check_shared_blocks.py --list
        List every block and the files that hold it.
    python3 scripts/check_shared_blocks.py --sync-from references/step-06-domain-model.md [--block NAME]
        Copy the blocks of that file (or only NAME) into every other file
        that holds the same block. Edit one copy, then sync from it.
"""
import argparse
import glob
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PATTERN = re.compile(r"<!-- shared:(?P<name>[^ ]+) -->\n(?P<body>.*?)\n<!-- /shared:(?P=name) -->", re.S)


def files():
    return sorted(glob.glob(os.path.join(ROOT, "references", "*.md")))


def blocks(path):
    text = open(path, encoding="utf-8").read()
    return {m.group("name"): m.group("body") for m in PATTERN.finditer(text)}


def check():
    seen = {}
    for f in files():
        for name, body in blocks(f).items():
            seen.setdefault(name, []).append((f, body))
    bad = 0
    for name, copies in sorted(seen.items()):
        variants = {}
        for f, body in copies:
            variants.setdefault(body, []).append(os.path.relpath(f, ROOT))
        if len(variants) > 1:
            bad += 1
            print(f"DIFFERS  {name}")
            for i, where in enumerate(variants.values(), 1):
                print(f"   variant {i}: {', '.join(where)}")
    if bad:
        print(f"\n{bad} shared block(s) differ. Edit one copy, then run --sync-from that file.")
        return 1
    print(f"OK  {len(seen)} shared blocks, all copies identical.")
    return 0


def list_blocks():
    seen = {}
    for f in files():
        for name in blocks(f):
            seen.setdefault(name, []).append(os.path.basename(f))
    for name, where in sorted(seen.items()):
        print(f"{name}: {', '.join(where)}")
    return 0


def sync(source, only=None):
    src = blocks(source)
    if only:
        if only not in src:
            print(f"{source} holds no block {only}")
            return 1
        src = {only: src[only]}
    changed = 0
    for f in files():
        if os.path.abspath(f) == os.path.abspath(source):
            continue
        text = open(f, encoding="utf-8").read()

        def repl(m):
            name = m.group("name")
            if name in src and m.group("body") != src[name]:
                return f"<!-- shared:{name} -->\n{src[name]}\n<!-- /shared:{name} -->"
            return m.group(0)

        new = PATTERN.sub(repl, text)
        if new != text:
            open(f, "w", encoding="utf-8").write(new)
            changed += 1
            print(f"updated  {os.path.relpath(f, ROOT)}")
    print(f"{changed} file(s) updated.")
    return 0


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--list", action="store_true")
    ap.add_argument("--sync-from")
    ap.add_argument("--block")
    a = ap.parse_args()
    if a.list:
        sys.exit(list_blocks())
    if a.sync_from:
        sys.exit(sync(a.sync_from, a.block))
    sys.exit(check())
