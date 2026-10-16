#!/usr/bin/env python3
"""Direction-aware compatibility check for two AsyncAPI 3.x documents.

`asyncapi diff` classifies channel/operation changes but leaves payload-schema
changes "unclassified". This script fills that gap with the Liskov rules from
references/quality-requirements.md §4:

  * messages this application SENDS  -> payload/headers are postconditions:
      removing a property, making it optional, changing type/format,
      adding an enum value, loosening constraints        => BREAKING / CHECK
  * messages this application RECEIVES -> payload/headers are preconditions:
      adding a required property, tightening constraints,
      removing an enum value, changing type/format       => BREAKING

Every changed `description` is reported as MEANING so a human can confirm the
meaning did not change (tools cannot).

Usage:  python3 payload_compat.py old.yaml new.yaml
Exit code 1 if any BREAKING finding, else 0.
"""
import sys
import yaml

LOWER = ("minLength", "minItems", "minimum", "exclusiveMinimum")
UPPER = ("maxLength", "maxItems", "maximum", "exclusiveMaximum")


def load(path):
    with open(path) as f:
        return yaml.safe_load(f)


def resolve(doc, node, seen=None):
    seen = seen or set()
    while isinstance(node, dict) and "$ref" in node and node["$ref"].startswith("#/"):
        ref = node["$ref"]
        if ref in seen:
            return {}
        seen.add(ref)
        target = doc
        for part in ref[2:].split("/"):
            target = target.get(part.replace("~1", "/").replace("~0", "~"), {})
        extra = {k: v for k, v in node.items() if k != "$ref"}
        node = {**target, **extra}
    return node or {}


def directions(doc):
    """message component key -> set of actions ('send'/'receive')."""
    out = {}
    for op in (doc.get("operations") or {}).values():
        action = op.get("action")
        ch = resolve(doc, op.get("channel", {}))
        refs = op.get("messages") or [{"$ref": "x/" + k} for k in (ch.get("messages") or {})]
        for m in refs:
            ref = m.get("$ref", "")
            key = ref.rsplit("/", 1)[-1]
            # through-channel refs point at the channel's message key; map to component key
            chmsg = (ch.get("messages") or {}).get(key, {})
            comp = chmsg.get("$ref", "").rsplit("/", 1)[-1] or key
            out.setdefault(comp, set()).add(action)
    return out


def compare(doc_o, doc_n, old, new, path, sends, receives, f):
    old, new = resolve(doc_o, old), resolve(doc_n, new)
    if old.get("description") != new.get("description") and old.get("description"):
        f.append(("MEANING", path, "description changed - confirm the meaning is unchanged"))
    for k in ("type", "format"):
        if old.get(k) != new.get(k) and (old.get(k) or new.get(k)):
            f.append(("BREAKING", path, f"{k} changed {old.get(k)!r} -> {new.get(k)!r}"))
    oe, ne = set(map(str, old.get("enum") or [])), set(map(str, new.get("enum") or []))
    if oe or ne:
        if sends and ne - oe:
            f.append(("BREAKING", path, f"enum values added to a sent message: {sorted(ne - oe)} (only safe for tolerant readers)"))
        if receives and oe - ne:
            f.append(("BREAKING", path, f"enum values removed from a received message: {sorted(oe - ne)}"))
        if sends and oe - ne:
            f.append(("CHECK", path, f"enum values no longer sent: {sorted(oe - ne)}"))
    for k in LOWER + UPPER + ("pattern",):
        if old.get(k) == new.get(k):
            continue
        tighter = (
            k == "pattern"
            or (k in LOWER and (old.get(k) is None or (new.get(k) or 0) > old.get(k)))
            or (k in UPPER and (old.get(k) is None or (new.get(k) is not None and new.get(k) < old.get(k))))
        )
        if receives and tighter:
            f.append(("BREAKING", path, f"{k} tightened on a received message: {old.get(k)!r} -> {new.get(k)!r}"))
        if sends and not tighter:
            f.append(("CHECK", path, f"{k} loosened on a sent message: {old.get(k)!r} -> {new.get(k)!r}"))
    op, np_ = old.get("properties") or {}, new.get("properties") or {}
    oreq, nreq = set(old.get("required") or []), set(new.get("required") or [])
    for name in op:
        if name not in np_:
            sev = "BREAKING" if sends else "CHECK"
            f.append((sev, f"{path}.{name}", "property removed"))
    for name in np_:
        if name not in op and name in nreq and receives:
            f.append(("BREAKING", f"{path}.{name}", "new required property on a received message"))
        if name not in op and sends:
            f.append(("INFO", f"{path}.{name}", "property added (safe for tolerant readers)"))
    for name in oreq - nreq:
        if name in np_ and sends:
            f.append(("BREAKING", f"{path}.{name}", "required -> optional on a sent message"))
    for name in (nreq - oreq) & set(op):
        if receives:
            f.append(("BREAKING", f"{path}.{name}", "optional -> required on a received message"))
    if old.get("additionalProperties") is not False and new.get("additionalProperties") is False:
        f.append(("CHECK", path, "additionalProperties: false added - closes the schema for tolerant readers"))
    for name in set(op) & set(np_):
        compare(doc_o, doc_n, op[name], np_[name], f"{path}.{name}", sends, receives, f)
    if "items" in old and "items" in new:
        compare(doc_o, doc_n, old["items"], new["items"], f"{path}[]", sends, receives, f)


def main(old_path, new_path):
    o, n = load(old_path), load(new_path)
    f = []
    for key, ch in (o.get("channels") or {}).items():
        nch = (n.get("channels") or {}).get(key)
        if nch is None:
            f.append(("BREAKING", f"channels.{key}", "channel removed"))
            continue
        if ch.get("address") != nch.get("address"):
            f.append(("BREAKING", f"channels.{key}.address", f"{ch.get('address')!r} -> {nch.get('address')!r}"))
        for m in ch.get("messages") or {}:
            if m not in (nch.get("messages") or {}):
                f.append(("BREAKING", f"channels.{key}.messages.{m}", "message removed from channel"))
    dirs = directions(o)
    for nd in directions(n).items():
        dirs.setdefault(nd[0], set()).update(nd[1])
    omsgs, nmsgs = (o.get("components") or {}).get("messages") or {}, (n.get("components") or {}).get("messages") or {}
    for key, om in omsgs.items():
        if key not in nmsgs:
            continue
        acts = dirs.get(key, {"send", "receive"})
        sends, receives = "send" in acts, "receive" in acts
        om, nm = resolve(o, om), resolve(n, nmsgs[key])
        if om.get("name") != nm.get("name"):
            f.append(("BREAKING", f"messages.{key}.name", f"{om.get('name')!r} -> {nm.get('name')!r}"))
        okey = ((om.get("bindings") or {}).get("kafka") or {}).get("key")
        nkey = ((nm.get("bindings") or {}).get("kafka") or {}).get("key")
        if okey != nkey:
            f.append(("BREAKING", f"messages.{key}.bindings.kafka.key", "partition key changed - per-key ordering may break"))
        for part in ("headers", "payload"):
            if part in om and part in nm:
                compare(o, n, om[part], nm[part], f"messages.{key}.{part}", sends, receives, f)
    order = {"BREAKING": 0, "CHECK": 1, "MEANING": 2, "INFO": 3}
    for sev, path, msg in sorted(set(f), key=lambda x: (order[x[0]], x[1])):
        print(f"{sev:9} {path}: {msg}")
    breaking = sum(1 for x in set(f) if x[0] == "BREAKING")
    print(f"\n{breaking} breaking finding(s).")
    return 1 if breaking else 0


if __name__ == "__main__":
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    sys.exit(main(sys.argv[1], sys.argv[2]))
