---
name: domain-story-glossary-consistency
description: >-
  Validate that one or several Domain Stories are consistent with a Visual
  Glossary — term fidelity (story "Bike" vs glossary "Bicycle"), coverage both
  ways (undefined story terms, unused glossary terms), contradicted
  cardinalities, bounded-context/lane alignment, and across a set of stories,
  vocabulary drift and one word used in two senses. Produces a Consistency
  Report: a term ledger with a column per story, coded findings with severity and
  cited evidence, and a patch list for each side. Use
  whenever someone provides a Visual Glossary together with domain stories (any
  number, as pictures, egon.io exports, or numbered actor→activity→work-object
  sentences) and asks to validate, check, verify, reconcile, align, or diff them,
  to confirm the stories speak the ubiquitous language, or to find where story
  and glossary disagree — even if they never say "Visual Glossary" or "Domain
  Storytelling". Prefer this over a general story critique when the ask is
  conformance to a glossary, or when several stories are in play.
compatibility: >-
  Reads best alongside two installed skills — visual-glossary-interpreter and
  domain-story-interpreter — but degrades gracefully without them. No scripts or
  dependencies.
---

# Domain Story ↔ Visual Glossary Consistency

A **Visual Glossary** is the static model of a domain: the agreed nouns, their
relationships, their cardinalities, grouped into bounded contexts. A **Domain
Story** is one concrete dynamic scenario: actors doing activities on work
objects, numbered into a sequence. They are meant to be two views of one domain,
and they are meant to stay in step — *the glossary defines the vocabulary, the
stories speak it.*

They drift anyway. A story gets told in a workshop where someone says "bike," a
glossary gets tidied up afterwards to say "Bicycle," a second story is drawn by a
different pair three weeks later, and now three artifacts assert three slightly
different domains. This skill finds that drift while it is still cheap.

**This is a conformance check, not a critique.** You are not judging whether the
stories are good stories or the glossary is a good glossary. You are diffing them
and reporting where they disagree. If the person wants the story's *own* quality
challenged — CRUD verbs, collapsed actors, hidden branches — that is
`domain-story-critic`'s job; offer it, don't do it here.

## Core mental model

Four things shape every judgement you make:

1. **Split authority.** The glossary is authoritative on **words**: when the two
   disagree on what to call a thing, the default fix is that the story adopts the
   glossary's term, and so do you in every rewrite you propose. The stories are
   authoritative on **behaviour and reality**: if a story shows something the
   glossary's structure forbids, that is at least as likely to mean the glossary
   is wrong. Never resolve a disagreement silently in either direction — report
   both moves and recommend one.
2. **N stories, one glossary.** Findings that come from *coverage* must be
   computed against the **union** of all stories (a relationship only one story
   exercises is still exercised). Findings that come from *conflict* must be
   computed **per story and pairwise between stories** — because the most
   valuable finding in a multi-story set, two stories using two words for one
   thing, is invisible if you check each story against the glossary alone.
3. **Most apparent mismatches are not mismatches.** Stories legitimately contain
   things a glossary never lists: UI channels, physical props, and above all
   *states* — `Assigned Task` and `Task done` are the glossary's `Task` in two
   states, not two undefined terms. A validator that flags these floods the
   report and gets ignored. The guards in `references/checks.md` matter as much
   as the checks.
4. **Findings, not verdicts.** Every disagreement is evidence that two groups of
   people understood something differently — which is exactly the value on offer.
   Phrase each so a domain expert can settle it in one sentence.

## Preconditions

You need **a glossary and at least one story**. If the glossary is missing, this
skill cannot run: say so and offer the alternatives (critique the stories on
their own quality, or derive a candidate glossary from the stories' work
objects). If only one story was given, run anyway and state plainly that the
cross-story checks (drift, sense conflict) were skipped for lack of a second
story.

Ask for each story's **scope declaration** — *as-is or to-be, coarse or fine* —
if it isn't obvious. A to-be story is allowed to contain vocabulary an as-is
glossary lacks; without knowing, you will file a deliberate design as a defect.
One question, then proceed.

## Workflow

### Step 1 — Read both sides into text, and confirm

Do not diff pictures. Transcribe first, because a misread edge or a misread
arrow invents disagreements that were never there.

- **The glossary** → the transcription and relationship table from
  `visual-glossary-interpreter` (its Steps 1–2 and the section 3 table).
  Read `/mnt/skills/user/visual-glossary-interpreter/SKILL.md` and follow it;
  mind its fan-out rule, which is the most common transcription error.
- **Each story** → the numbered sentence list from `domain-story-interpreter`
  (its Step 1). Read `/mnt/skills/user/domain-story-interpreter/SKILL.md`.
  Keep the stories **separate and labelled** (Story A, B, C…) with their
  sentence numbers intact — every finding you report later cites them as
  evidence, and a finding without a citation is unactionable.

If either skill is unavailable, transcribe directly: terms and
`Source —label→ cardinality Target` for the glossary, `N. Actor verb Work object
(preposition Work object)` for each story.

Show the transcriptions and ask for confirmation whenever anything was hard to
read. Mark what you could not read as **unchecked**, not as absent — reporting
"no findings" about a region you couldn't see is the one failure that destroys
trust in the whole report.

### Step 2 — Build the term ledger

One row per **glossary term**, one column per **story**, filled with the exact
spelling that story used (or `—` for absent). Below a divider, add the
**story-only nouns**: everything the stories name that no glossary row claimed.

The ledger is the heart of the report. It makes the whole comparison visible at a
glance, it forces you to visit every term rather than the ones that caught your
eye, and it is what the team will screenshot.

Match story nouns to glossary rows using the **matching ladder** in
`references/checks.md` — exact, case/spacing variant, plural, charitable typo,
state-of, synonym, unmatched. The ladder is what keeps `Task done` from becoming
a false finding and `Stand` from becoming a false match.

### Step 3 — Run the checks

Work through all nine, in this order, from `references/checks.md` (detection
recipe, worked example, default severity and typical resolution for each):

| Code | Check |
|---|---|
| `TERM` | Term mismatch — same concept, different word |
| `UNDEF` | Story term the glossary never defines |
| `UNUSED` | Glossary term no story ever touches |
| `DRIFT` | ★ Two stories, two words for one concept |
| `SENSE` | ★ One word, two meanings across stories |
| `CARD` | A story contradicts a glossary cardinality |
| `REL-0` | A glossary relationship no story exercises |
| `REL-X` | A relationship the stories imply, the glossary lacks |
| `CTX` | Story lanes and glossary contexts disagree |

★ = only available when you have more than one story; these are the ones nobody
gets from checking stories one at a time, so give them room.

`CARD` deserves particular care: single stories rarely violate a multiplicity on
their own, because a cardinality is a statement about a *population*. The
violation usually appears only when you lay the stories side by side — three
bicycles racked at one Rack the glossary caps at `0..1`. Check the aggregate, not
just each story.

### Step 4 — Assign severity and a resolution direction

Every finding gets both. Severity (rubric and examples in `references/checks.md`):

- **Blocking** — the artifacts assert *different facts* about the domain. Someone
  will build the wrong thing. Cardinality contradictions and sense conflicts live
  here.
- **Significant** — vocabulary divergence that defeats the point of having a
  glossary: term mismatches, drift, undefined domain terms, context misalignment.
- **Minor** — cosmetic or coverage: casing, plurals, typos, an unexercised
  relationship, an unused term.

Resolution direction is **story moves**, **glossary moves**, or **ask** — and say
which you'd pick. "The story should say Bicycle" and "the glossary is missing a
Mechanic" are both useful; "these differ" is not.

### Step 5 — Write the patch list

Close with edits concrete enough to apply without further discussion: *Story A
sentence 5: `Bike` → `Bicycle`.* *Glossary: add `Mechanic`, relate
`Mechanic —maintains→ 0..* Bicycle`.* This is what turns a report into a
half-hour of work someone can actually do.

## Output: the Consistency Report

```
# Consistency Report — <N> stories × <glossary name>

## 0. Scope checked
Stories with their scope (as-is/to-be, granularity) · the glossary ·
anything unreadable and therefore UNCHECKED.

## 1. Verdict
One line: aligned / aligned with minor drift / diverged.
Finding counts by severity, and per story.

## 2. Term ledger
Table: Glossary term · Story A · Story B · … · Status
(= exact / ~ variant / ✗ mismatch / — unused).
Then, below a divider, story-only nouns — split into
"domain terms missing from the glossary" and "out of glossary scope"
(UI channels, physical props, states).

## 3. Findings
Ordered by severity, biggest first. Each one:
ID · code · evidence (Story, sentence #) · why it matters ·
story moves / glossary moves · recommendation.

## 4. Relationships & cardinalities
Table: Glossary relationship · cardinality · exercised by (story, sentence) ·
verdict (consistent / contradicted / never exercised).
Plus relationships the stories imply that the glossary lacks.

## 5. Bounded contexts
Glossary context ↔ story lane alignment; contexts no story reaches.

## 6. Patch list
Two lists — edits to the stories, edits to the glossary.

## 7. Already aligned
Name what matches. It tells the team what to protect, and it is the evidence
that you checked everything rather than only what looked broken.
```

Adapt depth to the ask. "Do these use the same language?" wants sections 1–3 and
the patches; a pre-implementation review wants all of it. Always keep section 0
and the ledger — they are what make the report checkable rather than believable.

## Working with the neighbours

- **`domain-story-critic`** — story quality, one story at a time. It runs a
  glossary cross-check as one pass among many; this skill is that check taken
  seriously across a set. If your findings suggest the story is weak *in itself*
  (a hidden branch, a UI verb), name it in one line and offer the critic.
- **`visual-glossary-interpreter`** — if the glossary turns out to be the weaker
  artifact (undefined terms, missing inverse cardinalities, ambiguous
  arrowheads), a proper Glossary Brief is the follow-up.
- **`domain-story-seeder`** — when a glossary context has no story at all
  (`CTX`), seeding one is the natural next move.

## Reference files

- `references/checks.md` — the matching ladder, the nine checks with detection
  recipes and examples, the false-positive guards (states, channels, props,
  actors, typos, to-be scope), the multi-story union-vs-pairwise rule, and the
  severity rubric. Read it before Step 2; it carries the detail this file only
  names.
- `references/worked-example.md` — a bicycle-sharing glossary checked against
  three stories, transcription through report, containing a live example of each
  finding type. Read it to pattern-match before writing your first report.

## References

S. Hofer and H. Schwentner, *Domain Storytelling: A Collaborative, Visual, and
Agile Way to Build Domain-Driven Software.* Boston, MA, USA: Addison-Wesley,
2022. ISBN 978-0-13-745891-2. Companion site: domainstorytelling.org.

E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software.*
Boston, MA, USA: Addison-Wesley, 2003 — for the ubiquitous language the two
artifacts are jointly trying to hold.