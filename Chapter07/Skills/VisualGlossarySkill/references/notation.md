# Visual Glossary notation — legend and reading rules

Read this when an element of the diagram is ambiguous, when the user asks about
the notation itself, or before transcribing a glossary you have not seen the
style of before.

The notation is small and mostly self-evident, which is why it is easy to
misread with confidence. The rules below are the places people actually go
wrong.

## Contents

1. Elements
2. Pictograms
3. Reading a relationship
4. Edge routing — fan-outs, junctions, and shared trunks
5. Cardinalities
6. Arrowhead shapes
7. Groups
8. What the notation deliberately does not express
9. When the picture is ambiguous

---

## 1. Elements

| Element | Looks like | Means |
|---|---|---|
| **Term** | A sticky note / box with a noun, usually with a small icon in a corner | A concept in the domain's agreed language |
| **Relationship** | A line ending in a solid filled arrowhead | A directed association from source term to target term |
| **Label** | A verb phrase on the line (`has`, `is written by`, `is published by`, `stores`, `located at`) | How to read the relationship as a sentence |
| **Cardinality** | `1`, `0..1`, `1..*`, `1..10`, `0..25`, placed just before the arrowhead | How many target instances per one source instance |
| **Special relationship** | A line with a *different* arrowhead — hollow triangle, diamond | A kind of relationship the notation encodes by shape: generalisation, aggregation, composition |
| **Group** | A labeled box, ellipse, or lane around several terms | A subdomain / bounded context |

Terms are drawn as stickies because the glossary comes out of a workshop. The
sticky's text is the **agreed term** — transcribe its spelling and casing
exactly, including oddities like `Catalog entry` (sentence case) sitting beside
`Catalog Entry ID` (title case). Those inconsistencies are worth noting; they
are usually accidental, but noting them costs nothing and occasionally reveals
that two different people wrote two different concepts.

## 2. Pictograms

The icons typically come from the Domain Storytelling pictogram set, which is
widely reused for glossaries regardless of which technique the glossary serves
(egon.io, Miro templates and similar tools offer a palette: eye, briefcase, document, folder, person, gear, and so
on). **The notation does not fix their meanings.** Each board's author picks
icons to encode a distinction that matters to them, and the legend usually
lives in the workshop facilitator's head rather than on the board.

So: derive the legend from usage, state it as a hypothesis, ask for
confirmation. Practical procedure:

1. Group the terms by icon.
2. Ask what the members of each group have in common that the others lack.
3. Pay special attention to a **singleton icon** — one term marked differently
   from everything else is almost always deliberate.

Readings that commonly turn out to be right:

- **A single icon used everywhere** — decorative, no semantics. Say so and move on.
- **One icon for informational/descriptive terms, another for the "thing the
  business handles"** — e.g. an eye on data-like concepts (`Title`, `ISBN`,
  `Abstract`) and a briefcase on a business object with its own life
  (`Catalog entry`). This maps loosely onto value object vs. entity/aggregate.
- **A person icon** — an actor, carried over from the domain stories or from
  the small yellow actor stickies of an EventStorming board.
- **A gear / screen icon** — an external system or a channel rather than a
  domain concept.

Never assert one of these as fact. Write: *"the briefcase appears once, on
`Catalog entry` — I read it as marking a business object with its own identity
and lifecycle, distinct from the eye-marked descriptive terms. Is that right?"*

## 3. Reading a relationship

Read every edge as a sentence, in arrow direction, splicing in the cardinality:

```
<Source> <label> <cardinality> <Target>
```

- `Book —has→ 1 ISBN` reads *"a Book has exactly one ISBN."*
- `Book —is written by→ 1..10 Authors` reads *"a Book is written by one to ten Authors."*

If the sentence does not read as English, you have probably read the direction
backwards or attached the label to the wrong branch of a fan-out. Re-read
before you record it.

**Unlabeled edges.** Some edges carry only a cardinality. The intended verb is
almost always `has`, but transcribe it as `(unlabeled)` and confirm — an
unlabeled edge sometimes hides a more specific relationship the author could not
name, and that difficulty is itself informative.

## 4. Edge routing — fan-outs, junctions, and shared trunks

This is where transcription errors happen, so slow down here.

Tools route these diagrams with **orthogonal lines that share segments**. One
line leaves a term, runs across the board, and splits into several branches,
each ending at a different target with its own label and cardinality.

**The rule: a fan-out is N separate relationships, one per branch — not one
relationship to N targets.** A trunk leaving `Book` that splits six ways is six
statements about `Book`, each independently labeled.

Practical procedure for a fan-out:

1. Find the **source** — follow the trunk back to the term it leaves.
2. Find every **arrowhead** the trunk eventually reaches. Count them; that is
   your number of relationships.
3. For each arrowhead, walk **backwards** from it to the split point, picking up
   the label and cardinality that sit on *that* branch.
4. Sanity-check by reading each resulting sentence aloud.

Two traps:

- **A junction dot** on the trunk marks where branches diverge. It is routing,
  not semantics — it does not mean "and" or "xor".
- **Labels drift toward the trunk.** A label sitting near the split may belong
  to either branch. Match labels to branches by *vertical alignment with the
  arrowhead*, not by proximity to the trunk. When two labels are stacked near
  one split, the upper label belongs to the upper branch.

**Chains.** A target of one relationship is frequently the source of the next
(`Book → Authors → Surname`). Record them as separate edges; the chain is the
composition structure and matters when you work out aggregate boundaries.

## 5. Cardinalities

**Placement.** The multiplicity sits at the **arrowhead end**, and constrains
the *target*: `Book —has→ 0..1 Subtitle` means one book has zero or one
subtitle.

**The missing end.** This notation usually gives only the target multiplicity.
The inverse — how many Books per Author — is simply absent. Do not assume `1`,
do not assume `*`. Ask, on every edge. In practice this is the highest-yield
question you can put to a glossary's authors, because the unstated end is where
many-to-many relationships hide.

**Notation variants** you may encounter: `1`, `0..1`, `1..*`, `0..*`, `*`,
`n`, `1..n`, and hard numeric bounds like `1..10`, `0..25`.

**Hard numeric bounds deserve a question every time.** They are asserted
business rules, and they are frequently not business rules at all — they are
remembered form limits, an old database column, or a number someone said in the
workshop and nobody challenged. Ask what happens at the boundary: what does the
business do with the eleventh author?

**Optionality names a case.** Every `0..` asserts that a real instance exists
without the part. Make that instance concrete — a book with no publisher is a
self-published book, and the business probably has a word for it.

## 6. Arrowhead shapes

| Shape | Conventional meaning | How to treat it |
|---|---|---|
| Solid filled arrowhead | Plain directed association | The default; read the label |
| Hollow triangle | Generalisation / specialisation ("is a") | A subtype claim — check it against the label |
| Hollow or filled diamond | Aggregation / composition ("part of") | Ownership; strong signal for aggregate boundaries |
| No arrowhead | Undirected association | Direction is unstated; ask which way it reads |

**When shape and label disagree, that is a first-order finding.** A hollow
triangle carries an is-a claim; a label like `enhances`, `extends`,
`describes`, or `annotates` does not. At least three readings are then live:

1. **Subtype** — the source really is a kind of the target, and the label is a
   loose choice of word.
2. **Decoration / extension** — the source wraps or adds to the target without
   being one. In DDD this is usually a separate entity holding a reference.
3. **Separate concept in another context** — the two are the same real-world
   thing modelled in two bounded contexts, related by reference.

These produce completely different models — inheritance, composition, or a
context map. Do not pick one silently. Present the readings, say which you find
most likely and why, and ask.

## 7. Groups

Labeled boxes, ellipses, or lanes cluster terms into **subdomains / bounded
contexts**, and they usually mirror the groups used in the accompanying dynamic
artifact — a domain story's lanes, an EventStorming board's context clusters.
Each group becomes a module boundary.

A term appearing inside **two** groups is not automatically an error. In DDD the
same real-world thing routinely exists in several bounded contexts with
different models and different meanings. Ask whether it is deliberate rather
than reporting duplication.

If there are no groups, say so — and offer a clustering based on the
composition structure you found, clearly marked as a suggestion.

## 8. What the notation deliberately does not express

Knowing the notation's blind spots keeps you from reading things into it:

- **No behaviour, no order, no conditionals, no events.** That is what the
  dynamic artifact beside it is for — a domain story, an EventStorming board, or
  an Event Model timeline.
- **No lifecycle or states.** A glossary term has no status field until a story,
  an event board, or an event model gives it one. Resist inferring a lifecycle
  from optional relationships.
- **No attributes beyond what is drawn.** Every attribute the business cares
  about is supposed to be a sticky. Absence means "not discussed", not "does
  not exist".
- **No data types, no nullability beyond cardinality, no keys.** Any of these
  in your output is inference — label it as such.
- **No implementation.** Tables, classes, and endpoints are a later choice.

## 9. When the picture is ambiguous

Priority order, and it matters:

1. **Ask the user** — they were probably in the room. One question beats any
   amount of inference.
2. **Read charitably.** Typos are typos. Infer the intended term rather than
   critiquing a misspelling as a concept.
3. **If you must proceed without an answer, state the assumption inline** where
   it is used *and* repeat it in the open questions. An assumption buried in
   prose is one that ships.
4. **Never silently normalise.** Don't singularise `Authors`, don't rename
   `Catalog entry` to `CatalogEntry`, don't fill in a missing cardinality.
   Propose the change and let the domain experts move the glossary.