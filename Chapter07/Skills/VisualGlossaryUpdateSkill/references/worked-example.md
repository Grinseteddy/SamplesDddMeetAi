# Worked example — updating a cooking-help glossary from its schema

The glossary was drawn early: a cook asks Grandma or the community for help, and
gets an answer. Over four revisions the schema moved a long way from it. This is
the regeneration, end to end. The finished model is
`example/help-glossary.yaml`; it renders clean.

---

## Step 1 — The old picture

15 terms in the help context, 3 in a recipe context (a different colour), 21
edges. The parts that matter:

```
Cook —posts→ 0..* Help Request
Cook —prepares→ 0..* Meal
Help Request —belongs→ 1 Meal
Help Request —at→ 1 Community
Help Request —at→ 1 Grandma Avatar
Community —provides→ 0..* Help
Grandma Avatar —provides→ 0..* Help
Community —contains→ 1..* Cook
Meal —with→ 1 Recipe
Help —contains→ 0..* Ingredient Substitute
Help —contains→ 0..* Preparation Step Explanation
Help —contains→ 0..* Steps to Mitigate Catastrophy
Ingredient Substitute —refers to→ 1 Substitute
Thanks —to→ 0..1 Community / 0..1 Grandma Avatar
```

**A wrinkle worth repeating.** Two versions of the picture were supplied — an SVG
export and a photo — and they disagreed: the export had no recipe-context stickies
and said `0..*` where the photo said `0..10`. That is a Step 1 finding, not a
detail. The photo was the newer board; the export would have been the wrong
baseline.

## Step 2 — Schema coherence

`inspect_schema.py` reported four blocking `REQ-UNDEFINED`s: `HelpIngredients`
requires `recipe` and `ingredient`, `HelpStep` requires `recipe` and `step`, and
none of those four properties exist any more — they were removed one revision
earlier when the team de-duplicated the answers against the requests.

So the intent was clear (the properties are gone deliberately; the `required`
lists are stale) and the derivation could proceed on that reading, with the
defect reported. Had the intent been ambiguous, this is where to stop and ask.

## Step 3 — The four lists

**Remove**

| Term / edge | Why the schema forced it |
|---|---|
| `Meal`, `Cook —prepares→ Meal`, `Help Request —belongs→ Meal` | No meal anywhere; the team's position is that the cooking occasion *is* the request |
| `Substitute` | Merged into `Ingredient` — one shape, used for the thing replaced, the replacements, and the mitigation addition |
| `Ingredient Substitute` | Substitutes hang directly off the ingredients answer as an array |
| `Preparation Step Explanation` | No such type; the explanation is the base `helpDescription` |
| `Help Request —at→ 1 Community` / `1 Grandma Avatar` | The request records no target — only the answer records its provider |
| `Community —contains→ 1..* Cook`, `Thanks —to→ …` | *Kept*, see the guard list |

**Add** — `Question`, `Help Text`, `Thank Text` (the text payloads, which the
workshop drew structure for and forgot); `Help Provider` (from
`helpProviderType`); `Mitigation Step` with `Step Number` and `Step Text`;
`Name`, `Quantity`, `Unit` under `Ingredient`.

**Rename / reshape** — `Steps to Mitigate Catastrophy` → `Mitigation Step`
(singular, `1..10`, with its own content). The three request subtypes and three
answer subtypes renamed to pair 1:1 with the schema types.
`Catastrophy` → `Catastrophe`. `Ingredient` moved from the referenced colour to
the owned colour, because it is now described locally by value.

**Keep despite absence** — `Community —contains→ 1..* Cook` (true, and no payload
would ever carry it); the inverse ends of every relationship; `Cook` as an actor
rather than a uuid.

## Step 4 — Derivations that were judgement calls

**`helpProviderType` → one term, not two.** The enum values `GRANDMA_AVATAR` and
`COMMUNITY` could have stayed as the two original stickies with an edge each. But
that is what produced the old picture's bug — two mandatory `1` edges from one
term, reading as "every request goes to both" when an exclusive choice was meant.
One `Help Provider` term with the values as a subtitle says exactly what the enum
says.

**Identifiers → edges.** `helpRequestIdentifier` became `Help —for→ 1 Help
Request`; `help` on `Thanks` became `Thanks —for→ 1 Help`; `cook` became
`Cook —posts→ 0..*`, direction reversed so it reads as English. `helpingCook`
became `Help —given by→ 0..1 Cook`, keeping the optionality. No sticky in the
output ends in "Id".

**`Recipe` and `Step` stayed in the referenced colour; `Ingredient` moved out of
it.** The schema references a recipe by uuid and a step by a pattern, but
describes an ingredient by value with name, quantity and unit. Describing is what
decides the colour, not where the concept came from.

**`Pictures` produced nothing.** A wrapper type around `Picture` is a cardinality,
not a term — and it was an orphan contradicting its inline twin anyway.

## Step 5 — Rendering

The first model rendered with three `EDGE-THROUGH-STICKY` findings and four
`LABEL-ON-STICKY`. Fixes, in order of how often they'll be needed:

- Moved `Mitigation Step` down a band — it sat 70px from `Catastrophe Help`, and
  `contains 1..10` needs about 100px of label room.
- Widened the gap between `Recipe` and `Step` for the same reason.
- `Help —given by→ Cook` crossed `Help Request`; routed it through the corridor
  below the core band with `route: {via_y: 445}`.
- `Ingredients Help Request —about→ Ingredient` clipped `Step Help`; `route:
  {force: v}` sent it down and across instead.
- Two subtype names needed `wrap_px` to break onto two lines.

Four iterations to `Geometry clean`.

## Step 6 — What the redraw exposed

Two things, neither visible in a diff:

**`Step Help` is an empty sticky.** With `recipe` and `step` de-duplicated away,
the type adds nothing to `Help` — so the picture has a term with no content of
its own. Either it isn't a distinct concept and the base type plus a discriminator
is enough, or the step answer needs something the catastrophe answer has and it
doesn't. You only see this when you try to draw it.

**Nothing points at the community any more.** `Community` survives only as one of
two `Help Provider` values. The old picture had it containing cooks and providing
help; the new schema knows it only as an enum constant. If the community is
meant to be a real thing in this domain — with members, with moderation — the
schema has quietly demoted it.

---

## What this example is meant to teach

- **Ask which picture is current before deriving.** Two exports of "the glossary"
  disagreed; deriving from the wrong one wastes the whole exercise.
- **Stale `required` lists are the normal state of a schema mid-refactor.** Read
  the intent, report the defect, don't draw both readings.
- **An enum of domain nouns is one term, not several.** It also fixes the
  exclusivity the old picture couldn't express.
- **The colour rule is about describing, not about origin.** `Ingredient` is
  drawn as owned because this schema gives it a shape.
- **The redraw is a diagnostic.** The empty sticky and the demoted community are
  the output's most valuable sentences, and both came from drawing rather than
  from comparing.