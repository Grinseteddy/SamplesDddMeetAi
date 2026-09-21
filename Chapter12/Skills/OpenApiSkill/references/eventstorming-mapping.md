# EventStorming → OpenAPI derivation rules

This file governs the extra translation step needed when the input is an
EventStorming board (a photo/screenshot of stickies, a Miro/Mural export, a
plain sticky list, or an already-produced Event Storming Brief) instead of a
plain feature description. Read it whenever Step 1 of `SKILL.md` determines
the input is board-shaped. It assumes familiarity with Brandolini's notation
(orange = domain event, blue = command, large yellow = aggregate, small
yellow = actor, green = read model, lilac/pink = policy/external system, red
= hotspot); see the `event-storming-interpreter` skill's
`references/notation-legend.md` for the full palette if a note's role is
unclear.

The goal is **not** to reproduce an Event Storming Brief — it's to pull out
exactly the structure an OpenAPI spec needs, resource by resource. If the
user already has a brief (from `event-storming-interpreter`), start from its
§3 (bounded contexts), §4–5 (domain model / state machines), §6 (commands,
policies & flows), and §7 (actors) instead of re-reading the raw board.

## 1. Bounded contexts are the scoping unit

Each **bounded context** (labeled bubble/lane/cluster) is a candidate for its
**own API spec** — this mirrors the house style's one-API-per-service
assumption and keeps each spec's `x-api-id`, versioning, and audience
independent. Do not merge multiple contexts' resources into one spec unless
the user explicitly asks for a combined API.

Before writing anything, confirm scope with the user:
- **All contexts** — produce one `.yaml` file per bounded context (loop the
  rest of this workflow once per context), each following the normal
  file-naming/save conventions.
- **One named context** — produce a single `.yaml` for just that context.

If the board has only one context (or none were drawn — treat the whole
board as one context), skip the question and proceed with that single scope.

## 2. Aggregates → resources

Each **aggregate** (large yellow) owned by the in-scope context becomes a
top-level resource: `PascalCase` schema name, kebab-case plural path
(`Order` → `/orders`). An aggregate that only ever appears nested under one
parent (e.g. an order's line items, never addressed independently) becomes a
nested sub-resource (`/orders/{orderId}/line-items`), matching the
sub-resource pattern already in `assets/example-catalog-management.yaml`
(`/catalog-entries/{catalogEntryId}/tags`).

## 3. Commands → operations

Walk each aggregate's commands (from the brief's §6 slice table, or directly
off the board) and classify:

- **Creation command** ("Create X", "Register X", "Submit X") → `POST` on the
  collection (`POST /orders`).
- **Field-update command** ("Update X", "Rename X", "Edit X details") →
  `PATCH` (partial) or `PUT` (full replace, if the board implies one) on the
  item (`PATCH /orders/{orderId}`).
- **Hard-delete command** ("Delete X", "Remove X" — the aggregate actually
  disappears) → `DELETE` on the item.
- **Lifecycle/status-transition command** ("Approve X", "Cancel X", "Ship
  X" — the aggregate survives, its status changes) → a verb sub-resource
  action endpoint, **not** a generic `PATCH`, so the operation stays aligned
  with the event it produces: `POST /orders/{orderId}/approve`,
  `POST /orders/{orderId}/cancel`. This keeps the state machine from Step 5
  legible in the paths instead of being hidden inside a body field.
- **Command with no aggregate** (fired only by a policy, nothing in the
  domain model owns it) → do not expose it as a REST endpoint. List it under
  "not exposed" in your summary to the user instead of guessing a resource
  for it.

Every operation still gets the full house-style treatment from
`conventions.md` §3–4: `operationId`, `tags`, `summary`, a `description`
explaining behavior/side effects, the standard response set, and a narrowed
`security` block where the actor differs from the default.

## 4. Read models → GET endpoints

Each **read model** (green) becomes a query surface:
- A read model keyed by the aggregate's own id → folds into the aggregate's
  existing `GET /orders/{orderId}`; don't create a duplicate endpoint.
- A read model that lists/filters a collection ("Pending Orders",
  "Available Books") → `GET /orders` (or a dedicated nested/search path if
  the read model spans multiple aggregates or the context already has a
  search convention, e.g. `/catalog-entries/search`), with query parameters
  for the filter the read model implies (`status=pending`).
- A read model that clearly spans aggregates from *different* contexts is a
  signal the read model belongs to a read-only aggregating service, not this
  context's API — flag it rather than forcing it in.

## 5. State machines → status fields + transition docs

For every aggregate with a lifecycle (brief §5, or derived directly from its
events), add a `status` (or similarly named) property to its read schema
with an `enum` of the derived states. Document each transition in the
relevant action operation's `description` (`| Transitions status from
pending to approved. Only valid when status is pending.`), and add
server-set timestamp fields the events imply (`approvedAt`, `cancelledAt`).

## 6. Domain events → usually not first-class endpoints

Default: domain events are **not** exposed as REST resources — they surface
indirectly as the `status` field and timestamps from Step 5. Ask before doing
anything else with them. If the user *does* want event notifications
modeled:
- Prefer OpenAPI 3.1's top-level `webhooks:` map, one entry per event
  (`PascalCase`, named after the event), payload = the resource's read
  schema.
- Use per-operation `callbacks` instead only if the event is a direct,
  synchronous side effect of one specific operation the caller registers a
  callback for.

## 7. Actors → security scopes

Each **actor** (small yellow) role becomes an OAuth2 scope in
`components.securitySchemes` (`read:orders`, `write:orders`, or a
role-specific scope name if the board distinguishes roles sharply, e.g.
`admin:orders`). Apply narrowed per-operation `security` blocks exactly where
the actor for that command differs from the default reader/writer split —
same rule as `conventions.md` §8. **External systems** (pink, calling in
rather than being called) inform `x-audience`: if only internal systems and
actors touch this context, prefer `company-internal` or
`component-internal` over `external-public`.

## 8. Cross-context arrows → integration notes, not endpoints

Arrow-flows crossing context boundaries (an event in this context triggering
a command in another) are **not** modeled as synchronous calls in either
spec. Add a short `# integration:` comment near the producing operation
noting the downstream context and trigger, so the boundary is documented
without inventing a synchronous dependency. If the user explicitly wants
synchronous cross-context calls, model them as a normal operation and note
the deviation.

## 9. Hotspots and gaps block generation of the affected part

Carry over red hotspots and any open question that affects the *contract*
(a command with no aggregate, an ambiguous read model, a status with no
terminal state) — surface these to the user before finalizing that part of
the spec rather than silently resolving them with a guess. Non-contract
ambiguities (cosmetic naming, which tense to use) can be resolved with a
stated assumption and a note in the summary.