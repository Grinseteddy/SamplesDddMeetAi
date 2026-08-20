---
name: visual-glossary-updater
description: >-
  Redraw a Visual Glossary so it matches a newer schema. Given an older glossary
  picture (photo, SVG, or transcription) and the schema that has since moved
  ahead — OpenAPI, JSON Schema, protobuf, SQL DDL — derive the updated glossary
  and render it as a clean SVG, abstracting technical implementation away:
  identifier fields become relationships rather than stickies, arrays become
  cardinalities, allOf becomes is-a, enums of domain nouns become value terms,
  and referenced-but-not-described concepts get their own bounded-context colour.
  Ships a renderer with a geometry checker, so the drawing is verified rather
  than guessed at. Use whenever someone has both a glossary and a newer schema
  and wants the picture updated, adapted, regenerated, synced, or brought back
  in line — including asks like "make the glossary fit the API", "our diagram is
  stale, redraw it from the spec", or "reverse-engineer a domain picture from
  this schema". For diffing the two without redrawing, use
  schema-glossary-consistency instead.
compatibility: >-
  scripts/render_glossary.py needs Python 3 and PyYAML. Pairs with
  visual-glossary-interpreter (reading the old picture) and
  schema-glossary-consistency (the diff); works without either. Rendering a PNG
  preview needs cairosvg, which is optional.
author: Annegret Junker
---

# Visual Glossary Updater

The glossary was drawn in a workshop. The schema kept moving. Now the picture on
the wall asserts things the code stopped doing months ago, and everyone has
quietly stopped trusting it — which is worse than not having one, because a
stale glossary still gets cited.

This skill regenerates the picture from the schema. The output is an SVG in the
same visual language as the original — sticky-note terms, verb-labeled arrows,
multiplicities at the arrowhead — plus a changelog saying what moved and why.

**The direction of authority is inverted here, and only partly.** In a normal
consistency check the glossary wins on vocabulary. Here the schema is the
artifact that has been maintained, so it wins on *structure*: what exists, what
contains what, how many. It does **not** automatically win on *language*, and it
does not win on things it simply cannot express. Section "What the schema is not
allowed to delete" below is the guard, and it is the part of this skill that
stops the output being a class diagram with rounded corners.

## Core mental model

1. **A glossary is business vocabulary, not a serialization format.** Every
   derivation is a *lift*: `helpRequestIdentifier: uuid` is not a term called
   "Help Request Identifier", it is an arrow labeled `for` pointing at
   `Help Request`. If your output has stickies ending in "Id", you have
   transcribed rather than derived.

2. **Absence from a payload is not a business decision.** A schema cannot express
   an inverse multiplicity, an exclusive choice, or a relationship between two
   concepts it doesn't carry. When the old glossary asserts one of those and the
   schema is silent, the glossary keeps it. Deleting it destroys knowledge that
   was never contradicted.

3. **Draw the schema that exists, not the one that was meant.** If `required`
   names properties that were deleted, or a `$ref` doesn't resolve, you are
   looking at a mid-refactor file. Say so before drawing — a picture of a
   half-applied change is worse than no picture.

4. **Verify the geometry, don't eyeball it.** You cannot see the SVG you just
   wrote. `render_glossary.py --check` reports overlapping stickies, labels
   landing on boxes, edges routed through unrelated terms, and text that won't
   fit. Iterate until it is clean; that report is your eyes.

## Workflow

### Step 1 — Read the old glossary

Transcribe it to text first: terms with exact spelling and colour/pictogram
group, then every edge as `Source —label→ cardinality Target`. Use
`visual-glossary-interpreter` if installed
(`../VisualGlossarySkill/SKILL.md`), minding its fan-out
rule.

**Check the picture you were given is the current one.** An SVG export and a
photo of the same board often differ — a term added on the whiteboard after the
export, a cardinality tightened in one and not the other. If two versions are
supplied, or if what you're given contradicts something the person said earlier,
ask which is authoritative before spending effort on the wrong baseline.

Note the colour groups explicitly. They carry the bounded-context distinction,
and that distinction survives into the new drawing.

### Step 2 — Check the schema is coherent

Parse it. If `schema-glossary-consistency` is installed, its
`../VisualGlossaryAgainstSchemaSkill/scripts/inspect_schema.py` does this in one
call; otherwise load the file and check the essentials yourself: every `$ref`
resolves, every `required` name is a
property that exists (following `allOf`), examples match their own patterns,
subtypes add something.

**Blocking defects stop the drawing.** A `required` that names a deleted property
means the schema is between two states, and you'd be drawing whichever one you
guessed. Report the defects, say what you'd draw under each reading, and ask —
one message, then proceed.

### Step 3 — Diff, and classify every difference

Produce four lists before drawing anything:

- **Remove** — glossary terms and edges the schema contradicts or has dropped by
  decision.
- **Add** — schema constructs with no term.
- **Rename / reshape** — same concept, different word or different structure.
- **Keep despite absence** — the guard list. See below.

`schema-glossary-consistency` produces exactly this if it's installed; feed it
both artifacts and use its patch list as the input to Step 4.

### Step 4 — Derive the new glossary

Apply `references/derivation-rules.md` — the schema-construct → glossary-construct
table, the abstraction rules (which fields become edges and which become
nothing), the colour rule, and the naming rule. That file is the substance of
this skill; read it before building the model.

### Step 5 — Build the model and render

Write a model file (format in `references/rendering.md`), then:

```bash
python scripts/render_glossary.py model.yaml --check     # iterate until clean
python scripts/render_glossary.py model.yaml -o glossary.svg
```

`references/rendering.md` has the standard five-band layout, the routing
overrides for the two or three edges that always need one, and the check loop.
`references/example/help-glossary.yaml` is a complete 24-term model that renders
clean — start from it rather than from an empty file.

If `cairosvg` is available, also emit a PNG; many people can't preview an SVG
where they'll open it.

### Step 6 — Deliver the picture *and* the changelog

The SVG alone is unusable for review — nobody can tell what changed by looking at
two large diagrams. Write the changelog as prose grouped by *removed / changed /
added*, each entry naming the schema construct that forced it:

> *Removed `Meal` and its three edges — the schema has no meal, and the cooking
> occasion is represented by the help request itself.*

Then close with **what the redraw exposed**. Regenerating a picture surfaces
things a diff doesn't: a term that ends up with no content of its own, a cluster
with no edges, an actor nothing points at. Those are the most valuable sentences
in the output, because they are questions about the schema that only became
visible once it was drawn.

## What the schema is not allowed to delete

Keep these in the picture even though nothing in the schema carries them. Mark
them as unrepresented if you like, but do not drop them:

- **Actors and their actions.** A `cook` uuid is a `Cook` who *posts*. The person
  is not an implementation detail.
- **Relationships the schema has no occasion to express** — `Community —contains→
  1..* Cook` isn't in any payload and is still true.
- **Inverse multiplicities.** No payload schema can state how many shipments a
  sender has. The glossary can, and it's often the more useful half.
- **Exclusivity.** `oneOf` may be missing simply because nobody reached for it.
- **Terms in other bounded contexts.** They stay, in their own colour, referenced
  rather than described.
- **Anything the person told you was a deliberate business fact.** A conversation
  outranks both artifacts.

Conversely, do **not** import into the glossary: identifier fields, `format`,
`minLength`, `pattern`, `examples`, discriminators, pagination and envelope
types, or collection wrapper types (`Pictures` wrapping `Picture` is a
cardinality, not a term).

## Output

1. **The changelog** — removed / changed / added, each with the schema construct
   that forced it, then *what the redraw exposed*.
2. **The SVG** (and PNG if you can), with a legend for the context colours and
   the two arrowhead shapes.
3. **The model file**, so the next revision is an edit rather than a rebuild.

Lead with the changelog. The picture is the artifact; the changelog is what makes
it reviewable.

## Reference files

- `references/derivation-rules.md` — the schema→glossary construct table, the
  abstraction rules for identifiers and technical types, the colour rule for
  owned vs referenced concepts, naming, and the cases where you stop and ask
  instead of drawing. Read it before Step 4.
- `references/rendering.md` — the model file format, the standard five-band
  layout, edge routing and when to override it, the geometry check loop, and
  styling that matches a workshop board. Read it before Step 5.
- `references/example/help-glossary.yaml` — a complete, clean-rendering model of
  a cooking-help domain (24 terms, 31 edges, two contexts). Copy it as a
  starting point.
- `references/worked-example.md` — that glossary being derived from its schema,
  end to end: the four lists, the derivations that were judgement calls, the
  changelog, and the two things the redraw exposed.
