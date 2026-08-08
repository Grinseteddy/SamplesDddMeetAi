---
name: event-storming-seeder
description: >-
  Generate domain events from a rough description of a business process — a
  numbered past-tense event timeline (the orange-sticky spine) ready to seed an
  EventStorming board, plus, at the granularity asked for, the commands, actors,
  aggregates and policies around each event, pivotal events, candidate bounded
  contexts, and an honest list of every guess made. Use whenever someone wants to
  produce, draft, generate, brainstorm, or seed domain events, an event list, an
  event timeline, or an EventStorming board from a rough process description, a
  few sentences about how the business works, meeting notes, a domain story, or a
  capability map — including asks like "what events happen
  when a customer places an order", "turn this process into domain events",
  "seed an event storming session", or "give me the past-tense facts for this
  workflow". Trigger even when nobody says "EventStorming".
  This is the generative complement of the event-storming-interpreter: here you
  PRODUCE the events rather than read them off a board.
---

# Event Storming Seeder

EventStorming (Alberto Brandolini) explores a domain by sticking notes on a long
wall. The spine is a line of **domain events** — business facts in the **past
tense** — arranged left-to-right in time. This skill runs the *generative*
direction: from a rough, informal description of how some business process works,
it writes that spine as text, so a room has something concrete to argue with
before anyone picks up a marker.

## Why seed events at all

A blank wall is intimidating; a slightly-wrong strawman is easy to attack, and
the attacks are exactly where real domain knowledge surfaces. Two things follow,
and they shape everything below:

1. **The seed is disposable scaffolding, not ground truth.** Make it concrete and
   plausible, label every inference, and turn the shaky spots into questions for
   the room. Being a little wrong on purpose is productive — it provokes the
   correction that teaches you the domain. Never present a guess as a finding.
2. **Events are the only thing you can be confident about.** Commands,
   aggregates, and policies are *derived* — inferred from the events, not given
   by the description. Generate them when the requested depth calls for it, and
   mark them as inferred. A sparse, honest seed beats a rich, fabricated one.

## What counts as a domain event

A domain event is **something that happened in the business, stated as a
completed fact, in the language the business uses**. Test each candidate against
all four:

| Test | Passes | Fails |
|---|---|---|
| **Past tense, a fact** | `Order placed`, `Payment received` | `Place order` (a command — an intention) |
| **Business-meaningful** — a domain expert would care | `Shipment delayed` | `Row inserted`, `Cache invalidated` |
| **Domain language, not UI or CRUD** | `Customer registered`, `Subscription cancelled` | `Save button clicked`, `User record updated` |
| **Irreversible** — undoing it is its *own* event | `Invoice issued` … later `Invoice voided` | an event that "un-happens" |

Two habits worth naming explicitly, because they are where generated events
usually go wrong:

- **CRUD is not a domain event.** `Created / Updated / Deleted` names describe
  the database, not the business. Ask *why* the record changed and name that:
  `Customer updated` → `Delivery address corrected` or `Customer moved house`.
  If nobody can say why, that's a hotspot worth flagging, not an event.
- **Events are types, not instances.** Unlike a domain story (one concrete happy
  path, no branches), an event timeline legitimately carries alternatives,
  failures, and reversals side by side. Do not force it into a single path.

## Workflow

### Step 0 — Read the description and echo it back

The input can be anything: a paragraph, a bulleted process, meeting notes, a
transcript, a domain story, a capability map, a canvas, or one sentence. Read it,
then restate in two or three lines what process you think you're modelling, its
**trigger** (what starts it) and its **outcome** (what "done" looks like). A
misread spine poisons everything downstream, and this is the cheapest possible
place to catch it.

If the description is genuinely too thin to bracket — no discernible process, no
actors, no outcome — ask **one** question rather than several: what starts this
process, and what does success look like at the end? Then proceed. Do not stall
for a full requirements document; seeding is meant to work from very little.

### Step 1 — Fix the granularity (state it, and stay consistent)

Pick the flavour that matches what the user is going to do with it, say which you
picked and why, and hold that level all the way across the timeline. Mixed
granularity is the most common defect in generated event lists.

| Flavour | Scope | Roughly | Includes |
|---|---|---|---|
| **Big Picture** | the whole business flow, several departments | 8–15 events | events, actors, external systems, hotspots — few or no commands |
| **Process-level** | one process end to end | 15–30 events | the full grammar: commands, actors, policies, read models |
| **Design-level** | one aggregate's lifecycle | 10–20 events | full grammar plus states and invariants |

If the user didn't say, infer from the ask ("seed a workshop" → Big Picture;
"I want to build this" → Process-level) and name your choice so they can push
back. When the interactive option picker is available and the ask is genuinely
ambiguous, offering the three flavours is a couple of taps.

### Step 2 — Write the happy-path spine

Lay the events out in time order, numbered, past tense, one fact each. Start at
the trigger and end at the outcome you named in Step 0. Prefer a concrete
protagonist and concrete objects — `Repeat customer's order placed` beats
`Order processed`.

Keep the spine readable: the reason it exists is that a room can follow it
left-to-right and say "no, that's not what happens next".

### Step 3 — Hunt the events the description forgot

The rough description will describe the happy path and almost nothing else. The
missing events are the valuable ones, because they're where the room's tacit
knowledge lives. Sweep these six sources deliberately:

| Where events hide | Ask | Typical event |
|---|---|---|
| **Rejection** | how can each step refuse? | `Payment declined`, `Application rejected` |
| **Time passing** | what happens because nothing happened? | `Reservation expired`, `Invoice overdue` |
| **Reversal / compensation** | how is each fact undone? | `Order cancelled`, `Payment refunded` |
| **External systems** | who outside answers, and slowly? | `Credit check returned`, `Carrier confirmed pickup` |
| **Human decisions** | who judges, and what are the outcomes? | `Claim approved`, `Discount overridden` |
| **Thresholds & bulk** | what fires on a limit or a batch? | `Stock fell below reorder level`, `Payout batch settled` |

Failure events are first-class citizens — do not normalize them away into the
happy path. Where a failure has no visible handling, that gap becomes a hotspot
in Step 6.

### Step 4 — Derive the grammar around each event (Process-level and below)

Only at Process-level or Design-level, work outward from each event to assemble
Brandolini's sentence — and keep it in exactly this order, because the whole
notation depends on it:

```
An ACTOR (or a POLICY) issues a COMMAND
   to an AGGREGATE,
   which produces a DOMAIN EVENT,
   informed by / updating a READ MODEL.
```

Derive mechanically: the **command** is the event in the imperative
(`Order placed` ← `Place order`); the **actor** is whoever would issue it; the
**aggregate** is the noun whose rules the command must satisfy; a **policy**
replaces the actor wherever the event fires automatically ("whenever *Payment
received*, *Release order for picking*"); a **read model** is whatever someone
must see to decide. Where you can't name one honestly, leave the cell empty — a
gap is signal, and empty cells are what the workshop fills in.

### Step 5 — Mark pivotal events and candidate bounded contexts

Scan the finished timeline for the two or three **pivotal events** — the ones
after which the business is meaningfully in a new phase, and where a vertical
divider would go on the wall (`Order confirmed`, `Goods dispatched`). They
usually sit exactly on the seams between **candidate bounded contexts**, so
propose the contexts from those seams and from event clustering, clearly labelled
as candidates.

### Step 6 — Write the facilitator's hand-off

This is what turns a generated list into session fuel. Keep it specific:

- **Assumptions to confirm** — every actor, ordering, or event name you invented.
- **Questions for the room** — the deliberately uncertain spots, phrased as
  questions ("does the reservation really expire, or does someone chase it?").
- **Hotspots** — contradictions in the description, failure events with no
  handling path, steps nobody appears to own, terms used in two senses.

## Output template

Use this structure, dropping sections the chosen granularity doesn't reach:

```
# Seed domain events — <process / domain>

## Scope
The process as understood, its trigger and outcome, the granularity chosen and why, and the inputs used.

## Event timeline
1. <Event in past tense>
2. …
   (grouped by candidate context once there are enough events to group; pivotal events marked ⟂)

## Alternative & failure events
Rejections, expiries, reversals — each as its own past-tense fact, attached to the step it branches from.

## Slices                    (Process-level and below)
| # | Actor / Policy | Command | Aggregate | Event | Read model |

## Candidate bounded contexts
Each cluster, the events it owns, and why the seam falls there. Marked as candidates.

## For the session
- Assumptions to confirm: …
- Questions for the room: …
- Hotspots: …
```

## Quality checks before handing it over

- Every event is **past tense** and could be said aloud to a business person
  without embarrassment. No imperatives (those are commands), no gerunds.
- No CRUD names, no UI names, no technical names — or if one survived, it is
  flagged as a hotspot with the question "what actually happened here?".
- The granularity is **uniform** — no single event that swallows five others, no
  five events that should be one.
- Failure, timeout, and reversal events are present. If the timeline is pure
  happy path, Step 3 didn't run.
- Commands, aggregates, policies, and contexts are labelled **inferred**;
  nothing is dressed up as a finding from the description.
- The count is honest for the flavour — an 8-event Big Picture seed is finished,
  not lazy.

## Handing off

The seed is designed to feed the rest of the toolkit; offer the natural next step
rather than doing it unasked:

- **Run the workshop, photograph the wall** → `event-storming-interpreter` turns
  the corrected board into a buildable brief.
- **Same process, one concrete narrated scenario** → `domain-story-seeder` for
  the actor → activity → work object telling of it.
- **Publish the events as a contract** → `asyncapi-spec-author` for channels and
  message schemas.
- **Pin down the nouns first** → the visual glossary skills, once the timeline
  has surfaced the vocabulary.

## References

- `references/worked-examples.md` — three fully worked seeds (a Big Picture seed
  from three sentences, a Process-level seed with the full slice table from a
  bulleted description, and a design-level aggregate lifecycle), plus a naming
  clinic of before/after event names. Read it to pattern-match how a thin
  description becomes a spine, how the six hiding places generate the
  alternatives, and how commands and aggregates get derived without invention.