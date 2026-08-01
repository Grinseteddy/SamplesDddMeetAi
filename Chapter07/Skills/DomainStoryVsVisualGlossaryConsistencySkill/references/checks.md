# The checks

Contents:

1. [The matching ladder](#1-the-matching-ladder) — how a story noun is matched to a glossary term
2. [False-positive guards](#2-false-positive-guards) — what never to flag
3. [The nine checks](#3-the-nine-checks) — detection recipe, example, severity, resolution
4. [Union vs. pairwise](#4-union-vs-pairwise) — the multi-story rule
5. [Severity rubric](#5-severity-rubric)
6. [Phrasing findings](#6-phrasing-findings)

---

## 1. The matching ladder

Before any check can run, every noun in every story has to be matched against the
glossary's terms. Walk the rungs **in order** and stop at the first that fits.
The ladder exists because the difference between a finding and a false positive
is almost always a matching decision made too fast.

| Rung | Test | Verdict | Example (glossary → story) |
|---|---|---|---|
| 1 | Byte-identical | `=` exact | `Bicycle` → `Bicycle` |
| 2 | Differs only in case, spacing, hyphenation | `~` variant, **Minor** | `Monthly Fee` → `Monthly fee` |
| 3 | Differs only in number | `~` variant, **Minor** | `Bicycle` → `Bicycles` |
| 4 | One or two characters off, no other candidate nearby | `~` typo — read charitably, note once, do not make a finding of its own | `Management` → `Mangement` |
| 5 | Glossary term + a state adjective | `=` **state of** that term — see guard G1 | `Task` → `Assigned Task` |
| 6 | Different word, same referent in context | `✗` **`TERM` / `DRIFT`** | `Rack` → `Stand` |
| 7 | No glossary term plausibly refers to it | Story-only noun → guards, then `UNDEF` | — `Mechanic` |

Rung 6 is a judgement call and should read like one. The evidence is *positional*:
`Stand` matches `Rack` because it sits where `Rack` sits — the thing a Bicycle is
returned to. Say that reasoning out loud in the finding, so the person can
disagree with the reasoning rather than just the conclusion.

Where two glossary terms both plausibly match one story noun (`Fee` and `Monthly
Fee` for a story's `fee`), that ambiguity **is** the finding — the glossary
distinguishes two things the story does not.

---

## 2. False-positive guards

A story is not a glossary and is never expected to look like one. Each guard
below describes something that looks like a mismatch and is not. Apply them
before writing any `UNDEF` finding.

**G1 — States are not undefined terms.** Domain stories routinely carry a work
object through its lifecycle by putting adjectives on one noun: `New task`,
`Assigned Task`, `Task in progress`, `Task done`. That is one glossary term in
four states, not four undefined terms. Match them all to `Task` (ladder rung 5)
and record the state set. It becomes a finding only in one narrow case: the
stories imply a lifecycle and the glossary has **no** status, state, or lifecycle
concept anywhere — then raise **one** `REL-X` about the missing state model, not
one per adjective.

**G2 — UI channels and devices are out of scope.** `App`, `phone`, `terminal`,
`website` are where the activity happens, not domain nouns. A glossary is
entitled to omit them. List them under "out of glossary scope" and move on. The
exception is when a channel carries domain meaning the glossary needs (a
`Booking Terminal` that a Rack physically has) — then it is a real `UNDEF`.

**G3 — Physical props may be deliberately absent.** A `Bicycle` the system
tracks belongs in the glossary; the `Home` a commuter cycles to may be pure
scene-setting. Ask before flagging: does the system need to know about this?

**G4 — Actors are terms too, but glossaries differ.** Some glossaries include
actor stickies (Tourist, Commuter, Hotel), some model only work objects. Check
which convention this glossary follows *before* checking actor names against it.
If it has no actors at all, actor names cannot be `UNDEF` — note the convention
once and check only work objects.

**G5 — To-be stories may lead the glossary.** A story explicitly scoped as
to-be is allowed to name things that do not exist yet. Report these as
"glossary moves" — vocabulary the glossary should acquire — never as story
defects. This is why Step 0 asks for the scope declaration.

**G6 — Verbs are not glossary terms.** The glossary's relationship labels
(`stores`, `unlocks`) and the story's activities (`registers`, `pays`) are
different vocabularies serving different purposes. Do not check activities
against relationship labels for equality. They meet only in `REL-X`: a story
activity that implies a *structural* connection the glossary lacks.

**G7 — Read one artifact's typo charitably, but only once.** Do not build a
finding on a spelling slip. Note it in passing and use the intended term
throughout.

---

## 3. The nine checks

### `TERM` — Term mismatch
*Same concept, different word, story vs. glossary.*

- **Recipe:** every ladder rung-6 match.
- **Example:** glossary `Commuter`; Story B sentence 4 says `User`.
- **Why it matters:** divergent vocabulary is the exact failure the glossary was
  created to prevent. If the story keeps its own word, the glossary has already
  stopped being the ubiquitous language.
- **Severity:** Significant.
- **Resolution:** story moves — unless the story is drawing a genuine distinction
  the glossary is missing (a `User` who is neither Tourist nor Commuter), in
  which case ask.

### `UNDEF` — Undefined story term
*A domain noun a story names that the glossary does not define.*

- **Recipe:** ladder rung 7, after all seven guards.
- **Example:** Story C sentence 3, `Mechanic` — no such term in the glossary.
- **Why it matters:** either the glossary is incomplete, or the story invented
  vocabulary off the agreed language. Both are worth knowing; they have opposite
  fixes.
- **Severity:** Significant if it is a domain noun; Minor if borderline scenery.
- **Resolution:** ask. Lead with the likelier one: a term appearing in several
  stories is almost always a glossary gap.

### `UNUSED` — Unused glossary term
*A glossary term that the union of all stories never touches.*

- **Recipe:** ledger rows whose story columns are all `—`.
- **Example:** `Station Public Transport` appears nowhere in three stories.
- **Why it matters:** it is either dead vocabulary the glossary should drop, or —
  more interesting — the fingerprint of a **story that was never told**. A term
  the domain experts bothered to draw usually earns its scenario.
- **Severity:** Minor.
- **Resolution:** ask; if it is a real concept, the missing story is the fix.

### `DRIFT` ★ — Cross-story vocabulary drift
*Two stories use two different words for one concept.*

- **Recipe:** compare the story columns of each ledger row against each other,
  not only against the glossary term.
- **Example:** Story A says `Bike` and `Stand`; Story B says `Bicycle` and
  `Rack`.
- **Why it matters:** this is the highest-value finding in a multi-story set and
  the one nobody sees checking stories one at a time. It usually maps onto *who
  was in the room* — different workshops, different vocabularies, and a team that
  believes it agrees.
- **Severity:** Significant — Blocking when the drifting term names something two
  teams are building against.
- **Resolution:** story moves, both toward the glossary. Also worth naming the
  pattern out loud: if one story drifts on six terms, the finding is not six
  words, it is that the story was told outside the glossary's reach.

### `SENSE` ★ — Sense conflict
*One word, two meanings.*

- **Recipe:** for each shared noun, compare what it *does* in each story — what
  acts on it, what it is acted on with, what it results in. Divergence there
  means divergence in meaning.
- **Example:** Story A's `Fee` is a one-off charge a Tourist pays at rental;
  Story C's `Fee` is what a Hotel is invoiced monthly. The glossary has both
  `Fee` and `Monthly Fee` — so one story is using the wrong one, or the two are
  genuinely one concept and the glossary over-splits.
- **Why it matters:** a homonym in a ubiquitous language is worse than a
  synonym. Synonyms cause confusion, which someone notices; homonyms cause
  agreement that turns out to be false, which nobody notices until integration.
- **Severity:** **Blocking.**
- **Resolution:** ask — and propose the split explicitly, with a name for each
  sense.

### `CARD` — Cardinality contradiction
*A story shows something a glossary multiplicity forbids.*

- **Recipe:** for each glossary relationship, gather **every** story sentence
  that exercises it, then test the aggregate against the bound. Upper bounds are
  violated by counting instances; lower bounds (`1`, `1..*`) are violated by a
  story that constructs the thing without its mandatory part.
- **Example:** the glossary caps `Rack —stores→ 0..1 Bicycle`, but Story C
  sentence 4 has a Mechanic bringing several Bicycles to one Rack.
- **Why it matters:** the two artifacts assert incompatible facts. One of them is
  going into a schema, and the other is describing what people actually do.
- **Severity:** **Blocking.**
- **Resolution:** usually **glossary moves** — this is the check where the story
  is the stronger evidence, because it records observed reality while the
  cardinality records someone's recollection. Say so, but ask.
- **Guard:** a story showing *one* of something never contradicts a `0..*` or
  `1..*`. Only bounds can be broken, and only from the side they bound.

### `REL-0` — Unexercised relationship
*A glossary relationship no story ever walks.*

- **Recipe:** union of all stories; a relationship counts as exercised when one
  sentence puts both ends together in a way the label describes.
- **Example:** `Rack —located at→ 0..1 Station Public Transport` — never used.
- **Severity:** Minor.
- **Resolution:** ask; often a missing story, sometimes structure nobody needs.

### `REL-X` — Implied relationship the glossary lacks
*A story connects two nouns the glossary never relates.*

- **Recipe:** every story sentence linking two glossary terms via a preposition
  or a linking verb — check the pair exists as a glossary edge.
- **Example:** `Mechanic repairs Bicycle` implies a maintenance relationship the
  glossary has no edge for; `Bicycle` gains a condition the glossary cannot
  express.
- **Why it matters:** the glossary is meant to be the domain's structure. A
  connection the business performs but the model cannot express is a gap that
  surfaces later as a field nobody can find a home for.
- **Severity:** Significant.
- **Resolution:** glossary moves — propose the edge, with a label and a
  cardinality guess marked as a guess.

### `CTX` — Bounded-context misalignment
*Glossary contexts and story lanes disagree.*

- **Recipe:** map each story lane to the glossary context holding its terms.
  Three shapes: same terms, different names (`Billing` vs `Accounting`); a term
  filed in different contexts on each side; a glossary context no story lane
  touches at all.
- **Severity:** Significant for the first two; Minor but noteworthy for the
  third — an untouched context is a subdomain with no told scenario, which is
  either a gap in the stories or a subdomain nobody actually needs.
- **Resolution:** ask. Context names are usually the glossary's to keep, but a
  *term* sitting in different contexts on each side is a modelling disagreement,
  not a naming one, and deserves more than a rename.

---

## 4. Union vs. pairwise

The single rule that makes multi-story validation work:

| Finding kind | Computed against | Why |
|---|---|---|
| `UNUSED`, `REL-0` | the **union** of all stories | A term one story uses is used. Checking per story generates a false finding for every story that happens not to mention it — the fastest way to make a report worthless. |
| `TERM`, `UNDEF`, `REL-X`, `CTX` | **each story**, then deduplicated | The same mismatch in three stories is one finding with three citations, not three findings. Deduplicating is what keeps the report readable. |
| `DRIFT`, `SENSE` | **pairwise between stories** | These do not exist within a single story. They are the reason to check a set as a set. |
| `CARD` | the **aggregate** of all stories | A multiplicity constrains a population, so the violation is usually only visible once the stories are laid side by side. |

State your coverage denominator in the report. "Three stories, 14 glossary terms,
11 exercised" is checkable; "mostly consistent" is not.

---

## 5. Severity rubric

| Severity | Test | Codes usually here |
|---|---|---|
| **Blocking** | The two artifacts assert **different facts** about the domain. Building from one produces something incompatible with the other. | `CARD`, `SENSE` |
| **Significant** | The vocabulary has diverged. Nothing is factually contradictory, but the glossary has stopped doing its job. | `TERM`, `DRIFT`, `UNDEF`, `REL-X`, `CTX` |
| **Minor** | Cosmetic or coverage. Worth listing, not worth a meeting. | case/plural variants, `UNUSED`, `REL-0` |

Promote a Significant finding to Blocking when it sits on something being built
right now. Demote anything the person tells you was deliberate — and record that
it was deliberate, so the next check does not re-raise it.

---

## 6. Phrasing findings

Each finding should be settleable in one sentence by someone who knows the
domain. That means: cite the evidence, state the two possible worlds, and
recommend one.

> **F3 · `TERM` · Significant** — Story A (5, 6) says `Bike` and `Stand`; the
> glossary says `Bicycle` and `Rack`. Story B uses the glossary's words for the
> same things, so the two stories currently disagree with each other as well
> (see F1). *Story moves:* rename in Story A. *Glossary moves:* only if `Stand`
> is a different physical thing from a `Rack` — is it?
> **Recommendation:** rename in Story A.

Avoid the two failure modes. A report that lists 40 findings gets skimmed and
dropped; a report that says "broadly consistent, a few naming nits" hides the
homonym that will cost a sprint. Lead with the Blocking ones, group the Minor
ones into a single line each, and make the ledger carry the exhaustiveness so the
prose does not have to.