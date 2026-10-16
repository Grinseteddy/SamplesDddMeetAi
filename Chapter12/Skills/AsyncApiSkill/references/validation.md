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

### Quality rules in the ruleset

These rules back `references/quality-requirements.md`:
- `house-schema-properties-have-description` (warn) — every payload/header
  property says what it means; the only place a change of meaning shows.
- `house-messages-have-examples` (warn) — examples seed contract tests.
- `house-no-closed-schemas` (warn) — `additionalProperties: false` breaks
  tolerant readers on every additive change.
- `house-info-tolerant-reader` (warn) — `info.description` asks consumers to
  ignore unknown message types, properties and enum values.
- `house-deprecated-needs-sunset` / `house-deprecated-explains-successor`
  (error) — anything marked `x-deprecated: true` carries `x-sunset` and names
  its successor and sunset in `description`.

The rest of the checklist (§9 of that file) needs judgment.

## Layer 3 — Compatibility checks (evolving an existing spec)

Run these whenever an earlier version of the spec exists.

**Channels and operations — `asyncapi diff`:**

```bash
asyncapi diff old.yaml new.yaml --type breaking --format md
# exit code 1 if anything is classified as breaking
```

It reliably flags renamed/removed channels, address changes and operation
changes, but leaves **payload-schema changes "unclassified"** — a removed
payload property does not show up as breaking. (Ignore the many
`x-parser-schema-id` lines in `--type all` output; they are parser noise.)

**Payloads and headers — `scripts/payload_compat.py`:**

```bash
pip install pyyaml --break-system-packages -q
python3 -I scripts/payload_compat.py old.yaml new.yaml
# exit code 1 on any BREAKING finding
```

It reads each message's direction from the operations and applies the Liskov
rules: for `send` messages, removed properties, required→optional, type/format
changes, added enum values and changed Kafka partition keys are BREAKING
(loosened constraints are CHECK); for `receive` messages, new required
properties, tightened constraints, removed enum values and type/format changes
are BREAKING. Every changed `description` is reported as MEANING — confirm by
hand that the meaning did not change; no tool can.

**At runtime**, also set the schema-registry subject compatibility for retained
Kafka topics to `FULL_TRANSITIVE` (at least `BACKWARD_TRANSITIVE`), so old
messages remain readable on replay.

A breaking finding means: make the change additive, or ship it as a new
versioned channel with the old one deprecated (quality requirements §5).

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
