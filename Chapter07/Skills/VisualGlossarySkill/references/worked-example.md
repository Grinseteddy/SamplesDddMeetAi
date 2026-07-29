# Worked example — a library book-catalog glossary

A complete pass over one real glossary, from picture to Glossary Brief. Read it
to pattern-match: the fan-out transcription, the pictogram hypothesis, the
value-object/entity split, and above all the `enhances` arrowhead ambiguity,
which is the kind of finding this skill exists to catch.

## The picture

Thirteen stickies, each carrying a small icon. Twelve carry an **eye**; one —
`Catalog entry` — carries a **briefcase**. A single line leaves `Book` on the
left, runs right, and fans out into six labeled branches. `Authors` fans out
again into two. `Catalog entry` sits lower left with its own two branches, and
a line runs from it *up* to `Book`, ending in a **hollow triangle**, labeled
`enhances`.

---

## 1. Transcription

**Terms** (exact spelling, pictogram):

`Book` (eye) · `ISBN` (eye) · `Title` (eye) · `Subtitle` (eye) ·
`Authors` (eye) · `Name` (eye) · `Surname` (eye) · `Publisher` (eye) ·
`Abstract` (eye) · `Catalog entry` (**briefcase**) · `Catalog Entry ID` (eye) ·
`Tags` (eye) · `Tag Name` (eye)

**Relationships** (`Source —label→ cardinality Target`):

| # | Source | Label | Card. | Target |
|---|---|---|---|---|
| 1 | Book | has | 1 | ISBN |
| 2 | Book | has | 1 | Title |
| 3 | Book | has | 0..1 | Subtitle |
| 4 | Book | is written by | 1..10 | Authors |
| 5 | Book | is published by | 0..1 | Publisher |
| 6 | Book | has | 0..1 | Abstract |
| 7 | Authors | *(unlabeled)* | 1 | Name |
| 8 | Authors | *(unlabeled)* | 1 | Surname |
| 9 | Catalog entry | has | 1 | Catalog Entry ID |
| 10 | Catalog entry | has | 0..25 | Tags |
| 11 | Tags | *(unlabeled)* | 1 | Tag Name |
| 12 | Catalog entry | enhances | *(none)* | Book — **hollow triangle arrowhead** |

Note what the transcription preserves: edges 7, 8 and 11 have **no verb**, edge
12 has **no cardinality** and a **non-standard arrowhead**. Those three absences
generate half the findings below. Note also that relationships 1–6 come from a
single shared trunk leaving `Book` — six statements, not one.

---

## 2. Term catalogue

| Term | Icon | Gloss | Notes |
|---|---|---|---|
| Book | eye | The published work being catalogued | The subject of the glossary |
| ISBN | eye | The standard book identifier | Mandatory (`1`) → identity candidate |
| Title | eye | The book's main title | Mandatory |
| Subtitle | eye | Secondary title | Optional |
| Authors | eye | The people who wrote the book | **Plural sticky name** — likely `Author` |
| Name | eye | An author's given name | Leaf |
| Surname | eye | An author's family name | Leaf |
| Publisher | eye | Who published the book | Optional — entity or just a name? |
| Abstract | eye | Summary of the book | Optional |
| Catalog entry | **briefcase** | This library's record about a book | Only briefcase on the board |
| Catalog Entry ID | eye | Identifier of the catalog entry | Identity |
| Tags | eye | Classification labels on the entry | **Plural sticky name** |
| Tag Name | eye | The text of a tag | Leaf |

**Pictogram hypothesis.** Twelve eyes, one briefcase, and the briefcase sits on
the only term that is clearly *this library's own record* rather than a fact
about the book itself. Reading: the eye marks descriptive/informational
concepts, the briefcase marks a business object with its own identity and
lifecycle. Consistent with `Catalog entry` being the only term with a
purpose-built ID. **State as a hypothesis, ask to confirm.**

---

## 3. Relationship table

| Source | Label | Target | Given | Inverse (unstated) | Kind |
|---|---|---|---|---|---|
| Book | has | ISBN | 1 | ? — presumably 1 | composition |
| Book | has | Title | 1 | ? | composition |
| Book | has | Subtitle | 0..1 | ? | composition |
| Book | is written by | Authors | 1..10 | **? — likely many** | association |
| Book | is published by | Publisher | 0..1 | **? — likely many** | association |
| Book | has | Abstract | 0..1 | ? | composition |
| Authors | *(unlabeled)* | Name | 1 | ? | composition |
| Authors | *(unlabeled)* | Surname | 1 | ? | composition |
| Catalog entry | has | Catalog Entry ID | 1 | 1 | identity |
| Catalog entry | has | Tags | 0..25 | **? — likely many** | composition |
| Tags | *(unlabeled)* | Tag Name | 1 | ? | composition |
| Catalog entry | enhances | Book | **none** | ? | **ambiguous — see §7** |

The three inverses in bold are where many-to-many relationships are hiding. An
author writes more than one book; a publisher publishes many; a tag is applied
to many entries. None of that is on the picture.

---

## 4. Derived domain model *(derived — not drawn)*

**Entities**

- **Book** — identity `ISBN`. Attributes: `Title` (required), `Subtitle`
  (optional), `Abstract` (optional). References `Author` (1..10) and
  `Publisher` (0..1).
- **Author** — no identity shown. Attributes: `Name`, `Surname`, both required.
  *Judgement call:* modelled as an entity rather than a value object because
  authors are shared across books and libraries generally want to browse by
  author — but the glossary gives no author identifier, so this needs
  confirming.
- **Catalog entry** — identity `Catalog Entry ID`. Owns `Tags` (0..25).
  References `Book`.
- **Publisher** — *unresolved.* At `0..1` with no attributes drawn, this could
  be a plain name on a book (value object) or a company with its own record
  (entity). The glossary cannot settle it.

**Value objects**

`Title`, `Subtitle`, `Abstract`, `Name`, `Surname`, `Tag Name`, `ISBN`,
`Catalog Entry ID` — leaves with no independent identity. In an implementation
these are fields, not tables.

**Tag** deserves its own note: drawn as a leaf owned by the entry, it looks like
a value object. But tags are normally *shared* vocabulary — the point of tagging
is that many entries carry the same tag — which would make `Tag` an entity with
a controlled name. The picture does not say. Flag it.

**Aggregates**

- **Book** as root, with title/subtitle/abstract/ISBN as parts.
- **Catalog entry** as root, with its ID and tags as parts.

Two roots, deliberately separate: a book is a fact about the world, a catalog
entry is this library's record of it. That separation is the most interesting
thing the picture asserts.

---

## 5. Business rules asserted by the picture

Restate each cardinality so a librarian can confirm or deny it:

1. Every book has exactly one ISBN, and it always has one.
2. Every book has exactly one title.
3. A book may have no subtitle.
4. **A book has at least one and at most ten authors.**
5. A book may have no publisher at all.
6. A book may have no abstract.
7. Every author has exactly one given name and exactly one surname.
8. Every catalog entry has exactly one ID.
9. **A catalog entry carries at most 25 tags, and may carry none.**
10. Every tag has exactly one name.

Rules 4 and 9 are the ones to challenge. Ten authors is plausible for trade
books and immediately false for a multi-author scientific volume; twenty-five
tags looks like a screen limit that got promoted to a rule. Rule 1 forbids
cataloguing anything published before ISBNs existed, or any item without one —
a real constraint for a library. Rule 7 breaks on mononymous authors and on
naming conventions where `Name`/`Surname` does not apply.

---

## 6. Bounded contexts / grouping

**None drawn.** The composition structure nevertheless suggests two clusters,
offered as a suggestion, not a reading:

- *Bibliographic* — Book, ISBN, Title, Subtitle, Authors, Name, Surname,
  Publisher, Abstract. Facts about the published work; shared with the outside
  world.
- *Cataloguing* — Catalog entry, Catalog Entry ID, Tags, Tag Name. This
  library's own records and classification.

The `enhances` edge is precisely the seam between them, which is another reason
to resolve it.

---

## 7. Open questions — biggest first

1. **What does `enhances` mean, given it is drawn with a generalisation
   arrowhead?** A hollow triangle says *"a Catalog entry is a kind of Book."*
   The word `enhances` says something else — that the entry adds to the book
   without being one. Three live readings: (a) Catalog entry is a subtype of
   Book; (b) Catalog entry is a separate record that *references and decorates*
   a Book — supported by its own ID; (c) the two are the same work modelled in
   two contexts. Reading (b) fits the rest of the picture best, but the three
   produce inheritance, composition, and a context map respectively, so this is
   a question, not a call. **Also: this edge carries no cardinality — is it one
   entry per book, or many (one per copy, per branch, per edition)?**
2. **Are the inverse cardinalities as expected?** Can an author write more than
   one book, a publisher publish more than one, a tag apply to more than one
   entry? All three are almost certainly many, and none is on the picture.
3. **Is the 1..10 author limit a real rule?** What happens with the eleventh?
4. **Is the 0..25 tag limit a business rule or a leftover screen limit?**
5. **Should `Authors` and `Tags` be singular?** The cardinality already carries
   plurality; `Author` and `Tag` are the likelier terms.
6. **Is `Tag` shared vocabulary or per-entry text?** Shared tags make `Tag` an
   entity with a controlled name; free text makes it a value object.
7. **Do `Author` and `Publisher` need identities of their own?**
8. **Are edges 7, 8 and 11 all plain `has`?** They carry no verb.
9. **Can the pictogram legend be confirmed** — eye for descriptive concepts,
   briefcase for business objects with identity?
10. **Is ISBN a workable identity** for everything this library catalogues, or
    are there items without one?
11. **Is `Catalog entry` / `Catalog Entry ID` casing** deliberate or accidental?

---

## What this example teaches

- The **fan-out** from `Book` is six relationships, not one. Transcribe branches
  individually or you lose five business rules.
- **Absences are findings.** The unlabeled edges, the missing cardinality on
  `enhances`, and every unstated inverse each generated a question.
- **The singleton pictogram was the clue** that `Catalog entry` is a different
  kind of thing — which turned out to be the same seam the `enhances` edge
  crosses and the same seam the two suggested contexts fall along. Small signals
  in a glossary converge.
- **The highest-value finding was a shape/label disagreement**, not anything
  written down. Nobody had to be wrong for that ambiguity to exist; it just
  needed someone to read the picture carefully enough to notice.