# Validating an Arazzo document

Two layers, and they catch different things. Run both, in this order.

| Layer | Tool | Catches | Misses |
|---|---|---|---|
| Schema | `redocly lint` | wrong or misspelled fields, wrong types, missing required fields, malformed criteria, invalid version | **operationIds that don't exist**, broken runtime expressions, forward references, missing traceability |
| Cross-reference | `scripts/check_arazzo.py` | unresolved operations, undeclared `$inputs`, unknown or forward `$steps` references, unknown `dependsOn`/`goto` targets, malformed async steps, `in:` on a workflow-call parameter, missing `x-event-model` | anything the Arazzo schema itself defines |

The gap matters: a document full of operations that do not exist in the
OpenAPI passes `redocly lint` with a green tick. That is exactly the mistake
this skill is most likely to make, which is why the second layer is bundled.

## Layer 1 — Redocly CLI

```bash
npm install -g @redocly/cli
export PATH="$PATH:$(npm root -g)/../bin"   # if `redocly` isn't on PATH
redocly lint path/to/spec.arazzo.yaml
```

Redocly reads the `arazzo` version field and validates against the matching
schema; 1.0.x and 1.1.0 are both supported. Fix every error and re-run. There
is no `--fail-severity` distinction worth configuring here — an Arazzo document
either conforms or it doesn't.

## Layer 2 — the bundled checker

```bash
pip install pyyaml --break-system-packages -q
python3 scripts/check_arazzo.py path/to/spec.arazzo.yaml
```

Flags:

- `--strict` — exit non-zero on warnings too. Use this before handing the file
  over.
- `--no-traceability` — skip the `x-event-model` checks. Only for a file that
  did not come from a board.

It resolves `operationId`s by reading the local files named in
`sourceDescriptions[].url` (relative to the Arazzo file). Remote URLs are not
fetched: it says so as a warning rather than pretending the check happened.

Two results deserve interpretation rather than a fix:

- **`operation 'x' not found … mark the step proposed-operation`** — either the
  operation name is wrong, or the API genuinely doesn't have it yet. The second
  case is legitimate: set `x-event-model.status: proposed-operation` and list it
  in report §3. The checker then downgrades it to a warning, which is the honest
  state of a design-ahead document.
- **`could not read <url> — operation ids unverified`** — the source description
  is remote or missing. Say in the report that operation resolution was not
  checked for that lane.

## Layer 3, optional — actually running it

`redocly respect` executes an Arazzo document against live servers:

```bash
redocly respect path/to/spec.arazzo.yaml \
  --input planId=abc --server mealPlanningApi=https://api.example.com
```

Only useful when the APIs exist. Never run it against production from a
modelling session, and don't offer it for a file whose operations are still
`proposed-operation` — there is nothing to call.

## No npm available

Skip layer 1 and say so in the report. The bundled checker plus
`references/arazzo-reference.md` (§10, common mistakes) covers most structural
errors: required root fields, mutually exclusive step targets, criteria that
need a `context`, and action objects missing `name`/`type` are all checked
there. What you lose is the exhaustive field-name check — so re-read any field
you were unsure about rather than trusting it.