# Derivation rules — schema construct → glossary construct

Read this before building the model. It is the inverse of the mapping table in
`schema-glossary-consistency`, and it carries the part that table doesn't: what
to *drop*, because a glossary is drawn at business resolution and a schema is
not.

## Contents

1. The construct table
2. Abstracting the technical layer
3. The colour rule — owned vs referenced
4. Naming
5. Cardinality
6. When to stop and ask instead of drawing

---

## 1. The construct table

| Schema construct | Glossary construct |
|---|---|
| Object schema with properties | **Term** (sticky) |
| `allOf: [$ref Base]` + own properties | **Term** with an `is-a` edge to Base |
| `$ref` to an object, required | edge `—label→ 1 Target` |
| `$ref` to an object, optional | edge `—label→ 0..1 Target` |
| `type: array, items: $ref`, `minItems: m`, `maxItems: n` | edge `—label→ m..n Target` |
| array with no `maxItems` | `m..*` |
| Scalar field carrying business content (`question`, `helpDescription`) | **Term** for the content, edge `—label→ 1` |
| Scalar field that is a foreign key (`cook: uuid`) | edge to the **concept**, never a sticky named for the field |
| Scalar field that is an index into another context (`step: "2."`) | cross-context edge `—refers to→ 1 Step` |
| `enum` / `x-extensible-enum` of domain nouns | **one term** with the values as a subtitle |
| `enum` of a status or code | attribute term, values in the subtitle |
| Inline value object (`{name, quantity, unit}`) | **Term** plus one attribute term per field |
| `description` | the term's gloss — prose in your write-up, not a sticky |
| Collection wrapper type (`Pictures` → `Picture`) | nothing; the cardinality already says it |
| `discriminator` | nothing |

### Attributes: how many stickies?

A glossary records the words the business says, so a field the business talks
about earns a sticky — `Question`, `Help Text`, `Quantity`, `Unit`. A field it
never says out loud does not. The test is whether someone in the domain would use
the word in a sentence about the domain, not whether it appears in the payload.

Three or four attribute stickies on a value object is normal. Twenty attribute
stickies on an aggregate means you transcribed instead of deriving — pick the ones
that carry meaning and note the rest in the changelog.

---

## 2. Abstracting the technical layer

This is where most of the judgement lives, and where an unguided derivation goes
wrong most visibly.

**Identifiers disappear.** Three different cases, three different treatments:

| Field | Treatment |
|---|---|
| `helpRequestId` on `HelpRequest` — the type's own identity | **nothing.** Identity is implied by being a term. |
| `helpRequestIdentifier` on `Help` — a reference to another type | **an edge** `Help —for→ 1 Help Request` |
| `cook: uuid` on `HelpRequest` — a reference to an actor | **an edge** `Cook —posts→ 0..* Help Request` (note the direction usually reads better reversed) |

A sticky named `Help Identifier` is the signature failure of this step. If one
appears, the derivation has stopped being a lift and become a transcription.

**Required/optional becomes cardinality, not a second annotation.** `required` +
`$ref` is `1`; absent from `required` is `0..1`. Don't also write "mandatory" on
the sticky.

**These become nothing at all:** `format`, `minLength`, `maxLength`, `pattern`,
`exclusiveMinimum`, `examples`, `additionalProperties`, `discriminator`,
pagination and envelope types, error schemas, `createdAt`/`etag`-style
infrastructure fields.

Exception worth catching: a `pattern` or a bound that encodes a **business rule**
(`maxItems: 3` substitutes, `^\d$` capping something at ten) belongs in the
picture as a cardinality — and belongs in the changelog as a question, because
bounds like that are as often a remembered screen limit as a rule.

**Direction.** A schema can only point one way — the child holds the parent's id.
The glossary is free, so pick the direction that reads as English:
`Cook —posts→ 0..* Help Request`, not `Help Request —has cook→ 1 Cook`. State the
cardinality of the *target* at the arrowhead, as the notation requires.

---

## 3. The colour rule — owned vs referenced

Two groups, and the distinction is **whether this model describes the thing**, not
where it originally came from:

- **Owned colour (primary, e.g. yellow)** — terms whose structure lives in this
  schema. Includes a locally defined value object that mirrors another context's
  concept: if you describe `Ingredient` with name, quantity and unit, it is
  described *here*, whatever the recipe team calls theirs. That local definition
  is an anti-corruption layer and it is usually correct.
- **Referenced colour (secondary, e.g. blue)** — terms that appear only as an
  identifier or an index, with no local structure. The picture names them so the
  relationship is visible, and says nothing about their shape.

A concept can move between the groups across revisions, and that move is one of
the most informative changelog entries you can write: *"`Ingredient` moved from
referenced to described — the schema now carries name, quantity and unit by value
rather than an index into the recipe."*

Put the referenced terms in their own band or region, and label the colours in
the legend. Without a legend the distinction is invisible and the picture implies
a shared type where there is none.

---

## 4. Naming

The schema wins on structure; it does not automatically win on words.

- **Keep the glossary's term** when the schema's name is a technical variant of
  it — casing, a plural, `Catastrophy` vs `Catastrophe` (fix the typo, note it).
- **Adopt the schema's term** when the schema introduces a genuinely new concept,
  or when the team has clearly moved on.
- **Rename to what maps 1:1** when a glossary term and a schema type mean the
  same thing under different names, and the mapping is the point of the exercise.
  `Meal Preparation Catastrophy` → `Catastrophe Help Request` earns its keep
  because a reader can now find the type. Say so in the changelog.
- **Never invent a name for a concept that isn't in either artifact** to fill a
  gap in the picture. Draw the gap.

A term whose name mirrors its schema type is a feature here — the glossary's job
in this direction is partly to make the schema navigable.

---

## 5. Cardinality

Read them off the schema mechanically, then sanity-check two things:

- **Every `0..`** asserts an instance without the part. Name that case in the
  changelog: help with no pictures, an answer with no provider recorded.
- **Every hard bound** (`1..3`, `0..10`) is a business rule someone asserted.
  Carry it into the picture and ask whether it is real.

Cardinalities the schema **cannot** state — the inverse end of every
relationship — are not deleted from the old glossary just because the schema is
silent. Carry them across unchanged.

---

## 6. When to stop and ask instead of drawing

Six situations. In each, drawing quietly produces a confident picture of
something nobody decided:

1. **`required` names a property that doesn't exist.** The schema is mid-refactor.
   Which state is intended?
2. **A `$ref` doesn't resolve.** The subtype inherits nothing, so its real shape
   is unknown.
3. **A subtype adds nothing** to its base. Drawing it produces a term with no
   content of its own — a sticky that says nothing. Ask whether it's still a
   distinct concept.
4. **The schema drops something the glossary marked mandatory.** That is a
   business rule disappearing; it deserves a sentence, not a silent deletion.
5. **Two schema types collapse to one glossary term, or vice versa.** Ask which
   the team says out loud.
6. **A bound appeared with no discussion.** Draw it, flag it.

In all six: state what you'd draw under each reading, ask once, and proceed with
the most likely one if there's no answer — with the assumption written into the
changelog, not buried in the picture.