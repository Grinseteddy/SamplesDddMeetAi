---
name: domain-story-event-seeder
description: >-
  Seed an EventStorming board from Domain Stories: interprets one or several
  uploaded domain stories (pictures, egon.io exports, or numbered
  actor→activity→work-object sentences) and turns them into a past-tense list of
  proposed domain events — the orange stickies — each traced back to the sentence
  it came from. Use whenever someone has domain stories and wants events, an
  event list, an event timeline, or an EventStorming board out of them: "turn
  these domain stories into domain events", "seed an event storming session from
  our stories", "what events happen in this story". Prefer over
  event-storming-seeder whenever a domain story is provided, and over
  domain-story-interpreter whenever the wanted output is events rather than a
  prototype brief. If no story is supplied and the user has none, hands over to
  event-storming-seeder and seeds from a rough description instead. Proposes
  events ONLY — no commands, aggregates, policies, or bounded contexts.
compatibility: >-
  Depends on two installed skills (domain-story-interpreter and
  event-storming-seeder). Degrades gracefully if either is missing — see
  Fallbacks. No scripts or dependencies.
---

# Domain Story → Event Seeder

This skill is an **orchestrator**. It joins the two halves of a job that people
keep doing by hand: *read the domain stories the team already drew*, then *put
the resulting facts on a wall as orange stickies* so an EventStorming session
doesn't start blank.

It does **not** re-implement either half. Read them rather than guessing their
content:

- **`domain-story-interpreter`** — reading the pictographic language.
  `/mnt/skills/user/domain-story-interpreter/SKILL.md`
- **`event-storming-seeder`** — what a good domain event is, and how to hunt the
  ones nobody wrote down.
  `/mnt/skills/user/event-storming-seeder/SKILL.md`

What this skill owns, and neither sub-skill covers, is the **seam**: the
translation from story sentences to stickies, and the handling of several stories
at once.

## The pipeline

```
one or more Domain Stories ─▶ 1. INTERPRET ─▶ 2. RELATE ─▶ 3. TRANSLATE ─▶ 4. SWEEP ─▶ 5. PRESENT
                                (interpreter)   (this skill)  (this skill)   (seeder)    events only

no story at all ────────────▶ hand over to `event-storming-seeder`
```

## Stay in your lane: the output is events

The interpretation is **scaffolding, not deliverable**. You will derive a domain
model, state machines, and probably some module boundaries along the way — none
of that goes on the wall. The published output is domain events plus the honest
bookkeeping around them, exactly as `event-storming-seeder` defines it: no
commands, no aggregates, no actors, no policies, no bounded contexts, no
groupings.

This is a real constraint, not modesty. Those things are the *room's* work, and a
seed that arrives with boundaries already drawn has skipped the conversation the
session exists to have. If the user wants the rest, offer the full prototype
brief from `domain-story-interpreter` as a separate deliverable — don't quietly
extend the event list.

## Workflow

### 0. Get the stories — or hand over

Look for stories in `/mnt/user-data/uploads/`, in pasted text, and in any
interpretation already produced earlier in the conversation. Anything counts: a
photo of a whiteboard, an egon.io export, or plain numbered sentences.

If none is there, **ask once**:

> Do you have any **domain stories** for this process — numbered
> `Actor · verb · work object` sentences, an egon.io export, or a photo of the
> board? Send as many as you have; several stories covering different paths seed
> a much better wall than one. If there aren't any, say so and I'll seed from a
> description of the process instead.

If the answer is "none", a description, or a "just go ahead": **hand over
cleanly.** Read and follow `event-storming-seeder/SKILL.md` from its Step 0 and
seed from whatever description exists. Do not run a hollow version of this
pipeline on no input, and do not ask for stories a second time.

**A light sanity check, not a critique.** A shaky story still seeds fine. Flag
only a *blocking* defect — sentences with no discernible actor or work object, or
a "story" that is really a branching flowchart — and offer `domain-story-critic`
as the user's off-ramp rather than critiquing it yourself. Default to seeding.

### 1. Interpret each story — but only the parts that feed a wall

Follow `domain-story-interpreter` for the reading, and stop at the sections that
produce facts. Label the stories `A`, `B`, `C` … as you go.

| Interpreter section | Use it? | Why |
|---|---|---|
| §1 Story transcription | **yes** | the spine; everything downstream is traceable to it |
| §2 Actors & roles | **yes, as context** | tells you *whether* a fact is business-meaningful — but actors never reach the sticky |
| §3 Modules (groups) | read, don't publish | grouping is the room's work |
| §4 Domain model | **yes** | entity vs UI channel vs physical object decides what can even be an event |
| §5 State machines | **yes — the richest source** | every transition is a candidate event |
| §6–7 Use cases, screens | **no** | prototype material, irrelevant to a wall |
| §8 Open questions | **yes** | they flow straight into the seed's Questions |

The state machines earn special attention. A cluster like
`New task → Assigned task → Task in progress → Task done / Task rejected` is one
entity moving through states, and **each transition is a high-confidence event**
(`Task assigned`, `Task started`, `Task finished`, `Task rejected`). These are the
events you are least likely to be wrong about, because the team drew the state
change themselves.

If the diagram is unreadable in places, name the specific sentence you couldn't
read. Never invent the verb that was probably on the arrow.

### 2. Relate the stories to each other

With more than one story, decide which case you're in, **say so out loud**, and
put it in Questions if you had to guess:

| They are | You do |
|---|---|
| **Variants of one process** (happy path + a refusal or edge case) | one spine from the fullest story; the others' diverging steps become *Alternatives & failures*, marked with where they branch |
| **Segments of one longer flow** (order taking → picking → delivery) | concatenate in business time; ask whether the seam is really seamless or hides steps nobody wrote down |
| **Separate processes that merely share work objects** | keep separate event lists under separate headings. **Never invent a joining event** to make them one wall |

Then, across the set:

- **Deduplicate by fact, not by wording.** Two stories naming one fact
  differently (`Cost estimate sent` / `Quote sent`) is not a nuisance, it is the
  find: pick one name, show the other beside it, and ask which word the business
  actually uses.
- **Normalise the altitude.** Stories written by different people rarely sit at
  the same granularity. Pick one altitude (`event-storming-seeder` Step 1) and
  re-cut to match, noting where you split or merged.
- **Contradictions are gold.** A different order, an extra approval, an actor
  present in one story and absent in another — never quietly reconcile. Pick a
  reading, mark it, ask.

### 3. Translate sentences into stickies

Sentence by sentence, but **never one-to-one**:

| Story sentence | Sticky | Rule |
|---|---|---|
| `3 Dispatcher assigns Order to Driver` | `Order assigned to driver` | verb + work object, put in the past |
| `1 Visitor selects Plan on Pricing page` | `Plan selected` | the screen is not part of the fact |
| `5 System sends Welcome email to Customer` | `Customer welcomed` | name the business fact, not the transport |
| `2 Visitor enters Payment details` + `3 Billing system charges Payment details` | `Payment authorised` | two sentences, one fact — collapse |
| `4 Clerk opens Claim in Claims system` | *(nothing)* | navigation and lookup: nothing happened |
| `6 Clerk handles Return` | `Return inspected` · `Refund issued` | a vague verb hides several facts — split, and ask |

Five habits that keep the translation honest:

- **The actor stays behind.** `Dispatcher` is a small yellow sticky the room adds
  later; an event carrying its actor has smuggled in another colour. If *who did
  it* seems to change the fact, that's a Question, not a longer name.
- **Sentence count ≠ event count.** Expect to drop some, merge some, split
  others. Say which you did.
- **A story is one instance; events are types.** Keep the story's specific noun
  when the business really treats that case differently (`Repeat customer's order
  placed`); generalise when the specificity was just the example's flavour
  (`Order placed`). Either way, name the choice.
- **UI channels produce no events; physical objects do.** `App` and `Portal` are
  where an activity happened. `Bicycle` and `Invoice` are things whose state
  changes are facts.
- **Tag provenance as you go** — `(A3)`, `(B1–B2)`, `(—)` for anything invented.
  A seeded wall is trusted only if any sticky can be walked back to its sentence.

### 4. Sweep for the events no story could contain

Run `event-storming-seeder` Step 3 in full — rejection, time passing, reversal,
the outside world, human judgement, thresholds and bulk.

**This step matters more here, not less.** A domain story is a single happy path
*by definition of the notation*: it cannot draw a refusal, a timeout, or an undo.
So a tidy set of stories makes a process look like it never fails, and a seed
that mirrors them faithfully inherits that lie. Expect to generate more
alternatives than the stories contained.

Two extra hunting grounds the state machines hand you:

- **Dead-end states.** A state with no outgoing transition (`Task rejected`) is
  usually not really terminal. What happens next is an event nobody drew.
- **Missing reversals.** Every transition invites its undo: assigned → unassigned,
  approved → withdrawn. Ask whether the business has them.

### 5. Present

Use the `event-storming-seeder` output template, with two additions the story
input earns:

```
# Seed domain events — <process>

## Sources
A — <story, one line on what it covers>
B — <story …>
How they relate: <variants / segments / separate processes>.

## What I understood
The process, its trigger, its outcome, the altitude chosen. Four lines, no more.

## Events
1. <Event, past tense>  (A3)
2. …

## Alternatives & failures
- <Event>  (branches from 4 — rejection) (B2)
- …

## Made up
Which of the above I invented rather than read — everything tagged (—).

## Questions
The uncertain spots, as questions for the room — including every disagreement
between stories and every fact two stories named differently.
```

With **separate processes**, repeat `Events` and `Alternatives & failures` under
one heading per process rather than merging them.

## Fallbacks

- **No story supplied** → hand over to `event-storming-seeder` (Step 0 above).
- **`domain-story-interpreter` not installed** → transcribe the stories yourself
  from the numbers and arrows, say that you did so without the notation
  reference, and continue. Sequence numbers sit at the arrow's origin;
  unnumbered arrows continue the same sentence rather than starting a new one.
- **`event-storming-seeder` not installed** → you still own the seam, but say
  plainly that the event-quality rules and the six-source sweep are being applied
  from memory rather than from the skill.

## Handing off

Offer the next step; don't take it unasked.

- **They want the modules, entities, and screens too** → the full prototype brief
  from `domain-story-interpreter`. This skill deliberately published only events.
- **The stories look wrong, not just thin** → `domain-story-critic`.
- **They have no stories yet and want some** → `domain-story-seeder` drafts
  strawman ones, which feed straight back into this skill.
- **Session run, wall photographed** → `event-storming-interpreter` turns the
  corrected board into a buildable brief.
- **Publish the events as a contract** → `asyncapi-spec-author`.