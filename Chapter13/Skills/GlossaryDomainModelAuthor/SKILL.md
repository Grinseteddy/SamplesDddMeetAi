---
name: glossary-domain-model-author
description: Turn a Visual Glossary into source code for ONE bounded context's domain model, deciding entity vs value object per term with a written rationale. Asks for the target language first.
author: Annegret Junker
---

# Glossary Domain Model Author

Turn a **Visual Glossary** (photo, SVG, transcription, or Glossary Brief) into
a **domain model in code** for exactly **one bounded context**: entities,
value objects, aggregates, and the invariants the glossary's cardinalities
assert — written in the language the user picks.

The output is always two things:

1. **Source files** — one per aggregate (or per type, where the language's
   idiom demands it), compiling.
2. **A Model Decision Record** (`MODEL-DECISIONS.md`) — for every term: entity
   or value object, the evidence, the confidence, and the open questions.
   Code without the record hides the judgement calls; the record is how a
   domain expert checks them.

Use it whenever someone wants a domain model, domain classes, entities and
value objects, aggregates, or "the code for this context" from a visual
glossary, concept map, term diagram, or ubiquitous-language picture — even if
nobody says "Visual Glossary".

---

## Step 1 — Ask for the target language FIRST

Before reading the glossary in depth, ask which language to generate. Use
`AskUserQuestion` (or a plain-text question if that tool is absent) with the
**four most probable languages**, the best guess first and marked
`(Recommended)`. "Other" is always available for anything else.

Rank the candidates by evidence, strongest first:

1. **The user said it** — a language named anywhere in the conversation
   (then skip the question entirely and confirm in one line).
2. **The codebase** — if a repo or folder is attached, detect it:
   `pom.xml`/`build.gradle` → Java or Kotlin (check for `.kt` files),
   `*.csproj` → C#, `package.json` + `tsconfig.json` → TypeScript,
   `pyproject.toml`/`requirements.txt` → Python, `go.mod` → Go,
   `Cargo.toml` → Rust.
3. **Sibling artifacts** — an OpenAPI/AsyncAPI/protobuf spec with
   `java_package`, `csharp_namespace`, or `go_package` options, or code
   snippets earlier in the chat.
4. **Default ranking** when there is no evidence — the languages DDD domain
   models are most often written in:
   **Java, Kotlin, C#, TypeScript** (Python, Go, Rust via "Other").

In the same `AskUserQuestion` call you may add at most two more questions
**only if they are genuinely open**:

- *Which bounded context?* — when the glossary shows more than one
  group/colour/lane (list the contexts as options).
- *Root package / namespace?* — only if no codebase gives it away; otherwise
  derive it (e.g. `com.<org>.<context>.domain`).

If nobody is there to answer (scheduled/unattended run), take the top-ranked
language, state it at the top of the output, and carry on.

---

## Step 2 — Read the glossary

Prefer a **Glossary Brief** from `visual-glossary-interpreter`. If you only
have a picture, run that skill (or do its transcription step yourself) first:
a misread arrow becomes a wrong class.

Transcribe into three working tables before deciding anything:

| Term (exact spelling) | Group / context | Attributes drawn |
|---|---|---|

| From | Verb / label | To | Cardinality (both ends if given) |
|---|---|---|---|

| Generalisation (`is-a`) | Sub | Super |
|---|---|---|

Keep the glossary's **exact spelling** — it is the ubiquitous language.
Plural stickies (`Authors`) become singular types with a collection field.

---

## Step 3 — Fix the bounded context

The model belongs to **one** context. Everything else is *outside*.

- Terms in the chosen context → modelled in full.
- Terms in **other** contexts that this context points to → **not** modelled.
  They appear only as a typed identifier value object (`BookId`, `CustomerId`)
  and are listed under *External references* in the decision record.
- A term drawn in two contexts → model only this context's meaning of it;
  flag the other meaning as a translation concern.
- No groups drawn → the whole glossary is the context; say so.

Never import or embed a type from another context.

---

## Step 4 — Decide: entity or value object

This is the core of the skill. Decide **per term**, record the evidence, and
assign a confidence (**High / Medium / Low**).

### Signals for ENTITY

| Signal in the glossary | Weight |
|---|---|
| An identifier is drawn (`ISBN`, `Order Number`, `… ID`, `… Number`) | strong |
| It is referenced from **several** other terms or from another context | strong |
| It has a **lifecycle / status** term attached (`Status`, `State`, `Phase`) or verbs implying change over time (`is borrowed`, `is cancelled`) | strong |
| Something else **refers to it by identity** across an aggregate boundary | strong |
| It is the target of `0..*`/`1..*` from a term that does not own it (association, not composition) | medium |
| The domain would say "**which** one?" rather than "**what** value?" | medium |

### Signals for VALUE OBJECT

| Signal in the glossary | Weight |
|---|---|
| A **leaf** term with no outgoing edges (`Title`, `Amount`, `Date`) | strong |
| It is **measured, described or quantified** something (`Money`, `Address`, `Period`, `Name`) | strong |
| Two instances with equal attributes are **interchangeable** | strong |
| It is reached only through **composition** from one owner (`has`, unlabeled edge) | medium |
| It groups attributes that always travel together (`Name` + `Surname` → `PersonName`) | medium |
| A closed set of named values (`Genre: Fiction, Non-Fiction`) → value object as **enum** | strong |

### Decision rules

1. **Strong entity signal → Entity**, regardless of value-object signals.
2. **Only value-object signals → Value Object.**
3. **Mixed or none** — the classic case is a `0..1` term with no attributes
   (`Publisher` on a book). **Default to Value Object**, confidence **Low**,
   and raise an open question. Reason: promoting a value object to an entity
   later is an additive change; demoting an entity breaks identity and
   references.
4. **Identity is a value object too.** Every entity gets a typed ID value
   object (`BookId`), never a bare `String`/`UUID`/`long`.
5. **Single-leaf value objects still get a type** when they carry a rule
   (format, range, non-blank). A leaf with **no** rule may stay a primitive
   field — say so in the record. Don't wrap for the sake of wrapping.

### Aggregates

After classification, cut aggregates:

- Each **aggregate root** is an entity that is referenced from outside.
- An entity reachable **only** through composition from a root is an
  **inner entity** of that aggregate (local identity, no repository).
- **Inside an aggregate: hold the object. Across aggregates: hold the ID.**
- One repository interface per aggregate root, nothing else.

---

## Step 5 — Turn cardinalities and rules into invariants

The glossary's cardinalities are business rules. Enforce them in
constructors/factories and mutating methods, not in comments.

| Glossary | In the model |
|---|---|
| `1` | required constructor parameter, null/blank check |
| `0..1` | optional type (`Optional`, nullable `?`, `| undefined`, `Option`) |
| `0..*` | collection, may be empty, exposed read-only |
| `1..*` | collection, check non-empty |
| `1..10`, `0..25` | collection + bound check; **name the bound** as a constant |
| `is-a` | sealed interface/abstract type with subtypes (or a discriminated union) — only inside this context |
| closed value set | enum / sealed hierarchy / union of literals |

Each invariant gets a descriptive domain exception or error result —
`TooManyAuthors`, not `IllegalArgumentException("invalid")`.

Suspiciously round bounds (`10`, `25`) are often UI limits promoted to rules:
implement them, and list them as open questions.

---

## Step 6 — Write the code in the chosen language's idiom

Common shape in every language:

- **Value objects**: immutable, equality by all attributes, validated on
  creation, no setters, behaviour that belongs to the value
  (`Money.add`).
- **Entities**: equality and hash **by ID only**, state changed only through
  intention-revealing methods named with glossary verbs (`borrow()`, not
  `setStatus()`), invariants checked after every change.
- **Aggregate roots**: the only public entry point; inner collections exposed
  as unmodifiable views.
- **No framework annotations** (JPA, Jackson, ORM decorators) in the domain
  layer unless the user asks — persistence is another layer's concern.
- **Names** = glossary spelling in the language's casing convention.

Language idioms:

| Language | Value object | Entity | ID | Optional / union |
|---|---|---|---|---|
| Java 17+ | `record` with compact-constructor validation | `final class`, `equals`/`hashCode` on id | `record BookId(UUID value)` | `Optional<T>` (getters only), `sealed interface … permits` |
| Kotlin | `data class` / `@JvmInline value class` for single-field, `init { require(…) }` | `class` with id-based `equals` | `@JvmInline value class BookId(val value: UUID)` | `T?`, `sealed interface` |
| C# 12 | `sealed record` (or `readonly record struct` for tiny ones) | `sealed class` deriving from an `Entity<TId>` base | `readonly record struct BookId(Guid Value)` | `T?`, abstract record hierarchy |
| TypeScript | `class` with `readonly` fields + private ctor + static `create()` returning result/throwing; `equals()` | `class` with `equals` on id | branded type `type BookId = string & { readonly __brand: 'BookId' }` | `T \| undefined`, discriminated union |
| Python 3.11+ | `@dataclass(frozen=True)` + `__post_init__` | `@dataclass(eq=False)` with id-based `__eq__`/`__hash__` | `NewType` or frozen dataclass | `T \| None`, `Enum`, ABC |
| Go | struct with unexported fields + `New…` constructor returning `(T, error)` | struct + pointer receivers, `Equals` on id | `type BookID string` | pointer or `ok` bool, interface + type switch |
| Rust | struct, `TryFrom`/`new` returning `Result`, derive `Eq` | struct, `PartialEq` on id only | newtype `struct BookId(Uuid)` | `Option<T>`, `enum` |

File layout (adapt to the language's conventions):

```
<root>/<context>/domain/
  <aggregate>/            # one folder or module per aggregate
    <Root>.<ext>
    <Root>Id.<ext>
    <InnerEntity>.<ext>
    <ValueObject>.<ext>
    <Root>Repository.<ext> # interface only
  shared/                 # value objects used by several aggregates in THIS context
  external/               # ID value objects of other contexts' terms
```

---

## Step 7 — Verify

Compile it. Install the toolchain if missing (`javac`, `kotlinc`, `dotnet`,
`tsc --strict --noEmit`, `python -m mypy --strict`, `go vet`,
`cargo check`). Fix every error.

Then write a **small smoke test** per aggregate: construct a valid instance,
and violate one invariant per cardinality rule to prove it is rejected. Run it.
If a toolchain can't be installed, say so plainly and list what was not
verified.

---

## Step 8 — Deliver

1. The source tree (zip it if it is more than a handful of files).
2. `MODEL-DECISIONS.md`, structured as:

   - **Context**: name, language, root package, terms in scope, terms
     excluded and why.
   - **Classification table**:

     | Term | Kind (Entity / Aggregate Root / Inner Entity / Value Object / Enum / External ID) | Evidence from the glossary | Confidence |
     |---|---|---|---|

   - **Aggregates**: root, members, references to other aggregates by ID.
   - **Invariants**: each cardinality rule → where it is enforced.
   - **External references**: terms of other contexts held only by ID.
   - **Open questions** for a domain expert — every Low-confidence
     classification, every round-number bound, every unlabeled edge assumed to
     be composition, every term spelled differently in two places.

3. In the reply: one or two sentences — the aggregates produced and how many
   open questions need a domain expert. Don't restate the record.

---

## What not to do

- Don't model more than one bounded context, and don't invent contexts the
  glossary doesn't draw.
- Don't add attributes, terms or behaviour the glossary doesn't support; a gap
  is an open question, not an invention.
- Don't rename terms into "better" English — the glossary's spelling wins.
- Don't generate getters/setters for everything; entities change through
  domain verbs.
- Don't silently pick entity vs value object — every call is in the record.
- Don't use primitive IDs or embed another aggregate's object.
