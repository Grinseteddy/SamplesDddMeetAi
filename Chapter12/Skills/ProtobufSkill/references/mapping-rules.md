# Mapping rules — Visual Glossary to Protocol Buffers

The construct-by-construct translation. The bundled
`assets/example-book-catalog/` applies every rule here to a real glossary.

## Contents
1. The quick table
2. Terms: entity, value object, aggregate
3. Cardinality and presence
4. Relationships: composition, association, and the aggregate rule
5. Many-to-many
6. Generalisation — the case protobuf cannot express
7. Groups and bounded contexts
8. Identity
9. Closed sets and enums
10. Pictograms and other notation
11. What to do with the glossary's open questions
12. Opt-in: enforcing rules with protovalidate
13. The Rules Not Expressible list

---

## 1. The quick table

| Glossary construct | Proto construct |
|---|---|
| Entity | `message`, identity as field 1 |
| Value object, single leaf | a scalar field on its parent |
| Value object, several leaves | its own `message`, embedded by value |
| Aggregate root | the outer `message` that owns its parts |
| Part of an aggregate | an embedded `message` field |
| Reference to another aggregate | a `string` (or typed) **identifier field** |
| Group / bounded context | a `package` and its directory |
| `1` | plain field + a comment saying it's mandatory |
| `0..1` | `optional` field |
| `1..*` / `0..*` | `repeated` field |
| `1..10`, `0..25` | `repeated` + a comment carrying the bound |
| Closed set of values | `enum` with `_UNSPECIFIED = 0` |
| Generalisation (`is-a`) | **nothing** — see §6 |
| Plural sticky name (`Authors`) | singular message, plural `repeated` field |

---

## 2. Terms: entity, value object, aggregate

The Glossary Brief has already made this split. Inherit it rather than
re-deriving it, and mark anywhere you had to depart from it.

**Entity → `message`.** It has its own identity and is referenced
independently, so it needs to be addressable on the wire.

**Value object with one leaf → a scalar field.** `Title` is a term in the
glossary and a `string title = 2;` in the schema. Promoting every leaf term to
its own single-field message produces a schema nobody can read and buys nothing:
the glossary records *language*, the proto records *structure*.

**Value object with several leaves → its own message, embedded by value.**
`Author` with `Name` and `Surname` earns a message because it groups fields that
travel together. It stays embedded rather than referenced, because it has no
identity.

**Aggregate root → the outer message.** Its parts are embedded fields. The test
is the glossary's own: if the part has no independent life, it goes inside.

The judgement call you will hit repeatedly is a term at `0..1` with no
attributes drawn — `Publisher` in the worked example. Value object (just a name
on a book) or entity (a company with its own catalogue)? **Default to the value
object**, because it keeps the schema smaller and the change from value object
to referenced entity is a normal additive migration, while the reverse is not.
Then leave a `TODO(glossary)`.

---

## 3. Cardinality and presence

This is where most of the glossary's content lives and where proto3 is weakest.

**`1` — mandatory.** proto3 removed `required`; there is no way to say it. Write
a plain field and a comment:

```proto
// The book's main title.
//
// Glossary: `1` — mandatory.
string title = 2;
```

The rule then belongs in the **Rules Not Expressible** list. This is not
pedantry: a consumer reading only the `.proto` sees a field that may be an empty
string, and will handle it that way.

**`0..1` — optional.** Use the `optional` keyword. It gives explicit field
presence, so an absent subtitle is distinguishable from an empty one. Without
it, `""` and "not set" are the same value on the wire, which silently destroys
the distinction the glossary drew.

**`0..*` / `1..*` — repeated.** Never mark a `repeated` field `optional`; an
empty list already means absence, and the combination is illegal.

**Bounded repeats (`1..10`, `0..25`) — `repeated` plus the bound in a comment.**
Both ends are lost. Carry them, and challenge them: a suspiciously round maximum
is often a screen limit that got promoted to a business rule, and baking an
arbitrary cap into a wire contract is expensive to undo.

**Unstated inverses.** The glossary usually gives cardinality on one end only,
and the missing end is where many-to-many hides. See §5.

---

## 4. Relationships: composition, association, and the aggregate rule

The single most consequential rule in this skill:

> **Inside an aggregate, embed. Across aggregates or contexts, reference by
> identifier — and do not import.**

```proto
// Inside the aggregate: Book owns its authors.
repeated Author authors = 4;

// Across aggregates: CatalogEntry references a Book it does not own.
string book_isbn = 2;
```

Embedding across an aggregate boundary looks convenient and causes real damage:
it makes the two things impossible to load, version, or authorise separately,
and it duplicates the referenced data on every message that mentions it. Across
a *bounded context* boundary it is worse still, because the `import` couples two
independently deployable models and makes one context's version bump the other's
problem.

Naming: an identifier reference is named for the target plus its identifier —
`book_isbn`, `catalog_entry_id`, `publisher_id`. Not `book`, which implies the
message.

**Unlabeled edges** in the glossary usually mean `has`, which usually means
composition. Confirm rather than assume, and if you cannot, embed and leave a
`TODO(glossary)`.

---

## 5. Many-to-many

The glossary rarely draws these; they appear as the *unstated inverse*. A book
has `1..10` authors, and the picture is silent on how many books an author has —
which is obviously many.

Protobuf has no join table and no relational integrity, so a many-to-many
becomes a `repeated` identifier field on **one** side, chosen by which side owns
the relationship in the domain:

```proto
// On Book, if authorship is a fact about the book:
repeated string author_ids = 4;
```

Put it on the aggregate whose invariants the relationship participates in. If
both sides need to traverse it, that is a read-model concern, not a schema one —
don't put the list on both sides, because nothing keeps two copies in agreement.

Flag every unstated inverse in the open questions, whichever way you model it.

---

## 6. Generalisation — the case protobuf cannot express

**Protobuf has no inheritance.** An `is-a` edge, a hollow-triangle arrowhead, or
a term drawn as a kind of another term has no direct translation. There are
three honest options:

**a. Separate messages, duplicated fields.** Best when the subtypes are used in
different places and rarely handled together. Simple, explicit, and the shared
fields drift — which is acceptable when they were never really shared.

**b. A `oneof` in a wrapper message.** Best when a consumer receives "one of
these" and must dispatch:

```proto
message CatalogItem {
  oneof item {
    Book book = 1;
    Periodical periodical = 2;
  }
}
```

**c. Composition — a shared "base" message embedded as a field.** Best when the
common part is genuinely a coherent thing:

```proto
message Book {
  PublishedWork work = 1;  // the shared part
  string isbn = 2;         // the Book-specific part
}
```

Do **not** simulate inheritance with a `type` enum plus a union of every
subtype's fields all optional. It compiles, and it pushes the entire type
discipline into every consumer.

If the glossary's generalisation edge is itself ambiguous — a hollow triangle
labeled with something that is not an `is-a`, like `enhances` or `describes` —
that ambiguity is a finding, not a modelling choice. Take the reading that fits
the rest of the picture, say plainly which reading you took, and put it at the
top of the open questions. The worked example in
`assets/example-book-catalog/library/catalog/v1/catalog_entry.proto` shows how
to comment exactly this case.

---

## 7. Groups and bounded contexts

Labeled boxes, ellipses, or lanes in the glossary are bounded contexts, and each
becomes a package with a matching directory:

```
library/bibliographic/v1/book.proto      → package library.bibliographic.v1;
library/catalog/v1/catalog_entry.proto   → package library.catalog.v1;
```

A term appearing in two contexts is usually the *same real thing modelled twice
on purpose*, not duplication. Model it twice, once per package, with only the
fields that context needs — that is the point of the boundary. Note it in the
open questions so the intent is confirmed.

If the glossary shows **no groups**, use one package and say so. Don't invent
boundaries the picture doesn't assert; a wrong context boundary is far more
expensive than a missing one.

---

## 8. Identity

Every entity needs an identifier, and it is always **field 1**.

- If the glossary names one (`ISBN`, `Catalog Entry ID`), use it, keeping the
  domain term: `string isbn = 1;`, not `string id = 1;`.
- A natural key like an ISBN is a `string`, not a number, even when it looks
  numeric — leading zeros and check digits matter, and nobody does arithmetic
  on it.
- If the glossary names **none**, add one, name it `<entity>_id`, and mark it:

```proto
// TODO(glossary): the glossary names no identifier for this term. Confirm
// what identifies it in the business, or whether a surrogate ID is acceptable.
string author_id = 1;
```

Never let a missing identity pass silently — it is one of the highest-value
findings a glossary produces, and inventing `id = 1` without comment buries it.

Uniqueness is not expressible in protobuf. It goes on the Rules Not Expressible
list every time.

---

## 9. Closed sets and enums

A term with a fixed set of allowed values becomes an `enum`. Two rules exist for
reasons worth knowing:

**`_UNSPECIFIED = 0` is mandatory.** proto3 enums default to zero, so without an
explicit "not set" value, an unset field is indistinguishable from whichever
real state happens to be first — and that state gets silently asserted.

**Values are prefixed with the enum name** (`TAG_KIND_SUBJECT`, not `SUBJECT`).
Enum values share the *enclosing* scope in C++, so two unprefixed enums in one
package collide at compile time.

```proto
enum LoanStatus {
  LOAN_STATUS_UNSPECIFIED = 0;
  LOAN_STATUS_ACTIVE = 1;
  LOAN_STATUS_RETURNED = 2;
}
```

Only build an enum where the glossary actually asserts a closed set. A `string`
that the glossary never constrained should stay a `string` — an enum invents a
business rule, and adding values to a released enum is a compatibility event.

---

## 10. Pictograms and other notation

Pictogram distinctions in the glossary are usually the entity/value-object split
the brief already made — a briefcase on the one term with its own identity and
lifecycle, eyes on the descriptive ones. They carry into the proto as the
message/field distinction and nothing more.

If the brief's pictogram legend was an unconfirmed hypothesis, note it in the
open questions. Don't encode an unconfirmed legend as structure.

---

## 11. What to do with the glossary's open questions

Every unresolved question from the Glossary Brief lands in **two** places:

1. A `TODO(glossary):` comment on the field or message it affects, phrased so a
   domain expert can answer it in one sentence.
2. The summary you give the user when you present the files.

Keeping them in the file matters more than it sounds. A brief in a chat window
is gone next week; a comment on the field travels with the schema to whoever
next has to change it, which is exactly the person who needs to know that the
25-tag limit was never confirmed.

---

## 12. Opt-in: enforcing rules with protovalidate

If the user wants the constraints **enforced** rather than documented,
`protovalidate` expresses most of them as field options that generated code and
runtime validators honour:

```proto
import "buf/validate/validate.proto";

message Book {
  string isbn = 1 [(buf.validate.field).string.min_len = 1];

  repeated Author authors = 4 [
    (buf.validate.field).repeated.min_items = 1,
    (buf.validate.field).repeated.max_items = 10
  ];
}
```

This covers mandatory fields, bounded repeats, string formats, and numeric
ranges — a large share of what the glossary asserts and proto3 cannot.

It is **not the default**, for two reasons: it adds a module dependency
(`buf.build/bufbuild/protovalidate`) that has to be resolvable at build time,
and it only binds consumers who actually run the validator. Offer it; don't
assume it. When it is used, keep the glossary comments as well — the option says
*what*, the comment says *why*.

---

## 13. The Rules Not Expressible list

Always produce it. It is the deliverable that stops the translation from being
lossy in silence. It collects, at minimum:

- every `1` in the glossary (proto3 has no `required`);
- every bounded repeat, with its bounds;
- uniqueness of every identifier;
- referential integrity of every cross-aggregate ID reference;
- every generalisation edge and the reading you took;
- any cross-field or conditional rule the glossary implies.

Present it as prose the user can hand to whoever writes the validation layer,
and tell them whether `protovalidate` would cover each item.