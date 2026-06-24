# Validating & Linting the Spec

Always validate a finished or modified spec. There are two complementary layers:

1. **Spec conformance** — does the document obey the AsyncAPI 3.1.0 schema? Use
   the official **AsyncAPI CLI** (or a Python fallback).
2. **House-style governance** — does it follow our conventions? Use **Spectral**
   with the bundled ruleset.

Run both. Conformance catches structural mistakes; Spectral catches style drift.

## Layer 1 — AsyncAPI CLI (spec conformance)

The official CLI uses the AsyncAPI parser, so it validates against the real
3.1.0 specification (more than a plain JSON-Schema check).

### Install (once per environment)

```bash
npm install -g @asyncapi/cli
# Requires a current Node.js (the CLI tracks recent LTS/Node versions).
```

### Validate

```bash
asyncapi validate path/to/spec.yaml
```

Useful flags:
- `--diagnostics-format pretty` — readable, line-referenced output.
- `--fail-severity warn` — exit non-zero on warnings too (stricter gate).
- `--score` — print a quality score for the document.

A valid document reports success; otherwise the CLI underlines the offending
lines and lists diagnostics. Fix every error and re-run until clean. You can also
explore interactively with `asyncapi start studio path/to/spec.yaml` (opens
AsyncAPI Studio), but that needs a browser and is optional.

## Layer 2 — Spectral (house-style governance)

Spectral has a built-in `spectral:asyncapi` ruleset covering AsyncAPI v2 **and**
v3. The bundled `assets/house-style.spectral.yaml` extends it and layers on the
house conventions (camelCase operation/channel keys, PascalCase schema names,
kebab-case channel addresses, required contact, etc.).

### Install (once per environment)

```bash
npm install -g @stoplight/spectral-cli
export PATH="$PATH:$(npm root -g)/../bin"   # if `spectral` isn't on PATH
```

### Lint

Run from the skill directory (or pass the full path to the ruleset):

```bash
spectral lint path/to/spec.yaml --ruleset assets/house-style.spectral.yaml
```

Useful flags:
- `--format pretty` — readable, line-referenced output.
- `--fail-severity warn` — exit non-zero on warnings too.

### Interpret results

- **Errors** — must all be fixed. The bundled ruleset raises errors for: missing
  contact, non-camelCase operation or channel keys, and non-PascalCase schema
  names, on top of the structural errors from `spectral:asyncapi`.
- **Warnings** — review each and resolve unless there's a deliberate reason
  (missing operation/message descriptions, channel addresses that aren't
  kebab-case, message `name` not PascalCase, etc.).

Re-run after fixing until the output is clean. The built-in `spectral:asyncapi`
rules also check things like header schemas being `type: object`, tag
descriptions, and resolved-document structure — read
`assets/house-style.spectral.yaml` to see or adjust the exact severities.

## Fallback — Python structural check

If neither CLI can be installed (no npm access), do a minimum structural sanity
check in Python. This is **not** a full spec validation — review naming and
componentization manually against `references/conventions.md`.

```bash
pip install pyyaml --break-system-packages -q
python3 - <<'PY'
import yaml, sys
with open("path/to/spec.yaml") as f:
    doc = yaml.safe_load(f)

assert doc.get("asyncapi") == "3.1.0", "asyncapi must be 3.1.0"
assert "info" in doc and "title" in doc["info"] and "version" in doc["info"]
assert "channels" in doc and doc["channels"], "channels required"
assert "operations" in doc and doc["operations"], "operations required"
for op_id, op in doc["operations"].items():
    assert op.get("action") in ("send", "receive"), f"{op_id}: action must be send/receive"
    assert "channel" in op and "$ref" in op["channel"], f"{op_id}: channel $ref required"
for ch_id, ch in doc["channels"].items():
    assert "address" in ch, f"{ch_id}: address required"
print("Structural sanity check passed (NOT a full AsyncAPI validation).")
PY
```

For real conformance, prefer the AsyncAPI CLI whenever npm is available — it is
the authoritative validator for 3.1.0.
