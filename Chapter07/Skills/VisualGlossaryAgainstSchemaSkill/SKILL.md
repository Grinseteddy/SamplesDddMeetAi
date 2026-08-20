---
name: schema-glossary-consistency
description: >-
  Check an API schema — OpenAPI / JSON Schema, also protobuf or SQL DDL —
  against a Visual Glossary and report where the implementation and the agreed
  domain language disagree: contradicted cardinalities, renamed or unmodelled
  terms, fields no term covers, is-a edges that did or didn't become allOf,
  positional references where the glossary has an entity, and data duplicated
  across linked types. Tolerant of legitimate business abstraction — it
  publishes what it chose NOT to report — and parses the schema first, since
  many findings are the schema disagreeing with itself (broken $refs,
  `required:` parsed as null, bad examples). Use whenever someone gives a
  glossary, concept map, or term diagram together with a schema, spec, YAML,
  .proto, or data model and asks to check, validate, compare, diff, reconcile,
  or align them — including short asks like "does my API match the domain
  model", and re-checks of a revised file ("check again"), where it reports
  fixed / new / still-open rather than starting over.
compatibility: >-
  Pairs with visual-glossary-interpreter for reading the picture; degrades
  gracefully without it. scripts/inspect_schema.py needs Python 3 and PyYAML for
  YAML input (JSON needs nothing).
author: Annegret Junker
---

# Schema ↔ Visual Glossary Consistency

A **Visual Glossary** is what the business agreed the domain is made of: the
nouns, their relationships, their multiplicities. A **schema** is what got built.
They are two statements about one domain, written by overlapping but different
people, weeks apart, in languages with different expressive power — and they
drift the moment either one is edited.

This skill diffs them. The value is not in finding that the schema is "wrong":
most of the time the schema is *newer*, and a disagreement means someone made a
decision after the workshop and never moved the picture. Surfacing that decision
while it is still cheap to record is the whole point.

**This is a conformance check, not a critique of either artifact.** You are not
judging whether the glossary is a good glossary or the schema is a good API. If
the person wants the glossary itself challenged, that is
`visual-glossary-interpreter` followed by a Devil's Advocate pass; offer it,
don't do it here.

## Core mental model

Five things shape every judgement:

1. **Split authority, and it splits differently than you'd expect.** The glossary
   is authoritative on **words** — when the two disagree on what to call a thing,
   the default is that the schema adopts the glossary's term. The schema is often
   authoritative on **structure**, because implementation forces decisions the
   picture never faced (identity, reference style, which side of a link holds
   what). Neither wins automatically. Report both moves and recommend one.

2. **Abstraction is expected, not a defect.** A glossary is drawn at business
   resolution. It has no identifiers, no formats, no string lengths, no
   pagination, no envelope — and it names things the API legitimately doesn't
   own. A checker that reports every such gap produces a hundred findings, gets
   read once, and is never run again. **Maintain an explicit allowlist and
   publish it in the report.** Showing what you deliberately did not report is
   what makes the findings you *did* report credible. `references/checks.md` has
   the default allowlist.

3. **Parse, don't read.** Run the schema through a parser before comparing
   anything. In practice a large share of the findings in a first check are the
   schema disagreeing with *itself* — a `$ref` missing a slash so three types
   inherit nothing, a `required:` that parsed as null, an example that fails its
   own pattern, a `format` that doesn't pair with its `type`. These are invisible
   to the eye and obvious to `scripts/inspect_schema.py`. They are also the
   findings that make people trust the rest of the report, because they are
   checkable in ten seconds.

4. **Expressiveness differs in both directions.** The glossary can say things the
   schema cannot (an inverse multiplicity, a relationship with no owning side, an
   exclusive choice between two targets). The schema can say things the glossary
   cannot (optionality of a *field* versus a *relationship*, discriminators,
   formats). A mismatch is only a finding when one artifact could have expressed
   the other's claim and didn't. `references/checks.md` marks which is which.

5. **Findings, not verdicts.** Every disagreement is evidence that two groups
   understood something differently. Phrase each one so a domain expert can
   settle it in a sentence.

## Preconditions

You need **a glossary and at least one schema**. If either is missing, say so and
offer the alternative — derive a candidate glossary from the schema, or check the
schema on its own with the mechanical pass, which is useful standalone.

Ask two things if they aren't obvious, then proceed:

- **Is the schema shipped?** A design draft can be moved freely; a schema with
  stored data behind it makes "the schema moves" an expensive recommendation, and
  changes which resolution direction you should recommend.
- **Which is newer?** It flips the prior on nearly every finding. A schema newer
  than the picture usually means the picture is stale, not that the code is wrong.

## Workflow

### Step 1 — Read the glossary into text, and confirm

Do not diff a picture. Transcribe first: a misread edge invents disagreements
that were never there.

Use `visual-glossary-interpreter` (its Steps 1–2 and the section 3 relationship
table) — read `../VisualGlossarySkill/SKILL.md` and follow
it. Mind its **fan-out rule**: one line leaving a term and splitting into
branches is several relationships, not one. That is the most common transcription
error, and here it turns directly into false findings.

Without that skill, transcribe directly: every term with its exact spelling and
its colour/pictogram, then every edge as `Source —label→ cardinality Target`,
marking `(unlabeled)` and `(no cardinality)` rather than inventing values.

Colour or pictogram groups usually mark **bounded contexts** — terms owned by a
neighbouring model. That distinction does more work here than anywhere else,
because it decides whether a missing schema type is a finding or an allowlist
entry. State your reading of the legend and ask.

Show the transcription and ask for confirmation whenever anything was hard to
read. Mark what you couldn't read as **unchecked**, not as absent.

### Step 2 — Parse the schema

```bash
python scripts/inspect_schema.py <file.yaml> --inventory
```

This gives you two things: the mechanical findings (`DEFECT`, see
`references/mechanical-checks.md` for the catalogue and what each one means), and
a field inventory — every type with its fields, shapes, cardinalities and
inheritance — which is what you diff the glossary against in the next step.

Read the raw file too. The script cannot see descriptions, and stale descriptions
are a real finding: a type reused for a second purpose while its `description`
still names the first is how one word ends up meaning two things.

### Step 3 — Build the mapping ledger

One row per **glossary term**. Columns: the schema construct it maps to (type,
field, `$ref`, enum value, or `—`), the mapping kind, and status. Below a
divider, the **schema-only constructs** — every type and field no glossary term
claimed, split into *domain concepts missing from the glossary* and *allowlisted
technical constructs*.

The ledger is the heart of the report: it forces you to visit every term rather
than the ones that caught your eye, and it is what the team will screenshot.

Match using the ladder in `references/checks.md` — exact, case/spacing variant,
plural, charitable typo, role-of, synonym, unmatched. The ladder is what keeps a
`Substitute` that is genuinely an `Ingredient` in another role from becoming a
false finding.

### Step 4 — Run the checks

Work through all ten, in this order, from `references/checks.md` (detection
recipe, guards, severity and typical resolution for each):

| Code | Check |
|---|---|
| `CARD` | A multiplicity the schema contradicts, or drops |
| `TERM` | Same concept, different word |
| `SENSE` | ★ One word, two meanings inside the schema |
| `MISS` | Glossary term or relationship with no schema counterpart |
| `EXTRA` | Schema field no glossary term covers |
| `SUB` | is-a edges vs `allOf` — missing, asymmetric, or undiscriminated |
| `REF` | ★ Reference style: positional, identity, or by-value |
| `DUP` | ★ Data duplicated across two linked types |
| `CTX` | A term owned by another context, redefined or wrongly reused |
| `DEFECT` | Schema-internal defect, glossary-independent (from Step 2) |

★ = the three that are specific to checking against *code* rather than against
another picture, and the three nobody finds by reading. Give them room:

- **`SENSE`** is the highest-value finding in most real checks. `ingredient` as a
  positional digit on one type and as an object on another is not a style
  problem; it means two developers will implement two different things.
- **`REF`** is where bounded-context boundaries actually bite. A glossary entity
  referenced by position (`"001"`, an array index) is a reference that breaks
  when the referenced list is reordered. Ask the stability question, don't just
  flag the pattern: *does that index still mean the same thing after an edit?*
- **`DUP`** — when B links to A with cardinality `1`, anything B copies from A is
  derivable and will drift. It is also sometimes deliberate (a snapshot of a
  value that must not change when A changes). Ask which; don't assume.

### Step 5 — Separate the allowlist from the findings

Before writing anything up, walk your candidate findings against the abstraction
allowlist in `references/checks.md` and move everything that belongs there. Then
**publish the allowlist as its own report section**. Two sentences per class is
enough: *identifiers, formats and lengths have no glossary equivalent by design;
`Address` and `Postcode` belong to the customer context and are referenced, not
modelled.*

This section is not padding. It is the difference between a report someone acts
on and a report someone argues with.

### Step 6 — Severity and resolution direction

Every finding gets both.

- **Blocking** — the artifacts assert different facts, or the schema doesn't
  validate what it claims to. Someone will build or store the wrong thing.
- **Significant** — divergence that defeats the point of having a shared
  language: renamed terms, a concept the schema flattens away, one word two
  meanings.
- **Minor** — cosmetic or coverage: casing, an unexercised relationship, a stale
  description.

Direction is **schema moves**, **glossary moves**, or **ask** — and say which
you'd pick. "The glossary should drop `Meal`, or mark it as a kitchen-world term
outside the model" is useful; "these differ" is not.

### Step 7 — Two patch lists

Close with edits concrete enough to apply without further discussion. One list
for the schema (`HelpIngredients`: drop `recipe` and `ingredient`, both derivable
via `helpRequestIdentifier`), one for the glossary (add `Quantity`, `Unit` under
`Substitute`; mark `Step` as recipe-context). This is what turns a report into
half an hour of work someone can actually do.

## Output: the Conformance Report

```
# Conformance Report — <schema name> × <glossary name>

## 0. Scope checked
Which files, which revision, what you could not read (UNCHECKED),
and the two preconditions: shipped or draft, which artifact is newer.

## 1. Verdict
One line: aligned / aligned with drift / diverged.
Finding counts by severity.

## 2. Mapping ledger
Table: Glossary term · Schema construct · Kind · Status.
Then, below a divider, schema-only constructs, split into
"domain concepts missing from the glossary" and "allowlisted".

## 3. Findings
Ordered by severity, biggest first. Each one:
ID · code · evidence (term + edge, schema path) · why it matters ·
schema moves / glossary moves · recommendation.

## 4. Relationships & cardinalities
Table: Glossary relationship · cardinality · schema realization ·
verdict (consistent / contradicted / not expressible / absent).

## 5. Allowed abstraction
What you deliberately did not report, and why.

## 6. Patch list
Two lists — edits to the schema, edits to the glossary.

## 7. Already aligned
Name what matches. It tells the team what to protect, and it is the
evidence that you checked everything rather than only what looked broken.
```

Adapt depth to the ask. "Does this match?" wants sections 1–3 plus the patches; a
pre-implementation review wants all of it. Always keep section 0, the ledger, and
section 5 — they are what make the report checkable rather than merely believable.

## Re-check mode

Schemas arrive in revisions, and "check again" is the most common second message
this skill sees. **Do not re-run the report from scratch** — the person has read
the last one and wants the delta.

Structure a re-check as:

```
## Fixed          — what closed since the last revision, named by finding ID
## New breakage   — findings this revision introduced
## Unchanged      — one compact paragraph, not a re-listing
```

Three patterns to watch for specifically, because they are what re-checks are
*for*:

- **Migrating defects.** A `required:` that parsed as null gets fixed in one type
  and appears in the type added this round. The bug isn't fixed; it moved. Say so
  — it means the fix was a spot fix and the file needs a lint pass, not another
  patch.
- **Partial fixes that create new mismatches.** A field changed from a positional
  index to an object on the request but left as an index on the response now
  means two things (`SENSE`), where before it merely meant one wrong thing. A
  half-applied fix can score worse than no fix.
- **Decisions made in conversation that the artifacts haven't caught up with.**
  If the person argued a position two messages ago and the new file contradicts
  it, that is the headline of the re-check. They are usually not aware; the file
  was probably written before the argument landed.

Keep the finding IDs stable across revisions so "F3 is still open" means
something.

## Working with the neighbours

- **`visual-glossary-interpreter`** — reads the picture; run it first. If the
  glossary turns out to be the weaker artifact (undefined terms, missing inverse
  cardinalities, ambiguous arrowheads), a proper Glossary Brief is the follow-up.
- **`domain-story-glossary-consistency`** — the same check one artifact over:
  stories against the glossary rather than schema against the glossary. If the
  team has stories too, running both makes the glossary the hub of a three-way
  check.
- **`openapi-spec-author` / `asyncapi-spec-author` / `protobuf-model-author`** —
  when the patch list is long enough that rewriting beats patching, hand over.

## Reference files

- `references/checks.md` — the matching ladder, the ten checks with detection
  recipes and false-positive guards, the glossary→schema construct mapping table,
  the default abstraction allowlist, and the severity rubric. Read it before
  Step 3; it carries the detail this file only names.
- `references/mechanical-checks.md` — what `inspect_schema.py` looks for, what
  each finding code means, why each one is invisible to the eye, and the manual
  checks the script cannot do (stale descriptions, semantic naming, unit
  ambiguity). Read it when running Step 2 or when the script isn't available.
- `references/worked-example.md` — a parcel-delivery glossary checked against an
  OpenAPI components file, transcription through report, with a live example of
  each check code and a second-revision re-check. Read it to pattern-match on a
  real run.
