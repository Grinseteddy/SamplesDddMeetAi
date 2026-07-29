---
name: visual-glossary-interpreter
description: Interpret a Visual Glossary — the static domain-model artifact that pins down a domain's nouns and their relationships, and that pairs with Domain Storytelling, EventStorming and Event Modeling alike — turning it into a Glossary Brief with an exact term catalogue, a relationship-and-cardinality table, a derived domain model (entities, value objects, aggregates, identity), and the open questions the picture leaves unanswered. Use whenever someone shares or describes a visual glossary, domain concept map, term or noun diagram, or any picture of concept boxes or stickies joined by verb-labeled arrows carrying multiplicities like 1, 0..1, 1..*, 1..10 or 0..25, and asks to interpret, read, explain, transcribe, review, or build a data model, schema, ERD, aggregate design, ubiquitous language or glossary document from it. Trigger even when nobody says "visual glossary", and whenever such a picture accompanies a domain story, an EventStorming board or an Event Modeling timeline.
---

# Visual Glossary Interpreter

A **Visual Glossary** is the **static domain model** of a domain — the agreed nouns and how they relate. It is the counterpart to whichever *dynamic* artifact a team is using, and it plays the same role for all of them:

| Dynamic artifact | What it captures | What the glossary supplies |
|---|---|---|
| **Domain Story** | one concrete scenario, actors doing activities in sequence | the work objects the activities act on |
| **EventStorming board** | events, commands, policies, aggregates on a timeline | the nouns those events and commands are *about*, and their structure |
| **Event Model** | slices of command → event → read model along a timeline | the fields and their cardinalities that flow through the slices |

All three are excellent at verbs and weak at nouns. They tell you that an Order was shipped without ever pinning down what an Order *is*, what it must contain, or how many Line Items it may have. The glossary is where that gets settled — which makes it the **terminology authority**: whatever it calls a thing is what everyone should call that thing, in every other artifact.

Do not assume a glossary belongs to a domain story. Ask which dynamic artifact it accompanies, or work with none — a glossary is legitimate standing alone.

Your job is **interpretation, not redrawing and not redesigning**. Read the picture faithfully, transcribe it so the reading is checkable, derive the structure a builder or a modeller needs, and be honest about what the picture does not say. A glossary is small and deceptively simple, which is exactly why a sloppy reading slips through unnoticed.

## Core mental model

Three things are worth holding onto before you start reading:

1. **A glossary is vocabulary plus structure, not behaviour.** There are no steps, no order, no conditionals, no events. If you find yourself narrating a process or a timeline, you have drifted into the territory of whatever dynamic artifact sits beside this one.
2. **Every sticky is a first-class term, even the tiny ones.** `Title`, `Surname`, `Tag Name` are on the board because the business says those words. That does *not* mean each becomes its own table or class — the glossary records language; the domain model you derive from it is an interpretation, and you should keep the two visibly separate.
3. **The picture is usually half a specification.** Cardinalities are typically given on one end only, edges sometimes carry no verb, and pictograms carry meaning nobody wrote down. These gaps are findings, not things to quietly fill in.

## Workflow

Work through these in order. Do not derive a data model before you have transcribed the picture.

### Step 0 — Establish what this glossary accompanies

Ask (or note from context) whether there's a domain story, an EventStorming board, an Event Model, or nothing beside it. It costs one question and it changes the whole downstream job — with a board in hand your aggregates are checkable rather than hypothetical, and your open questions can be phrased in the vocabulary the team already uses. If nothing accompanies it, proceed and say so.

### Step 1 — Transcribe every element (and confirm)

If the input is an image, **read it into text first**. A misread edge poisons everything downstream, and it is cheap to fix here.

Produce two lists:

**Terms** — every sticky/box, with its exact spelling, capitalisation, and any pictogram or icon it carries:

```
Book (eye), ISBN (eye), Catalog entry (briefcase), ...
```

**Relationships** — one line each, always read *source → target*, in the form:

```
<Source> —<label>→ <cardinality> <Target>
```

Follow the edge-routing rules in `references/notation.md` — especially the fan-out rule: when one line leaves a term and splits into several branches, that is **several separate relationships**, one per branch, each with its own label and cardinality. Reading a fan-out as a single relationship to several targets is the most common transcription error with this notation.

Record faithfully what is *missing* too: an edge with no verb label, an end with no multiplicity, an arrowhead of a different shape. Write `(unlabeled)` and `(no cardinality)` rather than inventing plausible values.

For anything non-trivial or hard to read, show the transcription and ask the user to confirm before continuing.

### Step 2 — Build the term catalogue

For each term, capture: the exact name, its pictogram, and a one-line gloss of what it appears to mean *in this domain* based on how it is connected. Where the glossary gives no definition, say so — an undefined term is a finding, not a licence to write a dictionary entry from general knowledge.

Flag terminology smells as you go, without fixing them unilaterally:

- **Plural sticky names** (`Authors`, `Tags`) where the cardinality already carries the plurality. The term is probably singular; ask.
- **Two spellings or two words for one concept.** Read typos charitably; treat genuine synonyms as an open question.
- **Names that are technical rather than domain language** (`ID`, `Flag`, `Data`, `Record`) — flag, don't rename.
- **Terms with no edges at all** — orphans. Either the picture is unfinished or the term is dead vocabulary.

### Step 3 — Read the pictograms

Different icons on the stickies are a deliberate distinction the author made. Same icon = same kind of thing; a lone different icon is nearly always significant.

Derive the legend from *usage* — what do all the eye-marked terms have in common, and how does the briefcase-marked one differ? — then state your reading as a hypothesis and ask for confirmation. Common readings are in `references/notation.md`. Never assert a legend the picture does not provide.

### Step 4 — Classify: entities, value objects, aggregates

Now move from vocabulary to model, keeping the move explicit and reversible.

- **Value object** — a leaf term with no identity of its own, meaningful only as part of its parent, typically at the end of a `1` or `0..1` edge (`Title`, `Subtitle`, `Abstract`, `Surname`). Usually an *attribute* of its parent in an implementation, not a table.
- **Entity** — a term with its own identity that is referenced independently and persists (`Book`, `Publisher`, `Author`).
- **Identity** — look for the term that *is* the identifier (`ISBN`, `Catalog Entry ID`). Every entity needs one; an entity with none is an open question.
- **Aggregate root** — a term that owns a cluster of others through composition-flavoured edges (`has`), where the parts have no independent life.

If an **EventStorming board** or **Event Model** is available, do not guess the aggregates — check your clusters against the aggregates already named there, and report any disagreement as a finding rather than quietly adopting either side. See `references/companion-techniques.md`.

Say plainly which calls were judgement calls. `Publisher` at `0..1` could be a value object (just a name on a book) or an entity in its own right (a company with its own catalogue) — the glossary alone cannot settle it, so ask.

### Step 5 — Interrogate the cardinalities

Every multiplicity is a business rule someone asserted. Walk each one and record:

- **The unstated inverse.** The picture says a Book has `1..10` Authors; it does not say how many Books an Author has. Ask for every edge — this is the single largest gap in most glossaries.
- **Suspiciously precise bounds.** `1..10`, `0..25`. Are these real business rules, remembered UI limits, or arbitrary technical caps? A hard maximum that turns out to be arbitrary is an expensive constraint to bake in.
- **Optionality that implies a case.** Every `0..1` and `0..*` asserts that a real instance exists without it. Name that case: a book with no publisher (self-published?), with no abstract (not yet written?).
- **Mandatory ends** (`1`) that forbid partial creation — can the thing exist before that part is known?

### Step 6 — Read the special relationships

Most edges are plain associations. Watch for the ones that are not:

- **A different arrowhead** (a hollow triangle, a diamond) is a different *kind* of relationship — generalisation, aggregation, composition — carried by shape rather than by the label.
- **Shape and label disagreeing** is a first-order finding. An `is-a`-shaped arrow labeled with something that is not an is-a (`enhances`, `extends`, `describes`, `annotates`) is genuinely ambiguous: subtype, decoration, or a separate concept that merely points at the other. The answer changes the model completely, so surface it prominently rather than picking one.
- **Unlabeled edges** default to nothing. `has` is the usual intent; confirm it.

### Step 7 — Groups and bounded contexts

If the glossary clusters terms into labeled boxes, ellipses, or lanes, those are **subdomains / bounded contexts**, and each becomes a module boundary. Note terms appearing in two contexts — in DDD that is often the *same* real thing modelled twice on purpose, not duplication, but it is worth one confirming question.

If there are no groups, say so explicitly and offer the natural clustering you can see, marked as a suggestion.

### Step 8 — Surface the open questions

Collect everything you deferred: missing inverse cardinalities, unlabeled edges, undefined terms, ambiguous arrowheads, unconfirmed pictogram legend, plural names, arbitrary-looking bounds, missing identity, absent lifecycle. Keep each one **specific and falsifiable**, phrased so a domain expert can answer it in one sentence:

> *The glossary caps Tags at 25. Is that a rule the business enforces, or a limit that crept in from a screen?*

## Output: the Glossary Brief

Produce these sections, in this order:

```
# Glossary Brief — <glossary name>

## 1. Transcription
Terms (exact spelling + pictogram), then every relationship as
`Source —label→ cardinality Target`. Mark (unlabeled) / (no cardinality).

## 2. Term catalogue
Table: Term · Pictogram · Gloss · Notes/smells.

## 3. Relationship table
Table: Source · Label · Target · Cardinality (given) · Inverse (unstated) · Kind.

## 4. Derived domain model
Entities (with identity), value objects, aggregates and their roots.
State clearly that this is derived, and mark judgement calls.

## 5. Business rules asserted by the picture
Each cardinality restated as a sentence a domain expert can confirm or deny.

## 6. Bounded contexts / grouping
Groups as modules, or an explicit "none shown" plus a suggested clustering.

## 7. Open questions
Specific, answerable, prioritised — biggest ambiguity first.
```

Adapt depth to the ask. If the user only wants the data model, lead with sections 3–4 but still show section 1, because the transcription is what makes your interpretation checkable. If they want a written glossary document, sections 1–2 carry it.

## Working with other artifacts

**Establish the pairing early.** Ask which dynamic artifact — if any — this glossary sits beside, because it changes what the brief is *for* and which cross-checks are worth running. `references/companion-techniques.md` has the specifics for each; the headlines:

- **Domain Story** — the glossary supplies the work objects; the story supplies the verbs. Cross-checking a story against a glossary is `domain-story-critic`'s job (see its `references/visual-glossary-cross-check.md`); feed it this brief.
- **EventStorming board** — the glossary supplies the nouns behind the events and commands, and its cardinalities become **candidate aggregate invariants**. Your aggregate hypothesis and the board's yellow aggregates must agree. Red hotspots are very often unresolved glossary questions; check them against your open questions. Pair with `event-storming-interpreter`.
- **Event Model** — the glossary is the **shared field vocabulary** the slices flow. It's what makes information-completeness checkable — every field on a read model has to be traceable back to a field on some earlier event, and it can only be traced if both call it the same thing.
- **None** — perfectly legitimate. Say so, and note that the glossary asserts structure no behaviour has yet exercised.

**In every direction, the glossary wins on vocabulary and loses on behaviour.** When the board says `Bike` and the glossary says `Bicycle`, the board should move. When someone wants a flow, a lifecycle, or a screen, the glossary cannot supply it — go to the dynamic artifact rather than inventing.

**If the user wants the glossary challenged** rather than read, this brief is the input to a Devil's Advocate pass, not a substitute for one.

## References

- `references/notation.md` — the element legend and the reading rules: pictograms, edge routing and fan-outs, cardinality placement, arrowhead shapes, and what to do when the picture is ambiguous. Read it whenever an element is unclear or the user asks about the notation itself.
- `references/companion-techniques.md` — how the glossary pairs with a Domain Story, an EventStorming board, an Event Model, or nothing at all, and which cross-checks pay off in each case (aggregate-boundary agreement, cardinalities as invariants, hotspots as glossary questions, field traceability). Read it as soon as you know which dynamic artifact is in play — and also when the ask runs backwards, i.e. deriving a glossary *from* a board.

- `references/worked-example.md` — a library book-catalog glossary transcribed and worked into a full Glossary Brief, including the `enhances`-with-a-generalisation-arrowhead ambiguity. Read it to pattern-match on a real interpretation.