#!/usr/bin/env python3
"""
inspect_schema.py — mechanical defect scan for a JSON Schema / OpenAPI components file.

Finds the class of problem that is invisible when you read a schema and obvious
when you parse it: broken $refs, `required:` that parsed as null or as a mapping,
required names no property defines, examples that fail their own pattern or type,
type/format pairings that aren't in the OpenAPI registry, orphan schemas,
allOf subtype families without a discriminator, and unescaped dots in patterns.

Usage:
    python inspect_schema.py <file.yaml|file.json> [--json]

Exit code is always 0 — findings go to stdout. This is a reporting tool, not a gate.

Requires PyYAML for YAML input (`pip install pyyaml --break-system-packages`).
JSON input needs nothing.
"""

import argparse
import json
import re
import sys
from collections import defaultdict

# --- OpenAPI 3.1 / JSON Schema format registry (the pairings that are actually defined)
VALID_FORMATS = {
    "integer": {"int32", "int64"},
    "number": {"float", "double"},
    "string": {
        "date", "date-time", "time", "duration", "password", "byte", "binary",
        "email", "idn-email", "hostname", "idn-hostname", "ipv4", "ipv6",
        "uri", "uri-reference", "iri", "iri-reference", "uuid", "uri-template",
        "json-pointer", "relative-json-pointer", "regex",
    },
}

KNOWN_VENDOR_KEYS = {"x-extensible-enum", "x-enum-varnames", "x-enum-descriptions"}

findings = []


def add(severity, code, where, message):
    findings.append(
        {"severity": severity, "code": code, "where": where, "message": message}
    )


# --------------------------------------------------------------------------
# Loading
# --------------------------------------------------------------------------

class DuplicateKeyDetector(dict):
    """Marker type — see load()."""


def load(path):
    text = open(path, encoding="utf-8").read()
    if path.endswith((".json",)):
        return json.loads(text), text
    try:
        import yaml
    except ImportError:
        sys.exit("PyYAML required for YAML input: pip install pyyaml --break-system-packages")

    # Detect duplicate mapping keys, which safe_load silently collapses.
    class Loader(yaml.SafeLoader):
        pass

    def no_duplicates(loader, node, deep=False):
        seen = {}
        for key_node, value_node in node.value:
            key = loader.construct_object(key_node, deep=deep)
            if key in seen:
                add("blocking", "DUP-KEY", str(key),
                    f"duplicate mapping key `{key}` — the later one silently wins")
            seen[key] = True
        return yaml.SafeLoader.construct_mapping(loader, node, deep)

    Loader.add_constructor(
        yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, no_duplicates
    )
    return yaml.load(text, Loader), text


def schemas_of(doc):
    """Return the schema dict, tolerating a bare map of schemas or a full OpenAPI doc."""
    if isinstance(doc, dict):
        comp = doc.get("components")
        if isinstance(comp, dict) and isinstance(comp.get("schemas"), dict):
            return comp["schemas"], "#/components/schemas/"
        if isinstance(doc.get("definitions"), dict):
            return doc["definitions"], "#/definitions/"
        if isinstance(doc.get("$defs"), dict):
            return doc["$defs"], "#/$defs/"
        if all(isinstance(v, dict) for v in doc.values()):
            return doc, "#/"
    sys.exit("Could not find a schema map (components.schemas, definitions, $defs).")


# --------------------------------------------------------------------------
# Walking
# --------------------------------------------------------------------------

def walk(node, path, fn):
    fn(node, path)
    if isinstance(node, dict):
        for k, v in node.items():
            walk(v, f"{path}.{k}", fn)
    elif isinstance(node, list):
        for i, v in enumerate(node):
            walk(v, f"{path}[{i}]", fn)


def merged_properties(schema, schemas, seen=None):
    """Properties visible on a schema, following allOf branches and local $refs."""
    if seen is None:
        seen = set()
    props = {}
    if not isinstance(schema, dict):
        return props
    ref = schema.get("$ref")
    if ref and ref.startswith("#") and ref not in seen:
        seen.add(ref)
        target = resolve(ref, schemas)
        if target is not None:
            props.update(merged_properties(target, schemas, seen))
    if isinstance(schema.get("properties"), dict):
        props.update(schema["properties"])
    for branch in schema.get("allOf") or []:
        props.update(merged_properties(branch, schemas, seen))
    return props


def resolve(ref, schemas):
    name = ref.rsplit("/", 1)[-1]
    return schemas.get(name)


# --------------------------------------------------------------------------
# Checks
# --------------------------------------------------------------------------

def check_refs(doc, schemas, prefix):
    valid_prefixes = ("#/components/schemas/", "#/definitions/", "#/$defs/")

    def visit(node, path):
        if isinstance(node, dict) and isinstance(node.get("$ref"), str):
            ref = node["$ref"]
            if not ref.startswith("#"):
                return  # external ref, out of scope
            if not ref.startswith(valid_prefixes):
                add("blocking", "REF-BROKEN", path,
                    f"`{ref}` is malformed (a local ref starts `#/`) — this resolves to nothing, "
                    "so anything inheriting through it inherits nothing")
                return
            if resolve(ref, schemas) is None:
                add("blocking", "REF-MISSING", path,
                    f"`{ref}` points at a schema that does not exist")

    walk(doc, "", visit)


def check_required(schemas):
    for name, schema in schemas.items():
        if not isinstance(schema, dict):
            continue
        req = schema.get("required", "__absent__")
        if req == "__absent__":
            continue
        if req is None:
            add("blocking", "REQ-NULL", name,
                "`required:` is empty, which parses as null — it must be a list or be removed")
            continue
        if isinstance(req, dict):
            add("blocking", "REQ-MAPPING", name,
                f"`required:` holds a mapping ({', '.join(list(req)[:4])}) — a `properties:` key "
                "is missing, so these are not properties and the schema constrains nothing")
            continue
        if not isinstance(req, list):
            add("blocking", "REQ-TYPE", name, f"`required:` is a {type(req).__name__}, expected a list")
            continue
        props = merged_properties(schema, schemas)
        for field in req:
            if field not in props:
                near = [p for p in props if p.lower().rstrip("s") == str(field).lower().rstrip("s")]
                hint = f" — did you mean `{near[0]}`?" if near else ""
                add("blocking", "REQ-UNDEFINED", f"{name}.{field}",
                    f"required name `{field}` matches no property{hint}")


def check_examples(schemas):
    def visit(node, path):
        if not isinstance(node, dict):
            return
        ex = node.get("examples", node.get("example", "__absent__"))
        if ex == "__absent__":
            return
        if "examples" in node and not isinstance(ex, list):
            add("significant", "EX-NOT-LIST", path,
                f"`examples:` parsed as {type(ex).__name__} `{ex!r}` rather than a list "
                "— usually a missing space after the dash")
            values = [ex]
        else:
            values = ex if isinstance(ex, list) else [ex]

        declared = node.get("type")
        pattern = node.get("pattern")
        for v in values:
            if declared == "string" and not isinstance(v, str):
                add("significant", "EX-TYPE", path,
                    f"example `{v!r}` parsed as {type(v).__name__}, but type is string "
                    "— quote it")
                continue
            if declared in ("integer", "number") and isinstance(v, str):
                add("significant", "EX-TYPE", path,
                    f"example `{v!r}` is a string but type is {declared}")
                continue
            if pattern and isinstance(v, str):
                try:
                    if not re.match(pattern, v):
                        add("significant", "EX-PATTERN", path,
                            f"example `{v}` does not match its own pattern `{pattern}`")
                except re.error:
                    add("significant", "PAT-INVALID", path, f"pattern `{pattern}` is not valid regex")

    walk_schemas(schemas, visit)


def check_formats(schemas):
    def visit(node, path):
        if not isinstance(node, dict) or "format" not in node:
            return
        t, f = node.get("type"), node.get("format")
        if not isinstance(f, str) or t is None:
            return
        allowed = VALID_FORMATS.get(t)
        if allowed is None:
            return
        if f not in allowed:
            owner = [k for k, v in VALID_FORMATS.items() if f in v]
            hint = f" — `{f}` belongs with type {owner[0]}" if owner else ""
            add("significant", "FMT-PAIR", path,
                f"`type: {t}` with `format: {f}` is not a defined pairing{hint}; "
                "behaviour is generator-dependent")

    walk_schemas(schemas, visit)


def check_patterns(schemas):
    def visit(node, path):
        if not isinstance(node, dict) or "pattern" not in node:
            return
        p = node.get("pattern")
        if not isinstance(p, str):
            return
        # unescaped dot: a `.` not preceded by a backslash and not inside a class
        stripped = re.sub(r"\[[^\]]*\]", "", p)
        if re.search(r"(?<!\\)\.", stripped):
            add("significant", "PAT-DOT", path,
                f"pattern `{p}` has an unescaped `.` — it matches any character, "
                "so this is looser than it reads")
        if p.startswith("^") and p.endswith("$") and re.fullmatch(r"\^\\d\$", p):
            add("significant", "PAT-SINGLE-DIGIT", path,
                f"pattern `{p}` allows exactly one digit (0-9) — check this against any "
                "unbounded cardinality in the glossary")

    walk_schemas(schemas, visit)


def check_arrays(schemas):
    def visit(node, path):
        if not isinstance(node, dict) or node.get("type") != "array":
            return
        if "items" not in node:
            add("significant", "ARR-NO-ITEMS", path, "array has no `items` — contents unconstrained")
        lo, hi = node.get("minItems"), node.get("maxItems")
        if isinstance(lo, int) and isinstance(hi, int) and lo > hi:
            add("blocking", "ARR-BOUNDS", path, f"minItems {lo} > maxItems {hi}")

    walk_schemas(schemas, visit)


def check_vendor_keys(schemas):
    def visit(node, path):
        if not isinstance(node, dict):
            return
        for k in node:
            if not isinstance(k, str) or not k.startswith("x-"):
                continue
            if k in KNOWN_VENDOR_KEYS:
                continue
            close = [
                known for known in KNOWN_VENDOR_KEYS
                if abs(len(known) - len(k)) <= 2
                   and sum(a != b for a, b in zip(known.ljust(30), k.ljust(30))) <= 3
            ]
            if close:
                add("significant", "VENDOR-TYPO", f"{path}.{k}",
                    f"`{k}` looks like a misspelling of `{close[0]}` — tooling will ignore it silently")

    walk_schemas(schemas, visit)


def check_orphans(doc, schemas):
    referenced = set()

    def visit(node, path):
        if isinstance(node, dict) and isinstance(node.get("$ref"), str):
            referenced.add(node["$ref"].rsplit("/", 1)[-1])

    walk(doc, "", visit)
    orphans = [n for n in schemas if n not in referenced]
    if orphans:
        add("minor", "ORPHAN", ", ".join(orphans),
            "no $ref points at these — either dead weight, or they are entry points "
            "referenced from paths outside this file. Worth checking the ones that look "
            "like shared value types, since a duplicated-and-unused type drifts from its inline twin")


def check_subtype_families(schemas):
    families = defaultdict(list)
    for name, schema in schemas.items():
        if not isinstance(schema, dict):
            continue
        for branch in schema.get("allOf") or []:
            if isinstance(branch, dict) and isinstance(branch.get("$ref"), str):
                families[branch["$ref"].rsplit("/", 1)[-1]].append(name)
    for base, subs in families.items():
        if len(subs) < 2:
            continue
        parent = schemas.get(base, {})
        has_disc = isinstance(parent, dict) and "discriminator" in parent
        if not has_disc:
            add("significant", "SUB-NO-DISCRIMINATOR", base,
                f"{len(subs)} subtypes ({', '.join(subs)}) and no `discriminator` — "
                "a consumer cannot tell which one it received")
        for sub in subs:
            s = schemas[sub]
            own = {}
            for branch in s.get("allOf") or []:
                if isinstance(branch, dict) and isinstance(branch.get("properties"), dict):
                    own.update(branch["properties"])
            if isinstance(s.get("properties"), dict):
                own.update(s["properties"])
            if not own:
                add("significant", "SUB-EMPTY", sub,
                    f"adds nothing to `{base}` — indistinguishable on the wire")


def walk_schemas(schemas, visit):
    for name, schema in schemas.items():
        walk(schema, name, visit)


# --------------------------------------------------------------------------

def inventory(schemas):
    """A compact structural summary to diff glossary terms against."""
    out = {}
    for name, schema in schemas.items():
        if not isinstance(schema, dict):
            continue
        props = merged_properties(schema, schemas)
        req = schema.get("required") if isinstance(schema.get("required"), list) else []
        parents = [
            b["$ref"].rsplit("/", 1)[-1]
            for b in schema.get("allOf") or []
            if isinstance(b, dict) and isinstance(b.get("$ref"), str)
        ]
        fields = {}
        for pname, p in props.items():
            if not isinstance(p, dict):
                continue
            if "$ref" in p:
                shape = p["$ref"].rsplit("/", 1)[-1]
            elif p.get("type") == "array":
                item = p.get("items", {})
                inner = item.get("$ref", "").rsplit("/", 1)[-1] or item.get("type", "?")
                lo = p.get("minItems", 0)
                hi = p.get("maxItems", "*")
                shape = f"[{inner}] {lo}..{hi}"
            else:
                shape = p.get("type", "?")
            fields[pname] = {"shape": shape, "required": pname in req}
        out[name] = {"extends": parents, "fields": fields}
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("path")
    ap.add_argument("--json", action="store_true", help="emit findings as JSON")
    ap.add_argument("--inventory", action="store_true", help="also print the field inventory")
    args = ap.parse_args()

    doc, _ = load(args.path)
    schemas, prefix = schemas_of(doc)

    check_refs(doc, schemas, prefix)
    check_required(schemas)
    check_examples(schemas)
    check_formats(schemas)
    check_patterns(schemas)
    check_arrays(schemas)
    check_vendor_keys(schemas)
    check_orphans(doc, schemas)
    check_subtype_families(schemas)

    if args.json:
        print(json.dumps({"findings": findings, "inventory": inventory(schemas)}, indent=2))
        return

    order = {"blocking": 0, "significant": 1, "minor": 2}
    findings.sort(key=lambda f: (order.get(f["severity"], 9), f["code"], f["where"]))
    if not findings:
        print("No mechanical defects found.")
    else:
        print(f"{len(findings)} mechanical finding(s):\n")
        current = None
        for f in findings:
            if f["severity"] != current:
                current = f["severity"]
                print(f"--- {current.upper()} ---")
            print(f"  [{f['code']}] {f['where']}\n      {f['message']}")
    if args.inventory:
        print("\n--- INVENTORY ---")
        print(json.dumps(inventory(schemas), indent=2))


if __name__ == "__main__":
    main()
