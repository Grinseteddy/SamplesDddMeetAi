# House-Style Conventions (full reference)

The authoritative description of the protobuf house style. The bundled
`assets/example-book-catalog/` demonstrates every rule here and passes
`buf lint` with `STANDARD` + `COMMENTS`; treat it as canonical and mirror its
structure and formatting.

## Contents
1. Syntax and file header
2. Packages, directories, and versioning
3. File options
4. Naming
5. Messages
6. Fields and scalar type choice
7. Field numbering, `reserved`, and evolution
8. Presence: `optional`, `repeated`, `oneof`, maps
9. Enums
10. Well-known types
11. Comments and documentation
12. Formatting
13. Services (only when asked)
14. Protobuf Editions

---

## 1. Syntax and file header

Every file opens the same way:

```proto
syntax = "proto3";

// One or two sentences: what this file covers, and where the model came from.
// Naming the source glossary is what makes the schema traceable to the
// picture it was derived from.
package library.catalog.v1;
```

`syntax = "proto3";` is the first non-comment line — proto2 is legacy, and
Editions is deliberate opt-in (§14). A file header comment sits above the
`package` line, not above `syntax`.

Order within a file: `syntax`, package comment + `package`, `import`s (sorted),
`option`s (sorted), then messages, then enums. One top-level aggregate per file
is the default.

---

## 2. Packages, directories, and versioning

The package is `<domain>.<context>.v<N>`, all lowercase, dot-separated:

```
library.bibliographic.v1
library.catalog.v1
```

Three rules, all enforced by `buf lint`:

- **The directory path must match the package.** `package library.catalog.v1`
  lives at `library/catalog/v1/`. A build tool derives import paths from the
  directory, so a mismatch breaks imports in ways that are tedious to debug.
- **The package must end in a version suffix** (`v1`, `v1beta1`). The version is
  a *package*-level concept in protobuf, not a file-level one: `v2` is a new
  package that coexists with `v1` rather than replacing it.
- **All files in a directory share one package.**

The context segment comes from the glossary's groups. With no groups, use one
context segment named for the domain and say so.

---

## 3. File options

Include the options that make generated code land in sensible places. At
minimum:

```proto
option go_package = "github.com/<org>/<repo>/gen/library/catalog/v1;catalogv1";
option java_multiple_files = true;
option java_outer_classname = "CatalogEntryProto";
option java_package = "com.<org>.library.catalog.v1";
```

- `java_multiple_files = true` generates one class per message instead of one
  giant outer class — almost always what you want.
- `java_outer_classname` is the file name in PascalCase plus `Proto`.
- Ask for the org/repo rather than inventing one; if the user doesn't care, use
  `example` and note it.

Options for languages the user isn't generating are noise — leave them out.

---

## 4. Naming

| Element | Convention | Example |
|---|---|---|
| File | `lower_snake_case.proto`, named for its aggregate | `catalog_entry.proto` |
| Package | `lower.dotted.vN` | `library.catalog.v1` |
| Message | `PascalCase`, **singular** | `CatalogEntry` |
| Field | `lower_snake_case` | `catalog_entry_id` |
| `repeated` field | `lower_snake_case`, **plural** | `authors`, `tags` |
| Enum type | `PascalCase`, singular | `LoanStatus` |
| Enum value | `UPPER_SNAKE_CASE`, enum-name-prefixed | `LOAN_STATUS_ACTIVE` |
| `oneof` | `lower_snake_case` | `item` |

**Keep the domain's words.** The glossary is the terminology authority: if it
says `Catalog entry`, the message is `CatalogEntry`, not `CatalogRecord` or
`Item`. Fixing plural sticky names (`Authors` → `Author`) is the one routine
exception, because the plurality moves to `repeated`.

**Don't abbreviate** (`publication_date`, not `pub_dt`) and **don't repeat the
message name in its fields** (`Book.title`, not `Book.book_title`) — the one
deliberate exception is the identifier, where `catalog_entry_id` reads better
than a bare `id` at the point of use and matches the reference field on other
messages.

**Avoid field names that are keywords in target languages** — `class` breaks
Python attribute access, and `package`, `import`, `def`, `from`, `return` cause
friction elsewhere. If the domain term collides, keep the domain word in the
comment and pick the nearest non-colliding field name.

---

## 5. Messages

- One aggregate root per file; its parts follow it in the same file.
- The root message comes first, then its embedded parts, then enums.
- Nested messages (`message Book { message Author { … } }`) are for types that
  are meaningless outside their parent *and* never referenced elsewhere. Prefer
  top-level messages — nesting shows up in generated names in every language and
  is awkward to undo.
- Every message gets a doc comment saying what the thing is **in the domain's
  words**, and, for a root, that it is an aggregate root and what its identity
  is.

---

## 6. Fields and scalar type choice

| Domain thing | Type |
|---|---|
| Name, title, free text, description | `string` |
| Identifier — UUID, ISBN, any natural key | `string` |
| Count, quantity, index | `int32` (`int64` if it can exceed ~2 billion) |
| Numeric ID from a legacy system | `int64` |
| Flag | `bool` |
| Opaque blob, hash, encoded payload | `bytes` |
| Instant in time | `google.protobuf.Timestamp` |
| Calendar date with no time | `google.type.Date` |
| Money | `google.type.Money`, or minor units in `int64` |
| Measurement | `double`, with the unit in the field name |

Notes that repay attention:

- **Identifiers are `string`, even when they look numeric.** ISBNs have check
  digits and leading zeros; nobody does arithmetic on them; and a `string`
  survives a change of identifier scheme.
- **Never use `float`/`double` for money.** Binary floating point cannot
  represent 0.10, and the rounding errors surface in totals.
- **Put units in the name**: `duration_seconds`, `weight_grams`,
  `radius_meters`. A bare `weight` is a production incident waiting for its
  second consumer.
- `int32`/`int64` are variable-length encoded and inefficient for negative
  numbers — use `sint32`/`sint64` when values are genuinely often negative, and
  `fixed64` for values that are always large.

---

## 7. Field numbering, `reserved`, and evolution

Field numbers are the wire contract: the number, not the name, identifies the
field in the encoded bytes. Renaming a field is safe on the wire; renumbering it
is not, and a reused number silently reinterprets old data as the new field.

**Assigning numbers:**

1. Identity is always `1`.
2. Numbers `1–15` take one byte for the tag; `16–2047` take two. Spend the
   single-byte range on fields present on most messages.
3. After that, follow the glossary's reading order so the file is diffable
   against the picture.
4. Leave a gap before a block you expect to grow, and `reserved` the numbers you
   want protected.

**Removing a field:** never delete the number silently. Reserve both:

```proto
message CatalogEntry {
  reserved 4, 7 to 9;
  reserved "shelf_code";
}
```

This makes reuse a compile error rather than a data-corruption bug.

**19000–19999 are reserved by protobuf itself** — don't allocate there.

**Compatible changes:** adding a field with a fresh number; renaming a field;
adding an enum value; adding a message. **Incompatible:** changing a field's
number or type, moving a field in or out of a `oneof`, changing cardinality
between singular and `repeated`, removing an enum value.

---

## 8. Presence: `optional`, `repeated`, `oneof`, maps

- **`optional`** marks explicit presence on a singular field. Use it for every
  `0..1` in the glossary. Without it, a scalar's default (`""`, `0`, `false`) is
  indistinguishable from unset — which erases the optionality the glossary
  asserted.
- Message-typed singular fields always have presence; `optional` on them is
  allowed and worth writing anyway, for symmetry and readability.
- **`repeated`** for every `*` cardinality. Never combine with `optional`: an
  empty list is absence. Order is preserved on the wire, so if the domain has no
  order, say so in the comment.
- **`oneof`** for "exactly one of these". Fields inside a `oneof` cannot be
  `repeated` — wrap each alternative in a message if you need that. Note that a
  `oneof` with a single case is a compatible way to add presence later.
- **`map<K, V>`** for keyed collections where the key is meaningful in the
  domain. Maps have no defined order, cannot be `repeated`, and cannot be
  extended with metadata later — a `repeated` message of key/value pairs is
  often the more durable choice when the entry might grow fields.

---

## 9. Enums

```proto
// The lifecycle states of a loan.
enum LoanStatus {
  // Not set. Never a valid domain state.
  LOAN_STATUS_UNSPECIFIED = 0;

  // The borrower currently holds the item.
  LOAN_STATUS_ACTIVE = 1;

  // The item has been returned to the library.
  LOAN_STATUS_RETURNED = 2;
}
```

- **`_UNSPECIFIED = 0` always.** proto3 defaults enums to zero, so without it an
  unset field silently asserts whichever real state is first.
- **Prefix every value with the enum name.** Enum values live in the
  *enclosing* scope in C++, so unprefixed values collide across enums in one
  package.
- Enums are top-level unless they are meaningless outside one message.
- Only create an enum where the glossary asserts a closed set. Adding a value to
  a released enum is a compatibility event for every consumer with an exhaustive
  switch.
- Never renumber or remove a value; `reserved` it.

---

## 10. Well-known types

Prefer these to hand-rolled equivalents — every language's generated code has
conversions for them:

| Type | Import | Use for |
|---|---|---|
| `google.protobuf.Timestamp` | `google/protobuf/timestamp.proto` | an instant, UTC |
| `google.protobuf.Duration` | `google/protobuf/duration.proto` | a length of time |
| `google.type.Date` | `google/type/date.proto` | a calendar date with no time |
| `google.type.Money` | `google/type/money.proto` | an amount with a currency |
| `google.protobuf.FieldMask` | `google/protobuf/field_mask.proto` | partial-update paths |

`google.protobuf.*` ships with protobuf itself. `google.type.*` comes from
`buf.build/googleapis/googleapis`, which is a module dependency — check it
resolves before relying on it, and fall back to modelling the fields explicitly
if it doesn't.

Avoid the wrapper types (`google.protobuf.StringValue` and friends). They exist
to give proto3 scalars presence, and the `optional` keyword now does that more
cleanly.

---

## 11. Comments and documentation

This house style is comment-heavy on purpose, and `buf lint`'s `COMMENTS`
category enforces it: every message, field, enum, and enum value carries a `//`
comment.

The reason is specific to this skill. A `.proto` derived from a glossary throws
away most of the glossary — the cardinalities, the optionality reasons, the
unresolved questions. The comments are where that survives. Three kinds:

**What it is, in the domain's words:**
```proto
// This library's record about a book.
```

**What the glossary asserted:**
```proto
// Glossary: `1..10` — at least one, at most ten. Neither bound is
// expressible in proto3; both must be checked in application code.
```

**What is still unresolved:**
```proto
// TODO(glossary): is the maximum of ten a real business rule? A multi-author
// scientific volume breaks it immediately.
```

Use `//` throughout, never `/* */`. Leading comments attach to the element
below; trailing comments on the same line are for short asides only.

---

## 12. Formatting

`buf format` is canonical — run it and commit its output rather than arguing.
What it produces: two-space indent, one blank line between messages and between
commented fields, no trailing whitespace, imports and options sorted.

---

## 13. Services (only when asked)

This skill produces the data model. A glossary describes nouns, and deriving
RPCs from it means inventing behaviour the picture does not assert — that comes
from a domain story, an EventStorming board, or an event model. If the user does
ask for a service:

```proto
// Reads and writes catalog entries.
service CatalogService {
  // Returns a single catalog entry by its identifier.
  rpc GetCatalogEntry(GetCatalogEntryRequest) returns (GetCatalogEntryResponse);
}
```

- `PascalCase` service name ending in `Service`; `PascalCase` verb-noun RPC.
- **Always a dedicated request and response message per RPC**, named
  `<Rpc>Request` / `<Rpc>Response`, even when one field would do. It is the only
  way to add a parameter later without breaking the signature.
- Never use a domain message directly as a request or response type.
- Services live in their own file (`catalog_service.proto`) importing the model.

---

## 14. Protobuf Editions

Editions (`edition = "2023"`) replaces the proto2/proto3 split with per-file and
per-field *features* — `field_presence`, `enum_type`, and so on — and is where
protobuf is heading. It is **not** the default here: proto3 remains far better
supported across tooling, generated-code targets, and third-party consumers.

Use Editions when the user asks for it, or when they need per-field presence
control that proto3 can't express. The conventions in this document carry over
unchanged apart from the header:

```proto
edition = "2023";

package library.catalog.v1;
```

Under Editions, explicit presence is the default for singular fields, so the
`optional` keyword is replaced by `features.field_presence = EXPLICIT` (or
`IMPLICIT` to opt out). Note the change prominently when you use it.