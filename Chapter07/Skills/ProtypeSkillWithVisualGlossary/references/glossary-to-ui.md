# Glossary → UI: the full mapping

How each construct in a Glossary Brief becomes a decision in the prototype. Read this
before building whenever a visual glossary is in play.

Contents:
1. Term classification → what gets a screen
2. Cardinalities → form controls and validation
3. Relationship kinds → composition vs reference
4. Identity terms
5. Bounded contexts
6. Gaps in the picture
7. Seed data that exercises the bounds
8. Conflict protocol

---

## 1. Term classification → what gets a screen

The glossary's §4 (derived domain model) tells you the *shape* of things. The story
tells you which of them a user ever sees. Both constraints apply.

| Glossary calls it | In the data | In the UI |
|---|---|---|
| **Aggregate root** | The unit saved as a whole | The thing a detail screen is *about*; its parts are edited inline on that screen and saved together |
| **Entity** (referenced independently) | Own record with own id | A picker / autocomplete on the referencing form; gets list & detail screens **only if a story sentence acts on it** |
| **Value object** (leaf, no identity) | A field or embedded object on the parent | A form field on the parent's form. Never its own screen, never its own CRUD |
| **Identity term** (`ISBN`, `Order No.`) | The key | Shown prominently, read-only after create — see §4 below |
| **Orphan term** (no edges) | Nothing yet | Nothing. List it as an open question |

The trap worth naming: a glossary with fifteen terms does not imply fifteen screens.
Most terms are fields. A term becomes navigable only when the story shows a person
acting on it.

## 2. Cardinalities → form controls and validation

Read the cardinality on the *target* end (`Parent —label→ card Child`).

| Cardinality | Meaning | Control | Validation |
|---|---|---|---|
| `1` | Exactly one, mandatory | Single field or picker | Required; block save when empty; the parent cannot be created without it |
| `0..1` | Optional single | Single field or picker, clearly optional | None; render a real empty state ("No publisher") rather than a blank |
| `1..*` | One or more | Repeater / tag input, one row seeded | Min 1; refuse removal of the last row with a message |
| `0..*` | Any number | Repeater / tag input, starts empty | None; needs a genuine empty state |
| `1..N` (e.g. `1..10`) | Bounded, mandatory | Repeater | Min 1, max N; disable "Add" at N and say why |
| `0..N` (e.g. `0..25`) | Bounded, optional | Repeater / tag input | Max N; disable "Add" at N and say why |

Two rules about bounds:

- **Enforce the bound and question it.** A precise maximum is often a remembered
  screen limit rather than a business rule. Implement it, then repeat the glossary's
  open question in the readme. Enforcing silently makes an arbitrary cap look agreed.
- **The unstated inverse is not zero.** The glossary usually gives one end only.
  `Book —has→ 1..10 Author` says nothing about how many Books an Author has. Don't
  invent an inverse constraint; if the prototype needs one to work, pick the loosest
  reading (`0..*`) and flag it.

Optionality is a design instruction as well as a validation rule: every `0..1` and
`0..*` asserts a real instance exists without that part, so at least one screen has to
look right when it's missing.

## 3. Relationship kinds → composition vs reference

- **Composition-flavoured (`has`, `contains`, and the parts have no life of their
  own)** → edited inline on the parent's screen, created and deleted with it, no
  separate route.
- **Plain association (`assigned to`, `published by`, target has its own identity)** →
  a reference. A picker on the form, and the target's own screen only if the story
  earns it.
- **`is-a` / generalisation (hollow-triangle arrowhead)** → one shared form with the
  base fields plus a type selector that reveals the subtype-specific fields. Do not
  build parallel near-identical screens per subtype.
- **Shape and label disagreeing** (an `is-a`-shaped arrow labeled `enhances`,
  `annotates`, `describes`) → genuinely ambiguous; the glossary interpreter flags it
  as a first-order finding. Pick the *reversible* reading — model it as a plain
  reference rather than a subtype — and put the choice in the readme, because a
  subtype hierarchy is expensive to undo.

## 4. Identity terms

Every entity should have one. In the prototype:

- Show it on the detail screen and in list rows.
- Editable at create, read-only afterwards (a changing identity breaks the mental
  model a prototype exists to test).
- If the glossary names **no** identity for an entity, generate a synthetic id, show
  it as such, and put "what identifies a `<Term>`?" in the open questions. Do not
  quietly appoint the name field as the key.

## 5. Bounded contexts

Glossary §6 groups are module boundaries; story §3 groups are module boundaries. They
should agree.

- **They agree** → one nav section per group; the entities of that group live there.
- **They disagree** → follow the story for navigation (it knows the flow) and report
  the mismatch as a finding. The glossary may be describing a data boundary the UI
  doesn't need to mirror — that is worth knowing, not worth silently smoothing.
- **A term in two contexts** → in DDD that's often the same real thing modelled twice
  on purpose. Keep both, name the context in the UI where ambiguity would confuse, and
  ask one confirming question.
- **No groups shown** → one module, and say so.

## 6. Gaps in the picture

The glossary interpreter records `(unlabeled)` and `(no cardinality)` rather than
filling them in. Carry that honesty into the build:

| Gap | What to build | What to say |
|---|---|---|
| Edge with no label | A plain reference field named after the target | "The relationship verb wasn't given" |
| End with no cardinality | Unconstrained field — no required flag, no max | "No multiplicity given; nothing enforced here" |
| Undefined term (no gloss) | A field/label using the exact spelling | "The glossary names it but doesn't define it" |
| Term the story never touches | A field if it hangs off a shown entity; otherwise nothing | "Unexercised by the story" |
| Story term absent from glossary | Keep the story's spelling verbatim | "Not in the glossary — needs ratifying" |

Inventing a plausible default here is worse than leaving a gap, because a plausible
default gets reviewed as if it were agreed.

## 7. Seed data that exercises the bounds

Seed data is where cardinalities stop being abstract. Aim for a handful of records
that between them cover:

- **Each interesting state** from the story's state machines (§5) — so no screen is
  empty and every badge style is visible.
- **At least one record at an upper bound** — the Book with all ten Authors, the entry
  with 25 Tags — so the reviewer sees what a full one looks like and hits the disabled
  "Add" control.
- **At least one record with every optional part missing** — no Publisher, no
  Abstract, no Tags — so the empty states get exercised.
- **A `1..*` record with exactly one child**, so the "can't remove the last one"
  behaviour is reachable.

Use the story's own concrete scenario for at least one record: the story is a real
example, and reviewers recognise it.

## 8. Conflict protocol

When the story and the glossary disagree, the resolution is fixed and the reporting is
mandatory:

| Conflict | Who wins | Why |
|---|---|---|
| Different name for the same thing | Glossary | It is the terminology authority |
| Cardinality vs. what the story shows | Glossary | It is the structure authority |
| Which screens exist / what order | Story | The glossary has no behaviour |
| Module boundaries | Story, for navigation | The glossary boundary is still reported |
| Something only one artifact mentions | Whichever mentions it — build it, mark it | Nothing is dropped silently |

Every row of this table that fires becomes a line in the readme's open questions. The
disagreements are often the most valuable output of the whole exercise: they are the
places where the team hasn't actually agreed yet, and the prototype is what makes that
visible.