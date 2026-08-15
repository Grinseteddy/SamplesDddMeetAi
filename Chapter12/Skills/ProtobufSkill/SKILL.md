---
name: protobuf-model-author
description: >-
  Turn a Visual Glossary into a Protocol Buffers model — proto3 `.proto` files
  whose messages, enums, packages and field numbering follow a consistent house
  style (the same style as the bundled book-catalog example). Use this skill
  whenever someone wants protobuf, proto3, a `.proto` file, a protobuf schema,
  an IDL, or a gRPC/wire data model built from a visual glossary, glossary
  brief, domain concept map, term or noun diagram, ubiquitous language, or a
  derived domain model of entities, value objects and aggregates — including
  phrasings like "turn this glossary into protobuf", "generate proto messages
  from these terms", "model this domain in protobuf", or a glossary picture
  shared alongside a request for a schema. Trigger even when nobody says
  "Visual Glossary". Also use it when extending an existing `.proto` so
  additions match the conventions, and when the user asks to compile, validate
  or lint protobuf. Produces `.proto` files that compile and pass `buf lint`.
author: Annegret Junker
---

# Protobuf Model Author

This skill turns the **static domain model** a Visual Glossary captures into
**proto3** `.proto` files: one package per bounded context, one message per
entity, fields for value objects, and an explicit record of every business rule
protobuf cannot enforce.

The bundled `assets/example-book-catalog/` is the gold standard — a full
translation of the library book-catalog glossary from
`visual-glossary-interpreter`'s worked example. When in doubt about structure,
naming, or how to comment a judgement call, open it and mirror it.

The conventions here are **strong defaults**. Adapt them when the domain
genuinely calls for something else, but keep the output internally consistent.

## The mental model: protobuf is a wire format, not a domain model

This is the whole difficulty of the job, and getting it wrong produces a
plausible-looking file that quietly discards half the glossary.

A glossary asserts **structure and business rules**. Protobuf expresses
structure well and rules almost not at all:

| The glossary says | proto3 can express | proto3 cannot express |
|---|---|---|
| `1` (mandatory) | the field | **that it is required** — proto3 has no `required` |
| `0..1` (optional) | `optional` (field presence) | — |
| `1..*` / `0..*` | `repeated` | — |
| `1..10`, `0..25` | `repeated` | **the bounds** |
| identity (`ISBN`) | a field | that it is the identity, or that it is unique |
| an `is-a` / generalisation edge | nothing | **inheritance does not exist in protobuf** |
| composition vs association | both look like fields | the difference |

So the deliverable is **two things, always**: the `.proto` files, and a short
**Rules Not Expressible** list naming every constraint that has to move into
application code or a validation layer. Every one of those also becomes a
comment on the field it belongs to, so the rule travels with the schema instead
of living only in a chat message.

Three consequences worth internalising before you write anything:

1. **Every constraint you drop silently is a bug someone ships.** A `1..10`
   that becomes a bare `repeated` with no comment is a business rule deleted.
2. **Field numbers are permanent.** They are the wire contract. Renumbering a
   released field is a breaking change that silently misinterprets old data —
   which is why numbering deserves real thought (see step 5).
3. **You are translating an interpretation, not a picture.** The glossary's
   entity/value-object/aggregate split is already a judgement call. Inherit
   those calls, mark them, and never quietly make new ones.

## Workflow

### 1. Get the Glossary Brief (don't skip to the picture)

The input this skill wants is the **Glossary Brief** produced by
`visual-glossary-interpreter` — specifically its transcription, relationship
table with cardinalities, and derived domain model (entities, value objects,
aggregates, identity).

- If the user hands you a raw glossary image or diagram, run
  `visual-glossary-interpreter` first. Translating straight from a picture skips
  the transcription step, and a misread edge becomes a wrong wire contract.
- If they hand you a brief, an ERD, a class model, or a plain list of terms and
  cardinalities, use it directly.
- If a term's classification is genuinely unresolved in the brief (the classic
  case: a `0..1` term with no attributes, which could be a value object or an
  entity), pick the reading that keeps the schema smallest, and mark it with a
  `TODO(glossary)` comment. Don't stall the file on it, and don't hide it.

Also establish **who consumes this**: internal service-to-service messages, a
public gRPC API, or event payloads. It affects how conservative the versioning
and field-numbering need to be.

### 2. Read the conventions

Read `references/conventions.md` before writing — it's the authoritative house
style (packages, file layout, naming, field numbering, enums, well-known types,
comments, reserved ranges). The summary at the end of this file is a reminder,
not a substitute.

Read `references/mapping-rules.md` alongside it — that's the construct-by-
construct translation table from glossary elements to proto elements, including
the cases with no clean answer (generalisation, many-to-many, bounded
cardinality, shared vocabulary).

### 3. Decide the packages and the file layout

Glossary **groups / bounded contexts become packages**, one directory each,
versioned:

```
library/bibliographic/v1/book.proto      → package library.bibliographic.v1;
library/catalog/v1/catalog_entry.proto   → package library.catalog.v1;
```

The directory path must match the package (`buf lint` enforces this), and the
package must end in a version suffix. If the glossary shows **no groups**, use a
single package and say so — don't invent context boundaries the picture doesn't
assert.

One file per aggregate is a good default; one file per context is fine when the
context is small.

### 4. Map terms to messages and fields

Work aggregate by aggregate, following `references/mapping-rules.md`. The
headlines:

- **Entity → `message`**, its identity as **field 1**.
- **Value object with one leaf → a scalar field** on its parent (`Title` becomes
  `string title = 2`, not its own message).
- **Value object with several leaves → its own `message`**, embedded by value.
- **Parts of an aggregate → embedded messages**, owned by the root.
- **References across aggregates or contexts → the other thing's identifier,
  not an embedded message and not an import.** `CatalogEntry` holds
  `string book_isbn`, not a `Book`. This keeps aggregates independently
  loadable and keeps contexts decoupled — the single most consequential rule
  here.
- **Plural sticky names stay singular.** `Authors` with `1..10` becomes
  `repeated Author authors`; the plurality lives in `repeated`.
- **A closed set of values → `enum`**, with a mandatory `_UNSPECIFIED = 0`.

### 5. Number the fields deliberately

Field numbers are the contract. Assign them once, in this order:

1. **Identity first** — the identifier is always field `1`.
2. **Fields 1–15 for what is present on most messages** — they encode in one
   byte; 16+ take two. Spend them on required and frequently-set fields, not on
   rarely-populated optional ones.
3. **Then everything else in glossary reading order**, so the file is diffable
   against the picture.
4. **Leave a gap** (e.g. jump to 20) before a block you expect to grow, and add
   a `reserved` range for numbers you want to keep out of reach.

Never reuse or renumber a field that has shipped; `reserved` it instead.

### 6. Write down what protobuf cannot say

As you write each field, attach the rule as a comment. Then collect them into
the **Rules Not Expressible** section of your final message. Typical entries:

- mandatory fields (proto3 has no `required` — every `1` in the glossary is an
  application-layer check);
- bounded repeats (`1..10` authors, `0..25` tags);
- uniqueness of identifiers;
- referential integrity of cross-aggregate ID references;
- any generalisation edge, since protobuf has no inheritance.

If the user wants these **enforced rather than documented**, `protovalidate`
expresses most of them as field options — see the opt-in section in
`references/mapping-rules.md`. It adds a module dependency, so it's not the
default.

### 7. Compile and lint

Always verify. Read `references/validation.md` for exact commands and fallbacks.

```bash
npm install -g @bufbuild/buf
buf build            # does it compile?
buf lint             # does it match the conventions?
buf format -d        # is it formatted canonically?
```

`buf build` is the real check — it fails on unresolved imports, duplicate field
numbers, and syntax errors. Fix every error and resolve warnings. If `buf` can't
be installed, fall back to `protoc` via `grpcio-tools` (also in
`references/validation.md`).

### 8. Present

Save the `.proto` files (with their `buf.yaml`) to the outputs directory and
present them. In the message, briefly cover:

- the packages and messages produced, and which glossary terms each covers;
- **the Rules Not Expressible list**;
- the judgement calls you inherited or made, and the `TODO(glossary)` comments
  the user should resolve with a domain expert.

## House style at a glance

Full detail in `references/conventions.md`.

**Syntax** — `syntax = "proto3";` on the first line. Protobuf Editions is
noted in the conventions but is not the default.

**Naming**
- File: `lower_snake_case.proto`, named for its aggregate (`catalog_entry.proto`).
- Package: `lower.dotted.vN` — domain, context, version (`library.catalog.v1`).
- Message and enum types: `PascalCase`, singular (`CatalogEntry`, `Tag`).
- Fields: `lower_snake_case`, singular unless `repeated`, where they're plural
  (`authors`, `tags`).
- Enum values: `UPPER_SNAKE_CASE`, **prefixed with the enum name**
  (`TAG_KIND_SUBJECT`), because enum values share the enclosing scope in C++.
- Zero value: always `<ENUM_NAME>_UNSPECIFIED = 0`.

**Comments** — every message, field, and enum value carries a `//` comment. It's
where the glossary's cardinality, optionality, and open questions survive. Use
`TODO(glossary):` for anything a domain expert still has to settle.

**Scalars** — `string` for names, text, and identifiers (including UUIDs and
natural keys like ISBN); `int32`/`int64` for counts and IDs that are genuinely
numeric; `bool` for flags; `bytes` for opaque data. Prefer `google.protobuf.
Timestamp` for instants and `google.type.Date` for calendar dates. Don't use
`float`/`double` for money — use `google.type.Money` or minor units in `int64`.

**Presence** — `0..1` gets the `optional` keyword so absent is distinguishable
from empty. `1` gets a plain field plus a comment saying it's mandatory.
`repeated` fields are never `optional` (an empty list *is* absence).

**Scope** — this skill produces the **data model**: messages and enums. gRPC
`service` definitions are out of scope unless the user asks for them; a glossary
describes nouns, and inventing RPCs from it means inventing behaviour the
picture doesn't assert. If they do ask, `references/conventions.md` has the
minimal service conventions.

## Bundled resources

- `references/conventions.md` — full house-style specification. Read before writing.
- `references/mapping-rules.md` — glossary construct → proto construct, including the hard cases and the opt-in `protovalidate` section. Read before writing.
- `references/validation.md` — installing and running `buf` and `protoc`, and what each check catches.
- `assets/example-book-catalog/` — gold-standard two-context translation of the library glossary, with its `buf.yaml`. Compiles and lints clean. Mirror it.
- `assets/skeleton.proto` — minimal starting template with the right shape, including an enum.