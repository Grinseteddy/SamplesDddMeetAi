# Domain Story + Visual Glossary → OpenAPI derivation rules

This file governs the extra translation step needed when the input is a
**Domain Story** (Hofer & Schwentner pictographic diagram, its transcription,
or a Prototype Brief from `domain-story-interpreter`), a **Visual Glossary**
(concept-map diagram, its transcription, or a Glossary Brief from
`visual-glossary-interpreter`), or both together. Read it whenever Step 1 of
`SKILL.md` determines the input is one of these instead of a plain
description. For EventStorming boards, use `eventstorming-mapping.md`
instead — it covers a different vocabulary (aggregates, commands, read
models) that doesn't apply here.

## Why two documents, not one

These two notations split labor along the same line `visual-glossary-interpreter`
itself draws: the **Domain Story supplies the verbs** (a concrete sequence of
use cases, who does them, what they act on) and the **Visual Glossary
supplies the nouns** (the exact terms, their relationships, and their
cardinalities). Neither alone gives you a full API contract:

- **Domain Story only** → you can derive operations confidently, but resource
  *fields* come from the story's own inference step (attributes guessed from
  how a work object is used), not from an authoritative source. Say so in
  your summary, and suggest running `visual-glossary-interpreter` or
  supplying a glossary for firmer field-level detail.
- **Visual Glossary only** → you can derive resource schemas confidently, but
  there's nothing telling you which operations the API actually needs to
  expose. Default to the standard CRUD/list/search set from
  `conventions.md`'s Step 1 defaults, and say plainly that the operation set
  is a default, not sourced from a use case list.
- **Both** → reconcile them (§4) rather than picking one arbitrarily.

If the user already ran the interpreter skills, work from their briefs'
relevant sections instead of re-reading the raw pictures: Prototype Brief
§3–6 (modules, domain model, state machines, use cases/journey) and Glossary
Brief §4–6 (derived domain model, business rules, bounded contexts). Their
open-questions sections (§7/§8) carry over directly as unresolved items to
surface before finalizing the affected part of the spec.

## 1. Scope: groups are still the API/service boundary

Domain Story groups/lanes and Visual Glossary bounded-context groupings are
both candidates for the "one API per module" boundary, exactly as bounded
contexts are for EventStorming input (`eventstorming-mapping.md` §1). If
both pictures are present and group a term differently, flag the
disagreement and ask which is authoritative rather than silently picking
one — this is also exactly what `domain-story-glossary-consistency` checks
for, and is worth pointing the user at if the mismatch is more than
cosmetic. Confirm scope with the user using SKILL.md's Step 2 (all modules
vs. one named module) the same way as any other input.

## 2. Visual Glossary → schemas (the nouns)

- **Entity** (own identity, referenced independently, persists) → its own
  schema and its own resource collection: `Book` → `BookRead` /
  `BookCreate` / `BookUpdate` schemas at `/books`.
- **Value object** (leaf, no independent identity, meaningful only as part
  of its parent) → an inline property on the parent schema, never its own
  resource or endpoint: `Title` under `Book` → `Book.title`.
- **Aggregate root** (owns a cluster of parts through composition edges) →
  the resource whose endpoint the parts nest under, following the house
  style's sub-resource nesting pattern (`/orders/{orderId}/line-items`).
  Parts that are entities in their own right but are only ever addressed
  through the root still get nested paths, not top-level collections.
- **Identity term** (`ISBN`, `CatalogEntryId`) → the resource's id-equivalent
  path parameter and schema property. Carry over any implied shape as a
  schema `format` (`uuid`) or `pattern`, rather than defaulting to a bare
  string.
- **Cardinality on a relationship edge** → direct schema shape:
  - `1` / `0..1` target → a singular nested object or reference (`0..1` →
    nullable, not `required`).
  - `1..*` / `0..*` / `1..N` target → an array property, with
    `minItems`/`maxItems` taken from the stated bound (`1..10` →
    `minItems: 1, maxItems: 10`). Don't invent a bound the glossary didn't
    give — leave it unconstrained and note that it's an inferred default.
  - An **unstated inverse cardinality** (common — it's the glossary skill's
    own biggest flagged gap) decides whether the relationship becomes a
    nested array under the parent, a top-level resource with a foreign
    reference, or a many-to-many join resource. Ask, or state the
    assumption plainly (default: nest under the side that's obviously the
    "many" owner) and proceed.
- **Is-a / generalization edges** → `allOf` composition, or a discriminated
  union (`oneOf` + `discriminator`) if the story/glossary implies real
  polymorphism. If the edge's shape and label disagree (the glossary skill's
  own §6 finding), don't silently resolve it — ask.
- **Terminology** — use the glossary's exact term spelling for schema and
  property names (converted to `PascalCase`/`camelCase` per house style),
  even where the story or your own phrasing would differ. The glossary is
  the terminology authority.

## 3. Domain Story → operations (the verbs)

- Each numbered use case (`actor` + `verb` + `work object` [+ context]) → one
  operation, using the same verb classification as EventStorming commands
  (`eventstorming-mapping.md` §3): creation verbs → `POST`, field-edit verbs
  → `PATCH`/`PUT`, removal verbs → `DELETE`, and status-changing verbs
  ("approves", "rejects", "finishes") → a verb sub-resource action endpoint
  (`POST /tasks/{taskId}/approve`) rather than a generic `PATCH`, so the
  state machine stays visible in the paths.
- Required context captured in prepositions ("with Code", "at Rack", "from
  Project") → request body fields or path/query parameters on that
  operation — matching the glossary's field names/types when a glossary is
  present.
- State machines derived from the story's adjective-clusters
  (`Task → Assigned Task → Task done`) → a `status` enum property on the
  entity's schema, plus a per-transition action endpoint, same treatment as
  EventStorming state machines (`eventstorming-mapping.md` §5).
- Actors → OAuth2 scopes/roles, same rule as EventStorming actors
  (`eventstorming-mapping.md` §7). A role transition ("Anonymous *becomes*
  Cook") → a scope-elevation operation if the story shows how it happens;
  otherwise flag it as an open question rather than inventing the mechanism.
- Optional steps (a box literally labeled "optional") → operations that
  simply aren't required by any other operation's flow; no special OpenAPI
  treatment beyond normal optionality.
- Physical/external objects and UI-surface/channel objects from the story
  (a Bicycle, a Rack, "via App") are **not** resources — skip them; they
  describe real-world context, not API-owned data.
- A Domain Story is one concrete path with no conditionals (its own core
  mental model). Don't assume it lists every operation the API needs. If the
  "obvious" remaining CRUD isn't shown (the story never deletes a Task, but
  the resource clearly needs deletion), ask before adding it rather than
  filling the gap silently.

## 4. Reconciling both

When both a Domain Story and a Visual Glossary are supplied:

- The glossary wins on **naming and field shape** (§2); the story wins on
  **which operations exist and what triggers them** (§3).
- Where the same concept has two names across the two pictures (story says
  "Bike", glossary says "Bicycle"), use the glossary's term in the spec and
  note the discrepancy in your summary — don't silently harmonize without
  saying so.
- Where the story implies an attribute the glossary never defined, or the
  glossary defines a cardinality the story's use cases never exercise,
  surface it as an open item rather than guessing which source is stale.
- For anything beyond a quick one-off check, `domain-story-glossary-consistency`
  is the dedicated skill for a full term-by-term reconciliation — suggest it
  if the mismatches are numerous enough to warrant a structured pass before
  you generate the spec.

## 5. Hotspots and gaps still block generation of the affected part

Same rule as `eventstorming-mapping.md` §9: carry over open questions from
either brief that affect the *contract* (an undefined term, a missing
inverse cardinality that changes nesting, a use case with no clear resource)
and surface them to the user before finalizing that part of the spec.
Cosmetic ambiguities can be resolved with a stated assumption noted in the
summary.