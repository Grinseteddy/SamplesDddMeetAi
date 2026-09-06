# Arazzo field reference (1.0.1 and 1.1.0)

Condensed from *The Arazzo Specification* (OpenAPI Initiative): v1.0.0
2024-05-29, v1.0.1 2025-01-16, v1.1.0 2026-05-17.
Canonical text: https://spec.openapis.org/arazzo/latest.html — fetch it when a
field here is not enough. Read this rather than guessing field names; several
near-misses (`operationRef`, `on_success`, `assertions`) look plausible and are
not Arazzo.

Contents:

1. Which version to write
2. Root object
3. Workflow object
4. Step object
5. Parameter, Request Body, Criterion
6. Success and Failure actions
7. Components and Reusable
8. Runtime expressions
9. Async steps (1.1.0 only)
10. Common mistakes

---

## 1. Which version to write

| Write | When |
|---|---|
| `1.0.1` | Default. Every tool supports it. HTTP operations from OpenAPI only; automations become polls with `onFailure: retry`. |
| `1.1.0` | The board's automations or translations are genuinely asynchronous **and** an AsyncAPI description exists (or will). Adds `type: asyncapi` source descriptions, `action: send`/`receive`, `correlationId`, `timeout`, step-level `dependsOn`, `$self`, Selector Objects, and `parameters` on workflow-calling actions. |

Patch versions are not distinguished by tooling; `1.0.0` and `1.0.1` behave
identically. Say in the report which you chose and why.

---

## 2. Root object

| Field | Required | Notes |
|---|---|---|
| `arazzo` | ✔ | version string, e.g. `1.0.1` |
| `info` | ✔ | `title` ✔, `version` ✔, `summary`, `description` |
| `sourceDescriptions` | ✔ | **at least one entry**; each `name` ✔ (`[A-Za-z0-9_-]+`), `url` ✔, `type` (`openapi` \| `arazzo` \| `asyncapi` — the last is 1.1.0 only) |
| `workflows` | ✔ | at least one |
| `components` | | reusable `inputs`, `parameters`, `successActions`, `failureActions` |
| `$self` | | 1.1.0; absolute URI identifying this document, used as the base for relative `url`s |
| `x-…` | | specification extensions; `x-arazzo`, `x-oai-`, `x-oas-` prefixes are reserved |

---

## 3. Workflow object

| Field | Required | Notes |
|---|---|---|
| `workflowId` | ✔ | unique, case-sensitive, `[A-Za-z0-9_-]+` |
| `steps` | ✔ | ordered list |
| `summary`, `description` | | one line and CommonMark respectively |
| `inputs` | | a **JSON Schema 2020-12 object** (`type: object`, `properties`, `required`) — not a parameter list |
| `dependsOn` | | list of `workflowId`s that must have completed |
| `outputs` | | map name → runtime expression; keys match `^[a-zA-Z0-9\.\-_]+$` |
| `parameters` | | applied to every step; overridable per step, never removable |
| `successActions`, `failureActions` | | workflow-wide defaults, same override rule |

---

## 4. Step object

| Field | Required | Notes |
|---|---|---|
| `stepId` | ✔ | unique within the workflow, `[A-Za-z0-9_-]+` |
| `operationId` | | mutually exclusive with `operationPath`, `workflowId`. With more than one non-Arazzo source, **must** be written `$sourceDescriptions.<name>.<operationId>` |
| `operationPath` | | `'{$sourceDescriptions.<name>.url}#/paths/~1pets/get'` — JSON Pointer, `/` escaped as `~1`. Prefer `operationId` when one exists |
| `workflowId` | | call another workflow; `parameters` then map to its inputs and **omit `in`** |
| `channelPath` | | 1.1.0, AsyncAPI channels |
| `parameters` | | list of Parameter Objects or Reusable Objects |
| `requestBody` | | `contentType`, `payload`, `replacements` |
| `successCriteria` | | list of Criterion Objects; **all** must pass |
| `onSuccess` / `onFailure` | | Success / Failure Action Objects |
| `outputs` | | map name → expression (or Selector Object in 1.1.0) |
| `timeout` | | 1.1.0, milliseconds |
| `correlationId`, `action` | | 1.1.0, async only |
| `dependsOn` | | 1.1.0, list of `stepId`s; for synchronous workflows, order the array instead |

Default control flow: on success, run the next step in the array; on failure,
break and return. `onSuccess`/`onFailure` override that.

---

## 5. Parameter, Request Body, Criterion

**Parameter** — `name` ✔, `value` ✔, `in` (`path` | `query` | `querystring` |
`header` | `cookie`). `in` is required except when the step targets a
`workflowId`.

```yaml
- name: planId
  in: path
  value: $steps.planDinner.outputs.planId
```

**Request Body** — `contentType`, `payload` (literal, runtime expressions, or a
templated string with `{}` embedding), `replacements` (JSON Pointer `target` +
`value`).

```yaml
requestBody:
  contentType: application/json
  payload:
    guests: $inputs.guestCount
    occasion: $inputs.occasion
```

**Criterion** — `condition` ✔, plus `context` and `type` when the type is not
`simple`.

```yaml
successCriteria:
  - condition: $statusCode == 200                  # simple
  - context: $response.body                        # jsonpath
    condition: $.recipes[*]
    type: jsonpath
  - context: $statusCode                           # regex
    condition: '^2\d{2}$'
    type: regex
```

Operators: `< <= > >= == != ! && || () [] .`. Literals: booleans, `null`,
numbers, and single-quoted strings. **String comparison is case-insensitive.**
A `jsonpath` condition passes when the nodelist is non-empty. Runtime
expressions inside `regex`/`jsonpath`/`xpath` conditions must be wrapped in
`{}` and the whole condition quoted.

---

## 6. Success and Failure actions

**Success** — `name` ✔, `type` ✔ (`end` | `goto`), plus `workflowId` **or**
`stepId` for `goto`, optional `criteria`.

**Failure** — `name` ✔, `type` ✔ (`end` | `retry` | `goto`), plus `workflowId`
or `stepId`, `retryAfter` (seconds, decimal), `retryLimit` (integer), optional
`criteria`. `retryLimit` is exhausted before later failure actions run; with no
`retryLimit`, exactly one retry happens.

```yaml
onFailure:
  - name: keepWaiting
    type: retry
    retryAfter: 5
    retryLimit: 12
  - name: rescueNeverArrived
    type: end
```

The first action whose criteria match is the one that runs, so order matters.

---

## 7. Components and Reusable

`components.inputs` holds JSON Schemas, referenced from workflow `inputs` with
standard JSON Schema `$ref`. Everything else in `components` is referenced with
a **Reusable Object**, not `$ref`:

```yaml
parameters:
  - reference: $components.parameters.authorization
```

Component keys match `^[a-zA-Z0-9\.\-_]+$` (dots allowed here, unlike ids).

---

## 8. Runtime expressions

| Expression | Meaning |
|---|---|
| `$statusCode`, `$url`, `$method` | of the current step's call |
| `$response.body#/planId` | JSON Pointer into the response body |
| `$response.header.X-Rate-Limit` | single header value |
| `$inputs.<name>` | this workflow's input |
| `$outputs.<name>` | this workflow's output (inside the workflow) |
| `$steps.<stepId>.outputs.<name>` | an **earlier** step's output |
| `$workflows.<workflowId>.outputs.<name>` | another workflow's output |
| `$sourceDescriptions.<name>.<operationId>` | resolves to an operation; falls back to a field of the source object (`url`, `type`) if no operation matches |
| `$components.parameters.<key>` | a reusable component |
| `$message.payload#/orderId` | 1.1.0, an async received message |
| `$self` | 1.1.0, this document's canonical URI |

Embed an expression inside a string with braces:
`https://{$inputs.host}/plans/{$steps.planDinner.outputs.planId}`.

---

## 9. Async steps (1.1.0 only)

```yaml
- stepId: publishHelpRequest
  operationId: $sourceDescriptions.cookingEvents.helpRequested
  action: send
  requestBody:
    payload:
      situation: $inputs.situation

- stepId: awaitHelpProvided
  operationId: $sourceDescriptions.cookingEvents.helpProvided
  action: receive
  correlationId: $inputs.correlationId
  timeout: 300000
  dependsOn: [publishHelpRequest]
  successCriteria:
    - context: $message.payload
      condition: $.status == 'ANSWERED'
      type: jsonpath
  outputs:
    helpResponseId: $message.payload#/helpResponseId
```

- `send` completes as soon as the message is sent; Arazzo models no broker ack.
- `receive` completes on a matching message within `timeout`, or fails and runs
  `onFailure`. Give it `successCriteria` unless the channel carries exactly one
  message type that can only mean success.
- `dependsOn` is the intended way to say "this step waits for that async step",
  even when no output is referenced.

---

## 10. Common mistakes

- `operationRef` — not an Arazzo field. It is `operationPath`.
- `$response.body.planId` where a pointer is meant — write `$response.body#/planId`.
- Bare `operationId: settleMealPlan` with two source descriptions — ambiguous
  and invalid; qualify it.
- Dots in a `stepId` or `workflowId` — breaks expression parsing.
- Forward references to a later step's outputs — unsatisfiable sequentially.
- `in:` on a parameter of a `workflowId` step — parameters map to inputs there.
- `inputs` written as a list of parameters — it is a JSON Schema object.
- Double-quoted strings inside a `simple` condition — literals use single quotes.