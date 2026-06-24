# Validating & Linting the Spec

Always validate a finished or modified spec. The primary tool is **Spectral**;
a Python validator is the fallback when npm isn't available.

## Primary: Spectral

Spectral validates OpenAPI structure **and** applies the house-style ruleset.

### Install (once per environment)

```bash
npm install -g @stoplight/spectral-cli
# binary lands in the npm global bin dir, e.g. ~/.npm-global/bin/spectral
export PATH="$PATH:$(npm root -g)/../bin"   # if `spectral` isn't on PATH
```

### Lint

Run from the skill directory (or pass the full path to the ruleset):

```bash
spectral lint path/to/spec.yaml --ruleset assets/house-style.spectral.yaml
```

Useful flags:
- `--format pretty` — readable, line-referenced output.
- `--fail-severity warn` — exit non-zero on warnings too (stricter gate).

### Interpret results

- **Errors** — must all be fixed. The ruleset raises errors for: missing/duplicate
  `operationId`, non-camelCase `operationId`, operations tagged with an undeclared
  tag, missing `x-api-id`, missing `x-audience`, and missing `contact`.
- **Warnings** — review each and resolve unless there's a deliberate reason
  (missing summaries/descriptions, non-PascalCase schema names, tags without
  descriptions, missing `servers`, etc.).

Re-run after fixing until the output is clean (`No results with a severity of
'error' found!`). If the user asked for a strict gate, also clear warnings.

### What the ruleset checks

`assets/house-style.spectral.yaml` extends Spectral's built-in `spectral:oas`
ruleset (full structural validation for OpenAPI 3.x, including 3.1.0) and adds
the house-style rules above. Read the file to see or adjust the exact rules.

## Fallback: Python structural validator

If Spectral can't be installed (no npm access), validate structure with
`openapi-spec-validator`. This checks conformance to the OpenAPI 3.1.0 schema
but does **not** enforce the house style — review naming/componentization
manually against `references/conventions.md`.

```bash
pip install openapi-spec-validator --break-system-packages -q
python3 - <<'PY'
import yaml
from openapi_spec_validator import validate
with open("path/to/spec.yaml") as f:
    spec = yaml.safe_load(f)
validate(spec)          # raises on any schema violation
print("VALID OpenAPI 3.1.0")
PY
```

## Optional: Redocly CLI

Redocly is an alternative that also lints and bundles, with strong 3.1 support:

```bash
npm install -g @redocly/cli
redocly lint path/to/spec.yaml
```

Spectral with the bundled ruleset is preferred because it enforces the house
style; use Redocly only if the user specifically wants it.
